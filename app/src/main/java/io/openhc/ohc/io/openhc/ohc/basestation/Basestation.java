package io.openhc.ohc.io.openhc.ohc.basestation;

import android.content.Context;

import org.json.JSONObject;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.logging.Level;

import io.openhc.ohc.OHC;
import io.openhc.ohc.OHC_login;
import io.openhc.ohc.io.openhc.ohc.basestation.io.openhc.ohc.basestation.rpc.Base_rpc;
import io.openhc.ohc.io.openhc.ohc.network.Network;

public class Basestation
{
	private Network network;
	private OHC_login login_form;
	private Base_rpc rpc_interface;

	private InetSocketAddress endpoint_address;

	public Basestation(OHC_login login_form)
	{
		this.network = OHC.network;
		this.login_form = login_form;
		this.rpc_interface = new Base_rpc(this);
	}

	public OHC_login get_context()
	{
		return this.login_form;
	}

	public void update_address(InetSocketAddress addr)
	{
		this.login_form.update_network_status(addr != null);
		this.endpoint_address = addr;
		OHC.logger.log(Level.INFO , String.format("Endpoint address updated: %s:%s", addr.getAddress().getHostAddress(), Integer.toString(addr.getPort())));
	}

	public void handle_packet(JSONObject packet)
	{
		try
		{
			String method = packet.getString("method");
			this.rpc_interface.getClass().getMethod(method, JSONObject.class).invoke(this.rpc_interface, packet);
		}
		catch(Exception ex)
		{
			OHC.logger.log(Level.SEVERE, "JSON encoded data is missing valid rpc data: " + ex.getMessage());
		}
	}
}
