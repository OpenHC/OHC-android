package io.openhc.ohc.basestation.device;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.CompoundButton;
import android.widget.SeekBar;

import java.util.HashMap;
import java.util.Objects;

import io.openhc.ohc.basestation.Basestation;

public class Field implements TextWatcher, CompoundButton.OnCheckedChangeListener,
		SeekBar.OnSeekBarChangeListener
{
	private Basestation station;
	private String device_id;
	private int field_id;
	private Type type;
	private String name;
	private double min_value;
	private double max_value;
	private boolean writable;
	private boolean accessible = true;

	private Object value;

	public Field()
	{
		this.accessible = false;
	}

	public Field(Basestation bs, String devie_id, int field_id, Type type, String name, double min_value, double max_value, boolean writable)
	{
		this(bs, devie_id, field_id, type, name, min_value, max_value, writable, null);
		switch(type)
		{
			case SLIDER:
			case INT:
				this.value = new Integer((int)this.min_value);
				break;
			case FLOAT:
				this.value = new Double((int)this.max_value);
				break;
			case BOOL:
			case ONOFF:
				this.value = new Boolean(false);
				break;
			case STRING:
				String s = "";
				for(int i = 0; i < Math.ceil(this.min_value); i++)
					s += "-";
				this.value = s;
		}
	}

	public Field(Basestation bs, String devie_id, int field_id, Type type, String name, double min_value, double max_value, boolean writable, Object value)
	{
		this.station = bs;
		this.device_id = devie_id;
		this.field_id = field_id;
		this.type = type;
		this.name = name;
		this.min_value = min_value;
		this.max_value = max_value;
		this.writable = writable;
		this.value = value;
	}

	/*Sets a new value for this field. Throws a ClassCastException if type of
	* supplied value is incompatible with the fields type*/
	public void set_value(Object value) throws ClassCastException
	{
		this.value = this.type.get_data_type().cast(value);
		this.station.device_set_field_value(this.device_id, this.field_id, this.value);
	}

	public Type get_type()
	{
		return this.type;
	}

	public String get_name()
	{
		return this.name;
	}

	public boolean is_writable()
	{
		return this.writable;
	}

	public double get_min()
	{
		return this.min_value;
	}

	public double get_max()
	{
		return this.max_value;
	}

	public Object get_value()
	{
		return this.value;
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
		String str = e.toString();
		switch(this.type)
		{
			case INT:
				this.set_value(Integer.parseInt(str));
				break;
			case FLOAT:
				this.set_value(Double.parseDouble(str));
				break;
			case STRING:
				this.set_value(str);
				break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton bt, boolean state)
	{
		this.set_value(state);
	}

	@Override
	public void onStartTrackingTouch(SeekBar sb)
	{

	}

	@Override
	public void onStopTrackingTouch(SeekBar sb)
	{

	}

	@Override
	public void onProgressChanged(SeekBar sb, int progress, boolean from_user)
	{
		this.set_value(progress);
	}

	public boolean is_accessible()
	{
		return this.accessible;
	}

	public enum Type
	{
		INT(Integer.class, "int"),
		FLOAT(Double.class, "float"),
		STRING(String.class, "string"),
		ONOFF(Boolean.class, "onoff"),
		BOOL(Boolean.class, "bool"),
		SLIDER(Double.class, "slider");

		private Class c;

		Type(Class c, String text_rep)
		{
			this.c = c;
		}

		public Class get_data_type()
		{
			return this.c;
		}
	}
}
