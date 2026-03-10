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

import gameserver.model.actor.Player;
import gameserver.model.actor.templates.items.Item;
import gameserver.model.base.Race;
import gameserver.model.items.instance.ItemInstance;
import gameserver.model.items.itemcontainer.PcInventory;
import gameserver.model.skills.Skill;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.InventoryUpdate;
import gameserver.network.serverpackets.SystemMessage;
import gameserver.utils.Log;
import gameserver.utils.Util;

public final class RequestCrystallizeItem extends GameClientPacket
{
	private int _objectId;
	private long _count;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_count = readQ();
	}

	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (activeChar.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}

		if (_count <= 0)
		{
			Util.handleIllegalPlayerAction(activeChar, "[RequestCrystallizeItem] count <= 0! ban! oid: " + _objectId + " owner: " + activeChar.getName(null));
			return;
		}

		if ((activeChar.getPrivateStoreType() != Player.STORE_PRIVATE_NONE) || activeChar.isInCrystallize())
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE);
			return;
		}

		final int skillLevel = activeChar.getSkillLevel(Skill.SKILL_CRYSTALLIZE);
		if (skillLevel <= 0)
		{
			activeChar.sendPacket(SystemMessageId.CRYSTALLIZE_LEVEL_TOO_LOW);
			activeChar.sendActionFailed();
			if ((activeChar.getRace() != Race.DWARF) && (activeChar.getClassId().getId() != 117) && (activeChar.getClassId().getId() != 55))
			{
				_log.info("Player " + activeChar.getClient() + " used crystalize with classid: " + activeChar.getClassId().getId());
			}
			return;
		}

		final PcInventory inventory = activeChar.getInventory();
		if (inventory != null)
		{
			final ItemInstance item = inventory.getItemByObjectId(_objectId);
			if (item == null)
			{
				activeChar.sendActionFailed();
				return;
			}

			if (item.isHeroItem())
			{
				return;
			}

			if (_count > item.getCount())
			{
				_count = activeChar.getInventory().getItemByObjectId(_objectId).getCount();
			}
		}

		final ItemInstance itemToRemove = activeChar.getInventory().getItemByObjectId(_objectId);
		if ((itemToRemove == null) || itemToRemove.isShadowItem() || itemToRemove.isTimeLimitedItem() || itemToRemove.isEventItem())
		{
			return;
		}

		if (!itemToRemove.getItem().isCrystallizable() || (itemToRemove.getItem().getCrystalCount() <= 0) || (itemToRemove.getItem().getCrystalType() == Item.CRYSTAL_NONE))
		{
			Util.handleIllegalPlayerAction(activeChar, "" + activeChar.getName(null) + " tried to crystallize " + itemToRemove.getItem().getId());
			return;
		}

		if (!activeChar.getInventory().canManipulateWithItemId(itemToRemove.getId()))
		{
			activeChar.sendMessage("You cannot use this item.");
			return;
		}
		boolean canCrystallize = true;

		switch (itemToRemove.getItem().getItemGradeSPlus())
		{
			case Item.CRYSTAL_C :
			{
				if (skillLevel <= 1)
				{
					canCrystallize = false;
				}
				break;
			}
			case Item.CRYSTAL_B :
			{
				if (skillLevel <= 2)
				{
					canCrystallize = false;
				}
				break;
			}
			case Item.CRYSTAL_A :
			{
				if (skillLevel <= 3)
				{
					canCrystallize = false;
				}
				break;
			}
			case Item.CRYSTAL_S :
			{
				if (skillLevel <= 4)
				{
					canCrystallize = false;
				}
				break;
			}
		}

		if (!canCrystallize)
		{
			activeChar.sendPacket(SystemMessageId.CRYSTALLIZE_LEVEL_TOO_LOW);
			activeChar.sendActionFailed();
			return;
		}

		activeChar.setInCrystallize(true);

		SystemMessage sm;
		if (itemToRemove.isEquipped())
		{
			final var unequiped = activeChar.getInventory().unEquipItemInSlotAndRecord(itemToRemove.getLocationSlot(), true);
			final InventoryUpdate iu = new InventoryUpdate();
			for (final ItemInstance item : unequiped)
			{
				iu.addModifiedItem(item);
			}
			activeChar.sendPacket(iu);

			if (itemToRemove.getEnchantLevel() > 0)
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
				sm.addNumber(itemToRemove.getEnchantLevel());
				sm.addItemName(itemToRemove);
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED);
				sm.addItemName(itemToRemove);
			}
			activeChar.sendPacket(sm);
		}
		final ItemInstance removedItem = activeChar.getInventory().destroyItem("Crystalize", _objectId, _count, activeChar, null);

		final InventoryUpdate iu = new InventoryUpdate();
		iu.addRemovedItem(removedItem);
		activeChar.sendPacket(iu);

		final int crystalId = itemToRemove.getItem().getCrystalItemId();
		final int crystalAmount = itemToRemove.getCrystalCount();
		final ItemInstance createditem = activeChar.getInventory().addItem("Crystalize", crystalId, crystalAmount, activeChar, activeChar);

		sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CRYSTALLIZED);
		sm.addItemName(removedItem);
		activeChar.sendPacket(sm);

		sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
		sm.addItemName(createditem);
		sm.addItemNumber(crystalAmount);
		activeChar.sendPacket(sm);

		activeChar.broadcastCharInfo();

		activeChar.setInCrystallize(false);
		Log.addLogItem(getClass().getSimpleName(), activeChar.getName(null) + " -> destroy ->", removedItem.getName(null), removedItem.getCount(), removedItem.getEnchantLevel(), null);
		Log.addLogItem(getClass().getSimpleName(), activeChar.getName(null) + " -> receive ->", createditem.getName(null), createditem.getCount(), 0, null);
	}
}