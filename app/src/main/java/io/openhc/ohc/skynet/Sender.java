package io.openhc.ohc.skynet;

import android.os.AsyncTask;

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
import java.util.logging.Level;

import io.openhc.ohc.OHC;
import io.openhc.ohc.skynet.transaction.Transaction_generator;

public class Sender extends AsyncTask<Transaction_generator.Transaction, Void,
		Transaction_generator.Transaction> implements Socket_timeout.Socket_provider
{
	SocketAddress endpoint_address;
	private final int timeout;
	private final Packet_receiver receiver;
	private final OHC ohc;

	private DatagramSocket socket_rx;

	public Sender(OHC ohc, SocketAddress endpoint, Packet_receiver receiver)
	{
		/*UDP control turns out to be pretty unresponsive with more latency
		* should rather use JSON via HTTP with gzip compression and larger
		* control data chunks*/
		this(ohc, endpoint, 300, receiver);
	}

	public Sender(OHC ohc, SocketAddress endpoint, int timeout, Packet_receiver receiver)
	{
		this.ohc = ohc;
		this.receiver = receiver;
		this.endpoint_address = endpoint;
		this.timeout = timeout;
	}

	public Transaction_generator.Transaction doInBackground(Transaction_generator.Transaction... args)
	{
		Transaction_generator.Transaction transaction = args[0];
		try
		{
			this.socket_rx = DatagramChannel.open().socket();
			//Listen on all available interfaces and pick a random, unused port
			socket_rx.bind(new InetSocketAddress(Inet4Address.getByName("0.0.0.0"), 0));
			this.socket_rx.setSoTimeout(this.timeout);
			JSONObject json_tx = transaction.get_json();
			json_tx.put("rport", socket_rx.getLocalPort());
			DatagramSocket socket = new DatagramSocket();
			byte[] data_tx = json_tx.toString().getBytes(Charset.forName("UTF-8"));
			boolean valid_response_received = false;
			while(transaction.do_retry() && !valid_response_received)
			{
				if(this.socket_rx.isClosed())
				{
					this.socket_rx = DatagramChannel.open().socket();
					socket_rx.bind(new InetSocketAddress(Inet4Address.getByName("0.0.0.0"), 0));
					this.socket_rx.setSoTimeout(this.timeout);
					json_tx = transaction.get_json();
					json_tx.put("rport", socket_rx.getLocalPort());
					data_tx = json_tx.toString().getBytes(Charset.forName("UTF-8"));
				}
				try
				{
					DatagramPacket packet = new DatagramPacket(data_tx, data_tx.length,
							this.endpoint_address);
					socket.send(packet);
				}
				catch(Exception ex)
				{
					this.ohc.logger.log(Level.WARNING, "Failed to send packet: " + ex.toString(), ex);
				}
				try
				{
					byte[] data_rx = new byte[1500]; //1500 bytes = MTU in most LANs
					/*One more timeout that's slightly longer than the socket timeout itself.
					* Used to make a socket timeout that does receive a lot of data but not
					* a valid response to it's transaction*/
					Socket_timeout timeout = new Socket_timeout(this.ohc, this, this.timeout + 1);
					try
					{
						while(!valid_response_received)
						{
							DatagramPacket packet = new DatagramPacket(data_rx, data_rx.length);
							socket_rx.receive(packet);
							try
							{
								String jsonStr = new String(packet.getData(), "UTF-8");
								JSONObject json = new JSONObject(jsonStr);
								if(transaction.is_valid_response(json))
								{
									transaction.set_response(json);
									valid_response_received = true;
									timeout.cancel();
								}
							}
							catch(Exception ex)
							{
								this.ohc.logger.log(Level.WARNING,
										"Received invalid data on rx channel: " +
												ex.getMessage());
							}
						}
					}
					catch(SocketTimeoutException ex)
					{
						timeout.cancel();
					}
				}
				catch(IOException ex)
				{
					this.ohc.logger.log(Level.SEVERE, "Socket failed to receive data: " + ex.getMessage(),
							ex);
				}
				transaction.inc_retry_counter();
			}
		}
		catch(IOException ex)
		{
			this.ohc.logger.log(Level.SEVERE,
					"Failed to create listening socket for response: " +
							ex.getMessage(),
					ex);
		}
		catch(JSONException ex)
		{
			this.ohc.logger.log(Level.SEVERE, "Broken JSON supplied: " + ex.getMessage(), ex);
		}
		return transaction;
	}

	public void onPostExecute(Transaction_generator.Transaction transaction)
	{
		if(this.receiver != null)
			this.receiver.on_receive_transaction(transaction);
	}

	@Override
	public DatagramSocket get_socket()
	{
		return this.socket_rx;
	}

	public interface Packet_receiver
	{
		public void on_receive_transaction(Transaction_generator.Transaction transaction);
	}
}
