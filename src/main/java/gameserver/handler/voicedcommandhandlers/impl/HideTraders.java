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
import gameserver.model.actor.Player;
import gameserver.model.strings.server.ServerMessage;

public class HideTraders implements IVoicedCommandHandler
{
	private static final String[] _voicedCommands =
	{
	        "notraders"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if (!Config.ALLOW_HIDE_TRADERS_COMMAND)
		{
			return false;
		}
		
		if (activeChar.getNotShowTraders())
		{
			activeChar.setVar("useHideTraders@", "0");
			activeChar.restorePrivateStores();
			activeChar.sendMessage((new ServerMessage("HideTraders.DISABLED", activeChar.getLang())).toString());
		}
		else
		{
			activeChar.setVar("useHideTraders@", "1");
			activeChar.hidePrivateStores();
			activeChar.sendMessage((new ServerMessage("HideTraders.ENABLED", activeChar.getLang())).toString());
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}