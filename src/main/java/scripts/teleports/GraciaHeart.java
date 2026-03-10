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

import gameserver.data.parser.TeleLocationParser;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.quest.Quest;

/**
 * Based on L2J Eternity-World
 */
public class GraciaHeart extends Quest
{
	private GraciaHeart()
	{
		super(-1);

		addStartNpc(36570);
		addTalkId(36570);
	}

	@Override
	public String onTalk(Npc npc, Player player)
	{
		String htmltext = "";
		final var st = player.getQuestState(getName());

		final int npcId = npc.getId();

		if (npcId == 36570)
		{
			final var template = TeleLocationParser.getInstance().getTemplate(300006);
			if (template != null)
			{
				if (player.getLevel() >= template.getMinLevel())
				{
					player.teleToLocation(template.getLocation(), true, player.getReflection());
				}
				else
				{
					htmltext = "36570-00.htm";
				}
			}
		}
		st.exitQuest(true);
		return htmltext;
	}

	public static void main(String[] args)
	{
		new GraciaHeart();
	}
}