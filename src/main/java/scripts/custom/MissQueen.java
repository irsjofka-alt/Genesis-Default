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
package scripts.custom;

import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.quest.Quest;

public class MissQueen extends Quest
{
	private static final int COUPNE_ONE = 7832;
	private static final int COUPNE_TWO = 7833;

	private static final int[] NPCs =
	{
			31760, 31761, 31762, 31763, 31764, 31765, 31766
	};

	private static final int NEWBIE_REWARD = 16;
	private static final int TRAVELER_REWARD = 32;

	public MissQueen()
	{
		super(-1);

		for (final int i : NPCs)
		{
			addStartNpc(i);
			addFirstTalkId(i);
			addTalkId(i);
		}
	}

	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		final var st = player.getQuestState(getName());
		final int newbie = player.getNewbie();
		final int level = player.getLevel();
		final int occupation_level = player.getClassId().level();
		final int pkkills = player.getPkKills();
		if (event.equals("newbie_give_coupon"))
		{
			if (level >= 6 && level <= 25 && pkkills == 0 && occupation_level == 0)
			{
				if ((newbie | NEWBIE_REWARD) != newbie)
				{
					player.setNewbie(newbie | NEWBIE_REWARD);
					st.giveItems(COUPNE_ONE, 1);
					htmltext = "31760-2.htm";
				}
				else
				{
					htmltext = "31760-1.htm";
				}
			}
			else
			{
				htmltext = "31760-3.htm";
			}
		}
		else if (event.equals("traveller_give_coupon"))
		{
			if (level >= 6 && level <= 25 && pkkills == 0 && occupation_level == 1)
			{
				if ((newbie | TRAVELER_REWARD) != newbie)
				{
					player.setNewbie(newbie | TRAVELER_REWARD);
					st.giveItems(COUPNE_TWO, 1);
					htmltext = "31760-5.htm";
				}
				else
				{
					htmltext = "31760-4.htm";
				}
			}
			else
			{
				htmltext = "31760-6.htm";
			}
		}
		return htmltext;
	}

	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		var st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}
		return "31760.htm";
	}

	public static void main(String args[])
	{
		new MissQueen();
	}
}