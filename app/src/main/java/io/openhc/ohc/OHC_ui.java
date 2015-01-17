package io.openhc.ohc;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;

import java.io.IOException;
import java.util.List;

import io.openhc.ohc.ui.view_pages.Device;
import io.openhc.ohc.ui.view_pages.Login;
import io.openhc.ohc.ui.view_pages.Overview;
import io.openhc.ohc.ui.view_pages.Page;
import io.openhc.ohc.ui.view_pages.Settings;


public class OHC_ui extends ActionBarActivity
{
	private Login login;
	private Overview overview;
	private Device device;
	private Settings settings;

	private Page current_view;

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
		this.overview = new Overview(this, this.ohc);
		this.device = new Device(this, this.ohc);
		this.settings = new Settings(this, this.ohc);
		this.login = new Login(this, this.ohc, this.settings);
		this.login.set_nw_status(this.ohc.get_basestation() != null);
		this.setContentView(this.ohc.get_current_layout());
		this.current_view.restore_state(savedInstanceState);
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
		if(this.current_view != null)
			this.current_view.store_state(savedInstaceState);
	}

	@Override
	public void setContentView(int id)
	{
		this.setContentView(id, true);
	}

	public void setContentView(int id, boolean by_user)
	{
		super.setContentView(id);
		if(this.ohc.get_current_layout() != id && by_user)
			this.ohc.get_ui_state().get_page_history().add(this.ohc.get_current_layout());
		this.ohc.set_current_layout(id);
		switch(id)
		{
			case R.layout.activity_login:
				this.login.init();
				this.current_view = this.login;
				break;
			case R.layout.activity_settings:
				this.settings.init();
				this.current_view = this.settings;
				break;
			case R.layout.activity_overview:
				this.overview.init();
				this.current_view = this.overview;
				this.ohc.draw_device_overview();
				break;
			case R.layout.activity_device:
				this.device.init();
				this.current_view = this.device;
				this.ohc.draw_device_view(this.ohc.get_current_dev_id());
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
		List<Integer> page_history = this.ohc.get_ui_state().get_page_history();
		if(!page_history.isEmpty())
		{
			int index = page_history.size() - 1;
			this.setContentView(page_history.get(index), false);
			page_history.remove(index);
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
