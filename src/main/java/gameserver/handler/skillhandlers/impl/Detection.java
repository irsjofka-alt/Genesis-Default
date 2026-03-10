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
package gameserver.handler.skillhandlers.impl;

import gameserver.handler.skillhandlers.ISkillHandler;
import gameserver.model.GameObject;
import gameserver.model.World;
import gameserver.model.actor.Creature;
import gameserver.model.skills.Skill;
import gameserver.model.skills.SkillType;
import gameserver.model.skills.effects.EffectType;

public class Detection implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS =
	{
	        SkillType.DETECTION
	};

	@Override
	public void useSkill(Creature activeChar, Skill skill, GameObject[] targets, double cubicPower)
	{
		final boolean hasParty;
		final boolean hasClan;
		final boolean hasAlly;
		final var player = activeChar.getActingPlayer();
		if (player != null)
		{
			hasParty = player.isInParty();
			hasClan = player.getClanId() > 0;
			hasAlly = player.getAllyId() > 0;
			
			for (final var target : World.getAroundPlayers(activeChar, skill.getAffectRange(), 200))
			{
				if ((target != null) && target.isInvisible())
				{
					if (hasParty && (target.getParty() != null) && (player.getParty().getLeaderObjectId() == target.getParty().getLeaderObjectId()))
					{
						continue;
					}
					if (hasClan && (player.getClanId() == target.getClanId()))
					{
						continue;
					}
					if (hasAlly && (player.getAllyId() == target.getAllyId()))
					{
						continue;
					}
					
					final var eHide = target.getFirstEffect(EffectType.HIDE);
					if (eHide != null)
					{
						eHide.exit(true);
					}
				}
			}
		}
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}