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

import gameserver.handler.targethandlers.ITargetTypeHandler;
import gameserver.model.GameObject;
import gameserver.model.World;
import gameserver.model.actor.Creature;
import gameserver.model.skills.Skill;
import gameserver.model.skills.targets.TargetType;
import gameserver.model.zone.ZoneId;
import gameserver.network.SystemMessageId;
import gameserver.utils.Util;

public class Area implements ITargetTypeHandler
{
	@Override
	public GameObject[] getTargetList(Skill skill, Creature activeChar, boolean onlyFirst, Creature target)
	{
		final List<Creature> targetList = new ArrayList<>();
		if ((target == null) || (((target == activeChar) || target.isAlikeDead()) && (skill.getCastRange() >= 0)) || (!(target.isAttackable() || target.isPlayable())))
		{
			activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			return EMPTY_TARGET_LIST;
		}

		final Creature origin;
		final boolean srcInArena = (activeChar.isInsideZone(ZoneId.PVP) && !activeChar.isInsideZone(ZoneId.SIEGE));

		if (skill.getCastRange() >= 0)
		{
			if (!Skill.checkForAreaOffensiveSkills(activeChar, target, skill, srcInArena, activeChar))
			{
				return EMPTY_TARGET_LIST;
			}

			if (onlyFirst)
			{
				return new Creature[]
				{
				        target
				};
			}

			origin = target;
			targetList.add(origin);
		}
		else
		{
			origin = activeChar;
		}
		
		activeChar.setHeading(Util.calculateHeadingFrom(activeChar, target));

		final int maxTargets = skill.getAffectLimit();
		for (final var obj : World.getAroundCharacters(activeChar))
		{
			if (!(obj.isAttackable() || obj.isPlayable()))
			{
				continue;
			}

			if (obj == origin || obj.isDead())
			{
				continue;
			}
			
			if (skill.checkNormalRange(activeChar, origin, obj, srcInArena, target))
			{
				if ((maxTargets > 0) && (targetList.size() >= maxTargets))
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

	@Override
	public Enum<TargetType> getTargetType()
	{
		return TargetType.AREA;
	}
}