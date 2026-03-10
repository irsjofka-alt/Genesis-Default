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
package scripts.quests;

import java.util.Calendar;

import gameserver.instancemanager.ReflectionManager;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.entity.Reflection;
import gameserver.model.quest.Quest;
import gameserver.model.quest.State;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.SystemMessage;

/**
 * Rework by LordWinter 18.09.2020
 */
public final class _694_BreakThroughTheHallOfSuffering extends Quest
{
	private _694_BreakThroughTheHallOfSuffering()
	{
		super(694);
		
		addStartNpc(32603);
		addTalkId(32603);
		addTalkId(32530);
	}
	
	@Override
	public final String onAdvEvent(String event, Npc npc, Player player)
	{
		final String htmltext = event;
		final var st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		if (event.equalsIgnoreCase("32603-02.htm"))
		{
			if (st.isCreated())
			{
				st.startQuest();
			}
		}
		return htmltext;
	}
	
	@Override
	public final String onTalk(Npc npc, Player player)
	{
		String htmltext = getNoQuestMsg(player);
		final var st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		switch (st.getState())
		{
			case State.CREATED:
				if ((player.getLevel() >= getMinLvl(getId())) && (player.getLevel() <= getMaxLvl(getId())))
				{
					htmltext = "32603-01.htm";
				}
				else
				{
					htmltext = "32603-00.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				switch (npc.getId())
				{
					case 32603 :
						htmltext = "32603-01a.htm";
						break;
					case 32530 :
						final var r = player.getReflection();
						if (!r.isDefault() && r.getTemplateId() == 115)
						{
							if (r.getParams().getInteger("TAG", -1) == -1)
							{
								htmltext = "32530-11.htm";
							}
							else if ((player.getParty() != null) && (player.getParty().getLeaderObjectId() == player.getObjectId()))
							{
								for (final var member : player.getParty().getMembers())
								{
									final var st1 = member.getQuestState(getName());
									if (st1 != null)
									{
										if (r.getParams().getInteger("TAG", -1) == 13777)
										{
											if (st1.getQuestItemsCount(13691) == 0)
											{
												st1.giveItems(13691, 1);
											}
											st1.calcReward(getId(), 1);
											st1.exitQuest(true, true);
											htmltext = "32530-00.htm";
											finishInstance(r);
										}
										else if (r.getParams().getInteger("TAG", -1) == 13778)
										{
											if (st1.getQuestItemsCount(13691) == 0)
											{
												st1.giveItems(13691, 1);
											}
											st1.calcReward(getId(), 2);
											st1.exitQuest(true, true);
											htmltext = "32530-01.htm";
											finishInstance(r);
										}
										else if (r.getParams().getInteger("TAG", -1) == 13779)
										{
											if (st1.getQuestItemsCount(13691) == 0)
											{
												st1.giveItems(13691, 1);
											}
											st1.calcReward(getId(), 3);
											st1.exitQuest(true, true);
											htmltext = "32530-02.htm";
											finishInstance(r);
										}
										else if (r.getParams().getInteger("TAG", -1) == 13780)
										{
											if (st1.getQuestItemsCount(13691) == 0)
											{
												st1.giveItems(13691, 1);
											}
											st1.calcReward(getId(), 4);
											st1.exitQuest(true, true);
											htmltext = "32530-03.htm";
											finishInstance(r);
										}
										else if (r.getParams().getInteger("TAG", -1) == 13781)
										{
											if (st1.getQuestItemsCount(13691) == 0)
											{
												st1.giveItems(13691, 1);
											}
											st1.calcReward(getId(), 5);
											st1.exitQuest(true, true);
											htmltext = "32530-04.htm";
											finishInstance(r);
										}
										else if (r.getParams().getInteger("TAG", -1) == 13782)
										{
											if (st1.getQuestItemsCount(13691) == 0)
											{
												st1.giveItems(13691, 1);
											}
											st1.calcReward(getId(), 6);
											st1.exitQuest(true, true);
											htmltext = "32530-05.htm";
											finishInstance(r);
										}
										else if (r.getParams().getInteger("TAG", -1) == 13783)
										{
											if (st1.getQuestItemsCount(13691) == 0)
											{
												st1.giveItems(13691, 1);
											}
											st1.calcReward(getId(), 7);
											st1.exitQuest(true, true);
											htmltext = "32530-06.htm";
											finishInstance(r);
										}
										else if (r.getParams().getInteger("TAG", -1) == 13784)
										{
											if (st1.getQuestItemsCount(13691) == 0)
											{
												st1.giveItems(13691, 1);
											}
											st1.calcReward(getId(), 8);
											st1.exitQuest(true, true);
											htmltext = "32530-07.htm";
											finishInstance(r);
										}
										else if (r.getParams().getInteger("TAG", -1) == 13785)
										{
											if (st1.getQuestItemsCount(13691) == 0)
											{
												st1.giveItems(13691, 1);
											}
											st1.calcReward(getId(), 9);
											st1.exitQuest(true, true);
											htmltext = "32530-08.htm";
											finishInstance(r);
										}
										else if (r.getParams().getInteger("TAG", -1) == 13786)
										{
											if (st1.getQuestItemsCount(13691) == 0)
											{
												st1.giveItems(13691, 1);
											}
											st1.calcReward(getId(), 10);
											st1.exitQuest(true, true);
											htmltext = "32530-09.htm";
											finishInstance(r);
										}
										else
										{
											htmltext = "32530-11.htm";
										}
									}
								}
							}
							else
							{
								return "32530-10.htm";
							}
						}
						else
						{
							htmltext = "32530-11.htm";
						}
						break;
				}
				break;
		}
		return htmltext;
	}
	
	private static final void finishInstance(Reflection r)
	{
		final var reenter = Calendar.getInstance();
		reenter.set(Calendar.MINUTE, 30);
		
		if (reenter.get(Calendar.HOUR_OF_DAY) >= 6)
		{
			reenter.add(Calendar.DATE, 1);
		}
		reenter.set(Calendar.HOUR_OF_DAY, 6);
		
		final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.INSTANT_ZONE_S1_RESTRICTED);
		sm.addInstanceName(r.getTemplateId());
		
		final var instance = ReflectionManager.getInstance();
		for (final var obj : r.getAllowed())
		{
			instance.setReflectionTime(obj, r.getTemplateId(), reenter.getTimeInMillis(), r.isHwidCheck());
			if ((obj != null) && obj.isOnline())
			{
				obj.sendPacket(sm);
			}
		}
		r.setDuration(5 * 60000);
		r.setEmptyDestroyTime(0);
	}
	
	public static void main(String[] args)
	{
		new _694_BreakThroughTheHallOfSuffering();
	}
}