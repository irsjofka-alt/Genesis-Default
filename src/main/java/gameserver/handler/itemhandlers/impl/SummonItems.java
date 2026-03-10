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

import gameserver.data.parser.PetsParser;
import gameserver.model.PetData;
import gameserver.model.actor.Playable;
import gameserver.model.actor.Player;
import gameserver.model.entity.events.AbstractFightEvent;
import gameserver.model.holders.PetItemHolder;
import gameserver.model.items.instance.ItemInstance;
import gameserver.network.SystemMessageId;

public class SummonItems extends ItemSkillsTemplate
{
	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!playable.isPlayer())
		{
			playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
			return false;
		}
		
		for (final AbstractFightEvent e : playable.getFightEvents())
		{
			if (e != null && !e.canUseItemSummon(playable))
			{
				return false;
			}
		}
		
		final Player activeChar = playable.getActingPlayer();
		if ((activeChar.getBlockCheckerArena() != -1) || activeChar.inObserverMode() || activeChar.isAllSkillsDisabled() || activeChar.isCastingNow())
		{
			return false;
		}

		if (activeChar.isSitting())
		{
			activeChar.sendPacket(SystemMessageId.CANT_MOVE_SITTING);
			return false;
		}

		if (activeChar.hasSummon() || activeChar.isMounted())
		{
			activeChar.sendPacket(SystemMessageId.YOU_ALREADY_HAVE_A_PET);
			return false;
		}

		if (activeChar.isAttackingNow() || activeChar.isInCombat())
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_SUMMON_IN_COMBAT);
			return false;
		}

		final PetData petData = PetsParser.getInstance().getPetDataByItemId(item.getId());
		if ((petData == null) || (petData.getNpcId() == -1))
		{
			return false;
		}

		activeChar.addScript(new PetItemHolder(item));
		return super.useItem(playable, item, forceUse);
	}
}