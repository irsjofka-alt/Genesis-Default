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

import gameserver.model.actor.Attackable;
import gameserver.model.skills.effects.Effect;
import gameserver.model.skills.effects.EffectTemplate;
import gameserver.model.skills.effects.EffectType;
import gameserver.model.stats.Env;

public class Sweeper extends Effect
{
	public Sweeper(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public boolean onStart()
	{
		if ((getEffector() == null) || (getEffected() == null) || !getEffector().isPlayer() || !getEffected().isAttackable())
		{
			return false;
		}

		final var player = getEffector().getActingPlayer();
		final var monster = (Attackable) getEffected();
		if (!monster.checkSpoilOwner(player, false))
		{
			return false;
		}

		final var items = monster.takeSweep();
		if (items == null)
		{
			return false;
		}
		
		if (!player.getInventory().checkInventorySlotsAndWeight(monster.getSpoilLootItems(), false, false))
		{
			return false;
		}

		for (final var item : items)
		{
			if (player.isInParty() && player.getParty().getLootDistribution() == 2)
			{
				player.getParty().distributeItem(player, item._itemId, item._count, true, monster);
				continue;
			}
			player.addItem("Sweeper", item._itemId, item._count, getEffected(), true);
		}
		return true;
	}

	@Override
	public EffectType getEffectType()
	{
		return EffectType.NONE;
	}
}