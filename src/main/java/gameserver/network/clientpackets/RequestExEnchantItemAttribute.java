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

import l2e.commons.util.Rnd;
import gameserver.Config;
import gameserver.model.Elementals;
import gameserver.model.actor.Player;
import gameserver.model.items.instance.ItemInstance;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.ExAttributeEnchantResult;
import gameserver.network.serverpackets.InventoryUpdate;
import gameserver.network.serverpackets.SystemMessage;
import gameserver.utils.Util;

public class RequestExEnchantItemAttribute extends GameClientPacket
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
		final Player player = getActiveChar();
		if (player == null)
		{
			return;
		}
		
		if (player.isActionsDisabled())
		{
			player.setActiveEnchantAttrItemId(Player.ID_NONE);
			player.sendActionFailed();
			return;
		}
		
		if (_objectId == 0xFFFFFFFF)
		{
			player.setActiveEnchantAttrItemId(Player.ID_NONE);
			player.sendPacket(SystemMessageId.ELEMENTAL_ENHANCE_CANCELED);
			return;
		}
		
		if (!player.isOnline())
		{
			player.setActiveEnchantAttrItemId(Player.ID_NONE);
			return;
		}
		
		if (player.getPrivateStoreType() != Player.STORE_PRIVATE_NONE)
		{
			player.sendPacket(SystemMessageId.CANNOT_ADD_ELEMENTAL_POWER_WHILE_OPERATING_PRIVATE_STORE_OR_WORKSHOP);
			player.setActiveEnchantAttrItemId(Player.ID_NONE);
			return;
		}
		
		if (player.getActiveRequester() != null)
		{
			player.cancelActiveTrade();
			player.setActiveEnchantAttrItemId(Player.ID_NONE);
			player.sendMessage("You cannot add elemental power while trading.");
			return;
		}
		
		final ItemInstance item = player.getInventory().getItemByObjectId(_objectId);
		final ItemInstance stone = player.getInventory().getItemByObjectId(player.getActiveEnchantAttrItemId());
		if ((item == null) || (stone == null))
		{
			player.setActiveEnchantAttrItemId(Player.ID_NONE);
			player.sendPacket(SystemMessageId.ELEMENTAL_ENHANCE_CANCELED);
			return;
		}
		
		if (!item.isElementable())
		{
			player.sendPacket(SystemMessageId.ELEMENTAL_ENHANCE_REQUIREMENT_NOT_SUFFICIENT);
			player.setActiveEnchantAttrItemId(Player.ID_NONE);
			return;
		}
		
		switch (item.getItemLocation())
		{
			case INVENTORY :
			case PAPERDOLL :
			{
				if (item.getOwnerId() != player.getObjectId())
				{
					player.setActiveEnchantAttrItemId(Player.ID_NONE);
					return;
				}
				break;
			}
			default :
			{
				player.setActiveEnchantAttrItemId(Player.ID_NONE);
				Util.handleIllegalPlayerAction(player, "" + player.getName(null) + " tried to use enchant Exploit!");
				return;
			}
		}
		
		final int stoneId = stone.getId();
		byte elementToAdd = Elementals.getItemElement(stoneId);
		long count = Math.max(1, player.getActiveEnchantAttrItemCount());
		if (stone.getCount() < count)
		{
			count = 1;
		}
		
		if (item.isArmor())
		{
			elementToAdd = Elementals.getOppositeElement(elementToAdd);
		}
		final byte opositeElement = Elementals.getOppositeElement(elementToAdd);
		
		final Elementals oldElement = item.getElemental(elementToAdd);
		final int elementValue = oldElement == null ? 0 : oldElement.getValue();
		final int limit = getLimit(item, stoneId);
		int powerToAdd = getPowerToAdd(stoneId, elementValue, item);
		
		if ((item.isWeapon() && (oldElement != null) && (oldElement.getElement() != elementToAdd) && (oldElement.getElement() != -2)) || (item.isArmor() && (item.getElemental(elementToAdd) == null) && (item.getElementals() != null) && (item.getElementals().length >= Config.ELEMENT_ARMOR_LIMIT)))
		{
			player.sendPacket(SystemMessageId.ANOTHER_ELEMENTAL_POWER_ALREADY_ADDED);
			player.setActiveEnchantAttrItemId(Player.ID_NONE);
			return;
		}
		
		if (item.isArmor() && (item.getElementals() != null))
		{
			for (final Elementals elm : item.getElementals())
			{
				if (elm.getElement() == opositeElement)
				{
					player.setActiveEnchantAttrItemId(Player.ID_NONE);
					Util.handleIllegalPlayerAction(player, "" + player.getName(null) + " tried to add oposite attribute to item!");
					return;
				}
			}
		}
		
		int newPower = elementValue + powerToAdd;
		if (newPower > limit)
		{
			newPower = limit;
			powerToAdd = limit - elementValue;
		}
		
		if (powerToAdd <= 0)
		{
			player.sendPacket(SystemMessageId.ELEMENTAL_ENHANCE_CANCELED);
			player.setActiveEnchantAttrItemId(Player.ID_NONE);
			return;
		}
		
		int usedStones = 0;
		boolean success = false;
		for (int i = 0; i < count; i++)
		{
			usedStones++;
			final int result = addElement(player, stone, item, elementToAdd);
			if (result == 1)
			{
				success = true;
			}
			else if (result == -1)
			{
				break;
			}
		}
		
		if (!player.destroyItem("AttrEnchant", stone, usedStones, player, true))
		{
			player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			Util.handleIllegalPlayerAction(player, "" + player.getName(null) + " tried to attribute enchant with a stone he doesn't have");
			player.setActiveEnchantAttrItemId(Player.ID_NONE);
			return;
		}
		
		final byte realElement = item.isArmor() ? opositeElement : elementToAdd;
		final var iu = new InventoryUpdate();
		final var isEquiped = item.isEquipped();
		if (success)
		{
			item.updateItemElementals();
			if (isEquiped)
			{
				item.updateElementAttrBonus(player);
			}
			SystemMessage sm;
			if (item.getEnchantLevel() == 0)
			{
				if (item.isArmor())
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.THE_S2_ATTRIBUTE_WAS_SUCCESSFULLY_BESTOWED_ON_S1_RES_TO_S3_INCREASED);
				}
				else
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.ELEMENTAL_POWER_S2_SUCCESSFULLY_ADDED_TO_S1);
				}
				sm.addItemName(item);
				sm.addElemental(realElement);
				if (item.isArmor())
				{
					sm.addElemental(Elementals.getOppositeElement(realElement));
				}
			}
			else
			{
				if (item.isArmor())
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.THE_S3_ATTRIBUTE_BESTOWED_ON_S1_S2_RESISTANCE_TO_S4_INCREASED);
				}
				else
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.ELEMENTAL_POWER_S3_SUCCESSFULLY_ADDED_TO_S1_S2);
				}
				sm.addInt(item.getEnchantLevel());
				sm.addItemName(item);
				sm.addElemental(realElement);
				if (item.isArmor())
				{
					sm.addElemental(Elementals.getOppositeElement(realElement));
				}
			}
			player.sendPacket(sm);
			iu.addModifiedItem(item);
		}
		else
		{
			player.sendPacket(SystemMessageId.FAILED_ADDING_ELEMENTAL_POWER);
		}
		
		if (stone.getCount() == 0)
		{
			iu.addRemovedItem(stone);
		}
		else
		{
			iu.addModifiedItem(stone);
		}
		
		player.sendPacket(new ExAttributeEnchantResult(powerToAdd));
		if (isEquiped)
		{
			player.sendUserInfo();
		}
		player.sendInventoryUpdate(iu);
		player.setActiveEnchantAttrItemId(Player.ID_NONE);
	}
	
	private int addElement(Player player, ItemInstance stone, ItemInstance item, byte elementToAdd)
	{
		final int stoneId = stone.getId();
		final Elementals oldElement = item.getElemental(elementToAdd);
		final int elementValue = oldElement == null ? 0 : oldElement.getValue();
		final int limit = getLimit(item, stone.getId());
		int powerToAdd = getPowerToAdd(stone.getId(), elementValue, item);
		int newPower = elementValue + powerToAdd;
		if (newPower > limit)
		{
			newPower = limit;
			powerToAdd = limit - elementValue;
		}
		
		if (powerToAdd <= 0)
		{
			player.sendPacket(SystemMessageId.ELEMENTAL_ENHANCE_CANCELED);
			player.setActiveEnchantAttrItemId(Player.ID_NONE);
			return -1;
		}
		
		boolean success = switch (Elementals.getItemElemental(stoneId)._type)
		{
			case Stone :
			case Roughore : yield Rnd.get(100) < (Config.ENCHANT_CHANCE_ELEMENT_STONE + player.getPremiumBonus().getEnchantChance());
			case Crystal : yield Rnd.get(100) < (Config.ENCHANT_CHANCE_ELEMENT_CRYSTAL + player.getPremiumBonus().getEnchantChance());
			case Jewel : yield Rnd.get(100) < (Config.ENCHANT_CHANCE_ELEMENT_JEWEL + player.getPremiumBonus().getEnchantChance());
			case Energy : yield Rnd.get(100) < (Config.ENCHANT_CHANCE_ELEMENT_ENERGY + player.getPremiumBonus().getEnchantChance());
		};
		
		if (success)
		{
			item.setElementAttr(elementToAdd, newPower, false);
		}
		return success ? 1 : 0;
	}
	
	public int getLimit(ItemInstance item, int sotneId)
	{
		final Elementals.ElementalItems elementItem = Elementals.getItemElemental(sotneId);
		if (elementItem == null)
		{
			return 0;
		}
		
		if (item.isWeapon())
		{
			return Elementals.WEAPON_VALUES[elementItem._type._maxLevel];
		}
		return Elementals.ARMOR_VALUES[elementItem._type._maxLevel];
	}
	
	public int getPowerToAdd(int stoneId, int oldValue, ItemInstance item)
	{
		if (Elementals.getItemElement(stoneId) != Elementals.NONE)
		{
			if (item.isWeapon())
			{
				if (oldValue == 0)
				{
					return Elementals.FIRST_WEAPON_BONUS;
				}
				return Elementals.NEXT_WEAPON_BONUS;
			}
			else if (item.isArmor())
			{
				return Elementals.ARMOR_BONUS;
			}
		}
		return 0;
	}
}