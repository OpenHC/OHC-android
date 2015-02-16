package io.openhc.ohc.basestation.rpc.rpcs;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Level;

import io.openhc.ohc.basestation.Basestation;
import io.openhc.ohc.basestation.rpc.Rpc_group;

/**
 * This RPC sets the human readable name of a device
 *
 * @author Tobias Schramm
 */

public class Rpc_set_device_name extends Rpc
{
	private String id;
	private String name;

	public final String RPC_METHOD = "set_device_name";
	public final String RPC_ATTRIBUTE_ID = "id";
	public final String RPC_ATTRIBUTE_NAME = "name";

	public Rpc_set_device_name(Basestation bs, Rpc_group group)
	{
		this(bs, group, null, null);
	}

	public Rpc_set_device_name(Basestation bs, Rpc_group group, String id, String name)
	{
		super(bs, group);
		this.id = id;
		this.name = name;
	}

	@Override
	protected JSONObject get_json()
	{
		JSONObject json = new JSONObject();
		try
		{
			json.put(this.RPC_ATTRIBUTE_METHOD, this.RPC_METHOD)
					.put(RPC_ATTRIBUTE_ID, this.id)
					.put(RPC_ATTRIBUTE_NAME, this.name);
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

	/**
	 * Sets the human readable device name
	 *
	 * @param name Device name
	 */
	public void set_name(String name)
	{
		this.name = name;
	}

	@Override
	protected void process_response(JSONObject response) throws JSONException
	{

	}
}
