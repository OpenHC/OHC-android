package io.openhc.ohc;

import android.os.AsyncTask;
import android.widget.ArrayAdapter;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.openhc.ohc.basestation.Basestation;
import io.openhc.ohc.basestation.device.Device;
import io.openhc.ohc.logging.OHC_Logger;
import io.openhc.ohc.skynet.Network;
import io.openhc.ohc.skynet.Receiver;

public class OHC
{
	public static OHC_Logger logger = new OHC_Logger(Logger.getLogger("OHC"));
	public Network network = null;

	private OHC_ui context;
	private Basestation station;

	private int ui_current_view = R.layout.activity_ohc_login;

	public OHC(OHC_ui ctx)
	{
		this.context = ctx;
		try
		{
			network = new Network(ctx);
		}
		catch (Exception ex)
		{
			logger.log(Level.SEVERE, "Couldn't create Network handler: " + ex.getMessage(), ex);
		}
	}

	public void init()
	{
		if(this.network == null)
		{
			this.context.set_status(this.context.getString(R.string.status_fail_network));
			return;
		}
		this.station = new Basestation(this);
		Receiver receiver = network.setup_receiver(station);
		receiver.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR); //Run this task in parallel to others
		network.get_basestation_address(station);
	}

	public int get_current_view()
	{
		return this.ui_current_view;
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

	public void init_device_overview(Basestation bs)
	{
		ArrayAdapter<Device> deviceAdapter = new ArrayAdapter<Device>();
	}
}
