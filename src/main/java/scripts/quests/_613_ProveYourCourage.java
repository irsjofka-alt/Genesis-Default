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
 * Created by LordWinter 12.01.2013 Based on L2J Eternity-World
 */
public class _613_ProveYourCourage extends Quest
{
	private static final int HEKATON_HEAD = 7240;
	private static final int VALOR_FEATHER = 7229;
	private static final int VARKA_ALLIANCE_THREE = 7223;
	
	private _613_ProveYourCourage()
	{
		super(613);
		
		addStartNpc(31377);
		addTalkId(31377);
		
		addKillId(25299);
		
		questItemIds = new int[]
		{
		        HEKATON_HEAD
		};
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
		
		if (event.equalsIgnoreCase("31377-04.htm"))
		{
			st.startQuest();
		}
		else if (event.equalsIgnoreCase("31377-07.htm"))
		{
			if (st.getQuestItemsCount(HEKATON_HEAD) == 1 && st.isCond(2))
			{
				st.takeItems(HEKATON_HEAD, -1);
				st.giveItems(VALOR_FEATHER, 1);
				st.addExpAndSp(10000, 0);
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(true);
			}
			else
			{
				htmltext = "31377-06.htm";
				st.set("cond", "1");
				st.playSound("ItemSound.quest_accept");
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
		
		switch (st.getState())
		{
			case State.CREATED :
				htmltext = (player.getLevel() >= 75) ? (st.hasQuestItems(VARKA_ALLIANCE_THREE)) ? "31377-01.htm" : "31377-02.htm" : "31377-03.htm";
				break;
			case State.STARTED :
				if (st.getQuestItemsCount(HEKATON_HEAD) == 1)
				{
					htmltext = "31377-05.htm";
				}
				else
				{
					htmltext = "31377-06.htm";
				}
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isSummon)
	{
		for (final var partyMember : getMembersCond(player, npc, "cond"))
		{
			if (partyMember != null)
			{
				final var st = partyMember.getQuestState(getName());
				if (st != null && st.isCond(1))
				{
					st.giveItems(HEKATON_HEAD, 1);
					st.setCond(2, true);
				}
			}
		}
		return super.onKill(npc, player, isSummon);
	}
	
	public static void main(String[] args)
	{
		new _613_ProveYourCourage();
	}
}