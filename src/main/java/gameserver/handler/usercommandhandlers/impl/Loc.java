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
package gameserver.handler.usercommandhandlers.impl;

import gameserver.handler.usercommandhandlers.IUserCommandHandler;
import gameserver.instancemanager.MapRegionManager;
import gameserver.instancemanager.ZoneManager;
import gameserver.model.actor.Player;
import gameserver.model.base.Race;
import gameserver.model.zone.type.RespawnZone;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.SystemMessage;

public class Loc implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
	        0
	};

	@Override
	public boolean useUserCommand(int id, Player activeChar)
	{
		int region;
		final RespawnZone zone = ZoneManager.getInstance().getZone(activeChar, RespawnZone.class);
		
		if (zone != null)
		{
			region = MapRegionManager.getInstance().getRestartRegion(activeChar, zone.getAllRespawnPoints().get(Race.HUMAN)).getLocId();
		}
		else
		{
			region = MapRegionManager.getInstance().getMapRegionLocId(activeChar);
		}
		
		SystemMessage sm;
		if (region > 0)
		{
			sm = SystemMessage.getSystemMessage(region);
			if (sm.getSystemMessageId().getParamCount() == 3)
			{
				sm.addNumber(activeChar.getX());
				sm.addNumber(activeChar.getY());
				sm.addNumber(activeChar.getZ());
			}
		}
		else
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.CURRENT_LOCATION_S1);
			sm.addString(activeChar.getX() + ", " + activeChar.getY() + ", " + activeChar.getZ());
		}
		activeChar.sendPacket(sm);
		return true;
	}
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}