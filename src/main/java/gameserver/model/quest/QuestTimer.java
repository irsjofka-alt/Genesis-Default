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
package gameserver.model.quest;

import java.util.concurrent.ScheduledFuture;

import gameserver.ThreadPoolManager;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;

public class QuestTimer
{
	private final String _name;
	private final Quest _quest;
	private final Npc _npc;
	private final Player _player;
	private final boolean _isRepeating;
	private ScheduledFuture<?> _scheduler;
	private int _instanceId;

	public QuestTimer(Quest quest, String name, long time, Npc npc, Player player, boolean repeating)
	{
		_name = name;
		_quest = quest;
		_player = player;
		_npc = npc;
		_isRepeating = repeating;
		
		if (npc != null)
		{
			_instanceId = npc.getReflectionId();
		}
		else if (player != null)
		{
			_instanceId = player.getReflectionId();
		}
		
		if (repeating)
		{
			_scheduler = ThreadPoolManager.getInstance().scheduleAtFixedRate(new ScheduleTimerTask(), time, time);
		}
		else
		{
			_scheduler = ThreadPoolManager.getInstance().schedule(new ScheduleTimerTask(), time);
		}
		
		if (npc != null)
		{
			npc.addQuestTimer(this);
		}
		
		if (player != null)
		{
			player.addQuestTimer(this);
		}
	}

	public void cancel()
	{
		cancelTask();
		
		if (_npc != null)
		{
			_npc.removeQuestTimer(this);
		}
		
		if (_player != null)
		{
			_player.removeQuestTimer(this);
		}
		
	}
	
	public void cancelTask()
	{
		final var task = _scheduler;
		if ((task != null) && !task.isDone() && !task.isCancelled())
		{
			task.cancel(false);
			_scheduler = null;
		}
		_quest.removeQuestTimer(this);
	}
	
	public boolean equals(Quest quest, String name, Npc npc, Player player)
	{
		if ((quest == null) || (quest != _quest))
		{
			return false;
		}
		
		if ((name == null) || !name.equals(_name))
		{
			return false;
		}
		return ((npc == _npc) && (player == _player));
	}
	
	public boolean isActive()
	{
		final var task = _scheduler;
		return (task != null) && !task.isCancelled() && !task.isDone();
	}
	
	public boolean isRepeating()
	{
		return _isRepeating;
	}

	public Quest getQuest()
	{
		return _quest;
	}

	public Npc getNpc()
	{
		return _npc;
	}

	public Player getPlayer()
	{
		return _player;
	}

	public final int getReflectionId()
	{
		return _instanceId;
	}
	
	public class ScheduleTimerTask implements Runnable
	{
		@Override
		public void run()
		{
			if (_scheduler == null)
			{
				return;
			}
			
			if (!_isRepeating)
			{
				cancel();
			}
			_quest.notifyEvent(_name, _npc, _player);
		}
	}

	@Override
	public final String toString()
	{
		return _name;
	}
	
	@Override
	public boolean equals(final Object o)
	{
		if (o == this)
		{
			return true;
		}
		if (o == null)
		{
			return false;
		}
		if (o.getClass() != this.getClass())
		{
			return false;
		}
		return ((QuestTimer) o).toString().equals(toString());
	}
	
	@Override
	public int hashCode()
	{
		return _name.hashCode();
	}
}