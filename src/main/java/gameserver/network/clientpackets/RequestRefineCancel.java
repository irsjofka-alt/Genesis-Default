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

import gameserver.data.parser.AugmentationParser;
import gameserver.model.actor.Player;
import gameserver.model.actor.templates.items.Item;
import gameserver.model.base.ShortcutType;
import gameserver.model.items.instance.ItemInstance;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.ExVariationCancelResult;
import gameserver.network.serverpackets.InventoryUpdate;
import gameserver.utils.Log;
import gameserver.utils.Util;

public final class RequestRefineCancel extends GameClientPacket
{
	private int _targetItemObjId;
	
	@Override
	protected void readImpl()
	{
		_targetItemObjId = readD();
	}

	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		final ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(_targetItemObjId);

		if (targetItem == null)
		{
			activeChar.sendPacket(new ExVariationCancelResult(0));
			return;
		}
		
		if (activeChar.isActionsDisabled())
		{
			activeChar.sendPacket(new ExVariationCancelResult(0));
			return;
		}

		if (targetItem.getOwnerId() != activeChar.getObjectId())
		{
			Util.handleIllegalPlayerAction(getClient().getActiveChar(), "" + getClient().getActiveChar().getName(null) + " of account " + getClient().getActiveChar().getAccountName() + " tryied to augment item that doesn't own.");
			return;
		}

		if (!targetItem.isAugmented())
		{
			activeChar.sendPacket(SystemMessageId.AUGMENTATION_REMOVAL_CAN_ONLY_BE_DONE_ON_AN_AUGMENTED_ITEM);
			activeChar.sendPacket(new ExVariationCancelResult(0));
			return;
		}

		int price = 0;
		switch (targetItem.getItem().getCrystalType())
		{
			case Item.CRYSTAL_C :
				if (targetItem.getCrystalCount() < 1720)
				{
					price = 95000;
				}
				else if (targetItem.getCrystalCount() < 2452)
				{
					price = 150000;
				}
				else
				{
					price = 210000;
				}
				break;
			case Item.CRYSTAL_B :
				if (targetItem.getCrystalCount() < 1746)
				{
					price = 240000;
				}
				else
				{
					price = 270000;
				}
				break;
			case Item.CRYSTAL_A :
				if (targetItem.getCrystalCount() < 2160)
				{
					price = 330000;
				}
				else if (targetItem.getCrystalCount() < 2824)
				{
					price = 390000;
				}
				else
				{
					price = 420000;
				}
				break;
			case Item.CRYSTAL_S :
				price = 480000;
				break;
			case Item.CRYSTAL_S80 :
			case Item.CRYSTAL_S84 :
				price = 920000;
				break;
			default :
				if (!AugmentationParser.getInstance().getParams().getBool("allowAugmentationAllItemsGrade"))
				{
					activeChar.sendPacket(new ExVariationCancelResult(0));
					return;
				}
				price = 95000;
				break;
		}

		if (!activeChar.reduceAdena("RequestRefineCancel", price, null, true))
		{
			activeChar.sendPacket(new ExVariationCancelResult(0));
			activeChar.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
			return;
		}
		Log.addLogItem(getClass().getSimpleName(), activeChar.getName(null) + " -> destroy ->", "Adena", price, 0, null);
		
		final boolean equipped = targetItem.isEquipped();
		if (equipped)
		{
			activeChar.getInventory().unEquipItemInSlotAndRecord(targetItem.getLocationSlot(), true);
		}

		targetItem.removeAugmentation();

		activeChar.sendPacket(new ExVariationCancelResult(1));

		if (equipped)
		{
			activeChar.getInventory().equipItem(targetItem, true, true);
		}
		
		final InventoryUpdate iu = new InventoryUpdate();
		iu.addModifiedItem(targetItem);
		activeChar.sendPacket(iu);
		
		activeChar.broadcastUserInfo(true);
		activeChar.updateShortCuts(targetItem.getObjectId(), ShortcutType.ITEM);
	}
}