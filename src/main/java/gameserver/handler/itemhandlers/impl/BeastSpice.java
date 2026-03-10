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

import gameserver.handler.itemhandlers.IItemHandler;
import gameserver.model.actor.Playable;
import gameserver.model.actor.Player;
import gameserver.model.actor.instance.FeedableBeastInstance;
import gameserver.model.holders.SkillHolder;
import gameserver.model.items.instance.ItemInstance;
import gameserver.network.SystemMessageId;

public class BeastSpice implements IItemHandler
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
		final SkillHolder[] skills = item.getItem().getSkills();

		if (skills == null)
		{
			_log.warn(getClass().getSimpleName() + ": is missing skills!");
			return false;
		}

		if (!(activeChar.getTarget() instanceof FeedableBeastInstance))
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return false;
		}

		for (final SkillHolder sk : skills)
		{
			activeChar.useMagic(sk.getSkill(), false, false, true);
		}
		return true;
	}
}