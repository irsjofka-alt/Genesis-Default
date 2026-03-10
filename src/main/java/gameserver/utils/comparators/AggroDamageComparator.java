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

import gameserver.model.actor.templates.npc.aggro.DamageInfo;

public class AggroDamageComparator implements Comparator<DamageInfo>
{
	private static final AggroDamageComparator instance = new AggroDamageComparator();

	public static final AggroDamageComparator getInstance()
	{
		return instance;
	}

	@Override
	public int compare(DamageInfo o1, DamageInfo o2)
	{
		if (o1 == null || o2 == null)
		{
			return 0;
		}
		if (o1 == o2)
		{
			return 0;
		}
		return Long.compare(o2.getDamage(), o1.getDamage());
	}
}