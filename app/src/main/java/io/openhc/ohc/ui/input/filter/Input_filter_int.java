package io.openhc.ohc.ui.input.filter;

import android.text.InputFilter;
import android.text.Spanned;

import io.openhc.ohc.basestation.device.Field;

public class Input_filter_int implements InputFilter
{
	private Field field;

	public Input_filter_int(Field field)
	{
		this.field = field;
	}

	@Override
	public CharSequence filter(CharSequence input, int start, int end, Spanned dest, int dstart, int dend)
	{
		try
		{
			double i = Double.parseDouble(input.toString());
			if(i >= this.field.get_min() && i <= this.field.get_max())
				return null;
		}
		catch(NumberFormatException ex)
		{
		}
		return "";
	}
}
