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
import gameserver.model.quest.State;

/**
 * Rework by LordWinter 03.06.2024
 */
public final class _638_SeekersOfTheHolyGrail extends Quest
{
	private _638_SeekersOfTheHolyGrail()
	{
		super(638);
		
		addStartNpc(31328);
		addTalkId(31328);
		
		addKillId(22138, 22139, 22140, 22142, 22143, 22144, 22145, 22146, 22147, 22148, 22149, 22150, 22151, 22152, 22153, 22154, 22154, 22155, 22156, 22157, 22158, 22159, 22160, 22161, 22162, 22163, 22164, 22165, 22166, 22167, 22168, 22169, 22170, 22171, 22172, 22173, 22174, 22175);
		
		questItemIds = new int[]
		{
		        8068
		};
	}
	
	@Override
	public final String onAdvEvent(String event, Npc npc, Player player)
	{
		final String htmltext = event;
		final var st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		if (event.equalsIgnoreCase("31328-02.htm"))
		{
			if ((player.getLevel() >= getMinLvl(getId()) && player.getLevel() <= getMaxLvl(getId())) && st.isCreated())
			{
				st.startQuest();
			}
		}
		else if (event.equalsIgnoreCase("31328-06.htm"))
		{
			st.exitQuest(true, true);
		}
		return htmltext;
	}
	
	@Override
	public final String onTalk(Npc npc, Player player)
	{
		String htmltext = Quest.getNoQuestMsg(player);
		final var st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		switch (st.getState())
		{
			case State.CREATED :
				if (player.getLevel() >= getMinLvl(getId()) && player.getLevel() <= getMaxLvl(getId()))
				{
					htmltext = "31328-01.htm";
				}
				else
				{
					htmltext = "31328-00.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED :
				if (st.getQuestItemsCount(8068) >= 2000)
				{
					st.takeItems(8068, 2000);
					st.calcReward(getId(), Rnd.get(1, 3));
					htmltext = "31328-03.htm";
				}
				else
				{
					htmltext = "31328-04.htm";
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
			st.calcDropItems(getId(), 8068, npc.getId(), Integer.MAX_VALUE);
			st.calcDropItems(getId(), 8273, npc.getId(), Integer.MAX_VALUE);
			st.calcDropItems(getId(), 8274, npc.getId(), Integer.MAX_VALUE);
			st.calcDropItems(getId(), 8275, npc.getId(), Integer.MAX_VALUE);
		}
		return super.onKill(npc, player, isSummon);
	}
	
	public static void main(String[] args)
	{
		new _638_SeekersOfTheHolyGrail();
	}
}