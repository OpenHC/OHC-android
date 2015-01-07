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
			this.station.set_session_token(token);
		}
		catch (Exception ex)
		{
			OHC.logger.log(Level.WARNING, "Received invalid login token configuration: " + ex.getMessage(), ex);
		}
	}
}
