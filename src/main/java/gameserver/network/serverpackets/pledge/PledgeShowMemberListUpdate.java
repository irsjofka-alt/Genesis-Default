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
package gameserver.network.serverpackets.pledge;

import gameserver.model.Clan;
import gameserver.model.ClanMember;
import gameserver.model.actor.Player;
import gameserver.network.serverpackets.GameServerPacket;

public final class PledgeShowMemberListUpdate extends GameServerPacket
{
	private final int _pledgeType, _hasSponsor, _level, _classId, _objectId, _sex, _race;
	private final String _name;
	private final boolean _isOnline;
	
	public PledgeShowMemberListUpdate(Player player)
	{
		_pledgeType = player.getPledgeType();
		_hasSponsor = _pledgeType == Clan.SUBUNIT_ACADEMY && player.getSponsor() != 0 ? 0x01 : 0x00;
		_name = player.getName(null);
		_level = player.getLevel();
		_classId = player.getClassId().getId();
		_race = player.getRace().ordinal();
		_sex = player.getAppearance().getSex() ? 1 : 0;
		_objectId = player.getObjectId();
		_isOnline = player.isOnline() && !player.isInOfflineMode();
	}

	public PledgeShowMemberListUpdate(ClanMember member)
	{
		_name = member.getName();
		_level = member.getLevel();
		_classId = member.getClassId();
		_objectId = member.getObjectId();
		_race = member.getRaceOrdinal();
		_isOnline = member.isOnline() && member.getPlayerInstance() != null;
		_pledgeType = member.getPledgeType();
		_sex = member.getSex() ? 1 : 0;
		_hasSponsor = _pledgeType == Clan.SUBUNIT_ACADEMY && member.getSponsor() != 0 ? 0x01 : 0x00;
	}

	@Override
	protected final void writeImpl()
	{
		writeS(_name);
		writeD(_level);
		writeD(_classId);
		writeD(_sex);
		writeD(_race);
		writeD(_isOnline ? _objectId : 0x00);
		writeD(_pledgeType);
		writeD(_hasSponsor);
	}
}