package io.openhc.ohc.ui.view_pages;

import android.support.v7.app.ActionBar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.logging.Level;

import io.openhc.ohc.OHC;
import io.openhc.ohc.OHC_ui;
import io.openhc.ohc.R;

/**
 * Device overview page. Shows a ListView containing all devices attached to the connected
 * basestation
 *
 * @author Tobias Schramm
 */
public class Overview extends Page implements View.OnClickListener, AdapterView.OnItemClickListener
{
	private ListView lv_devices;

	private ImageView iv_header_icon;
	private EditText et_header_name;
	private ImageView iv_header_settings;

	public Overview(OHC_ui ctx, OHC ohc)
	{
		super(ctx, ohc);
	}

	@Override
	public void init()
	{
		this.lv_devices = (ListView)this.ctx.findViewById(R.id.lv_devices);
		this.lv_devices.setOnItemClickListener(this);
		ViewGroup layout = (ViewGroup)this.ctx.getLayoutInflater().inflate(R.layout.action_bar_overview, null);
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

	@Override
	public void onClick(View v)
	{
		 if(v == this.iv_header_settings)
		{

		}
	}

	@Override
	public void onItemClick(AdapterView parent, View view, int position, long id)
	{
		this.ohc.logger.log(Level.INFO, String.format("Item %d in device view clicked", position));
		this.ohc.device_show_details(position);
	}

	public ListView get_lv_devices()
	{
		return this.lv_devices;
	}

	public int get_layout_id()
	{
		return R.layout.activity_overview;
	}
}
