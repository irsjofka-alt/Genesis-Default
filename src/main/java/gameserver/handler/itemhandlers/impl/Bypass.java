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
import gameserver.model.items.instance.ItemInstance;
import gameserver.network.serverpackets.NpcHtmlMessage;

public class Bypass implements IItemHandler
{
	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!(playable.isPlayer()))
		{
			return false;
		}
		
		final var activeChar = playable.getActingPlayer();
		final var html = new NpcHtmlMessage(0, item.getId());
		html.setFile(activeChar, activeChar.getLang(), "data/html/item/" + item.getId() + ".htm");
		html.replace("%itemId%", String.valueOf(item.getObjectId()));
		activeChar.sendPacket(html);
		return true;
	}
}