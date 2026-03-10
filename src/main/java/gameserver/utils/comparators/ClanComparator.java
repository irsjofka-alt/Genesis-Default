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

import gameserver.model.Clan;

public class ClanComparator implements Comparator<Clan>
{
	private static final ClanComparator instance = new ClanComparator();

	public static final ClanComparator getInstance()
	{
		return instance;
	}

	@Override
	public int compare(Clan o1, Clan o2)
	{
		if (o1.getLevel() > o2.getLevel())
		{
			return -1;
		}
		if (o2.getLevel() > o1.getLevel())
		{
			return 1;
		}
		if (o1.getReputationScore() > o2.getReputationScore())
		{
			return -1;
		}
		if (o2.getReputationScore() > o1.getReputationScore())
		{
			return 1;
		}
		return 0;
	}
}