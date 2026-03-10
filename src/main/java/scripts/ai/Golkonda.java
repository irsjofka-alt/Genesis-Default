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
package scripts.ai;

import gameserver.ai.npc.Fighter;
import gameserver.model.Location;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;
import gameserver.utils.NpcUtils;

/**
 * Created by LordWinter 15.09.2018
 */
public class Golkonda extends Fighter
{
	public Golkonda(Attackable actor)
	{
		super(actor);
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		final Attackable actor = getActiveChar();
		if (attacker != null && (actor.getZ() > 7500) || (actor.getZ() < 6900))
		{
			actor.teleToLocation(116313, 15896, 6999, true, actor.getReflection());
			actor.getStatus().setCurrentHp(actor.getMaxHp());
		}
		super.onEvtAttacked(attacker, damage);
	}
	
	@Override
	protected void onEvtDead(Creature killer)
	{
		NpcUtils.spawnSingleNpc(31029, Location.findAroundPosition(getActiveChar(), 80, 120), 60000);
		super.onEvtDead(killer);
	}
}
