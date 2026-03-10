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
package gameserver.network.clientpackets.pledge;

import gameserver.Config;
import gameserver.data.holder.ClanHolder;
import gameserver.model.Clan;
import gameserver.model.actor.Player;
import gameserver.network.clientpackets.GameClientPacket;
import gameserver.network.serverpackets.pledge.PledgeInfo;

public final class RequestPledgeInfo extends GameClientPacket
{
	private int _clanId;
	
	@Override
	protected void readImpl()
	{
		_clanId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		if (Config.DEBUG)
		{
			_log.info("Info for clan " + _clanId + " requested");
		}
		
		final Player activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			return;
		}
		
		final Clan clan = ClanHolder.getInstance().getClan(_clanId);
		if (clan == null)
		{
			if (Config.DEBUG)
			{
				_log.warn("Clan data for clanId " + _clanId + " is missing for player " + activeChar.getName(null));
			}
			return;
		}
		
		final PledgeInfo pc = new PledgeInfo(clan);
		activeChar.sendPacket(pc);
		
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}