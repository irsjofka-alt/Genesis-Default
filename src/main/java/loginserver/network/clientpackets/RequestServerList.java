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
package loginserver.network.clientpackets;

import loginserver.network.LoginClient;
import loginserver.network.SessionKey;
import loginserver.network.serverpackets.LoginFail.LoginFailReason;
import loginserver.network.serverpackets.ServerList;

public class RequestServerList extends LoginClientPacket
{
	private int _loginOkID1;
	private int _loginOkID2;
	protected int _unk;

	@Override
	protected void readImpl()
	{
		_loginOkID1 = readD();
		_loginOkID2 = readD();
		_unk = readC();
	}

	@Override
	protected void runImpl()
	{
		final LoginClient client = getClient();
		final SessionKey skey = client.getSessionKey();
		if(skey == null || !skey.checkLoginPair(_loginOkID1, _loginOkID2))
		{
			client.close(LoginFailReason.REASON_ACCESS_FAILED);
			return;
		}
		client.sendPacket(new ServerList(client.getAccount()));
	}
}