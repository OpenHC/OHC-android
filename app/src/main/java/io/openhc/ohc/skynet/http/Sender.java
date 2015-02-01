package io.openhc.ohc.skynet.http;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import io.openhc.ohc.OHC;
import io.openhc.ohc.R;
import io.openhc.ohc.skynet.transaction.Transaction_generator;

/**
 * Async HTTP sender
 *
 * @author Tobias Schramm
 */
public class Sender extends io.openhc.ohc.skynet.Sender
{
	private HttpClient client;
	private final int timeout;
	private final InetSocketAddress endpoint;
	private final Transaction_receiver receiver;
	private final boolean secure;
	private final OHC ohc;

	/**
	 * Default constructor. Initializes timeout with a sensible default value
	 * Callback is optional and thus may be null
	 *
	 * @param ohc OHC instance
	 * @param client HTTP client
	 * @param endpoint Address and port of basestation
	 * @param receiver Callback
	 */
	public Sender(OHC ohc, HttpClient client, InetSocketAddress endpoint,
	              Transaction_receiver receiver)
	{
		//Pretty long timeout for bad mobile broadband connections
		this(ohc, client, endpoint,
				ohc.get_context().getResources().getBoolean(R.bool.ohc_network_http_secure_default),
				ohc.get_context().getResources().getInteger(R.integer.ohc_network_timeout_http),
				receiver);
	}

	/**
	 * Constructor allowing for a manual adjustment of timeout
	 * Callback is optional and may be null
	 *
	 * @param ohc OHC instance
	 * @param client HTTP client
	 * @param endpoint Address and port of basestation
	 * @param secure Establish a secure connection if set
	 * @param timeout Timeout
	 * @param receiver Callback
	 */
	public Sender(OHC ohc, HttpClient client, InetSocketAddress endpoint, boolean secure,
	              int timeout, Transaction_receiver receiver)
	{
		this.ohc = ohc;
		this.receiver = receiver;
		this.client = client;
		this.endpoint = endpoint;
		this.secure = secure;
		this.timeout = timeout;
	}

	public Transaction_generator.Transaction doInBackground(Transaction_generator.Transaction... args)
	{
		Transaction_generator.Transaction transaction = args[0];
		try
		{
			List<NameValuePair> form_data = new ArrayList<>();
			String key = this.ohc.get_context().getString(R.string.ohc_network_key_rpc);
			String json_str = transaction.get_json().toString();
			form_data.add(new BasicNameValuePair(key, json_str));
			HttpPost post_request = new HttpPost();
			post_request.setEntity(new UrlEncodedFormEntity(form_data));
			String uri_scheme = this.ohc.get_context().getString(R.string.ohc_network_http_scheme_default);
			if(this.secure)
				uri_scheme = this.ohc.get_context().getString(R.string.ohc_network_http_scheme_secure);
			String uri_format = this.ohc.get_context().getString(R.string.ohc_network_http_format);
			post_request.setURI(URI.create(String.format(uri_format, uri_scheme,
					this.endpoint.getHostString(), this.endpoint.getPort())));
			HttpResponse response = this.client.execute(post_request);
			String body = EntityUtils.toString(response.getEntity());
			try
			{
				JSONObject json = new JSONObject(body);
				if(transaction.is_valid_response(json))
				{
					transaction.set_response(json);
				}
			}
			catch(JSONException ex)
			{
				this.ohc.logger.log(Level.WARNING, "Response contains invalid json", ex);
			}
			catch(Exception ex)
			{
				this.ohc.logger.log(Level.SEVERE, "Encountered an unexpected exception whilst " +
						"processing response data", ex);
			}
		}
		catch(UnsupportedEncodingException ex)
		{
			this.ohc.logger.log(Level.SEVERE, "URL encoding not supported", ex);
		}
		catch(ClientProtocolException ex)
		{
			this.ohc.logger.log(Level.SEVERE, "Protocol not supported", ex);
		}
		catch(IOException ex)
		{
			this.ohc.logger.log(Level.SEVERE, "Failed to send post request", ex);
		}
		return transaction;
	}
}
