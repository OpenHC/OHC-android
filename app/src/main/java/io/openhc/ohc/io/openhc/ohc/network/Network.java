package io.openhc.ohc.io.openhc.ohc.network;

import android.content.Context;
import android.content.res.Resources;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.json.JSONObject;

import java.math.BigInteger;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.DatagramChannel;
import java.util.logging.Level;

import io.openhc.ohc.OHC;
import io.openhc.ohc.R;
import io.openhc.ohc.io.openhc.ohc.basestation.Basestation;

public class Network
{
	protected DatagramSocket socket;

	private Context ctx;

	private Receiver receiver;
	private Sender sender;
	private int port_b_cast;

	public Network(Context ctx) throws Exception
	{
		Resources resources = ctx.getResources();
		this.port_b_cast = resources.getInteger(R.integer.ohc_network_b_cast_port);
		int port_l_rx = resources.getInteger(R.integer.ohc_network_rx_l_port);
		OHC.logger.log(Level.INFO, "Broadcast port is " + port_b_cast);
		OHC.logger.log(Level.INFO, "Local RX port is " + port_l_rx);
		try
		{
			DatagramChannel channel = DatagramChannel.open();
			this.socket = channel.socket();
			this.socket.bind(new InetSocketAddress(InetAddress.getByName("0.0.0.0"), port_l_rx));
			OHC.logger.log(Level.INFO, "Listening on port " + port_l_rx);
		}
		catch (Exception ex)
		{
			OHC.logger.log(Level.SEVERE, "Couldn't create comm socket: " + ex.getMessage(), ex);
			throw ex;
		}
		this.ctx = ctx;
	}

	public boolean connect(SocketAddress addr)
	{
		try
		{
			this.socket.connect(addr);
			return true;
		}
		catch (Exception ex)
		{
			OHC.logger.log(Level.SEVERE, "Couldn't connect comm socket: " + ex.toString());
		}
		return false;
	}

	public void get_basestation_address(Basestation base)
	{
		try
		{
			JSONObject json = new JSONObject();
			json.put("method", "get_ip").put("port", this.receiver.get_port());
			BroadcastSender b_cast_sender = new BroadcastSender(this.ctx, this.port_b_cast);
			b_cast_sender.execute(json.toString());
		}
		catch (Exception ex)
		{
			OHC.logger.log(Level.SEVERE, "Failed to compose JSON: " + ex.getMessage(), ex);
		}
	}

	public Sender setup_sender(Basestation bs)
	{
		this.sender = new Sender();
		return this.sender;
	}

	public Receiver setup_receiver(Basestation bs)
	{
		this.receiver = new Receiver(this.socket, bs);
		return this.receiver;
	}

}
