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
import gameserver.ai.model.CtrlEvent;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;
import gameserver.utils.NpcUtils;

/**
 * Created by LordWinter 29.05.2019
 */
public class BatwingDrake extends HerbCollectorMystic
{
	public BatwingDrake(Attackable actor)
	{
		super(actor);
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		final Attackable actor = getActiveChar();
		if (actor == null || actor.isDead())
		{
			return;
		}
		final int chance = actor.getTemplate().getParameter("helpersSpawnChance", 0);
		if (attacker != null && Rnd.chance(chance))
		{
			final var npc = NpcUtils.spawnSingle(22828, (actor.getX() + Rnd.get(-100, 100)), (actor.getY() + Rnd.get(-100, 100)), actor.getZ());
			npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 2);
		}
		super.onEvtAttacked(attacker, damage);
	}
}
