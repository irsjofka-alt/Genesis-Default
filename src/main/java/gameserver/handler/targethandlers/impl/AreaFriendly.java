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
package gameserver.handler.targethandlers.impl;

import java.util.ArrayList;
import java.util.List;

import gameserver.geodata.GeoEngine;
import gameserver.handler.targethandlers.ITargetTypeHandler;
import gameserver.model.GameObject;
import gameserver.model.World;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Player;
import gameserver.model.skills.Skill;
import gameserver.model.skills.targets.TargetType;
import gameserver.network.SystemMessageId;

public class AreaFriendly implements ITargetTypeHandler
{
	@Override
	public GameObject[] getTargetList(Skill skill, Creature activeChar, boolean onlyFirst, Creature target)
	{
		final List<Creature> targetList = new ArrayList<>();
		if (!checkTarget(activeChar, target) && (skill.getCastRange() >= 0))
		{
			activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			return EMPTY_TARGET_LIST;
		}

		if (onlyFirst)
		{
			return new Creature[]
			{
			        target
			};
		}
		
		if (activeChar.getActingPlayer().isInOlympiadMode())
		{
			return new Creature[]
			{
			        activeChar
			};
		}
		targetList.add(activeChar);
		
		if (target != null)
		{
			if (target != activeChar)
			{
				targetList.add(target);
			}
			
			for (final Creature obj : World.getAroundCharacters(target, skill.getAffectRange(), 200))
			{
				if (!checkTarget(activeChar, obj) || (obj == activeChar))
				{
					continue;
				}
				
				if ((skill.getAffectLimit() > 0) && (targetList.size() >= skill.getAffectLimit()))
				{
					break;
				}
				targetList.add(obj);
			}
		}
		
		if (targetList.isEmpty())
		{
			return EMPTY_TARGET_LIST;
		}
		return targetList.toArray(new Creature[targetList.size()]);
	}
	
	private boolean checkTarget(Creature activeChar, Creature target)
	{
		if (!GeoEngine.getInstance().canSeeTarget(activeChar, target) || target == null)
		{
			return false;
		}
		
		if (target.isDead() || (!target.isPlayer() && !target.isSummon()))
		{
			return false;
		}
		
		final Player actingPlayer = activeChar.getActingPlayer();
		final Player targetPlayer = target.getActingPlayer();
		if ((actingPlayer == null) || (targetPlayer == null))
		{
			return false;
		}
		
		if (!actingPlayer.isFriend(targetPlayer))
		{
			return false;
		}
		return true;
	}
	
	@Override
	public Enum<TargetType> getTargetType()
	{
		return TargetType.AREA_FRIENDLY;
	}
}