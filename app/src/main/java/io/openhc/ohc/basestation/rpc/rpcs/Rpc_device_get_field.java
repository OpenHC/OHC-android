package io.openhc.ohc.basestation.rpc.rpcs;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Level;

import io.openhc.ohc.basestation.Basestation;
import io.openhc.ohc.basestation.device.Field;
import io.openhc.ohc.basestation.rpc.Rpc_group;
import io.openhc.ohc.skynet.transaction.Transaction_generator;

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
	public final String RPC_ATTRIBUTE_DEVICE_ID = "device_id";
	public final String RPC_ATTRIBUTE_FIELD_ID = "field_id";
	public final String RPC_ATTRIBUTE_FIELD = "field";
	public final String RPC_ATTRIBUTE_NAME = "name";
	public final String RPC_ATTRIBUTE_TYPE = "type";
	public final String RPC_ATTRIBUTE_VALUE = "value";
	public final String RPC_ATTRIBUTE_MAX_VALUE = "max_value";
	public final String RPC_ATTRIBUTE_MIN_VALUE = "min_value";
	public final String RPC_ATTRIBUTE_WRITABLE = "writable";

	public Rpc_device_get_field(Basestation bs)
	{
		this(bs, null);
	}

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
	protected JSONObject get_json()
	{
		JSONObject json = new JSONObject();
		try
		{
			json.put(this.RPC_ATTRIBUTE_SESSION_TOKEN, this.session_token)
					.put(this.RPC_ATTRIBUTE_METHOD, this.RPC_METHOD)
					.put(RPC_ATTRIBUTE_DEVICE_ID, this.id)
					.put(RPC_ATTRIBUTE_FIELD_ID, this.field_id);
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

	@Override
	protected void process_response(JSONObject response) throws JSONException
	{
		String id = response.getString(RPC_ATTRIBUTE_DEVICE_ID);
		int field_id = response.getInt(RPC_ATTRIBUTE_FIELD_ID);
		try
		{
			JSONObject field_json = response.getJSONObject(RPC_ATTRIBUTE_FIELD);
			String name = field_json.getString(RPC_ATTRIBUTE_NAME);
			String type = field_json.getString(RPC_ATTRIBUTE_TYPE);
			Object value = field_json.get(RPC_ATTRIBUTE_VALUE);
			double max_value = field_json.getDouble(RPC_ATTRIBUTE_MAX_VALUE);
			double min_value = field_json.getDouble(RPC_ATTRIBUTE_MIN_VALUE);
			boolean writable = field_json.getBoolean(RPC_ATTRIBUTE_WRITABLE);
			Field.Type data_type = Field.Type.valueOf(type.toUpperCase());
			Field field = new Field(this.station.ohc, id, field_id, data_type, name, min_value, max_value, writable, value);
			this.station.device_set_field(id, field_id, field);
		}
		catch(Exception ex)
		{
			this.station.ohc.logger.log(Level.WARNING, "Received invalid field configuration: " +
					ex.getMessage(), ex);
			//Prevent crashes due to invalid RPC data; create inaccessible field
			Field field = new Field();
			field.set_accessible(false);
			this.station.device_set_field(id, field_id, field);
		}
	}
}
