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

import gameserver.model.quest.Quest;

public class QuestComparator implements Comparator<Quest>
{
	private static final QuestComparator instance = new QuestComparator();

	public static final QuestComparator getInstance()
	{
		return instance;
	}

	@Override
	public int compare(Quest o1, Quest o2)
	{
		return Integer.compare(o1.getId(), o2.getId());
	}
}