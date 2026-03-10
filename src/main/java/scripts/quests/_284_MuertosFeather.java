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
 * Created by LordWinter 18.01.2013 Based on L2J Eternity-World
 */
public class _284_MuertosFeather extends Quest
{
	private static final int TREVOR = 32166;

	private static final int[] MOBS =
	{
		22239, 22240, 22242, 22243, 22245, 22246
	};

	private static final int FEATHER = 9748;

	private _284_MuertosFeather()
	{
		super(284);

		addStartNpc(TREVOR);
		addTalkId(TREVOR);

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

		if(event.equalsIgnoreCase("32166-03.htm"))
		{
			if (st.isCreated())
			{
       			st.set("cond", "1");
       			st.setState(State.STARTED);
       			st.playSound("ItemSound.quest_accept");
			}
		}
		else if(event.equalsIgnoreCase("32166-06.htm"))
		{
			final long counts = st.getQuestItemsCount(FEATHER) * 45;
			st.takeItems(FEATHER, -1);
			if (counts > 0)
			{
				st.giveItems(57, counts);
			}
		}
		else if(event.equalsIgnoreCase("32166-08.htm"))
		{
			st.takeItems(FEATHER, -1);
			st.exitQuest(true);
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

		switch (st.getState())
		{
			case State.CREATED:
				if(player.getLevel() < 11)
				{
					htmltext = "32166-02.htm";
					st.exitQuest(true);
				}
				else
				{
					htmltext = "32166-01.htm";
				}
				break;
			case State.STARTED:
				if(st.getQuestItemsCount(FEATHER) == 0)
				{
					htmltext = "32166-04.htm";
				}
				else
				{
					htmltext = "32166-05.htm";
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
       		final int chance = st.getRandom(100);
			if (chance < 70)
			{
				st.giveItems(FEATHER, 1);
				st.playSound("ItemSound.quest_itemget");
			}
		}
		return super.onKill(npc, player, isSummon);
	}

	public static void main(String[] args)
	{
		new _284_MuertosFeather();
	}
}