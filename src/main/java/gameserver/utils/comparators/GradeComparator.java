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

import gameserver.model.entity.auction.Auction;

public class GradeComparator implements Comparator<Auction>
{
	private final boolean _rightOrder;
	
	public GradeComparator(boolean rightOrder)
	{
		_rightOrder = rightOrder;
	}
	
	@Override
	public int compare(Auction o1, Auction o2)
	{
		final int grade1 = o1.getItem().getItem().getCrystalType();
		final int grade2 = o2.getItem().getItem().getCrystalType();
		
		if (_rightOrder)
		{
			return Integer.compare(grade1, grade2);
		}
		else
		{
			return Integer.compare(grade2, grade1);
		}
	}
}