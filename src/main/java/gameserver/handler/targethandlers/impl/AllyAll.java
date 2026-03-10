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

import gameserver.data.holder.ClanHolder;
import gameserver.geodata.GeoEngine;
import gameserver.handler.targethandlers.ITargetTypeHandler;
import gameserver.model.GameObject;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Player;
import gameserver.model.skills.Skill;
import gameserver.model.skills.targets.TargetType;

public class AllyAll implements ITargetTypeHandler
{
	@Override
	public GameObject[] getTargetList(Skill skill, Creature activeChar, boolean onlyFirst, Creature target)
	{
		final List<Creature> targetList = new ArrayList<>();
		if (activeChar.isPlayable())
		{
			final var player = activeChar.getActingPlayer();
			if (player == null)
			{
				return EMPTY_TARGET_LIST;
			}
			
			if (player.isInOlympiadMode())
			{
				return new Creature[]
				{
				        player
				};
			}
			
			if (onlyFirst)
			{
				return new Creature[]
				{
				        player
				};
			}
			
			targetList.add(player);
			
			final int radius = skill.getAffectRange();
			final var allyId = player.getAllyId();
			
			if (Skill.addSummon(activeChar, player, radius, false, false))
			{
				targetList.add(player.getSummon());
			}
			
			if (allyId > 0)
			{
				Player obj;
				for (final var clan : ClanHolder.getInstance().getClanAllies(allyId))
				{
					for (final var member : clan.getMembers())
					{
						obj = member.getPlayerInstance();
						
						if ((obj == null) || (obj == player))
						{
							continue;
						}
						
						if (player.isInDuel())
						{
							if (player.getDuelId() != obj.getDuelId())
							{
								continue;
							}
							if (player.isInParty() && obj.isInParty() && (player.getParty().getLeaderObjectId() != obj.getParty().getLeaderObjectId()))
							{
								continue;
							}
						}
						
						if (!GeoEngine.getInstance().canSeeTarget(player, obj) || !player.checkPvpSkill(obj, skill))
						{
							continue;
						}
						
						for (final var e : player.getFightEvents())
						{
							if (e != null && !e.canUseMagic(player, obj, skill))
							{
								continue;
							}
						}
						
						if (!onlyFirst && Skill.addSummon(activeChar, obj, radius, false, false))
						{
							targetList.add(obj.getSummon());
						}
						
						if (!Skill.addCharacter(activeChar, obj, radius, false))
						{
							continue;
						}
						
						if (onlyFirst)
						{
							return new Creature[]
							{
							        obj
							};
						}
						targetList.add(obj);
					}
				}
			}
		}
		return targetList.toArray(new Creature[targetList.size()]);
	}
	
	@Override
	public Enum<TargetType> getTargetType()
	{
		return TargetType.ALLY_MEMBER;
	}
}