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
import gameserver.model.actor.instance.PetInstance;
import gameserver.model.items.instance.ItemInstance;
import gameserver.network.SystemMessageId;
import gameserver.utils.Util;

public final class RequestGetItemFromPet extends GameClientPacket
{
	private int _objectId;
	private long _amount;
	protected int _unknown;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		final long amount = readQ();
		_unknown = readD();
		if (amount < 0)
		{
			Util.handleIllegalPlayerAction(getClient().getActiveChar(), "" + getClient().getActiveChar().getName(null) + " tried an overflow exploit!");
			_amount = 0;
		}
		_amount = amount;
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if ((_amount <= 0) || (player == null) || !player.hasPet())
		{
			return;
		}

		if (player.isInStoreMode())
		{
			player.sendPacket(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE);
			return;
		}

		if (player.isProcessingRequest())
		{
			player.sendActionFailed();
			return;
		}
		
		if (player.isFishing())
		{
			player.sendPacket(SystemMessageId.CANNOT_DO_WHILE_FISHING_2);
			return;
		}
		
		final PetInstance pet = (PetInstance) player.getSummon();
		if (pet == null || player.getActiveEnchantItemId() != Player.ID_NONE)
		{
			return;
		}
		
		if (Util.calculateDistance(player, pet, true) > 600)
		{
			player.sendPacket(SystemMessageId.TARGET_TOO_FAR);
			return;
		}

		final ItemInstance item = pet.getInventory().getItemByObjectId(_objectId);
		if (item == null || (item.getCount() < _amount) || item.isEquipped())
		{
			player.sendActionFailed();
			return;
		}

		if (_amount > item.getCount())
		{
			Util.handleIllegalPlayerAction(player, "" + player.getName(null) + " of account " + player.getAccountName() + " tried to get item with oid " + _objectId + " from pet but has invalid count " + _amount + " item count: " + item.getCount());
			return;
		}
		
		final var controlItem = pet.getControlItem();
		if (controlItem != null && _objectId == controlItem.getObjectId())
		{
			player.sendActionFailed();
			return;
		}

		if (pet.transferItem("Transfer", _objectId, _amount, player.getInventory(), player, pet) == null)
		{
			_log.warn("Invalid item transfer request: " + pet.getName(null) + "(pet) --> " + player.getName(null));
		}
	}
}