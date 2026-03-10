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

public class SkillComparator implements Comparator<String>
{
	private static final SkillComparator instance = new SkillComparator();

	public static final SkillComparator getInstance()
	{
		return instance;
	}

	@Override
	public int compare(String l, String r)
	{
		final int left = Integer.valueOf(l.split(";")[0]).intValue();
		final int right = Integer.valueOf(r.split(";")[0]).intValue();
		
		if (left > right)
		{
			return 1;
		}
		
		if (left < right)
		{
			return -1;
		}
		
		if (Integer.valueOf(l.split(";")[1]).intValue() > Integer.valueOf(r.split(";")[1]).intValue())
		{
			return 1;
		}
		
		if (Integer.valueOf(r.split(";")[1]).intValue() > Integer.valueOf(l.split(";")[1]).intValue())
		{
			return -1;
		}
		
		final Random x = new Random();
		return x.nextInt(2) == 1 ? 1 : 1;
	}
}