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
package gameserver.handler.usercommandhandlers.impl;

import gameserver.handler.usercommandhandlers.IUserCommandHandler;
import gameserver.model.CommandChannel;
import gameserver.model.Party;
import gameserver.model.actor.Player;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.SystemMessage;

public class ChannelLeave implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
	        96
	};
	
	@Override
	public boolean useUserCommand(int id, Player activeChar)
	{
		if (id != COMMAND_IDS[0])
		{
			return false;
		}

		if (!activeChar.isInParty() || !activeChar.getParty().isLeader(activeChar))
		{
			activeChar.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_LEAVE_CHANNEL);
			return false;
		}

		if (activeChar.getParty().isInCommandChannel())
		{
			final CommandChannel channel = activeChar.getParty().getCommandChannel();
			final Party party = activeChar.getParty();
			channel.removeParty(party);
			party.getLeader().sendPacket(SystemMessageId.LEFT_COMMAND_CHANNEL);

			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_PARTY_LEFT_COMMAND_CHANNEL);
			sm.addPcName(party.getLeader());
			channel.broadCast(sm);
			return true;
		}
		return false;

	}

	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}