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

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gameserver.ThreadPoolManager;
import gameserver.model.actor.Creature;
import gameserver.network.serverpackets.AutoAttackStop;

public class AttackStanceTaskManager
{
	private static final Logger _log = LogManager.getLogger(AttackStanceTaskManager.class);
	
	private static final Map<Creature, Long> _attackStanceTasks = new ConcurrentHashMap<>();
	
	public AttackStanceTaskManager()
	{
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new FightModeScheduler(), 0, 1000);
	}

	public void addAttackStanceTask(Creature actor)
	{
		if (actor != null)
		{
			_attackStanceTasks.put(actor, System.currentTimeMillis());
		}
	}
	
	public void removeAttackStanceTask(Creature actor)
	{
		if (actor != null)
		{
			if (actor.isSummon())
			{
				actor = actor.getActingPlayer();
			}
			_attackStanceTasks.remove(actor);
		}
	}
	
	public boolean hasAttackStanceTask(Creature actor)
	{
		if (actor != null)
		{
			return _attackStanceTasks.containsKey(actor);
		}
		return false;
	}
	
	private class FightModeScheduler implements Runnable
	{
		@Override
		public void run()
		{
			final long current = System.currentTimeMillis();
			try
			{
				final var iter = _attackStanceTasks.entrySet().iterator();
				Entry<Creature, Long> e;
				Creature actor;
				while (iter.hasNext())
				{
					e = iter.next();
					if ((current - e.getValue()) > 15000)
					{
						actor = e.getKey();
						if (actor != null)
						{
							actor.broadcastPacket(new AutoAttackStop(actor.getObjectId()));
						}
						iter.remove();
					}
				}
			}
			catch (final Exception e)
			{
				_log.warn("Error in FightModeScheduler: " + e.getMessage(), e);
			}
		}
	}
	
	public static AttackStanceTaskManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final AttackStanceTaskManager _instance = new AttackStanceTaskManager();
	}
}