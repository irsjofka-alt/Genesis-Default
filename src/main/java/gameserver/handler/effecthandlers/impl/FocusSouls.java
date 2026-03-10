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

import gameserver.model.actor.Player;
import gameserver.model.skills.effects.EffectTemplate;
import gameserver.model.skills.effects.Effect;
import gameserver.model.skills.effects.EffectType;
import gameserver.model.stats.Env;
import gameserver.model.stats.Stats;
import gameserver.network.SystemMessageId;

public class FocusSouls extends Effect
{
	public FocusSouls(Env env, EffectTemplate template)
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
		if (!getEffected().isPlayer() || getEffected().isAlikeDead())
		{
			return false;
		}
		
		final Player target = getEffected().getActingPlayer();
		final int maxSouls = (int) target.calcStat(Stats.MAX_SOULS, 0, null, null);
		if (maxSouls > 0)
		{
			final int amount = (int) calc();
			if ((target.getChargedSouls() < maxSouls))
			{
				final int count = ((target.getChargedSouls() + amount) <= maxSouls) ? amount : (maxSouls - target.getChargedSouls());
				target.increaseSouls(count);
			}
			else
			{
				target.sendPacket(SystemMessageId.SOUL_CANNOT_BE_INCREASED_ANYMORE);
				return false;
			}
		}
		return true;
	}
}