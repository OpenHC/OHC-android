package io.openhc.ohc.ui.input.filter;

import android.text.InputFilter;
import android.text.Spanned;

import io.openhc.ohc.basestation.device.Field;

public class Input_filter_string implements InputFilter
{
	private Field field;

	public Input_filter_string(Field field)
	{
		this.field = field;
	}

	@Override
	public CharSequence filter(CharSequence input, int start, int end, Spanned dest, int dstart, int dend)
	{
		String s = input.toString();
		if(s.length() >= this.field.get_min() && s.length() <= this.field.get_max())
			return null;
		return "";
	}
}
