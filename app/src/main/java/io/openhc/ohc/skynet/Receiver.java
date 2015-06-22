package io.openhc.ohc.skynet;

import org.json.JSONObject;

/**
 * Created by Tobias on 22.06.2015.
 */
public abstract class Receiver extends Thread
{
	public interface JSON_receiver
	{
		void on_json_receive(JSONObject json);
	}
	public abstract int get_port();
}
