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
package gameserver.model.actor.templates.cubic.conditions;

import gameserver.model.GameObject;
import gameserver.model.actor.Creature;
import gameserver.model.actor.instance.CubicInstance;
import gameserver.model.actor.instance.DoorInstance;

public class HpCondition implements ICubicCondition
{
	private final HpConditionType _type;
	private final int _hpPer;
	
	public HpCondition(HpConditionType type, int hpPer)
	{
		_type = type;
		_hpPer = hpPer;
	}
	
	@Override
	public boolean test(CubicInstance cubic, Creature owner, GameObject target)
	{
		if (target.isCreature() || target.isDoor())
		{
			final double hpPer = (target.isDoor() ? (DoorInstance) target : (Creature) target).getCurrentHpPercents();
			switch (_type)
			{
				case GREATER:
				{
					return hpPer > _hpPer;
				}
				case LESSER:
				{
					return hpPer < _hpPer;
				}
			}
		}
		return false;
	}
	
	public enum HpConditionType
	{
		GREATER,
		LESSER;
	}
}
