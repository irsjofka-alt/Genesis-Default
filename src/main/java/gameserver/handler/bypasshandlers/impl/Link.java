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
package gameserver.handler.bypasshandlers.impl;

import gameserver.handler.bypasshandlers.IBypassHandler;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.network.serverpackets.NpcHtmlMessage;

public class Link implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
	        "Link"
	};
	
	@Override
	public boolean useBypass(String command, Player activeChar, Creature target)
	{
		if (!target.isNpc())
		{
			return false;
		}

		try
		{
			final String path = command.substring(5).trim();
			if (path.indexOf("..") != -1)
			{
				return false;
			}
			final String filename = "data/html/" + path;
			final NpcHtmlMessage html = new NpcHtmlMessage(((Npc) target).getObjectId());
			html.setFile(activeChar, activeChar.getLang(), filename);
			html.replace("%objectId%", String.valueOf(((Npc) target).getObjectId()));
			activeChar.sendPacket(html);
			return true;
		}
		catch (final Exception e)
		{
			_log.warn("Exception in " + getClass().getSimpleName(), e);
		}
		return false;
	}

	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}