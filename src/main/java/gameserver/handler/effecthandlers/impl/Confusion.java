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

import java.util.ArrayList;
import java.util.List;

import l2e.commons.util.Rnd;
import gameserver.ai.model.CtrlIntention;
import gameserver.model.World;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;
import gameserver.model.skills.effects.Effect;
import gameserver.model.skills.effects.EffectFlag;
import gameserver.model.skills.effects.EffectTemplate;
import gameserver.model.skills.effects.EffectType;
import gameserver.model.stats.Env;

public class Confusion extends Effect
{
	public Confusion(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public int getEffectFlags()
	{
		return EffectFlag.CONFUSED.getMask();
	}

	@Override
	public EffectType getEffectType()
	{
		return EffectType.NONE;
	}

	private Creature getRndTarget()
	{
		final List<Creature> targetList = new ArrayList<>();
		for (final Creature obj : World.getAroundCharacters(getEffected(), 2000, 400))
		{
			if (((getEffected().isMonster() && obj.isAttackable()) || (obj.isCreature())) && (obj != getEffected()) && obj != getEffector())
			{
				targetList.add(obj);
			}
		}
		if (!targetList.isEmpty())
		{
			return targetList.get(Rnd.nextInt(targetList.size()));
		}
		return null;
	}
	
	@Override
	public boolean onStart()
	{
		getEffected().startConfused();
		final Creature target = getRndTarget();
		if (target != null)
		{
			if (getEffected().isMonster())
			{
				((Attackable) getEffected()).addDamageHate(target, 1, 99999);
			}
			getEffected().setTarget(target);
			getEffected().getAI().setIntention(CtrlIntention.ATTACK, target);
		}
		return true;
	}
	
	@Override
	public void onExit()
	{
		getEffected().stopConfused();
		if (getEffected().isMonster() && getEffected().getTarget() != null && getEffected().getTarget().isMonster())
		{
			((Attackable) getEffected()).getAggroList().stopHating((Creature) getEffected().getTarget());
			getEffected().breakAttack();
			getEffected().abortCast();
			((Attackable) getEffected().getTarget()).getAggroList().stopHating(getEffected());
			((Attackable) getEffected().getTarget()).breakAttack();
			((Attackable) getEffected().getTarget()).abortCast();
		}
	}
}