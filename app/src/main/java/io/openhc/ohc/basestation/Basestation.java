package io.openhc.ohc.basestation;

import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.util.logging.Level;

import io.openhc.ohc.OHC;
import io.openhc.ohc.R;
import io.openhc.ohc.basestation.rpc.Base_rpc;
import io.openhc.ohc.skynet.Network;
import io.openhc.ohc.skynet.Sender;

public class Basestation
{
	private Network network;
	private OHC ohc;
	private Base_rpc rpc_interface;
	private Sender sender;

	private InetSocketAddress endpoint_address;
	private String login_token = null;


	public Basestation(OHC ohc)
	{
		this.network = ohc.network;
		this.ohc = ohc;
		this.rpc_interface = new Base_rpc(this);
	}

	//Code being called from Base_rpc
	public void update_endpoint(InetSocketAddress addr)
	{
		this.ohc.get_context().update_network_status(addr != null);
		this.ohc.get_context().set_status(this.ohc.get_context().getString(R.string.status_found) + addr.getHostString());
		this.endpoint_address = addr;
		OHC.logger.log(Level.INFO , String.format("Endpoint address updated: %s:%s", addr.getAddress().getHostAddress(), Integer.toString(addr.getPort())));
		this.sender = new Sender(this.endpoint_address);
	}

	public void set_login_token(String token)
	{
		this.login_token = token;
	}

	//Dynamic calls to Base_rpc depending on the received JSON data
	public void handle_packet(JSONObject packet)
	{
		try
		{
			String method = packet.getString("method");
			OHC.logger.log(Level.WARNING, "Received RPC call: " + method);
			//Dynamically reflecting into the local instance of Base_rpc to dynamically call functions inside
			//Base_rpc depending on the method supplied by the main control unit (OHC-node)
			this.rpc_interface.getClass().getMethod(method, JSONObject.class).invoke(this.rpc_interface, packet);
		}
		catch(Exception ex)
		{
			OHC.logger.log(Level.SEVERE, "JSON encoded data is missing valid rpc data: " + ex.getMessage());
		}
	}

	//RPC functions calling methods on the main control unit (OHC-node)
	public void login(String uname, String passwd)
	{
		try
		{
			JSONObject json = new JSONObject();
			json.put("method", "login").put("uname", uname).put("passwd", passwd);
			Sender s = new Sender(this.endpoint_address);
			s.execute(json);
		}
		catch (Exception ex)
		{
			OHC.logger.log(Level.SEVERE, "Failed to compose JSON: " + ex.getMessage(), ex);
		}
	}

	public void get_num_devices()
	{
		try
		{
			JSONObject json = new JSONObject();
			json.put("method", "get_num_devices");
			Sender s = new Sender(this.endpoint_address);
			s.execute(json);
		}
		catch (Exception ex)
		{
			OHC.logger.log(Level.SEVERE, "Failed to compose JSON: " + ex.getMessage(), ex);
		}
	}
}
