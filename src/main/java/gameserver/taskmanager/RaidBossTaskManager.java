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

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

import l2e.commons.log.LoggerObject;
import gameserver.Config;
import gameserver.ThreadPoolManager;
import gameserver.instancemanager.DayNightSpawnManager;
import gameserver.instancemanager.RaidBossSpawnManager;
import gameserver.instancemanager.RaidBossSpawnManager.StatusEnum;
import gameserver.model.GameObjectsStorage;
import gameserver.model.actor.instance.MonsterInstance;
import gameserver.model.actor.instance.RaidBossInstance;
import gameserver.model.actor.templates.npc.AnnounceTemplate;
import gameserver.model.stats.StatsSet;
import gameserver.model.strings.server.ServerMessage;
import gameserver.network.clientpackets.Say2;
import gameserver.network.serverpackets.CreatureSay;

/**
 * Created by LordWinter
 */
public class RaidBossTaskManager extends LoggerObject
{
	private final Map<Integer, Long> _raidList = new ConcurrentHashMap<>();
	private final Map<AnnounceTemplate, Long> _announceList = new ConcurrentHashMap<>();
	private final Map<MonsterInstance, Long> _minionList = new ConcurrentHashMap<>();
	private ScheduledFuture<?> _timeTask = null;
	private ScheduledFuture<?> _announceTask = null;
	private ScheduledFuture<?> _minionTask = null;
	
	public RaidBossTaskManager()
	{
		_raidList.clear();
		_announceList.clear();
		_minionList.clear();
	}
	
	public void recalcAll()
	{
		recalcTime();
		recalcAnnounceTime();
	}
	
	public void addToMinionList(MonsterInstance minion, long time)
	{
		_minionList.putIfAbsent(minion, time);
		recalcMinionTime();
	}
	
	public void removeMinions(MonsterInstance leader)
	{
		final var task = _minionTask;
		if (task != null)
		{
			task.cancel(false);
			_minionTask = null;
		}
		
		for (final var entry : _minionList.entrySet())
		{
			final var minion = entry.getKey();
			final var master = minion.getLeader();
			if (master != null && master == leader)
			{
				_minionList.remove(minion);
			}
		}
		recalcMinionTime();
	}
	

	public void addToAnnounceList(AnnounceTemplate tpl, long time, boolean isRecalc)
	{
		_announceList.putIfAbsent(tpl, time);
		if (isRecalc)
		{
			recalcAnnounceTime();
		}
	}
	
	public void removeAnnounce(int bossId)
	{
		AnnounceTemplate template = null;
		for (final var tpl : _announceList.keySet())
		{
			if (tpl != null && tpl.getTemplate().getId() == bossId)
			{
				template = tpl;
				break;
			}
		}
		
		if (template != null)
		{
			_announceList.remove(template);
		}
	}
	
	public void addToRaidList(int bossId, long time, boolean isRecalc)
	{
		_raidList.putIfAbsent(bossId, time);
		if (isRecalc)
		{
			recalcTime();
		}
	}
	
	public boolean isInRaidList(int bossId)
	{
		return _raidList.containsKey(bossId);
	}
	
	public void removeFromList(int bossId)
	{
		_raidList.remove(bossId);
		AnnounceTemplate template = null;
		for (final var tpl : _announceList.keySet())
		{
			if (tpl != null && tpl.getTemplate().getId() == bossId)
			{
				template = tpl;
				break;
			}
		}
		
		if (template != null)
		{
			_announceList.remove(template);
		}
	}
	
	public void cleanUp()
	{
		var task = _timeTask;
		if (task != null)
		{
			task.cancel(false);
			_timeTask = null;
		}
		task = _announceTask;
		if (task != null)
		{
			task.cancel(false);
			_announceTask = null;
		}
		task = _minionTask;
		if (task != null)
		{
			task.cancel(false);
			_minionTask = null;
		}
		_raidList.clear();
		_announceList.clear();
		_minionList.clear();
	}
	
	public void recalcAnnounceTime()
	{
		final var task = _announceTask;
		if (task != null)
		{
			task.cancel(false);
			_announceTask = null;
		}
		
		if (_announceList.isEmpty())
		{
			return;
		}
		
		final var sorted = _announceList.entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		if (!sorted.isEmpty())
		{
			long nextTime = sorted.entrySet().iterator().next().getValue() - System.currentTimeMillis();
			if (nextTime < 0)
			{
				nextTime = 0;
			}
			_announceTask = ThreadPoolManager.getInstance().schedule(new CheckAnnounceList(), nextTime);
			if (Config.DEBUG && nextTime > 0)
			{
				info("Next announce task run at " + new Date(System.currentTimeMillis() + nextTime));
			}
		}
	}
	
	public void recalcMinionTime()
	{
		final var task = _minionTask;
		if (task != null)
		{
			task.cancel(false);
			_minionTask = null;
		}
		
		if (_minionList.isEmpty())
		{
			return;
		}
		
		final var sorted = _minionList.entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		if (!sorted.isEmpty())
		{
			long nextTime = sorted.entrySet().iterator().next().getValue() - System.currentTimeMillis();
			if (nextTime < 0)
			{
				nextTime = 0;
			}
			_minionTask = ThreadPoolManager.getInstance().schedule(new CheckMinionList(), nextTime);
			if (Config.DEBUG && nextTime > 0)
			{
				info("Next minion task run at " + new Date(System.currentTimeMillis() + nextTime));
			}
		}
	}
	
	public void recalcTime()
	{
		final var task = _timeTask;
		if (task != null)
		{
			task.cancel(false);
			_timeTask = null;
		}
		
		if (_raidList.isEmpty())
		{
			return;
		}
		
		final var sorted = _raidList.entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		if (!sorted.isEmpty())
		{
			long nextTime = sorted.entrySet().iterator().next().getValue() - System.currentTimeMillis();
			if (nextTime < 0)
			{
				nextTime = 0;
			}
			_timeTask = ThreadPoolManager.getInstance().schedule(new CheckRaidList(), nextTime);
			if (Config.DEBUG && nextTime > 0)
			{
				info("Next task run at " + new Date(System.currentTimeMillis() + nextTime));
			}
		}
	}
	
	private class CheckAnnounceList implements Runnable
	{
		@Override
		public void run()
		{
			final long time = System.currentTimeMillis();
			for (final var entry : _announceList.entrySet())
			{
				final var tpl = entry.getKey();
				if (time < entry.getValue())
				{
					continue;
				}
				
				for (final var player : GameObjectsStorage.getPlayers())
				{
					if (player != null && player.isOnline())
					{
						final ServerMessage msg = new ServerMessage("Announce.RAID_PRE_ANNOUNCE", player.getLang());
						msg.add(tpl.getTemplate().getName(player.getLang()));
						msg.add((int) (tpl.getDelay() / 60000));
						player.sendPacket(new CreatureSay(0, Say2.ANNOUNCEMENT, "", msg.toString()));
					}
				}
				_announceList.remove(tpl);
			}
			recalcAnnounceTime();
		}
	}
	
	private class CheckMinionList implements Runnable
	{
		@Override
		public void run()
		{
			final long time = System.currentTimeMillis();
			for (final var entry : _minionList.entrySet())
			{
				final var minion = entry.getKey();
				if (time < entry.getValue())
				{
					continue;
				}
				
				final var master = minion.getLeader();
				if (master != null && !master.isDead())
				{
					master.spawnMinion(master, minion);
				}
				_minionList.remove(minion);
			}
			recalcMinionTime();
		}
	}
	
	private class CheckRaidList implements Runnable
	{
		@Override
		public void run()
		{
			final long time = System.currentTimeMillis();
			final var instance = RaidBossSpawnManager.getInstance();
			for (final var entry : _raidList.entrySet())
			{
				final int bossId = entry.getKey();
				if (time < entry.getValue())
				{
					continue;
				}
				
				RaidBossInstance raidboss = null;
				
				if (bossId == 25328)
				{
					raidboss = DayNightSpawnManager.getInstance().handleBoss(instance.getSpawns().get(bossId));
				}
				else
				{
					if (instance.isDefined(bossId))
					{
						raidboss = (RaidBossInstance) instance.getSpawns().get(bossId).doSpawn();
					}
				}
				
				if (raidboss != null)
				{
					if (Arrays.binarySearch(Config.RAIDBOSS_ANNOUNCE_LIST, raidboss.getId()) >= 0)
					{
						for (final var player : GameObjectsStorage.getPlayers())
						{
							if (player.isOnline())
							{
								final ServerMessage msg = new ServerMessage("Announce.RAID_RESPAWN", player.getLang());
								msg.add(raidboss.getTemplate().getName(player.getLang()));
								player.sendPacket(new CreatureSay(0, Say2.ANNOUNCEMENT, "", msg.toString()));
							}
						}
					}
					raidboss.setRaidStatus(StatusEnum.ALIVE);
					final StatsSet info = new StatsSet();
					info.set("currentHP", raidboss.getCurrentHp());
					info.set("currentMP", raidboss.getCurrentMp());
					info.set("respawnTime", 0L);
					
					instance.addStoreInfo(bossId, info);
					
					info("Spawning Raid Boss " + raidboss.getName(null));
					
					instance.addRaidBoss(bossId, raidboss);
				}
				_raidList.remove(bossId);
			}
			recalcTime();
		}
	}
	
	public static final RaidBossTaskManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final RaidBossTaskManager _instance = new RaidBossTaskManager();
	}
}