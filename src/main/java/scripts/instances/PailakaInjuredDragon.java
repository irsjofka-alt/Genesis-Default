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
package scripts.instances;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import gameserver.ai.model.CtrlIntention;
import gameserver.model.World;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.actor.templates.reflection.ReflectionTemplate;
import gameserver.model.entity.Reflection;
import gameserver.model.zone.ZoneType;

/**
 * Rework by LordWinter 13.12.2020
 */
public class PailakaInjuredDragon extends AbstractReflection
{
	private static final Map<Integer, int[]> NOEXIT_ZONES = new ConcurrentHashMap<>();
	static
	{
		NOEXIT_ZONES.put(200001, new int[]
		{
		        123167, -45743, -3023
		});
		NOEXIT_ZONES.put(200002, new int[]
		{
		        117783, -46398, -2560
		});
		NOEXIT_ZONES.put(200003, new int[]
		{
		        116791, -51556, -2584
		});
		NOEXIT_ZONES.put(200004, new int[]
		{
		        117993, -52505, -2480
		});
		NOEXIT_ZONES.put(200005, new int[]
		{
		        113226, -44080, -2776
		});
		NOEXIT_ZONES.put(200006, new int[]
		{
		        107916, -46716, -2008
		});
		NOEXIT_ZONES.put(200007, new int[]
		{
		        118341, -55951, -2280
		});
		NOEXIT_ZONES.put(200008, new int[]
		{
		        110127, -41562, -2332
		});
	}
	
	public PailakaInjuredDragon()
	{
		super(45);
		
		addStartNpc(32499);
		addTalkId(32499);
		
		for (final int zoneid : NOEXIT_ZONES.keySet())
		{
			addEnterZoneId(zoneid);
		}
	}
	
	@Override
	protected void onTeleportEnter(Player player, ReflectionTemplate template, Reflection r, boolean firstEntrance)
	{
		if (firstEntrance)
		{
			r.addAllowed(player);
			player.getAI().setIntention(CtrlIntention.IDLE);
			final var teleLoc = template.getTeleportCoord();
			player.teleToLocation(teleLoc, true, r);
			if (player.hasSummon())
			{
				player.getSummon().getAI().setIntention(CtrlIntention.IDLE);
				player.getSummon().teleToLocation(teleLoc, true, r);
			}
		}
		else
		{
			player.getAI().setIntention(CtrlIntention.IDLE);
			final var teleLoc = template.getTeleportCoord();
			player.teleToLocation(teleLoc, true, r);
			if (player.hasSummon())
			{
				player.getSummon().getAI().setIntention(CtrlIntention.IDLE);
				player.getSummon().teleToLocation(teleLoc, true, r);
			}
		}
	}
	
	private synchronized void enterInstance(Player player, Npc npc)
	{
		if (enterReflection(player, npc, 45))
		{
			final var r = player.getReflection();
			if (isInReflection(r))
			{
				final var latana = addSpawn(18660, 105732, -41787, -1775, 35742, false, 0, false, r);
				if (latana != null)
				{
					latana.setIsImmobilized(true);
				}
			}
		}
	}
	
	@Override
	public final String onAdvEvent(String event, Npc npc, Player player)
	{
		if (event.equalsIgnoreCase("enter"))
		{
			enterInstance(player, npc);
			return null;
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onEnterZone(Creature character, ZoneType zone)
	{
		if (character.isPlayer() && !character.isDead() && !character.isTeleporting())
		{
			final var r = character.getReflection();
			if (isInReflection(r))
			{
				final int[] zoneTeleport = NOEXIT_ZONES.get(zone.getId());
				if (zoneTeleport != null)
				{
					for (final var npcs : World.getAroundAttackable(character, 1200, 200))
					{
						if (npcs == null || npcs.isDead())
						{
							continue;
						}
						character.getAI().setIntention(CtrlIntention.IDLE);
						character.teleToLocation(zoneTeleport[0], zoneTeleport[1], zoneTeleport[2], true, r);
						break;
					}
				}
			}
		}
		return super.onEnterZone(character, zone);
	}
	
	public static void main(String[] args)
	{
		new PailakaInjuredDragon();
	}
}