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
 * Created by LordWinter 12.01.2013 Based on L2J Eternity-World
 */
public class _608_SlayTheEnemyCommander extends Quest
{
	private static final int MOS_HEAD = 7236;
	private static final int TOTEM = 7220;

	private _608_SlayTheEnemyCommander()
	{
		super(608);

		addStartNpc(31370);
		addTalkId(31370);

		addKillId(25312);

		questItemIds = new int[]
		{
		        MOS_HEAD
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

		if (event.equalsIgnoreCase("31370-04.htm"))
		{
			if (st.isCreated())
			{
				st.startQuest();
			}
		}
		else if (event.equalsIgnoreCase("31370-07.htm"))
		{
			if (st.getQuestItemsCount(MOS_HEAD) == 1 && st.isCond(2))
			{
				st.takeItems(MOS_HEAD, -1);
				st.giveItems(TOTEM, 1);
				st.addExpAndSp(10000, 0);
				st.exitQuest(true, true);
			}
			else
			{
				htmltext = "31370-06.htm";
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
		final var st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}

		switch (st.getState())
		{
			case State.CREATED :
				htmltext = "31370-01.htm";
				break;
			case State.STARTED :
				if (st.getQuestItemsCount(MOS_HEAD) > 0)
				{
					htmltext = "31370-05.htm";
				}
				else
				{
					htmltext = "31370-06.htm";
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
					st.giveItems(MOS_HEAD, 1);
					st.setCond(2, true);
				}
			}
		}
		return super.onKill(npc, player, isSummon);
	}

	public static void main(String[] args)
	{
		new _608_SlayTheEnemyCommander();
	}
}