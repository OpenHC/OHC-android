package io.openhc.ohc.basestation.rpc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.openhc.ohc.OHC;
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
	 * Executes all stored RPCs
	 */
	public void run()
	{
		this.rpc_iterator = this.rpcs.iterator();
		if(this.rpc_iterator.hasNext())
			this.send_rpc(this.rpc_iterator.next());
	}

	protected void send_rpc(Rpc rpc)
	{
		Transaction_generator.Transaction transaction = rpc.get_transaction();
	}

	@Override
	public void on_receive_transaction(Transaction_generator.Transaction transaction)
	{
		if(this.has_finished() && this.callback != null)
			this.callback.on_group_finish(this);
		else
			if(this.mode == RPC_GROUP_MODE.SERIAL && this.rpc_iterator.hasNext())
				this.send_rpc(this.rpc_iterator.next());
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
