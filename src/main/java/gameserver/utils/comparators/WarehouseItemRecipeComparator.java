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

import gameserver.data.parser.RecipeParser;
import gameserver.model.RecipeList;
import gameserver.model.actor.templates.items.Item;
import gameserver.model.actor.templates.items.WarehouseItem;
import gameserver.model.items.type.EtcItemType;

public class WarehouseItemRecipeComparator implements Comparator<WarehouseItem>
{
	private int _order = 0;
	private RecipeParser _rd = null;

	public WarehouseItemRecipeComparator(int sortOrder)
	{
		_order = sortOrder;
		_rd = RecipeParser.getInstance();
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
		if ((o1.isEtcItem() && (o1.getItemType() == EtcItemType.RECIPE)) && (o2.isEtcItem() && (o2.getItemType() == EtcItemType.RECIPE)))
		{
			try
			{
				final RecipeList rp1 = _rd.getRecipeByItemId(o1.getId());
				final RecipeList rp2 = _rd.getRecipeByItemId(o2.getId());
				
				if (rp1 == null)
				{
					return (_order == 1 ? 1 : -1);
				}
				if (rp2 == null)
				{
					return (_order == 1 ? -1 : 1);
				}
				
				final Integer i1 = rp1.getLevel();
				final Integer i2 = rp2.getLevel();
				
				return (_order == 1 ? i1.compareTo(i2) : i2.compareTo(i1));
			}
			catch (final Exception e)
			{
				return 0;
			}
		}
		
		final String s1 = o1.getName(null);
		final String s2 = o2.getName(null);
		return (_order == 1 ? s1.compareTo(s2) : s2.compareTo(s1));
	}
}