package io.openhc.ohc.basestation.rpc.rpcs;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Level;

import io.openhc.ohc.R;
import io.openhc.ohc.basestation.Basestation;
import io.openhc.ohc.basestation.rpc.Rpc_group;
import io.openhc.ohc.skynet.Sender;
import io.openhc.ohc.skynet.transaction.Transaction_generator;

/**
 * Abstract parent for RPCs allowing for easy implementation of new RPCs
 *
 * @author Tobias Schramm
 */
public abstract class Rpc implements Transaction_generator.Transaction_receiver
{
	protected boolean finished;
	protected final Basestation station;
	protected Rpc_group group;
	protected String session_token;
	private Transaction_generator.Transaction transaction;

	public final String RPC_ATTRIBUTE_METHOD;
	public final String RPC_ATTRIBUTE_SESSION_TOKEN;
	public final String RPC_REQUEST_KEY;
	public final String RPC_RESPONSE_KEY;

	/**
	 * Default constructor.
	 *
	 * @param bs    Linked basestation instance
	 */
	public Rpc(Basestation bs)
	{
		this(bs, null);
	}

	/**
	 * Default constructor. The rpc group defines an action to perform when the execution of the rpc
	 * finishes
	 *
	 * @param bs    Linked basestation instance
	 * @param group Rpc group for callback handling
	 */
	public Rpc(Basestation bs, Rpc_group group)
	{
		this.station = bs;
		this.group = group;
		this.RPC_ATTRIBUTE_METHOD = bs.get_resources().getString(R.string.ohc_rpc_attribute_method);
		this.RPC_ATTRIBUTE_SESSION_TOKEN = bs.get_resources().getString(
				R.string.ohc_rpc_attribute_session_token);
		this.RPC_REQUEST_KEY = bs.get_resources().getString(R.string.ohc_rpc_request_key);
		this.RPC_RESPONSE_KEY = bs.get_resources().getString(R.string.ohc_rpc_response_key);
		this.transaction = this.station.transaction_gen
				.generate_transaction(new JSONObject());
	}

	/**
	 * Indicates if this rpc finished
	 *
	 * @return Has the rpc finished
	 */
	public boolean has_finished()
	{
		return this.finished;
	}

	/**
	 * Sets the current session token
	 *
	 * @param session_token The current session token
	 */
	public void set_session_token(String session_token)
	{
		this.session_token = session_token;
	}

	/**
	 * Returns a JSON representation of the rpc data
	 *
	 * @return A JSON representation of the rpc data
	 */
	protected abstract JSONObject get_json();

	/**
	 * Returns the transaction UUID
	 *
	 * @return Transaction UUID
	 */
	public String get_transaction_uuid()
	{
		return this.transaction.get_uuid();
	}

	/**
	 * Method stub. Subclases can use this to handle responses
	 *
	 * @throws Exception
	 */
	protected void process_response(JSONObject response) throws Exception
	{

	}

	/**
	 * Sets the group of this RPC
	 *
	 * @param group The group
	 */
	public void set_group(Rpc_group group)
	{
		this.group = group;
	}

	/**
	 * Generates a transaction containing the json rpc represented by this class
	 *
	 * @return TX-Ready transaction
	 */
	public Transaction_generator.Transaction get_transaction() throws JSONException
	{
		this.transaction.set_json(this.get_json());
		return transaction;
	}

	@Override
	public void on_receive_transaction(Transaction_generator.Transaction transaction)
	{
		JSONObject response = transaction.get_response();
		if(response != null)
		{
			try
			{
				this.process_response(response);
			}
			catch(Exception ex)
			{
				this.station.ohc.logger.log(Level.WARNING,
						"Received invalid JSON response from basestation", ex);
			}
		}
		if(!this.group.do_bundle_requests())
			this.group.on_receive_transaction(transaction);
	}
}
