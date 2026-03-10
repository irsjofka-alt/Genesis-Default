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

import gameserver.Announcements;
import gameserver.ThreadPoolManager;
import gameserver.data.parser.WorldEventParser;
import gameserver.instancemanager.ReflectionManager;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.entity.events.AbstractWorldEvent;
import gameserver.model.entity.events.model.template.WorldEventTemplate;
import gameserver.model.strings.server.ServerMessage;
import gameserver.network.SystemMessageId;
import gameserver.utils.TimeUtils;

/**
 * Updated by LordWinter 13.07.2020
 */
public class CofferOfShadows extends AbstractWorldEvent
{
	private boolean _isActive = false;
	private WorldEventTemplate _template = null;
	private ScheduledFuture<?> _eventTask = null;
	
	private final List<Npc> _npcList = new ArrayList<>();

	private CofferOfShadows()
	{
		addStartNpc(32091);
		addFirstTalkId(32091);
		addTalkId(32091);

		_template = WorldEventParser.getInstance().getEvent(4);
		if (_template != null && !_isActive)
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
	public boolean eventStart(long totalTime, boolean force)
	{
		if (_isActive || totalTime == 0)
		{
			return false;
		}
		
		final var task = _eventTask;
		if (task != null)
		{
			task.cancel(false);
			_eventTask = null;
		}
		
		_isActive = true;
		
		final var spawnList = _template.getSpawnList();
		if (spawnList != null && !spawnList.isEmpty())
		{
			spawnList.stream().filter(s -> s != null).forEach(s -> _npcList.add(addSpawn(s.getNpcId(), s.getLocation().getX(), s.getLocation().getY(), s.getLocation().getZ(), s.getLocation().getHeading(), false, 0, false, ReflectionManager.DEFAULT, s.getTriggerId())));
		}

		final var msg = new ServerMessage("EventCofferOfShadows.START", true);
		Announcements.getInstance().announceToAll(msg);

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

		_npcList.stream().filter(n -> n != null).forEach(n -> n.deleteMe());
		_npcList.clear();
		
		final var msg = new ServerMessage("EventCofferOfShadows.STOP", true);
		Announcements.getInstance().announceToAll(msg);
		
		checkTimerTask(calcEventStartTime(_template, false));

		return true;
	}

	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final String htmltext = event;
		if (event.equalsIgnoreCase("COFFER1"))
		{
			if (!isTakeRequestItems(player, _template, 1))
			{
				player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
				return null;
			}
			calcReward(player, _template, 1);
			return null;
		}
		else if (event.equalsIgnoreCase("COFFER5"))
		{
			if (!isTakeRequestItems(player, _template, 2))
			{
				player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
				return null;
			}
			calcReward(player, _template, 2);
			return null;
		}
		else if (event.equalsIgnoreCase("COFFER10"))
		{
			if (!isTakeRequestItems(player, _template, 3))
			{
				player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
				return null;
			}
			calcReward(player, _template, 3);
			return null;
		}
		else if (event.equalsIgnoreCase("COFFER50"))
		{
			if (!isTakeRequestItems(player, _template, 4))
			{
				player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
				return null;
			}
			calcReward(player, _template, 4);
			return null;
		}
		return htmltext;
	}

	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		var st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}

		return npc.getId() + ".htm";
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
		
		_template = WorldEventParser.getInstance().getEvent(4);
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
		new CofferOfShadows();
	}
}
