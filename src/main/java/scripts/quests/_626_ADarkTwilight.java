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
 * Rework by LordWinter 13.06.2024
 */
public class _626_ADarkTwilight extends Quest
{
	private _626_ADarkTwilight()
	{
		super(626);
		
		addStartNpc(31517);
		addTalkId(31517);
		
		addKillId(21520, 21523, 21524, 21526, 21529, 21530, 21531, 21532, 21535, 21536, 21539, 21540);
		
		questItemIds = new int[]
		{
		        7169
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
		
		if (event.equalsIgnoreCase("31517-03.htm"))
		{
			if (st.isCreated())
			{
				st.startQuest();
			}
		}
		else if (event.equalsIgnoreCase("reward1"))
		{
			if (st.isCond(2) && st.getQuestItemsCount(7169) >= 300L)
			{
				htmltext = "31517-07.htm";
				st.takeItems(7169, 300L);
				st.calcExpAndSp(getId());
				st.exitQuest(true, true);
			}
			else
			{
				htmltext = "31517-08.htm";
			}
		}
		else if (event.equalsIgnoreCase("reward2"))
		{
			if (st.isCond(2) && st.getQuestItemsCount(7169) >= 300L)
			{
				htmltext = "31517-07.htm";
				st.takeItems(7169, 300L);
				st.calcReward(getId());
				st.exitQuest(true, true);
			}
			else
			{
				htmltext = "31517-08.htm";
			}
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
			case State.CREATED:
				if ((player.getLevel() >= getMinLvl(getId())) && (player.getLevel() <= getMaxLvl(getId())))
				{
					htmltext = "31517-01.htm";
				}
				else
				{
					htmltext = "31517-02.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				final int cond = st.getInt("cond");
				if ((cond == 1) && (st.getQuestItemsCount(7169) < 300L))
				{
					htmltext = "31517-05.htm";
				}
				else
				{
					if (cond != 2)
					{
						break;
					}
					htmltext = "31517-04.htm";
				}
				break;
		}
		return htmltext;
	}
	
	@Override
	public final String onKill(Npc npc, Player player, boolean isSummon)
	{
		final var partyMember = getRandomPartyMember(player, 1);
		if (partyMember == null)
		{
			return super.onKill(npc, player, isSummon);
		}
		
		final var st = partyMember.getQuestState(getName());
		if (st != null)
		{
			if (st.calcDropItems(getId(), 7169, npc.getId(), 300))
			{
				st.setCond(2);
			}
		}
		return super.onKill(npc, player, isSummon);
	}
	
	public static void main(String[] args)
	{
		new _626_ADarkTwilight();
	}
}