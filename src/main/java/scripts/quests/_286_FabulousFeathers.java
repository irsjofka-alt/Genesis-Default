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

import l2e.commons.util.Rnd;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.quest.Quest;
import gameserver.model.quest.QuestState;
import gameserver.model.quest.State;

/**
 * Created by LordWinter 19.01.2013 Based on L2J Eternity-World
 */
public class _286_FabulousFeathers extends Quest
{
	private static int ERINU = 32164;

	private static final int[] MOBS =
	{
	                22251, 22253, 22254, 22255, 22256
	};

	private static int FEATHER = 9746;

	private _286_FabulousFeathers()
	{
		super(286);

		addStartNpc(ERINU);
		addTalkId(ERINU);

		for (final int mob : MOBS)
		{
			addKillId(mob);
		}

		questItemIds = new int[]
		{
		                FEATHER
		};
	}

	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final String htmltext = event;
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return null;
		}

		if (event.equalsIgnoreCase("32164-03.htm"))
		{
			if (st.isCreated())
			{
				st.set("cond", "1");
				st.setState(State.STARTED);
				st.playSound("ItemSound.quest_accept");
			}
		}
		else if (event.equalsIgnoreCase("32164-06.htm"))
		{
			if (st.isCond(2))
			{
				st.takeItems(FEATHER, -1);
				st.giveItems(57, 4160);
				st.exitQuest(true, true);
			}
		}
		return htmltext;
	}

	@Override
	public String onTalk(Npc npc, Player player)
	{
		String htmltext = Quest.getNoQuestMsg(player);
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}

		final int cond = st.getInt("cond");

		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() >= 17)
				{
					htmltext = "32164-01.htm";
				}
				else
				{
					htmltext = "32164-02.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				if (cond == 1)
				{
					htmltext = "32164-04.htm";
				}
				else if (cond == 2)
				{
					htmltext = "32164-05.htm";
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
		
		final QuestState st = partyMember.getQuestState(getName());
		if (st != null)
		{
			if (Rnd.chance(70))
			{
				st.giveItems(FEATHER, 1);
				st.playSound("ItemSound.quest_itemget");
				if (st.getQuestItemsCount(FEATHER) == 80)
				{
					st.set("cond", "2");
					st.playSound("ItemSound.quest_middle");
				}
			}
		}
		return super.onKill(npc, player, isSummon);
	}

	public static void main(String[] args)
	{
		new _286_FabulousFeathers();
	}
}
