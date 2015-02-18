package io.openhc.ohc.basestation.rpc.rpcs;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Level;

import io.openhc.ohc.basestation.Basestation;
import io.openhc.ohc.basestation.rpc.Rpc_group;
import io.openhc.ohc.skynet.transaction.Transaction_generator;

/**
 * This RPC returns the number of devices attached to a basestation
 *
 * @author Tobias Schramm
 */
public class Rpc_get_num_devices extends Rpc
{
	public final String RPC_METHOD = "get_num_devices";
	public final String RPC_ATTRIBUTE_NUM_DEVICES = "num_devices";

	public Rpc_get_num_devices(Basestation bs, Rpc_group group)
	{
		super(bs, group);
	}

	@Override
	protected JSONObject get_json()
	{
		JSONObject json = new JSONObject();
		try
		{
			json.put(this.RPC_ATTRIBUTE_SESSION_TOKEN, this.session_token)
					.put(this.RPC_ATTRIBUTE_METHOD, this.RPC_METHOD);
		}
		catch(Exception ex)
		{
			this.station.ohc.logger.log(Level.SEVERE, "Failed to compose JSON: " + ex.getMessage(), ex);
		}
		return json;
	}

	@Override
	protected void process_response(JSONObject response) throws JSONException
	{
		int num_devices = response.getInt(RPC_ATTRIBUTE_NUM_DEVICES);
		this.station.set_num_devices(num_devices);
	}
}
