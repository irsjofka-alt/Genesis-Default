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
package gameserver.utils.comparators;

import java.util.Comparator;

import gameserver.model.actor.templates.items.Item;
import gameserver.model.actor.templates.items.WarehouseItem;

public class WarehouseItemBodypartComparator implements Comparator<WarehouseItem>
{
	private byte _order = 0;

	public WarehouseItemBodypartComparator(byte sortOrder)
	{
		_order = sortOrder;
	}

	@Override
	public int compare(WarehouseItem o1, WarehouseItem o2)
	{
		if ((o1.getType2() == Item.TYPE2_MONEY) && (o2.getType2() != Item.TYPE2_MONEY))
		{
			return (_order == 1 ? -1 : 1);
		}
		if ((o2.getType2() == Item.TYPE2_MONEY) && (o1.getType2() != Item.TYPE2_MONEY))
		{
			return (_order == 1 ? 1 : -1);
		}
		final Integer i1 = o1.getBodyPart();
		final Integer i2 = o2.getBodyPart();
		return (_order == 1 ? i1.compareTo(i2) : i2.compareTo(i1));
	}
}