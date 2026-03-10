package gameserver.utils.strixplatform.configs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Properties;

import gameserver.utils.strixplatform.logging.Log;
import gameserver.utils.strixplatform.utils.FailedCheckResolve;

public class MainConfig
{
	public static final String CONFIG_FILE = "./strix-platform/strix_platform_main.properties";
	
	public static final String LOG_FILE = "./strix-platform/log/general.log";
	public static final String DEBUG_LOG_FILE = "./strix-platform/log/debug.log";
	public static final String AUDIT_LOG_FILE = "./strix-platform/log/audit.log";
	public static final String ERROR_LOG_FILE = "./strix-platform/log/error.log";
	public static final String AUTH_LOG_FILE = "./strix-platform/log/auth.log";
	
	public static final String STRIX_CLIENT_UPDATE_CHECK_URL = "http://l2j-project.su/info/client_version.txt";
	
	public static final int PROTOCOL_VERSION_DATA_SIZE = 264;
	public static final int CLIENT_DATA_SIZE = 260;
	
	public static boolean STRIX_PLATFORM_ENABLED;
	public static boolean STRIX_PLATFORM_DEBUG_ENABLED;
	public static boolean STRIX_PLATFORM_ENABLED_AUTHLOG;
	public static boolean STRIX_PLATFORM_GAME_SESSION_CHECK_ENABLED;
	public static boolean STRIX_PLATFORM_CLIENT_BACK_NOTIFICATION_ENABLED;
	public static boolean STRIX_PLATFORM_CHECK_CLIENT_SIDE_VERSION;
	public static int STRIX_PLATFORM_MANUAL_CLIENT_SIDE_VERSION;
	public static int CLIENT_SIDE_VERSION_STORED;
	
	public static FailedCheckResolve FAILED_CHECK_LICENSE_KEY;
	public static FailedCheckResolve FAILED_CHECK_GAME_SESSION;
	public static FailedCheckResolve FAILED_CHECK_FILES_CHECKSUM;
	public static FailedCheckResolve FAILED_CHECK_DETECTION_INFO;
	public static FailedCheckResolve FAILED_CHECK_LAUNCH_STATE;
	public static FailedCheckResolve FAILED_CHECK_CLIENT_SIDE_VERSION;
	public static FailedCheckResolve FAILED_CHECK_ACTIVE_WINDOW;
	
	public static String STRIX_PLATFORM_KEY;
	public static int STRIX_PLATFORM_SECOND_KEY;
	
	public static int STRIX_PLATFORM_ACTIVE_WINDOW_COUNT;
	public static int STRIX_PLATFORM_HWID_BLOCK_TO_LOCK;
	public static long STRIX_PLATFORM_AUTOMATICAL_BAN_TIME;
	public static long[] STRIX_PLATFORM_FILES_CHECKSUM;
	public static boolean STRIX_PLATFORM_VIRTUAL_MACHINE_CHECK_ENABLED;
	public static boolean STRIX_PLATFORM_ONLY_LAUNCHER_CHECK_ENABLED;
	public static boolean STRIX_PLATFORM_ANTIBRUTE;
	public static boolean STRIX_PLATFORM_DRAW;
	public static String STRIX_PLATFORM_DRAW_TEXT;
	
	public static String STX_PF_XOR_KEY;
	
	private static boolean parseConfigFile()
	{
		FileInputStream fileInputStream = null;
		InputStreamReader inputStreamReader = null;
		BufferedReader bufferedReader = null;
		
		try
		{
			fileInputStream = new FileInputStream(new File(CONFIG_FILE));
			inputStreamReader = new InputStreamReader(fileInputStream, Charset.forName("UTF-8"));
			bufferedReader = new BufferedReader(inputStreamReader);
			
			final Properties protectionSettings = new Properties();
			
			protectionSettings.load(bufferedReader);
			
			STRIX_PLATFORM_ENABLED = Boolean.parseBoolean(protectionSettings.getProperty("strixplatform.globalconfig.StrixPlatformEnabled", "false"));
			if (STRIX_PLATFORM_ENABLED)
			{
				try
				{
					STRIX_PLATFORM_KEY = protectionSettings.getProperty("strixplatform.globalconfig.StrixPlatformKey", "NOT_INITED_KEY");
					STRIX_PLATFORM_SECOND_KEY = Integer.parseInt(protectionSettings.getProperty("strixplatform.globalconfig.StrixPlatformSecondKey", "-1"));
					STX_PF_XOR_KEY = "" + STRIX_PLATFORM_SECOND_KEY;
				}
				catch (final Exception _)
				{
					Log.error("Error on load key string from guard config. Please check StrixPlatformKey and StrixPlatformSecondKey in " + CONFIG_FILE);
					STRIX_PLATFORM_ENABLED = false;
					return STRIX_PLATFORM_ENABLED;
				}
				
				STRIX_PLATFORM_CHECK_CLIENT_SIDE_VERSION = Boolean.parseBoolean(protectionSettings.getProperty("strixplatform.globalconfig.EnableCheckClientSideVersion", "false"));
				STRIX_PLATFORM_MANUAL_CLIENT_SIDE_VERSION = Integer.parseInt(protectionSettings.getProperty("strixplatform.globalconfig.ManualClientSideVersion", "0"));
				
				STRIX_PLATFORM_DEBUG_ENABLED = Boolean.parseBoolean(protectionSettings.getProperty("strixplatform.guard.DebugEnabled", "false"));
				STRIX_PLATFORM_ENABLED_AUTHLOG = Boolean.parseBoolean(protectionSettings.getProperty("strixplatform.guard.AuthLogEnabled", "false"));
				STRIX_PLATFORM_HWID_BLOCK_TO_LOCK = Integer.parseInt(protectionSettings.getProperty("strixplatform.guard.HWIDBlockToLock", "2"));
				STRIX_PLATFORM_AUTOMATICAL_BAN_TIME = Integer.parseInt(protectionSettings.getProperty("strixplatform.guard.AutomaticalBanTime", "365"));
				STRIX_PLATFORM_GAME_SESSION_CHECK_ENABLED = Boolean.parseBoolean(protectionSettings.getProperty("strixplatform.guard.GameSessionCheckEnabled", "false"));
				STRIX_PLATFORM_CLIENT_BACK_NOTIFICATION_ENABLED = Boolean.parseBoolean(protectionSettings.getProperty("strixplatform.guard.BackToClientNotificationEnabled", "false"));
				STRIX_PLATFORM_ACTIVE_WINDOW_COUNT = Integer.parseInt(protectionSettings.getProperty("strixplatform.guard.ActiveWindowCount", "32"));
				final String[] tmp = protectionSettings.getProperty("strixplatform.extend.FilesChecksum", "0").replaceAll(",", ";").split(";");
				STRIX_PLATFORM_FILES_CHECKSUM = new long[tmp.length];
				for (int i = 0; i < tmp.length; i++)
				{
					STRIX_PLATFORM_FILES_CHECKSUM[i] = Long.parseLong(tmp[i].trim());
				}
				STRIX_PLATFORM_VIRTUAL_MACHINE_CHECK_ENABLED = Boolean.parseBoolean(protectionSettings.getProperty("strixplatform.extend.VirtualMachineCheckEnabled", "false"));
				STRIX_PLATFORM_ONLY_LAUNCHER_CHECK_ENABLED = Boolean.parseBoolean(protectionSettings.getProperty("strixplatform.extend.OnlyLauncherCheckEnabled", "false"));
				STRIX_PLATFORM_ANTIBRUTE = Boolean.parseBoolean(protectionSettings.getProperty("strixplatform.extend.antibrute", "false"));
				STRIX_PLATFORM_DRAW = Boolean.parseBoolean(protectionSettings.getProperty("strixplatform.extend.draw", "false"));
				STRIX_PLATFORM_DRAW_TEXT = protectionSettings.getProperty(protectionSettings.getProperty("strixplatform.extend.draw.text", "Strix-Platform"));
				
				FAILED_CHECK_LICENSE_KEY = FailedCheckResolve.valueOf(protectionSettings.getProperty("strixplatform.check.LicenseFile", "NONE"));
				FAILED_CHECK_GAME_SESSION = FailedCheckResolve.valueOf(protectionSettings.getProperty("strixplatform.check.GameSession", "NONE"));
				FAILED_CHECK_FILES_CHECKSUM = FailedCheckResolve.valueOf(protectionSettings.getProperty("strixplatform.check.FilesChecksum", "NONE"));
				FAILED_CHECK_DETECTION_INFO = FailedCheckResolve.valueOf(protectionSettings.getProperty("strixplatform.check.DetectionInfo", "NONE"));
				FAILED_CHECK_LAUNCH_STATE = FailedCheckResolve.valueOf(protectionSettings.getProperty("strixplatform.check.LaunchState", "NONE"));
				FAILED_CHECK_CLIENT_SIDE_VERSION = FailedCheckResolve.valueOf(protectionSettings.getProperty("strixplatform.check.ClientSideVersion", "NONE"));
				FAILED_CHECK_ACTIVE_WINDOW = FailedCheckResolve.valueOf(protectionSettings.getProperty("strixplatform.check.ActiveWindow", "NONE"));
			}
		}
		catch (final IOException _)
		{
			Log.error("Config file not found or parser return error. Please check " + CONFIG_FILE + " file");
		}
		finally
		{
			if (bufferedReader != null)
			{
				try
				{
					bufferedReader.close();
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
			}
			if (inputStreamReader != null)
			{
				try
				{
					inputStreamReader.close();
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
			}
			if (fileInputStream != null)
			{
				try
				{
					fileInputStream.close();
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		return STRIX_PLATFORM_ENABLED;
	}
	
	public static void init()
	{
		final boolean loaded = parseConfigFile();
		Log.info("Configuration file loaded! Protection - " + (loaded ? "Enabled" : "Disabled"));
	}
	
	public static void reparseClientSideVersion()
	{
		FileInputStream fileInputStream = null;
		InputStreamReader inputStreamReader = null;
		BufferedReader bufferedReader = null;
		
		try
		{
			fileInputStream = new FileInputStream(new File(CONFIG_FILE));
			inputStreamReader = new InputStreamReader(fileInputStream, Charset.forName("UTF-8"));
			bufferedReader = new BufferedReader(inputStreamReader);
			
			final Properties protectionSettings = new Properties();
			
			protectionSettings.load(bufferedReader);
			
			final boolean SPEnabled = Boolean.parseBoolean(protectionSettings.getProperty("strixplatform.globalconfig.StrixPlatformEnabled", "false"));
			if (SPEnabled)
			{
				STRIX_PLATFORM_CHECK_CLIENT_SIDE_VERSION = Boolean.parseBoolean(protectionSettings.getProperty("strixplatform.globalconfig.EnableCheckClientSideVersion", "false"));
				STRIX_PLATFORM_MANUAL_CLIENT_SIDE_VERSION = Integer.parseInt(protectionSettings.getProperty("strixplatform.globalconfig.ManualClientSideVersion", "0"));
			}
		}
		catch (final IOException _)
		{
			Log.error("Config file not found or parser return error. Please check " + CONFIG_FILE + " file");
		}
		finally
		{
			if (bufferedReader != null)
			{
				try
				{
					bufferedReader.close();
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
			}
			if (inputStreamReader != null)
			{
				try
				{
					inputStreamReader.close();
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
			}
			if (fileInputStream != null)
			{
				try
				{
					fileInputStream.close();
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}