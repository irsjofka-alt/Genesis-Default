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
package gameserver.network.serverpackets;

import gameserver.model.TradeItem;
import gameserver.model.actor.Player;

public class PrivateStoreSellManageList extends GameServerPacket
{
	private final int _objId;
	private final long _playerAdena;
	private final boolean _packageSale;
	private final TradeItem[] _itemList;
	private final TradeItem[] _sellList;

	public PrivateStoreSellManageList(Player player, boolean isPackageSale)
	{
		_objId = player.getObjectId();
		_playerAdena = player.getAdena();
		player.getSellList().updateItems();
		_packageSale = isPackageSale;
		_itemList = player.getInventory().getAvailableSellItems(player.getSellList());
		if (player.getVar("isPacketSell") != null && !isPackageSale)
		{
			player.getSellList().clear();
			player.unsetVar("isPacketSell");
		}
		_sellList = player.getSellList().getItems();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeD(_objId);
		writeD(_packageSale ? 1 : 0);
		writeQ(_playerAdena);
		writeD(_itemList.length);
		for (final var item : _itemList)
		{
			writeD(item.getObjectId());
			writeD(item.getItem().getDisplayId());
			writeD(item.getLocationSlot());
			writeQ(item.getCount());
			writeH(item.getItem().getType2());
			writeH(item.getCustomType1());
			writeH(0x00);
			writeD(item.getItem().getBodyPart());
			writeH(item.getEnchant());
			writeH(item.getCustomType2());
			writeD(0x00);
			writeD(-1);
			writeD(-9999);
			writeH(item.getAttackElementType());
			writeH(item.getAttackElementPower());
			for (byte i = 0; i < 6; i++)
			{
				writeH(item.getElementDefAttr(i));
			}
			for (final int op : item.getEnchantOptions())
			{
				writeH(op);
			}
			writeQ(item.getItem().getReferencePrice() * 2);
		}
		writeD(_sellList.length);
		for (final var item : _sellList)
		{
			writeD(item.getObjectId());
			writeD(item.getItem().getId());
			writeD(item.getLocationSlot());
			writeQ(item.getCount());
			writeH(item.getItem().getType2());
			writeH(item.getCustomType1());
			writeH(0x00);
			writeD(item.getItem().getBodyPart());
			writeH(item.getEnchant());
			writeH(item.getCustomType2());
			writeD(0x00);
			writeD(-1);
			writeD(-9999);
			writeH(item.getAttackElementType());
			writeH(item.getAttackElementPower());
			for (byte i = 0; i < 6; i++)
			{
				writeH(item.getElementDefAttr(i));
			}
			writeH(0x00);
			writeH(0x00);
			writeH(0x00);
			writeQ(item.getPrice());
			writeQ(item.getItem().getReferencePrice() * 2);
		}
	}
}