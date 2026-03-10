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
package scripts.instances.ChambersOfDelusion;

import gameserver.model.Location;

/**
 * Rework by LordWinter 14.11.2020
 */
public final class ChamberOfDelusionNorth extends Chamber
{
	private final Location[] _enterCoords = new Location[]
	{
		new Location(-108976, -207772, -6720),
		new Location(-108976, -206972, -6720),
		new Location(-108960, -209164, -6720),
		new Location(-108048, -207340, -6720),
		new Location(-108048, -209020, -6720)
	};

	private ChamberOfDelusionNorth()
	{
		super(130, 32661, 32679, 32683, 25693, 18838, "north_chamber_box");
		_coords = _enterCoords;
	}
	
	public static void main(String[] args)
	{
		new ChamberOfDelusionNorth();
	}
}