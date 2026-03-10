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
package gameserver.handler.voicedcommandhandlers.impl;

import gameserver.Config;
import gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import gameserver.model.GameObjectsStorage;
import gameserver.model.actor.Player;

public class Online implements IVoicedCommandHandler
{
	private static String[] _voicedCommands =
	{
	        "online"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if (command.equalsIgnoreCase("online"))
		{
			if (Config.VOICE_ONLINE_ENABLE)
			{
				final int currentOnline = (int) (GameObjectsStorage.getAllPlayersCount() * Config.FAKE_ONLINE) + Config.FAKE_ONLINE_MULTIPLIER;
				activeChar.sendMessage("Total online: " + currentOnline + " players.");
			}
		}
		return true;
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}