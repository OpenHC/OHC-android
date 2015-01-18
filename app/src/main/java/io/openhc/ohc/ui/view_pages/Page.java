package io.openhc.ohc.ui.view_pages;

import android.os.Bundle;

import io.openhc.ohc.OHC;
import io.openhc.ohc.OHC_ui;

//Basic page class, common Interface
public abstract class Page
{
	protected OHC_ui ctx;
	protected OHC ohc;

	/**
	 * Default page constructor
	 *
	 * @param ctx Instance of OHC_ui
	 * @param ohc OHC instance
	 */
	protected Page(OHC_ui ctx, OHC ohc)
	{
		this.ctx = ctx;
		this.ohc = ohc;
	}

	/**
	 * Function subclasses may implement to allow for easy restoration of previous Ui state
	 * on app reinitialization
	 *
	 * @param saved_state Saved data
	 */
	public void restore_state(Bundle saved_state)
	{

	}

	/**
	 * Function subclasses may implement to allow for easy restoration of previous Ui state
	 * on app reinitialization
	 *
	 * @param save_state Bundle to save data
	 */
	public void store_state(Bundle save_state)
	{

	}

	/**
	 * Returns the id of the layout corresponding to this page
	 *
	 * @return Id of corresponding layout
	 */
	public abstract int get_layout_id();

	/**
	 * Initialization method, called when the page should show up
	 */
	public abstract void init();
}
