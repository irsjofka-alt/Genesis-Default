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

import gameserver.ai.character.SummonAI;
import gameserver.ai.model.CtrlEvent;
import gameserver.ai.model.CtrlIntention;
import gameserver.model.skills.effects.AbnormalEffect;
import gameserver.model.skills.effects.Effect;
import gameserver.model.skills.effects.EffectFlag;
import gameserver.model.skills.effects.EffectTemplate;
import gameserver.model.skills.effects.EffectType;
import gameserver.model.stats.Env;

public class Paralyze extends Effect
{
	public Paralyze(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	public Paralyze(Env env, Effect effect)
	{
		super(env, effect);
	}

	@Override
	public boolean canBeStolen()
	{
		return true;
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.PARALYZE;
	}
	
	@Override
	public boolean onStart()
	{
		getEffected().startAbnormalEffect(AbnormalEffect.HOLD_1);
		getEffected().getAI().setIntention(CtrlIntention.IDLE, getEffector());
		getEffected().startParalyze();
		return super.onStart();
	}
	
	@Override
	public void onExit()
	{
		getEffected().stopAbnormalEffect(AbnormalEffect.HOLD_1);
		if (!getEffected().isPlayer())
		{
			if (getEffected().isSummon())
			{
				((SummonAI) getEffected().getAI()).notifyFollowStatusChange();
			}
			else
			{
				getEffected().getAI().notifyEvent(CtrlEvent.EVT_THINK);
			}
		}
		super.onExit();
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
	
	@Override
	public int getEffectFlags()
	{
		return EffectFlag.PARALYZED.getMask();
	}
}