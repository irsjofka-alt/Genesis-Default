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

import gameserver.model.actor.Npc;
import gameserver.model.skills.effects.AbnormalEffect;
import gameserver.model.skills.effects.Effect;
import gameserver.model.skills.effects.EffectTemplate;
import gameserver.model.skills.effects.EffectType;
import gameserver.model.stats.Env;

public class Grow extends Effect
{
	public Grow(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public EffectType getEffectType()
	{
		return EffectType.BUFF;
	}

	@Override
	public boolean onStart()
	{
		if (getEffected().isNpc())
		{
			final Npc npc = (Npc) getEffected();
			npc.setCollisionRadius((npc.getColRadius() * 1.19));

			getEffected().startAbnormalEffect(AbnormalEffect.GROW);
			return true;
		}
		return false;
	}

	@Override
	public void onExit()
	{
		if (getEffected() instanceof Npc)
		{
			final Npc npc = (Npc) getEffected();
			npc.setCollisionRadius(npc.getTemplate().getfCollisionRadius());

			getEffected().stopAbnormalEffect(AbnormalEffect.GROW);
		}
	}
}