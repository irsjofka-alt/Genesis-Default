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
package gameserver.model.entity;

import java.util.Date;
import java.util.List;

import l2e.commons.util.Rnd;
import gameserver.Config;
import gameserver.data.parser.SpawnParser;
import gameserver.instancemanager.BloodAltarManager;
import gameserver.model.actor.instance.RaidBossInstance;
import gameserver.model.quest.Quest;

/**
 * Created by LordWinter 14.02.2019
 */
public abstract class BloodAltarsEngine extends Quest
{
	public BloodAltarsEngine()
	{
		super(-1);
	}
	
	public abstract boolean changeSpawnInterval(long time, int status, int progress);

	public abstract int getStatus();

	public abstract int getProgress();
	
	protected void updateStatus(String altar, int status)
	{
		BloodAltarManager.getInstance().updateStatus(altar, status);
	}
	
	protected void updateProgress(String altar, int progress)
	{
		BloodAltarManager.getInstance().updateProgress(altar, progress);
	}

	private void updateStatusTime(String altar, long time)
	{
		final long changeStatus = System.currentTimeMillis() + time;
		BloodAltarManager.getInstance().updateStatusTime(altar, changeStatus);
	}
	
	private void cleanBossStatus(String altar)
	{
		BloodAltarManager.getInstance().cleanBossStatus(altar);
	}

	protected List<Integer> getBossList(String altar)
	{
		return BloodAltarManager.getInstance().getBossList(altar);
	}

	protected void restoreStatus(String altar)
	{
		final var info = BloodAltarManager.getInstance().getAltarInfo(altar);
		
		final int status = info.getInteger("status");
		final int progress = info.getInteger("progress");
		final long time = info.getLong("changeTime");
		
		if (time > System.currentTimeMillis())
		{
			final long changeStatus = time - System.currentTimeMillis();
			if (Config.DEBUG)
			{
				_log.info(getClass().getSimpleName() + ": " + getName() + " blood altar change status at " + new Date(time));
			}
			changeSpawnInterval(changeStatus, status, progress);
			switch (status)
			{
				case 0 :
					manageBosses(altar, false);
					manageNpcs(altar, true);
					break;
				case 1 :
					manageNpcs(altar, false);
					manageBosses(altar, true);
					break;
				case 2 :
					manageNpcs(altar, false);
					restoreActive(altar);
					break;
			}
		}
		else
		{
			changeStatus(altar, getChangeTime(), 0);
		}
	}

	private void restoreActive(String altar)
	{
		SpawnParser.getInstance().spawnCheckGroup(altar + "_bloodaltar_bosses", BloodAltarManager.getInstance().getDeadBossList(altar));
	}
	
	protected void updateBossStatus(String altar, RaidBossInstance boss, int status)
	{
		BloodAltarManager.getInstance().updateBossStatus(altar, boss, status);
	}

	public void changeStatus(String altar, long time, int status)
	{
		int newStatus;
		if (Rnd.chance(Config.CHANCE_SPAWN) && status == 0)
		{
			newStatus = 1;
			manageNpcs(altar, false);
			manageBosses(altar, true);
			updateStatus(altar, newStatus);
		}
		else
		{
			newStatus = 0;
			manageBosses(altar, false);
			manageNpcs(altar, true);
			updateStatus(altar, newStatus);
		}
		updateProgress(altar, 0);
		changeSpawnInterval(time, newStatus, 0);
		updateStatusTime(altar, time);
		cleanBossStatus(altar);
	}

	private static void manageNpcs(String altar, boolean spawnAlive)
	{
		if (spawnAlive)
		{
			SpawnParser.getInstance().despawnGroup(altar + "_bloodaltar_dead_npc");
			SpawnParser.getInstance().spawnGroup(altar + "_bloodaltar_alive_npc");
		}
		else
		{
			SpawnParser.getInstance().despawnGroup(altar + "_bloodaltar_alive_npc");
			SpawnParser.getInstance().spawnGroup(altar + "_bloodaltar_dead_npc");
		}
	}
	
	private static void manageBosses(String altar, boolean spawn)
	{
		if (spawn)
		{
			SpawnParser.getInstance().spawnGroup(altar + "_bloodaltar_bosses");
		}
		else
		{
			SpawnParser.getInstance().despawnGroup(altar + "_bloodaltar_bosses");
		}
	}
	
	public long getChangeTime()
	{
		final long changeStatus = System.currentTimeMillis() + (Config.RESPAWN_TIME * 3600 * 1000L);
		if (Config.DEBUG)
		{
			_log.info(getClass().getSimpleName() + ": " + getName() + " blood altar change status at " + new Date(changeStatus));
		}
		return changeStatus - System.currentTimeMillis();
	}
}