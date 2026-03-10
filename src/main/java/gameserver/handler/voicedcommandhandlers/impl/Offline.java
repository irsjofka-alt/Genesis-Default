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
import gameserver.listener.player.impl.OfflineAnswerListener;
import gameserver.listener.player.impl.OfflineFarmAnswerListener;
import gameserver.model.actor.Player;
import gameserver.model.service.autofarm.FarmSettings;
import gameserver.model.strings.server.ServerMessage;

public class Offline implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
	        "offline", "offlinefarm"
	};

	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String params)
	{
		if (command.equalsIgnoreCase("offline"))
		{
			if (!Config.ALLOW_OFFLINE_COMMAND)
			{
				return false;
			}
			
			if (!activeChar.canOfflineMode(activeChar, true))
			{
				activeChar.sendMessage((new ServerMessage("Community.ALL_DISABLE", activeChar.getLang())).toString());
				return false;
			}
			activeChar.sendConfirmDlg(new OfflineAnswerListener(activeChar), 15000, new ServerMessage("Offline.CHOCICE", activeChar.getLang()).toString());
		}
		else if (command.equalsIgnoreCase("offlinefarm"))
		{
			if (!FarmSettings.ALLOW_OFFLINE)
			{
				return false;
			}
			
			if (!activeChar.canOfflineFarmMode())
			{
				activeChar.sendMessage((new ServerMessage("Community.ALL_DISABLE", activeChar.getLang())).toString());
				return false;
			}
			activeChar.sendConfirmDlg(new OfflineFarmAnswerListener(activeChar), 15000, new ServerMessage("Offline.CHOCICE", activeChar.getLang()).toString());
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}