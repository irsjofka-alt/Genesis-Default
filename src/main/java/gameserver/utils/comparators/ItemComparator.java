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

import gameserver.Config;
import gameserver.model.actor.templates.items.Item;
import gameserver.model.reward.CalculateRewardChances;

public class ItemComparator implements Comparator<Item>
{
	private final String _search;
	
	public ItemComparator(String search)
	{
		_search = search;
	}
	
	@Override
	public int compare(Item o1, Item o2)
	{
		if (o1.equals(o2))
		{
			return 0;
		}
		for (final String lang : Config.MULTILANG_ALLOWED)
		{
			if (lang != null)
			{
				if (o1.getName(lang).equalsIgnoreCase(_search))
				{
					return -1;
				}
				if (o2.getName(lang).equalsIgnoreCase(_search))
				{
					return 1;
				}
			}
		}
		return Integer.compare(CalculateRewardChances.getDroplistsCountByItemId(o2.getId(), true), CalculateRewardChances.getDroplistsCountByItemId(o1.getId(), true));
	}
}