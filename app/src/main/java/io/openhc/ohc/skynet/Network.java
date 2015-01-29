package io.openhc.ohc.skynet;

import android.content.res.Resources;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.logging.Level;

import io.openhc.ohc.OHC;
import io.openhc.ohc.R;
import io.openhc.ohc.basestation.Basestation;
import io.openhc.ohc.skynet.transaction.Transaction_generator;
import io.openhc.ohc.skynet.udp.Broadcaster;

public class Network
{
	protected DatagramSocket socket;

	private Basestation station;

	private Receiver receiver;
	private int port_b_cast;

	/**
	 * Default constructor. Constructs a new Network interface class from basestation
	 *
	 * @param bs Basestation
	 * @throws IOException
	 */
	public Network(Basestation bs) throws IOException
	{
		Resources resources = bs.get_resources();
		this.port_b_cast = resources.getInteger(R.integer.ohc_network_b_cast_port);
		bs.ohc.logger.log(Level.INFO, "Broadcast port is " + port_b_cast);
		try
		{
			DatagramChannel channel = DatagramChannel.open();
			this.socket = channel.socket();
			//Neat trick: Setting port to 0 makes the socket pick a random unused port
			this.socket.bind(new InetSocketAddress(InetAddress.getByName("0.0.0.0"), 0));
			bs.ohc.logger.log(Level.INFO, "Listening on port " + this.socket.getLocalPort());
		}
		catch(IOException ex)
		{
			bs.ohc.logger.log(Level.SEVERE, "Couldn't create comm socket: " + ex.getMessage(), ex);
			throw ex;
		}
	}

	/**
	 * Sends a broadcast packet to discover a basestation on the LAN
	 *
	 * @param ohc OHC instance
	 * @param port Broadcast port
	 * @param receiver Callback
	 * @return Could the broadcast be sent
	 */
	public static boolean find_basestation_lan(OHC ohc, int port, Broadcaster.Broadcast_receiver receiver)
	{
		Transaction_generator gen = new Transaction_generator(ohc);
		JSONObject json = new JSONObject();
		try
		{
			json.put("method", "get_ip");
		}
		catch(JSONException ex)
		{
			//Can't happen, all prameters are static
			ohc.logger.log(Level.SEVERE, "This can't happen");
		}
		Transaction_generator.Transaction transaction = gen.generate_transaction(json);
		InetAddress broadcast_addr = Broadcaster.get_broadcast_address(ohc);
		if(broadcast_addr != null)
		{
			Broadcaster bc = new Broadcaster(ohc, broadcast_addr, port, receiver);
			bc.execute(transaction);
			return true;
		}
		return false;
	}

	/**
	 * Initialize a new RPC Receiver
	 *
	 * @return A new RPC receiver
	 */
	public Receiver setup_receiver()
	{
		this.receiver = new Receiver(this.socket, this.station);
		return this.receiver;
	}

	/**
	 * Supported network protocols
	 */
	public enum Protocol
	{
		UDP("UDP"),
		HTTP("HTTP");

		private String name;

		/**
		 * Default constructor
		 *
		 * @param human_readable_name Human readable protocol name
		 */
		private Protocol(String human_readable_name)
		{
			this.name = human_readable_name;
		}

		public String get_human_readable_name()
		{
			return this.name;
		}
	}
}
