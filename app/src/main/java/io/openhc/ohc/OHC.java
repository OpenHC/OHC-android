package io.openhc.ohc;

import android.os.AsyncTask;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.openhc.ohc.basestation.Basestation;
import io.openhc.ohc.basestation.device.Device;
import io.openhc.ohc.logging.OHC_Logger;
import io.openhc.ohc.skynet.Broadcaster;
import io.openhc.ohc.skynet.Network;
import io.openhc.ohc.skynet.Receiver;
import io.openhc.ohc.skynet.transaction.Transaction_generator;
import io.openhc.ohc.ui.Field_adapter;

public class OHC implements Broadcaster.Broadcast_receiver
{
	public static OHC_Logger logger = new OHC_Logger(Logger.getLogger("OHC"));

	private OHC_ui context;
	private Basestation station;

	private int ui_current_view = R.layout.activity_ohc_login;

	private ArrayAdapter<Device> device_adapter;

	public OHC(OHC_ui ctx)
	{
		this.context = ctx;
	}

	public void init()
	{
		this.find_basestation_lan();
	}

	//Retrieve address of basestation via udp broadcast
	public void find_basestation_lan()
	{
		int bcast_port = this.context.getResources().getInteger(R.integer.ohc_network_b_cast_port);
		if(Network.find_basestation_lan(this.context, bcast_port, this))
			this.context.set_status(this.context.getString(R.string.status_searching));
		else
			this.context.set_status(this.context.getString(R.string.status_fail_network));
	}

	public void on_receive_transaction(Transaction_generator.Transaction transaction)
	{
		JSONObject json = transaction.get_response();
		if(json != null)
		{
			try
			{
				InetAddress addr = InetAddress.getByName(json.getString("ip_address"));
				int port = json.getInt("port");
				this.station = new Basestation(this, new InetSocketAddress(addr, port), this.context.getResources());
				this.context.update_network_status(true);
				this.context.set_status(this.context.getString(R.string.status_found) + addr.getHostAddress());
				return;
			}
			catch(Exception ex)
			{
				logger.log(Level.WARNING, "Received invalid endpoint configuration: " + ex.getMessage(), ex);
			}
			this.context.update_network_status(false);
			this.context.set_status(this.context.getString(R.string.status_not_found));
		}
	}

	public int get_current_view()
	{
		return this.ui_current_view;
	}

	public void set_current_view(int id)
	{
		this.ui_current_view = id;
	}

	public void set_view(int id)
	{
		this.context.setContentView(id);
		this.ui_current_view = id;
	}

	public OHC_ui get_context()
	{
		return this.context;
	}

	public void connect(String uname, String passwd)
	{
		this.station.login(uname, passwd);
	}

	public void draw_device_overview()
	{
		this.set_view(R.layout.activity_ohc_overview);
		this.device_adapter = new ArrayAdapter<Device>(this.context, R.layout.list_view_item, this.station.get_devices());
		this.context.get_lv_devices().setAdapter(this.device_adapter);
	}

	public void device_show_details(int position)
	{
		this.draw_device_view(this.device_adapter.getItem(position));
	}

	public void draw_device_view(Device dev)
	{
		this.set_view(R.layout.activity_ohc_device);
		Field_adapter deviceAdapter = new Field_adapter(this.context, R.layout.list_view_item, dev.get_fields());
		this.context.get_lv_devices().setAdapter(deviceAdapter);
	}
}
