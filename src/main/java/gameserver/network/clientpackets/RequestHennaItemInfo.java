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

import gameserver.data.parser.HennaParser;
import gameserver.model.actor.Player;
import gameserver.model.actor.templates.items.Henna;
import gameserver.network.serverpackets.HennaItemInfo;

public final class RequestHennaItemInfo extends GameClientPacket
{
	private int _symbolId;
	
	@Override
	protected void readImpl()
	{
		_symbolId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		final Henna henna = HennaParser.getInstance().getHenna(_symbolId);
		if (henna == null)
		{
			if (_symbolId != 0)
			{
				_log.warn(getClass().getSimpleName() + ": Invalid Henna Id: " + _symbolId + " from player " + activeChar);
			}
			sendActionFailed();
			return;
		}
		activeChar.sendPacket(new HennaItemInfo(henna, activeChar));
	}
}