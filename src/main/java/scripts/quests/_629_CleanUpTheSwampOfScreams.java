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

import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.quest.Quest;
import gameserver.model.quest.State;

/**
 * Rework by LordWinter 03.06.2024
 */
public class _629_CleanUpTheSwampOfScreams extends Quest
{
	private _629_CleanUpTheSwampOfScreams()
	{
		super(629);
		
		addStartNpc(31553);
		addTalkId(31553);
		
		for (int i = 21508; i <= 21517; i++)
		{
			addKillId(i);
		}
		
		questItemIds = new int[]
		{
		        7250, 7251
		};
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		final var st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		if (event.equalsIgnoreCase("31553-1.htm"))
		{
			if ((player.getLevel() >= getMinLvl(getId()) && player.getLevel() <= getMaxLvl(getId())) && st.isCreated())
			{
				st.startQuest();
			}
			else
			{
				htmltext = "31553-0a.htm";
				st.exitQuest(true);
			}
		}
		else if (event.equalsIgnoreCase("31553-3.htm"))
		{
			if (st.getQuestItemsCount(7250) >= 100)
			{
				st.takeItems(7250, 100);
				st.calcReward(getId());
			}
			else
			{
				htmltext = "31553-3a.htm";
			}
		}
		else if (event.equalsIgnoreCase("31553-5.htm"))
		{
			st.exitQuest(true, true);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		String htmltext = getNoQuestMsg(player);
		final var st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		if ((st.hasQuestItems(7246)) || (st.hasQuestItems(7247)))
		{
			switch (st.getState())
			{
				case State.CREATED :
					if (player.getLevel() >= getMinLvl(getId()) && player.getLevel() <= getMaxLvl(getId()))
					{
						htmltext = "31553-0.htm";
					}
					else
					{
						htmltext = "31553-0a.htm";
						st.exitQuest(true);
					}
					break;
				case State.STARTED :
					if (st.getQuestItemsCount(7250) >= 100)
					{
						htmltext = "31553-2.htm";
					}
					else
					{
						htmltext = "31553-1a.htm";
					}
					break;
			}
		}
		else
		{
			htmltext = "31553-6.htm";
			st.exitQuest(true);
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isSummon)
	{
		final var partyMember = getRandomPartyMemberState(player, State.STARTED);
		if (partyMember == null)
		{
			return super.onKill(npc, player, isSummon);
		}
		
		final var st = partyMember.getQuestState(getName());
		if (st != null)
		{
			st.calcDropItems(getId(), 7250, npc.getId(), Integer.MAX_VALUE);
		}
		return super.onKill(npc, player, isSummon);
	}
	
	public static void main(String[] args)
	{
		new _629_CleanUpTheSwampOfScreams();
	}
}