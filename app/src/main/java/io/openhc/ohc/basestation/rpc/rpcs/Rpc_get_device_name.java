package io.openhc.ohc.basestation.rpc.rpcs;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Level;

import io.openhc.ohc.basestation.Basestation;
import io.openhc.ohc.basestation.rpc.Rpc_group;
import io.openhc.ohc.skynet.transaction.Transaction_generator;

/**
 * This RPC returns the human readable name of a device
 *
 * @author Tobias Schramm
 */
public class Rpc_get_device_name extends Rpc
{
	private String id;

	public final String RPC_METHOD = "get_device_name";
	public final String RPC_ATTRIBUTE_ID = "id";
	public final String RPC_ATTRIBUTE_NAME = "name";

	public Rpc_get_device_name(Basestation bs, Rpc_group group)
	{
		this(bs, group, null);
	}

	public Rpc_get_device_name(Basestation bs, Rpc_group group, String id)
	{
		super(bs, group);
		this.id = id;
	}

	@Override
	protected JSONObject get_json()
	{
		JSONObject json = new JSONObject();
		try
		{
			json.put(this.RPC_ATTRIBUTE_SESSION_TOKEN, this.session_token)
					.put(this.RPC_ATTRIBUTE_METHOD, this.RPC_METHOD)
					.put(RPC_ATTRIBUTE_ID, this.id);
		}
		catch(Exception ex)
		{
			this.station.ohc.logger.log(Level.SEVERE, "Failed to compose JSON: " + ex.getMessage(), ex);
		}
		return json;
	}

	/**
	 * Sets the internal device id
	 *
	 * @param id Internal device id
	 */
	public void set_id(String id)
	{
		this.id = id;
	}

	@Override
	protected void process_response(JSONObject response) throws JSONException
	{
		String id = response.getString(RPC_ATTRIBUTE_ID);
		String name = response.getString(RPC_ATTRIBUTE_NAME);
		this.station.set_device_name(id, name);
	}
}
