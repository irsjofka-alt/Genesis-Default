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

import org.apache.commons.lang3.ArrayUtils;
import gameserver.Config;
import gameserver.handler.chathandlers.IChatHandler;
import gameserver.instancemanager.MapRegionManager;
import gameserver.model.GameObjectsStorage;
import gameserver.model.PcCondOverride;
import gameserver.model.actor.Player;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.CreatureSay;

public class ChatTrade implements IChatHandler
{
	private static final int[] COMMAND_IDS =
	{
	        8
	};

	@Override
	public void handleChat(int type, Player activeChar, String target, String text, boolean blockBroadCast)
	{
		if (activeChar.isChatBanned() && ArrayUtils.contains(Config.BAN_CHAT_CHANNELS, type))
		{
			activeChar.sendPacket(SystemMessageId.CHATTING_IS_CURRENTLY_PROHIBITED);
			return;
		}
		
		final CreatureSay cs = new CreatureSay(activeChar.getObjectId(), type, activeChar.getName(null), text);
		if (blockBroadCast)
		{
			activeChar.sendPacket(cs);
			return;
		}
		
		if (Config.DEFAULT_TRADE_CHAT.equalsIgnoreCase("on") || (Config.DEFAULT_TRADE_CHAT.equalsIgnoreCase("gm") && activeChar.isGM()))
		{
			final int region = MapRegionManager.getInstance().getMapRegionLocId(activeChar);
			for (final Player player : GameObjectsStorage.getPlayers())
			{
				if (region == MapRegionManager.getInstance().getMapRegionLocId(player) && !player.getBlockList().isBlocked(activeChar) && player.getReflectionId() == activeChar.getReflectionId())
				{
					player.sendPacket(cs);
				}
			}
		}
		else if (Config.DEFAULT_TRADE_CHAT.equalsIgnoreCase("global"))
		{
			if (!activeChar.canOverrideCond(PcCondOverride.CHAT_CONDITIONS) && !activeChar.checkFloodProtection("TRADECHAT", "trade_chat"))
			{
				activeChar.sendMessage("Do not spam trade channel.");
				return;
			}
			
			for (final Player player : GameObjectsStorage.getPlayers())
			{
				if (!player.getBlockList().isBlocked(activeChar))
				{
					player.sendPacket(cs);
				}
			}
		}
	}
	
	@Override
	public int[] getChatTypeList()
	{
		return COMMAND_IDS;
	}
}