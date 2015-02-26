package io.openhc.ohc.basestation.rpc.rpcs;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Level;

import io.openhc.ohc.basestation.Basestation;
import io.openhc.ohc.basestation.rpc.Rpc_group;
import io.openhc.ohc.skynet.transaction.Transaction_generator;

/**
 * This RPC returns the internal alphanumerical id of a device
 *
 * @author Tobias Schramm
 */
public class Rpc_get_device_id extends Rpc
{
	private int index;

	public final String RPC_METHOD = "get_device_id";
	public final String RPC_ATTRIBUTE_INDEX = "index";
	public final String RPC_ATTRIBUTE_ID = "id";

	public Rpc_get_device_id(Basestation bs)
	{
		this(bs, null);
	}

	public Rpc_get_device_id(Basestation bs, Rpc_group group)
	{
		this(bs, group, -1);
	}

	public Rpc_get_device_id(Basestation bs, Rpc_group group, int index)
	{
		super(bs, group);
		this.index = index;
	}

	@Override
	protected JSONObject get_json()
	{
		JSONObject json = new JSONObject();
		try
		{
			json.put(this.RPC_ATTRIBUTE_SESSION_TOKEN, this.session_token)
					.put(this.RPC_ATTRIBUTE_METHOD, this.RPC_METHOD)
					.put(RPC_ATTRIBUTE_INDEX, this.index);
		}
		catch(Exception ex)
		{
			this.station.ohc.logger.log(Level.SEVERE, "Failed to compose JSON: " + ex.getMessage(), ex);
		}
		return json;
	}

	/**
	 * Sets the index of the device
	 *
	 * @param index Device index
	 */
	public void set_index(int index)
	{
		this.index = index;
	}

	@Override
	protected void process_response(JSONObject response) throws JSONException
	{
		String id = response.getString(RPC_ATTRIBUTE_ID);
		int index = response.getInt(RPC_ATTRIBUTE_INDEX);
		this.station.set_device_id(index, id);
	}
}
