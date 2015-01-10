package io.openhc.ohc.skynet;

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
import io.openhc.ohc.skynet.transaction.Transaction_generator;

public class Broadcaster extends AsyncTask<Transaction_generator.Transaction, Void, Transaction_generator.Transaction> implements Socket_timeout.Socket_provider
{
	private final InetAddress broadcast_addr;
	private final int rport;
	private final int timeout;
	private final Broadcast_receiver receiver;

	private DatagramSocket socket_rx;

	public Broadcaster(InetAddress broadcast_addr, int rport, Broadcast_receiver receiver)
	{
		//Pretty "low" default timeout but broadcasts will only work inside LANs thus 100ms is an adequate value
		this(broadcast_addr, rport, 100, receiver);
	}

	public Broadcaster(InetAddress broadcast_addr, int rport, int timeout, Broadcast_receiver receiver)
	{
		this.receiver = receiver;
		this.broadcast_addr = broadcast_addr;
		this.rport = rport;
		this.timeout = timeout;
	}

	public static InetAddress get_broadcast_address(Context ctx)
	{
		WifiManager wifi = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
		DhcpInfo dhcp = wifi.getDhcpInfo();
		if(dhcp == null)
		{
			OHC.logger.log(Level.WARNING, "Failed to retrieve dhcp info.");
			return null;
		}
		//Figure out the broadcast address
		int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask; //Unseparated broadcast address
		byte[] quads = new byte[4];
		for(int k = 0; k < 4; k++)
			quads[k] = (byte) ((broadcast >> k * 8) & 0xFF); //Shift one byte out
		try //Might return an invalid address if dhcp settings are garbage
		{
			return InetAddress.getByAddress(quads);
		}
		catch(Exception ex)
		{
			OHC.logger.log(Level.WARNING, "Failed to retrieve broadcast address from dhcp info.");
			return null;
		}
	}

	protected Transaction_generator.Transaction doInBackground(Transaction_generator.Transaction... args)
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
			socket.setBroadcast(true);
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
							this.broadcast_addr, this.rport);
					socket.send(packet);
					OHC.logger.log(Level.INFO, "Broadcast packet send.");
				}
				catch(Exception ex)
				{
					OHC.logger.log(Level.WARNING, "Failed to send broadcast: " + ex.toString(), ex);
				}
				try
				{
					byte[] data_rx = new byte[1500]; //1500 bytes = MTU in most LANs
					/*One more timeout that's slightly longer than the socket timeout itself.
					* Used to make a socket timeout that does receive a lot of data but not
					* a valid response to it's transaction*/
					Socket_timeout timeout = new Socket_timeout(this, this.timeout + 1);
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
									OHC.logger.log(Level.WARNING,
											"Received invalid transaction uuid");

							}
							catch(Exception ex)
							{
								OHC.logger.log(Level.WARNING,
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
					OHC.logger.log(Level.SEVERE, "Socket failed to receive data: " + ex.getMessage(), ex);
				}
				transaction.inc_retry_counter();
			}
		}
		catch(IOException ex)
		{
			OHC.logger.log(Level.SEVERE, "Failed to create listening socket for broadcast response: " + ex.getMessage(), ex);
		}
		catch(JSONException ex)
		{
			OHC.logger.log(Level.SEVERE, "Broken JSON supplied: " + ex.getMessage(), ex);
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

	/*The name might be a little missleading on this one. This is NOT a receiver as in
	 a socket that receives data but a receiver as an interface a class that wants to
	 get the synchronized data received as a response to our broadcast must implement*/
	public interface Broadcast_receiver
	{
		public void on_receive_transaction(Transaction_generator.Transaction transaction);
	}
}
