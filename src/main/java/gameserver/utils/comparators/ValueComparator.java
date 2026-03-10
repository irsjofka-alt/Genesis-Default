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
import java.util.Map;

import gameserver.data.parser.NpcsParser;
import gameserver.handler.communityhandlers.impl.CommunityRaidBoss;
import gameserver.model.actor.templates.npc.NpcTemplate;
import gameserver.model.base.SortType;
import gameserver.model.stats.StatsSet;

public class ValueComparator implements Comparator<Integer>
{
	private final Map<Integer, StatsSet> _base;
	private final SortType _sortType;
	private final String _lang;

	public ValueComparator(Map<Integer, StatsSet> base, SortType sortType, String lang)
	{
		_base = base;
		_sortType = sortType;
		_lang = lang;
	}

	@Override
	public int compare(Integer o1, Integer o2)
	{
		int sortResult = sortById(o1, o2, _sortType, _lang);
		if (sortResult == 0 && !o1.equals(o2) && Math.abs(_sortType._index) != 1)
		{
			sortResult = sortById(o1, o2, SortType.NAME_ASC, _lang);
		}
		return sortResult;
	}

	private int sortById(Integer a, Integer b, SortType sorting, String lang)
	{
		final NpcTemplate temp1 = NpcsParser.getInstance().getTemplate(a.intValue());
		final NpcTemplate temp2 = NpcsParser.getInstance().getTemplate(b.intValue());
		final StatsSet set1 = _base.get(a);
		final StatsSet set2 = _base.get(b);
		final var isAlive1 = CommunityRaidBoss.isBossAlive(set1);
		final var isAlive2 = CommunityRaidBoss.isBossAlive(set2);
		return switch (sorting)
		{
			case NAME_ASC  -> temp1.getName(lang).compareTo(temp2.getName(lang));
			case NAME_DESC  -> temp2.getName(lang).compareTo(temp1.getName(lang));
			case LEVEL_ASC  -> Integer.compare(temp1.getLevel(), temp2.getLevel());
			case LEVEL_DESC  -> Integer.compare(temp2.getLevel(), temp1.getLevel());
			case STATUS_ASC  -> Integer.compare((int) set1.getLong("respawnTime"), (int) set2.getLong("respawnTime"));
			case STATUS_DESC  -> Integer.compare((int) set2.getLong("respawnTime"), (int) set1.getLong("respawnTime"));
			case STATUS_ALIVE  -> Boolean.compare(isAlive1, isAlive2);
			case STATUS_DEATH  -> Boolean.compare(isAlive2, isAlive1);
		};
	}
}