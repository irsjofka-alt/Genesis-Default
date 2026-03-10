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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gameserver.instancemanager.MapRegionManager;
import gameserver.model.actor.Player;
import gameserver.model.matching.MatchingRoom;

public class ExMpccRoomMember extends GameServerPacket
{
	private final int _type;
	private List<MpccRoomMemberInfo> _members = Collections.emptyList();
	
	public ExMpccRoomMember(MatchingRoom room, Player player)
	{
		_type = room.getMemberType(player);
		_members = new ArrayList<>(room.getPlayers().size());
		
		for (final Player member : room.getPlayers())
		{
			_members.add(new MpccRoomMemberInfo(member, room.getMemberType(member)));
		}
	}

	static class MpccRoomMemberInfo
	{
		public final int objectId;
		public final int classId;
		public final int level;
		public final int location;
		public final int memberType;
		public final String name;
		
		public MpccRoomMemberInfo(Player member, int type)
		{
			objectId = member.getObjectId();
			name = member.getName(null);
			classId = member.getClassId().getId();
			level = member.getLevel();
			location = MapRegionManager.getInstance().getBBs(member.getLocation());
			memberType = type;
		}
	}
	
	@Override
	public void writeImpl()
	{
		writeD(_type);
		writeD(_members.size());
		for (final MpccRoomMemberInfo member : _members)
		{
			writeD(member.objectId);
			writeS(member.name);
			writeD(member.level);
			writeD(member.classId);
			writeD(member.location);
			writeD(member.memberType);
		}
	}
}