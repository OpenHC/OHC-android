package io.openhc.ohc;

import android.os.AsyncTask;
import android.os.Bundle;
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
import io.openhc.ohc.basestation.Basestation_state;
import io.openhc.ohc.basestation.device.Device;
import io.openhc.ohc.logging.OHC_Logger;
import io.openhc.ohc.skynet.Broadcaster;
import io.openhc.ohc.skynet.Network;
import io.openhc.ohc.skynet.Receiver;
import io.openhc.ohc.skynet.transaction.Transaction_generator;
import io.openhc.ohc.ui.Field_adapter;
import io.openhc.ohc.ui.Ui_state;

public class OHC implements Broadcaster.Broadcast_receiver
{
	public final OHC_Logger logger;

	private OHC_ui context;
	private Basestation station;

	private ArrayAdapter<Device> device_adapter;
	private Field_adapter field_adapter;
	private String current_dev_id;

	private Ui_state ui_state;

	public OHC(OHC_ui ctx)
	{
		this.context = ctx;
		this.logger = new OHC_Logger(ctx.getString(R.string.log_tag));
		this.ui_state = new Ui_state();
	}

	public OHC(OHC_ui ctx, Bundle saved_state) throws IOException
	{
		this.context = ctx;
		this.logger = new OHC_Logger(ctx.getString(R.string.log_tag));
		this.ui_state = (Ui_state)saved_state.getSerializable(ctx.getString(R.string.save_id_ohc));
		try
		{
			this.restore_basestation((Basestation_state)saved_state.getSerializable(
					ctx.getString(R.string.save_id_basestation)));
		}
		catch(IOException ex)
		{
			logger.log(Level.SEVERE, "Failed to restore state of basestation: ", ex);
			throw ex;
		}
	}

	public void init()
	{
		this.find_basestation_lan();
	}

	//Retrieve address of basestation via udp broadcast
	public void find_basestation_lan()
	{
		int bcast_port = this.context.getResources().getInteger(R.integer.ohc_network_b_cast_port);
		if(Network.find_basestation_lan(this, bcast_port, this))
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
				this.station = new Basestation(this, new InetSocketAddress(addr, port));
				this.context.update_network_status(true);
				this.context.set_status(this.context.getString(R.string.status_found) + addr.getHostAddress());
				return;
			}
			catch(Exception ex)
			{
				logger.log(Level.WARNING, "Received invalid endpoint configuration: " + ex.getMessage(), ex);
			}
		}
		this.context.update_network_status(false);
		this.context.set_status(this.context.getString(R.string.status_not_found));
	}

	public void restore_basestation(Basestation_state state) throws IOException
	{
		state.set_ohc_instance(this);
		this.station = new Basestation(this, state);
	}

	public int get_current_layout()
	{
		return this.ui_state.get_current_layout();
	}

	public void set_current_layout(int id)
	{
		this.ui_state.set_current_layout(id);
	}

	public void set_layout(int id)
	{
		this.context.setContentView(id);
		this.ui_state.set_current_layout(id);
	}

	public OHC_ui get_context()
	{
		return this.context;
	}

	public String get_current_dev_id()
	{
		return this.ui_state.get_current_device_id();
	}

	public Basestation get_basestation()
	{
		return this.station;
	}

	public Ui_state get_ui_state()
	{
		return this.ui_state;
	}

	public void connect(String uname, String passwd)
	{
		this.station.login(uname, passwd);
	}

	public void draw_login_page()
	{
		this.set_layout(R.layout.activity_ohc_login);
	}

	public void draw_device_overview()
	{
		this.set_layout(R.layout.activity_ohc_overview);
		if(this.device_adapter == null)
			this.device_adapter = new ArrayAdapter<Device>(this.context, R.layout.list_view_item, this.station.get_devices());
		this.context.get_lv_devices().setAdapter(this.device_adapter);
	}

	public void device_show_details(int position)
	{
		this.draw_device_view(this.device_adapter.getItem(position).get_id());
	}

	public void draw_device_view(String dev_id)
	{
		this.set_layout(R.layout.activity_ohc_device);
		Device dev = station.get_device(dev_id);
		if(this.field_adapter == null)
			this.field_adapter = new Field_adapter(this.context, R.layout.list_view_group, dev.get_fields());
		else if(this.current_dev_id != dev_id)
		{
			this.field_adapter.clear();
			this.field_adapter.addAll(dev.get_fields());
			this.field_adapter.notifyDataSetChanged();
		}
		this.context.get_et_action_bar_name().setText(dev.get_name());
		this.context.get_et_action_bar_name().addTextChangedListener(this.context.get_page_device());
		this.context.get_lv_fields().setAdapter(this.field_adapter);
		this.current_dev_id = dev_id;
		this.ui_state.set_current_device_id(dev_id);
	}
}
