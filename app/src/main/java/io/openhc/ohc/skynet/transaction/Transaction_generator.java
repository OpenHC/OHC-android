package io.openhc.ohc.skynet.transaction;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;
import java.util.logging.Level;

import io.openhc.ohc.OHC;

public class Transaction_generator
{
	public static int DEFAULT_RETRY_COUNT = 5;

	public Transaction generate_transaction(JSONObject json)
	{
		return generate_transaction(json, DEFAULT_RETRY_COUNT);
	}

	public Transaction generate_transaction(JSONObject json, int tries)
	{
		UUID uuid = UUID.randomUUID();
		try
		{
			return new Transaction(json, uuid);
		}
		catch (JSONException ex)
		{
			/*This should NEVER, absolutely __NEVER__ happen (except if you plug in a broken JSON object)
			 But that's why it's called an exception, isn't it?*/
			OHC.logger.log(Level.SEVERE, "Transaction generator dying x(");
		}
		return null;
	}

	public class Transaction
	{
		private JSONObject json_request;
		private JSONObject json_response;
		private UUID uuid;
		private int retry_counter;
		private int max_retry;

		protected Transaction(JSONObject json, UUID uuid) throws JSONException
		{
			this(json,uuid, 5);
		}

		protected Transaction(JSONObject json, UUID uuid, int max_retry) throws JSONException
		{
			this.json_request = json;
			this.uuid = uuid;
			this.max_retry = max_retry;
			try
			{
				json.put("transaction_uuid", this.get_uuid());
			}
			catch (JSONException ex)
			{
				OHC.logger.log(Level.SEVERE, "FATAL ERROR! FAILED TO SET TRANSACTION ID!");
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
				return this.get_uuid().equals(json.getString("transaction_uuid"));
			}
			catch (Exception ex) { }
			return false;
		}

		public boolean do_retry()
		{
			return this.retry_counter < this.max_retry || this.max_retry == 0;
		}
	}
}
