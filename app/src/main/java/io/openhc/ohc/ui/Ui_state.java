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

	public int get_current_layout()
	{
		return this.current_layout;
	}

	public String get_current_device_id()
	{
		return this.current_device_id;
	}

	public void set_current_layout(int layout)
	{
		this.current_layout = layout;
	}

	public void set_current_device_id(String id)
	{
		this.current_device_id = id;
	}

	public List<Integer> get_page_history()
	{
		return this.page_history;
	}
}
