package io.openhc.ohc.basestation;

import android.content.res.Resources;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.logging.Level;

import io.openhc.ohc.OHC;
import io.openhc.ohc.basestation.device.Device;
import io.openhc.ohc.basestation.device.Field;
import io.openhc.ohc.basestation.rpc.Base_rpc;
import io.openhc.ohc.skynet.Network;
import io.openhc.ohc.skynet.udp.Receiver;
import io.openhc.ohc.skynet.Sender;
import io.openhc.ohc.skynet.transaction.Transaction_generator;

/**
 * OOP representation of the Basestation (Gateway, OHC-Node)
 * This class implements all high-level RPCs. It also holds a dedicated network handler allowing
 * for the coexistence of multiple basestation instances
 *
 * @author Tobias Schramm
 */
public class Basestation implements io.openhc.ohc.skynet.udp.Sender.Packet_receiver
{
	private Network network;
	public final OHC ohc;
	private Base_rpc rpc_interface;
	private Resources resources;
	private Transaction_generator transaction_gen;
	private Receiver rx_thread;

	private Basestation_state state;

	/**
	 * Constructor for recreating the basestation from a serialized state object
	 *
	 * @param ohc The linked ohc instance
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
	 * @param ohc The linked ohc instance
	 * @param station_address The address of the basestation
	 * @param protocol The network protocol used to connect to physical basestation
	 * @throws IOException
	 */
	public Basestation(OHC ohc, InetSocketAddress station_address, Network.Protocol protocol) throws IOException
	{
		this.ohc = ohc;
		this.resources = ohc.get_context().getResources();
		this.network = new Network(this);
		this.rpc_interface = new Base_rpc(ohc);
		this.transaction_gen = new Transaction_generator(ohc);
		this.state = new Basestation_state();
		this.state.set_remote_socket_addr(station_address);
		this.state.set_protocol(protocol);

		//Receiver for state updates initiated by the basestation
		this.rx_thread = network.setup_receiver();
		this.rx_thread.start();
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
	 * @param token A new session token
	 * @param success Login successful
	 */
	public void set_session_token(String token, boolean success)
	{
		if(success)
		{
			this.ohc.logger.log(Level.INFO, "Session token updated");
			this.state.set_session_token(token);
			this.get_num_devices();
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
		for(int i = 0; i < this.state.get_num_devices(); i++)
		{
			this.get_device_id(i);
		}
	}

	/**
	 * [RPC] Sets the internal id of a device based on its index
	 *
	 * @param index Index of device in device list
	 * @param id Internal id
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
		this.get_device_name(id);
	}

	/**
	 * [RPC] Sets the human readable name of a device based on its internal id
	 *
	 * @param device_id Internal device id
	 * @param name Human readable name
	 */
	public void set_device_name(String device_id, String name)
	{
		this.state.put_device(device_id, new Device(name, device_id));
		this.device_get_num_fields(device_id);
	}

	/**
	 * [RPC] Sets the number of fields available on the specified device
	 *
	 * @param id Internal id of the device
	 * @param num_fields Number of fields
	 */
	public void device_set_num_fields(String id, int num_fields)
	{
		Device dev = this.state.get_device(id);
		if(dev != null)
		{
			dev.set_field_num(num_fields);
			for(int i = 0; i < num_fields; i++)
			{
				this.device_get_field(id, i);
			}
		}
	}

	/**
	 * [RPC} Sets a whole field on the specified device
	 *
	 * @param id_dev Internal device id
	 * @param id_field Numeric field id
	 * @param field The field
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

	//Dynamic calls to Base_rpc depending on the received JSON data
	/**
	 * Handles incoming JSON RPC data
	 *
	 * @param packet JSON RPC data
	 */
	public void handle_packet(JSONObject packet)
	{
		try
		{
			String method = packet.getString("method");
			this.ohc.logger.log(Level.INFO, "Received RPC: " + method);
			/*Dynamically reflecting into the local instance of Base_rpc to dynamically call functions inside
			* Base_rpc depending on the method supplied by the main control unit / basestation (OHC-node)*/
			this.rpc_interface.getClass().getMethod(method,
					JSONObject.class).invoke(this.rpc_interface, packet);
		}
		catch(Exception ex)
		{
			this.ohc.logger.log(Level.SEVERE, "JSON encoded data is missing valid rpc data: " +
					ex.getMessage());
		}
	}

	/**
	 * Wrapper method handling sending of RPCs. Automatically embeds the session token of
	 * this device in each RPC
	 *
	 * @param json The JSON RPC to be transmitted
	 * @throws JSONException
	 */
	private void make_rpc_call(JSONObject json) throws JSONException
	{
		json.put("session_token", this.state.get_session_token());
		Sender s = new io.openhc.ohc.skynet.udp.Sender(this.ohc, this.state.get_remote_socket_address(), this);
		Transaction_generator.Transaction transaction = this.transaction_gen.generate_transaction(json);
		s.execute(transaction);
	}

	//***** RPC functions calling methods on the main control unit (OHC-node) *****

	/**
	 * Makes a login RPC tho the basestation
	 *
	 * @param uname Username
	 * @param passwd Password
	 */
	public void login(String uname, String passwd)
	{
		try
		{
			JSONObject json = new JSONObject();
			json.put("method", "login").put("uname", uname).put("passwd", passwd);
			this.make_rpc_call(json);
		}
		catch(Exception ex)
		{
			this.ohc.logger.log(Level.SEVERE, "Failed to compose JSON: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Requests the number of attached devices from the basestation
	 */
	public void get_num_devices()
	{
		try
		{
			JSONObject json = new JSONObject();
			json.put("method", "get_num_devices");
			this.make_rpc_call(json);
		}
		catch(Exception ex)
		{
			this.ohc.logger.log(Level.SEVERE, "Failed to compose JSON: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Gets the internal id of a device by its index
	 *
	 * @param index Device index
	 */
	public void get_device_id(int index)
	{
		try
		{
			JSONObject json = new JSONObject();
			json.put("method", "get_device_id").put("index", index);
			this.make_rpc_call(json);
		}
		catch(Exception ex)
		{
			this.ohc.logger.log(Level.SEVERE, "Failed to compose JSON: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Gets the human readable name of a device by its internal id
	 *
	 * @param id Internal device id
	 */
	public void get_device_name(String id)
	{
		try
		{
			JSONObject json = new JSONObject();
			json.put("method", "get_device_name").put("id", id);
			this.make_rpc_call(json);
		}
		catch(Exception ex)
		{
			this.ohc.logger.log(Level.SEVERE, "Failed to compose JSON: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Gets the number of fields of an attached device by its internal id
	 *
	 * @param id Internal device id
	 */
	public void device_get_num_fields(String id)
	{
		try
		{
			JSONObject json = new JSONObject();
			json.put("method", "device_get_num_fields").put("id", id);
			this.make_rpc_call(json);
		}
		catch(Exception ex)
		{
			this.ohc.logger.log(Level.SEVERE, "Failed to compose JSON: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Get a field of an attached device by the internal device id and the field id
	 *
	 * @param id_dev Internal device id
	 * @param id_field Numeric field id
	 */
	public void device_get_field(String id_dev, int id_field)
	{
		try
		{
			JSONObject json = new JSONObject();
			json.put("method", "device_get_field").put("device_id", id_dev).put("field_id", id_field);
			this.make_rpc_call(json);
		}
		catch(Exception ex)
		{
			this.ohc.logger.log(Level.SEVERE, "Failed to compose JSON: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Set the value of a field on an attached device
	 *
	 * @param id_dev Internal device id
	 * @param id_field Numeric field id
	 * @param value Value of the field
	 */
	public void device_set_field_value(String id_dev, int id_field, Object value)
	{
		try
		{
			JSONObject json = new JSONObject();
			json.put("method", "device_set_field_value").put("device_id", id_dev)
					.put("field_id", id_field).put("value", value);
			this.make_rpc_call(json);
		}
		catch(Exception ex)
		{
			this.ohc.logger.log(Level.SEVERE, "Failed to compose JSON: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Set the human readable name of a device
	 *
	 * @param dev Device object
	 * @param name Human readable device name
	 */
	public void device_set_name(Device dev, String name)
	{
		this.device_set_name(dev.get_id(), name);
	}

	/**
	 * Set the human readable name of a device
	 *
	 * @param id Internal device id
	 * @param name Human readable device name
	 */
	public void device_set_name(String id, String name)
	{
		try
		{
			JSONObject json = new JSONObject();
			json.put("method", "set_device_name").put("id", id).put("name", name);
			this.make_rpc_call(json);
		}
		catch(Exception ex)
		{
			this.ohc.logger.log(Level.SEVERE, "Failed to compose JSON: " + ex.getMessage(), ex);
		}
	}

	//Callback for transactions
	@Override
	public void on_receive_transaction(Transaction_generator.Transaction transaction)
	{
		JSONObject json = transaction.get_response();
		if(json != null)
		{
			this.handle_packet(json);
			return;
		}
		this.ohc.logger.log(Level.WARNING, String.format("Didn't receive response for transaction %s in time", transaction.get_uuid()));
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
	 * Quits all tasks related to this basestation
	 */
	public void destroy()
	{
		if(this.rx_thread != null)
			this.rx_thread.kill();
	}
}
