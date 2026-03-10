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
package gameserver.handler.skillhandlers.impl;

import gameserver.handler.skillhandlers.ISkillHandler;
import gameserver.model.Augmentation;
import gameserver.model.GameObject;
import gameserver.model.actor.Creature;
import gameserver.model.actor.templates.items.Weapon;
import gameserver.model.items.itemcontainer.Inventory;
import gameserver.model.skills.Skill;
import gameserver.model.skills.SkillType;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.InventoryUpdate;
import gameserver.network.serverpackets.SystemMessage;

public class ConvertItem implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS =
	{
	        SkillType.CONVERT_ITEM
	};

	@Override
	public void useSkill(Creature activeChar, Skill skill, GameObject[] targets, double cubicPower)
	{
		if (activeChar.isAlikeDead() || !activeChar.isPlayer())
		{
			return;
		}
		
		final var player = activeChar.getActingPlayer();
		if (player.isEnchanting())
		{
			return;
		}
		
		final var weaponItem = player.getActiveWeaponItem();
		if (weaponItem == null)
		{
			return;
		}
		
		var wpn = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		if (wpn == null)
		{
			wpn = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		}
		
		if ((wpn == null) || (weaponItem.getChangeWeaponId() == 0))
		{
			return;
		}
		
		final int newItemId = weaponItem.getChangeWeaponId();
		if (newItemId == -1)
		{
			return;
		}
		
		final int enchantLevel = wpn.getEnchantLevel();
		final var elementals = wpn.getElementals();
		final var augment = wpn.getAugmentation();
		final var unequipped = player.getInventory().unEquipItemInBodySlotAndRecord(wpn.getItem().getBodyPart(), true);
		final var iu = new InventoryUpdate();
		for (final var unequippedItem : unequipped)
		{
			iu.addModifiedItem(unequippedItem);
		}
		player.sendInventoryUpdate(iu);
		
		if (unequipped.size() == 0)
		{
			return;
		}
		
		byte count = 0;
		for (final var unequippedItem : unequipped)
		{
			if (!(unequippedItem.getItem() instanceof Weapon))
			{
				count++;
				continue;
			}
			
			final SystemMessage sm;
			if (unequippedItem.getEnchantLevel() > 0)
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
				sm.addInt(unequippedItem.getEnchantLevel());
				sm.addItemName(unequippedItem);
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED);
				sm.addItemName(unequippedItem);
			}
			player.sendPacket(sm);
		}
		
		if (count == unequipped.size())
		{
			return;
		}
		
		final var destroyItem = player.getInventory().destroyItem("ChangeWeapon", wpn, player, null);
		if (destroyItem == null)
		{
			return;
		}
		
		final var newItem = player.getInventory().addItem("ChangeWeapon", newItemId, 1, player, destroyItem);
		if (newItem == null)
		{
			return;
		}
		
		if (elementals != null)
		{
			for (final var elm : elementals)
			{
				newItem.setElementAttr(elm.getElement(), elm.getValue(), false);
			}
			newItem.updateItemElementals();
		}
		
		if (augment != null)
		{
			newItem.setAugmentation(new Augmentation(augment.getAugmentationId()));
		}
		
		newItem.setEnchantLevel(enchantLevel);
		newItem.updateDatabase();
		player.getInventory().equipItem(newItem, true, true);
		
		final SystemMessage msg;
		if (newItem.getEnchantLevel() > 0)
		{
			msg = SystemMessage.getSystemMessage(SystemMessageId.S1_S2_EQUIPPED);
			msg.addNumber(newItem.getEnchantLevel());
			msg.addItemName(newItem);
		}
		else
		{
			msg = SystemMessage.getSystemMessage(SystemMessageId.S1_EQUIPPED);
			msg.addItemName(newItem);
		}
		player.sendPacket(msg);
		
		final var u = new InventoryUpdate();
		u.addRemovedItem(destroyItem);
		u.addItem(newItem);
		player.sendInventoryUpdate(u);
		player.refreshExpertisePenalty();
		player.broadcastUserInfo(true);
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}