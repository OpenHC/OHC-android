package io.openhc.ohc.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OHC_Logger
{
	private final Logger logger;

	public OHC_Logger(Logger logger)
	{
		this.logger = logger;
	}

	public void log(Level level, String str)
	{
		this.logger.log(level, str);
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
