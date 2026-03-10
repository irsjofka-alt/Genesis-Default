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
import gameserver.model.actor.Creature;
import gameserver.model.actor.Player;
import gameserver.model.actor.Summon;
import gameserver.model.skills.Skill;
import gameserver.model.skills.targets.TargetType;
import gameserver.utils.Util;

public class PartyNotMe implements ITargetTypeHandler
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
		
		Player player = null;

		if (activeChar.isSummon())
		{
			player = ((Summon) activeChar).getOwner();
			targetList.add(player);
		}
		else if (activeChar.isPlayer())
		{
			player = activeChar.getActingPlayer();
			if (activeChar.getSummon() != null)
			{
				targetList.add(activeChar.getSummon());
			}
		}

		if (activeChar.getParty() != null)
		{
			final List<Player> partyList = activeChar.getParty().getMembers();

			for (final Player partyMember : partyList)
			{
				if (partyMember == null)
				{
					continue;
				}
				else if (partyMember == player)
				{
					continue;
				}
				else if (!partyMember.isDead() && Util.checkIfInRange(skill.getAffectRange(), activeChar, partyMember, true))
				{
					targetList.add(partyMember);

					if ((partyMember.getSummon() != null) && !partyMember.getSummon().isDead())
					{
						targetList.add(partyMember.getSummon());
					}
				}
			}
		}
		return targetList.toArray(new Creature[targetList.size()]);
	}

	@Override
	public Enum<TargetType> getTargetType()
	{
		return TargetType.PARTY_NOTME;
	}
}