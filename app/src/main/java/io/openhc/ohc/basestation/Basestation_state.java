package io.openhc.ohc.basestation;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.openhc.ohc.OHC;
import io.openhc.ohc.basestation.device.Device;
import io.openhc.ohc.basestation.device.Field;
import io.openhc.ohc.skynet.Network;

/**
 * Serializable description of a Basestation instance; used to reconstruct the basestation
 * on app recreation (e.g. when the android devices orientation is changed)
 *
 * @author Tobias Schramm
 */
public class Basestation_state implements Serializable
{
	private String ip_addr = null;
	private int rport = -1;
	private int num_devices = -1;
	private List<String> device_ids = null;
	private String session_token = null;
	private HashMap<String, Device> devices = new HashMap<>();
	private Network.Protocol protocol;

	public InetAddress get_remote_ip_address()
	{
		try
		{
			return InetAddress.getByName(this.ip_addr);
		}
		catch(Exception ex)
		{
			return null;
		}
	}

	public InetSocketAddress get_remote_socket_address()
	{
		try
		{
			return new InetSocketAddress(this.ip_addr, this.rport);
		}
		catch(Exception ex)
		{
			return null;
		}
	}

	public int get_remote_port()
	{
		return this.rport;
	}

	public String get_session_token()
	{
		return this.session_token;
	}

	public int get_num_devices()
	{
		if(this.device_ids != null)
			return this.device_ids.size();
		return this.num_devices;
	}

	public List<String> get_device_ids()
	{
		if(this.device_ids != null)
			return this.device_ids;
		ArrayList<String> ids = new ArrayList<>();
		Iterator it = this.devices.entrySet().iterator();
		while(it.hasNext())
			ids.add((String)((Map.Entry)it.next()).getKey());
		return ids;
	}

	public List<Device> get_devices()
	{
		ArrayList<Device> devices = new ArrayList<>();
		Iterator it = this.devices.entrySet().iterator();
		while(it.hasNext())
			devices.add((Device)((Map.Entry)it.next()).getValue());
		return devices;
	}

	public Device get_device(String id)
	{
		return this.devices.get(id);
	}

	public Network.Protocol get_protocol()
	{
		return this.protocol;
	}

	public void put_device(String id, Device dev)
	{
		this.devices.put(id, dev);
	}

	public void set_remote_ip_addr(String ip_addr)
	{
		this.ip_addr = ip_addr;
	}

	public void set_remote_ip_addr(InetAddress addr)
	{
		this.ip_addr = addr.getHostAddress();
	}

	public void set_remote_port(int port)
	{
		this.rport = port;
	}

	public void set_remote_socket_addr(InetSocketAddress addr)
	{
		this.ip_addr = addr.getAddress().getHostAddress();
		this.rport = addr.getPort();
	}

	public void set_session_token(String token)
	{
		this.session_token = token;
	}

	public void set_num_devices(int num_devices)
	{
		this.num_devices = num_devices;
	}

	public void set_ohc_instance(OHC ohc)
	{
		for(Device dev : this.get_devices())
			for(Field field : dev.get_fields())
				field.set_ohc_instance(ohc);
	}

	public void set_protocol(Network.Protocol protocol)
	{
		this.protocol = protocol;
	}

	public void set_device_ids(List<String> ids)
	{
		this.device_ids = ids;
	}
}
