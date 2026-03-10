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
import gameserver.instancemanager.TerritoryWarManager;
import gameserver.model.actor.Playable;
import gameserver.model.actor.Player;
import gameserver.model.items.instance.ItemInstance;
import gameserver.network.SystemMessageId;

public class Disguise implements IItemHandler
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
		
		final int regId = TerritoryWarManager.getInstance().getRegisteredTerritoryId(activeChar);
		if (regId > 0 && regId == (item.getId() - 13596))
		{
			if (activeChar.getClan() != null && activeChar.getClan().getCastleId() > 0)
			{
				activeChar.sendPacket(SystemMessageId.TERRITORY_OWNING_CLAN_CANNOT_USE_DISGUISE_SCROLL);
				return false;
			}
			TerritoryWarManager.getInstance().addDisguisedPlayer(activeChar.getObjectId());
			activeChar.broadcastUserInfo(true);
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
			return true;
		}
		else if (regId > 0)
		{
			activeChar.sendPacket(SystemMessageId.THE_DISGUISE_SCROLL_MEANT_FOR_DIFFERENT_TERRITORY);
			return false;
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.TERRITORY_WAR_SCROLL_CAN_NOT_USED_NOW);
			return false;
		}
	}
}