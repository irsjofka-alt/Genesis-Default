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

import gameserver.network.serverpackets.Interface.InterfaceConfigPacket;
import gameserver.network.serverpackets.Interface.InterfaceCustomFontsPacket;
import gameserver.network.serverpackets.Interface.InterfaceKeyPacket;
import gameserver.network.serverpackets.Interface.InterfaceScreenTextInfoPacket;

public class RequestKeyPacket extends GameClientPacket
{
	private byte[] data = null;
	private int data_size;

	@Override
	public void readImpl()
	{
		if(_buf.remaining() > 2)
		{
			data_size = readH();
			if(_buf.remaining() >= data_size)
			{
				data = new byte[data_size];
				readB(data);
			}
		}
	}

	@Override
	public void runImpl()
	{
		final var activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		
		activeChar.sendPacket(new InterfaceKeyPacket().sendKey(data, data_size));
		activeChar.sendPacket(new InterfaceConfigPacket());
		activeChar.sendPacket(new InterfaceCustomFontsPacket().sendFontInfos());
		activeChar.sendPacket(new InterfaceScreenTextInfoPacket().sendTextInfos());
	}
}