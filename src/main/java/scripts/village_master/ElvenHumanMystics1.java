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
 * Created by LordWinter 28.12.2012
 * Based on L2J Eternity-World
 */
public class ElvenHumanMystics1 extends Quest
{
	// NPCs
	private static int[] NPCS =
	{
	        30070, 30289, 30037, 32153, 32147
	};

	// Items
	private static int MARK_OF_FAITH = 1201;
	private static int ETERNITY_DIAMOND = 1230;
	private static int LEAF_OF_ORACLE = 1235;
	private static int BEAD_OF_SEASON = 1292;

	private static int[][] CLASSES =
	{
	        {
	                26, 25, 15, 16, 17, 18, ETERNITY_DIAMOND
			},
			{
			        29, 25, 19, 20, 21, 22, LEAF_OF_ORACLE
			},
			{
			        11, 10, 23, 24, 25, 26, BEAD_OF_SEASON
			},
			{
			        15, 10, 27, 28, 29, 30, MARK_OF_FAITH
			}
	};

	private ElvenHumanMystics1()
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
						final var rewards = ClassMasterParser.getInstance().getGrandMasterRewards(1);
						if (rewards != null && !rewards.isEmpty())
						{
							rewards.stream().filter(r -> r != null).forEach(it -> st.giveItems(it.getId(), it.getCountMax()));
						}
						st.takeItems(CLASSES[i][6], -1);
						player.setClassId(CLASSES[i][0]);
						player.setBaseClass(CLASSES[i][0]);
						st.playSound("ItemSound.quest_fanfare_2");
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
				case ELVEN_MAGE :
				{
					htmltext = npc.getId() + "-01.htm";
					break;
				}
				case MAGE :
				{
					htmltext = npc.getId() + "-08.htm";
					break;
				}
				default :
				{
					if (cid.level() == 1)
					{
						return npc.getId() + "-31.htm";
					}
					else if (cid.level() >= 2)
					{
						return npc.getId() + "-32.htm";
					}
				}
			}
		}
		else
		{
			htmltext = npc.getId() + "-33.htm";
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new ElvenHumanMystics1();
	}
}