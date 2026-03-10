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
import gameserver.model.quest.QuestState;
import gameserver.model.quest.State;

/**
 * Created by LordWinter 24.06.2011
 * Based on L2J Eternity-World
 */
public final class _700_CursedLife extends Quest
{
	// NPCs
	private static final int ORBYU = 32560;

	// MOBs
	private static final int[] MOBS	= { 22602, 22603, 22604, 22605 };

	// Quest Item
	private static final int SWALLOWED_SKULL   = 13872;
	private static final int SWALLOWED_STERNUM = 13873;
	private static final int SWALLOWED_BONES   = 13874;

	private _700_CursedLife()
	{
		super(700);

		addStartNpc(ORBYU);
		addTalkId(ORBYU);

		for (final int i : MOBS)
		{
			addKillId(i);
		}

		questItemIds = new int[] { SWALLOWED_SKULL, SWALLOWED_STERNUM, SWALLOWED_BONES };
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

		if (event.equalsIgnoreCase("32560-03.htm"))
		{
			if (st.isCreated())
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound("ItemSound.quest_accept");
			}
		}
		else if (event.equalsIgnoreCase("32560-quit.htm"))
		{
			st.exitQuest(true);
			st.playSound("ItemSound.quest_finish");
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

		if (npc.getId() == ORBYU)
		{
			final QuestState first = player.getQuestState("_10273_GoodDayToFly");
			if (first != null && first.getState() == State.COMPLETED && st.getState() == State.CREATED && player.getLevel() >= 75)
			{
				htmltext = "32560-01.htm";
			}
			else
			{
				switch (st.getState())
				{
					case State.CREATED:
						htmltext = "32560-00.htm";
						break;
					case State.STARTED:
						final long count1 = st.getQuestItemsCount(SWALLOWED_BONES);
						final long count2 = st.getQuestItemsCount(SWALLOWED_STERNUM);
						final long count3 = st.getQuestItemsCount(SWALLOWED_SKULL);
						if (count1 > 0 || count2 > 0 || count3 > 0)
						{
							final long reward = ((count1 * 500) + (count2 * 5000) + (count3 * 50000));
							st.takeItems(SWALLOWED_BONES, -1);
							st.takeItems(SWALLOWED_STERNUM, -1);
							st.takeItems(SWALLOWED_SKULL, -1);
							st.giveItems(57, reward);
							st.playSound("ItemSound.quest_itemget");
							htmltext = "32560-06.htm";
						}
						else
						{
							htmltext = "32560-04.htm";
						}
						break;
				}
			}
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
			if (ArrayUtils.contains(MOBS, npc.getId()))
			{
				final int chance = st.getRandom(100);
				if (chance < 5)
				{
					st.giveItems(SWALLOWED_SKULL, 1);
				}
				else if (chance < 20)
				{
					st.giveItems(SWALLOWED_STERNUM, 1);
				}
				else
				{
					st.giveItems(SWALLOWED_BONES, 1);
				}
				st.playSound("ItemSound.quest_itemget");
			}
		}
		return super.onKill(npc, player, isSummon);
	}

	public static void main(String[] args)
	{
		new _700_CursedLife();
	}
}