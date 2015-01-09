package io.openhc.ohc.skynet;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.logging.Level;

import io.openhc.ohc.OHC;
import io.openhc.ohc.OHC_ui;

public class Broadcast_sender extends AsyncTask<String, Void, Void>
{
	private Context ctx;
	private int rport;

	public Broadcast_sender(Context ctx, int rport)
	{
		this.ctx = ctx;
		this.rport = rport;
	}

	InetAddress getBroadcastAddress(Context ctx)
	{
		WifiManager wifi = (WifiManager)ctx.getSystemService(Context.WIFI_SERVICE);
		DhcpInfo dhcp = wifi.getDhcpInfo();
		if(dhcp == null)
		{
			OHC.logger.log(Level.WARNING, "Failed to retrieve dhcp info.");
			return null;
		}
		//Figure out the broadcast address
		int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask; //Unseparated broadcast address
		byte[] quads = new byte[4];
		for (int k = 0; k < 4; k++)
			quads[k] = (byte) ((broadcast >> k * 8) & 0xFF); //Shift one byte out
		try //Might return an invalid address if dhcp settings are garbage
		{
			return InetAddress.getByAddress(quads);
		}
		catch (Exception ex)
		{
			OHC.logger.log(Level.WARNING, "Failed to retrieve broadcast address from dhcp info.");
			return null;
		}
	}

	protected Void doInBackground(String... args)
	{
		try
		{
			DatagramSocket socket = new DatagramSocket();
			socket.setBroadcast(true);
			byte[] data = args[0].getBytes(Charset.forName("UTF-8"));
			DatagramPacket packet = new DatagramPacket(data, data.length,
					getBroadcastAddress(this.ctx), this.rport);
			socket.send(packet);
			OHC.logger.log(Level.INFO, "Broadcast packet send.");
		}
		catch (Exception ex)
		{
			OHC.logger.log(Level.WARNING, "Failed to send broadcast: " + ex.toString());
		}
		return null;
	}
}
