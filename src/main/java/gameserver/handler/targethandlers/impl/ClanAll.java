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
import gameserver.model.Clan;
import gameserver.model.GameObject;
import gameserver.model.World;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Player;
import gameserver.model.skills.Skill;
import gameserver.model.skills.targets.TargetType;
import gameserver.utils.Util;

public class ClanAll implements ITargetTypeHandler
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
			final Clan clan = player.getClan();

			if (Skill.addSummon(activeChar, player, radius, false, false))
			{
				targetList.add(player.getSummon());
			}

			if (clan != null)
			{
				Player obj;
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
		else if (activeChar.isNpc())
		{
			final var npc = activeChar.getActingNpc();
			final var isSocial = npc.isMinion() || npc.hasMinions();
			if (!isSocial && npc.getFaction().isNone())
			{
				return new Creature[]
				{
				        activeChar
				};
			}

			final var minions = npc.isMinion() && npc.getLeader() != null ? npc.getLeader().getMinionList() : npc.hasMinions() ? npc.getMinionList() : null;
			targetList.add(activeChar);

			final int maxTargets = skill.getAffectLimit();
			for (final var newTarget : World.getAroundNpc(activeChar))
			{
				if (newTarget != null && newTarget.isAttackable() && (!npc.getFaction().isNone() && npc.isInFaction((newTarget)) || minions != null && minions.hasNpcId(newTarget.getId())))
				{
					if (!GeoEngine.getInstance().canSeeTarget(activeChar, newTarget) || !Util.checkIfInRange(skill.getCastRange(), activeChar, newTarget, true))
					{
						continue;
					}

					if ((maxTargets > 0) && (targetList.size() >= maxTargets))
					{
						break;
					}
					targetList.add(newTarget);
				}
			}
		}
		return targetList.toArray(new Creature[targetList.size()]);
	}

	@Override
	public Enum<TargetType> getTargetType()
	{
		return TargetType.CLAN;
	}
}