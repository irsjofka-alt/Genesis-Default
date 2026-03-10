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

import gameserver.ai.model.CtrlIntention;
import gameserver.model.Location;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.quest.Quest;
import gameserver.model.quest.QuestState;
import gameserver.model.quest.QuestState.QuestType;
import gameserver.model.quest.State;
import gameserver.network.NpcStringId;
import gameserver.network.serverpackets.NpcSay;

/**
 * Updated by LordWinter 03.10.2011 Based on L2J Eternity-World
 */
public class _450_GraveRobberMemberRescue extends Quest
{
	private static final String qn = "_450_GraveRobberMemberRescue";

	private static final int KANEMIKA = 32650;
	private static final int WARRIOR_NPC = 32651;

	private static final int WARRIOR_MON = 22741;

	private static final int EVIDENCE_OF_MIGRATION = 14876;

	private _450_GraveRobberMemberRescue()
	{
		super(450);

		addStartNpc(KANEMIKA);
		addTalkId(KANEMIKA);
		addTalkId(WARRIOR_NPC);

		questItemIds = new int[]
		{
		                EVIDENCE_OF_MIGRATION
		};
	}

	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final String htmltext = event;
		final QuestState st = player.getQuestState(qn);
		if (st == null || (st.isCompleted() && !st.isNowAvailable()))
		{
			return htmltext;
		}

		if (event.equalsIgnoreCase("32650-05.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		return htmltext;
	}

	@Override
	public String onTalk(Npc npc, Player player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}

		final int cond = st.getInt("cond");
		final int id = st.getState();
		
		if (npc.getId() == KANEMIKA)
		{
			if ((id == State.CREATED) && (cond == 0))
			{
				if (player.getLevel() >= 80)
				{
					htmltext = "32650-01.htm";
				}
				else
				{
					htmltext = "32650-00.htm";
					st.exitQuest(true);
				}
			}
			else if (id == State.STARTED)
			{
				if (cond == 1)
				{
					if (st.getQuestItemsCount(EVIDENCE_OF_MIGRATION) >= 1)
					{
						htmltext = "32650-07.htm";
					}
					else
					{
						htmltext = "32650-06.htm";
					}
				}
				else if ((cond == 2) && (st.getQuestItemsCount(EVIDENCE_OF_MIGRATION) == 10))
				{
					htmltext = "32650-08.htm";
					st.giveItems(57, 65000);
					st.takeItems(EVIDENCE_OF_MIGRATION, 10);
					st.playSound("ItemSound.quest_finish");
					st.setState(State.COMPLETED);
					st.exitQuest(QuestType.DAILY);
				}
			}
			else if (id == State.COMPLETED)
			{
				if (st.isNowAvailable())
				{
					if (player.getLevel() >= 80)
					{
						htmltext = "32650-01.htm";
					}
					else
					{
						htmltext = "32650-00.htm";
						st.exitQuest(true);
					}
				}
				else
				{
					htmltext = "32650-09.htm";
				}
			}
		}
		else if ((cond == 1) && (npc.getId() == WARRIOR_NPC))
		{
			if (getRandom(100) < 50)
			{
				htmltext = "32651-01.htm";
				st.giveItems(EVIDENCE_OF_MIGRATION, 1);
				st.playSound("ItemSound.quest_itemget");
				npc.getAI().setIntention(CtrlIntention.MOVING, new Location(npc.getX() + 100, npc.getY() + 100, npc.getZ(), 0), 0);
				npc.getSpawn().decreaseCount(npc);
				npc.deleteMe();
				if (st.getQuestItemsCount(EVIDENCE_OF_MIGRATION) == 10)
				{
					st.set("cond", "2");
					st.playSound("ItemSound.quest_middle");
				}
			}
			else
			{
				htmltext = "";
				final Npc warrior = st.addSpawn(WARRIOR_MON, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 600000);
				warrior.setRunning();
				((Attackable) warrior).addDamageHate(player, 0, 999);
				warrior.getAI().setIntention(CtrlIntention.ATTACK, player);
				showOnScreenMsg(player, NpcStringId.THE_GRAVE_ROBBER_WARRIOR_HAS_BEEN_FILLED_WITH_DARK_ENERGY_AND_IS_ATTACKING_YOU, 5, 5000);
				if (getRandom(100) < 50)
				{
					npc.broadcastPacketToOthers(2000, new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.GRUNT_OH));
				}
				else
				{
					npc.broadcastPacketToOthers(2000, new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.GRUNT_WHATS_WRONG_WITH_ME));
				}
				npc.getSpawn().decreaseCount(npc);
				npc.deleteMe();
			}
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new _450_GraveRobberMemberRescue();
	}
}
