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
public final class ChamberOfDelusionEast extends Chamber
{
	private final Location[] _enterCoords = new Location[]
	{
		new Location(-122368, -218972, -6720),
		new Location(-122352, -218044, -6720),
		new Location(-122368, -220220, -6720),
		new Location(-121440, -218444, -6720),
		new Location(-121424, -220124, -6720)
	};

	public ChamberOfDelusionEast()
	{
		super(127, 32658, 32664, 32668, 25690, 18838, "east_chamber_box");
		_coords = _enterCoords;
	}
	
	void main()
	{
		new ChamberOfDelusionEast();
	}
}