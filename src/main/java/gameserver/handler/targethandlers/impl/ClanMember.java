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
import gameserver.model.skills.Skill;
import gameserver.model.skills.targets.TargetType;
import gameserver.utils.Util;

public class ClanMember implements ITargetTypeHandler
{
	@Override
	public GameObject[] getTargetList(Skill skill, Creature activeChar, boolean onlyFirst, Creature target)
	{
		final List<Creature> targetList = new ArrayList<>();
		if (activeChar.isNpc())
		{
			final var npc = activeChar.getActingNpc();
			if (npc.getFaction().isNone())
			{
				return new Creature[]
				{
				        activeChar
				};
			}
			
			for (final var newTarget : World.getAroundNpc(activeChar))
			{
				if (newTarget != null && newTarget.isAttackable() && npc.isInFaction((newTarget)))
				{
					if (!GeoEngine.getInstance().canSeeTarget(activeChar, newTarget) || !Util.checkIfInRange(skill.getCastRange(), activeChar, newTarget, true) || newTarget.getFirstEffect(skill) != null)
					{
						continue;
					}
					targetList.add(newTarget);
					break;
				}
			}
			
			if (targetList.isEmpty())
			{
				targetList.add(npc);
			}
		}
		else
		{
			return EMPTY_TARGET_LIST;
		}
		return targetList.toArray(new Creature[targetList.size()]);
	}
	
	@Override
	public Enum<TargetType> getTargetType()
	{
		return TargetType.CLAN_MEMBER;
	}
}