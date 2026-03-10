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

import l2e.commons.util.Rnd;
import gameserver.ai.model.CtrlIntention;
import gameserver.model.actor.Attackable;
import gameserver.model.skills.effects.Effect;
import gameserver.model.skills.effects.EffectTemplate;
import gameserver.model.skills.effects.EffectType;
import gameserver.model.stats.Env;
import gameserver.model.stats.Formulas;

public class TargetCancel extends Effect
{
	private final int _chance, _delayMin, _delayMax;
	
	public TargetCancel(Env env, EffectTemplate template)
	{
		super(env, template);
		_chance = template.getParameters().getInteger("chance", 100);
		_delayMin = (template.getParameters().getInteger("delayMin", 2) * 1000);
		_delayMax = (template.getParameters().getInteger("delayMax", 3) * 1000);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.NONE;
	}
	
	@Override
	public boolean onStart()
	{
		final var effected = getEffected();
		if (effected == null || effected.isRaid() || effected.isLethalImmune())
		{
			return false;
		}
		
		if (Formulas.calcProbability(_chance, getEffector(), effected, getSkill(), getSkill().getId() == 821))
		{
			if (effected.isAttackable())
			{
				((Attackable) effected).getAggroList().stopHating(getEffector());
				if (_delayMin > 0 && _delayMax > 0)
				{
					((Attackable) effected).setFindTargetDelay(Rnd.get(_delayMin, _delayMax));
				}
			}
			else
			{
				if (effected.getTarget() != null)
				{
					effected.setTarget(null);
				}
			}
			
			effected.abortAttack();
			effected.abortCast();
			
			if (!effected.isAttackable() && !effected.isSummon())
			{
				effected.getAI().setIntention(CtrlIntention.IDLE);
			}
			return true;
		}
		return false;
	}
}