package io.openhc.ohc;

import android.content.res.Configuration;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONObject;

import java.net.InetAddress;
import java.util.List;
import java.util.logging.Level;

import io.openhc.ohc.skynet.Broadcaster;
import io.openhc.ohc.skynet.transaction.Transaction_generator;


public class OHC_ui extends ActionBarActivity implements View.OnClickListener, TextWatcher,
		AdapterView.OnItemClickListener
{
	private TextView t_status;
	private Button bt_connect;
	private EditText e_uname;
	private EditText e_passwd;

	private ListView lv_devices;
	private ListView lv_fields;

	private ImageView iv_header_icon;
	private EditText et_header_name;

	private boolean nw_status;
	private boolean lc_status;
	private boolean lg_status;

	private OHC ohc;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if(this.ohc == null)
			this.ohc = new OHC(this);
		this.setContentView(this.ohc.get_current_view());
	}

	@Override
	public void setContentView(int id)
	{
		super.setContentView(id);
		this.ohc.set_current_view(id);
		ViewGroup layout = null;
		if(id == R.layout.activity_ohc_login)
		{
			this.t_status = (TextView)this.findViewById(R.id.t_status);
			this.bt_connect = (Button)this.findViewById(R.id.bt_connect);
			this.e_uname = (EditText)this.findViewById(R.id.e_uname);
			this.e_passwd = (EditText)this.findViewById(R.id.e_passwd);
			this.e_uname.addTextChangedListener(this);
			this.e_passwd.addTextChangedListener(this);
			this.bt_connect.setOnClickListener(this);
			this.ohc.init();
			layout = (ViewGroup)this.getLayoutInflater().inflate(R.layout.action_bar_login, null);
		}
		else if(id == R.layout.activity_ohc_overview)
		{
			this.lv_devices = (ListView)this.findViewById(R.id.lv_devices);
			this.lv_devices.setOnItemClickListener(this);
			layout = (ViewGroup)this.getLayoutInflater().inflate(R.layout.action_bar_overview, null);
		}
		else if(id == R.layout.activity_ohc_device)
		{
			this.lv_fields = (ListView)this.findViewById(R.id.lv_fields);
			layout = (ViewGroup)this.getLayoutInflater().inflate(R.layout.action_bar_device, null);
		}
		ActionBar action_bar = this.getSupportActionBar();
		action_bar.setDisplayShowHomeEnabled(false);
		action_bar.setDisplayShowTitleEnabled(false);
		action_bar.setDisplayShowCustomEnabled(true);
		action_bar.setCustomView(layout);
		this.iv_header_icon = (ImageView)this.findViewById(R.id.iv_icon);
		this.iv_header_icon.setImageResource(R.drawable.ic_launcher);
		this.iv_header_icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		if(id == R.layout.activity_ohc_device)
		{
			this.et_header_name = (EditText)this.findViewById(R.id.et_name);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_ohc_login, menu);
		return true;
	}

	@Override
	public void onBackPressed()
	{
		if(this.ohc.get_current_view() == R.layout.activity_ohc_device)
		{
			ohc.draw_device_overview();
		}
		else if(this.ohc.get_current_view() == R.layout.activity_ohc_overview)
		{
			ohc.draw_login_page();
		}
		else
			super.onBackPressed();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if(id == R.id.action_settings)
			return true;

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v)
	{
		if(v == this.bt_connect)
		{
			this.lg_status = true;
			this.recalc_bt_connect();
			this.ohc.connect(this.e_uname.getText().toString(), this.e_passwd.getText().toString());
			this.set_status(getString(R.string.status_connecting));
		}
	}

	@Override
	public void beforeTextChanged(CharSequence cs, int start, int count, int after)
	{

	}

	@Override
	public void onTextChanged(CharSequence cs, int start, int before, int count)
	{
		if(this.ohc.get_current_view() == R.layout.activity_ohc_login)
		{
			this.lc_status = this.e_passwd.length() > 0 && this.e_uname.length() > 0;
			this.recalc_bt_connect();
		}
	}

	@Override
	public void afterTextChanged(Editable e)
	{
		if(this.ohc.get_current_view() == R.layout.activity_ohc_device)
		{
			this.ohc.get_basestation().device_set_name(ohc.get_current_device(), e.toString());
		}
	}

	@Override
	public void onItemClick(AdapterView parent, View view, int position, long id)
	{
		if(this.ohc.get_current_view() == R.layout.activity_ohc_overview)
		{
			OHC.logger.log(Level.WARNING.INFO, String.format("Item %d in device view clicked", position));
			this.ohc.device_show_details(position);
		}
	}

	public void recalc_bt_connect()
	{
		this.bt_connect.setEnabled(this.nw_status && this.lc_status && !this.lg_status);
	}

	public void update_network_status(boolean state)
	{
		this.nw_status = state;
		this.recalc_bt_connect();
	}

	public void set_status(String str)
	{
		this.t_status.setText(str);
	}

	public void login_wrong()
	{
		this.lg_status = false;
		this.set_status(getString(R.string.status_login_failed));
		this.recalc_bt_connect();
	}

	public void set_login_status(boolean state)
	{
		this.lg_status = state;
		if(this.bt_connect != null)
			this.recalc_bt_connect();
	}

	public ListView get_lv_devices()
	{
		return this.lv_devices;
	}

	public ListView get_lv_fields()
	{
		return this.lv_fields;
	}

	public ImageView get_iv_action_bar_icon()
	{
		return this.iv_header_icon;
	}

	public EditText get_et_action_bar_name()
	{
		return this.et_header_name;
	}
}
