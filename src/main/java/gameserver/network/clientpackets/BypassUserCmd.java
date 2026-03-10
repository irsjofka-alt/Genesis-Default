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

import gameserver.handler.usercommandhandlers.IUserCommandHandler;
import gameserver.handler.usercommandhandlers.UserCommandHandler;
import gameserver.model.actor.Player;

public class BypassUserCmd extends GameClientPacket
{
	private int _command;
	
	@Override
	protected void readImpl()
	{
		_command = readD();
	}

	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		final IUserCommandHandler handler = UserCommandHandler.getInstance().getHandler(_command);
		
		if (handler == null)
		{
			if (player.isGM())
			{
				player.sendMessage("User commandID " + _command + " not implemented yet.");
			}
		}
		else
		{
			handler.useUserCommand(_command, getClient().getActiveChar());
		}
	}
}