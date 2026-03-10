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
 * Create by LordWinter 13.06.2019
 */
public class TemporaryTeleporter extends Quest
{
	private TemporaryTeleporter()
	{
		super(-1);

		addStartNpc(32602);
		addFirstTalkId(32602);
	}

	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		if (npc.getId() == 32602)
		{
			final var template = TeleLocationParser.getInstance().getTemplate(300007);
			if (template != null)
			{
				player.teleToLocation(template.getLocation(), true, player.getReflection());
			}
		}
		player.sendActionFailed();
		return null;
	}

	public static void main(String[] args)
	{
		new TemporaryTeleporter();
	}
}