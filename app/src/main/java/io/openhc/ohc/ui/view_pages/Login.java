package io.openhc.ohc.ui.view_pages;

import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import io.openhc.ohc.OHC;
import io.openhc.ohc.OHC_ui;
import io.openhc.ohc.R;

/**
 * The login page
 *
 * @author Tobias Schramm
 */
public class Login extends Page implements View.OnClickListener, TextWatcher
{
	private TextView t_status;
	private Button bt_connect;
	private EditText e_uname;
	private EditText e_passwd;

	private ImageView iv_header_icon;
	private EditText et_header_name;
	private ImageView iv_header_settings;

	private boolean nw_status;
	private boolean lc_status;
	private boolean lg_status;

	private Settings settings;

	/**
	 * Default constructor.
	 *
	 * @param ctx Instance of main activity
	 * @param ohc Instance of OHC
	 * @param settings Settings page
	 */
	public Login(OHC_ui ctx, OHC ohc, Settings settings)
	{
		super(ctx, ohc);
		this.settings = settings;
	}

	@Override
	public void init()
	{
		this.t_status = (TextView)this.ctx.findViewById(R.id.t_status);
		this.bt_connect = (Button)this.ctx.findViewById(R.id.bt_connect);
		this.e_uname = (EditText)this.ctx.findViewById(R.id.e_uname);
		this.e_passwd = (EditText)this.ctx.findViewById(R.id.e_passwd);
		this.e_uname.addTextChangedListener(this);
		this.e_passwd.addTextChangedListener(this);
		this.bt_connect.setOnClickListener(this);
		this.t_status.setOnClickListener(this);
		this.lc_status = this.e_passwd.length() > 0 && this.e_uname.length() > 0;
		this.recalc_bt_connect();
		if(this.ohc.get_basestation() == null)
			this.init_ohc();
		ViewGroup layout = (ViewGroup)this.ctx.getLayoutInflater().inflate(R.layout.action_bar_login, null);
		ActionBar action_bar = this.ctx.getSupportActionBar();
		action_bar.setDisplayShowHomeEnabled(false);
		action_bar.setDisplayShowTitleEnabled(false);
		action_bar.setDisplayShowCustomEnabled(true);
		action_bar.setCustomView(layout);
		this.iv_header_icon = (ImageView)this.ctx.findViewById(R.id.iv_icon);
		this.iv_header_icon.setImageResource(R.drawable.ic_launcher);
		this.iv_header_icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		this.iv_header_settings = (ImageView)this.ctx.findViewById(R.id.iv_settings);
		this.iv_header_settings.setImageResource(R.drawable.ic_action_settings);
		this.iv_header_settings.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		this.iv_header_settings.setOnClickListener(this);
	}

	/**
	 * Initialize OHC
	 */
	public void init_ohc()
	{
		if(this.ohc.get_basestation() != null)
			this.ohc.get_basestation().destroy();
		if(this.settings.is_ip_manually_set())
			this.ohc.init(this.settings.get_ip_address());
		else
			this.ohc.init();
	}

	/**
	 * Calculate visibility of connect button
	 */
	public void recalc_bt_connect()
	{
		this.bt_connect.setEnabled(this.nw_status && this.lc_status && !this.lg_status);
	}

	/**
	 * Set network status and update login button
	 *
	 * @param state State (true = online / false = offline)
	 */
	public void update_network_status(boolean state)
	{
		this.set_nw_status(state);
		this.recalc_bt_connect();
	}

	/**
	 * Set content of status field
	 *
	 * @param str Text to display
	 */
	public void set_status(String str)
	{
		this.t_status.setText(str);
	}

	/**
	 * Display login failed message and set login status
	 */
	public void login_wrong()
	{
		this.lg_status = false;
		this.set_status(this.ctx.getString(R.string.status_login_failed));
		this.recalc_bt_connect();
	}

	/**
	 * Set login status
	 *
	 * @param state State (true = logging in / false = not connected)
	 */
	public void set_login_status(boolean state)
	{
		this.lg_status = state;
		if(this.bt_connect != null)
			this.recalc_bt_connect();
	}

	@Override
	public void onClick(View v)
	{
		if(v == this.bt_connect)
		{
			this.lg_status = true;
			this.recalc_bt_connect();
			this.ohc.connect(this.e_uname.getText().toString(), this.e_passwd.getText().toString());
			this.set_status(this.ctx.getString(R.string.status_connecting));
		}
		else if(v == this.t_status)
		{
			if(!this.nw_status)
			{
				this.ohc.find_basestation_lan();
			}
		}
		else if(v == this.iv_header_settings)
		{
			this.ctx.setContentView(R.layout.activity_settings);
		}
	}

	@Override
	public void beforeTextChanged(CharSequence cs, int start, int count, int after)
	{

	}

	@Override
	public void onTextChanged(CharSequence cs, int start, int before, int count)
	{
		if(this.ohc.get_current_layout() == R.layout.activity_login)
		{
			this.lc_status = this.e_passwd.length() > 0 && this.e_uname.length() > 0;
			this.recalc_bt_connect();
		}
	}

	@Override
	public void afterTextChanged(Editable e)
	{
		if(this.ohc.get_current_layout() == R.layout.activity_device)
		{
			this.ohc.get_basestation().device_set_name(ohc.get_current_dev_id(), e.toString());
		}
	}

	@Override
	public int get_layout_id()
	{
		return R.layout.activity_login;
	}

	/**
	 * Set network status but don't update login button
	 *
	 * @param state State (true = online / false = offline)
	 */
	public void set_nw_status(boolean state)
	{
		this.nw_status = state;
	}
}
