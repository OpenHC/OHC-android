package io.openhc.ohc.ui.view_pages;

import io.openhc.ohc.OHC;
import io.openhc.ohc.OHC_ui;

public abstract class Page
{
	protected OHC_ui ctx;
	protected OHC ohc;

	protected Page(OHC_ui ctx, OHC ohc)
	{
		this.ctx = ctx;
		this.ohc = ohc;
	}

	public abstract int get_layout_id();
	public abstract void init();
}
