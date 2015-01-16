package io.openhc.ohc;

import android.content.res.Configuration;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.logging.Level;

import io.openhc.ohc.skynet.Broadcaster;
import io.openhc.ohc.skynet.transaction.Transaction_generator;
import io.openhc.ohc.ui.view_pages.Device;
import io.openhc.ohc.ui.view_pages.Login;
import io.openhc.ohc.ui.view_pages.Overview;
import io.openhc.ohc.ui.view_pages.Page;


public class OHC_ui extends ActionBarActivity
{
	private Login login;
	private Overview overview;
	private Device device;

	private OHC ohc;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if(this.ohc == null)
			if(savedInstanceState != null)
				try
				{
					this.ohc = new OHC(this, savedInstanceState);
				}
				catch(IOException ex)
				{
					Log.e(this.getString(R.string.log_tag), "Failed to load saved state", ex);
					this.ohc = new OHC(this);
				}
			else
				this.ohc = new OHC(this);
		this.login = new Login(this, this.ohc);
		this.login.set_nw_status(this.ohc.get_basestation() != null);
		this.overview = new Overview(this, this.ohc);
		this.device = new Device(this, this.ohc);
		switch(this.ohc.get_current_layout())
		{
			case R.layout.activity_ohc_overview:
				this.ohc.draw_device_overview();
				break;
			case R.layout.activity_ohc_device:
				this.ohc.draw_device_view(this.ohc.get_current_dev_id());
				break;
			default:
				this.setContentView(this.ohc.get_current_layout());
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstaceState)
	{
		super.onSaveInstanceState(savedInstaceState);
		if(this.ohc != null)
		{
			savedInstaceState.putSerializable(this.getString(R.string.save_id_ohc),
					this.ohc.get_ui_state());
			if(this.ohc.get_basestation() != null)
			{
				savedInstaceState.putSerializable(this.getString(R.string.save_id_basestation),
						this.ohc.get_basestation().get_state());
			}
		}
	}

	@Override
	public void setContentView(int id)
	{
		super.setContentView(id);
		this.ohc.set_current_layout(id);
		ViewGroup layout = null;
		if(id == R.layout.activity_ohc_login)
		{
			this.login.init();
		}
		else if(id == R.layout.activity_ohc_overview)
		{
			this.overview.init();
		}
		else if(id == R.layout.activity_ohc_device)
		{
			this.device.init();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		return false;
	}

	@Override
	public void onBackPressed()
	{
		if(this.ohc.get_current_layout() == R.layout.activity_ohc_device)
		{
			ohc.draw_device_overview();
		}
		else if(this.ohc.get_current_layout() == R.layout.activity_ohc_overview)
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

	public void recalc_bt_connect()
	{
		this.login.recalc_bt_connect();
	}

	public void update_network_status(boolean state)
	{
		this.login.update_network_status(state);
	}

	public void set_status(String str)
	{
		this.login.set_status(str);
	}

	public void login_wrong()
	{
		this.login.login_wrong();
	}

	public void set_login_status(boolean state)
	{
		this.login.set_login_status(state);
	}

	public ListView get_lv_devices()
	{
		return this.overview.get_lv_devices();
	}

	public ListView get_lv_fields()
	{
		return this.device.get_lv_fields();
	}

	public EditText get_et_action_bar_name()
	{
		return this.device.get_et_action_bar_name();
	}

	public Login get_page_login()
	{
		return this.login;
	}

	public Overview get_page_overview()
	{
		return this.overview;
	}

	public Device get_page_device()
	{
		return this.device;
	}
}
