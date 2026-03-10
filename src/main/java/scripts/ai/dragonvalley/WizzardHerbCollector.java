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
package scripts.ai.dragonvalley;

import l2e.commons.util.Rnd;
import gameserver.Config;
import gameserver.ai.npc.Fighter;
import gameserver.data.parser.CategoryParser;
import gameserver.model.CategoryType;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;

/**
 * Created by LordWinter 18.04.2023
 */
public class WizzardHerbCollector extends Fighter
{
	public WizzardHerbCollector(Attackable actor)
	{
		super(actor);
	}
	
	@Override
	protected void onEvtDead(Creature killer)
	{
		super.onEvtDead(killer);
		if (killer != null && killer.isPlayer())
		{
			final var player = killer.getActingPlayer();
			if (player != null && CategoryParser.getInstance().isInCategory(CategoryType.WIZARD_GROUP, player.getClassId().getId()))
			{
				final var chance = 70 * Config.RATE_CHANCE_DROP_HERBS;
				if (chance > 0)
				{
					getActiveChar().dropSingleItem(player, Rnd.chance(chance) ? 8603 : 8604, 1);
				}
			}
		}
	}
}