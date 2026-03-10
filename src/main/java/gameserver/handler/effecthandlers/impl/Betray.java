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

import gameserver.ai.model.CtrlIntention;
import gameserver.model.actor.Player;
import gameserver.model.skills.effects.Effect;
import gameserver.model.skills.effects.EffectFlag;
import gameserver.model.skills.effects.EffectTemplate;
import gameserver.model.skills.effects.EffectType;
import gameserver.model.stats.Env;

public class Betray extends Effect
{
	public Betray(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public EffectType getEffectType()
	{
		return EffectType.BETRAY;
	}

	@Override
	public boolean onStart()
	{
		if (getEffector().isPlayer() && getEffected().isSummon())
		{
			final Player targetOwner = getEffected().getActingPlayer();
			getEffected().getAI().setIntention(CtrlIntention.ATTACK, targetOwner);
			return true;
		}
		return false;
	}

	@Override
	public void onExit()
	{
		getEffected().getAI().setIntention(CtrlIntention.IDLE);
	}

	@Override
	public int getEffectFlags()
	{
		return EffectFlag.BETRAYED.getMask();
	}
}