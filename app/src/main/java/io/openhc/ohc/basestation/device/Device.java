package io.openhc.ohc.basestation.device;

import java.util.ArrayList;
import java.util.List;

public class Device
{
	private String name;
	private String id;

	private List<Field> fields = new ArrayList<>();

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
		this.fields.set(id, f);
	}

	public void rm_field(int id)
	{
		this.fields.set(id, null);
	}

	@Override
	public String toString()
	{
		return this.name;
	}
}
