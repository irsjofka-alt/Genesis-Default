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
import gameserver.model.actor.Player;
import gameserver.model.items.instance.ItemInstance;
import gameserver.model.strings.server.ServerMessage;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.InventoryUpdate;
import gameserver.network.serverpackets.SystemMessage;
import gameserver.utils.Util;

public class EnchantOlfAnswerListner implements OnAnswerListener
{
	private final Player _player;
	private final int _enchantLevel;

	public EnchantOlfAnswerListner(Player player, int enchantLevel)
	{
		_player = player;
		_enchantLevel = enchantLevel;
	}

	@Override
	public void sayYes()
	{
		if (_player == null)
		{
			return;
		}
		
		final long price;
		switch (_enchantLevel)
		{
			case 0 :
				price = Config.SERVICES_OLF_STORE_0_PRICE;
				break;
			case 6 :
				price = Config.SERVICES_OLF_STORE_6_PRICE;
				break;
			case 7 :
				price = Config.SERVICES_OLF_STORE_7_PRICE;
				break;
			case 8 :
				price = Config.SERVICES_OLF_STORE_8_PRICE;
				break;
			case 9 :
				price = Config.SERVICES_OLF_STORE_9_PRICE;
				break;
			case 10 :
				price = Config.SERVICES_OLF_STORE_10_PRICE;
				break;
			default :
				return;
		}
		
		if (_player.getInventory().getItemByItemId(Config.SERVICES_OLF_STORE_ITEM) == null)
		{
			_player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
			return;
		}
		if (_player.getInventory().getItemByItemId(Config.SERVICES_OLF_STORE_ITEM).getCount() < price)
		{
			_player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
			return;
		}
		
		_player.destroyItemByItemId("DonateOLFBBS", Config.SERVICES_OLF_STORE_ITEM, price, _player, true);
		
		final ItemInstance item = _player.getInventory().addItem("DonateOLFBBS", 21580, 1, _player, true);
		item.setEnchantLevel(_enchantLevel);
		Util.addServiceLog(_player.getName(null) + " buy OLF +" + _enchantLevel);
		final InventoryUpdate iu = new InventoryUpdate();
		iu.addModifiedItem(item);
		_player.sendPacket(iu);
		
		_player.getInventory().unEquipItemInBodySlot(item.getItem().getBodyPart(), true);
		_player.getInventory().equipItem(item, true, true);
		
		final ServerMessage msg = new ServerMessage("ServiceBBS.YOU_BUY_ITEM", _player.getLang());
		msg.add(item.getItem().getName(_player.getLang()));
		_player.sendMessage(msg.toString());
	}
	
	@Override
	public void sayNo()
	{
	}
}
