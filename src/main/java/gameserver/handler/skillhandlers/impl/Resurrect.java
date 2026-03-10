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

import java.util.ArrayList;
import java.util.List;

import gameserver.handler.skillhandlers.ISkillHandler;
import gameserver.model.GameObject;
import gameserver.model.ShotType;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Player;
import gameserver.model.actor.instance.PetInstance;
import gameserver.model.entity.events.AbstractFightEvent;
import gameserver.model.skills.Skill;
import gameserver.model.skills.SkillType;
import gameserver.model.skills.targets.TargetType;
import gameserver.model.stats.Formulas;
import gameserver.taskmanager.DecayTaskManager;

public class Resurrect implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS =
	{
	        SkillType.RESURRECT
	};

	@Override
	public void useSkill(Creature activeChar, Skill skill, GameObject[] targets, double cubicPower)
	{
		Player player = null;
		if (activeChar.isPlayer())
		{
			player = activeChar.getActingPlayer();
		}
		
		Player targetPlayer;
		final List<Creature> targetToRes = new ArrayList<>();
		for (final Creature target : (Creature[]) targets)
		{
			if (target.isPlayer())
			{
				targetPlayer = target.getActingPlayer();
				
				if (skill.getTargetType() == TargetType.CORPSE_CLAN)
				{
					if ((player != null) && (player.getClanId() != targetPlayer.getClanId()))
					{
						continue;
					}
				}
				
				if (skill.getTargetType() == TargetType.CORPSE_ALLY)
				{
					if (player != null && (player.getAllyId() == 0 || player.getAllyId() != targetPlayer.getAllyId()))
					{
						continue;
					}
				}
				
				for (final AbstractFightEvent e : player.getFightEvents())
				{
					if (!e.canRessurect(player, targetPlayer))
					{
						continue;
					}
				}
			}
			if (target.isVisible())
			{
				targetToRes.add(target);
			}
		}
		
		if (targetToRes.isEmpty())
		{
			activeChar.abortCast();
			return;
		}
		
		for (final Creature cha : targetToRes)
		{
			if (activeChar.isPlayer())
			{
				if (cha.isPlayer())
				{
					cha.getActingPlayer().reviveRequest(activeChar.getActingPlayer(), skill, skill.getResurrectTime(), false, activeChar.getFarmSystem().isAutofarming());
				}
				else if (cha.isPet())
				{
					((PetInstance) cha).getOwner().reviveRequest(activeChar.getActingPlayer(), skill, skill.getResurrectTime(), true, activeChar.getFarmSystem().isAutofarming());
				}
			}
			else
			{
				DecayTaskManager.getInstance().cancelDecayTask(cha);
				cha.doRevive(Formulas.calculateSkillResurrectRestorePercent(skill.getPower(), activeChar));
			}
		}
		activeChar.setChargedShot(activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOTS) ? ShotType.BLESSED_SPIRITSHOTS : ShotType.SPIRITSHOTS, false);
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}