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

import org.apache.commons.lang3.ArrayUtils;

import l2e.commons.util.Rnd;
import gameserver.ai.npc.Fighter;
import gameserver.model.Location;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;
import gameserver.utils.NpcUtils;

public class OrbisCursedMobs extends Fighter
{
	private static final int[] Orbis_Zhertva = { 22911, 22912, 22913, 25833 };
	private static final int[] Orbis_Strazh = { 22914, 22915, 22916, 25834 };
	private static final int[] Orbis_Metatel = { 22917, 22918, 22919, 25835, 22920 };
	private static final int[] Orbis_ychennui = { 22921, 22922, 22923, 25836 };
	private static final int[] Orbis_drevnui = { 22924, 22925 };
	private static final int[] Orbis_starwii = { 22926, 22927 };
	
	public OrbisCursedMobs(Attackable actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		final var actor = getActiveChar();
		if (actor.isDead())
		{
			return;
		}
			
		if(Rnd.chance(1))
		{
			spawnDamn(actor.getId(), actor.getLocation(), attacker);
			actor.doDie(attacker);
			actor.decayMe();
		}
		super.onEvtAttacked(attacker, damage);
	}
	
	public void spawnDamn(int npcId, Location loc, Creature attacker)
	{
		int cursedId = 18978;

		if (ArrayUtils.contains(Orbis_Zhertva, npcId))
		{
			cursedId = 18978;
		}
		else if (ArrayUtils.contains(Orbis_Strazh, npcId))
		{
			cursedId = 18979;
		}
		else if (ArrayUtils.contains(Orbis_Metatel, npcId))
		{
			cursedId = 18980;
		}
		else if (ArrayUtils.contains(Orbis_ychennui, npcId))
		{
			cursedId = 18981;
		}
		else if (ArrayUtils.contains(Orbis_drevnui, npcId))
		{
			cursedId = 18982;
		}
		else if (ArrayUtils.contains(Orbis_starwii, npcId))
		{
			cursedId = 18983;
		}
			
		final var cursed = NpcUtils.spawnSingle(cursedId, loc);
		((Attackable) cursed).addDamageHate(attacker, 10000, 0);
  }
}
