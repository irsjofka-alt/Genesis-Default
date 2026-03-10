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
import gameserver.network.serverpackets.ExPrivateStorePackageMsg;
import gameserver.utils.Util;

public class SetPrivateStoreWholeMsg extends GameClientPacket
{
	private static final int MAX_MSG_LENGTH = 29;

	private String _msg;

	@Override
	protected void readImpl()
	{
		_msg = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if ((player == null) || (player.getSellList() == null))
		{
			return;
		}

		if ((_msg != null) && (_msg.length() > MAX_MSG_LENGTH))
		{
			Util.handleIllegalPlayerAction(player, "" + player.getName(null) + " tried to overflow private store whole message");
			return;
		}

		player.getSellList().setTitle(_msg);
		sendPacket(new ExPrivateStorePackageMsg(player));
	}
}