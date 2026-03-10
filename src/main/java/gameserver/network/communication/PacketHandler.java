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
package gameserver.network.communication;

import java.nio.ByteBuffer;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import gameserver.network.communication.loginserverpackets.AuthResponse;
import gameserver.network.communication.loginserverpackets.ChangePasswordResponse;
import gameserver.network.communication.loginserverpackets.GetAccountInfo;
import gameserver.network.communication.loginserverpackets.KickPlayer;
import gameserver.network.communication.loginserverpackets.LoginServerFail;
import gameserver.network.communication.loginserverpackets.PingRequest;
import gameserver.network.communication.loginserverpackets.PlayerAuthResponse;

public class PacketHandler
{
	private static final Logger _log = LogManager.getLogger(PacketHandler.class);
	
	public static ReceivablePacket handlePacket(ByteBuffer buf)
	{
		ReceivablePacket packet = null;
		
		final int id = buf.get() & 0xff;
		
		switch (id)
		{
			case 0x00 :
				packet = new AuthResponse();
				break;
			case 0x01 :
				packet = new LoginServerFail();
				break;
			case 0x02 :
				packet = new PlayerAuthResponse();
				break;
			case 0x03 :
				packet = new KickPlayer();
				break;
			case 0x04 :
				packet = new GetAccountInfo();
				break;
			case 0x06 :
				packet = new ChangePasswordResponse();
				break;
			case 0xff :
				packet = new PingRequest();
				break;
			default :
				_log.warn("Received unknown packet: " + Integer.toHexString(id));
		}
		return packet;
	}
}