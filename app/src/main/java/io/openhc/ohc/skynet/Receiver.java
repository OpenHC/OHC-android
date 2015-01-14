package io.openhc.ohc.skynet;

import android.os.AsyncTask;

import org.json.JSONObject;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.logging.Level;

import io.openhc.ohc.OHC;
import io.openhc.ohc.basestation.Basestation;

/*Using an async task to get received data transfered to the UI thread without
   having to care about synchronisation manually*/
public class Receiver extends AsyncTask<Void, JSONObject, Void>
{
	private DatagramSocket socket;
	private Basestation basestation;

	public Receiver(DatagramSocket socket, Basestation base)
	{
		this.socket = socket;
		this.basestation = base;
	}

	public Void doInBackground(Void... args)
	{
		while(true)
		{
			byte[] data = new byte[1024];
			DatagramPacket packet = new DatagramPacket(data, data.length);
			try
			{
				this.socket.receive(packet);
				String jsonStr = new String(packet.getData(), "UTF-8");
				this.basestation.ohc.logger.log(Level.INFO, "Packet received: " + jsonStr);
				JSONObject json = new JSONObject(jsonStr);
				//Publish received data to UI thread via progress notification
				this.publishProgress(json);
			}
			catch(Exception ex)
			{
				this.basestation.ohc.logger.log(Level.WARNING, "Error receiving data on udp socket: " + ex.toString());
				break;
			}
		}
		return null;
	}

	//This function is synchronized to the UI thread
	@Override
	public void onProgressUpdate(JSONObject... args)
	{
		this.basestation.handle_packet(args[0]);
	}

	public int get_port()
	{
		return this.socket.getLocalPort();
	}
}
