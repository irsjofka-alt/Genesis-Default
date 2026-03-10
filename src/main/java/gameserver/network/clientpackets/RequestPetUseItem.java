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

import gameserver.handler.itemhandlers.ItemHandler;
import gameserver.model.actor.Player;
import gameserver.model.actor.instance.PetInstance;
import gameserver.model.items.instance.ItemInstance;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.PetItemList;
import gameserver.network.serverpackets.SystemMessage;

public final class RequestPetUseItem extends GameClientPacket
{
	private int _objectId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}

	@Override
	protected void runImpl()
	{
		final var activeChar = getClient().getActiveChar();
		if ((activeChar == null) || !activeChar.hasPet())
		{
			return;
		}
		
		if (activeChar.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}
		
		final var pet = (PetInstance) activeChar.getSummon();
		final var item = pet.getInventory().getItemByObjectId(_objectId);
		if (item == null)
		{
			return;
		}
		
		if (!item.getItem().isForNpc())
		{
			activeChar.sendPacket(SystemMessageId.PET_CANNOT_USE_ITEM);
			return;
		}
		
		if (activeChar.isAlikeDead() || pet.isDead())
		{
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addItemName(item);
			activeChar.sendPacket(sm);
			return;
		}
		
		final int reuseDelay = item.getReuseDelay();
		if (reuseDelay > 0)
		{
			final long reuse = pet.getItemRemainingReuseTime(item.getObjectId());
			if (reuse > 0)
			{
				return;
			}
		}
		
		if (!item.isEquipped() && !item.getItem().checkCondition(pet, pet, true))
		{
			return;
		}
		useItem(pet, item, activeChar);
	}
	
	private void useItem(PetInstance pet, ItemInstance item, Player activeChar)
	{
		if (item.isEquipable())
		{
			if (!item.getItem().isConditionAttached())
			{
				activeChar.sendPacket(SystemMessageId.PET_CANNOT_USE_ITEM);
				return;
			}
			
			if (item.isEquipped())
			{
				pet.getInventory().unEquipItemInSlot(item.getLocationSlot(), true);
			}
			else
			{
				pet.getInventory().equipItem(item, true, true);
			}
			
			activeChar.sendPacket(new PetItemList(pet.getInventory().getItems()));
			pet.updateAndBroadcastStatus(1);
		}
		else
		{
			final var handler = ItemHandler.getInstance().getHandler(item.getEtcItem());
			if (handler != null)
			{
				if (handler.useItem(pet, item, false))
				{
					final int reuseDelay = item.getReuseDelay();
					if (reuseDelay > 0)
					{
						activeChar.addTimeStampItem(item, reuseDelay, item.isReuseByCron());
					}
					pet.updateAndBroadcastStatus(1);
				}
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.PET_CANNOT_USE_ITEM);
				_log.warn("No item handler registered for itemId: " + item.getId());
			}
		}
	}
}