package io.openhc.ohc.io.openhc.ohc.network;

import android.os.AsyncTask;

import org.json.JSONObject;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.logging.Level;

import io.openhc.ohc.OHC;
import io.openhc.ohc.io.openhc.ohc.basestation.Basestation;

public class Receiver extends AsyncTask<Void, Void, Void>
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
				JSONObject json = new JSONObject(jsonStr);
				this.basestation.handle_packet(json);
			}
			catch(Exception ex)
			{
				OHC.logger.log(Level.WARNING, "Error receiving data on udp socket: " + ex.toString());
				break;
			}
		}
		return null;
	}

	public int get_port()
	{
		return this.socket.getLocalPort();
	}
}
