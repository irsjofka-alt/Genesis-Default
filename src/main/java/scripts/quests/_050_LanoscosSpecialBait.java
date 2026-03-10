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
 * Rework by LordWinter 08.12.2019
 */
public class _050_LanoscosSpecialBait extends Quest
{
	private _050_LanoscosSpecialBait()
	{
		super(50);
		
		addStartNpc(31570);
		addTalkId(31570);
		
		addKillId(21026);
		
		questItemIds = new int[]
		{
		        7621
		};
	}

	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		final QuestState st = player.getQuestState(getName());
		if (st == null || st.isCompleted())
		{
			return htmltext;
		}

		if (event.equalsIgnoreCase("31570-03.htm"))
		{
			st.startQuest();
		}
		else if (event.equalsIgnoreCase("31570-06.htm"))
		{
			if (st.getQuestItemsCount(7621) < 100)
			{
				htmltext = "31570-07.htm";
			}
			else
			{
				st.takeItems(7621, -1);
				st.calcReward(getId());
				st.exitQuest(false, true);
			}
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

		final int npcId = npc.getId();
		final int cond = st.getCond();
		final int id = st.getState();
		if (npcId == 31570)
		{
			if (id == State.CREATED)
			{
				if (player.getLevel() < getMinLvl(getId()))
				{
					htmltext = "31570-02a.htm";
					st.exitQuest(true);
				}
				else if (player.getSkillLevel(1315) >= 8)
				{
					htmltext = "31570-01.htm";
				}
				else
				{
					htmltext = "31570-02.htm";
					st.exitQuest(true);
				}
			}
			else if ((cond == 1) || (cond == 2))
			{
				if (st.getQuestItemsCount(7621) < 100)
				{
					htmltext = "31570-05.htm";
				}
				else
				{
					htmltext = "31570-04.htm";
				}
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(Npc npc, Player player, boolean isSummon)
	{
		final Player partyMember = getRandomPartyMember(player, 1);
		if (partyMember == null)
		{
			return super.onKill(npc, player, isSummon);
		}
		
		final QuestState st = partyMember.getQuestState(getName());
		if (st.calcDropItems(getId(), 7621, npc.getId(), 100))
		{
			st.setCond(2);
		}
		return super.onKill(npc, player, isSummon);
	}

	public static void main(String[] args)
	{
		new _050_LanoscosSpecialBait();
	}
}
