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
public class _725_FortheSakeoftheTerritorySchuttgart extends TerritoryWarSuperClass
{
	public _725_FortheSakeoftheTerritorySchuttgart()
	{
		super(725);
		
		CATAPULT_ID = 36507;
		TERRITORY_ID = 89;
		LEADER_IDS = new int[]
		{
		        36556, 36558, 36561, 36599
		};
		GUARD_IDS = new int[]
		{
		        36557, 36559, 36560
		};
		npcString = new NpcStringId[]
		{
		        NpcStringId.THE_CATAPULT_OF_SCHUTTGART_HAS_BEEN_DESTROYED
		};
		registerKillIds();
	}
}
