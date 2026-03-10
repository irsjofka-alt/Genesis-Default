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
package scripts.events;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import l2e.commons.util.Rnd;
import gameserver.Announcements;
import gameserver.ThreadPoolManager;
import gameserver.data.parser.WorldEventParser;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.actor.instance.EventMonsterInstance;
import gameserver.model.actor.instance.MonsterInstance;
import gameserver.model.entity.events.AbstractWorldEvent;
import gameserver.model.entity.events.model.template.WorldEventTemplate;
import gameserver.model.strings.server.ServerMessage;
import gameserver.taskmanager.ItemsAutoDestroy;
import gameserver.utils.TimeUtils;

/**
 * Updated by LordWinter 13.07.2020
 */
public class Elpies extends AbstractWorldEvent
{
	private boolean _isActive = false;
	private WorldEventTemplate _template = null;
	private ScheduledFuture<?> _eventTask = null;
	
	private final List<Npc> _npcList = new ArrayList<>();
	
	private static int _elpy = 900100;
	private static int _option_howmuch;
	private static int _elpies_count = 0;
	private static boolean _canUseMagic;
	
	private Elpies()
	{
		addSpawnId(_elpy);
		addKillId(_elpy);
		
		_template = WorldEventParser.getInstance().getEvent(5);
		if (_template != null && !_isActive)
		{
			_canUseMagic = _template.getParams().getBool("canUseMagic", false);
			_option_howmuch = _template.getParams().getInteger("totalAmount", 100);
			if (_template.isNonStop())
			{
				eventStart(-1, false);
			}
			else
			{
				final long startTime = calcEventStartTime(_template, false);
				final long expireTime = calcEventStopTime(_template, false);
				if (startTime <= System.currentTimeMillis() && expireTime > System.currentTimeMillis() || (expireTime < startTime && expireTime > System.currentTimeMillis()))
				{
					eventStart(expireTime - System.currentTimeMillis(), false);
				}
				else
				{
					checkTimerTask(startTime);
				}
			}
		}
	}

	@Override
	public boolean isEventActive()
	{
		return _isActive;
	}
	
	@Override
	public WorldEventTemplate getEventTemplate()
	{
		return _template;
	}

	@Override
	public String onSpawn(Npc npc)
	{
		((EventMonsterInstance) npc).eventSetDropOnGround(true);
		if (!_canUseMagic)
		{
			((EventMonsterInstance) npc).eventSetBlockOffensiveSkills(true);
		}
		return super.onSpawn(npc);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		if (_isActive)
		{
			if (_template.getDropList() != null && !_template.getDropList().isEmpty())
			{
				boolean isRecalc = false;
				for (final var drop : _template.getDropList())
				{
					if (Rnd.chance(drop.getChance()))
					{
						final long amount = drop.getMinCount() != drop.getMaxCount() ? Rnd.get(drop.getMinCount(), drop.getMaxCount()) : drop.getMinCount();
						((MonsterInstance) npc).dropItem(killer, drop.getId(), (int) amount);
						isRecalc = true;
					}
					
					if (isRecalc)
					{
						ItemsAutoDestroy.getInstance().tryRecalcTime();
					}
				}
			}
			_elpies_count--;
			
			if (_elpies_count <= 0)
			{
				final var msg = new ServerMessage("EventElpies.NO_MORE", true);
				Announcements.getInstance().announceToAll(msg);
				eventStop();
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public boolean eventStart(long totalTime, boolean force)
	{
		if (_isActive || totalTime == 0)
		{
			return false;
		}
		
		if (totalTime == -1)
		{
			final long startTime = calcEventStartTime(_template, force);
			final long expireTime = calcEventStopTime(_template, force);
			totalTime = expireTime - startTime;
			if (totalTime <= 0)
			{
				return false;
			}
		}
		
		final var task = _eventTask;
		if (task != null)
		{
			task.cancel(false);
			_eventTask = null;
		}
		
		final var t = _template.getTerritories().get(Rnd.get(_template.getTerritories().size()));
		if (t == null)
		{
			_log.info(_template.getName(null) + ": Event can't be started, because territoty cant selected!");
		}
		
		final var territory = t.getTerritory();
		if (territory == null)
		{
			_log.info(_template.getName(null) + ": Event can't be started, because territoty cant selected!");
			return false;
		}
		
		_npcList.clear();
		_isActive = true;
		_elpies_count = 0;
		
		for (int i = 0; i < _option_howmuch; i++)
		{
			final var loc = territory.getRandomLoc(false);
			_npcList.add(addSpawn(_elpy, loc.getX(), loc.getY(), loc.getZ(), 0, true, totalTime));
			_elpies_count++;
		}
		
		final var msg1 = new ServerMessage("EventElpies.START_MSG_1", true);
		msg1.add(t.getName());
		Announcements.getInstance().announceToAll(msg1);
		
		final var msg2 = new ServerMessage("EventElpies.START_MSG_2", true);
		Announcements.getInstance().announceToAll(msg2);
		
		final var msg3 = new ServerMessage("EventElpies.START_MSG_3", true);
		msg3.add((totalTime / 60000));
		Announcements.getInstance().announceToAll(msg3);
		
		if (totalTime > 0)
		{
			_eventTask = ThreadPoolManager.getInstance().schedule(new Runnable()
			{
				@Override
				public void run()
				{
					eventStop();
				}
			}, totalTime);
			_log.info("Event " + _template.getName(null) + " will end in: " + TimeUtils.toSimpleFormat(System.currentTimeMillis() + totalTime));
		}
		return true;
	}
	
	@Override
	public boolean eventStop()
	{
		if (!_isActive)
		{
			return false;
		}
		
		final var task = _eventTask;
		if (task != null)
		{
			task.cancel(false);
			_eventTask = null;
		}
		_isActive = false;
		
		if (!_npcList.isEmpty())
		{
			for (final Npc _npc : _npcList)
			{
				if (_npc != null)
				{
					_npc.deleteMe();
				}
			}
		}
		_npcList.clear();
		
		final var msg = new ServerMessage("EventElpies.STOP", true);
		Announcements.getInstance().announceToAll(msg);
		
		checkTimerTask(calcEventStartTime(_template, false));
		
		return true;
	}
	
	@Override
	public void startTimerTask(long time)
	{
		final var task = _eventTask;
		if (task != null)
		{
			task.cancel(false);
			_eventTask = null;
		}
		
		_eventTask = ThreadPoolManager.getInstance().schedule(new Runnable()
		{
			@Override
			public void run()
			{
				final long expireTime = calcEventStopTime(_template, false);
				if (expireTime > System.currentTimeMillis())
				{
					eventStart(expireTime - System.currentTimeMillis(), false);
				}
			}
		}, (time - System.currentTimeMillis()));
		_log.info("Event " + _template.getName(null) + " will start in: " + TimeUtils.toSimpleFormat(time));
	}
	
	@Override
	public boolean isReloaded()
	{
		if (isEventActive())
		{
			return false;
		}
		
		_template = WorldEventParser.getInstance().getEvent(5);
		if (_template != null)
		{
			if (_template.isNonStop())
			{
				eventStart(-1, false);
			}
			else
			{
				final long startTime = calcEventStartTime(_template, false);
				final long expireTime = calcEventStopTime(_template, false);
				if (startTime <= System.currentTimeMillis() && expireTime > System.currentTimeMillis() || (expireTime < startTime && expireTime > System.currentTimeMillis()))
				{
					eventStart(expireTime - System.currentTimeMillis(), false);
				}
				else
				{
					checkTimerTask(startTime);
				}
			}
			return true;
		}
		return false;
	}
	
	public static void main(String[] args)
	{
		new Elpies();
	}
}
