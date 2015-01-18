package io.openhc.ohc.skynet;

import java.net.DatagramSocket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

import io.openhc.ohc.OHC;

/**
 * Closes a socket after a given amount of time
 *
 * @author Tobias Schramm
 */
public class Socket_timeout extends TimerTask
{
	private final OHC ohc;
	private final Socket_provider supplier;
	private final long timeout;

	/**
	 * Default constructor
	 *
	 * @param ohc OHC instance
	 * @param supplier A socket supplier
	 * @param timeout The timeout in ms
	 */
	public Socket_timeout(OHC ohc, Socket_provider supplier, long timeout)
	{
		this.ohc = ohc;
		this.supplier = supplier;
		this.timeout = timeout;
	}

	/**
	 * Start timeout
	 */
	public void start()
	{
		Timer t = new Timer();
		t.schedule(this, this.timeout);
	}

	@Override
	public void run()
	{
		DatagramSocket socket = this.supplier.get_socket();
		try
		{
			socket.close();
			this.ohc.logger.log(Level.INFO, "Closing socket: " + socket.toString());
		}
		catch(Exception ex)
		{
			this.ohc.logger.log(Level.INFO, String.format("Closing socket %s : %s", socket.toString(), ex.getMessage()), ex);
		}
	}

	public interface Socket_provider
	{
		/**
		 * Callback to get a socket
		 *
		 * @return The socket
		 */
		public DatagramSocket get_socket();
	}
}
