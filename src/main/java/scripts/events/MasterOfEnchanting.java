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
import gameserver.model.entity.events.EventsDropManager;
import gameserver.model.entity.events.model.template.WorldEventTemplate;
import gameserver.model.items.itemcontainer.Inventory;
import gameserver.model.strings.server.ServerMessage;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.SystemMessage;
import gameserver.utils.TimeUtils;

/**
 * Updated by LordWinter 13.07.2020
 */
public class MasterOfEnchanting extends AbstractWorldEvent
{
	private boolean _isActive = false;
	private WorldEventTemplate _template = null;
	private ScheduledFuture<?> _eventTask = null;
	
	private final List<Npc> _npcList = new ArrayList<>();
	
	private MasterOfEnchanting()
	{
		addStartNpc(32599);
		addFirstTalkId(32599);
		addTalkId(32599);
		
		_template = WorldEventParser.getInstance().getEvent(13);
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
		
		if (_template.getDropList() != null && !_template.getDropList().isEmpty())
		{
			EventsDropManager.getInstance().addRule(_template.getId(), _template.getDropList(), true);
		}
		
		final var msg = new ServerMessage("EventMasterOfEnchanting.START", true);
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
		
		EventsDropManager.getInstance().removeRule(_template.getId());
		
		final var msg = new ServerMessage("EventMasterOfEnchanting.STOP", true);
		Announcements.getInstance().announceToAll(msg);
		
		checkTimerTask(calcEventStartTime(_template, false));
		
		return true;
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		final var st = player.getQuestState(getName());
		if (event.equalsIgnoreCase("buy_staff"))
		{
			final var rewards = _template.getVariantRewards().get(13539);
			if (rewards != null && !rewards.isEmpty())
			{
				for (final var reward : rewards)
				{
					if (reward != null)
					{
						if (st.hasQuestItems(reward.getId()))
						{
							return "32599-staffcant.htm";
						}
					}
				}
			}
			
			if (isTakeRequestItems(player, _template, 13539))
			{
				calcReward(player, _template, 13539);
				htmltext = "32599-staffbuyed.htm";
			}
			else
			{
				htmltext = "32599-staffcant.htm";
			}
		}
		else if (event.equalsIgnoreCase("buy_scroll_24"))
		{
			final long curTime = System.currentTimeMillis();
			final String value = loadGlobalQuestVar(player.getAccountName());
			final long reuseTime = value == null || value.isEmpty() ? 0 : Long.parseLong(value);
			
			if (curTime > reuseTime)
			{
				if (isTakeRequestItems(player, _template, 13540))
				{
					calcReward(player, _template, 13540);
					saveGlobalQuestVar(player.getAccountName(), Long.toString(System.currentTimeMillis() + (6 * 3600000)));
					htmltext = "32599-scroll24.htm";
				}
				else
				{
					htmltext = "32599-s24-no.htm";
				}
			}
			else
			{
				final long remainingTime = (reuseTime - curTime) / 1000;
				final int hours = (int) remainingTime / 3600;
				final int minutes = ((int) remainingTime % 3600) / 60;
				if (hours > 0)
				{
					final var sm = SystemMessage.getSystemMessage(SystemMessageId.ITEM_PURCHASABLE_IN_S1_HOURS_S2_MINUTES);
					sm.addNumber(hours);
					sm.addNumber(minutes);
					player.sendPacket(sm);
					htmltext = "32599-scroll24.htm";
				}
				else if (minutes > 0)
				{
					final var sm = SystemMessage.getSystemMessage(SystemMessageId.ITEM_PURCHASABLE_IN_S1_MINUTES);
					sm.addNumber(minutes);
					player.sendPacket(sm);
					htmltext = "32599-scroll24.htm";
				}
				else
				{
					if (isTakeRequestItems(player, _template, 13540))
					{
						calcReward(player, _template, 13540);
						saveGlobalQuestVar(player.getAccountName(), Long.toString(System.currentTimeMillis() + (6 * 3600000)));
						htmltext = "32599-scroll24.htm";
					}
					else
					{
						htmltext = "32599-s24-no.htm";
					}
				}
			}
		}
		else if (event.equalsIgnoreCase("buy_scroll_1"))
		{
			if (isTakeRequestItems(player, _template, 13541))
			{
				calcReward(player, _template, 13541);
				htmltext = "32599-scroll-ok.htm";
			}
			else
			{
				htmltext = "32599-s1-no.htm";
			}
		}
		else if (event.equalsIgnoreCase("receive_reward"))
		{
			if ((st.getItemEquipped(Inventory.PAPERDOLL_RHAND) == 13539) && (st.getEnchantLevel(13539) > 3))
			{
				calcReward(player, _template, st.getEnchantLevel(13539));
				st.takeItems(13539, 1);
				htmltext = "32599-rewardok.htm";
			}
			else
			{
				htmltext = "32599-rewardnostaff.htm";
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
		
		_template = WorldEventParser.getInstance().getEvent(13);
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
		new MasterOfEnchanting();
	}
}
