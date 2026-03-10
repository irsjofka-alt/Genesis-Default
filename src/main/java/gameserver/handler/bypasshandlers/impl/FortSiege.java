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
import gameserver.model.Clan;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.actor.instance.FortSiegeNpcInstance;
import gameserver.network.serverpackets.CastleSiegeInfo;

public class FortSiege implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
	        "fort_register", "fort_unregister"
	};
	
	@Override
	public boolean useBypass(String command, Player activeChar, Creature target)
	{
		if (!(target instanceof FortSiegeNpcInstance))
		{
			return false;
		}
		
		final var npc = (Npc) target;
		if ((activeChar.getClanId() > 0) && ((activeChar.getClanPrivileges() & Clan.CP_CS_MANAGE_SIEGE) == Clan.CP_CS_MANAGE_SIEGE))
		{
			if (command.toLowerCase().startsWith(COMMANDS[0]))
			{
				activeChar.sendPacket(new CastleSiegeInfo(npc.getFort()));
			}
			else if (command.toLowerCase().startsWith(COMMANDS[1]))
			{
				npc.getFort().getSiege().removeSiegeClan(activeChar.getClan());
				npc.showChatWindow(activeChar, 8);
				return true;
			}
			return true;
		}
		npc.showChatWindow(activeChar, 10);
		return true;
	}

	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}