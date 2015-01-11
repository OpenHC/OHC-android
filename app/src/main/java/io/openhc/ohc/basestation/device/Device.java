package io.openhc.ohc.basestation.device;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import io.openhc.ohc.OHC;

public class Device
{
	private String name;
	private String id;

	private HashMap<Integer, Field> fields = new HashMap<>();
	private int field_num;

	public Device(String name, String id)
	{
		this.name = name;
		this.id = id;
	}

	public void set_name(String name)
	{
		this.name = name;
	}

	public void add_field(int id, Field f)
	{
		this.fields.put(id, f);
	}

	public void rm_field(int id)
	{
		this.fields.remove(id);
	}

	public void set_field(int id, Field field)
	{
		this.fields.put(id, field);
	}

	public int get_field_num()
	{
		return this.field_num;
	}

	public void set_field_num(int num)
	{
		this.field_num = num;
	}

	public String get_name()
	{
		return this.name;
	}

	public String get_id()
	{
		return this.id;
	}

	public List<Field> get_fields()
	{
		List<Field> fields = new ArrayList<>();
		Iterator it = this.fields.entrySet().iterator();
		while(it.hasNext())
		{
			Field field = (Field)((Map.Entry)it.next()).getValue();
			if(field.is_accessible())
				fields.add(field);
			it.remove();
		}
		return fields;
	}

	@Override
	public String toString()
	{
		return this.name;
	}
}
