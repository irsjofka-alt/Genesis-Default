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

import gameserver.Config;
import gameserver.network.serverpackets.VersionCheck;
import gameserver.utils.strixplatform.StrixPlatform;
import gameserver.utils.strixplatform.managers.ClientGameSessionManager;
import gameserver.utils.strixplatform.managers.ClientProtocolDataManager;

public final class SendProtocolVersion extends GameClientPacket
{
	private int _version;
	private byte[] _data;
	private int _dataChecksum;
	
	@Override
	protected void readImpl()
	{
		_version = readD();
		if (StrixPlatform.getInstance().isPlatformEnabled())
		{
			try
			{
				if (_buf.remaining() >= StrixPlatform.getInstance().getProtocolVersionDataSize())
				{
					_data = new byte[StrixPlatform.getInstance().getClientDataSize()];
					readB(_data);
					_dataChecksum = readD();
				}
			}
			catch (final Exception e)
			{
				final var client = getClient();
				if (client != null)
				{
					client.close(new VersionCheck(null));
				}
				return;
			}
		}
	}
	
	@Override
	protected void runImpl()
	{
		final var client = getClient();
		if (client == null)
		{
			return;
		}
		
		if (_version == -2)
		{
			client.closeNow(false);
		}
		else if (!Config.PROTOCOL_LIST.contains(_version))
		{
			client.close(new VersionCheck(null));
		}
		
		if (!StrixPlatform.getInstance().isPlatformEnabled())
		{
			client.setRevision(_version);
			client.sendPacket(new VersionCheck(_client.enableCrypt()));
			return;
		}
		else
		{
			if (_data == null)
			{
				client.close(new VersionCheck(null));
				return;
			}
			else
			{
				final var clientData = ClientProtocolDataManager.getInstance().getDecodedData(_data, _dataChecksum);
				if (clientData != null)
				{
					if (!ClientGameSessionManager.getInstance().checkServerResponse(clientData))
					{
						client.close(new VersionCheck(null, clientData));
						return;
					}
					client.setStrixClientData(clientData);
					client.setRevision(_version);
					sendPacket(new VersionCheck(_client.enableCrypt()));
					return;
				}
				client.close(new VersionCheck(null));
			}
		}
	}
}