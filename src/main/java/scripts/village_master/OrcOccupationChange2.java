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
public class OrcOccupationChange2 extends Quest
{
	// NPCs
	private static int[] NPCS =
	{
	        30513, 30681, 30704, 30865, 30913, 31288, 31326, 31977
	};

	// Items
	private static int MARK_OF_CHALLENGER = 2627;
	private static int MARK_OF_PILGRIM = 2721;
	private static int MARK_OF_DUELIST = 2762;
	private static int MARK_OF_WARSPIRIT = 2879;
	private static int MARK_OF_GLORY = 3203;
	private static int MARK_OF_CHAMPION = 3276;
	private static int MARK_OF_LORD = 3390;

	private static int[][] CLASSES =
	{
	        {
	                48, 47, 16, 17, 18, 19, MARK_OF_CHALLENGER, MARK_OF_GLORY, MARK_OF_DUELIST
			},
			{
			        46, 45, 20, 21, 22, 23, MARK_OF_CHALLENGER, MARK_OF_GLORY, MARK_OF_CHAMPION
			},
			{
			        51, 50, 24, 25, 26, 27, MARK_OF_PILGRIM, MARK_OF_GLORY, MARK_OF_LORD
			},
			{
			        52, 50, 28, 29, 30, 31, MARK_OF_PILGRIM, MARK_OF_GLORY, MARK_OF_WARSPIRIT
			}
	};

	private OrcOccupationChange2()
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
			if ((cid.getRace() == Race.ORC) && (cid.getId() == CLASSES[i][1]))
			{
				int suffix;
				final boolean item1 = st.hasQuestItems(CLASSES[i][6]);
				final boolean item2 = st.hasQuestItems(CLASSES[i][7]);
				final boolean item3 = st.hasQuestItems(CLASSES[i][8]);
				if (player.getLevel() < 40)
				{
					suffix = (!item1 || !item2 || !item3) ? CLASSES[i][2] : CLASSES[i][3];
				}
				else
				{
					if (!item1 || !item2 || !item3)
					{
						suffix = CLASSES[i][4];
					}
					else
					{
						suffix = CLASSES[i][5];
						st.takeItems(CLASSES[i][6], -1);
						st.takeItems(CLASSES[i][7], -1);
						st.takeItems(CLASSES[i][8], -1);
						st.playSound("ItemSound.quest_fanfare_2");
						final var rewards = ClassMasterParser.getInstance().getGrandMasterRewards(2);
						if (rewards != null && !rewards.isEmpty())
						{
							rewards.stream().filter(r -> r != null).forEach(it -> st.giveItems(it.getId(), it.getCountMax()));
						}
						player.setClassId(CLASSES[i][0]);
						player.setBaseClass(CLASSES[i][0]);
						player.broadcastCharInfo(UserInfoType.BASIC_INFO, UserInfoType.BASE_STATS, UserInfoType.MAX_HPCPMP, UserInfoType.STATS, UserInfoType.SPEED);
						st.exitQuest(false);
					}
				}
				event = "30513-" + suffix + ".htm";
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
		if (cid.getRace() == Race.ORC)
		{
			switch (cid)
			{
				case ORC_MONK :
				{
					htmltext = "30513-01.htm";
					break;
				}
				case ORC_RAIDER :
				{
					htmltext = "30513-05.htm";
					break;
				}
				case ORC_SHAMAN :
				{
					htmltext = "30513-09.htm";
					break;
				}
				default :
				{
					if (cid.level() == 0)
					{
						htmltext = "30513-33.htm";
					}
					else if (cid.level() >= 2)
					{
						htmltext = "30513-32.htm";
					}
					else
					{
						htmltext = "30513-34.htm";
					}
				}
			}
		}
		else
		{
			htmltext = "30513-34.htm";
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new OrcOccupationChange2();
	}
}