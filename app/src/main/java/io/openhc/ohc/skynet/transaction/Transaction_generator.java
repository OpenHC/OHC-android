package io.openhc.ohc.skynet.transaction;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;
import java.util.logging.Level;

import io.openhc.ohc.OHC;

public class Transaction_generator
{
	private final int default_retry_count;
	private final OHC ohc;

	public Transaction_generator(OHC ohc)
	{
		this(ohc, 5);
	}

	public Transaction_generator(OHC ohc, int retry_count)
	{
		this.default_retry_count = retry_count;
		this.ohc = ohc;
	}

	public Transaction generate_transaction(JSONObject json)
	{
		return generate_transaction(json, this.default_retry_count);
	}

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

		public String get_uuid()
		{
			return this.uuid.toString();
		}

		public JSONObject get_json()
		{
			return this.json_request;
		}

		public int get_retry_counter()
		{
			return this.retry_counter;
		}

		public void inc_retry_counter()
		{
			this.retry_counter++;
		}

		public void set_response(JSONObject response)
		{
			this.json_response = response;
		}

		public JSONObject get_response()
		{
			return this.json_response;
		}

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

		public boolean do_retry()
		{
			return this.retry_counter < this.max_retry_num || this.max_retry_num == 0;
		}

		public void reset()
		{
			this.uuid = UUID.randomUUID();
			this.retry_counter = 0;
			this.json_response = null;
		}
	}
}
