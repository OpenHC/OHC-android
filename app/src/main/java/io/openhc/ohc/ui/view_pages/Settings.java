package io.openhc.ohc.ui.view_pages;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.net.InetAddress;

import io.openhc.ohc.OHC;
import io.openhc.ohc.OHC_ui;
import io.openhc.ohc.R;

/**
 * Displays the settings page. Also utilizes the page store and load functions to recreate dialogs
 * when the device orientation is changed
 *
 * @author Tobias Schramm
 */
public class Settings extends Page implements View.OnClickListener, DialogInterface.OnClickListener,
		DialogInterface.OnCancelListener, DialogInterface.OnDismissListener
{
	private SharedPreferences preferences;

	private ImageView iv_header_icon;

	private CheckBox cb_ip_lan_manual;
	private RelativeLayout bl_ip_lan_manual;

	private TextView tv_set_ip_lan;
	private TextView tv_ip_addr_lan;
	private RelativeLayout st_set_ip_lan;

	private EditText et_ip_addr;

	private final String ET_IP_ADDR_STR = "settings_et_ip_addr";

	/**
	 * Default constructor
	 *
	 * @param ctx Instance of main activity
	 * @param ohc Instance of OHC
	 */
	public Settings(OHC_ui ctx, OHC ohc)
	{
		super(ctx, ohc);
		this.preferences = this.ctx.getPreferences(Context.MODE_PRIVATE);
	}

	@Override
	public void store_state(Bundle save_state)
	{
		if(this.et_ip_addr != null)
			save_state.putString(ET_IP_ADDR_STR, this.et_ip_addr.getText().toString());
	}

	@Override
	public void restore_state(Bundle saved_state)
	{
		String et_ip = saved_state.getString(ET_IP_ADDR_STR);
		if(et_ip != null)
		{
			this.show_ip_addr_dialog();
			this.et_ip_addr.setText(et_ip);
		}
	}

	@Override
	public void init()
	{
		this.cb_ip_lan_manual = (CheckBox)this.ctx.findViewById(R.id.cb_ip_lan_manual);
		this.bl_ip_lan_manual = (RelativeLayout)this.ctx.findViewById(R.id.bl_ip_lan_manual);
		this.bl_ip_lan_manual.setOnClickListener(this);
		this.tv_set_ip_lan = (TextView)this.ctx.findViewById(R.id.tv_set_ip_lan);
		this.tv_ip_addr_lan = (TextView)this.ctx.findViewById(R.id.tv_ip_addr_lan);
		this.st_set_ip_lan = (RelativeLayout)this.ctx.findViewById(R.id.st_set_ip_lan);
		this.st_set_ip_lan.setOnClickListener(this);
		ViewGroup layout = (ViewGroup)this.ctx.getLayoutInflater().inflate(R.layout.action_bar_settings, null);
		ActionBar action_bar = this.ctx.getSupportActionBar();
		action_bar.setDisplayShowHomeEnabled(false);
		action_bar.setDisplayShowTitleEnabled(false);
		action_bar.setDisplayShowCustomEnabled(true);
		action_bar.setCustomView(layout);
		this.iv_header_icon = (ImageView)this.ctx.findViewById(R.id.iv_icon);
		this.iv_header_icon.setImageResource(R.drawable.ic_action_settings);
		this.iv_header_icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		this.update_view();
	}

	/**
	 * Update function to display new UI state on config change
	 */
	private void update_view()
	{
		boolean manual = this.preferences.getBoolean(this.ctx.getString(R.string.ohc_ip_lan_manual), false);
		this.cb_ip_lan_manual.setChecked(manual);
		this.tv_set_ip_lan.setEnabled(manual);
		this.tv_ip_addr_lan.setEnabled(manual);
		this.st_set_ip_lan.setClickable(manual);
		String ip_addr = this.preferences.getString(this.ctx.getString(R.string.ohc_ip_lan), "");
		this.tv_ip_addr_lan.setText(ip_addr);
	}

	@Override
	public void onClick(View view)
	{
		if(view == this.bl_ip_lan_manual)
		{
			this.preferences.edit().putBoolean(this.ctx.getString(R.string.ohc_ip_lan_manual),
					!this.cb_ip_lan_manual.isChecked()).commit();
			this.update_view();
			this.ctx.get_page_login().init_ohc();
		}
		else if(view == this.st_set_ip_lan)
		{
			this.show_ip_addr_dialog();
		}
	}

	/**
	 * Shows a custom dialog to manually enter a IP for the basestation address
	 */
	private void show_ip_addr_dialog()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this.ctx);
		builder.setTitle(R.string.ip_address);
		builder.setMessage(R.string.enter_ip);
		builder.setPositiveButton(this.ctx.getString(R.string.ok), this);
		builder.setNegativeButton(this.ctx.getString(R.string.cancel), this);
		LayoutInflater inflater = this.ctx.getLayoutInflater();
		RelativeLayout layout = (RelativeLayout)inflater.inflate(R.layout.dialog_ip_address, null);
		this.et_ip_addr = (EditText)layout.findViewById(R.id.et_ip_addr);
		this.et_ip_addr.setText(this.preferences.getString(this.ctx.getString(R.string.ohc_ip_lan), ""));
		AlertDialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(true);
		dialog.setOnCancelListener(this);
		dialog.setView(layout);
		dialog.show();
	}

	@Override
	public void onClick(DialogInterface iface, int button)
	{
		if(button == Dialog.BUTTON_POSITIVE)
		{
			this.preferences.edit().putString(this.ctx.getString(R.string.ohc_ip_lan),
					this.et_ip_addr.getText().toString()).commit();
			this.update_view();
		}
		this.et_ip_addr = null;
	}

	@Override
	public void onDismiss(DialogInterface iface)
	{
		this.et_ip_addr = null;
	}

	@Override
	public void onCancel(DialogInterface iface)
	{
		this.et_ip_addr = null;
	}

	@Override
	public int get_layout_id()
	{
		return R.layout.activity_settings;
	}

	/**
	 * Returns true if the option for manually setting an ip address is enabled
	 *
	 * @return Is the option for manually setting an ip address enabled
	 */
	public boolean is_ip_manually_set()
	{
		return this.preferences.getBoolean(this.ctx.getString(R.string.ohc_ip_lan_manual), false);
	}

	/**
	 * Returns the manually set ip address
	 * Returns null if the ip address can't be parsed
	 *
	 * @return The mnually set ip address
	 */
	public InetAddress get_ip_address()
	{
		try
		{
			return InetAddress.getByName(this.preferences.getString(this.ctx.getString(R.string.ohc_ip_lan), ""));
		}
		catch(Exception ex)
		{
			return null;
		}
	}
}
