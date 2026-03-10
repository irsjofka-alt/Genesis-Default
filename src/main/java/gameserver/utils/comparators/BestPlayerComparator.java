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

import gameserver.model.entity.events.model.template.FightEventPlayer;

public class BestPlayerComparator implements Comparator<FightEventPlayer>
{
	private final boolean _scoreNotKills;

	public BestPlayerComparator(boolean scoreNotKills)
	{
		_scoreNotKills = scoreNotKills;
	}
	
	@Override
	public int compare(FightEventPlayer arg0, FightEventPlayer arg1)
	{
		if (_scoreNotKills)
		{
			return Integer.compare(arg1.getScore(), arg0.getScore());
		}
		return Integer.compare(arg1.getKills(), arg0.getKills());
	}
}