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
package scripts.instances.KamalokaSolo;

import gameserver.model.Location;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;

/**
 * Rework by LordWinter 13.02.2020
 */
public class Kamaloka35_45 extends KamalokaSolo
{
	public Kamaloka35_45()
	{
		super();

		addStartNpc(32484);
		addTalkId(32484, 32485);
		addKillId(22461, 22462, 22463);
		
		_rewardPosition = new Location(9290, -212993, -7799);
	}

	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (event.equalsIgnoreCase("enter"))
		{
			return onEnterTo(npc, player, 49);
		}
		return onAdvEventTo(event, npc, player);
	}

	@Override
	public String onTalk(Npc npc, Player player)
	{
		if (npc.getId() == 32484)
		{
			return onEnterTo(npc, player, 49);
		}
		return onTalkTo(npc, player);
	}

	@Override
	public String onKill(Npc npc, Player player, boolean isPet)
	{
		return onKillTo(npc, player, isPet, 22461, new int[]
		{
		        22462, 22463
		});
	}

	public static void main(String[] args)
	{
		new Kamaloka35_45();
	}
}
