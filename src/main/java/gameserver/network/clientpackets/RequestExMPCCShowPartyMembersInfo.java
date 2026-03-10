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
import gameserver.network.serverpackets.ExMPCCShowPartyMemberInfo;

public final class RequestExMPCCShowPartyMembersInfo extends GameClientPacket
{
	private int _partyLeaderId;

	@Override
	protected void readImpl()
	{
		_partyLeaderId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		final Player player = GameObjectsStorage.getPlayer(_partyLeaderId);
		if ((player != null) && (player.getParty() != null))
		{
			activeChar.sendPacket(new ExMPCCShowPartyMemberInfo(player.getParty()));
		}
	}
}