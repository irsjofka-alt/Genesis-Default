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
package loginserver.network;

import java.nio.ByteBuffer;

import org.nio.impl.IPacketHandler;
import org.nio.impl.ReceivablePacket;

import loginserver.network.LoginClient.LoginClientState;
import loginserver.network.clientpackets.AuthGameGuard;
import loginserver.network.clientpackets.RequestAuthLogin;
import loginserver.network.clientpackets.RequestServerList;
import loginserver.network.clientpackets.RequestServerLogin;

public final class LoginPacketHandler implements IPacketHandler<LoginClient>
{
	@Override
	public ReceivablePacket<LoginClient> handlePacket(ByteBuffer buf, LoginClient client)
	{
		final int opcode = buf.get() & 0xFF;

		ReceivablePacket<LoginClient> packet = null;
		final LoginClientState state = client.getState();

		switch(state)
		{
			case CONNECTED:
				if(opcode == 0x07)
				{
					packet = new AuthGameGuard();
				}
				break;
			case AUTHED_GG:
				if(opcode == 0x00)
				{
					packet = new RequestAuthLogin();
				}
				break;
			case AUTHED:
				if(opcode == 0x05)
				{
					packet = new RequestServerList();
				}
				else if(opcode == 0x02)
				{
					packet = new RequestServerLogin();
				}
				break;
		}
		return packet;
	}
}