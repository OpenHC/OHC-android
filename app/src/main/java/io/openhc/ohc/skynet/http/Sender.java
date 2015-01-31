package io.openhc.ohc.skynet.http;

import java.net.HttpURLConnection;
import java.net.SocketAddress;

import io.openhc.ohc.OHC;
import io.openhc.ohc.skynet.transaction.Transaction_generator;

/**
 * Async HTTP sender
 *
 * @author Tobias Schramm
 */
public class Sender extends io.openhc.ohc.skynet.Sender
{
	private HttpURLConnection connection;
	private final int timeout;
	private final Transaction_receiver receiver;
	private final OHC ohc;

	/**
	 * Default constructor. Initializes timeout with a sensible default value
	 * Callback is optional and thus may be null
	 *
	 * @param ohc OHC instance
	 * @param connection HTTP based connection
	 * @param receiver Callback
	 */
	public Sender(OHC ohc, HttpURLConnection connection, Transaction_receiver receiver)
	{
		//Pretty long timeout for bad mobile broadband connections
		this(ohc, connection, 10000, receiver);
	}

	/**
	 * Constructor allowing for a manual adjustment of timeout
	 * Callback is optional and may be null
	 *
	 * @param ohc OHC instance
	 * @param connection HTTP based connection
	 * @param timeout Timeout
	 * @param receiver Callback
	 */
	public Sender(OHC ohc, HttpURLConnection connection, int timeout, Transaction_receiver receiver)
	{
		this.ohc = ohc;
		this.receiver = receiver;
		this.connection = connection;
		this.timeout = timeout;
	}

	public Transaction_generator.Transaction doInBackground(Transaction_generator.Transaction... args)
	{
		Transaction_generator.Transaction transaction = args[0];
		return transaction;
	}
}
