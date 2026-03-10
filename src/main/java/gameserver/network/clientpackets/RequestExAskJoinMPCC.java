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

import gameserver.model.CommandChannel;
import gameserver.model.GameObjectsStorage;
import gameserver.model.Party;
import gameserver.model.actor.Player;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.ExAskJoinMPCC;
import gameserver.network.serverpackets.SystemMessage;

public final class RequestExAskJoinMPCC extends GameClientPacket
{
	private String _name;
	
	@Override
	protected void readImpl()
	{
		_name = readS();
	}

	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		final Player target = GameObjectsStorage.getPlayer(_name);
		if (target == null)
		{
			activeChar.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET);
			return;
		}
		
		final Player resultTarget = CommandChannel.checkAndAskToCreateChannel(activeChar, target);
		
		final Party activeParty = activeChar.getParty();
		
		if (resultTarget != null)
		{
			if (activeParty.isInCommandChannel())
			{
				if (activeParty.getCommandChannel().getLeader() != activeChar)
				{
					activeChar.sendPacket(SystemMessageId.CANNOT_INVITE_TO_COMMAND_CHANNEL);
					return;
				}
				
				activeChar.onTransactionRequest(target);
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.COMMAND_CHANNEL_CONFIRM_FROM_C1);
				sm.addString(activeChar.getName(null));
				target.sendPacket(sm);
				target.sendPacket(new ExAskJoinMPCC(activeChar.getName(null)));
			}
			else
			{
				activeChar.onTransactionRequest(target);
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.COMMAND_CHANNEL_CONFIRM_FROM_C1);
				sm.addString(activeChar.getName(null));
				target.sendPacket(sm);
				target.sendPacket(new ExAskJoinMPCC(activeChar.getName(null)));
			}
		}
	}
}