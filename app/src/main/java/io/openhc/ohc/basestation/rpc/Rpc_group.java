package io.openhc.ohc.basestation.rpc;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import io.openhc.ohc.OHC;
import io.openhc.ohc.R;
import io.openhc.ohc.basestation.Basestation;
import io.openhc.ohc.basestation.rpc.rpcs.Rpc;
import io.openhc.ohc.skynet.transaction.Transaction_generator;

/**
 * A RPC group handles the serial / parallel execution of RPCs and calls a callback whenever either
 * any of the associated RPCs or all of the RPCs finished execution
 *
 * @author Tobias Schramm
 */
public class Rpc_group implements Transaction_generator.Transaction_receiver
{
	private final Basestation station;
	private List<Rpc> rpcs;
	private Iterator<Rpc> rpc_iterator;
	private RPC_GROUP_MODE mode;
	private Rpc_group_callback callback;
	private boolean bundle_requests;
	private HashMap<String, Rpc> uuid_map;

	private final String RPC_RESPONSE_KEY;

	/**
	 * Default constructor.
	 *
	 * @param station Basestation instance
	 */
	public Rpc_group(Basestation station)
	{
		this(station, new ArrayList<Rpc>());
	}

	/**
	 * Specifies a list of RPCs to call on execution
	 *
	 * @param station Basestation instnace
	 * @param rpcs List of RPCs
	 */
	public Rpc_group(Basestation station, List<Rpc> rpcs)
	{
		this(station, rpcs, RPC_GROUP_MODE.SERIAL, null);
	}

	/**
	 * Specifies a list of RPCs to call on execution and a group mode to determine which condition
	 * must be met for the group to be finished
	 *
	 * @param station Basestation instance
	 * @param rpcs List of RPCs
	 */
	public Rpc_group(Basestation station, List<Rpc> rpcs, Rpc_group_callback callback)
	{
		this(station, rpcs, RPC_GROUP_MODE.SERIAL, callback);
	}

	/**
	 * Specifies a list of RPCs to call on execution and a group mode to determine which condition
	 * must be met for the group to be finished
	 *
	 * @param station Basestation instance
	 * @param rpcs List of RPCs
	 * @param mode Execution mode
	 * @param callback Callback
	 */
	public Rpc_group(Basestation station, List<Rpc> rpcs, RPC_GROUP_MODE mode, Rpc_group_callback callback)
	{
		this.station = station;
		this.rpcs = rpcs;
		this.callback = callback;
		this.mode = mode;
		this.bundle_requests = this.station.do_bundle_requests();
		this.RPC_RESPONSE_KEY = this.station.get_resources().getString(R.string.ohc_rpc_response_key);
	}

	/**
	 * Determines if this group has finished based on the group mode
	 *
	 * @return Has this group finished execution
	 */
	public boolean has_finished()
	{
		boolean finished = true;
		for(Rpc rpc : this.rpcs)
			finished = finished && rpc.has_finished();
		return finished;
	}

	/**
	 * Sets a session token for all contained RPCs
	 *
	 * @param token Session token
	 */
	public void set_session_token(String token)
	{
		for(Rpc rpc : this.rpcs)
			rpc.set_session_token(token);
	}

	/**
	 * Returns all associated RPCs
	 *
	 * @return RPCs
	 */
	public List<Rpc> get_rpcs()
	{
		return this.rpcs;
	}

	/**
	 * Adds one or more RPCs
	 *
	 * @param rpcs Rpcs
	 */
	public void add_rpcs(Rpc ... rpcs)
	{
		this.rpcs.addAll(Arrays.asList(rpcs));
	}

	/**
	 * Adds one or more RPCs
	 *
	 * @param rpcs RPCs
	 */
	public void add_rpcs(List<Rpc> rpcs)
	{
		this.rpcs.addAll(rpcs);
	}

	/**
	 * Updates the reference to this group for each RPC
	 */
	public void update_group_ref()
	{
		for(Rpc rpc : this.rpcs)
			rpc.set_group(this);
	}

	/**
	 * Maps all transaction UUIDs to their respective transaction
	 */
	private void create_uuid_map()
	{
		this.uuid_map = new HashMap<>();
		for(Rpc rpc : this.rpcs)
		{
			this.uuid_map.put(rpc.get_transaction_uuid(), rpc);
		}
	}

	/**
	 * Executes all stored RPCs
	 */
	public void run()
	{
		if(this.bundle_requests)
		{
			this.create_uuid_map();
			try
			{
				this.station.make_rpc_call(this);
			}
			catch(ProtocolException ex)
			{
				this.station.ohc.logger.log(Level.SEVERE, "Internal state exception", ex);
			}
			catch(JSONException ex)
			{
				this.station.ohc.logger.log(Level.SEVERE, "Internal exception. Malformed JSON " +
						"in bundled request", ex);
			}
		}
		else
		{
			this.rpc_iterator = this.rpcs.iterator();
			if(this.rpc_iterator.hasNext())
				this.send_rpc(this.rpc_iterator.next());
		}
	}

	/**
	 * Sends a single RPC via the basestation Sender interface
	 *
	 * @param rpc The RPC
	 */
	protected void send_rpc(Rpc rpc)
	{
		try
		{
			this.station.make_rpc_call(rpc);
		}
		catch(Exception ex)
		{
			this.station.ohc.logger.log(Level.SEVERE,
					"Failed to assemble RPC request: ", ex);
		}
	}

	/**
	 * Returns whether requests should be bundled in single network transmissions
	 *
	 * @return Bundle requests
	 */
	public boolean do_bundle_requests()
	{
		return this.bundle_requests;
	}

	@Override
	public void on_receive_transaction(Transaction_generator.Transaction transaction)
	{
		if(this.bundle_requests)
		{
			try
			{
				JSONObject response = transaction.get_response();
				JSONArray responses = response.getJSONArray(RPC_RESPONSE_KEY);
				for(int i = 0; i < responses.length(); i++)
				{
					JSONObject json = responses.getJSONObject(i);
					String uuid = json.getString(transaction.UUID_KEY);
					Rpc rpc = this.uuid_map.get(uuid);
					if(rpc == null)
					{
						this.station.ohc.logger.log(Level.WARNING, String.format(
								"No RPC for UUID '%s' found", uuid));
						continue;
					}
					rpc.get_transaction().set_response(json);
					rpc.on_receive_transaction(rpc.get_transaction());
				}
			}
			catch(Exception ex)
			{
				this.station.ohc.logger.log(Level.WARNING,
						"Failed to parse bundled response: " + transaction.get_response().toString(), ex);
			}
		}
		else
		{
			if(this.has_finished() && this.callback != null)
				this.callback.on_group_finish(this);
			else if(this.mode == RPC_GROUP_MODE.SERIAL && this.rpc_iterator.hasNext())
				this.send_rpc(this.rpc_iterator.next());
		}
	}

	/**
	 * Contains all supported RPC group modes
	 */
	public enum RPC_GROUP_MODE
	{
		SERIAL,
		PARALLEL;
	}

	/**
	 * Interface for callbacks
	 */
	public interface Rpc_group_callback
	{
		/**
		 * Callback function
		 *
		 * @param group The RPC group that finished execution
		 */
		public void on_group_finish(Rpc_group group);
	}
}
