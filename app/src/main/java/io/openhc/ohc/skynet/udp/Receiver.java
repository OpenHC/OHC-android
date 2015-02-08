package io.openhc.ohc.skynet.udp;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.logging.Level;

import io.openhc.ohc.basestation.Basestation;

/**
 * UDP packet receiver. Automatically interprets UDP packets as JSON data, parses and checks it
 * and tries to make a RPC from it
 *
 * @author Tobias Schramm
 */
public class Receiver extends Thread
{
	private DatagramSocket socket;
	private Basestation basestation;

	/**
	 * Default constructor.
	 *
	 * @param socket Receiving socket, must be open
	 * @param base   Basestation, for callbacks
	 */
	public Receiver(DatagramSocket socket, Basestation base)
	{
		this.socket = socket;
		this.basestation = base;
	}

	@Override
	public void run()
	{
		while(!this.socket.isClosed())
		{
			byte[] data = new byte[1024];
			DatagramPacket packet = new DatagramPacket(data, data.length);
			try
			{
				this.socket.receive(packet);
				String jsonStr = new String(packet.getData(), "UTF-8");
				this.basestation.ohc.logger.log(Level.INFO, "Packet received: " + jsonStr);
				final JSONObject json = new JSONObject(jsonStr);
				final Basestation basestation = this.basestation;
				//Publish received data to UI thread via synchronized method call
				this.basestation.ohc.get_context().runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						basestation.handle_rpc(json);
					}
				});
			}
			catch(JSONException ex)
			{
				this.basestation.ohc.logger.log(Level.INFO, "Received invalid JSON object");
			}
			catch(Exception ex)
			{
				this.basestation.ohc.logger.log(Level.WARNING, "Error receiving data on udp socket: " + ex.toString());
				break;
			}
		}
	}

	public synchronized void kill()
	{
		this.socket.close();
	}

	/**
	 * Returns the listening port of this receiver
	 *
	 * @return Local receiving port
	 */
	public int get_port()
	{
		return this.socket.getLocalPort();
	}
}
