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
package loginserver.network.communication;

import java.nio.ByteBuffer;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import loginserver.network.communication.gameserverpackets.AuthRequest;
import loginserver.network.communication.gameserverpackets.ChangeAccessLevel;
import loginserver.network.communication.gameserverpackets.ChangeAllowedHwid;
import loginserver.network.communication.gameserverpackets.ChangeAllowedIp;
import loginserver.network.communication.gameserverpackets.ChangePassword;
import loginserver.network.communication.gameserverpackets.OnlineStatus;
import loginserver.network.communication.gameserverpackets.PingResponse;
import loginserver.network.communication.gameserverpackets.PlayerAuthRequest;
import loginserver.network.communication.gameserverpackets.PlayerInGame;
import loginserver.network.communication.gameserverpackets.PlayerLogout;
import loginserver.network.communication.gameserverpackets.SetAccountInfo;

public class PacketHandler
{
	private static final Logger _log = LogManager.getLogger(PacketHandler.class);
	
	public static ReceivablePacket handlePacket(GameServer gs, ByteBuffer buf)
	{
		ReceivablePacket packet = null;

		final int id = buf.get() & 0xff;

		if (!gs.isAuthed())
		{
			switch (id)
			{
				case 0x00 :
					packet = new AuthRequest();
					break;
				default :
					_log.warn("Received unknown packet: " + Integer.toHexString(id));
			}
		}
		else
		{
			switch (id)
			{
				case 0x01 :
					packet = new OnlineStatus();
					break;
				case 0x02 :
					packet = new PlayerAuthRequest();
					break;
				case 0x03 :
					packet = new PlayerInGame();
					break;
				case 0x04 :
					packet = new PlayerLogout();
					break;
				case 0x05 :
					packet = new SetAccountInfo();
					break;
				case 0x07 :
					packet = new ChangeAllowedIp();
					break;
				case 0x08 :
					packet = new ChangePassword();
					break;
				case 0x09 :
					packet = new ChangeAllowedHwid();
					break;
				case 0x11 :
					packet = new ChangeAccessLevel();
					break;
				case 0xff :
					packet = new PingResponse();
					break;
				default :
					_log.warn("Received unknown packet: " + Integer.toHexString(id));
			}
		}
		return packet;
	}
}