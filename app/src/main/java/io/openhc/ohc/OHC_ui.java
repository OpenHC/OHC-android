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

/**
 * This is the only activity and entry point for the app.
 * All the different layouts are organised in pages that update the layout being displayed
 * by this activity. Also this activity keeps track of the pages visited, creating a history
 * of visited pages to allow for easy navigation by native Android controls like the back button.
 *
 * @author Tobias Schramm
 */
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
			savedInstaceState.putSerializable(this.getString(R.string.ohc_save_state_ohc),
					this.ohc.get_ui_state());
			if(this.ohc.get_basestation() != null)
			{
				savedInstaceState.putSerializable(this.getString(R.string.ohc_save_state_basestation),
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

	/**
	 * Custom implementation of DrawItemView allowing user tracking across pages, enabling easy
	 * navigation via back key
	 *
	 * @param id Layout id
	 * @param by_user Was forward navigation initiated by user
	 */
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
				if(!by_user)
					this.login.init_ohc();
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

	/**
	 * Updates the network status of the login page
	 *
	 * @param state Network state (false = offline | true = online)
	 */
	public void update_network_status(boolean state)
	{
		this.login.update_network_status(state);
	}

	/**
	 * Updates the status TextView on the login page
	 *
	 * @param str Text to display
	 */
	public void set_status(String str)
	{
		this.login.set_status(str);
	}

	/**
	 * Displays the login failed message on the login page and sets the network status accordingly
	 */
	public void login_wrong()
	{
		this.login.login_wrong();
	}

	/**
	 * Sets the login status of the login page
	 *
	 * @param state Network state (false = logged out | true = logging in / logged in)
	 */
	public void set_login_status(boolean state)
	{
		this.login.set_login_status(state);
	}

	/**
	 * Get device ListView from overview page
	 *
	 * @return Device ListView
	 */
	public ListView get_lv_devices()
	{
		return this.overview.get_lv_devices();
	}

	/**
	 * Get field ListView from device page
	 *
	 * @return Field ListView
	 */
	public ListView get_lv_fields()
	{
		return this.device.get_lv_fields();
	}

	/**
	 * Get EditText from action bar (device page)
	 *
	 * @return EditText on action bar
	 */
	public EditText get_et_action_bar_name()
	{
		return this.device.get_et_action_bar_name();
	}

	/**
	 * Returns the login page
	 *
	 * @return Login page
	 */
	public Login get_page_login()
	{
		return this.login;
	}

	/**
	 * Returns the overview page
	 *
	 * @return Overview page
	 */
	public Overview get_page_overview()
	{
		return this.overview;
	}

	/**
	 * Returns the device page
	 *
	 * @return Device page
	 */
	public Device get_page_device()
	{
		return this.device;
	}
}
