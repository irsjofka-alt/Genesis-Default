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
import gameserver.model.holders.SkillHolder;
import gameserver.model.quest.State;
import gameserver.model.strings.server.ServerMessage;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.SystemMessage;
import gameserver.utils.TimeUtils;

/**
 * Updated by LordWinter 13.07.2020
 */
public class LoveYourGatekeeper extends AbstractWorldEvent
{
	private boolean _isActive = false;
	private WorldEventTemplate _template = null;
	private ScheduledFuture<?> _eventTask = null;
	
	private final List<Npc> _npcList = new ArrayList<>();

	private LoveYourGatekeeper()
	{
		addStartNpc(32477);
		addFirstTalkId(32477);
		addTalkId(32477);

		_template = WorldEventParser.getInstance().getEvent(12);
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

		final var msg = new ServerMessage("EventLoveYourGatekeeper.START", true);
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

		final var msg = new ServerMessage("EventLoveYourGatekeeper.STOP", true);
		Announcements.getInstance().announceToAll(msg);

		checkTimerTask(calcEventStartTime(_template, false));
		
		return true;
	}

	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final var st = player.getQuestState(getName());
		if (st == null)
		{
			return getNoQuestMsg(player);
		}

		switch (event)
		{
			case "transform_stick" :
			{
				long reuseTime = 0;
				final String stReuse = st.get("reuse");
				if (stReuse != null)
				{
					reuseTime = Long.parseLong(stReuse);
				}
				
				if (reuseTime > System.currentTimeMillis())
				{
					final var rewards = _template.getVariantRewards().get(1);
					if (rewards != null && !rewards.isEmpty())
					{
						for (final var reward : rewards)
						{
							if (reward != null)
							{
								final long remainingTime = (reuseTime - System.currentTimeMillis()) / 1000;
								final int hours = (int) (remainingTime / 3600);
								final int minutes = (int) ((remainingTime % 3600) / 60);
								final var sm = SystemMessage.getSystemMessage(SystemMessageId.AVAILABLE_AFTER_S1_S2_HOURS_S3_MINUTES);
								sm.addItemName(reward.getId());
								sm.addNumber(hours);
								sm.addNumber(minutes);
								player.sendPacket(sm);
							}
						}
					}
				}
				else
				{
					if (!isTakeRequestItems(player, _template, 1))
					{
						return "32477-3.htm";
					}
					calcReward(player, _template, 1);
					st.setState(State.STARTED);
					st.set("reuse", String.valueOf(System.currentTimeMillis() + (24 * 3600000)));
				}
				return null;
			}
			case "transform" :
			{
				if (player.isTransformed())
				{
					return null;
				}
				player.doCast(new SkillHolder(5655, 1).getSkill());
				return null;
			}
		}
		return event;
	}

	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		if (player.getQuestState(getName()) == null)
		{
			newQuestState(player);
		}
		return "32477.htm";
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
		
		_template = WorldEventParser.getInstance().getEvent(12);
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
		new LoveYourGatekeeper();
	}
}
