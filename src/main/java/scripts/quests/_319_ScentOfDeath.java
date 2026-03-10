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
 * Created by LordWinter 30.09.2012
 * Based on L2J Eternity-World
 */
public class _319_ScentOfDeath extends Quest
{
	// NPC
	private static final int MINALESS = 30138;

	// Item
	private static final int ZOMBIE_SKIN = 1045;
	
	private _319_ScentOfDeath()
	{
		super(319);

		addStartNpc(MINALESS);
		addTalkId(MINALESS);

		addKillId(20015, 20020);

		questItemIds = new int[] { ZOMBIE_SKIN };
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
		
		if (event.equalsIgnoreCase("30138-04.htm"))
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
		final QuestState st = player.getQuestState(getName());
		String htmltext = getNoQuestMsg(player);
		if (st == null)
		{
			return htmltext;
		}
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() >= 11 && player.getLevel() <= 18)
				{
					htmltext = "30138-03.htm";
				}
				else
				{
					htmltext = "30138-02.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				if (st.getQuestItemsCount(ZOMBIE_SKIN) == 5)
				{
					htmltext = "30138-06.htm";
					st.takeItems(ZOMBIE_SKIN, 5);
					st.rewardItems(57, 3350);
					st.rewardItems(1060, 1);
					st.playSound("ItemSound.quest_finish");
					st.exitQuest(true);
				}
				else
				{
					htmltext = "30138-05.htm";
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
		
		final QuestState st = partyMember.getQuestState(getName());
		if (st != null)
		{
			if (st.dropQuestItems(ZOMBIE_SKIN, 1, 5, 300000, true))
			{
				st.set("cond", "2");
			}
		}
		return super.onKill(npc, player, isSummon);
	}
	
	public static void main(String[] args)
	{
		new _319_ScentOfDeath();
	}
}