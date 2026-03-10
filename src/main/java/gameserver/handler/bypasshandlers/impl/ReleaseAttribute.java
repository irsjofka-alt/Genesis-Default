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
import gameserver.model.actor.Player;
import gameserver.network.serverpackets.ExShowBaseAttributeCancelWindow;

public class ReleaseAttribute implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
	        "ReleaseAttribute"
	};

	@Override
	public boolean useBypass(String command, Player activeChar, Creature target)
	{
		if (!target.isNpc())
		{
			return false;
		}
		
		activeChar.sendPacket(new ExShowBaseAttributeCancelWindow(activeChar));
		return true;
	}
	
	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}