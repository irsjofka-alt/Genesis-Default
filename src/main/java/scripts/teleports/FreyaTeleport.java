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
import gameserver.model.strings.server.ServerStorage;
import scripts.ai.AbstractNpcAI;

/**
 * Based on L2J Eternity-World
 */
public class FreyaTeleport extends AbstractNpcAI
{
	private FreyaTeleport()
	{
		addStartNpc(32734);
		addTalkId(32734);
	}

	@Override
	public String onTalk(Npc npc, Player player)
	{
		final String htmltext = "";
		
		switch (npc.getId())
		{
			case 32734 :
			{
				final var template = TeleLocationParser.getInstance().getTemplate(300005);
				if (template != null)
				{
					if (player.getLevel() < template.getMinLevel())
					{
						return "<html><body>" + ServerStorage.getInstance().getString(player.getLang(), "SeedOfAnnihilation.CANT_TELE") + "</body></html>";
					}
					else
					{
						player.teleToLocation(template.getLocation(), true, player.getReflection());
					}
				}
				break;
			}
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new FreyaTeleport();
	}
}
