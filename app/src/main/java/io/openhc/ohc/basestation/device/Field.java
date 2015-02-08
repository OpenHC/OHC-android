package io.openhc.ohc.basestation.device;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.CompoundButton;
import android.widget.SeekBar;

import java.io.Serializable;

import io.openhc.ohc.OHC;

/**
 * This class represents data in the memory of smart devices.
 * All variables on a smart device that can be changed by the user are represented as fields.
 * A field is a memory segment storing data of a specific type.
 *
 * @author Tobias Schramm
 */
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

	/**
	 * Only used for serialization
	 */
	public Field()
	{

	}

	/**
	 * The default constructor. Does not take an initial value for the field but constructs
	 * a default value from the given minimum value
	 *
	 * @param ohc       The ohc instance containing the device this field is associated with
	 * @param devie_id  The internal id of the associated device
	 * @param field_id  The numeric id of this field
	 * @param type      The type of data stored in this field
	 * @param name      The human readable name of this field
	 * @param min_value The minimum value this field can have
	 * @param max_value The maximum value this field can have
	 * @param writable  Is this field writable
	 */
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

	/**
	 * Almost the same as the default constructor but does take an extra argument for the field
	 * value.
	 *
	 * @param ohc       The ohc instance containing the device this field is associated with
	 * @param devie_id  The internal id of the associated device
	 * @param field_id  The numeric id of this field
	 * @param type      The type of data stored in this field
	 * @param name      The human readable name of this field
	 * @param min_value The minimum value this field can have
	 * @param max_value The maximum value this field can have
	 * @param writable  Is this field writable
	 * @param value     Initial value of this field
	 */
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

	/**
	 * Sets and commits a new value for this field. Throws a ClassCastException if the type of
	 * value and the field type don't match
	 *
	 * @param value     The new value
	 * @param from_user Has this change in field value been initiated by the user
	 * @throws ClassCastException
	 */
	public void set_value(Object value, boolean from_user) throws ClassCastException
	{
		//Dynamically cast the supplied object to the appropriate data type
		this.value = this.type.get_data_type().cast(value);
		this.ohc.get_basestation().device_set_field_value(this.device_id, this.field_id, this.value);
	}

	/**
	 * Returns the data type of this field
	 *
	 * @return The data type of this field
	 */
	public Type get_type()
	{
		return this.type;
	}

	/**
	 * Returns the human readable name of this field
	 *
	 * @return Human readable name of this field
	 */
	public String get_name()
	{
		return this.name;
	}

	/**
	 * Returns if this field can be written
	 *
	 * @return Can write field
	 */
	public boolean is_writable()
	{
		return this.writable;
	}

	/**
	 * Returns the minimum value of this field
	 *
	 * @return Minimum field value
	 */
	public double get_min()
	{
		return this.min_value;
	}

	/**
	 * Returns the maximum field value
	 *
	 * @return Maximum field value
	 */
	public double get_max()
	{
		return this.max_value;
	}

	/**
	 * Returns the current value of the field
	 *
	 * @return Current field value
	 */
	public Object get_value()
	{
		return this.value;
	}

	/**
	 * Set if this field is accessible. Inaccessible fields are marked as invalid and thus
	 * not displayed to the user
	 *
	 * @param accessible Accessibility of the field
	 */
	public void set_accessible(boolean accessible)
	{
		this.accessible = accessible;
	}

	/**
	 * Sets the ohc instance that contains the basestation with a reference to this field.
	 * Primarily used for reassigning a ohc instance after deserialization of this class
	 *
	 * @param ohc OHC instance
	 */
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
		//Parse string and set field value
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

	//Called when the value of the SeekBar is changed by the user
	@Override
	public void onProgressChanged(SeekBar sb, int progress, boolean from_user)
	{
		this.set_value(progress, true);
	}

	public boolean is_accessible()
	{
		return this.accessible;
	}

	//All currently possible field data types
	public enum Type
	{
		INT(Integer.class, "int"),
		FLOAT(Double.class, "float"),
		STRING(String.class, "string"),
		ONOFF(Boolean.class, "onoff"),
		BOOL(Boolean.class, "bool"),
		SLIDER(Integer.class, "slider");

		private Class c;

		/**
		 * Field type default constructor
		 *
		 * @param c        Class of data type
		 * @param text_rep Textual representation of the field data type
		 */
		Type(Class c, String text_rep)
		{
			this.c = c;
		}

		/**
		 * Returns the class of the data type associated with this field type
		 *
		 * @return The actual data type
		 */
		public Class get_data_type()
		{
			return this.c;
		}
	}
}
