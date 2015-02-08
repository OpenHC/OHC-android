package io.openhc.ohc.basestation.rpc.rpcs;

import org.json.JSONObject;

import java.util.logging.Level;

import io.openhc.ohc.basestation.Basestation;
import io.openhc.ohc.basestation.rpc.Rpc_group;
import io.openhc.ohc.skynet.transaction.Transaction_generator;

/**
 * This RPC returns the number of fields on a device
 *
 * @author Tobias Schramm
 */
public class Rpc_device_get_num_fields extends Rpc
{
	private String id;

	public final String RPC_METHOD = "device_get_num_fields";
	public final String RPC_ATTRIBUTE_ID = "id";

	public Rpc_device_get_num_fields(Basestation bs, Rpc_group group)
	{
		this(bs, group, null);
	}

	public Rpc_device_get_num_fields(Basestation bs, Rpc_group group, String id)
	{
		super(bs, group);
		this.id = id;
	}

	@Override
	public JSONObject get_json()
	{
		JSONObject json = new JSONObject();
		try
		{
			json.put(this.RPC_ATTRIBUTE_METHOD, this.RPC_METHOD)
					.put(RPC_ATTRIBUTE_ID, this.id);
		}
		catch(Exception ex)
		{
			this.station.ohc.logger.log(Level.SEVERE, "Failed to compose JSON: " + ex.getMessage(), ex);
		}
		return json;
	}

	public void set_id(String id)
	{
		this.id = id;
	}

	@Override
	public void on_receive_transaction(Transaction_generator.Transaction transaction)
	{

	}
}
