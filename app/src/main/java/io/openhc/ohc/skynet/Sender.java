package io.openhc.ohc.skynet;

import android.os.AsyncTask;

import io.openhc.ohc.skynet.transaction.Transaction_generator;

/**
 * Abstract base class to allow for a uniform async interface on multiple protocols
 *
 * @author Tobias Schramm
 */
public abstract class Sender extends AsyncTask<Transaction_generator.Transaction, Void,
		Transaction_generator.Transaction>
{
	public interface Transaction_receiver
	{
		public void on_receive_transaction(Transaction_generator.Transaction transaction);
	}
}
