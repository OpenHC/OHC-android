package io.openhc.ohc.ui.view_pages;

import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import io.openhc.ohc.OHC;
import io.openhc.ohc.OHC_ui;
import io.openhc.ohc.R;

//Defines the UI of a Device page
public class Device extends Page implements View.OnClickListener, TextWatcher
{
	private ListView lv_fields;

	private ImageView iv_header_icon;
	private EditText et_header_name;
	private ImageView iv_header_settings;

	/**
	 * Default constructor
	 *
	 * @param ctx Instance of main activity
	 * @param ohc OHC instance
	 */
	public Device(OHC_ui ctx, OHC ohc)
	{
		super(ctx, ohc);
	}

	@Override
	public void init()
	{
		this.lv_fields = (ListView)this.ctx.findViewById(R.id.lv_fields);
		ViewGroup layout = (ViewGroup)this.ctx.getLayoutInflater().inflate(R.layout.action_bar_device, null);
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
		this.et_header_name = (EditText)this.ctx.findViewById(R.id.et_name);
	}

	@Override
	public void onClick(View v)
	{
		if(v == this.iv_header_settings)
		{

		}
	}

	@Override
	public void beforeTextChanged(CharSequence cs, int start, int count, int after)
	{

	}

	@Override
	public void onTextChanged(CharSequence cs, int start, int before, int count)
	{

	}

	@Override
	public void afterTextChanged(Editable e)
	{
		this.ohc.get_basestation().device_set_name(ohc.get_current_dev_id(), e.toString());
	}

	@Override
	public int get_layout_id()
	{
		return R.layout.activity_device;
	}

	/**
	 * Get the view containing the device fields
	 *
	 * @return The view containing the devices fields
	 */
	public ListView get_lv_fields()
	{
		return this.lv_fields;
	}

	/**
	 * Get the EditText of the action bar
	 *
	 * @return The EditText field embedded in the action bar
	 */
	public EditText get_et_action_bar_name()
	{
		return this.et_header_name;
	}
}
