package io.openhc.ohc.ui.input.filter;

import android.text.InputFilter;
import android.text.Spanned;

import io.openhc.ohc.basestation.device.Field;

/**
 * Filters an EditText for any non float values
 *
 * @author Tobias Schramm
 */
public class Input_filter_float implements InputFilter
{
	private Field field;

	/**
	 * Default constructor
	 *
	 * @param field Field to filter
	 */
	public Input_filter_float(Field field)
	{
		this.field = field;
	}

	@Override
	public CharSequence filter(CharSequence input, int start, int end, Spanned dest, int dstart, int dend)
	{
		try
		{
			int i = Integer.parseInt(input.toString());
			if(i >= this.field.get_min() && i <= this.field.get_max())
				return null;
		}
		catch(NumberFormatException ex)
		{
		}
		return "";
	}
}
