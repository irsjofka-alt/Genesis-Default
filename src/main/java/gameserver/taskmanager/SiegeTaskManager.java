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

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

import l2e.commons.log.LoggerObject;
import gameserver.Config;
import gameserver.ThreadPoolManager;
import gameserver.model.actor.templates.residences.sieges.SiegeTemplate;

/**
 * Created by LordWinter
 */
public class SiegeTaskManager extends LoggerObject
{
	private final Map<SiegeTemplate, Long> _siegeTasks = new ConcurrentHashMap<>();
	private ScheduledFuture<?> _timeTask = null;
	private boolean _isRecalc = false;
	
	public SiegeTaskManager()
	{
		_siegeTasks.clear();
	}
	
	public void addSiegeTask(int castleId, SiegeTemplate template, long newTime)
	{
		SiegeTemplate tpl = null;
		for (final var task : _siegeTasks.keySet())
		{
			if (task != null && task.getSiege().getCastle().getId() == castleId)
			{
				tpl = task;
				break;
			}
		}
		
		if (tpl != null)
		{
			_siegeTasks.remove(tpl);
		}
		_siegeTasks.put(template, newTime);
		
		if (!_isRecalc)
		{
			_isRecalc = true;
			ThreadPoolManager.getInstance().schedule(() -> recalcTime(), 1000);
		}
	}
	
	public void recalcTime()
	{
		try
		{
			final var task = _timeTask;
			if (_timeTask != null)
			{
				task.cancel(false);
				_timeTask = null;
			}
			
			if (_siegeTasks.isEmpty())
			{
				_isRecalc = false;
				return;
			}
			
			final var sorted = _siegeTasks.entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
			if (!sorted.isEmpty())
			{
				long nextTime = sorted.entrySet().iterator().next().getValue() - System.currentTimeMillis();
				if (nextTime < 0)
				{
					nextTime = 0;
				}
				_timeTask = ThreadPoolManager.getInstance().schedule(new CheckSiegeTasks(), nextTime);
				if (Config.DEBUG && nextTime > 0)
				{
					info("Next task run at " + new Date(System.currentTimeMillis() + nextTime));
				}
			}
		}
		finally
		{
			_isRecalc = false;
		}
	}
	
	private class CheckSiegeTasks implements Runnable
	{
		@Override
		public void run()
		{
			_isRecalc = true;
			try
			{
				final long time = System.currentTimeMillis();
				for (final var entry : _siegeTasks.entrySet())
				{
					final var tpl = entry.getKey();
					if (time < entry.getValue())
					{
						continue;
					}
					
					if (tpl != null)
					{
						if (tpl.isStartSige())
						{
							tpl.getSiege().startSiege();
						}
						else
						{
							tpl.getSiege().endSiege();
						}
					}
					_siegeTasks.remove(tpl);
				}
			}
			finally
			{
				_isRecalc = false;
				recalcTime();
			}
		}
	}
	
	public static final SiegeTaskManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final SiegeTaskManager _instance = new SiegeTaskManager();
	}
}