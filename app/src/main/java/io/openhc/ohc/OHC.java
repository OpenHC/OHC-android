package io.openhc.ohc;

import android.os.AsyncTask;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.openhc.ohc.io.openhc.ohc.basestation.Basestation;
import io.openhc.ohc.io.openhc.ohc.io.openhc.ohc.logging.OHC_Logger;
import io.openhc.ohc.io.openhc.ohc.network.Network;
import io.openhc.ohc.io.openhc.ohc.network.Receiver;

public class OHC
{
	public static OHC_Logger logger = new OHC_Logger(Logger.getLogger("OHC"));
	public static Network network = null;

	private OHC_login login_form;

	public OHC(OHC_login ctx)
	{
		this.login_form = ctx;
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
		if(network == null)
		{
			this.login_form.set_status(this.login_form.getString(R.string.status_fail_network));
			return;
		}
		Basestation station = new Basestation(this.login_form);
		Receiver receiver = network.setup_receiver(station);
		receiver.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		network.get_basestation_address(station);
	}
}
