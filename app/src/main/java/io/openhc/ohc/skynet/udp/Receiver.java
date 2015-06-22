package io.openhc.ohc.skynet.udp;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.Arrays;
import java.util.logging.Level;

import io.openhc.ohc.OHC;
import io.openhc.ohc.basestation.Basestation;
import io.openhc.ohc.skynet.Network;

/**
 * UDP packet receiver. Automatically interprets UDP packets as JSON data, parses and checks it
 * and tries to make a RPC from it
 *
 * @author Tobias Schramm
 */
public class Receiver extends io.openhc.ohc.skynet.Receiver
{
	private DatagramSocket socket;
	private Network nw;

	/**
	 * Default constructor.
	 *
	 * @param nw Network instance, for callbacks
	 */
	public Receiver(Network nw)
	{
		this.new_socket();
		this.nw = nw;
	}

	public boolean new_socket()
	{
		try
		{
			DatagramChannel channel = DatagramChannel.open();
			this.socket = channel.socket();
			//Setting port to 0 makes the socket pick a random unused port
			this.socket.bind(new InetSocketAddress(InetAddress.getByName("0.0.0.0"), 0));
			OHC.logger.log(Level.INFO, "Listening on port " + this.socket.getLocalPort());
			return true;
		}
		catch(Exception ex)
		{
			OHC.logger.log(Level.SEVERE, "Couldn't create comm socket: " + ex.getMessage(), ex);
		}
		return false;
	}

	@Override
	public void run()
	{
		byte[] data = new byte[1500];
		DatagramPacket packet = new DatagramPacket(data, data.length);
		while(!this.socket.isClosed())
		{
			try
			{
				this.socket.receive(packet);
				String jsonStr = new String(packet.getData(), "UTF-8");
				OHC.logger.log(Level.INFO, "Packet received: " + jsonStr);
				final JSONObject json = new JSONObject(jsonStr);
				this.nw.on_json_receive(json);
				Arrays.fill(data, (byte)0);
			}
			catch(JSONException ex)
			{
				OHC.logger.log(Level.INFO, "Received invalid JSON object");
			}
			catch(Exception ex)
			{
				OHC.logger.log(Level.WARNING, "Error receiving data on udp socket: " + ex.toString());
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
