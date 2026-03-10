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
import gameserver.model.skills.effects.Effect;
import gameserver.model.skills.effects.EffectTemplate;
import gameserver.model.skills.effects.EffectType;
import gameserver.model.stats.Env;

public class UnsummonAgathion extends Effect
{
	public UnsummonAgathion(Env env, EffectTemplate template)
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
		final Player player = getEffector().getActingPlayer();
		if (player != null)
		{
			player.setAgathionId(0);
			player.broadcastUserInfo(true);
			boolean isUpdate = false;
			for (final Effect e : player.getAllEffects())
			{
				if ((e != null) && (e.getSkill().hasEffectType(EffectType.ENERGY_DAM_OVER_TIME)))
				{
					e.exit(false);
					isUpdate = true;
				}
			}
			
			if (isUpdate)
			{
				player.updateEffectIcons();
			}
		}
		return true;
	}
}