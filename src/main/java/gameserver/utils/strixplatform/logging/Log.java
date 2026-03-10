package gameserver.utils.strixplatform.logging;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import gameserver.utils.strixplatform.configs.MainConfig;

public class Log
{
	private static FileLog MAINLOG = null;
	private static FileLog DEBUGLOG = null;
	private static FileLog AUDITLOG = null;
	private static FileLog ERRORLOG = null;
	private static FileLog AUTHLOG = null;
	
	static
	{
		try
		{
			MAINLOG = new FileLog(MainConfig.LOG_FILE);
			DEBUGLOG = new FileLog(MainConfig.DEBUG_LOG_FILE);
			AUDITLOG = new FileLog(MainConfig.AUDIT_LOG_FILE);
			ERRORLOG = new FileLog(MainConfig.ERROR_LOG_FILE);
			AUTHLOG = new FileLog(MainConfig.AUTH_LOG_FILE);
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void error(final String msg)
	{
		final Calendar cal = Calendar.getInstance();
		final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		System.err.println("[" + sdf.format(cal.getTime()) + "] StrixPlatform: " + msg);
		ERRORLOG.log("ERROR - " + msg);
	}
	
	public static void info(final String msg)
	{
		final Calendar cal = Calendar.getInstance();
		final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		System.out.println("[" + sdf.format(cal.getTime()) + "] StrixPlatform: " + msg);
		MAINLOG.log("INFO - " + msg);
	}
	
	public static void warn(final String msg)
	{
		final Calendar cal = Calendar.getInstance();
		final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		System.err.println("[" + sdf.format(cal.getTime()) + "] StrixPlatform: " + msg);
		ERRORLOG.log("WARN - " + msg);
	}
	
	public static void log(final String msg)
	{
		final Calendar cal = Calendar.getInstance();
		final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		System.out.println("[" + sdf.format(cal.getTime()) + "] StrixPlatform: " + msg);
		MAINLOG.log("LOG - " + msg);
	}
	
	public static void audit(final String msg)
	{
		final Calendar cal = Calendar.getInstance();
		final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		System.out.println("[" + sdf.format(cal.getTime()) + "] StrixPlatform: " + msg);
		AUDITLOG.log("AUDIT - " + msg);
	}
	
	public static void debug(final String msg)
	{
		final Calendar cal = Calendar.getInstance();
		final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		System.out.println("[" + sdf.format(cal.getTime()) + "] StrixPlatform: " + msg);
		DEBUGLOG.log("DEBUG - " + msg);
	}
	
	public static void auth(final String msg)
	{
		final Calendar cal = Calendar.getInstance();
		final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		System.out.println("[" + sdf.format(cal.getTime()) + "] StrixPlatform: " + msg);
		AUTHLOG.log("AUTH - " + msg);
	}
}