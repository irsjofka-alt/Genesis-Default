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
package gameserver.handler.actionshifthandlers.impl;

import gameserver.handler.actionhandlers.IActionHandler;
import gameserver.handler.admincommandhandlers.AdminCommandHandler;
import gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import gameserver.model.GameObject;
import gameserver.model.GameObject.InstanceType;
import gameserver.model.actor.Player;

public class SummonActionShift implements IActionHandler
{
	@Override
	public boolean action(Player activeChar, GameObject target, boolean interact, boolean shift)
	{
		if (activeChar.getTarget() != target)
		{
			activeChar.setTarget(target);
		}
		
		if (activeChar.getAccessLevel().allowSummonActionShift())
		{
			
			final IAdminCommandHandler ach = AdminCommandHandler.getInstance().getHandler("admin_summon_info");
			if (ach != null)
			{
				ach.useAdminCommand("admin_summon_info", activeChar);
			}
		}
		return true;
	}
	
	@Override
	public InstanceType getInstanceType()
	{
		return InstanceType.Summon;
	}
}