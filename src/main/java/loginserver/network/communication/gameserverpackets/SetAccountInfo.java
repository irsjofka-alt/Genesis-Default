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

import org.apache.commons.lang3.ArrayUtils;

import loginserver.accounts.SessionManager;
import loginserver.network.communication.ReceivablePacket;

public class SetAccountInfo extends ReceivablePacket
{
	private String _account;
	private int _size;
	private int[] _deleteChars;
	
	@Override
	protected void readImpl()
	{
		_account = readS();
		_size = readC();
		final int size = readD();
		if (size > 7 || size <= 0)
		{
			_deleteChars = ArrayUtils.EMPTY_INT_ARRAY;
		}
		else
		{
			_deleteChars = new int[size];
			for (int i = 0; i < _deleteChars.length; i++)
			{
				_deleteChars[i] = readD();
			}
		}
	}
	
	@Override
	protected void runImpl()
	{
		final var gs = getGameServer();
		if (gs.isAuthed())
		{
			final var session = SessionManager.getInstance().getSessionByName(_account);
			if (session == null)
			{
				return;
			}
			
			final var host = gs.getHost();
			if (host != null)
			{
				session.getAccount().addAccountInfo(host.getId(), _size, _deleteChars);
			}
		}
	}
}