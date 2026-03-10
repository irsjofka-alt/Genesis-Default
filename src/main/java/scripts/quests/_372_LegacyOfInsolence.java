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
import gameserver.model.quest.QuestState;
import gameserver.model.quest.State;

/**
 * Rework by LordWinter 03.06.2024
 */
public class _372_LegacyOfInsolence extends Quest
{
	private static final int[][] SCROLLS =
	{
		{
			5989,
			6001
		},
		{
			5984,
			5988
		},
		{
			5979,
			5983
		},
		{
			5972,
			5978
		},
		{
			5972,
			5978
		}
	};
	
	private _372_LegacyOfInsolence()
	{
		super(372);
		
		addStartNpc(30844);
		addTalkId(30844, 30929, 30839, 31001, 30855);
		
		addKillId(20817, 20821, 20825, 20829, 21069, 21063);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		if (event.equalsIgnoreCase("30844-04.htm"))
		{
			if ((player.getLevel() >= getMinLvl(getId()) && player.getLevel() <= getMaxLvl(getId())) && st.isCreated())
			{
				st.startQuest();
			}
		}
		else if (event.equalsIgnoreCase("30844-05b.htm"))
		{
			if (st.isCond(1))
			{
				st.setCond(2, true);
			}
		}
		else if (event.equalsIgnoreCase("30844-07.htm"))
		{
			for (int i = 5989; i <= 6001; i++)
			{
				if (!st.hasQuestItems(i))
				{
					htmltext = "30844-06.htm";
					break;
				}
			}
		}
		else if (event.startsWith("30844-07-"))
		{
			checkAndRewardItems(this, st, 0, Integer.parseInt(event.substring(9, 10)), 30844);
		}
		else if (event.equalsIgnoreCase("30844-09.htm"))
		{
			st.exitQuest(true, true);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() >= getMinLvl(getId()) && player.getLevel() <= getMaxLvl(getId()))
				{
					htmltext = "30844-02.htm";
				}
				else
				{
					htmltext = "30844-01.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				switch (npc.getId())
				{
					case 30844 :
						htmltext = "30844-05.htm";
						break;
					case 30839 :
						htmltext = checkAndRewardItems(this, st, 1, 4, 30839);
						break;
					case 30929 :
						htmltext = checkAndRewardItems(this, st, 2, 5, 30929);
						break;
					case 31001 :
						htmltext = checkAndRewardItems(this, st, 3, 6, 31001);
						break;
					case 30855 :
						htmltext = checkAndRewardItems(this, st, 4, 7, 30855);
						break;
				}
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
			switch (npc.getId())
			{
				case 20817 :
				case 20821 :
				case 20825 :
					st.calcDropItems(getId(), 5966, npc.getId(), Integer.MAX_VALUE);
					break;
				case 20829 :
					st.calcDropItems(getId(), 5967, npc.getId(), Integer.MAX_VALUE);
					break;
				case 21063 :
					st.calcDropItems(getId(), 5969, npc.getId(), Integer.MAX_VALUE);
					break;
				case 21069 :
					st.calcDropItems(getId(), 5968, npc.getId(), Integer.MAX_VALUE);
					break;
			}
		}
		return super.onKill(npc, player, isSummon);
	}
	
	private static String checkAndRewardItems(Quest q, QuestState st, int itemType, int rewardType, int npcId)
	{
		final int[] itemsToCheck = SCROLLS[itemType];
		for (int item = itemsToCheck[0]; item <= itemsToCheck[1]; item++)
		{
			if (!st.hasQuestItems(item))
			{
				return npcId + ((npcId == 30844) ? "-07a.htm" : "-01.htm");
			}
		}
		
		for (int item = itemsToCheck[0]; item <= itemsToCheck[1]; item++)
		{
			st.takeItems(item, 1);
		}
		
		st.calcReward(q.getId(), rewardType, true);
		return npcId + "-02.htm";
	}
	
	public static void main(String[] args)
	{
		new _372_LegacyOfInsolence();
	}
}