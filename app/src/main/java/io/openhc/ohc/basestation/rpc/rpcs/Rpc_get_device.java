package io.openhc.ohc.basestation.rpc.rpcs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import io.openhc.ohc.basestation.Basestation;
import io.openhc.ohc.basestation.device.Device;
import io.openhc.ohc.basestation.device.Field;
import io.openhc.ohc.basestation.rpc.Rpc_group;

/**
 * This RPC returns a whole device object including all fields
 *
 * @author Tobias Schramm
 */
public class Rpc_get_device extends Rpc
{
	private String id;

	public final String RPC_METHOD = "get_device_by_id";
	public final String RPC_ATTRIBUTE_ID = "id";
	public final String RPC_ATTRIBUTE_NUM_FIELDS = "num_fields";
	public final String RPC_ATTRIBUTE_NAME = "name";
	public final String RPC_ATTRIBUTE_FIELDS = "fields";
	public final String RPC_ATTRIBUTE_FIELD_ID = "id";
	public final String RPC_ATTRIBUTE_DEVICE = "device";
	public final String RPC_ATTRIBUTE_TYPE = "type";
	public final String RPC_ATTRIBUTE_VALUE = "value";
	public final String RPC_ATTRIBUTE_MAX_VALUE = "max_value";
	public final String RPC_ATTRIBUTE_MIN_VALUE = "min_value";
	public final String RPC_ATTRIBUTE_WRITABLE = "writable";

	public Rpc_get_device(Basestation bs)
	{
		this(bs, null);
	}

	public Rpc_get_device(Basestation bs, Rpc_group group)
	{
		this(bs, group, null);
	}

	public Rpc_get_device(Basestation bs, Rpc_group group, String id)
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
		JSONObject dev_json = response.getJSONObject(RPC_ATTRIBUTE_DEVICE);
		Device dev = new Device(dev_json.getString(RPC_ATTRIBUTE_NAME), this.id);
		int num_fields = dev_json.getInt(RPC_ATTRIBUTE_NUM_FIELDS);
		JSONArray fields_json = dev_json.getJSONArray(RPC_ATTRIBUTE_FIELDS);
		for(int i = 0; i < fields_json.length(); i++)
		{
			JSONObject field_json = fields_json.getJSONObject(i);
			int field_id = field_json.getInt(RPC_ATTRIBUTE_FIELD_ID);
			try
			{
				String name = field_json.getString(RPC_ATTRIBUTE_NAME);
				String type = field_json.getString(RPC_ATTRIBUTE_TYPE);
				Object value = field_json.get(RPC_ATTRIBUTE_VALUE);
				double max_value = field_json.getDouble(RPC_ATTRIBUTE_MAX_VALUE);
				double min_value = field_json.getDouble(RPC_ATTRIBUTE_MIN_VALUE);
				boolean writable = field_json.getBoolean(RPC_ATTRIBUTE_WRITABLE);
				Field.Type data_type = Field.Type.valueOf(type.toUpperCase());
				dev.add_field(field_id, new Field(this.station.ohc, id, field_id, data_type, name, min_value, max_value, writable, value));
			}
			catch(Exception ex)
			{
				this.station.ohc.logger.log(Level.WARNING, "Received invalid field configuration: " +
						ex.getMessage(), ex);
				//Prevent crashes due to invalid RPC data; create inaccessible field
				Field field = new Field();
				field.set_accessible(false);
				dev.add_field(field_id, field);
			}
		}
		this.station.add_device(dev);
	}
}
