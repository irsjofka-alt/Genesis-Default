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
package gameserver.handler.chathandlers.impl;

import java.util.Collection;
import java.util.StringTokenizer;

import org.apache.commons.lang3.ArrayUtils;
import gameserver.Config;
import gameserver.handler.chathandlers.IChatHandler;
import gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import gameserver.handler.voicedcommandhandlers.VoicedCommandHandler;
import gameserver.model.World;
import gameserver.model.actor.Player;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.CreatureSay;

public class ChatAll implements IChatHandler
{
	private static final int[] COMMAND_IDS =
	{
	        0
	};
	
	@Override
	public void handleChat(int type, Player activeChar, String params, String text, boolean blockBroadCast)
	{
		if (text.startsWith("."))
		{
			final StringTokenizer st = new StringTokenizer(text);
			IVoicedCommandHandler vch;
			String command = "";

			if (st.countTokens() > 1)
			{
				command = st.nextToken().substring(1);
				params = text.substring(command.length() + 2);
				vch = VoicedCommandHandler.getInstance().getHandler(command);
			}
			else
			{
				command = text.substring(1);
				vch = VoicedCommandHandler.getInstance().getHandler(command);
			}
			
			if (vch != null)
			{
				if (!Config.DISABLE_VOICE_BYPASSES.contains(command))
				{
					vch.useVoicedCommand(command, activeChar, params);
				}
				return;
			}
		}
		
		if (activeChar.isChatBanned() && ArrayUtils.contains(Config.BAN_CHAT_CHANNELS, type))
		{
			activeChar.sendPacket(SystemMessageId.CHATTING_IS_CURRENTLY_PROHIBITED);
			return;
		}
		
		if (text.matches("\\.{1}[^\\.]+"))
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_SYNTAX);
		}
		else
		{
			final CreatureSay cs = new CreatureSay(activeChar.getObjectId(), type, activeChar.getAppearance().getVisibleName(), text);
			Collection<Player> plrs = null;
			if (activeChar.isInFightEvent())
			{
				plrs = activeChar.getFightEvent().getAllFightingPlayers();
			}
			else
			{
				plrs = World.getAroundPlayers(activeChar);
			}
			
			for (final Player player : plrs)
			{
				if (player != null && activeChar.isInsideRadius(player, 1250, false, true) && !player.getBlockList().isBlocked(activeChar) && !blockBroadCast)
				{
					if (activeChar.isInFightEvent() && player == activeChar)
					{
						continue;
					}
					player.sendPacket(cs);
				}
			}
			activeChar.sendPacket(cs);
		}
	}

	@Override
	public int[] getChatTypeList()
	{
		return COMMAND_IDS;
	}
}