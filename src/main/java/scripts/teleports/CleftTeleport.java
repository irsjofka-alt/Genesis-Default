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
import gameserver.model.skills.Skill;
import scripts.ai.AbstractNpcAI;

/**
 * Based on L2J Eternity-World
 */
public class CleftTeleport extends AbstractNpcAI
{
	private CleftTeleport()
	{
		addStartNpc(36570);
		addTalkId(36570);
	}

	@Override
	public String onTalk(Npc npc, Player player)
	{
		String htmltext = "";
		switch (npc.getId())
		{
			case 36570 :
			{
				final var template = TeleLocationParser.getInstance().getTemplate(300001);
				if (template != null)
				{
					if (canTeleport(player))
					{
						player.teleToLocation(template.getLocation(), true, player.getReflection());
					}
					else
					{
						htmltext = "36570-1.htm";
					}
				}
				break;
			}
		}
		return htmltext;
	}
	
	private static boolean canTeleport(Player player)
	{
		boolean checkReg = false;
		for (final Skill s : player.getAllSkills())
		{
			if (s == null)
			{
				continue;
			}

			if (s.getId() == 840 || s.getId() == 841 || s.getId() == 842)
			{
				checkReg = true;
			}
		}
		return checkReg;
	}
	
	public static void main(String[] args)
	{
		new CleftTeleport();
	}
}