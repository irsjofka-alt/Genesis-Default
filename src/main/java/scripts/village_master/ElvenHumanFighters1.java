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
package scripts.village_master;

import gameserver.data.parser.ClassMasterParser;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.base.ClassId;
import gameserver.model.base.Race;
import gameserver.model.quest.Quest;
import gameserver.model.quest.QuestState;
import gameserver.network.serverpackets.updatetype.UserInfoType;
import gameserver.utils.Util;

/**
 * Created by LordWinter 27.12.2012
 * Based on L2J Eternity-World
 */
public class ElvenHumanFighters1 extends Quest
{
	// NPCs
	private static int[] NPCS =
	{
	        30066, 30288, 30373, 32154
	};

	// Items
	private static int MEDALLION_OF_WARRIOR = 1145;
	private static int SWORD_OF_RITUAL = 1161;
	private static int BEZIQUES_RECOMMENDATION = 1190;
	private static int ELVEN_KNIGHT_BROOCH = 1204;
	private static int REORIA_RECOMMENDATION = 1217;

	private static int[][] CLASSES =
	{
	        {
	                19, 18, 18, 19, 20, 21, ELVEN_KNIGHT_BROOCH
			},
			{
			        22, 18, 22, 23, 24, 25, REORIA_RECOMMENDATION
			},
			{
			        1, 0, 26, 27, 28, 29, MEDALLION_OF_WARRIOR
			},
			{
			        4, 0, 30, 31, 32, 33, SWORD_OF_RITUAL
			},
			{
			        7, 0, 34, 35, 36, 37, BEZIQUES_RECOMMENDATION
			}
	};

	private ElvenHumanFighters1()
	{
		super(-1);
		
		addStartNpc(NPCS);
		addTalkId(NPCS);
	}

	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return getNoQuestMsg(player);
		}

		if (Util.isDigit(event))
		{
			final int i = Integer.valueOf(event);
			final ClassId cid = player.getClassId();
			if ((cid.getRace() == Race.ELF || cid.getRace() == Race.HUMAN) && (cid.getId() == CLASSES[i][1]))
			{
				int suffix;
				final boolean item = st.hasQuestItems(CLASSES[i][6]);
				if (player.getLevel() < 20)
				{
					suffix = (!item) ? CLASSES[i][2] : CLASSES[i][3];
				}
				else
				{
					if (!item)
					{
						suffix = CLASSES[i][4];
					}
					else
					{
						suffix = CLASSES[i][5];
						st.takeItems(CLASSES[i][6], -1);
						player.setClassId(CLASSES[i][0]);
						player.setBaseClass(CLASSES[i][0]);
						st.playSound("ItemSound.quest_fanfare_2");
						final var rewards = ClassMasterParser.getInstance().getGrandMasterRewards(1);
						if (rewards != null && !rewards.isEmpty())
						{
							rewards.stream().filter(r -> r != null).forEach(it -> st.giveItems(it.getId(), it.getCountMax()));
						}
						player.broadcastCharInfo(UserInfoType.BASIC_INFO, UserInfoType.BASE_STATS, UserInfoType.MAX_HPCPMP, UserInfoType.STATS, UserInfoType.SPEED);
						st.exitQuest(false);
					}
				}
				event = npc.getId() + "-" + suffix + ".htm";
			}
		}
		return event;
	}

	@Override
	public String onTalk(Npc npc, Player player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}
		if (player.isSubClassActive())
		{
			return htmltext;
		}

		final ClassId cid = player.getClassId();

		if (cid.getRace() == Race.ELF || cid.getRace() == Race.HUMAN)
		{
			switch (cid)
			{
				case ELVEN_FIGHTER :
				{
					htmltext = npc.getId() + "-01.htm";
					break;
				}
				case FIGHTER :
				{
					htmltext = npc.getId() + "-08.htm";
					break;
				}
				default :
				{
					if (cid.level() == 1)
					{
						return npc.getId() + "-38.htm";
					}
					else if (cid.level() >= 2)
					{
						return npc.getId() + "-39.htm";
					}
				}
			}
		}
		else
		{
			htmltext = npc.getId() + "-40.htm";
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new ElvenHumanFighters1();
	}
}