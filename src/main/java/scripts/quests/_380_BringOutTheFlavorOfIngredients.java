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
 * Created by LordWinter 13.01.2013 Based on L2J Eternity-World
 */
public class _380_BringOutTheFlavorOfIngredients extends Quest
{
	private final static int DIRE_WOLF = 20205;
	private final static int KADIF_WEREWOLF = 20206;
	private final static int GIANT_MIST_LEECH = 20225;
	
	private final static int RITRONS_FRUIT = 5895;
	private final static int MOON_FACE_FLOWER = 5896;
	private final static int LEECH_FLUIDS = 5897;
	private final static int ANTIDOTE = 1831;
	
	private final static int RITRON_JELLY = 5960;
	private final static int JELLY_RECIPE = 5959;
	
	private final static int REC_CHANCE = 55;
	
	private _380_BringOutTheFlavorOfIngredients()
	{
		super(380);
		
		addStartNpc(30069);
		addTalkId(30069);
		
		addKillId(DIRE_WOLF, KADIF_WEREWOLF, GIANT_MIST_LEECH);

		questItemIds = new int[]
		{
			RITRONS_FRUIT,
			MOON_FACE_FLOWER,
			LEECH_FLUIDS
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
		
		if (event.equalsIgnoreCase("30069-04.htm"))
		{
			if (st.isCreated())
			{
				st.startQuest();
			}
		}
		else if (event.equalsIgnoreCase("30069-12.htm"))
		{
			if (st.isCond(6))
			{
				if (getRandom(100) < REC_CHANCE)
				{
					st.giveItems(JELLY_RECIPE, 1);
				}
				else
				{
					st.giveItems(RITRON_JELLY, 1);
					htmltext = "30069-13.htm";
				}
				st.exitQuest(true, true);
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
				if (player.getLevel() >= 24)
				{
					htmltext = "30069-01.htm";
				}
				else
				{
					htmltext = "30069-00.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				final int cond = st.getInt("cond");
				if (cond == 1)
				{
					htmltext = "30069-06.htm";
				}
				else if (cond == 2)
				{
					if (st.getQuestItemsCount(ANTIDOTE) >= 2)
					{
						st.takeItems(RITRONS_FRUIT, -1);
						st.takeItems(MOON_FACE_FLOWER, -1);
						st.takeItems(LEECH_FLUIDS, -1);
						st.takeItems(ANTIDOTE, 2);
						st.setCond(3, true);
						htmltext = "30069-07.htm";
					}
					else
					{
						htmltext = "30069-06.htm";
					}
				}
				else if (cond == 3)
				{
					st.setCond(4, true);
					htmltext = "30069-08.htm";
				}
				else if (cond == 4)
				{
					st.setCond(5, true);
					htmltext = "30069-09.htm";
				}
				else if (cond == 5)
				{
					st.setCond(6, true);
					htmltext = "30069-10.htm";
				}
				else if (cond == 6)
				{
					htmltext = "30069-11.htm";
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
				case DIRE_WOLF :
					if (st.dropItems(RITRONS_FRUIT, 1, 4, 100000))
					{
						if (st.getQuestItemsCount(MOON_FACE_FLOWER) >= 20 && st.getQuestItemsCount(LEECH_FLUIDS) >= 10)
						{
							st.setCond(2, true);
						}
					}
					break;
				case KADIF_WEREWOLF :
					if (st.dropItems(MOON_FACE_FLOWER, 1, 20, 250000))
					{
						if (st.getQuestItemsCount(RITRONS_FRUIT) >= 4 && st.getQuestItemsCount(LEECH_FLUIDS) >= 10)
						{
							st.setCond(2, true);
						}
					}
					break;
				case GIANT_MIST_LEECH :
					if (st.dropItems(LEECH_FLUIDS, 1, 10, 250000))
					{
						if (st.getQuestItemsCount(RITRONS_FRUIT) >= 4 && st.getQuestItemsCount(MOON_FACE_FLOWER) >= 20)
						{
							st.setCond(2, true);
						}
					}
					break;
			}
		}
		return super.onKill(npc, player, isSummon);
	}
	
	public static void main(String[] args)
	{
		new _380_BringOutTheFlavorOfIngredients();
	}
}