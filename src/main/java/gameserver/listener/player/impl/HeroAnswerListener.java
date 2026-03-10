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
import gameserver.listener.player.OnAnswerListener;
import gameserver.model.ClanMember;
import gameserver.model.actor.Player;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.SocialAction;
import gameserver.network.serverpackets.SystemMessage;
import gameserver.utils.Util;

public class HeroAnswerListener implements OnAnswerListener
{
	private final Player _player;
		
	public HeroAnswerListener(Player player)
	{
		_player = player;
	}
		
	@Override
	public void sayYes()
	{
		if (_player != null && _player.isOnline())
		{
			if (_player.isHero())
			{
				return;
			}
			
			if (_player.getInventory().getItemByItemId(Config.SERVICES_GIVEHERO_ITEM[0]) == null)
			{
				_player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
				return;
			}
			if (_player.getInventory().getItemByItemId(Config.SERVICES_GIVEHERO_ITEM[0]).getCount() < Config.SERVICES_GIVEHERO_ITEM[1])
			{
				_player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
				return;
			}
			
			_player.destroyItemByItemId("SetHeroBBS", Config.SERVICES_GIVEHERO_ITEM[0], Config.SERVICES_GIVEHERO_ITEM[1], _player, true);
			Util.addServiceLog(_player.getName(null) + " buy hero status!");
			final var endTime = System.currentTimeMillis() + (Config.SERVICES_GIVEHERO_TIME * 60000L);
			_player.setVar("tempHero", String.valueOf(endTime));
			_player.setHero(true, Config.SERVICES_GIVEHERO_SKILLS);
			_player.broadcastPacket(new SocialAction(_player.getObjectId(), 16));
			if (Config.SERVICES_GIVEHERO_SKILLS)
			{
				_player.setVar("tempHeroSkills", "1");
			}
			_player.startTempHeroTask(endTime, Config.SERVICES_GIVEHERO_SKILLS ? 1 : 0);
			if (_player.getClan() != null)
			{
				_player.setPledgeClass(ClanMember.calculatePledgeClass(_player));
			}
			else
			{
				_player.setPledgeClass(8);
			}
			_player.broadcastUserInfo(true);
		}
	}
	
	@Override
	public void sayNo()
	{
	}
}