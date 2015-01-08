package io.openhc.ohc.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

import io.openhc.ohc.basestation.device.Field;

public class Field_adapter extends ArrayAdapter
{
	public Field_adapter(Context ctx, int resource, List<Field> fields)
	{
		super(ctx, resource, fields);
	}

	@Override
	public View getView(int position, View view, ViewGroup group)
	{
		return null;
	}
}
