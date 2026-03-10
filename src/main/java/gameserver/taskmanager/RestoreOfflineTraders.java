/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://l2jeternity.com/>.
 */
package gameserver.taskmanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import l2e.commons.dbutils.DbUtils;
import gameserver.Config;
import gameserver.database.DatabaseFactory;
import gameserver.instancemanager.mods.OfflineTaskManager;
import gameserver.model.actor.Player;
import gameserver.model.service.autofarm.FarmSettings;
import gameserver.model.skills.effects.AbnormalEffect;
import gameserver.network.GameClient;
import gameserver.network.GameClient.GameClientState;
import gameserver.network.communication.AuthServerCommunication;

public class RestoreOfflineTraders implements Runnable
{
	private static final Logger _log = LogManager.getLogger(RestoreOfflineTraders.class);

	@Override
	public void run()
	{
		int count = 0;
		int buffers = 0;
		int farmers = 0;
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			if (Config.OFFLINE_MAX_DAYS > 0)
			{
				final int expireTimeSecs = (int) (System.currentTimeMillis() / 1000L - (Config.OFFLINE_MAX_DAYS * 86400L));
				statement = con.prepareStatement("DELETE FROM character_variables WHERE name = 'offline' AND value < ?");
				statement.setLong(1, expireTimeSecs);
				statement.executeUpdate();
				DbUtils.close(statement);
				
				statement = con.prepareStatement("DELETE FROM character_variables WHERE name = 'offlineBuff' AND value < ?");
				statement.setLong(1, expireTimeSecs);
				statement.executeUpdate();
				DbUtils.close(statement);
			}
			
			if (FarmSettings.OFFLINE_FARM_MAX_DAYS > 0)
			{
				final int expireFarmTimeSecs = (int) (System.currentTimeMillis() / 1000L - (FarmSettings.OFFLINE_FARM_MAX_DAYS * 86400L));
				statement = con.prepareStatement("DELETE FROM character_variables WHERE name = 'offlineFarm' AND value < ?");
				statement.setLong(1, expireFarmTimeSecs);
				statement.executeUpdate();
				DbUtils.close(statement);
			}

			statement = con.prepareStatement("DELETE FROM character_variables WHERE name = 'offline' AND obj_id IN (SELECT charId FROM characters WHERE accessLevel < 0)");
			statement.executeUpdate();
			DbUtils.close(statement);
			
			statement = con.prepareStatement("DELETE FROM character_variables WHERE name = 'offlineBuff' AND obj_id IN (SELECT charId FROM characters WHERE accessLevel < 0)");
			statement.executeUpdate();
			DbUtils.close(statement);
			
			statement = con.prepareStatement("DELETE FROM character_variables WHERE name = 'offlineFarm' AND obj_id IN (SELECT charId FROM characters WHERE accessLevel < 0)");
			statement.executeUpdate();
			DbUtils.close(statement);
			
			final long curTime = System.currentTimeMillis();
			int objectId = 0;
			Player player = null;
			if ((Config.OFFLINE_TRADE_ENABLE || Config.OFFLINE_CRAFT_ENABLE) && Config.RESTORE_OFFLINERS)
			{
				statement = con.prepareStatement("SELECT obj_id, value FROM character_variables WHERE name = 'offline'");
				rset = statement.executeQuery();
				while (rset.next())
				{
					objectId = rset.getInt("obj_id");
					
					final GameClient client = new GameClient(null);
					client.setDetached(true);
					player = Player.load(objectId);
					if (player == null)
					{
						continue;
					}
					
					client.setActiveChar(player);
					player.setOnlineStatus(true, false);
					client.setLogin(player.getAccountNamePlayer());
					
					if (player.isDead() || player.getAccessLevel().getLevel() < 0)
					{
						player.kick();
						continue;
					}
					
					if (Config.OFFLINE_MODE_PRICE[0] > 0)
					{
						final long time = player.getVarLong("offlineTime", 0);
						if (time < curTime)
						{
							if (player.getInventory().getItemByItemId(Config.OFFLINE_MODE_PRICE[0]) == null || player.getInventory().getItemByItemId(Config.OFFLINE_MODE_PRICE[0]).getCount() < Config.OFFLINE_MODE_PRICE[1])
							{
								player.kick();
								continue;
							}
							player.destroyItemByItemId("OfflineMode", Config.OFFLINE_MODE_PRICE[0], Config.OFFLINE_MODE_PRICE[1], player, false);
							OfflineTaskManager.getInstance().addOfflinePlayer(player, 0, false);
						}
						else
						{
							OfflineTaskManager.getInstance().addOfflinePlayer(player, time, false);
						}
					}
					
					client.setState(GameClientState.IN_GAME);
					player.setClient(client);
					player.spawnMe();
					if (Config.OFFLINE_SET_NAME_COLOR)
					{
						player.getAppearance().setNameColor(Config.OFFLINE_NAME_COLOR);
					}
					player.setOfflineMode(true);
					player.setOnlineStatus(true, true);
					if (Config.OFFLINE_SET_VISUAL_EFFECT)
					{
						player.startAbnormalEffect(AbnormalEffect.SLEEP);
					}
					final var summon = player.getSummon();
					if (summon != null)
					{
						summon.setRestoreSummon(true);
						summon.unSummon(player);
					}
					player.broadcastCharInfo();
					if (Config.DOUBLE_SESSIONS_CONSIDER_OFFLINE_TRADERS)
					{
						AuthServerCommunication.getInstance().addAuthedClient(client);
					}
					count++;
				}
				DbUtils.close(statement, rset);
				
				statement = con.prepareStatement("SELECT obj_id, value FROM character_variables WHERE name = 'offlineBuff'");
				rset = statement.executeQuery();
				
				objectId = 0;
				player = null;
				
				while (rset.next())
				{
					objectId = rset.getInt("obj_id");
					
					final GameClient client = new GameClient(null);
					client.setDetached(true);
					player = Player.load(objectId);
					if (player == null)
					{
						continue;
					}
					
					client.setActiveChar(player);
					player.setOnlineStatus(true, false);
					
					if (player.isDead() || player.getAccessLevel().getLevel() < 0)
					{
						player.kick();
						continue;
					}
					
					if (Config.OFFLINE_MODE_PRICE[0] > 0)
					{
						final long time = player.getVarLong("offlineTime", 0);
						if (time < curTime)
						{
							if (player.getInventory().getItemByItemId(Config.OFFLINE_MODE_PRICE[0]) == null || player.getInventory().getItemByItemId(Config.OFFLINE_MODE_PRICE[0]).getCount() < Config.OFFLINE_MODE_PRICE[1])
							{
								player.kick();
								continue;
							}
							player.destroyItemByItemId("OfflineMode", Config.OFFLINE_MODE_PRICE[0], Config.OFFLINE_MODE_PRICE[1], player, false);
							OfflineTaskManager.getInstance().addOfflinePlayer(player, 0, false);
						}
						else
						{
							OfflineTaskManager.getInstance().addOfflinePlayer(player, time, false);
						}
					}
					
					client.setLogin(player.getAccountNamePlayer());
					client.setState(GameClientState.IN_GAME);
					player.setClient(client);
					player.spawnMe();
					if (Config.OFFLINE_SET_NAME_COLOR)
					{
						player.getAppearance().setNameColor(Config.OFFLINE_NAME_COLOR);
					}
					player.setOfflineMode(true);
					player.setIsSellingBuffs(true);
					player.setOnlineStatus(true, true);
					if (Config.OFFLINE_SET_VISUAL_EFFECT)
					{
						player.startAbnormalEffect(AbnormalEffect.SLEEP);
					}
					final var summon = player.getSummon();
					if (summon != null)
					{
						summon.setRestoreSummon(true);
						summon.unSummon(player);
					}
					player.broadcastCharInfo();
					if (Config.DOUBLE_SESSIONS_CONSIDER_OFFLINE_TRADERS)
					{
						AuthServerCommunication.getInstance().addAuthedClient(client);
					}
					buffers++;
					DbUtils.close(statement, rset);
				}
			}
			
			if (FarmSettings.ALLOW_OFFLINE)
			{
				statement = con.prepareStatement("SELECT obj_id, value FROM character_variables WHERE name = 'offlineFarm'");
				rset = statement.executeQuery();
				
				objectId = 0;
				player = null;
				
				while (rset.next())
				{
					objectId = rset.getInt("obj_id");
					
					final GameClient client = new GameClient(null);
					client.setDetached(true);
					player = Player.load(objectId);
					if (player == null)
					{
						continue;
					}
					
					client.setActiveChar(player);
					player.setOnlineStatus(true, false);
					
					if (player.isDead() || player.getAccessLevel().getLevel() < 0)
					{
						player.deleteMe();
						continue;
					}
					
					client.setLogin(player.getAccountNamePlayer());
					client.setState(GameClientState.IN_GAME);
					player.setClient(client);
					player.spawnMe();
					player.setRunning();
					player.setOfflineFarmMode(true);
					player.setOnlineStatus(true, true);
					player.broadcastCharInfo();
					player.getFarmSystem().restoreVariables();
					final var shots = player.getVar("offlineFarmShots", "");
					if (!shots.isEmpty())
					{
						for (final var i : shots.split(";"))
						{
							if (!i.isEmpty())
							{
								final var shotId = Integer.parseInt(i);
								player.addAutoSoulShot(shotId);
								player.rechargeShots(true, true);
							}
						}
					}
					player.restoreAcpTask();
					player.getFarmSystem().startFarmTask();
					if (FarmSettings.DOUBLE_SESSIONS_CONSIDER_OFFLINE_FARM)
					{
						AuthServerCommunication.getInstance().addAuthedClient(client);
					}
					farmers++;
				}
			}
		}
		catch (final SQLException e)
		{
			_log.warn("Error while restoring offline traders!", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		_log.info("RestoreOfflineTraders: Restored " + count + " offline traders and " + buffers + " sellbuff traders and " + farmers + " offline farmers.");
		OfflineTaskManager.getInstance().recalcTime();
	}
}
