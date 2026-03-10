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
import gameserver.model.actor.Player;
import gameserver.network.SystemMessageId;
import gameserver.network.clientpackets.GameClientPacket;

public final class RequestReplyStopPledgeWar extends GameClientPacket
{
	private int _answer;
	protected String _reqName;
	
	@Override
	protected void readImpl()
	{
		_reqName = readS();
		_answer = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		final Player requestor = activeChar.getActiveRequester();
		if (requestor == null)
		{
			return;
		}
		
		if (_answer == 1)
		{
			ClanHolder.getInstance().deleteclanswars(requestor.getClanId(), activeChar.getClanId());
		}
		else
		{
			requestor.sendPacket(SystemMessageId.REQUEST_TO_END_WAR_HAS_BEEN_DENIED);
		}
		activeChar.setActiveRequester(null);
		requestor.onTransactionResponse();
	}
}