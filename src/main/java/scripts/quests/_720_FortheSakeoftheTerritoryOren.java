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
package scripts.quests;

import gameserver.network.NpcStringId;

/**
 * Updated by LordWinter 03.10.2011
 * Based on L2J Eternity-World
 */
public class _720_FortheSakeoftheTerritoryOren extends TerritoryWarSuperClass
{
	public _720_FortheSakeoftheTerritoryOren()
	{
		super(720);
		CATAPULT_ID = 36502;
		TERRITORY_ID = 84;
		LEADER_IDS = new int[]
		{
		        36526, 36528, 36531, 36594
		};
		GUARD_IDS = new int[]
		{
		        36527, 36529, 36530
		};
		npcString = new NpcStringId[]
		{
		        NpcStringId.THE_CATAPULT_OF_OREN_HAS_BEEN_DESTROYED
		};
		registerKillIds();
	}
}
