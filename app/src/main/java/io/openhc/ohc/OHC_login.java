package io.openhc.ohc;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.logging.Logger;

import io.openhc.ohc.io.openhc.ohc.basestation.Basestation;
import io.openhc.ohc.io.openhc.ohc.network.BroadcastSender;


public class OHC_login extends ActionBarActivity
{
	private TextView t_status;
	private Button bt_connect;
	private EditText e_uname;
	private EditText e_passwd;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ohc_login);
		this.t_status = (TextView)this.findViewById(R.id.t_status);
		this.bt_connect = (Button)this.findViewById(R.id.bt_connect);
		this.e_uname = (EditText)this.findViewById(R.id.e_uname);
		this.e_passwd = (EditText)this.findViewById(R.id.e_passwd);
		new OHC(this).init();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_ohc_login, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings)
			return true;

		return super.onOptionsItemSelected(item);
	}

	public void update_network_status(boolean state)
	{
		if(state)
		{
			this.t_status.setText(R.string.status_nofind);
		}
		else
			this.t_status.setText("");
		this.findViewById(R.id.bt_connect).setEnabled(OHC.network != null);
	}

	public void set_status(String str)
	{
		this.t_status.setText(str);
	}
}
