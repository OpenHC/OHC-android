package io.openhc.ohc.basestation.device;

import java.util.Objects;

public class Field
{
	private Type type;
	private double min_value;
	private double max_value;

	private Object value;

	public Field(Type type, int min_value, int max_value)
	{
		this.type = type;
		this.min_value = min_value;
		this.max_value = max_value;
	}

	//Sets a new value for this field. Handles UI updating. Throws a ClassCastException if type of
	// supplied value is incompatible with the fields type
	public void set_value(Objects value) throws ClassCastException
	{
		this.value = this.type.get_data_type().cast(value);
	}

	public enum Type
	{
		INT(Integer.class),
		FLOAT(Double.class),
		STRING(String.class),
		ONOFF(Boolean.class),
		BOOL(Boolean.class),
		SLIDER(Double.class);

		private Class c;

		Type(Class c)
		{
			this.c = c;
		}

		public Class get_data_type()
		{
			return this.c;
		}
	}
}
