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
package scripts.ai.gracia;

import l2e.commons.util.Rnd;
import gameserver.Config;
import gameserver.ai.npc.Fighter;
import gameserver.data.parser.CategoryParser;
import gameserver.instancemanager.ServerVariables;
import gameserver.instancemanager.ZoneManager;
import gameserver.model.CategoryType;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;
import gameserver.model.zone.type.EffectZone;

/**
 * Created by LordWinter 18.04.2023
 */
public class GraciaHerbCollector extends Fighter
{
	private static final int[] ZONE_BUFFS =
	{
	        60006, 60007, 60008
	};
	
	public GraciaHerbCollector(Attackable actor)
	{
		super(actor);
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		final Attackable npc = getActiveChar();
		if (npc.isScriptValue(0) && attacker != null && attacker.isPlayer())
		{
			final var player = attacker.getActingPlayer();
			if (player != null && CategoryParser.getInstance().isInCategory(CategoryType.WIZARD_GROUP, player.getClassId().getId()))
			{
				npc.setScriptValue(1);
			}
		}
		super.onEvtAttacked(attacker, damage);
	}
	
	@Override
	protected void onEvtDead(Creature killer)
	{
		final Attackable npc = getActiveChar();
		super.onEvtDead(killer);
		if (npc.isScriptValue(1) && killer != null && killer.isPlayer())
		{
			final int zoneId = ServerVariables.getInt("SOABuffList", 0);
			final var zone = ZoneManager.getInstance().getZoneById(ZONE_BUFFS[zoneId], EffectZone.class);
			if (zone != null && zone.isInsideZone(npc.getLocation()))
			{
				final var chance = 70 * Config.RATE_CHANCE_DROP_HERBS;
				if (chance > 0)
				{
					if (Rnd.chance(chance))
					{
						npc.dropSingleItem(killer.getActingPlayer(), 8603, 1);
					}
					
					if (Rnd.chance(chance))
					{
						npc.dropSingleItem(killer.getActingPlayer(), 8603, 1);
					}
					
					if (Rnd.chance(chance))
					{
						npc.dropSingleItem(killer.getActingPlayer(), 8604, 1);
					}
				}
			}
		}
	}
}