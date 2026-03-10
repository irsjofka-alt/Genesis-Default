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
import gameserver.data.parser.CategoryParser;
import gameserver.model.CategoryType;
import gameserver.model.Location;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;

/**
 * Created by LordWinter 29.05.2019
 */
public class DrakeRunners extends PatrollersNoWatch
{
	public DrakeRunners(Attackable actor)
	{
		super(actor);

		_points = new Location[]
		{
		        new Location(148984, 112952, -3720), new Location(149160, 114312, -3720), new Location(149096, 115480, -3720), new Location(147720, 116216, -3720), new Location(146536, 116296, -3720), new Location(145192, 115304, -3720), new Location(144888, 114504, -3720), new Location(145240, 113272, -3720), new Location(145960, 112696, -3720), new Location(147416, 112488, -3720)
		};
	}
	
	@Override
	protected void onEvtDead(Creature killer)
	{
		final var actor = getActiveChar();
		super.onEvtDead(killer);
		
		if (killer != null && killer.isPlayer() && (actor.getId() == 22850 || actor.getId() == 22851))
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
