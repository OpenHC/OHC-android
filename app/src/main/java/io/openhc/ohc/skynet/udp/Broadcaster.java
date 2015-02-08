package io.openhc.ohc.skynet.udp;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.channels.DatagramChannel;
import java.nio.charset.Charset;
import java.util.logging.Level;

import io.openhc.ohc.OHC;
import io.openhc.ohc.R;
import io.openhc.ohc.skynet.transaction.Transaction_generator;

/**
 * Async Broadcast sender with integrated UI thread synchronisation and transaction handling
 *
 * @author Tobias Schramm
 */
public class Broadcaster extends AsyncTask<Transaction_generator.Transaction, Void, Transaction_generator.Transaction> implements Socket_timeout.Socket_provider
{
	private final OHC ohc;
	private final InetAddress broadcast_addr;
	private final int rport;
	private final int timeout;
	private final Broadcast_receiver receiver;

	private DatagramSocket socket_rx;

	/**
	 * Default constructor. Initializes timeout with a sensible default value
	 * Callback is optional and thus may be null
	 *
	 * @param ohc            OHC instance
	 * @param broadcast_addr Broadcast address
	 * @param rport          Remote port
	 * @param receiver       Callback
	 */
	public Broadcaster(OHC ohc, InetAddress broadcast_addr, int rport, Broadcast_receiver receiver)
	{
		//Pretty "low" default timeout but since broadcasts will only work inside LANs 100ms are enough
		this(ohc, broadcast_addr, rport,
				ohc.get_context().getResources().getInteger(R.integer.ohc_network_timeout_bcast),
				receiver);
	}

	/**
	 * Constructor allowing for a manual adjustment of timeout
	 * Callback is optional and thus may be null
	 *
	 * @param ohc            OHC instance
	 * @param broadcast_addr Broadcast address
	 * @param rport          Remote port
	 * @param timeout        Timeout
	 * @param receiver       Callback
	 */
	public Broadcaster(OHC ohc, InetAddress broadcast_addr, int rport, int timeout, Broadcast_receiver receiver)
	{
		this.ohc = ohc;
		this.receiver = receiver;
		this.broadcast_addr = broadcast_addr;
		this.rport = rport;
		this.timeout = timeout;
	}

	/**
	 * Returns the local broadcast address
	 * Returns null if the broadcast address can't be found
	 *
	 * @param ohc OHC instance
	 * @return The broadcast address
	 */
	public static InetAddress get_broadcast_address(OHC ohc)
	{
		WifiManager wifi = (WifiManager)ohc.get_context().getSystemService(Context.WIFI_SERVICE);
		DhcpInfo dhcp = wifi.getDhcpInfo();
		if(dhcp == null)
		{
			ohc.logger.log(Level.WARNING, "Failed to retrieve dhcp info.");
			return null;
		}
		//Figure out the broadcast address
		int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask; //Unseparated broadcast address
		byte[] quads = new byte[4];
		for(int k = 0; k < 4; k++)
			quads[k] = (byte)((broadcast >> k * 8) & 0xFF); //Shift one byte out
		try //Might return an invalid address if dhcp settings are garbage
		{
			return InetAddress.getByAddress(quads);
		}
		catch(Exception ex)
		{
			ohc.logger.log(Level.WARNING, "Failed to retrieve broadcast address from dhcp info.");
			return null;
		}
	}

	@Override
	protected Transaction_generator.Transaction doInBackground(Transaction_generator.Transaction... args)
	{
		Transaction_generator.Transaction transaction = args[0];
		try
		{
			this.socket_rx = DatagramChannel.open().socket();
			//Listen on all available interfaces and pick a random unused port
			socket_rx.bind(new InetSocketAddress(Inet4Address.getByName("0.0.0.0"), 0));
			this.socket_rx.setSoTimeout(this.timeout);
			JSONObject json_tx = transaction.get_json();
			json_tx.put("rport", socket_rx.getLocalPort());
			DatagramSocket socket = new DatagramSocket();
			socket.setBroadcast(true);
			byte[] data_tx = json_tx.toString().getBytes(Charset.forName("UTF-8"));
			boolean valid_response_received = false;
			while(transaction.do_retry() && !valid_response_received)
			{
				//Send a packet...
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
							this.broadcast_addr, this.rport);
					socket.send(packet);
					this.ohc.logger.log(Level.INFO, "Broadcast packet sent");
				}
				catch(Exception ex)
				{
					this.ohc.logger.log(Level.WARNING, "Failed to send broadcast: " + ex.toString(), ex);
				}
				//... and wait for a response
				try
				{
					byte[] data_rx = new byte[1500]; //1500 bytes = MTU in most LANs
					/*One more timeout that's slightly longer than the socket timeout itself.
					* Used to make a socket that receives a lot of data but not
					* a valid response to it's transaction timeout*/
					Socket_timeout timeout = new Socket_timeout(ohc, this, this.timeout + 1);
					timeout.start();
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
								else
									this.ohc.logger.log(Level.WARNING,
											"Received invalid transaction uuid");
							}
							catch(Exception ex)
							{
								this.ohc.logger.log(Level.WARNING,
										"Received invalid data on broadcast rx channel: " +
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
					this.ohc.logger.log(Level.SEVERE, "Socket failed to receive data: " + ex.getMessage(), ex);
				}
				transaction.inc_retry_counter();
			}
		}
		catch(IOException ex)
		{
			this.ohc.logger.log(Level.SEVERE, "Failed to create listening socket for broadcast response: " + ex.getMessage(), ex);
		}
		catch(JSONException ex)
		{
			this.ohc.logger.log(Level.SEVERE, "Broken JSON supplied: " + ex.getMessage(), ex);
		}
		return transaction;
	}

	@Override
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

	/*The name might be a little misleading on this one. This is NOT a receiver as in
	 a socket that receives data but a receiver as an interface a class that wants to
	 get the synchronized data received as a response to our broadcast must implement*/
	public interface Broadcast_receiver
	{
		/**
		 * Called when the broadcast finishes
		 *
		 * @param transaction Related transaction
		 */
		public void on_receive_transaction(Transaction_generator.Transaction transaction);
	}
}
