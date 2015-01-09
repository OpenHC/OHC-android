package io.openhc.ohc.skynet;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.TimerTask;
import java.util.logging.Level;

import io.openhc.ohc.OHC;

//Closes a socket after a given amount of time
public class Socket_timeout extends TimerTask
{
	private Socket_supplier supplier;

	public Socket_timeout(Socket_supplier supplier, long timeout)
	{
		this.supplier = supplier;
	}

	public void run()
	{
		DatagramSocket socket = this.supplier.get_socket();
		socket.close();
		OHC.logger.log(Level.INFO, String.format("Closing socket %s : %s", socket.toString()));
	}

	public interface Socket_supplier
	{
		public DatagramSocket get_socket();
	}
}
