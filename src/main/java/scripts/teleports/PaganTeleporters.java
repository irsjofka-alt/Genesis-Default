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
package scripts.teleports;

import gameserver.data.parser.DoorParser;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.quest.Quest;
import gameserver.model.quest.QuestState;

public class PaganTeleporters extends Quest
{
	private PaganTeleporters()
	{
		super(-1);
		
		addStartNpc(32034, 32035, 32036, 32037, 32039, 32040);
		addTalkId(32034, 32035, 32036, 32037, 32039, 32040);
		
		addFirstTalkId(32034, 32039, 32040);
	}

	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (event.equalsIgnoreCase("Close_Door1"))
		{
			DoorParser.getInstance().getDoor(19160001).closeMe();
		}
		else if (event.equalsIgnoreCase("Close_Door2"))
		{
			DoorParser.getInstance().getDoor(19160010).closeMe();
			DoorParser.getInstance().getDoor(19160011).closeMe();
		}

		return "";
	}

	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		switch (npc.getId())
		{
			case 32034 :
				if (player.destroyItemByItemId("Mark", 8064, 1, player, false))
				{
					player.addItem("Mark", 8065, 1, player, true);
				}
				npc.showChatWindow(player);
				return null;
			case 32039 :
				player.teleToLocation(36640, -51218, 718, true, player.getReflection());
				break;
			case 32040 :
				player.teleToLocation(-12766, -35840, -10856, true, player.getReflection());
				break;
		}
		return "";
	}

	@Override
	public String onTalk(Npc npc, Player player)
	{
		String htmltext = "";
		final QuestState st = player.getQuestState(getName());

		if (st == null)
		{
			return null;
		}

		switch (npc.getId())
		{
			case 32034:
				if (!st.hasQuestItems(8064) && !st.hasQuestItems(8065) && !st.hasQuestItems(8067))
				{
					htmltext = "noItem.htm";
				}
				else
				{
					htmltext = "FadedMark.htm";
					DoorParser.getInstance().getDoor(19160001).openMe();
					startQuestTimer("Close_Door1", 10000, null, null);
				}
				break;
			case 32035:
				DoorParser.getInstance().getDoor(19160001).openMe();
				startQuestTimer("Close_Door1", 10000, null, null);
				htmltext = "FadedMark.htm";
				break;
			case 32036:
				if (!st.hasQuestItems(8067))
				{
					htmltext = "noMark.htm";
				}
				else
				{
					htmltext = "openDoor.htm";
					startQuestTimer("Close_Door2", 10000, null, null);
					DoorParser.getInstance().getDoor(19160010).openMe();
					DoorParser.getInstance().getDoor(19160011).openMe();
				}
				break;
			case 32037:
				DoorParser.getInstance().getDoor(19160010).openMe();
				DoorParser.getInstance().getDoor(19160011).openMe();
				startQuestTimer("Close_Door2", 10000, null, null);
				htmltext = "FadedMark.htm";
				break;
		}
		st.exitQuest(true);
		return htmltext;
	}

	public static void main(String[] args)
	{
		new PaganTeleporters();
	}
}