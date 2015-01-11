package io.openhc.ohc.ui;

import android.content.Context;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.List;
import java.util.logging.Level;

import io.openhc.ohc.OHC;
import io.openhc.ohc.R;
import io.openhc.ohc.basestation.device.Device;
import io.openhc.ohc.basestation.device.Field;
import io.openhc.ohc.ui.input.filter.Input_filter_float;
import io.openhc.ohc.ui.input.filter.Input_filter_int;
import io.openhc.ohc.ui.input.filter.Input_filter_string;

public class Field_adapter extends ArrayAdapter<Field>
{
	private List<Field> fields;

	public Field_adapter(Context ctx, int resource, List<Field> fields)
	{
		super(ctx, resource, fields);
		this.fields = fields;
	}

	private int dip_to_px(double dip)
	{
		float scale = getContext().getResources().getDisplayMetrics().density;
		return (int)(dip * scale + 0.5F);
	}

	@Override
	public View getView(int position, View view, ViewGroup parent)
	{
		LayoutInflater inflater = (LayoutInflater)this.getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ViewGroup group = (ViewGroup)inflater.inflate(R.layout.list_view_group, parent, false);
		TextView tv_key = new TextView(this.getContext());
		group.addView(tv_key);
		Field field = this.fields.get(position);
		tv_key.setText(field.get_name());
		TableLayout.LayoutParams layout = new TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				this.dip_to_px(64), 0.5F);
		layout.setMargins(dip_to_px(5), 0, dip_to_px(5), 0);
		tv_key.setLayoutParams(layout);
		OHC.logger.log(Level.INFO, "Generating view for field " + field.get_name());
		View v_value;
		switch(field.get_type())
		{
			case INT:
				EditText et_value_int = new EditText(this.getContext());
				et_value_int.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
				et_value_int.setFilters(new InputFilter[]{new Input_filter_int(field)});
				et_value_int.addTextChangedListener(field);
				et_value_int.setText(Integer.toString((Integer)field.get_value()));
				v_value = et_value_int;
				break;
			case FLOAT:
				EditText et_value_float = new EditText(this.getContext());
				et_value_float.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
				et_value_float.setFilters(new InputFilter[]{new Input_filter_float(field)});
				et_value_float.addTextChangedListener(field);
				et_value_float.setText(Double.toString((Double)field.get_value()));
				v_value = et_value_float;
				break;
			case BOOL:
				CheckBox cb_value = new CheckBox(this.getContext());
				cb_value.setChecked((Boolean)field.get_value());
				cb_value.setOnCheckedChangeListener(field);
				v_value = cb_value;
				break;
			case ONOFF:
				Switch sw_value = new Switch(this.getContext());
				sw_value.setChecked((Boolean)field.get_value());
				sw_value.setOnCheckedChangeListener(field);
				v_value = sw_value;
				break;
			case STRING:
				EditText et_value = new EditText(this.getContext());
				et_value.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
				et_value.setFilters(new InputFilter[]{new Input_filter_string(field)});
				et_value.addTextChangedListener(field);
				et_value.setText((String)field.get_value());
				v_value = et_value;
				break;
			case SLIDER:
				SeekBar sb_value = new SeekBar(this.getContext());
				sb_value.setOnSeekBarChangeListener(field);
				sb_value.setMax((int)field.get_max());
				sb_value.setProgress((Integer)field.get_value());
				v_value = sb_value;
				break;
			default:
				v_value = null;
		}
		v_value.setEnabled(field.is_writable());
		v_value.setLayoutParams(layout);
		group.addView(v_value);
		return group;
	}
}
