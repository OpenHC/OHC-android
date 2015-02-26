package io.openhc.ohc.basestation.rpc.rpcs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import io.openhc.ohc.basestation.Basestation;
import io.openhc.ohc.basestation.rpc.Rpc_group;

/**
 * This RPC returns the internal alphanumerical ids of all devices
 *
 * @author Tobias Schramm
 */
public class Rpc_get_device_ids extends Rpc
{
	public final String RPC_METHOD = "get_device_ids";
	public final String RPC_ATTRIBUTE_IDS = "ids";

	public Rpc_get_device_ids(Basestation bs)
	{
		super(bs, null);
	}

	public Rpc_get_device_ids(Basestation bs, Rpc_group group)
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
		JSONArray json_ids = response.getJSONArray(RPC_ATTRIBUTE_IDS);
		List<String> ids = new ArrayList<>();
		for(int i= 0; i < json_ids.length(); i++)
			ids.add(json_ids.getString(i));
		this.station.set_device_ids(ids);
	}
}
