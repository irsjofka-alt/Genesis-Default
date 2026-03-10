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

import gameserver.data.holder.ClanHolder;
import gameserver.model.Clan;
import gameserver.model.actor.Player;
import gameserver.network.SystemMessageId;
import gameserver.network.clientpackets.GameClientPacket;
import gameserver.network.serverpackets.SystemMessage;

public final class RequestSurrenderPledgeWar extends GameClientPacket
{
	private String _pledgeName;
	private Clan _clan;
	private Player _activeChar;
	
	@Override
	protected void readImpl()
	{
		_pledgeName = readS();
	}

	@Override
	protected void runImpl()
	{
		_activeChar = getClient().getActiveChar();
		if (_activeChar == null)
		{
			return;
		}
		_clan = _activeChar.getClan();
		if (_clan == null)
		{
			return;
		}
		final Clan clan = ClanHolder.getInstance().getClanByName(_pledgeName);
		
		if (clan == null)
		{
			_activeChar.sendMessage("No such clan.");
			_activeChar.sendActionFailed();
			return;
		}
		
		_log.info("RequestSurrenderPledgeWar by " + getClient().getActiveChar().getClan().getName() + " with " + _pledgeName);
		
		if (!_clan.isAtWarWith(clan.getId()))
		{
			_activeChar.sendMessage("You aren't at war with this clan.");
			_activeChar.sendActionFailed();
			return;
		}
		SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_SURRENDERED_TO_THE_S1_CLAN);
		msg.addString(_pledgeName);
		_activeChar.sendPacket(msg);
		msg = null;
		_activeChar.deathPenalty(null, false, false, false);
		ClanHolder.getInstance().deleteclanswars(_clan.getId(), clan.getId());
	}
}