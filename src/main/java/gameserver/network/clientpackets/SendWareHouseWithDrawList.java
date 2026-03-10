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
package gameserver.network.clientpackets;

import gameserver.Config;
import gameserver.model.Clan;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.holders.ItemHolder;
import gameserver.model.items.instance.ItemInstance;
import gameserver.model.items.itemcontainer.ClanWarehouse;
import gameserver.model.items.itemcontainer.ItemContainer;
import gameserver.model.items.itemcontainer.PcWarehouse;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.InventoryUpdate;
import gameserver.network.serverpackets.StatusUpdate;
import gameserver.utils.Log;
import gameserver.utils.Util;

public final class SendWareHouseWithDrawList extends GameClientPacket
{
	private static final int BATCH_LENGTH = 12;
	
	private ItemHolder _items[] = null;

	@Override
	protected void readImpl()
	{
		final int count = readD();
		if ((count <= 0) || (count > Config.MAX_ITEM_IN_PACKET) || ((count * BATCH_LENGTH) != _buf.remaining()))
		{
			return;
		}
		
		_items = new ItemHolder[count];
		for (int i = 0; i < count; i++)
		{
			final int objId = readD();
			final long cnt = readQ();
			if ((objId < 1) || (cnt < 0))
			{
				_items = null;
				return;
			}
			_items[i] = new ItemHolder(objId, cnt);
		}
	}
	
	@Override
	protected void runImpl()
	{
		if (_items == null)
		{
			return;
		}
		
		final Player player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		if (player.isActionsDisabled() || player.getActiveTradeList() != null)
		{
			player.sendActionFailed();
			return;
		}
		
		final ItemContainer warehouse = player.getActiveWarehouse();
		if (warehouse == null)
		{
			return;
		}
		
		final Npc manager = player.getLastFolkNPC();
		if (manager != null && manager.isWarehouse())
		{
			if (!manager.canInteract(player))
			{
				return;
			}
		}
		
		if (!(warehouse instanceof PcWarehouse) && !player.getAccessLevel().allowTransaction())
		{
			player.sendMessage("Transactions are disabled for your Access Level.");
			return;
		}
		
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE && (player.getKarma() > 0))
		{
			return;
		}
		
		if (Config.ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH)
		{
			if ((warehouse instanceof ClanWarehouse) && ((player.getClanPrivileges() & Clan.CP_CL_VIEW_WAREHOUSE) != Clan.CP_CL_VIEW_WAREHOUSE))
			{
				return;
			}
		}
		else
		{
			if ((warehouse instanceof ClanWarehouse) && !player.isClanLeader())
			{
				player.sendPacket(SystemMessageId.ONLY_CLAN_LEADER_CAN_RETRIEVE_ITEMS_FROM_CLAN_WAREHOUSE);
				return;
			}
		}
		
		int weight = 0;
		int slots = 0;
		
		for (final ItemHolder i : _items)
		{
			final ItemInstance item = warehouse.getItemByObjectId(i.getId());
			if ((item == null) || (item.getCount() < i.getCount()))
			{
				Util.handleIllegalPlayerAction(player, "" + player.getName(null) + " of account " + player.getAccountName() + " tried to withdraw non-existent item from warehouse.");
				return;
			}
			
			weight += i.getCount() * item.getItem().getWeight();
			if (!item.isStackable())
			{
				slots += i.getCount();
			}
			else if (player.getInventory().getItemByItemId(item.getId()) == null)
			{
				slots++;
			}
		}
		
		if (!player.getInventory().validateCapacity(slots))
		{
			player.sendPacket(SystemMessageId.SLOTS_FULL);
			return;
		}
		
		if (!player.getInventory().validateWeight(weight))
		{
			player.sendPacket(SystemMessageId.WEIGHT_LIMIT_EXCEEDED);
			return;
		}
		
		final InventoryUpdate playerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
		for (final ItemHolder i : _items)
		{
			final ItemInstance oldItem = warehouse.getItemByObjectId(i.getId());
			if ((oldItem == null) || (oldItem.getCount() < i.getCount()))
			{
				_log.warn("Error withdrawing a warehouse object for char " + player.getName(null) + " (olditem == null)");
				return;
			}
			final ItemInstance newItem = warehouse.transferItem(warehouse.getName(), i.getId(), i.getCount(), player.getInventory(), player, manager);
			if (newItem == null)
			{
				_log.warn("Error withdrawing a warehouse object for char " + player.getName(null) + " (newitem == null)");
				return;
			}
			Log.addLogItem(getClass().getSimpleName(), player.getName(null) + " -> get from " + warehouse.getName() + " ->", newItem.getName(null), newItem.getCount(), newItem.getEnchantLevel(), null);
			
			if (playerIU != null)
			{
				if (newItem.getCount() > i.getCount())
				{
					playerIU.addModifiedItem(newItem);
				}
				else
				{
					playerIU.addNewItem(newItem);
				}
			}
		}
		
		if (playerIU != null)
		{
			player.sendPacket(playerIU);
		}
		else
		{
			player.sendItemList(false);
		}
		player.sendStatusUpdate(false, false, StatusUpdate.CUR_LOAD);
	}
}