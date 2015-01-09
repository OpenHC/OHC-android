package io.openhc.ohc.basestation.rpc;

import org.json.JSONObject;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.logging.Level;

import io.openhc.ohc.OHC;
import io.openhc.ohc.basestation.Basestation;

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
		catch (Exception ex)
		{
			OHC.logger.log(Level.WARNING, "Received invalid ip address configuration: " + ex.getMessage(), ex);
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
		catch (Exception ex)
		{
			OHC.logger.log(Level.WARNING, "Received invalid login token configuration: " + ex.getMessage(), ex);
		}
	}

	public void set_num_devices(JSONObject object)
	{
		try
		{
			int num_devices = object.getInt("num_devices");
			this.station.set_num_devices(num_devices);
		}
		catch (Exception ex)
		{
			OHC.logger.log(Level.WARNING, "Received invalid rpc response for set_num_devices: " + ex.getMessage(), ex);
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
		catch (Exception ex)
		{
			OHC.logger.log(Level.WARNING, "Received invalid rpc response for set_device_id: " + ex.getMessage(), ex);
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
		catch (Exception ex)
		{
			OHC.logger.log(Level.WARNING, "Received invalid rpc response for set_device_name: " + ex.getMessage(), ex);
		}
	}
}
