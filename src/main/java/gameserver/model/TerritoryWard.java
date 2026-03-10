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

import java.util.concurrent.Future;

import gameserver.Config;
import gameserver.ThreadPoolManager;
import gameserver.data.parser.ItemsParser;
import gameserver.instancemanager.TerritoryWarManager;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.items.instance.ItemInstance;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.InventoryUpdate;
import gameserver.network.serverpackets.SystemMessage;

public class TerritoryWard
{
	private Player _player = null;
	private ItemInstance _item = null;
	private Npc _npc = null;
	
	private Location _location;
	private Location _oldLocation;

	private final int _itemId;
	private int _ownerCastleId;
	
	private final int _territoryId;
	private Future<?> _returnToBase;
	
	public TerritoryWard(int territory_id, int x, int y, int z, int heading, int item_id, int castleId, Npc npc)
	{
		_territoryId = territory_id;
		_location = new Location(x, y, z, heading);
		_itemId = item_id;
		_ownerCastleId = castleId;
		_npc = npc;
	}

	public int getTerritoryId()
	{
		return _territoryId;
	}
	
	public int getOwnerCastleId()
	{
		return _ownerCastleId;
	}
	
	public void setOwnerCastleId(int newOwner)
	{
		_ownerCastleId = newOwner;
	}
	
	public Npc getNpc()
	{
		return _npc;
	}
	
	public void setNpc(Npc npc)
	{
		_npc = npc;
	}
	
	public Player getPlayer()
	{
		return _player;
	}
	
	public int getObjectId()
	{
		return _player != null ? _player.getObjectId() : 0;
	}
	
	public synchronized void spawnBack()
	{
		if (_player != null)
		{
			dropIt();
		}
		_npc = TerritoryWarManager.getInstance().spawnNPC(36491 + _territoryId, _oldLocation);
	}
	
	public synchronized void spawnMe()
	{
		if (_player != null)
		{
			dropIt();
		}
		_npc = TerritoryWarManager.getInstance().spawnNPC(36491 + _territoryId, _location);
	}
	
	public synchronized void unSpawnMe()
	{
		final var task = _returnToBase;
		if (task != null)
		{
			task.cancel(false);
			_returnToBase = null;
		}
		
		if (_player != null)
		{
			dropIt();
		}
		if ((_npc != null) && _npc.isVisible())
		{
			_npc.decayMe();
		}
	}
	
	public boolean activate(Player player, ItemInstance item)
	{
		if (player.isMounted() || player.isCombatFlagEquipped())
		{
			player.sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
			player.destroyItem("CombatFlag", item, null, true);
			spawnMe();
			return false;
		}
		else if (TerritoryWarManager.getInstance().getRegisteredTerritoryId(player) == 0)
		{
			player.sendMessage("Non participants can't pickup Territory Wards!");
			player.destroyItem("CombatFlag", item, null, true);
			spawnMe();
			return false;
		}
		
		if (!TerritoryWarManager.getInstance().canStealWard(player))
		{
			player.sendMessage("Your castle owns the maximum amount of wards!");
			player.destroyItem("CombatFlag", item, null, true);
			spawnMe();
			return false;
		}
		
		_player = player;
		_oldLocation = new Location(_npc.getX(), _npc.getY(), _npc.getZ(), _npc.getHeading());
		_npc = null;
		
		final var task = _returnToBase;
		if (task != null)
		{
			task.cancel(false);
			_returnToBase = null;
		}
		
		if (item == null)
		{
			_item = ItemsParser.getInstance().createItem("Combat", _itemId, 1, null, null);
		}
		else
		{
			_item = item;
		}
		_player.getInventory().equipItem(_item, true, true);
		final var sm = SystemMessage.getSystemMessage(SystemMessageId.S1_EQUIPPED);
		sm.addItemName(_item);
		_player.sendPacket(sm);
		
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			final var iu = new InventoryUpdate();
			iu.addItem(_item);
			_player.sendInventoryUpdate(iu);
		}
		else
		{
			_player.sendItemList(false);
		}
		
		_player.broadcastUserInfo(true);
		_player.setCombatFlagEquipped(true);
		_player.sendPacket(SystemMessageId.YOU_VE_ACQUIRED_THE_WARD);
		TerritoryWarManager.getInstance().giveTWPoint(player, _territoryId, 5);
		scheduleReturnToBase();
		return true;
	}
	
	public void dropIt()
	{
		if (_player == null)
		{
			return;
		}
		
		final var task = _returnToBase;
		if (task != null)
		{
			task.cancel(false);
			_returnToBase = null;
		}
		
		_player.setCombatFlagEquipped(false);
		if (_item != null)
		{
			final var slot = _player.getInventory().getSlotFromItem(_item);
			_player.getInventory().unEquipItemInBodySlot(slot, true);
			_player.destroyItem("TerritoryWard", _item, null, true);
		}
		_location = new Location(_player.getX(), _player.getY(), _player.getZ(), _player.getHeading());
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
			player.setCombatFlagEquipped(false);
			player.broadcastUserInfo(true);
		}
		_player = null;
		_item = null;
	}
	
	private void scheduleReturnToBase()
	{
		if (_returnToBase == null && TerritoryWarManager.TW_FLAG_TIME > 0)
		{
			_returnToBase = ThreadPoolManager.getInstance().schedule(() ->
			{
				spawnBack();
			}, TerritoryWarManager.TW_FLAG_TIME * 1000L);
		}
	}
}