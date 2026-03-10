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

import gameserver.Config;
import gameserver.ai.model.CtrlIntention;
import gameserver.model.skills.effects.Effect;
import gameserver.model.skills.effects.EffectFlag;
import gameserver.model.skills.effects.EffectTemplate;
import gameserver.model.skills.effects.EffectType;
import gameserver.model.stats.Env;

public class Relax extends Effect
{
	public Relax(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.RELAXING;
	}

	@Override
	public boolean onStart()
	{
		if (getEffected().isPlayer())
		{
			getEffected().getActingPlayer().sitDown(false);
		}
		else
		{
			getEffected().getAI().setIntention(CtrlIntention.REST);
		}
		return super.onStart();
	}
	
	@Override
	public double getMpReduce()
	{
		return Math.floor(((calc() * getEffectTemplate().getTotalTickCount()) * Config.TOGGLE_MOD_MP));
	}
	
	@Override
	public int getEffectFlags()
	{
		return EffectFlag.RELAXING.getMask();
	}
	
	@Override
	public boolean onActionTime()
	{
		if (getEffected().isDead())
		{
			return false;
		}
		
		if (getEffected().isPlayer())
		{
			if (!getEffected().getActingPlayer().isSitting())
			{
				return false;
			}
		}
		return true;
	}
}