package io.openhc.ohc.basestation;

import android.content.res.Resources;
import android.net.http.AndroidHttpClient;

import org.apache.http.client.HttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProtocolException;
import java.util.List;
import java.util.logging.Level;

import io.openhc.ohc.OHC;
import io.openhc.ohc.R;
import io.openhc.ohc.basestation.device.Device;
import io.openhc.ohc.basestation.device.Field;
import io.openhc.ohc.basestation.rpc.Base_rpc;
import io.openhc.ohc.basestation.rpc.Rpc_group;
import io.openhc.ohc.basestation.rpc.rpcs.Rpc;
import io.openhc.ohc.basestation.rpc.rpcs.Rpc_device_get_field;
import io.openhc.ohc.basestation.rpc.rpcs.Rpc_device_get_num_fields;
import io.openhc.ohc.basestation.rpc.rpcs.Rpc_device_set_field_value;
import io.openhc.ohc.basestation.rpc.rpcs.Rpc_get_device;
import io.openhc.ohc.basestation.rpc.rpcs.Rpc_get_device_id;
import io.openhc.ohc.basestation.rpc.rpcs.Rpc_get_device_ids;
import io.openhc.ohc.basestation.rpc.rpcs.Rpc_get_device_name;
import io.openhc.ohc.basestation.rpc.rpcs.Rpc_get_num_devices;
import io.openhc.ohc.basestation.rpc.rpcs.Rpc_login;
import io.openhc.ohc.basestation.rpc.rpcs.Rpc_set_device_name;
import io.openhc.ohc.skynet.Network;
import io.openhc.ohc.skynet.Sender;
import io.openhc.ohc.skynet.transaction.Transaction_generator;
import io.openhc.ohc.skynet.udp.Receiver;

/**
 * OOP representation of the Basestation (Gateway, OHC-Node)
 * This class implements all high-level RPCs. It also holds a dedicated network handler allowing
 * for the coexistence of multiple basestation instances
 *
 * @author Tobias Schramm
 */
public class Basestation implements Rpc_group.Rpc_group_callback
{
	private Network network;
	public final OHC ohc;
	public final Transaction_generator transaction_gen;
	private Base_rpc rpc_interface;
	private Resources resources;
	private Receiver rx_thread;
	private AndroidHttpClient http_client;

	private Basestation_state state;

	private final String RPC_ATTRIBUTE_METHOD;
	private final String RPC_REQUEST_KEY;
	private final String RPC_RESPONSE_KEY;

	/**
	 * Constructor for recreating the basestation from a serialized state object
	 *
	 * @param ohc   The linked ohc instance
	 * @param state The basestation state
	 * @throws IOException
	 */
	public Basestation(OHC ohc, Basestation_state state) throws IOException
	{
		this(ohc, state.get_remote_socket_address(), state.get_protocol());
		this.state = state;
	}

	/**
	 * Default constructor for constructing a new basestation
	 *
	 * @param ohc             The linked ohc instance
	 * @param station_address The address of the basestation
	 * @param protocol        The network protocol used to connect to physical basestation
	 * @throws IOException
	 */
	public Basestation(OHC ohc, InetSocketAddress station_address, Network.Protocol protocol) throws IOException
	{
		this.ohc = ohc;
		this.resources = ohc.get_context().getResources();
		this.network = new Network(this, protocol);
		this.rpc_interface = new Base_rpc(ohc);
		this.transaction_gen = new Transaction_generator();
		this.state = new Basestation_state();
		this.state.set_remote_socket_addr(station_address);
		this.state.set_protocol(protocol);

		switch(protocol)
		{
			case UDP:
				break;
			case HTTP:
				this.http_client = AndroidHttpClient.newInstance(this.resources.getString(
						R.string.ohc_network_http_user_agent));
				this.state.set_remote_port(this.resources.getInteger(R.integer.ohc_network_http_port));
		}

		this.RPC_ATTRIBUTE_METHOD = this.resources.getString(R.string.ohc_rpc_attribute_method);
		this.RPC_REQUEST_KEY = this.resources.getString(R.string.ohc_rpc_request_key);
		this.RPC_RESPONSE_KEY = this.resources.getString(R.string.ohc_rpc_response_key);
	}

	//***** Code being called from Base_rpc *****

	/**
	 * [RPC] Sets a new address for the basestation
	 *
	 * @param addr The new address
	 */
	public void update_endpoint(InetSocketAddress addr)
	{
		this.ohc.get_context().update_network_status(addr != null);
		this.state.set_remote_socket_addr(addr);
		this.ohc.logger.log(Level.INFO, String.format("Endpoint address updated: %s:%s",
				addr.getAddress().getHostAddress(), Integer.toString(addr.getPort())));
	}

	/**
	 * [RPC] Sets the session token of this device
	 *
	 * @param token   A new session token
	 * @param success Login successful
	 */
	public void set_session_token(String token, boolean success)
	{
		if(success)
		{
			this.ohc.logger.log(Level.INFO, "Session token updated");
			this.state.set_session_token(token);
			Rpc_group group = new Rpc_group(this);
			if(this.get_protocol() == Network.Protocol.HTTP)
				group.add_rpcs(this.get_device_ids());
			else
				group.add_rpcs(this.get_num_devices());
			this.run_rpc_group(group);
			this.ohc.get_context().set_login_status(false);
		}
		else
		{
			this.ohc.logger.log(Level.WARNING, "Wrong username and/or password");
			this.ohc.get_context().login_wrong();
		}
	}

	/**
	 * [RPC] Sets the number of devices attached to this basestation
	 *
	 * @param num_devices Number of devices
	 */
	public void set_num_devices(int num_devices)
	{
		this.ohc.logger.log(Level.INFO, "Number of attached devices updated: " + num_devices);
		this.state.set_num_devices(num_devices);
		Rpc_group group = new Rpc_group(this);
		for(int i = 0; i < this.state.get_num_devices(); i++)
		{
			group.add_rpcs(this.get_device_id(i));
		}
		this.run_rpc_group(group);
	}

	/**
	 * [RPC] Sets the internal id of a device based on its index
	 *
	 * @param index Index of device in device list
	 * @param id    Internal id
	 */
	public void set_device_id(int index, String id)
	{
		this.ohc.logger.log(Level.INFO, String.format("Setting id of device [%d]: %s", index, id));
		if(index >= this.state.get_num_devices() || index < 0)
		{
			this.ohc.logger.log(Level.WARNING, String.format("Device index '%d' out of range. Max %d", index, this.state.get_num_devices() - 1));
			return;
		}
		this.state.put_device(id, null);
		Rpc_group group = new Rpc_group(this);
		group.add_rpcs(this.get_device_name(id));
		this.run_rpc_group(group);
	}

	/**
	 * [RPC] Sets the human readable name of a device based on its internal id
	 *
	 * @param device_id Internal device id
	 * @param name      Human readable name
	 */
	public void set_device_name(String device_id, String name)
	{
		this.state.put_device(device_id, new Device(name, device_id));
		Rpc_group group = new Rpc_group(this);
		group.add_rpcs(this.device_get_num_fields(device_id));
		this.run_rpc_group(group);
	}

	/**
	 * [RPC] Sets the number of fields available on the specified device
	 *
	 * @param id         Internal id of the device
	 * @param num_fields Number of fields
	 */
	public void device_set_num_fields(String id, int num_fields)
	{
		Device dev = this.state.get_device(id);
		if(dev != null)
		{
			dev.set_field_num(num_fields);
			Rpc_group group = new Rpc_group(this);
			for(int i = 0; i < num_fields; i++)
			{
				group.add_rpcs(this.device_get_field(id, i));
			}
			this.run_rpc_group(group);
		}
	}

	/**
	 * [RPC} Sets a whole field on the specified device
	 *
	 * @param id_dev   Internal device id
	 * @param id_field Numeric field id
	 * @param field    The field
	 */
	public void device_set_field(String id_dev, int id_field, Field field)
	{
		Device dev = this.state.get_device(id_dev);
		if(dev != null)
		{
			dev.set_field(id_field, field);
			if(this.state.get_device_ids().indexOf(id_dev) == this.state.get_num_devices() - 1 && dev.get_field_num() - 1 == id_field)
				ohc.draw_device_overview();
		}
	}

	/**
	 * [RPC] Sets all device ids
	 *
	 * @param ids List of all device ids
	 */
	public void set_device_ids(List<String> ids)
	{
		this.state.set_device_ids(ids);
		Rpc_group group = new Rpc_group(this);
		for(String id : ids)
			group.add_rpcs(this.rpc_get_device(id));
		this.run_rpc_group(group);
	}

	/**
	 * [RPC] Adds a device
	 *
	 * @param dev Device
	 */
	public void add_device(Device dev)
	{
		this.state.put_device(dev.get_id(), dev);
		if(this.state.get_device_ids().indexOf(dev.get_id()) == this.state.get_num_devices() - 1)
			ohc.draw_device_overview();
	}

	//Dynamic calls to Base_rpc depending on the received JSON data

	/**
	 * Handles incoming JSON RPC data
	 *
	 * @param rpc JSON RPC data
	 */
	private void call_rpc(JSONObject rpc)
	{
		try
		{
			String method = rpc.getString(this.RPC_ATTRIBUTE_METHOD);
			this.ohc.logger.log(Level.INFO, "Received RPC: " + method);
			/*Dynamically reflecting into the local instance of Base_rpc to dynamically call functions inside
			* Base_rpc depending on the method supplied by the main control unit / basestation (OHC-node)*/
			this.rpc_interface.getClass().getMethod(method,
					JSONObject.class).invoke(this.rpc_interface, rpc);
		}
		catch(Exception ex)
		{
			this.ohc.logger.log(Level.SEVERE, "JSON encoded data is missing valid rpc data: " +
					ex.getMessage());
		}
	}

	public void handle_rpc(JSONObject data)
	{
		switch(this.get_protocol())
		{
			case UDP:
				this.call_rpc(data);
				break;
			case HTTP:
				try
				{
					JSONArray array = data.getJSONArray(this.RPC_RESPONSE_KEY);
					for(int i = 0; i < array.length(); i++)
						this.call_rpc(array.getJSONObject(i));
				}
				catch(Exception ex)
				{
					this.ohc.logger.log(Level.WARNING, "Failed to parse HTTP multipart JSON rpc", ex);
				}
		}
	}

	/**
	 * Wrapper method handling sending of RPCs
	 *
	 * @param group Group containing all RPCs to be called
	 * @throws JSONException
	 */
	public void make_rpc_call(Rpc_group group) throws JSONException, ProtocolException
	{
		switch(this.get_protocol())
		{
			case UDP:
				throw new ProtocolException("UDP doesn't support direct sending of RPC groups");
			case HTTP:
				group.set_session_token(this.state.get_session_token());
				InetSocketAddress endpoint = new InetSocketAddress(this.state.get_remote_ip_address(),
						this.state.get_remote_port());
				Sender s_http = new io.openhc.ohc.skynet.http.Sender(this.ohc, this.http_client,
						endpoint, group);
				JSONArray rpcs = new JSONArray();
				for(Rpc rpc : group.get_rpcs())
				{
					rpcs.put(rpc.get_transaction().get_json());
				}
				JSONObject obj = new JSONObject();
				obj.put(this.RPC_REQUEST_KEY, rpcs);
				Transaction_generator.Transaction transaction_tcp = this.transaction_gen
						.generate_transaction(obj);
				s_http.execute(transaction_tcp);
		}
	}

	/**
	 * Wrapper method handling sending of RPCs
	 *
	 * @param rpc RPC to be called
	 * @throws JSONException
	 */
	public void make_rpc_call(Rpc rpc) throws JSONException
	{
		switch(this.get_protocol())
		{
			case UDP:
				Transaction_generator.Transaction transaction_udp = rpc.get_transaction();
				this.network.send_transaction(transaction_udp);
				break;
			case HTTP:
				InetSocketAddress endpoint = new InetSocketAddress(this.state.get_remote_ip_address(),
						this.state.get_remote_port());
				Sender s_tcp = new io.openhc.ohc.skynet.http.Sender(this.ohc, this.http_client,
						endpoint, rpc);
				JSONArray rpcs = new JSONArray();
				rpcs.put(rpc.get_transaction().get_json());
				JSONObject obj = new JSONObject();
				obj.put(this.RPC_REQUEST_KEY, rpcs);
				Transaction_generator.Transaction transaction_tcp = this.transaction_gen
						.generate_transaction(obj);
				s_tcp.execute(transaction_tcp);
		}
	}

	/**
	 *
	 */
	public void run_rpc_group(Rpc_group group)
	{
		group.set_session_token(this.state.get_session_token());
		group.run();
	}

	//***** RPC functions calling methods on the main control unit (OHC-node) *****

	/**
	 * Makes a login RPC tho the basestation
	 *
	 * @param uname  Username
	 * @param passwd Password
	 */
	public void login(String uname, String passwd)
	{
		Rpc_group group = new Rpc_group(this);
		Rpc_login rpc = new Rpc_login(this);
		rpc.set_uname(uname);
		rpc.set_passwd(passwd);
		group.add_rpcs(rpc);
		this.run_rpc_group(group);
	}

	/**
	 * Requests the number of attached devices from the basestation
	 *
	 * @return The rpc
	 */
	public Rpc get_num_devices()
	{
		Rpc_get_num_devices rpc = new Rpc_get_num_devices(this);
		return rpc;
	}

	/**
	 * Gets the internal id of a device by its index
	 *
	 * @param index Device index
	 * @return The rpc
	 */
	public Rpc get_device_id(int index)
	{
		Rpc_get_device_id rpc = new Rpc_get_device_id(this);
		rpc.set_index(index);
		return rpc;
	}

	/**
	 * Gets the human readable name of a device by its internal id
	 *
	 * @param id Internal device id
	 * @return The rpc
	 */
	public Rpc get_device_name(String id)
	{
		Rpc_get_device_name rpc = new Rpc_get_device_name(this);
		rpc.set_id(id);
		return rpc;
	}

	/**
	 * Gets the number of fields of an attached device by its internal id
	 *
	 * @param id Internal device id
	 * @return The rpc
	 */
	public Rpc device_get_num_fields(String id)
	{
		Rpc_device_get_num_fields rpc = new Rpc_device_get_num_fields(this);
		rpc.set_id(id);
		return rpc;
	}

	/**
	 * Get a field of an attached device by the internal device id and the field id
	 *
	 * @param id_dev   Internal device id
	 * @param id_field Numeric field id
	 * @return The rpc
	 */
	public Rpc device_get_field(String id_dev, int id_field)
	{
		Rpc_device_get_field rpc = new Rpc_device_get_field(this);
		rpc.set_id(id_dev);
		rpc.set_field_id(id_field);
		return rpc;
	}

	/**
	 * Set the value of a field on an attached device
	 *
	 * @param id_dev   Internal device id
	 * @param id_field Numeric field id
	 * @param value    Value of the field
	 */
	public void device_set_field_value(String id_dev, int id_field, Object value)
	{
		Rpc_group group = new Rpc_group(this);
		Rpc_device_set_field_value rpc = new Rpc_device_set_field_value(this);
		rpc.set_id(id_dev);
		rpc.set_field_id(id_field);
		rpc.set_field_value(value);
		group.add_rpcs(rpc);
		this.run_rpc_group(group);
	}

	/**
	 * Set the human readable name of a device
	 *
	 * @param dev  Device object
	 * @param name Human readable device name
	 */
	public void device_set_name(Device dev, String name)
	{
		this.device_set_name(dev.get_id(), name);
	}

	/**
	 * Set the human readable name of a device
	 *
	 * @param id   Internal device id
	 * @param name Human readable device name
	 */
	public void device_set_name(String id, String name)
	{
		Rpc_group group = new Rpc_group(this);
		Rpc_set_device_name rpc = new Rpc_set_device_name(this);
		rpc.set_id(id);
		rpc.set_name(name);
		group.add_rpcs(rpc);
		this.run_rpc_group(group);
	}

	/**
	 * Requests all device ids from the basestation
	 *
	 * @return The rpc
	 */
	public Rpc get_device_ids()
	{
		return new Rpc_get_device_ids(this);
	}

	/**
	 * Queries a device object from the basestation
	 *
	 * @return The rpc
	 */
	public Rpc rpc_get_device(String id)
	{
		Rpc_get_device rpc = new Rpc_get_device(this);
		rpc.set_id(id);
		return rpc;
	}

	@Override
	public void on_group_finish(Rpc_group group)
	{

	}

	//General purpose functions

	/**
	 * Get a list of all known devices
	 *
	 * @return A list of all attached devices
	 */
	public List<Device> get_devices()
	{
		return this.state.get_devices();
	}

	/**
	 * Get resources
	 *
	 * @return Resources
	 */
	public Resources get_resources()
	{
		return this.resources;
	}

	/**
	 * Get device by id
	 *
	 * @param id Internal device id
	 * @return Device instance
	 */
	public Device get_device(String id)
	{
		return this.state.get_device(id);
	}

	/**
	 * Get serializable version of this basestation
	 *
	 * @return Serializable representation
	 */
	public Basestation_state get_state()
	{
		return this.state;
	}

	/**
	 * Returns the protocol being used
	 *
	 * @return Current protocol
	 */
	public Network.Protocol get_protocol()
	{
		return this.state.get_protocol();
	}

	/**
	 * Returns whether requests to the basestation should be bundled together or not
	 *
	 * @return Bundle requests
	 */
	public boolean do_bundle_requests()
	{
		return this.state.get_protocol() == Network.Protocol.HTTP;
	}

	/**
	 * Quits all tasks related to this basestation
	 */
	public void destroy()
	{
		if(this.rx_thread != null)
			this.rx_thread.kill();
		if(this.http_client != null)
			this.http_client.close();
	}
}
