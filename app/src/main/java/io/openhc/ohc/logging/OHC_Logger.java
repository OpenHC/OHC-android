package io.openhc.ohc.logging;

import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.openhc.ohc.OHC;

public class OHC_Logger
{
	private final String tag;

	public OHC_Logger(String tag)
	{
		this.tag = tag;
	}

	public void log(Level level, String str)
	{
		if(level == Level.SEVERE)
		{
			Log.e(this.tag, str);
		}
		else if(level == Level.WARNING)
		{
			Log.w(this.tag, str);
		}
		else
		{
			Log.d(this.tag, str);
		}
	}

	public void log(Level level, Exception ex)
	{
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);
		this.log(level, sw.toString());
	}

	public void log(Level level, String str, Exception ex)
	{
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);
		this.log(level, String.format("%s\n%s", str, sw.toString()));
	}
}
