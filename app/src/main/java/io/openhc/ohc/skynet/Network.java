package io.openhc.ohc.skynet;

import android.content.Context;
import android.content.res.Resources;

import org.json.JSONObject;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.logging.Level;

import io.openhc.ohc.OHC;
import io.openhc.ohc.R;
import io.openhc.ohc.basestation.Basestation;
import io.openhc.ohc.skynet.transaction.Transaction_generator;

public class Network
{
	protected DatagramSocket socket;

	private Context ctx;

	private Receiver receiver;
	private int port_b_cast;

	public Network(Context ctx) throws Exception
	{
		Resources resources = ctx.getResources();
		this.port_b_cast = resources.getInteger(R.integer.ohc_network_b_cast_port);
		OHC.logger.log(Level.INFO, "Broadcast port is " + port_b_cast);
		try
		{
			DatagramChannel channel = DatagramChannel.open();
			this.socket = channel.socket();
			//Neat trick: Setting port to 0 makes the socket pick a random unused port
			this.socket.bind(new InetSocketAddress(InetAddress.getByName("0.0.0.0"), 0));
			OHC.logger.log(Level.INFO, "Listening on port " + this.socket.getLocalPort());
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
			json.put("method", "get_ip").put("rport", this.receiver.get_port());
			Broadcast_sender b_cast_sender = new Broadcast_sender(this.ctx, this.port_b_cast);
			b_cast_sender.execute(json.toString());
		}
		catch (Exception ex)
		{
			OHC.logger.log(Level.SEVERE, "Failed to compose JSON: " + ex.getMessage(), ex);
		}
	}

	public void send_transaction(Transaction_generator.Transaction transaction)
	{

	}

	public Receiver setup_receiver(Basestation bs)
	{
		this.receiver = new Receiver(this.socket, bs);
		return this.receiver;
	}

}
