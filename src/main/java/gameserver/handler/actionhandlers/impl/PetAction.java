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
package gameserver.handler.actionhandlers.impl;

import gameserver.ai.model.CtrlIntention;
import gameserver.geodata.GeoEngine;
import gameserver.handler.actionhandlers.IActionHandler;
import gameserver.model.GameObject;
import gameserver.model.GameObject.InstanceType;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Player;
import gameserver.model.actor.instance.PetInstance;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.PetStatusShow;

public class PetAction implements IActionHandler
{
	@Override
	public boolean action(Player activeChar, GameObject target, boolean interact, boolean shift)
	{
		if (activeChar.isLockedTarget() && (activeChar.getLockedTarget() != target))
		{
			activeChar.sendPacket(SystemMessageId.FAILED_CHANGE_TARGET);
			return false;
		}

		final boolean isOwner = activeChar.getObjectId() == ((PetInstance) target).getOwner().getObjectId();
		
		if (isOwner && (activeChar != ((PetInstance) target).getOwner()))
		{
			((PetInstance) target).updateRefOwner(activeChar);
		}
		if (activeChar.getTarget() != target)
		{
			activeChar.setTarget(target);
		}
		else if (interact)
		{
			if (target.isAutoAttackable(activeChar, false) && !isOwner)
			{
				if (GeoEngine.getInstance().canSeeTarget(activeChar, target) || shift)
				{
					activeChar.getAI().setIntention(CtrlIntention.ATTACK, target, shift);
					activeChar.onActionRequest();
				}
			}
			else if (!((Creature) target).isInsideRadius(activeChar, 150, false, false))
			{
				if (GeoEngine.getInstance().canSeeTarget(activeChar, target))
				{
					activeChar.getAI().setIntention(CtrlIntention.INTERACT, target);
					activeChar.onActionRequest();
				}
			}
			else
			{
				if (isOwner)
				{
					activeChar.sendPacket(new PetStatusShow((PetInstance) target));
				}
				activeChar.updateNotMoveUntil();
			}
		}
		return true;
	}
	
	@Override
	public InstanceType getInstanceType()
	{
		return InstanceType.PetInstance;
	}
}