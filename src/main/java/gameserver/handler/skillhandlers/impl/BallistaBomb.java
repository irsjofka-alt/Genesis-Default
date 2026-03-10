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

import l2e.commons.util.Rnd;
import gameserver.handler.skillhandlers.ISkillHandler;
import gameserver.model.GameObject;
import gameserver.model.actor.Creature;
import gameserver.model.actor.instance.FortBallistaInstance;
import gameserver.model.skills.Skill;
import gameserver.model.skills.SkillType;

public class BallistaBomb implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS =
	{
	        SkillType.BALLISTA
	};
	
	@Override
	public void useSkill(Creature activeChar, Skill skill, GameObject[] targets, double cubicPower)
	{
		if (!activeChar.isPlayer())
		{
			return;
		}
		
		final GameObject[] targetList = skill.getTargetList(activeChar);

		if ((targetList == null) || (targetList.length == 0))
		{
			return;
		}
		final Creature target = (Creature) targetList[0];
		if (target instanceof FortBallistaInstance)
		{
			if (Rnd.get(3) == 0)
			{
				target.setIsInvul(false);
				target.reduceCurrentHp(target.getMaxHp() + 1, activeChar, skill);
			}
		}
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}