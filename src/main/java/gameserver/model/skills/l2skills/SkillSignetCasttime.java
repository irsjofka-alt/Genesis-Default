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
package gameserver.model.skills.l2skills;

import gameserver.model.GameObject;
import gameserver.model.actor.Creature;
import gameserver.model.skills.Skill;
import gameserver.model.stats.StatsSet;

public final class SkillSignetCasttime extends Skill
{
	public SkillSignetCasttime(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(Creature caster, GameObject[] targets, double cubicPower)
	{
		if (caster.isAlikeDead())
		{
			return;
		}

		getEffectsSelf(caster, true);
	}
}