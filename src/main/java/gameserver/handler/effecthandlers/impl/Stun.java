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

import gameserver.ai.model.CtrlEvent;
import gameserver.ai.model.CtrlIntention;
import gameserver.model.skills.effects.Effect;
import gameserver.model.skills.effects.EffectFlag;
import gameserver.model.skills.effects.EffectTemplate;
import gameserver.model.skills.effects.EffectType;
import gameserver.model.stats.Env;

public class Stun extends Effect
{
	public Stun(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public int getEffectFlags()
	{
		return EffectFlag.STUNNED.getMask();
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.STUN;
	}
	
	@Override
	public void onExit()
	{
		if (getSkill().getId() == 5008)
		{
			getEffected().setIsDanceStun(false);
		}
		getEffected().setIsStuned(false);
	}
	
	@Override
	public boolean onStart()
	{
		final var effected = getEffected();
		if (getSkill().getId() == 5008)
		{
			effected.setIsDanceStun(true);
		}
		effected.abortAttack();
		effected.abortCast();
		effected.stopMove(null);
		effected.getAI().notifyEvent(CtrlEvent.EVT_STUNNED);
		if (!effected.isSummon() && !effected.isAttackable())
		{
			effected.getAI().setIntention(CtrlIntention.IDLE);
		}
		effected.setIsStuned(true);
		return super.onStart();
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}