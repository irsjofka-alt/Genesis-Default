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
package loginserver.network.communication.gameserverpackets;

import loginserver.network.communication.GameServer;
import loginserver.network.communication.ReceivablePacket;

public class PlayerLogout extends ReceivablePacket
{
	private String _account;
  
	@Override
	protected void readImpl()
	{
		_account = readS();
	}
  
	@Override
	protected void runImpl()
	{
		final GameServer gs = getGameServer();
		if (gs.isAuthed())
		{
			gs.removeAccount(_account);
		}
	}
}
