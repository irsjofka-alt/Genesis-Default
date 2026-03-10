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

import gameserver.model.actor.Player;
import gameserver.network.serverpackets.PrivateStoreBuyMsg;
import gameserver.utils.Util;

public final class SetPrivateStoreBuyMsg extends GameClientPacket
{
	private static final int MAX_MSG_LENGTH = 29;
	
	private String _storeMsg;
	
	@Override
	protected void readImpl()
	{
		_storeMsg = readS();
	}

	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if ((player == null) || (player.getBuyList() == null))
		{
			return;
		}
		
		if ((_storeMsg != null) && (_storeMsg.length() > MAX_MSG_LENGTH))
		{
			Util.handleIllegalPlayerAction(player, "" + player.getName(null) + " tried to overflow private store buy message");
			return;
		}
		
		player.getBuyList().setTitle(_storeMsg);
		player.sendPacket(new PrivateStoreBuyMsg(player));
	}
}