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
package gameserver.handler.effecthandlers.impl;

import gameserver.model.actor.Creature;
import gameserver.model.skills.effects.Effect;
import gameserver.model.skills.effects.EffectTemplate;
import gameserver.model.skills.effects.EffectType;
import gameserver.model.stats.Env;

public class MpConsumePerLevel extends Effect
{
	public MpConsumePerLevel(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public EffectType getEffectType()
	{
		return EffectType.MP_CONSUME_PER_LEVEL;
	}

	@Override
	public double getMpReduce()
	{
		final Creature target = getEffected();
		if (target == null || target.isDead())
		{
			return 0;
		}
		return Math.floor((calc() * getEffectTemplate().getTotalTickCount()) * (target.getLevel() * 0.062));
	}
	
	@Override
	public boolean onActionTime()
	{
		final Creature target = getEffected();
		if (target == null)
		{
			return false;
		}
		
		if (target.isDead())
		{
			return false;
		}
		return getSkill().isToggle();
	}
}