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
package gameserver.model;

import gameserver.Config;
import gameserver.data.parser.ItemsParser;
import gameserver.model.actor.Player;
import gameserver.model.items.instance.ItemInstance;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.InventoryUpdate;
import gameserver.network.serverpackets.SystemMessage;

public class CombatFlag
{
	private Player _player = null;
	private ItemInstance _item = null;
	private ItemInstance _itemInstance;
	private final Location _location;
	private final int _itemId;
	protected final int _fortId;

	public CombatFlag(int fort_id, int x, int y, int z, int heading, int item_id)
	{
		_fortId = fort_id;
		_location = new Location(x, y, z, heading);
		_itemId = item_id;
	}
	
	public synchronized void spawnMe()
	{
		_itemInstance = ItemsParser.getInstance().createItem("Combat", _itemId, 1, null, null);
		_itemInstance.dropMe(null, _location.getX(), _location.getY(), _location.getZ(), true);
	}
	
	public synchronized void unSpawnMe()
	{
		if (_player != null)
		{
			dropIt();
		}
		if (_itemInstance != null)
		{
			_itemInstance.decayMe();
		}
	}
	
	public boolean activate(Player player, ItemInstance item)
	{
		if (player.isMounted() || player.isCombatFlagEquipped())
		{
			player.sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
			return false;
		}
		
		_player = player;
		_itemInstance = null;
		
		_item = item;
		_player.getInventory().equipItem(_item, true, true);
		final var sm = SystemMessage.getSystemMessage(SystemMessageId.S1_EQUIPPED);
		sm.addItemName(_item);
		_player.sendPacket(sm);
		
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			final InventoryUpdate iu = new InventoryUpdate();
			iu.addItem(_item);
			_player.sendInventoryUpdate(iu);
		}
		else
		{
			_player.sendItemList(false);
		}
		_player.broadcastUserInfo(true);
		_player.setCombatFlagEquipped(true);
		return true;
	}
	
	public void dropIt()
	{
		if (_player == null)
		{
			return;
		}
		
		_player.setCombatFlagEquipped(false);
		if (_item != null)
		{
			final var slot = _player.getInventory().getSlotFromItem(_item);
			_player.getInventory().unEquipItemInBodySlot(slot, true);
			_player.destroyItem("CombatFlag", _item, null, true);
		}
		checkFlag(_player);
	}
	
	private void checkFlag(Player player)
	{
		if (player != null)
		{
			final var flag = player.getInventory().getItemByItemId(_itemId);
			if (flag != null)
			{
				final var unequipped = player.getInventory().unEquipItemInBodySlotAndRecord(flag.getItem().getBodyPart(), true);
				final var iu = new InventoryUpdate();
				for (final var unequippedItem : unequipped)
				{
					iu.addModifiedItem(unequippedItem);
				}
				player.sendInventoryUpdate(iu);
				
				final var destroyItem = player.getInventory().destroyItem("TerritoryWard", flag, player, null);
				if (destroyItem != null)
				{
					final var u = new InventoryUpdate();
					u.addRemovedItem(destroyItem);
					player.sendInventoryUpdate(u);
				}
			}
			player.broadcastUserInfo(true);
		}
		_player = null;
		_item = null;
	}
	
	public int getObjectId()
	{
		return _player != null ? _player.getObjectId() : 0;
	}
	
	public ItemInstance getCombatFlag()
	{
		return _itemInstance;
	}
}