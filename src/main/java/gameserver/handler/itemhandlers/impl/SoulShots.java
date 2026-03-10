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
package gameserver.handler.itemhandlers.impl;

import l2e.commons.util.Rnd;
import gameserver.Config;
import gameserver.handler.itemhandlers.IItemHandler;
import gameserver.model.ShotType;
import gameserver.model.actor.Playable;
import gameserver.model.actor.Player;
import gameserver.model.actor.templates.items.Weapon;
import gameserver.model.holders.SkillHolder;
import gameserver.model.items.instance.ItemInstance;
import gameserver.model.items.type.ActionType;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.MagicSkillUse;
import gameserver.network.serverpackets.SystemMessage;

public class SoulShots implements IItemHandler
{
	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!playable.isPlayer())
		{
			playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
			return false;
		}
		
		final Player activeChar = playable.getActingPlayer();
		final ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		final Weapon weaponItem = activeChar.getActiveWeaponItem();
		final SkillHolder[] skills = item.getItem().getSkills();

		final int itemId = item.getId();

		if (skills == null)
		{
			_log.warn(getClass().getSimpleName() + ": is missing skills!");
			return false;
		}

		if ((weaponInst == null) || (weaponItem.getSoulShotCount() == 0))
		{
			if (!activeChar.getAutoSoulShot().contains(itemId))
			{
				activeChar.sendPacket(SystemMessageId.CANNOT_USE_SOULSHOTS);
			}
			return false;
		}
		final boolean gradeCheck = item.isEtcItem() && (item.getEtcItem().getDefaultAction() == ActionType.soulshot) && (weaponInst.getItem().getItemGradeSPlus() == item.getItem().getItemGradeSPlus());
		if (!gradeCheck)
		{
			return false;
		}

		activeChar.soulShotLock.lock();
		try
		{
			if (activeChar.isChargedShot(ShotType.SOULSHOTS))
			{
				return false;
			}

			int SSCount = weaponItem.getSoulShotCount();
			if ((weaponItem.getReducedSoulShot() > 0) && (Rnd.get(100) < weaponItem.getReducedSoulShotChance()))
			{
				SSCount = weaponItem.getReducedSoulShot();
			}

			if (!Config.INFINITE_SOUL_SHOT && !activeChar.getInventory().reduceShortsCount(item, SSCount))
			{
				if (!activeChar.haveAutoShot(itemId))
				{
					activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_SOULSHOTS);
				}
				return false;
			}
			weaponInst.setChargedShot(ShotType.SOULSHOTS, true);
		}
		finally
		{
			activeChar.soulShotLock.unlock();
		}
		
		final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.USE_S1_);
		sm.addItemName(itemId);
		activeChar.sendPacket(sm, SystemMessageId.ENABLED_SOULSHOT);
		activeChar.broadcastPacket(600, new MagicSkillUse(activeChar, activeChar, skills[0].getId(), skills[0].getLvl(), 0, 0));
		return true;
	}
}