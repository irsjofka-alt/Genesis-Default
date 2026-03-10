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

import gameserver.model.skills.options.Options;

public class PassiveSkillsComparator implements Comparator<Options>
{
	private static final PassiveSkillsComparator instance = new PassiveSkillsComparator();

	public static final PassiveSkillsComparator getInstance()
	{
		return instance;
	}

	@Override
	public int compare(Options left, Options right)
	{
		if (!left.hasPassiveSkill() || !right.hasPassiveSkill())
		{
			return 0;
		}
		return Integer.valueOf(left.getPassiveSkill().getId()).compareTo(right.getPassiveSkill().getId());
	}
}