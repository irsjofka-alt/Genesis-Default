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
package gameserver.instancemanager;

import java.util.List;

import gameserver.model.entity.Castle;
import gameserver.model.zone.ZoneType;
import gameserver.model.zone.type.TownZone;

public class TownManager
{
	public static final int getTownCastle(int townId)
	{
		return switch (townId)
		{
			case 912  -> 1;
			case 916  -> 2;
			case 918  -> 3;
			case 922  -> 4;
			case 924  -> 5;
			case 926  -> 6;
			case 1538  -> 7;
			case 1537  -> 8;
			case 1714  -> 9;
			default  -> 0;
		};
	}
	
	public static final boolean townHasCastleInSiege(int townId)
	{
		final int castleIndex = getTownCastle(townId);
		if (castleIndex > 0)
		{
			final Castle castle = CastleManager.getInstance().getCastleById(castleIndex);
			if (castle != null)
			{
				return castle.getSiege().getIsInProgress();
			}
		}
		return false;
	}
	
	public static final boolean townHasCastleInSiege(int x, int y)
	{
		return townHasCastleInSiege(MapRegionManager.getInstance().getMapRegionLocId(x, y));
	}
	
	public static final TownZone getTown(int townId)
	{
		for (final TownZone temp : ZoneManager.getInstance().getAllZones(TownZone.class))
		{
			if (temp != null && temp.getTownId() == townId)
			{
				return temp;
			}
		}
		return null;
	}
	
	public static final TownZone getTown(int x, int y, int z)
	{
		final List<ZoneType> zones = ZoneManager.getInstance().getZones(x, y, z);
		if (zones != null && !zones.isEmpty())
		{
			for (final ZoneType zone : zones)
			{
				if (zone != null && zone instanceof TownZone townZone)
				{
					return townZone;
				}
			}
		}
		return null;
	}
	
	public static final TownZone getTownZone(int x, int y, int z)
	{
		final TownZone zone = ZoneManager.getInstance().getZone(x, y, z, TownZone.class);
		if (zone != null && zone.getTaxById() > 0)
		{
			return zone;
		}
		return null;
	}
}