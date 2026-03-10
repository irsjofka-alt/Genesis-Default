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
package loginserver.network.communication.loginserverpackets;

import org.HostInfo;

import loginserver.Config;
import loginserver.network.communication.GameServer;
import loginserver.network.communication.SendablePacket;

public class AuthResponse extends SendablePacket
{
	private final HostInfo _host;
	
	public AuthResponse(GameServer gs)
	{
		_host = gs.getHost();
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0x00);
		writeC(0x00);
		writeS("");
		if (_host != null)
		{
			writeC(0x01);
			writeC(_host.getId());
			writeS(Config.SERVER_NAMES.get(_host.getId()));
		}
		else
		{
			writeC(0x00);
		}
	}
}