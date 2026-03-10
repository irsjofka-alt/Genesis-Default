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
import gameserver.model.actor.Creature;
import gameserver.model.skills.Skill;
import gameserver.model.skills.targets.TargetType;

public class Party implements ITargetTypeHandler
{
	@Override
	public GameObject[] getTargetList(Skill skill, Creature activeChar, boolean onlyFirst, Creature target)
	{
		final List<Creature> targetList = new ArrayList<>();
		if (onlyFirst)
		{
			return new Creature[]
			{
			        activeChar
			};
		}

		targetList.add(activeChar);
		
		final int radius = skill.getAffectRange();
		
		final var player = activeChar.getActingPlayer();
		if (activeChar.isSummon())
		{
			if (Skill.addCharacter(activeChar, player, radius, false))
			{
				targetList.add(player);
			}
		}
		else if (activeChar.isPlayer())
		{
			if (Skill.addSummon(activeChar, player, radius, false, false))
			{
				targetList.add(player.getSummon());
			}
		}

		if (activeChar.isInParty())
		{
			for (final var partyMember : activeChar.getParty().getMembers())
			{
				if ((partyMember == null) || (partyMember == player) || !GeoEngine.getInstance().canSeeTarget(player, partyMember))
				{
					continue;
				}
				
				if (Skill.addCharacter(activeChar, partyMember, radius, false))
				{
					targetList.add(partyMember);
				}
				
				if (Skill.addSummon(activeChar, partyMember, radius, false, false))
				{
					targetList.add(partyMember.getSummon());
				}
			}
			
			if (skill.isPartyBuff())
			{
				player.getParty().addPartyEffect(skill, player);
			}
		}
		return targetList.toArray(new Creature[targetList.size()]);
	}
	
	@Override
	public Enum<TargetType> getTargetType()
	{
		return TargetType.PARTY;
	}
}