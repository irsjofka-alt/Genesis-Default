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
package gameserver.network.serverpackets;

import java.util.HashMap;
import java.util.Map;

import gameserver.model.Location;
import gameserver.model.Party;

public class PartyMemberPosition extends GameServerPacket
{
	private final Map<Integer, Location> _locations = new HashMap<>();

	public PartyMemberPosition(Party party)
	{
		reuse(party);
	}
	
	public void reuse(Party party)
	{
		_locations.clear();
		party.getMembers().stream().filter(p -> p != null).forEach(p -> _locations.put(p.getObjectId(), p.getLocation()));
	}

	@Override
	protected void writeImpl()
	{
		writeD(_locations.size());
		for (final var entry : _locations.entrySet())
		{
			final var loc = entry.getValue();
			writeD(entry.getKey());
			writeD(loc.getX());
			writeD(loc.getY());
			writeD(loc.getZ());
		}
	}
}