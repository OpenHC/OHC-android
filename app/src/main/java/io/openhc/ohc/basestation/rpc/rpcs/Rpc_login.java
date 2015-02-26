package io.openhc.ohc.basestation.rpc.rpcs;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Level;

import io.openhc.ohc.basestation.Basestation;
import io.openhc.ohc.basestation.rpc.Rpc_group;
import io.openhc.ohc.skynet.transaction.Transaction_generator;

/**
 * This RPC returns a login token if the login credentials are valid.
 *
 * @author Tobias Schramm
 */
public class Rpc_login extends Rpc
{
	private String uname;
	private String passwd;

	public final String RPC_METHOD = "login";
	public final String RPC_ATTRIBUTE_UNAME = "uname";
	public final String RPC_ATTRIBUTE_PASSWD = "passwd";

	public Rpc_login(Basestation bs)
	{
		this(bs, null);
	}

	public Rpc_login(Basestation bs, Rpc_group group)
	{
		this(bs, group, null, null);
	}

	public Rpc_login(Basestation bs, Rpc_group group, String uname, String passwd)
	{
		super(bs, group);
		this.uname = uname;
		this.passwd = passwd;
	}

	/**
	 * Sets the username
	 *
	 * @param uname Username
	 */
	public void set_uname(String uname)
	{
		this.uname = uname;
	}

	/**
	 * Sets the password
	 *
	 * @param passwd Password
	 */
	public void set_passwd(String passwd)
	{
		this.passwd = passwd;
	}

	@Override
	protected JSONObject get_json()
	{
		JSONObject json = new JSONObject();
		try
		{
			json.put(this.RPC_ATTRIBUTE_METHOD, this.RPC_METHOD)
					.put(this.RPC_ATTRIBUTE_UNAME, uname)
					.put(this.RPC_ATTRIBUTE_PASSWD, passwd);
		}
		catch(Exception ex)
		{
			this.station.ohc.logger.log(Level.SEVERE, "Failed to compose JSON: " + ex.getMessage(), ex);
		}
		return json;
	}

	@Override
	protected void process_response(JSONObject response) throws Exception
	{
		String token = response.getString("session_token");
		boolean success = response.getBoolean("success");
		this.station.set_session_token(token, success);
	}
}
