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

import gameserver.Config;
import gameserver.handler.itemhandlers.IItemHandler;
import gameserver.model.ShotType;
import gameserver.model.actor.Playable;
import gameserver.model.actor.Player;
import gameserver.model.holders.SkillHolder;
import gameserver.model.items.instance.ItemInstance;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.MagicSkillUse;
import gameserver.network.serverpackets.SystemMessage;

public class BeastSpiritShot implements IItemHandler
{
	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!playable.isPlayer())
		{
			playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
			return false;
		}
		
		final Player activeOwner = playable.getActingPlayer();
		if (!activeOwner.hasSummon())
		{
			activeOwner.sendPacket(SystemMessageId.PETS_ARE_NOT_AVAILABLE_AT_THIS_TIME);
			return false;
		}
		
		if (activeOwner.getSummon().isDead())
		{
			activeOwner.sendPacket(SystemMessageId.SOULSHOTS_AND_SPIRITSHOTS_ARE_NOT_AVAILABLE_FOR_A_DEAD_PET);
			return false;
		}
		
		final int itemId = item.getId();
		final boolean isBlessed = (itemId == 6647 || itemId == 20334);
		final int shotConsumption = activeOwner.getSummon().getSpiritShotsPerHit();
		final SkillHolder[] skills = item.getItem().getSkills();
		
		if (skills == null)
		{
			_log.warn(getClass().getSimpleName() + ": is missing skills!");
			return false;
		}
		
		final long shotCount = item.getCount();
		if (shotCount < shotConsumption)
		{
			if (!activeOwner.haveAutoShot(itemId))
			{
				activeOwner.sendPacket(SystemMessageId.NOT_ENOUGH_SPIRITHOTS_FOR_PET);
			}
			return false;
		}
		
		if (activeOwner.getSummon().isChargedShot(isBlessed ? ShotType.BLESSED_SPIRITSHOTS : ShotType.SPIRITSHOTS))
		{
			return false;
		}
		
		if (!Config.INFINITE_BEAST_SPIRIT_SHOT && !activeOwner.getInventory().reduceShortsCount(item, shotConsumption))
		{
			if (!activeOwner.haveAutoShot(itemId))
			{
				activeOwner.sendPacket(SystemMessageId.NOT_ENOUGH_SPIRITHOTS_FOR_PET);
			}
			return false;
		}
		activeOwner.getSummon().setChargedShot(isBlessed ? ShotType.BLESSED_SPIRITSHOTS : ShotType.SPIRITSHOTS, true);
		
		final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.USE_S1_);
		sm.addItemName(itemId);
		activeOwner.sendPacket(sm, SystemMessageId.PET_USE_SPIRITSHOT);
		activeOwner.broadcastPacket(600, new MagicSkillUse(activeOwner.getSummon(), activeOwner.getSummon(), skills[0].getId(), skills[0].getLvl(), 0, 0));
		return true;
	}
}