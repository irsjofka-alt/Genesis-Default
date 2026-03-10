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
import gameserver.model.actor.instance.NpcInstance;
import gameserver.network.SystemMessageId;

public class SkillList implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
	        "SkillList", "CustomSkillList"
	};
	
	@Override
	public boolean useBypass(String command, Player activeChar, Creature target)
	{
		if (!(target instanceof NpcInstance))
		{
			return false;
		}
		
		if ((activeChar.getWeightPenalty() >= 3) || !activeChar.isInventoryUnder90(true))
		{
			activeChar.sendPacket(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT);
			return false;
		}

		if (command.startsWith("CustomSkillList"))
		{
			final String id = command.substring(15).trim();
			if (id.length() != 0)
			{
				try
				{
					NpcInstance.showCustomSkillList(activeChar, Integer.parseInt(id));
				}
				catch (final Exception e)
				{
					_log.warn("Exception in " + getClass().getSimpleName(), e);
				}
			}
		}
		else if (command.startsWith("SkillList"))
		{
			NpcInstance.showSkillList(activeChar, (Npc) target, activeChar.getClassId());
		}
		return true;
	}

	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}