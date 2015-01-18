package io.openhc.ohc.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.openhc.ohc.R;

public class Ui_state implements Serializable
{
	private int current_layout = R.layout.activity_login;
	private String current_device_id = null;
	private List<Integer> page_history = new ArrayList<>();

	/**
	 * Returns the id of the saved layout
	 *
	 * @return Id of saved layout
	 */
	public int get_current_layout()
	{
		return this.current_layout;
	}

	/**
	 * Returns the internal id of the currently viewed device
	 *
	 * @return Internal id of viewed device
	 */
	public String get_current_device_id()
	{
		return this.current_device_id;
	}

	/**
	 * Sets the id of the current layout
	 *
	 * @param layout Layout id
	 */
	public void set_current_layout(int layout)
	{
		this.current_layout = layout;
	}

	/**
	 * Set the internal id of the currently viewed device
	 *
	 * @param id Internal device id
	 */
	public void set_current_device_id(String id)
	{
		this.current_device_id = id;
	}

	/**
	 * Get the page history
	 *
	 * @return List of visited app layouts
	 */
	public List<Integer> get_page_history()
	{
		return this.page_history;
	}
}
