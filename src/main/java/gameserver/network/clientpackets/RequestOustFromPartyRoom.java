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
package gameserver.network.clientpackets;

import gameserver.model.GameObjectsStorage;
import gameserver.model.actor.Player;
import gameserver.model.matching.MatchingRoom;
import gameserver.network.SystemMessageId;

public final class RequestOustFromPartyRoom extends GameClientPacket
{
	private int _charid;

	@Override
	protected void readImpl()
	{
		_charid = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();

		final MatchingRoom room = player.getMatchingRoom();
		if (room == null || room.getType() != MatchingRoom.PARTY_MATCHING)
		{
			return;
		}

		if (room.getLeader() != player)
		{
			return;
		}

		final Player member = GameObjectsStorage.getPlayer(_charid);
		if (member == null)
		{
			return;
		}

		final int type = room.getMemberType(member);
		if (type == MatchingRoom.ROOM_MASTER)
		{
			return;
		}
		if (type == MatchingRoom.PARTY_MEMBER)
		{
			player.sendPacket(SystemMessageId.CANNOT_DISMISS_PARTY_MEMBER);
			return;
		}
		room.removeMember(member, true);
	}
}