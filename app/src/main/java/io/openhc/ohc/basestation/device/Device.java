package io.openhc.ohc.basestation.device;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//OOP representation of a smart device
public class Device implements Serializable
{
	private String name;
	private String id;

	//Fields mapped by their id
	private HashMap<Integer, Field> fields = new HashMap<>();
	private int field_num;

	/**
	 * Only used for serialization
	 */
	public Device()
	{

	}

	/**
	 * Default constructor
	 *
	 * @param name The devices human readable device name
	 * @param id The internal device id
	 */
	public Device(String name, String id)
	{
		this.name = name;
		this.id = id;
	}

	/**
	 * Sets the human readable name of the device
	 *
	 * @param name The devices human readable device name
	 */
	public void set_name(String name)
	{
		this.name = name;
	}

	/**
	 * Adds a field to the device
	 *
	 * @param id The numeric ID of the field
	 * @param field The field
	 */
	public void add_field(int id, Field field)
	{
		this.fields.put(id, field);
	}

	/**
	 * Removes a field from the device
	 *
	 * @param id The numeric ID of the field
	 */
	public void rm_field(int id)
	{
		this.fields.remove(id);
	}

	/**
	 * Replaces an old field by a new one
	 *
	 * @param id The numeric ID of the field
	 * @param field The field
	 */
	public void set_field(int id, Field field)
	{
		this.fields.put(id, field);
	}

	/**
	 * Returns the total amount of fields on this device
	 *
	 * @return The number of fields on this device
	 */
	public int get_field_num()
	{
		return this.field_num;
	}

	/**
	 * Sets the total amount of fields on this device
	 *
	 * @param num The number of fields on this device
	 */
	public void set_field_num(int num)
	{
		this.field_num = num;
	}

	/**
	 * Returns the human readable name of this device
	 *
	 * @return The human readable device name
	 */
	public String get_name()
	{
		return this.name;
	}

	/**
	 * Returns the id of this device
	 *
	 * @return The devices ID
	 */
	public String get_id()
	{
		return this.id;
	}

	/**
	 * Returns a list of all accessible fields
	 *
	 * @return All accessible fields on this device
	 */
	public List<Field> get_fields()
	{
		List<Field> fields = new ArrayList<>();
		Iterator it = this.fields.entrySet().iterator();
		while(it.hasNext())
		{
			Field field = (Field)((Map.Entry)it.next()).getValue();
			if(field.is_accessible())
				fields.add(field);
		}
		return fields;
	}

	/**
	 * Returns the devices human readable name
	 *
	 * @return The human readable name of this device
	 */
	@Override
	public String toString()
	{
		return this.name;
	}
}
