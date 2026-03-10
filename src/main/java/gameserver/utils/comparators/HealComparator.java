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

import gameserver.model.actor.Player;

public class HealComparator implements Comparator<Player>
{
	private static final HealComparator instance = new HealComparator();

	public static final HealComparator getInstance()
	{
		return instance;
	}

	@Override
	public int compare(Player o1, Player o2)
	{
		if (o1 == null || o2 == null)
		{
			return 0;
		}
		return Double.compare(o1.getCurrentHpPercents(), o2.getCurrentHpPercents());
	}
}