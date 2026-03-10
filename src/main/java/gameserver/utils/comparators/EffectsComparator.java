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

import gameserver.model.skills.effects.Effect;

public class EffectsComparator implements Comparator<Effect>
{
	private static final EffectsComparator instance = new EffectsComparator();

	public static final EffectsComparator getInstance()
	{
		return instance;
	}

	@Override
	public int compare(Effect e1, Effect e2)
	{
		final boolean toggle1 = e1.getSkill().isToggle();
		final boolean toggle2 = e2.getSkill().isToggle();

		if(toggle1 && toggle2)
		{
			return compareStartTime(e1, e2);
		}

		if(toggle1 || toggle2)
		{
			if (toggle1)
			{
				return 1;
			}
			else
			{
				return -1;
			}
		}

		final boolean music1 = e1.getSkill().isDance();
		final boolean music2 = e2.getSkill().isDance();

		if(music1 && music2)
		{
			return compareStartTime(e1, e2);
		}

		if(music1 || music2)
		{
			if (music1)
			{
				return 1;
			}
			else
			{
				return -1;
			}
		}

		final boolean offensive1 = e1.getSkill().isDebuff();
		final boolean offensive2 = e2.getSkill().isDebuff();

		if(offensive1 && offensive2)
		{
			return compareStartTime(e1, e2);
		}

		if(offensive1 || offensive2)
		{
			if (!offensive1)
			{
				return 1;
			}
			else
			{
				return -1;
			}
		}

		final boolean trigger1 = e1.getSkill().isTriggeredSkill();
		final boolean trigger2 = e2.getSkill().isTriggeredSkill();

		if(trigger1 && trigger2)
		{
			return compareStartTime(e1, e2);
		}

		if(trigger1 || trigger2)
		{
			if (trigger1)
			{
				return 1;
			}
			else
			{
				return -1;
			}
		}
		return compareStartTime(e1, e2);
	}

	private int compareStartTime(Effect o1, Effect o2)
	{
		if (o1.getPeriodStartTicks() > o2.getPeriodStartTicks())
		{
			return 1;
		}

		if (o1.getPeriodStartTicks() < o2.getPeriodStartTicks())
		{
			return -1;
		}
		return 0;
	}
}