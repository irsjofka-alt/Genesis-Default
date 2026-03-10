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

import static gameserver.model.items.itemcontainer.PcInventory.ADENA_ID;

import java.util.ArrayList;
import java.util.List;

import gameserver.Config;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.holders.ItemHolder;
import gameserver.model.items.instance.ItemInstance;
import gameserver.model.items.itemcontainer.ItemContainer;
import gameserver.model.items.itemcontainer.PcWarehouse;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.InventoryUpdate;
import gameserver.network.serverpackets.StatusUpdate;
import gameserver.utils.Log;
import gameserver.utils.Util;

public final class SendWareHouseDepositList extends GameClientPacket
{
	private static final int BATCH_LENGTH = 12;

	private List<ItemHolder> _items = null;
	
	@Override
	protected void readImpl()
	{
		final int size = readD();
		if ((size <= 0) || (size > Config.MAX_ITEM_IN_PACKET) || ((size * BATCH_LENGTH) != _buf.remaining()))
		{
			return;
		}

		_items = new ArrayList<>(size);
		for (int i = 0; i < size; i++)
		{
			final int objId = readD();
			final long count = readQ();
			if ((objId < 1) || (count < 0))
			{
				_items = null;
				return;
			}
			_items.add(new ItemHolder(objId, count));
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
		final boolean isPrivate = warehouse instanceof PcWarehouse;

		final Npc manager = player.getLastFolkNPC();
		if (manager != null && manager.isWarehouse())
		{
			if (!manager.canInteract(player))
			{
				return;
			}
		}

		if (!isPrivate && !player.getAccessLevel().allowTransaction())
		{
			player.sendMessage("Transactions are disabled for your Access Level.");
			return;
		}

		if (player.getActiveEnchantItemId() != Player.ID_NONE)
		{
			Util.handleIllegalPlayerAction(player, "" + player.getName(null) + " tried to use enchant Exploit!");
			return;
		}

		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE && (player.getKarma() > 0))
		{
			return;
		}

		final long fee = _items.size() * 30;
		long currentAdena = player.getAdena();
		int slots = 0;

		for (final ItemHolder i : _items)
		{
			final ItemInstance item = player.checkItemManipulation(i.getId(), i.getCount(), "deposit");
			if (item == null)
			{
				Util.handleIllegalPlayerAction(player, "Error depositing a warehouse object for char " + player.getName(null) + " (validity check)!");
				if (Config.DEBUG)
				{
					_log.warn("Error depositing a warehouse object for char " + player.getName(null) + " (validity check)");
				}
				return;
			}

			if (item.getId() == ADENA_ID)
			{
				currentAdena -= i.getCount();
			}
			if (!item.isStackable())
			{
				slots += i.getCount();
			}
			else if (warehouse.getItemByItemId(item.getId()) == null)
			{
				slots++;
			}
		}

		if (!warehouse.validateCapacity(slots))
		{
			player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
			return;
		}

		if ((currentAdena < fee) || !player.reduceAdena(warehouse.getName(), fee, manager, false))
		{
			player.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
			return;
		}

		if (player.getActiveTradeList() != null)
		{
			return;
		}

		final InventoryUpdate playerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
		for (final ItemHolder i : _items)
		{
			final ItemInstance oldItem = player.checkItemManipulation(i.getId(), i.getCount(), "deposit");
			if (oldItem == null)
			{
				_log.warn("Error depositing a warehouse object for char " + player.getName(null) + " (olditem == null)");
				return;
			}

			if (!oldItem.isDepositable(isPrivate) || !oldItem.isAvailable(player, true, isPrivate))
			{
				continue;
			}

			final ItemInstance newItem = player.getInventory().transferItem(warehouse.getName(), i.getId(), i.getCount(), warehouse, player, manager);
			if (newItem == null)
			{
				_log.warn("Error depositing a warehouse object for char " + player.getName(null) + " (newitem == null)");
				continue;
			}
			
			Log.addLogItem(getClass().getSimpleName(), player.getName(null) + " -> add to " + warehouse.getName() + " ->", newItem.getName(null), newItem.getCount(), newItem.getEnchantLevel(), null);
			
			if (playerIU != null)
			{
				if ((oldItem.getCount() > 0) && (oldItem != newItem))
				{
					playerIU.addModifiedItem(oldItem);
				}
				else
				{
					playerIU.addRemovedItem(oldItem);
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