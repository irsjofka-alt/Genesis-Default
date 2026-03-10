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

import java.util.Collection;

import gameserver.Config;
import gameserver.handler.communityhandlers.impl.CommunityGeneral;
import gameserver.listener.player.OnAnswerListener;
import gameserver.model.actor.Player;
import gameserver.model.base.SubClass;
import gameserver.model.olympiad.Olympiad;
import gameserver.model.strings.server.ServerMessage;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.ShowBoard;
import gameserver.network.serverpackets.SystemMessage;

public class ChangeMainClassListener implements OnAnswerListener
{
	private final Player _player;
	private final int _classIndex;
	
	public ChangeMainClassListener(Player player, int classIndex)
	{
		_player = player;
		_classIndex = classIndex;
	}
	
	@Override
	public void sayYes()
	{
		if (_player != null)
		{
			final var points = Olympiad.getInstance().getNoblePoints(_player.getObjectId());
			final var fights = Olympiad.getInstance().getNobleFights(_player.getObjectId());
			if ((points != Config.ALT_OLY_START_POINTS || fights > 0) && !Config.CHANGE_MAIN_CLASS_WITHOUT_OLY_CHECK)
			{
				_player.sendMessage((new ServerMessage("ServiceBBS.CHANGE_CLASS_WRONG_POINTS", _player.getLang())).toString());
				return;
			}
			
			if (_player.getClassId().level() != 3)
			{
				_player.sendMessage((new ServerMessage("ServiceBBS.CHANGE_CLASS_WRONG_CLASS", _player.getLang())).toString());
				return;
			}
			
			if (_player.isHero())
			{
				_player.sendMessage((new ServerMessage("ServiceBBS.CHANGE_CLASS_HERO", _player.getLang())).toString());
				return;
			}
			
			if (_player.isSubClassActive())
			{
				_player.sendMessage((new ServerMessage("ServiceBBS.CHANGE_CLASS_ACTIVESUB", _player.getLang())).toString());
				return;
			}
			
			final Collection<SubClass> allSubs = _player.getSubClasses().values();
			if (allSubs.isEmpty())
			{
				_player.sendMessage((new ServerMessage("ServiceBBS.CHANGE_CLASS_HAVENT_SUBS", _player.getLang())).toString());
				return;
			}
			
			final var subClass = _player.getSubClasses().get(_classIndex);
			if (subClass == null)
			{
				return;
			}
			
			if (_player.getInventory().getItemByItemId(Config.SERVICES_CHANGE_MAIN_CLASS[0]).getCount() < Config.SERVICES_CHANGE_MAIN_CLASS[1])
			{
				_player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
				return;
			}
			
			if (CommunityGeneral.getInstance().activateSubClassAsBase(_player, _classIndex, subClass))
			{
				final var newClass = subClass.getClassId();
				final var newExp = subClass.getExp();
				final var newSp = subClass.getSp();
				final var newLevel = subClass.getLevel();
				
				_player.destroyItemByItemId("ExpandInventory", Config.SERVICES_CHANGE_MAIN_CLASS[0], Config.SERVICES_CHANGE_MAIN_CLASS[1], _player, true);
				subClass.setClassId(_player.getClassId().getId());
				subClass.setExp(_player.getExp());
				subClass.setSp(_player.getSp());
				subClass.setLevel(_player.getLevel());
				_player.setBaseClass(newClass);
				_player.setClassId(newClass);
				_player.setExp(newExp);
				_player.setSp(newSp);
				_player.getStat().setLevel(newLevel);
				_player.setActiveClass(0);
				Olympiad.getInstance().updateNobleClass(_player);
				if (Config.CHANGE_MAIN_CLASS_WITHOUT_OLY_CHECK)
				{
					final var stats = Olympiad.getNobleStats(_player.getObjectId());
					if (stats != null)
					{
						stats.set("olympiad_points", 0);
					}
				}
				_player.sendPacket(new ShowBoard());
			}
		}
	}
	
	@Override
	public void sayNo()
	{
	}
}