package io.openhc.ohc.basestation.rpc;

import org.json.JSONObject;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.logging.Level;

import io.openhc.ohc.OHC;
import io.openhc.ohc.basestation.Basestation;
import io.openhc.ohc.basestation.device.Field;

public class Base_rpc
{
	private final Basestation station;

	public Base_rpc(Basestation station)
	{
		this.station = station;
	}

	public void set_ip_address(JSONObject object)
	{
		try
		{
			InetAddress addr = InetAddress.getByName(object.getString("ip_address"));
			int port = object.getInt("port");
			this.station.update_endpoint(new InetSocketAddress(addr, port));
		}
		catch(Exception ex)
		{
			OHC.logger.log(Level.WARNING, "Received invalid ip address configuration: " +
					ex.getMessage(), ex);
		}
	}

	public void set_session_token(JSONObject object)
	{
		try
		{
			String token = object.getString("session_token");
			boolean success = object.getBoolean("success");
			this.station.set_session_token(token, success);
		}
		catch(Exception ex)
		{
			OHC.logger.log(Level.WARNING, "Received invalid login token configuration: " +
					ex.getMessage(), ex);
		}
	}

	public void set_num_devices(JSONObject object)
	{
		try
		{
			int num_devices = object.getInt("num_devices");
			this.station.set_num_devices(num_devices);
		}
		catch(Exception ex)
		{
			OHC.logger.log(Level.WARNING, "Received invalid rpc response for set_num_devices: " +
					ex.getMessage(), ex);
		}
	}

	public void set_device_id(JSONObject object)
	{
		try
		{
			String id = object.getString("id");
			int index = object.getInt("index");
			this.station.set_device_id(index, id);
		}
		catch(Exception ex)
		{
			OHC.logger.log(Level.WARNING, "Received invalid rpc response for set_device_id: " +
					ex.getMessage(), ex);
		}
	}

	public void set_device_name(JSONObject object)
	{
		try
		{
			String id = object.getString("id");
			String name = object.getString("name");
			this.station.set_device_name(id, name);
		}
		catch(Exception ex)
		{
			OHC.logger.log(Level.WARNING, "Received invalid rpc response for set_device_name: " +
					ex.getMessage(), ex);
		}
	}

	public void device_set_num_fields(JSONObject object)
	{
		try
		{
			String id = object.getString("id");
			int num_fields = object.getInt("num_fields");
			this.station.device_set_num_fields(id, num_fields);
		}
		catch(Exception ex)
		{
			OHC.logger.log(Level.WARNING, "Received invalid rpc response for device_set_num_fields: " +
					ex.getMessage(), ex);
		}
	}

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
				Field.Type data_type = Field.Type.valueOf(type);
				Field field = new Field(data_type, name, min_value, max_value, writable);
				this.station.device_set_field(id, field_id, field);
			}
			catch(Exception ex)
			{
				this.station.device_set_field(id, field_id, new Field());
			}
		}
		catch(Exception ex)
		{
			OHC.logger.log(Level.WARNING, "Received invalid rpc response for device_set_field: " +
					ex.getMessage(), ex);
		}
	}
}
