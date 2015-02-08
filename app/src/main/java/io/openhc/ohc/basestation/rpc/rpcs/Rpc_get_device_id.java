package io.openhc.ohc.basestation.rpc.rpcs;

import org.json.JSONObject;

import java.util.logging.Level;

import io.openhc.ohc.basestation.Basestation;
import io.openhc.ohc.basestation.rpc.Rpc_group;

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
	public JSONObject get_json()
	{
		JSONObject json = new JSONObject();
		try
		{
			json.put(this.RPC_ATTRIBUTE_METHOD, this.RPC_METHOD)
					.put(RPC_ATTRIBUTE_INDEX, this.index);
		}
		catch(Exception ex)
		{
			this.station.ohc.logger.log(Level.SEVERE, "Failed to compose JSON: " + ex.getMessage(), ex);
		}
		return json;
	}

	public void set_index(int index)
	{
		this.index = index;
	}
}
