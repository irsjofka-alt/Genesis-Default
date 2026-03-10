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
import gameserver.model.quest.State;

/**
 * Rework by LordWinter 03.06.2024
 */
public class _633_InTheForgottenVillage extends Quest
{
	private static final int[] MOBS =
	{
	        21557, 21558, 21559, 21560, 21563, 21564, 21565, 21566, 21567, 21572, 21574, 21575, 21580, 21581, 21583, 21584, 401000
	};
	
	private static final int[] UNDEADS =
	{
	        21553, 21554, 21561, 21578, 21596, 21597, 21598, 21599, 21600, 21601, 411000
	};
	
	private _633_InTheForgottenVillage()
	{
		super(633);
		
		addStartNpc(31388);
		addTalkId(31388);
		
		addKillId(MOBS);
		addKillId(UNDEADS);

		questItemIds = new int[]
		{
		        7544, 7545
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
		
		if (event.equalsIgnoreCase("31388-04.htm"))
		{
			if ((player.getLevel() >= getMinLvl(getId()) && player.getLevel() <= getMaxLvl(getId())) && st.isCreated())
			{
				st.startQuest();
			}
		}
		else if (event.equalsIgnoreCase("31388-10.htm"))
		{
			st.takeItems(7544, -1);
			st.exitQuest(true, true);
		}
		else if (event.equalsIgnoreCase("31388-09.htm"))
		{
			if (st.getQuestItemsCount(7544) >= 200)
			{
				htmltext = "31388-08.htm";
				st.takeItems(7544, 200);
				st.calcExpAndSp(getId());
				st.calcReward(getId());
				st.setCond(1, true);
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
				if (player.getLevel() >= getMinLvl(getId()) && player.getLevel() <= getMaxLvl(getId()))
				{
					htmltext = "31388-01.htm";
				}
				else
				{
					htmltext = "31388-03.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				final int cond = st.getCond();
				if (cond == 1)
				{
					htmltext = "31388-06.htm";
				}
				else if (cond == 2)
				{
					htmltext = "31388-05.htm";
				}
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isSummon)
	{
		if (ArrayUtils.contains(UNDEADS, npc.getId()))
		{
			final var partyMember = getRandomPartyMemberState(player, State.STARTED);
			if (partyMember == null)
			{
				return super.onKill(npc, player, isSummon);
			}
			
			final var st = partyMember.getQuestState(getName());
			if (st != null)
			{
				st.calcDropItems(getId(), 7545, npc.getId(), Integer.MAX_VALUE);
			}
		}
		else if (ArrayUtils.contains(MOBS, npc.getId()))
		{
			final var partyMember = getRandomPartyMember(player, 1);
			if (partyMember == null)
			{
				return super.onKill(npc, player, isSummon);
			}
			
			final var st = partyMember.getQuestState(getName());
			if (st != null && st.calcDropItems(getId(), 7544, npc.getId(), 200))
			{
				st.setCond(2, true);
			}
		}
		return super.onKill(npc, player, isSummon);
	}
	
	public static void main(String[] args)
	{
		new _633_InTheForgottenVillage();
	}
}