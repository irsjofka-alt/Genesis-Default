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
import gameserver.model.entity.events.cleft.AerialCleftEvent;
import gameserver.network.SystemMessageId;

public class PlayerAction implements IActionHandler
{
	@Override
	public boolean action(Player activeChar, GameObject target, boolean interact, boolean shift)
	{
		for (final var e : activeChar.getFightEvents())
		{
			if (e != null && !e.canAction((Creature) target, activeChar))
			{
				return false;
			}
		}
		
		if (!AerialCleftEvent.getInstance().onAction(activeChar, target.getObjectId()))
		{
			return false;
		}
		
		if (activeChar.isOutOfControl())
		{
			return false;
		}

		if (activeChar.isLockedTarget() && (activeChar.getLockedTarget() != target))
		{
			activeChar.sendPacket(SystemMessageId.FAILED_CHANGE_TARGET);
			return false;
		}
		
		if (activeChar.getTarget() != target)
		{
			activeChar.setTarget(target);
			return false;
		}
		
		if (interact)
		{
			final var player = target.getActingPlayer();
			if (player.getPrivateStoreType() != Player.STORE_PRIVATE_NONE)
			{
				activeChar.getAI().setIntention(CtrlIntention.INTERACT, player);
			}
			else
			{
				if (target.isAutoAttackable(activeChar, false))
				{
					if ((player.isCursedWeaponEquipped() && (activeChar.getLevel() < 21)) || (activeChar.isCursedWeaponEquipped() && (player.getLevel() < 21)))
					{
						activeChar.sendActionFailed();
					}
					else
					{
						if (GeoEngine.getInstance().canSeeTarget(activeChar, player) || shift)
						{
							activeChar.getAI().setIntention(CtrlIntention.ATTACK, player, shift);
						}
						else
						{
							activeChar.sendActionFailed();
							activeChar.getAI().setIntention(CtrlIntention.MOVING, player.getLocation(), 0);
						}
						activeChar.onActionRequest();
					}
				}
				else
				{
					activeChar.sendActionFailed();
					activeChar.getAI().setIntention(CtrlIntention.FOLLOW, player);
				}
			}
		}
		return true;
	}
	
	@Override
	public InstanceType getInstanceType()
	{
		return InstanceType.Player;
	}
}