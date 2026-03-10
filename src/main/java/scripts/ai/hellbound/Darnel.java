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
package scripts.ai.hellbound;

import gameserver.ai.npc.Fighter;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;

/**
 * Created by LordWinter 15.11.2018
 */
public class Darnel extends Fighter
{
	public Darnel(Attackable actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		// Quest.addSpawn(32279, new Location(152761, 145950, -12588, 0),
		// killer.getReflection());
		super.onEvtDead(killer);
	}
}
