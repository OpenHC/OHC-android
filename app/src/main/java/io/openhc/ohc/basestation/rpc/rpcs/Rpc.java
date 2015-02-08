package io.openhc.ohc.basestation.rpc.rpcs;

import org.json.JSONObject;

import io.openhc.ohc.R;
import io.openhc.ohc.basestation.Basestation;
import io.openhc.ohc.basestation.rpc.Rpc_group;
import io.openhc.ohc.skynet.Sender;

/**
 * Abstract parent for RPCs allowing for easy implementation of new RPCs
 *
 * @author Tobias Schramm
 */
public abstract class Rpc implements Sender.Transaction_receiver
{
	protected boolean finished;
	protected final Basestation station;
	protected final Rpc_group group;
	protected String session_token;

	public final String RPC_ATTRIBUTE_METHOD;
	public final String RPC_ATTRIBUTE_SESSION_TOKEN;
	public final String RPC_REQUEST_KEY;
	public final String RPC_RESPONSE_KEY;

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
	public abstract JSONObject get_json();
}
