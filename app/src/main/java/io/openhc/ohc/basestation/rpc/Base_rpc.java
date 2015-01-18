package io.openhc.ohc.basestation.rpc;

import org.json.JSONObject;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.logging.Level;

import io.openhc.ohc.OHC;
import io.openhc.ohc.basestation.device.Field;

/* As a security measure this class is dedicated to RPCs. By only allowing RPCs to functions in
 * this class a potential attacker is prevented from gaining access to any internal functions*/
public class Base_rpc
{
	private OHC ohc;

	public Base_rpc(OHC ohc)
	{
		this.ohc = ohc;
	}

	/**
	 * Sets ip address and port of the Baestation (Gateway / OHC-Node)
	 *
	 * @param object The RPC data
	 */
	public void set_ip_address(JSONObject object)
	{
		try
		{
			InetAddress addr = InetAddress.getByName(object.getString("ip_address"));
			int port = object.getInt("port");
			this.ohc.get_basestation().update_endpoint(new InetSocketAddress(addr, port));
		}
		catch(Exception ex)
		{
			this.ohc.logger.log(Level.WARNING, "Received invalid ip address configuration: " +
					ex.getMessage(), ex);
		}
	}

	/**
	 * Sets the session token for this user frontend device
	 *
	 * @param object The RPC data
	 */
	public void set_session_token(JSONObject object)
	{
		try
		{
			String token = object.getString("session_token");
			boolean success = object.getBoolean("success");
			this.ohc.get_basestation().set_session_token(token, success);
		}
		catch(Exception ex)
		{
			this.ohc.logger.log(Level.WARNING, "Received invalid login token configuration: " +
					ex.getMessage(), ex);
		}
	}

	/**
	 * Sets the number of devices accessible to this user frontend device
	 *
	 * @param object The RPC data
	 */
	public void set_num_devices(JSONObject object)
	{
		try
		{
			int num_devices = object.getInt("num_devices");
			this.ohc.get_basestation().set_num_devices(num_devices);
		}
		catch(Exception ex)
		{
			this.ohc.logger.log(Level.WARNING, "Received invalid rpc response for set_num_devices: " +
					ex.getMessage(), ex);
		}
	}

	/**
	 * Sets the id of a device
	 *
	 * @param object The RPC data
	 */
	public void set_device_id(JSONObject object)
	{
		try
		{
			String id = object.getString("id");
			int index = object.getInt("index");
			this.ohc.get_basestation().set_device_id(index, id);
		}
		catch(Exception ex)
		{
			this.ohc.logger.log(Level.WARNING, "Received invalid rpc response for set_device_id: " +
					ex.getMessage(), ex);
		}
	}

	/**
	 * Sets the name of a device
	 *
	 * @param object The RPC data
	 */
	public void set_device_name(JSONObject object)
	{
		try
		{
			String id = object.getString("id");
			String name = object.getString("name");
			this.ohc.get_basestation().set_device_name(id, name);
		}
		catch(Exception ex)
		{
			this.ohc.logger.log(Level.WARNING, "Received invalid rpc response for set_device_name: " +
					ex.getMessage(), ex);
		}
	}

	/**
	 * Sets the number of fields the specified device offers
	 *
	 * @param object The RPC data
	 */
	public void device_set_num_fields(JSONObject object)
	{
		try
		{
			String id = object.getString("id");
			int num_fields = object.getInt("num_fields");
			this.ohc.get_basestation().device_set_num_fields(id, num_fields);
		}
		catch(Exception ex)
		{
			this.ohc.logger.log(Level.WARNING, "Received invalid rpc response for device_set_num_fields: " +
					ex.getMessage(), ex);
		}
	}

	/**
	 * Sets the field at the given index on the specified device
	 *
	 * @param object The RPC data
	 */
	public void device_set_field(JSONObject object)
	{
		try
		{
			String id = object.getString("device_id");
			int field_id = object.getInt("field_id");
			try
			{
				JSONObject field_json = object.getJSONObject("field");
				String name = field_json.getString("name");
				String type = field_json.getString("type");
				Object value = field_json.get("value");
				double max_value = field_json.getDouble("max_value");
				double min_value = field_json.getDouble("min_value");
				boolean writable = field_json.getBoolean("writable");
				Field.Type data_type = Field.Type.valueOf(type.toUpperCase());
				Field field = new Field(this.ohc, id, field_id, data_type, name, min_value, max_value, writable, value);
				this.ohc.get_basestation().device_set_field(id, field_id, field);
			}
			catch(Exception ex)
			{
				this.ohc.logger.log(Level.WARNING, "Received invalid field configuration: " +
						ex.getMessage(), ex);
				//Prevent crashes due to invalid RPC data; create inaccessible field
				Field field = new Field();
				field.set_accessible(false);
				this.ohc.get_basestation().device_set_field(id, field_id, field);
			}
		}
		catch(Exception ex)
		{
			this.ohc.logger.log(Level.WARNING, "Received invalid rpc response for device_set_field: " +
					ex.getMessage(), ex);
		}
	}
}
