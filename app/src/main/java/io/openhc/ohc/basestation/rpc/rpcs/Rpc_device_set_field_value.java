package io.openhc.ohc.basestation.rpc.rpcs;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Level;

import io.openhc.ohc.basestation.Basestation;
import io.openhc.ohc.basestation.rpc.Rpc_group;
import io.openhc.ohc.skynet.transaction.Transaction_generator;

/**
 * This RPC sets the value of a remote field on the basestation.
 *
 * @author Tobias Schramm
 */
public class Rpc_device_set_field_value extends Rpc
{
	private String id;
	private int field_id;
	private Object value;

	public final String RPC_METHOD = "device_set_field_value";
	public final String RPC_ATTRIBUTE_ID = "device_id";
	public final String RPC_ATTRIBUTE_FIELD_ID = "field_id";
	public final String RPC_ATTRIBUTE_VALUE = "value";

	public Rpc_device_set_field_value(Basestation bs, Rpc_group group)
	{
		this(bs, group, null, -1, null);
	}

	public Rpc_device_set_field_value(Basestation bs, Rpc_group group, String id, int field_id,
	                                  Object value)
	{
		super(bs, group);
		this.id = id;
		this.field_id = field_id;
		this.value = value;
	}

	@Override
	protected JSONObject get_json()
	{
		JSONObject json = new JSONObject();
		try
		{
			json.put(this.RPC_ATTRIBUTE_METHOD, this.RPC_METHOD)
					.put(RPC_ATTRIBUTE_ID, this.id)
					.put(RPC_ATTRIBUTE_FIELD_ID, this.field_id)
					.put(RPC_ATTRIBUTE_VALUE, this.value);
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
	 * Sets the numerical field id
	 *
	 * @param field_id Numeric field id
	 */
	public void set_field_id(int field_id)
	{
		this.field_id = field_id;
	}

	/**
	 * Sets the value of the field
	 *
	 * @param value Field value
	 */
	public void set_field_value(Object value)
	{
		this.value = value;
	}

	@Override
	protected void process_response(JSONObject response) throws JSONException
	{

	}
}
