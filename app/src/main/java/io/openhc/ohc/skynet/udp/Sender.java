package io.openhc.ohc.skynet.udp;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.channels.DatagramChannel;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.logging.Level;

import io.openhc.ohc.OHC;
import io.openhc.ohc.R;
import io.openhc.ohc.skynet.Network;
import io.openhc.ohc.skynet.transaction.Transaction_generator;

/**
 * Async UDP datagram sender
 *
 * @author Tobias Schramm
 */
public class Sender extends io.openhc.ohc.skynet.Sender
{
	public class Sender_parameters
	{
		public DatagramSocket socket;
	}

	private SocketAddress endpoint_address;
	private final Network nw;
	private final int timeout;
	private DatagramSocket socket_tx;

	private static Sender_parameters params;

	/**
	 * Default constructor. Initializes timeout with a sensible default value
	 *
	 * @param endpoint Endpoint address
	 */
	public Sender(Network nw, SocketAddress endpoint)
	{
		/*UDP control turns out to be pretty unresponsive with more latency
		* should rather use JSON via HTTP with gzip compression and larger
		* control data chunks then*/
		this(nw, endpoint, OHC.resources.getInteger(R.integer.ohc_network_timeout_udp));
	}

	/**
	 * Constructor allowing for a manual adjustment of timeout
	 *
	 * @param endpoint Endpoint address
	 * @param timeout  Timeout
	 */
	public Sender(Network nw, SocketAddress endpoint, int timeout)
	{
		this.nw = nw;
		this.endpoint_address = endpoint;
		this.timeout = timeout;
		if(params == null)
			params = new Sender_parameters();
		this.socket_tx = params.socket;
	}

	public void new_socket() throws IOException
	{
		this.socket_tx = new DatagramSocket();
		params.socket = this.socket_tx;
	}

	public Transaction_generator.Transaction doInBackground(Transaction_generator.Transaction... args)
	{
		Transaction_generator.Transaction transaction = args[0];
		try
		{
			JSONObject json_tx = transaction.get_json();
			json_tx.put("rport", this.nw.receiver.get_port());
			if(this.socket_tx == null || this.socket_tx.isClosed())
				new_socket();
			byte[] data_tx = json_tx.toString().getBytes(Charset.forName("UTF-8"));
			DatagramPacket packet = new DatagramPacket(data_tx, data_tx.length,
					this.endpoint_address);
			this.nw.watch_transaction(transaction);
			transaction.set_timeout(this.timeout, this.nw);
			this.socket_tx.send(packet);
		}
		catch(IOException ex)
		{
			OHC.logger.log(Level.SEVERE, "Failed to send packet: " + ex.getMessage(), ex);
		}
		catch(JSONException ex)
		{
			OHC.logger.log(Level.SEVERE, "Broken JSON supplied: " + ex.getMessage(), ex);
		}
		return transaction;
	}
}