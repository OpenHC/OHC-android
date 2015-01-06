package io.openhc.ohc.skynet;

import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.DatagramChannel;
import java.nio.charset.Charset;

public class Sender extends AsyncTask<JSONObject, Void, Void>
{
	SocketAddress endpoint_address;

	public Sender(SocketAddress endpoint_address)
	{
		this.endpoint_address = endpoint_address;
	}

	public Void doInBackground(JSONObject... json)
	{
		try
		{
			String str = json.toString();
			byte[] data = str.getBytes(Charset.forName("UTF-8"));
			DatagramSocket socket = DatagramChannel.open().socket();
			DatagramPacket packet = new DatagramPacket(data, data.length, this.endpoint_address);
			socket.send(packet);
			socket.close();
		}
		catch (Exception ex)
		{

		}
		return null;
	}
}
