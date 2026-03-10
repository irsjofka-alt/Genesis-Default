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

import gameserver.model.actor.Player;
import gameserver.model.items.instance.ItemInstance;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.ExPutItemResultForVariationMake;

public final class RequestConfirmTargetItem extends AbstractRefinePacket
{
	private int _itemObjId;
	
	@Override
	protected void readImpl()
	{
		_itemObjId = readD();
	}

	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		final ItemInstance item = activeChar.getInventory().getItemByObjectId(_itemObjId);
		if (item == null)
		{
			return;
		}

		if (!isValid(activeChar, item))
		{
			if (item.isAugmented())
			{
				activeChar.sendPacket(SystemMessageId.ONCE_AN_ITEM_IS_AUGMENTED_IT_CANNOT_BE_AUGMENTED_AGAIN);
				return;
			}

			activeChar.sendPacket(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM);
			return;
		}

		activeChar.sendPacket(new ExPutItemResultForVariationMake(_itemObjId, item.getId()));
	}
}