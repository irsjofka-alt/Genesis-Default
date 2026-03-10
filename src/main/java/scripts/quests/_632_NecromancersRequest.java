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

import org.apache.commons.lang3.ArrayUtils;

import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.quest.Quest;
import gameserver.model.quest.State;

/**
 * Rework by LordWinter 03.06.2024
 */
public class _632_NecromancersRequest extends Quest
{
	private static final int[] VAMPIRES =
	{
	        21568, 21573, 21582, 21585, 21586, 21587, 21588, 21589, 21590, 21591, 21592, 21593, 21594, 21595
	};
	
	private static final int[] UNDEADS =
	{
	        21547, 21548, 21549, 21551, 21552, 21555, 21556, 21562, 21571, 21576, 21577, 21579
	};
	
	private _632_NecromancersRequest()
	{
		super(632);
		
		addStartNpc(31522);
		addTalkId(31522);
		
		addKillId(VAMPIRES);
		addKillId(UNDEADS);
		
		questItemIds = new int[]
		{
		        7542, 7543
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
		
		if (event.equalsIgnoreCase("31522-03.htm"))
		{
			if ((player.getLevel() >= getMinLvl(getId()) && player.getLevel() <= getMaxLvl(getId())) && st.isCreated())
			{
				st.startQuest();
			}
		}
		else if (event.equalsIgnoreCase("31522-06.htm"))
		{
			if (st.getQuestItemsCount(7542) >= 200)
			{
				st.takeItems(7542, -1);
				st.calcReward(getId());
				st.setCond(1, true);
			}
			else
			{
				htmltext = "31522-09.htm";
			}
		}
		else if (event.equalsIgnoreCase("31522-08.htm"))
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
		
		switch (st.getState())
		{
			case State.CREATED :
				if ((player.getLevel() >= getMinLvl(getId()) && player.getLevel() <= getMaxLvl(getId())) && st.isCreated())
				{
					htmltext = "31522-02.htm";
				}
				else
				{
					st.exitQuest(true);
					htmltext = "31522-01.htm";
				}
				break;
			case State.STARTED :
				htmltext = st.getQuestItemsCount(7542) >= 200 ? "31522-05.htm" : "31522-04.htm";
				break;
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
			if (ArrayUtils.contains(UNDEADS, npc.getId()))
			{
				st.calcDropItems(getId(), 7543, npc.getId(), Integer.MAX_VALUE);
			}
			else
			{
				if (st.isCond(1) && ArrayUtils.contains(VAMPIRES, npc.getId()) && st.calcDropItems(getId(), 7542, npc.getId(), 200))
				{
					st.setCond(2, true);
				}
			}
		}
		return super.onKill(npc, player, isSummon);
	}
	
	public static void main(String[] args)
	{
		new _632_NecromancersRequest();
	}
}