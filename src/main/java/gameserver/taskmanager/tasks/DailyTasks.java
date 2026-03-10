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
package gameserver.taskmanager.tasks;


import java.sql.Connection;
import java.sql.PreparedStatement;

import l2e.commons.dbutils.DbUtils;
import l2e.commons.time.cron.SchedulingPattern;
import gameserver.Config;
import gameserver.database.DatabaseFactory;
import gameserver.instancemanager.DailyTaskManager;
import gameserver.instancemanager.QuestManager;
import gameserver.instancemanager.RevengeManager;
import gameserver.model.GameObjectsStorage;
import gameserver.model.actor.Player;
import gameserver.model.olympiad.Olympiad;

public class DailyTasks extends AutomaticTask
{
	private static final SchedulingPattern PATTERN = new SchedulingPattern("30 6 * * *");
	
	@Override
	public void doTask() throws Exception
	{
		_log.info("Daily Refresh Tasks: launched.");
		for (final Player player : GameObjectsStorage.getPlayers())
		{
			if (player == null)
			{
				continue;
			}
			player.getRecommendation().restartRecom();
			player.getNevitSystem().restartSystem();
			player.restartChatMessages();
		}
		clearDbRecommendations();
		if (Config.ALLOW_DAILY_TASKS)
		{
			DailyTaskManager.getInstance().checkDailyTimeTask(true);
		}
		QuestManager.getInstance().cleanHwidList();
		if (Config.ALLOW_REVENGE_SYSTEM)
		{
			RevengeManager.getInstance().cleanUpDatas(true);
		}
		Olympiad.addDailyPoints();
		_log.info("Daily Tasks: Update completed...");
	}
	
	private void clearDbRecommendations()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE `characters` SET `rec_bonus_time`=3600");
			statement.execute();
		}
		catch (final Exception e)
		{
			_log.warn("Could not update chararacters recommendations!", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	@Override
	public long reCalcTime(boolean start)
	{
		return PATTERN.next(System.currentTimeMillis());
	}
}