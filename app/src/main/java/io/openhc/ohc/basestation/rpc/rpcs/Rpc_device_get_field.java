package io.openhc.ohc.basestation.rpc.rpcs;

import org.json.JSONObject;

import java.util.logging.Level;

import io.openhc.ohc.basestation.Basestation;
import io.openhc.ohc.basestation.rpc.Rpc_group;

/**
 * This RPC requests information about a field from the basestation. The basestation returns
 * all information required to construct an instance of Field.class
 *
 * @author Tobias Schramm
 */
public class Rpc_device_get_field extends Rpc
{
	private String id;
	private int field_id;

	public final String RPC_METHOD = "device_get_field";
	public final String RPC_ATTRIBUTE_ID = "device_id";
	public final String RPC_ATTRIBUTE_FIELD_ID = "field_id";

	public Rpc_device_get_field(Basestation bs, Rpc_group group)
	{
		this(bs, group, null, -1);
	}

	public Rpc_device_get_field(Basestation bs, Rpc_group group, String id, int field_id)
	{
		super(bs, group);
		this.id = id;
		this.field_id = field_id;
	}

	@Override
	public JSONObject get_json()
	{
		JSONObject json = new JSONObject();
		try
		{
			json.put(this.RPC_ATTRIBUTE_METHOD, this.RPC_METHOD)
					.put(RPC_ATTRIBUTE_ID, this.id)
					.put(RPC_ATTRIBUTE_FIELD_ID, this.field_id);
		}
		catch(Exception ex)
		{
			this.station.ohc.logger.log(Level.SEVERE, "Failed to compose JSON: " + ex.getMessage(), ex);
		}
		return json;
	}

	/**
	 * @param id
	 */
	public void set_id(String id)
	{
		this.id = id;
	}

	public void set_field_id(int field_id)
	{
		this.field_id = field_id;
	}
}
