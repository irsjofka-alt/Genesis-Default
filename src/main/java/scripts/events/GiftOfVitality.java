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
public class GiftOfVitality extends AbstractWorldEvent
{
	private boolean _isActive = false;
	private WorldEventTemplate _template = null;
	private ScheduledFuture<?> _eventTask = null;
	
	private final List<Npc> _npcList = new ArrayList<>();

	private static SkillHolder[] FIGHTER_SKILLS =
	{
	        new SkillHolder(5627, 1), new SkillHolder(5628, 1), new SkillHolder(5637, 1), new SkillHolder(5629, 1), new SkillHolder(5630, 1), new SkillHolder(5631, 1), new SkillHolder(5632, 1),
	};

	private static SkillHolder[] MAGE_SKILLS =
	{
	        new SkillHolder(5627, 1), new SkillHolder(5628, 1), new SkillHolder(5637, 1), new SkillHolder(5633, 1), new SkillHolder(5634, 1), new SkillHolder(5635, 1), new SkillHolder(5636, 1),
	};

	private static SkillHolder[] SERVITOR_SKILLS =
	{
	        new SkillHolder(5627, 1), new SkillHolder(5628, 1), new SkillHolder(5637, 1), new SkillHolder(5629, 1), new SkillHolder(5633, 1), new SkillHolder(5630, 1), new SkillHolder(5634, 1), new SkillHolder(5631, 1), new SkillHolder(5635, 1), new SkillHolder(5632, 1), new SkillHolder(5636, 1),
	};
	
	private static int _reuseHours;

	private GiftOfVitality()
	{
		addStartNpc(4306);
		addFirstTalkId(4306);
		addTalkId(4306);

		_template = WorldEventParser.getInstance().getEvent(7);
		if (_template != null && !_isActive)
		{
			_reuseHours = _template.getParams().getInteger("reuseHours", 5);
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

		final var msg = new ServerMessage("EventGiftOfVitality.START", true);
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

		final var msg = new ServerMessage("EventGiftOfVitality.STOP", true);
		Announcements.getInstance().announceToAll(msg);

		checkTimerTask(calcEventStartTime(_template, false));
		
		return true;
	}

	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		final var st = player.getQuestState(getName());
		if (!_isActive)
		{
			return null;
		}
		
		switch (event)
		{
			case "vitality" :
			{
				final long reuse = st.get("reuse") != null ? Long.parseLong(st.get("reuse")) : 0;
				if (reuse > System.currentTimeMillis())
				{
					final long remainingTime = (reuse - System.currentTimeMillis()) / 1000;
					final int hours = (int) (remainingTime / 3600);
					final int minutes = (int) ((remainingTime % 3600) / 60);
					final var sm = SystemMessage.getSystemMessage(SystemMessageId.AVAILABLE_AFTER_S1_S2_HOURS_S3_MINUTES);
					sm.addSkillName(23179);
					sm.addNumber(hours);
					sm.addNumber(minutes);
					player.sendPacket(sm);
					htmltext = "4306-notime.htm";
				}
				else
				{
					player.doCast(new SkillHolder(23179, 1).getSkill());
					player.doCast(new SkillHolder(23180, 1).getSkill());
					st.setState(State.STARTED);
					st.set("reuse", String.valueOf(System.currentTimeMillis() + (_reuseHours * 3600000)));
					htmltext = "4306-okvitality.htm";
				}
				break;
			}
			case "memories_player" :
			{
				if (player.getLevel() <= 75)
				{
					htmltext = "4306-nolevel.htm";
				}
				else
				{
					final var skills = (player.isMageClass()) ? MAGE_SKILLS : FIGHTER_SKILLS;
					npc.setTarget(player);
					for (final SkillHolder sk : skills)
					{
						npc.doCast(sk.getSkill());
					}
					htmltext = "4306-okbuff.htm";
				}
				break;
			}
			case "memories_summon" :
			{
				if (player.getLevel() <= 75)
				{
					htmltext = "4306-nolevel.htm";
				}
				else if (!player.hasServitor())
				{
					htmltext = "4306-nosummon.htm";
				}
				else
				{
					npc.setTarget(player.getSummon());
					for (final var sk : SERVITOR_SKILLS)
					{
						npc.doCast(sk.getSkill());
					}
					htmltext = "4306-okbuff.htm";
				}
				break;
			}
		}
		return htmltext;
	}

	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		if (player.getQuestState(getName()) == null)
		{
			newQuestState(player);
		}
		return "4306.htm";
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
		
		_template = WorldEventParser.getInstance().getEvent(7);
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
		new GiftOfVitality();
	}
}
