package io.openhc.ohc.logging;

import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;

/**
 * Simple logger class to unify Log output in terms of log tags and verbosity
 *
 * @author Tobias Schramm
 */
public class OHC_Logger
{
	private final String tag;

	/**
	 * Default constructor.
	 *
	 * @param tag The common log tag
	 */
	public OHC_Logger(String tag)
	{
		this.tag = tag;
	}

	/**
	 * Prints out a simple log message
	 *
	 * @param level Severity of the message
	 * @param str   The log message
	 */
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

	/**
	 * Prints out a stacktrace
	 *
	 * @param level Severity
	 * @param ex    Exception
	 */
	public void log(Level level, Exception ex)
	{
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);
		this.log(level, sw.toString());
	}

	/**
	 * Prints out a log message together with a stacktrace
	 *
	 * @param level Severity
	 * @param str   Message
	 * @param ex    Exception
	 */
	public void log(Level level, String str, Exception ex)
	{
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);
		this.log(level, String.format("%s\n%s", str, sw.toString()));
	}
}
