package io.openhc.ohc.basestation;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import io.openhc.ohc.OHC;
import io.openhc.ohc.R;
import io.openhc.ohc.basestation.device.Device;
import io.openhc.ohc.basestation.rpc.Base_rpc;
import io.openhc.ohc.skynet.Network;
import io.openhc.ohc.skynet.Sender;
import io.openhc.ohc.skynet.transaction.Transaction_generator;

public class Basestation
{
	private Network network;
	private OHC ohc;
	private Base_rpc rpc_interface;
	private Sender sender;
	private Transaction_generator transaction_gen;

	private InetSocketAddress endpoint_address;
	private String session_token = "";
	private int num_devices = -1;
	private List<String> device_ids;

	HashMap<String, Device> devices = new HashMap<>();


	public Basestation(OHC ohc)
	{
		this.network = ohc.network;
		this.ohc = ohc;
		this.rpc_interface = new Base_rpc(this);
		this.transaction_gen = new Transaction_generator();
	}

	//Code being called from Base_rpc
	public void update_endpoint(InetSocketAddress addr)
	{
		this.ohc.get_context().update_network_status(addr != null);
		this.ohc.get_context().set_status(this.ohc.get_context().getString(R.string.status_found) + addr.getHostString());
		this.endpoint_address = addr;
		OHC.logger.log(Level.INFO, String.format("Endpoint address updated: %s:%s", addr.getAddress().getHostAddress(), Integer.toString(addr.getPort())));
		this.sender = new Sender(this.endpoint_address);
	}

	public void set_session_token(String token, boolean success)
	{
		if(success)
		{
			OHC.logger.log(Level.INFO, "Session token updated");
			this.session_token = token;
			this.get_num_devices();
		}
		else
		{
			OHC.logger.log(Level.WARNING, "Wrong username and/or password");
			this.ohc.get_context().login_wrong();
		}
	}

	public void set_num_devices(int num_devices)
	{
		OHC.logger.log(Level.INFO, "Number of attached devices updated: " + num_devices);
		this.num_devices = num_devices;
		this.device_ids = new ArrayList<>();
		for(int i = 0; i < this.num_devices; i++)
		{
			this.device_ids.add("");
			this.get_device_id(i);
		}
	}

	public void set_device_id(int index, String id)
	{
		OHC.logger.log(Level.INFO, String.format("Setting id of device [%d]: %s", index, id));
		if(index >= this.num_devices || index < 0)
		{
			OHC.logger.log(Level.WARNING, String.format("Device index '%d' out of range. Max %d", index, this.num_devices - 1));
			return;
		}
		this.device_ids.set(index, id);
		this.get_device_name(id);
	}

	public void set_device_name(String device_id, String name)
	{
		this.devices.put(device_id, new Device(name, device_id));
		if(this.device_ids.indexOf(device_id) == this.num_devices - 1)
		{
			this.ohc.draw_device_overview();
		}
	}

	//Dynamic calls to Base_rpc depending on the received JSON data
	public void handle_packet(JSONObject packet)
	{
		try
		{
			String method = packet.getString("method");
			OHC.logger.log(Level.WARNING, "Received RPC: " + method);
			/*Dynamically reflecting into the local instance of Base_rpc to dynamically call functions inside
			Base_rpc depending on the method supplied by the main control unit (OHC-node)*/
			this.rpc_interface.getClass().getMethod(method, JSONObject.class).invoke(this.rpc_interface, packet);
		}
		catch(Exception ex)
		{
			OHC.logger.log(Level.SEVERE, "JSON encoded data is missing valid rpc data: " + ex.getMessage());
		}
	}

	private void make_rpc_call(JSONObject json) throws JSONException
	{
		json.put("session_token", this.session_token);
		Sender s = new Sender(this.endpoint_address);
		s.execute(json);
	}

	//RPC functions calling methods on the main control unit (OHC-node)
	public void login(String uname, String passwd)
	{
		try
		{
			JSONObject json = new JSONObject();
			json.put("method", "login").put("uname", uname).put("passwd", passwd);
			this.make_rpc_call(json);
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
			this.make_rpc_call(json);
		}
		catch (Exception ex)
		{
			OHC.logger.log(Level.SEVERE, "Failed to compose JSON: " + ex.getMessage(), ex);
		}
	}

	public void get_device_id(int index)
	{
		try
		{
			JSONObject json = new JSONObject();
			json.put("method", "get_device_id").put("index", index);
			this.make_rpc_call(json);
		}
		catch (Exception ex)
		{
			OHC.logger.log(Level.SEVERE, "Failed to compose JSON: " + ex.getMessage(), ex);
		}
	}

	public void get_device_name(String id)
	{
		try
		{
			JSONObject json = new JSONObject();
			json.put("method", "get_device_name").put("id", id);
			this.make_rpc_call(json);
		}
		catch (Exception ex)
		{
			OHC.logger.log(Level.SEVERE, "Failed to compose JSON: " + ex.getMessage(), ex);
		}
	}

	public List<Device> get_devices()
	{
		List<Device> devices = new ArrayList<>();
		Iterator it = this.devices.entrySet().iterator();
		while(it.hasNext())
		{
			devices.add((Device)((Map.Entry)it.next()).getValue());
			it.remove();
		}
		return devices;
	}

	public void receive_transaction()
	{

	}
}
