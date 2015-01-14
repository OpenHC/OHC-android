package io.openhc.ohc.basestation.device;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.CompoundButton;
import android.widget.SeekBar;

import java.io.Serializable;

import io.openhc.ohc.OHC;

public class Field implements TextWatcher, CompoundButton.OnCheckedChangeListener,
		SeekBar.OnSeekBarChangeListener, Serializable
{
	private transient OHC ohc; //Don't try to serialize OHC instance. It must be recreated @ app create
	private String device_id;
	private int field_id;
	private Type type;
	private String name;
	private double min_value;
	private double max_value;
	private boolean writable;
	private boolean accessible = true;

	private Object value;

	private String serial_value_store;

	public Field()
	{

	}

	public Field(OHC ohc, String devie_id, int field_id, Type type, String name, double min_value, double max_value, boolean writable)
	{
		this(ohc, devie_id, field_id, type, name, min_value, max_value, writable, null);
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

	public Field(OHC ohc, String devie_id, int field_id, Type type, String name, double min_value, double max_value, boolean writable, Object value)
	{
		this.ohc = ohc;
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
	public void set_value(Object value, boolean from_user) throws ClassCastException
	{
		//Dynamically cast the supplied object to the appropriate data type
		this.value = this.type.get_data_type().cast(value);
		this.ohc.get_basestation().device_set_field_value(this.device_id, this.field_id, this.value);
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

	public void set_accessible(boolean accessible)
	{
		this.accessible = accessible;
	}

	public void set_ohc_instance(OHC ohc)
	{
		this.ohc = ohc;
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
				this.set_value(Integer.parseInt(str), true);
				break;
			case FLOAT:
				this.set_value(Double.parseDouble(str), true);
				break;
			case STRING:
				this.set_value(str, true);
				break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton bt, boolean state)
	{
		this.set_value(state, true);
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
		this.set_value(progress, true);
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
		SLIDER(Integer.class, "slider");

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
