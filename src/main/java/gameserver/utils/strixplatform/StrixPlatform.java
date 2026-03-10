package gameserver.utils.strixplatform;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import l2e.commons.dbutils.DbUtils;
import gameserver.ThreadPoolManager;
import gameserver.database.DatabaseFactory;
import gameserver.utils.strixplatform.configs.MainConfig;
import gameserver.utils.strixplatform.logging.Log;
import gameserver.utils.strixplatform.managers.ClientBanManager;

public class StrixPlatform
{
	public static final Logger _log = LogManager.getLogger(StrixPlatform.class);
	
	public boolean isPlatformEnabled()
	{
		return MainConfig.STRIX_PLATFORM_ENABLED;
	}
	
	public boolean isAuthLogEnabled()
	{
		return MainConfig.STRIX_PLATFORM_ENABLED_AUTHLOG;
	}
	
	public boolean isBackNotificationEnabled()
	{
		return MainConfig.STRIX_PLATFORM_CLIENT_BACK_NOTIFICATION_ENABLED;
	}
	
	public boolean isPlatformAntibrute()
	{
		return MainConfig.STRIX_PLATFORM_ANTIBRUTE;
	}
	
	public boolean isPlatformDraw()
	{
		return MainConfig.STRIX_PLATFORM_DRAW;
	}
	
	public String isPlatformDrawText()
	{
		return MainConfig.STRIX_PLATFORM_DRAW_TEXT;
	}
	
	public int getProtocolVersionDataSize()
	{
		return MainConfig.PROTOCOL_VERSION_DATA_SIZE;
	}
	
	public int getClientDataSize()
	{
		return MainConfig.CLIENT_DATA_SIZE;
	}
	
	public void checkClientSideVersion()
	{
		if (MainConfig.STRIX_PLATFORM_CHECK_CLIENT_SIDE_VERSION)
		{
			if (MainConfig.STRIX_PLATFORM_MANUAL_CLIENT_SIDE_VERSION < 0)
			{
				BufferedReader in = null;
				try
				{
					final var url = new URL(MainConfig.STRIX_CLIENT_UPDATE_CHECK_URL);
					final var conn = url.openConnection();
					conn.setDefaultUseCaches(false);
					conn.setRequestProperty("User-Agent", "StrixPlatform/" + MainConfig.STRIX_PLATFORM_KEY + "/" + MainConfig.STRIX_PLATFORM_SECOND_KEY);
					in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
					String buffer;
					int loadedVersion = -1;
					while ((buffer = in.readLine()) != null)
					{
						if (buffer.length() > 3)
						{
							Log.error("Update server Strix-Platform not avaliable on this time, or your firewall or server configuration cannot use out connection to Strix-Platform server. This option seted to DISABLED...");
							MainConfig.STRIX_PLATFORM_CHECK_CLIENT_SIDE_VERSION = false;
							return;
						}
						loadedVersion = Integer.parseInt(buffer);
					}
					MainConfig.CLIENT_SIDE_VERSION_STORED = loadedVersion;
				}
				catch (final Exception e)
				{
					Log.error("Error on check client side version. Please, check your server configuration, firewall, network, etc... Exception: " + e.getLocalizedMessage());
				}
				finally
				{
					if (in != null)
					{
						try
						{
							in.close();
						}
						catch (final Exception e)
						{
							Log.error("Error on close loaded buffer. Send this info to Strix-Platform team support! Exception: " + e.getLocalizedMessage());
						}
					}
				}
			}
			else
			{
				startClientSideVersionCheckThread();
			}
		}
		else
		{
			Log.info("Automatical update check disabled. If needed, see Strix-Platform configuration file from path " + MainConfig.CONFIG_FILE);
		}
	}
	
	private void startClientSideVersionCheckThread()
	{
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable()
		{
			@Override
			public void run()
			{
				MainConfig.reparseClientSideVersion();
			}
		}, 5000L, 30000L);
	}
	
	public StrixPlatform()
	{
		printSection("Strix Guard");
		try
		{
			MainConfig.init();
			if (MainConfig.STRIX_PLATFORM_ENABLED)
			{
				checkClientSideVersion();
				checkTableExist();
				ClientBanManager.getInstance();
			}
		}
		catch (final Exception e)
		{
			Log.error("An error occurred during initialization. Disabling protection ...");
			MainConfig.STRIX_PLATFORM_ENABLED = false;
		}
	}
	
	private static void printSection(String s)
	{
		s = "=[ " + s + " ]";
		while (s.length() < 78)
		{
			s = "-" + s;
		}
		_log.info(s);
	}
	
	private void checkTableExist()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("CREATE TABLE IF NOT EXISTS strix_platform_hwid_ban (id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY, hwid VARCHAR(32), time_expire BIGINT, reason VARCHAR(255), gm_name VARCHAR(50))");
			statement.execute();
		}
		catch (final Exception e)
		{
			Log.error("Exception in function DatabaseManager::checkTableExist. Exception: " + e.getLocalizedMessage());
			DbUtils.closeQuietly(con, statement);
			return;
			
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
			Log.info("Initialized database factory complete");
		}
	}
	
	public static StrixPlatform getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		private static final StrixPlatform _instance = new StrixPlatform();
	}
}