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
import java.util.Random;

import gameserver.model.base.AttackType;

public class AttackTypeComparator implements Comparator<AttackType>
{
	private static final AttackTypeComparator instance = new AttackTypeComparator();

	public static final AttackTypeComparator getInstance()
	{
		return instance;
	}

	@Override
	public int compare(AttackType l, AttackType r)
	{
		final int left = l.getId();
		final int right = r.getId();
		if (left > right)
		{
			return 1;
		}
		
		if (left < right)
		{
			return -1;
		}
		
		final Random rnd = new Random();
		
		return rnd.nextInt(2) == 1 ? 1 : 1;
	}
}