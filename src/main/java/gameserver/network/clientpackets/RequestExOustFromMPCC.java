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

import gameserver.model.GameObjectsStorage;
import gameserver.model.actor.Player;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.SystemMessage;

public final class RequestExOustFromMPCC extends GameClientPacket
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
		final Player target = GameObjectsStorage.getPlayer(_name);
		final Player activeChar = getClient().getActiveChar();

		if ((target != null) && target.isInParty() && activeChar.isInParty() && activeChar.getParty().isInCommandChannel() && target.getParty().isInCommandChannel() && activeChar.getParty().getCommandChannel().getLeader().equals(activeChar) && activeChar.getParty().getCommandChannel().equals(target.getParty().getCommandChannel()))
		{
			if (activeChar.equals(target))
			{
				return;
			}

			target.getParty().getCommandChannel().removeParty(target.getParty());

			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DISMISSED_FROM_COMMAND_CHANNEL);
			target.getParty().broadCast(sm);

			if (activeChar.getParty().isInCommandChannel())
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.C1_PARTY_DISMISSED_FROM_COMMAND_CHANNEL);
				sm.addString(target.getParty().getLeader().getName(null));
				activeChar.getParty().getCommandChannel().broadCast(sm);
			}
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.TARGET_CANT_FOUND);
		}
	}
}