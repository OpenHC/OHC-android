package io.openhc.ohc.skynet;

import android.content.res.Resources;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.HashMap;
import java.util.logging.Level;

import io.openhc.ohc.OHC;
import io.openhc.ohc.R;
import io.openhc.ohc.basestation.Basestation;
import io.openhc.ohc.skynet.transaction.Transaction_generator;
import io.openhc.ohc.skynet.udp.Broadcaster;
import io.openhc.ohc.skynet.udp.Receiver;

/**
 * Networking class
 * Handles most low level network stuff
 *
 * @author Tobias Schramm
 */
public class Network implements Transaction_generator.Transaction_timeout,
		io.openhc.ohc.skynet.Receiver.JSON_receiver
{
	private Basestation station;

	public final io.openhc.ohc.skynet.Receiver receiver;
	private int port_b_cast;

	public final Protocol protocol;

	private HashMap<String, Transaction_generator.Transaction> transactions = new HashMap<>();

	/**
	 * Default constructor. Constructs a new Network interface class from basestation
	 *
	 * @param bs Basestation
	 * @throws IOException
	 */
	public Network(Basestation bs, Protocol protocol)
	{
		Resources resources = bs.get_resources();
		this.port_b_cast = resources.getInteger(R.integer.ohc_network_b_cast_port);
		bs.ohc.logger.log(Level.INFO, "Broadcast port is " + port_b_cast);
		this.protocol = protocol;
		switch(protocol)
		{
			case UDP:
				this.receiver = new Receiver(this);
				break;
			default:
				this.receiver = null;
		}
		if(this.receiver != null)
			this.receiver.start();
		this.station = bs;
	}

	/**
	 * Sends a broadcast packet to discover a basestation on the LAN
	 *
	 * @param ohc      OHC instance
	 * @param port     Broadcast port
	 * @param receiver Callback
	 * @return Could the broadcast be sent
	 */
	public static boolean find_basestation_lan(OHC ohc, int port, Broadcaster.Broadcast_receiver receiver)
	{
		Transaction_generator gen = new Transaction_generator();
		JSONObject json = new JSONObject();
		try
		{
			json.put("method", "get_ip");
			json.put(Network.JSON.KEY_TYPE, PacketType.RPC_SIMPLE.id);
		}
		catch(JSONException ex)
		{
			//Can't happen, all parameters are static
			OHC.logger.log(Level.SEVERE, "This can't happen");
		}
		Transaction_generator.Transaction transaction = gen.generate_transaction(json);
		InetAddress broadcast_addr = Broadcaster.get_broadcast_address(ohc);
		if(broadcast_addr != null)
		{
			Broadcaster bc = new Broadcaster(broadcast_addr, port, receiver);
			bc.execute(transaction);
			return true;
		}
		return false;
	}

	public void send_transaction(Transaction_generator.Transaction transaction)
	{
		System.gc();
		Sender sender = get_sender();
		sender.execute(transaction);
	}

	private Sender get_sender()
	{
		switch(this.protocol)
		{
			case UDP:
				return new io.openhc.ohc.skynet.udp.Sender(this,
						this.station.get_state().get_remote_socket_address());
			case HTTP:
				//sender = new io.openhc.ohc.skynet.http.Sender()
				break;
		}
		return null;
	}

	public void watch_transaction(Transaction_generator.Transaction transaction)
	{
		this.transactions.put(transaction.get_uuid(), transaction);
	}

	public void on_transaction_timeout(Transaction_generator.Transaction transaction)
	{
		if(transaction.do_retry())
			this.send_transaction(transaction);
		transaction.inc_retry_counter();
	}

	public void on_json_receive(final JSONObject json)
	{
		try
		{
			String type = json.getString(JSON.KEY_TYPE);
			if(type == null)
			{
				OHC.logger.log(Level.WARNING, "Packet is missing type information");
				return;
			}
			try
			{
				PacketType packet_type = PacketType.valueOf(type.toUpperCase());
				switch(packet_type)
				{
					case RPC_SIMPLE:
						final Basestation basestation = this.station;
						//Publish received data to UI thread via synchronized method call
						this.station.ohc.get_context().runOnUiThread(new Runnable()
						{
							@Override
							public void run()
							{
								basestation.handle_rpc(json);
							}
						});
						break;
					case RPC:
						String uuid = json.getString(Transaction_generator.JSON.UUID_KEY);
						if(uuid == null)
						{
							OHC.logger.log(Level.WARNING, "ACK Packet is missing uuid information");
							return;
						}
						Transaction_generator.Transaction transaction = this.transactions.get(uuid);
						if(transaction == null)
						{
							OHC.logger.log(Level.WARNING, "ACK Packet specified invalid transaction");
							return;
						}
						OHC.logger.log(Level.INFO, "Cancelling timeout");
						transaction.cancel_timeout();
						if(transaction.get_callback() != null)
						{
							OHC.logger.log(Level.INFO, "Calling callback");
							transaction.set_response(json);
							transaction.get_callback().on_receive_transaction(transaction);
						}
						transaction.set_response(json);
						this.transactions.remove(uuid);
						OHC.logger.log(Level.INFO, String.format("%s transactions pending",
								this.transactions.size()));
				}
			}
			catch(IllegalArgumentException ex)
			{
				OHC.logger.log(Level.WARNING, "Invalid packet type: " + type);
			}
		}
		catch(JSONException ex)
		{
			OHC.logger.log(Level.WARNING, "Failed to operate on json: " + ex.getMessage(), ex);
		}
	}

	public static class JSON
	{
		public static String KEY_TYPE = "type";
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

	public enum PacketType
	{
		RPC_SIMPLE("rpc_simple"),
		RPC("rpc");

		public String id;

		PacketType(String id)
		{
			this.id = id;
		}
	}
}
