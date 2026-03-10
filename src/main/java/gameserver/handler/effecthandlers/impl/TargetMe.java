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

import gameserver.model.actor.Playable;
import gameserver.model.actor.instance.SiegeSummonInstance;
import gameserver.model.skills.effects.Effect;
import gameserver.model.skills.effects.EffectTemplate;
import gameserver.model.skills.effects.EffectType;
import gameserver.model.stats.Env;

public class TargetMe extends Effect
{
	public TargetMe(Env env, EffectTemplate template)
	{
		super(env, template);
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
		if (effected != null)
		{
			if (effected.isPlayable())
			{
				if (effected instanceof SiegeSummonInstance)
				{
					return false;
				}
				
				if (effected.getTarget() != getEffector())
				{
					final var effector = getEffector().getActingPlayer();
					if ((effector == null) || effector.checkPvpSkill(effected, getSkill()))
					{
						effected.setTarget(getEffector());
					}
				}
				((Playable) effected).setLockedTarget(getEffector());
				return true;
			}
			else if (effected.isAttackable() && !effected.isRaid())
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void onExit()
	{
		final var effected = getEffected();
		if (effected != null && effected.isPlayable())
		{
			((Playable) effected).setLockedTarget(null);
			effected.sendActionFailed();
		}
	}
}