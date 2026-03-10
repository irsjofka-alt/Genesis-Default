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
package gameserver.listener.player.impl;

import gameserver.Config;
import gameserver.data.parser.ClanParser;
import gameserver.listener.player.OnAnswerListener;
import gameserver.model.Clan;
import gameserver.model.actor.Player;
import gameserver.model.strings.server.ServerMessage;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.SystemMessage;
import gameserver.utils.Util;

public class ClanLevelAnswerListener implements OnAnswerListener
{
	private final Player _player;
		
	public ClanLevelAnswerListener(Player player)
	{
		_player = player;
	}
		
	@Override
	public void sayYes()
	{
		if (_player != null && _player.isOnline() && _player.getClan() != null)
		{
			if (!ClanParser.getInstance().hasClanLevel(_player.getClan().getLevel() + 1))
			{
				_player.sendMessage(new ServerMessage("ServiceBBS.MAXLVL", _player.getLang()).toString());
				return;
			}
			
			if ((_player.getClanPrivileges() & Clan.CP_ALL) != Clan.CP_ALL)
			{
				_player.sendMessage((new ServerMessage("ServiceBBS.CLAN_LEADER", _player.getLang())).toString());
				return;
			}
			
			if (_player.getInventory().getItemByItemId(Config.SERVICES_CLANLVL_ITEM[0]) == null)
			{
				_player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
				return;
			}
			if (_player.getInventory().getItemByItemId(Config.SERVICES_CLANLVL_ITEM[0]).getCount() < Config.SERVICES_CLANLVL_ITEM[1])
			{
				_player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
				return;
			}
			_player.destroyItemByItemId("ClanlvlUpBBS", Config.SERVICES_CLANLVL_ITEM[0], Config.SERVICES_CLANLVL_ITEM[1], _player, true);
			Util.addServiceLog(_player.getName(null) + " buy lvl up for clan service!");
			_player.getClan().changeLevel((Config.LEARN_CLAN_MAX_LEVEL ? ClanParser.getInstance().getMaxLevel() : _player.getClan().getLevel() + 1), true);
			final var msg = new ServerMessage("ServiceBBS.CLAN_LVLUP", _player.getLang());
			msg.add(_player.getClan().getLevel());
			_player.sendMessage(msg.toString());
		}
	}
	
	@Override
	public void sayNo()
	{
	}
}