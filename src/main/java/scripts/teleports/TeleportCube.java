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

import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.quest.Quest;
import gameserver.model.quest.QuestState;

/**
 * Based on L2J Eternity-World
 */
public class TeleportCube extends Quest
{
	private TeleportCube()
	{
		super(-1);

		addStartNpc(32107);
		addTalkId(32107);
	}

	@Override
	public String onTalk(Npc npc, Player player)
	{
		final String htmltext = "";
		final QuestState st = player.getQuestState(getName());
		final int npcId = npc.getId();

		if (npcId == 32107)
		{
			player.teleToLocation(10468, -24569, -3650, true, player.getReflection());
			return null;
		}
		st.exitQuest(true);
		return htmltext;
	}

	public static void main(String[] args)
	{
		new TeleportCube();
	}
}