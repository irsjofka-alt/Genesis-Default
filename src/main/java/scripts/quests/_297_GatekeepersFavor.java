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
 * Created by LordWinter 08.07.2012
 * Based on L2J Eternity-World
 */
public class _297_GatekeepersFavor extends Quest
{
	// NPC
	private static final int WIRPHY = 30540;
	
	// Item
	private static final int STARSTONE = 1573;
	
	// Reward
	private static final int GATEKEEPER_TOKEN = 1659;
	
	// Monster
	private static final int WHINSTONE_GOLEM = 20521;
	
	private _297_GatekeepersFavor()
	{
		super(297);
		
		addStartNpc(WIRPHY);
		addTalkId(WIRPHY);

		addKillId(WHINSTONE_GOLEM);

		questItemIds = new int[] { STARSTONE };
	}
			
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final String htmltext = event;
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		if (event.equalsIgnoreCase("30540-03.htm"))
		{
			if (st.isCreated())
			{
				st.set("cond", "1");
				st.setState(State.STARTED);
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
		if(st == null)
		{
			return htmltext;
		}

		final int cond = st.getInt("cond");
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() >= 15 && player.getLevel() <= 21)
				{
					htmltext = "30540-02.htm";
				}
				else
				{
					htmltext = "30540-01.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				if (cond == 1)
				{
					htmltext = "30540-04.htm";
				}
				else if (cond == 2)
				{
					if (st.getQuestItemsCount(STARSTONE) == 20)
					{
						htmltext = "30540-05.htm";
						st.takeItems(STARSTONE, 20);
						st.rewardItems(GATEKEEPER_TOKEN, 2);
						st.playSound("ItemSound.quest_finish");
						st.exitQuest(true);
					}
					else
					{
						htmltext = "30540-04.htm";
					}
				}
				break;
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg(player);
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isSummon)
	{
		final var partyMember = getRandomPartyMember(player, 1);
		if (partyMember == null)
		{
			return super.onKill(npc, player, isSummon);
		}
		
		final QuestState st = partyMember.getQuestState(getName());
		if (st != null)
		{
			if (st.getRandom(10) < 5)
			{
				st.giveItems(STARSTONE, 1);
				if (st.getQuestItemsCount(STARSTONE) == 20)
				{
					st.set("cond", "2");
					st.playSound("ItemSound.quest_middle");
				}
				else
				{
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		return super.onKill(npc, player, isSummon);
	}

	public static void main(String[] args)
	{
		new _297_GatekeepersFavor();
	}
}