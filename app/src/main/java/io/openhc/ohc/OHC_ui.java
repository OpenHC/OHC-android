package io.openhc.ohc;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;


public class OHC_ui extends ActionBarActivity implements View.OnClickListener, TextWatcher
{
	private TextView t_status;
	private Button bt_connect;
	private EditText e_uname;
	private EditText e_passwd;

	private ListView lv_devices;

	private boolean nw_status;
	private boolean lc_status;
	private boolean lg_status;

	private OHC ohc;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(ohc.get_current_view());
	}

	@Override
	public void setContentView(int id)
	{
		super.setContentView(id);
		if(id == R.layout.activity_ohc_login)
		{
			this.ohc = new OHC(this);
			this.t_status = (TextView) this.findViewById(R.id.t_status);
			this.bt_connect = (Button) this.findViewById(R.id.bt_connect);
			this.e_uname = (EditText) this.findViewById(R.id.e_uname);
			this.e_passwd = (EditText) this.findViewById(R.id.e_passwd);
			this.e_uname.addTextChangedListener(this);
			this.e_passwd.addTextChangedListener(this);
			this.bt_connect.setOnClickListener(this);
			this.t_status.setText(getString(R.string.status_nofind));
			this.ohc.init();
		}
		else if(id == R.layout.activity_ohc_overview)
		{
			this.lv_devices = (ListView) this.findViewById(R.id.lv_devices);
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

	@Override
	public void onClick(View v)
	{
		if(v == this.bt_connect)
		{
			this.lg_status = true;
			this.recalc_bt_connect();
			this.ohc.connect(this.e_uname.getText().toString(), this.e_passwd.getText().toString());
		}
	}

	@Override
	public void beforeTextChanged(CharSequence cs, int start, int count, int after)
	{

	}

	public void onTextChanged(CharSequence cs, int start, int before, int count)
	{
		this.lc_status = this.e_passwd.length() > 0 && this.e_uname.length() > 0;
		this.recalc_bt_connect();
	}

	@Override
	public void afterTextChanged(Editable e)
	{

	}

	public void recalc_bt_connect()
	{
		this.bt_connect.setEnabled(this.nw_status && this.lc_status && !this.lg_status);
	}

	public void update_network_status(boolean state)
	{
		if(!state)
			this.t_status.setText(R.string.status_nofind);
		this.nw_status = state;
		this.recalc_bt_connect();
	}

	public void set_status(String str)
	{
		this.t_status.setText(str);
	}

	public ListView get_lv_devices()
	{
		return this.lv_devices;
	}
}
