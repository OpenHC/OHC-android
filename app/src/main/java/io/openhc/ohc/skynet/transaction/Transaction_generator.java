package io.openhc.ohc.skynet.transaction;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;
import java.util.logging.Level;

import io.openhc.ohc.OHC;

//UDP transaction used to make sure that a UDP packet that has been sent has also been received
public class Transaction_generator
{
	private final int default_retry_count;
	private final OHC ohc;

	/**
	 * Default constructor
	 *
	 * @param ohc Related ohc instance
	 */
	public Transaction_generator(OHC ohc)
	{
		this(ohc, 5);
	}

	/**
	 * Constructs a new transaction generator specifying the default count of retransmits for
	 * UDP transactions
	 *
	 * @param ohc Related ohc instance
	 * @param retry_count Default number of retransmits
	 */
	public Transaction_generator(OHC ohc, int retry_count)
	{
		this.default_retry_count = retry_count;
		this.ohc = ohc;
	}

	/**
	 * Generates a new Transaction containing the given JSON data
	 *
	 * @param json Json data
	 * @return A new transaction object
	 */
	public Transaction generate_transaction(JSONObject json)
	{
		return generate_transaction(json, this.default_retry_count);
	}

	/**
	 * Generates a new Transaction overwriting the default retransmit count
	 *
	 * @param json Json data
	 * @param tries Number of retransmits
	 * @return A new transaction object
	 */
	public Transaction generate_transaction(JSONObject json, int tries)
	{
		UUID uuid = UUID.randomUUID();
		try
		{
			return new Transaction(this.ohc, json, uuid, tries);
		}
		catch(JSONException ex)
		{
			/* This should NEVER, absolutely __NEVER__ happen (except if you hand in a broken JSON object)
			 * But that's why it's called an exception, isn't it?*/
			this.ohc.logger.log(Level.SEVERE, "Transaction generator dying x(");
		}
		return null;
	}

	public class Transaction
	{
		public final String UUID_KEY = "transaction_uuid";

		private JSONObject json_request;
		private JSONObject json_response;
		private UUID uuid;
		private int retry_counter;
		private int max_retry_num;

		/**
		 * Default Transaction constructor.
		 *
		 * @param ohc Related ohc instance
		 * @param json Json data
		 * @param uuid A unique identifier
		 * @param max_retry Maximum number of retransmits
		 * @throws JSONException
		 */
		protected Transaction(OHC ohc, JSONObject json, UUID uuid, int max_retry) throws JSONException
		{
			this.json_request = json;
			this.uuid = uuid;
			this.max_retry_num = max_retry;
			try
			{
				json.put(UUID_KEY, this.get_uuid());
			}
			catch(JSONException ex)
			{
				ohc.logger.log(Level.SEVERE, "FATAL ERROR! FAILED TO SET TRANSACTION ID!");
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
				return this.get_uuid().equals(json.getString(UUID_KEY));
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
	}
}
