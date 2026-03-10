package gameserver.utils.strixplatform.database.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import l2e.commons.dbutils.DbUtils;
import gameserver.database.DatabaseFactory;
import gameserver.utils.strixplatform.logging.Log;
import gameserver.utils.strixplatform.utils.BannedHWIDInfo;

public class BanDAO
{
	private static final String LOAD_ALL_BAN = "SELECT `hwid`,`time_expire`,`reason`,`gm_name` FROM `strix_platform_hwid_ban` WHERE `hwid` IS NOT NULL";
	private static final String ADD_HWID = "INSERT INTO `strix_platform_hwid_ban` (hwid, time_expire, reason, gm_name) VALUES (?,?,?,?)";
	private static final String DELETE_HWID = "DELETE FROM strix_platform_hwid_ban WHERE hwid=?";
	
	public Map<String, BannedHWIDInfo> loadAllBannedHWID()
	{
		final Map<String, BannedHWIDInfo> allBannedHWID = new ConcurrentHashMap<>();
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(LOAD_ALL_BAN);
			rset = statement.executeQuery();
			while (rset.next())
			{
				final String HWID = rset.getString("hwid");
				final long timeExpire = rset.getLong("time_expire");
				final String reason = rset.getString("reason");
				final String gmName = rset.getString("gm_name");
				
				final var bhi = new BannedHWIDInfo(HWID, timeExpire, reason, gmName);
				if (!allBannedHWID.containsKey(HWID))
				{
					allBannedHWID.put(HWID, bhi);
				}
			}
		}
		catch (final Exception e)
		{
			Log.error("Exception in function BanDAO::loadAllBannedHWID(). Exception: " + e.getLocalizedMessage());
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return allBannedHWID;
	}
	
	public boolean insert(final String HWID, final long timeExpire, final String reason, final String gmName)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(ADD_HWID);
			statement.setString(1, HWID);
			statement.setLong(2, timeExpire);
			statement.setString(3, reason);
			statement.setString(4, gmName);
			statement.executeUpdate();
		}
		catch (final Exception e)
		{
			Log.error("Exception in function BanDAO::insert(String, long, String, String). Exception: " + e.getLocalizedMessage());
			return false;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return true;
	}
	
	public boolean insert(final BannedHWIDInfo bannedHWIDInfo)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(ADD_HWID);
			statement.setString(1, bannedHWIDInfo.getHWID());
			statement.setLong(2, bannedHWIDInfo.getTimeExpire());
			statement.setString(3, bannedHWIDInfo.getReason());
			statement.setString(4, bannedHWIDInfo.getGmName());
			statement.executeUpdate();
		}
		catch (final Exception e)
		{
			Log.error("Exception in function BanDAO::insert(BannedHWIDInfo). Exception: " + e.getLocalizedMessage());
			return false;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return true;
	}
	
	public boolean delete(final String HWID)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(DELETE_HWID);
			statement.setString(1, HWID);
			statement.execute();
		}
		catch (final Exception e)
		{
			Log.error("Exception in function BanDAO::deleteHWID(String). Exception: " + e.getLocalizedMessage());
			return false;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return true;
	}
	
	public static BanDAO getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		private static final BanDAO _instance = new BanDAO();
	}
}