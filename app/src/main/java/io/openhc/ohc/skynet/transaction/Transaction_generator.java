package io.openhc.ohc.skynet.transaction;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.logging.Level;

import io.openhc.ohc.OHC;

/**
 * Transaction generator for UDP transactions. As this app uses WLAN to communicate with the
 * physical basestation it's likely that sooner or later a UDP packet will be lost. Thus a
 * transaction has to be used to prevent packet loss. In a transaction a packet is repeated until
 * either a maximum retransmit count is reached or a response is received.
 *
 * @author Tobias Schramm
 */
public class Transaction_generator
{
	private final int default_retry_count;

	/**
	 * Constructs a new transaction generator
	 *
	 */
	public Transaction_generator()
	{
		this(5);
	}

	/**
	 * Constructs a new transaction generator specifying the default count of retransmits for
	 * UDP transactions
	 *
	 * @param retry_count Default number of retransmits
	 */
	public Transaction_generator(int retry_count)
	{
		this.default_retry_count = retry_count;
	}

	/**
	 * Generates a new Transaction containing the given JSON data
	 *
	 * @param json Json data
	 * @return A new transaction object
	 */
	public Transaction generate_transaction(JSONObject json)
	{
		return this.generate_transaction(json, this.default_retry_count);
	}

	/**
	 * Generates a new Transaction overwriting the default retransmit count
	 *
	 * @param json  JSON data
	 * @param tries Number of retransmits
	 * @return A new transaction object
	 */
	public Transaction generate_transaction(JSONObject json, int tries)
	{
		return this.generate_transaction(json, null, null, tries);
	}

	/**
	 * Generates a new Transaction with a callback
	 *
	 * @param json  JSON data
	 * @param callback Callback for finished transactions
	 * @return A new transaction object
	 */
	public Transaction generate_transaction(JSONObject json, Transaction_receiver callback)
	{
		return this.generate_transaction(json, callback, null);
	}

	/**
	 * Generates a new Transaction overwriting the default uuid
	 *
	 * @param json  JSON data
	 * @param callback Callback for finished transactions
	 * @param uuid A specific uuid
	 * @return A new transaction object
	 */
	public Transaction generate_transaction(JSONObject json, Transaction_receiver callback, UUID uuid)
	{
		return this.generate_transaction(json, callback, uuid, this.default_retry_count);
	}

	/**
	 * Generates a new Transaction overwriting the default retransmit count and specifying a uuid
	 *
	 * @param json  JSON data
	 * @param tries Number of retransmits
	 * @return A new transaction object
	 */
	public Transaction generate_transaction(JSONObject json, Transaction_receiver callback, UUID uuid, int tries)
	{
		if(uuid == null)
			uuid = UUID.randomUUID();
		try
		{
			return new Transaction(json, uuid, callback, tries);
		}
		catch(JSONException ex)
		{
			/* This should NEVER, absolutely __NEVER__ happen (except if you hand in a broken JSON object)
			 * But that's why it's called an exception, isn't it?*/
			OHC.logger.log(Level.SEVERE, "Transaction generator dying x(");
		}
		return null;
	}

	/**
	 * The actual transaction. Contains both the request and a potential response. Responses must
	 * be validated before passing them back
	 *
	 * @author Tobias Schramm
	 */
	public class Transaction
	{
		private JSONObject json_request;
		private JSONObject json_response;
		private UUID uuid;
		private int retry_counter;
		private int max_retry_num;
		private List<Transaction> sub_transactions = new ArrayList<>();
		private Transaction_receiver callback;
		private TimerTask timeout;

		/**
		 * Default Transaction constructor.
		 *
		 * @param json      Json data
		 * @param uuid      A unique identifier
		 * @param callback  Callback for finished transactions
		 * @param max_retry Maximum number of retransmits
		 * @throws JSONException
		 */
		protected Transaction(JSONObject json, UUID uuid, Transaction_receiver callback, int max_retry) throws JSONException
		{
			this.json_request = json;
			this.uuid = uuid;
			this.callback = callback;
			this.max_retry_num = max_retry;
			this.store_uuid();
		}

		/**
		 * Stores the transaction uuid in the JSON payload
		 *
		 * @throws JSONException
		 */
		private void store_uuid() throws JSONException
		{
			try
			{
				this.json_request.put(JSON.UUID_KEY, this.get_uuid());
			}
			catch(JSONException ex)
			{
				OHC.logger.log(Level.SEVERE, "FATAL ERROR! FAILED TO SET TRANSACTION ID!");
				throw ex;
			}
		}

		/**
		 * Returns a textual representation of the transaction UUID
		 *
		 * @return UUID of the transaction
		 */
		public String get_uuid()
		{
			return this.uuid.toString();
		}

		/**
		 * Returns the enclosed JSON object
		 *
		 * @return JSON tx data
		 */
		public void set_json(JSONObject json) throws JSONException
		{
			this.json_request = json;
			this.store_uuid();
		}

		/**
		 * Returns the enclosed JSON object
		 *
		 * @return JSON tx data
		 */
		public JSONObject get_json()
		{
			return this.json_request;
		}

		/**
		 * Returns the current number of retries
		 *
		 * @return Number of retires
		 */
		public int get_retry_counter()
		{
			return this.retry_counter;
		}

		/**
		 * Increment retry counter
		 */
		public void inc_retry_counter()
		{
			this.retry_counter++;
		}

		/**
		 * Set response JSON object
		 *
		 * @param response Response
		 */
		public void set_response(JSONObject response)
		{
			this.json_response = response;
		}

		/**
		 * Get response JSON object
		 *
		 * @return JSON response
		 */
		public JSONObject get_response()
		{
			return this.json_response;
		}

		/**
		 * Returns if the transaction UUID in the given JSON is equal with the UUID of this
		 * transaction
		 *
		 * @param json Response JSON
		 * @return Is response valid
		 */
		public boolean is_valid_response(JSONObject json)
		{
			try
			{
				return this.get_uuid().equals(json.getString(JSON.UUID_KEY));
			}
			catch(Exception ex)
			{
				return false;
			}
		}

		/**
		 * Returns if the JSON request should be repeated
		 *
		 * @return Should retry
		 */
		public boolean do_retry()
		{
			return this.retry_counter < this.max_retry_num || this.max_retry_num == 0;
		}

		/**
		 * Reset this request, generate new UUID and make ready for resending
		 */
		public void reset()
		{
			this.uuid = UUID.randomUUID();
			this.retry_counter = 0;
			this.json_response = null;
		}

		/**
		 * Adds an enclosed transaction to this transaction
		 *
		 * @param trans Transaction
		 */
		public void add_transaction(Transaction trans)
		{
			this.sub_transactions.add(trans);
		}

		/**
		 * Returns all enclosed transactions
		 *
		 * @return Enclosed transactions
		 */
		public List<Transaction> get_transactions()
		{
			return this.sub_transactions;
		}

		public void set_timeout(long time, final Transaction_timeout timeout)
		{
			final Transaction transaction = this;
			this.timeout = new TimerTask()
			{
				@Override
				public void run()
				{
					timeout.on_transaction_timeout(transaction);
					transaction.timeout = null;
				}
			};
			Timer timer = new Timer();
			timer.schedule(this.timeout, time);
		}

		public void cancel_timeout()
		{
			if(this.timeout != null)
				this.timeout.cancel();
		}

		public Transaction_receiver get_callback()
		{
			return this.callback;
		}
	}

	public static class JSON
	{
		public static String UUID_KEY = "transaction_uuid";
	}

	public interface Transaction_timeout
	{
		public void on_transaction_timeout(Transaction transaction);
	}

	public interface Transaction_receiver
	{
		public void on_receive_transaction(Transaction_generator.Transaction transaction);
	}
}
