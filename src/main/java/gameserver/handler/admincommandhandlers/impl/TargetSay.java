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
package gameserver.handler.admincommandhandlers.impl;

import gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import gameserver.model.GameObject;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Player;
import gameserver.model.actor.instance.StaticObjectInstance;
import gameserver.network.SystemMessageId;
import gameserver.network.clientpackets.Say2;
import gameserver.network.serverpackets.CreatureSay;

public class TargetSay implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
	        "admin_targetsay"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.startsWith("admin_targetsay"))
		{
			try
			{
				final GameObject obj = activeChar.getTarget();
				if ((obj instanceof StaticObjectInstance) || !(obj instanceof Creature))
				{
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
					return false;
				}

				final String message = command.substring(16);
				final Creature target = (Creature) obj;
				target.broadcastPacket(new CreatureSay(target.getObjectId(), (target.isPlayer() ? Say2.ALL : Say2.NPC_ALL), target.getName(activeChar.getLang()), message));
			}
			catch (final StringIndexOutOfBoundsException _)
			{
				activeChar.sendMessage("Usage: //targetsay <text>");
				return false;
			}
		}
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}