package io.openhc.ohc.basestation.rpc.rpcs;

import org.json.JSONObject;

import io.openhc.ohc.R;
import io.openhc.ohc.basestation.Basestation;
import io.openhc.ohc.basestation.rpc.Rpc_group;

public abstract class Rpc
{
	protected boolean finished;
	protected final Basestation station;
	protected final Rpc_group group;

	public final String RPC_ATTRIBUTE_METHOD;
	public final String RPC_ATTRIBUTE_SESSION_TOKEN;
	public final String RPC_REQUEST_KEY;
	public final String RPC_RESPONSE_KEY;

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

	public boolean has_finished()
	{
		return this.finished;
	}

	public abstract JSONObject get_json();
}
