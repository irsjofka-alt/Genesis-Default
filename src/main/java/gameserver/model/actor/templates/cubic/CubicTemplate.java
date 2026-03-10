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
package gameserver.model.actor.templates.cubic;

import java.util.ArrayList;
import java.util.List;

import gameserver.model.GameObject;
import gameserver.model.actor.Creature;
import gameserver.model.actor.instance.CubicInstance;
import gameserver.model.actor.templates.cubic.conditions.ICubicCondition;
import gameserver.model.base.CubicTargetType;
import gameserver.model.stats.StatsSet;

public class CubicTemplate implements ICubicConditionHolder
{
	private final int _id;
	private final int _level;
	private final int _duration;
	private final int _delay;
	private final int _maxCount;
	private final double _power;
	private final CubicTargetType _targetType;
	private final List<ICubicCondition> _conditions = new ArrayList<>();
	public List<CubicSkill> _skills = new ArrayList<>();
	
	public CubicTemplate(StatsSet set)
	{
		_id = set.getInteger("id");
		_level = set.getInteger("val");
		_duration = set.getInteger("duration");
		_delay = set.getInteger("delay");
		_maxCount = set.getInteger("maxCount");
		_power = set.getDouble("power");
		_targetType = set.getEnum("targetType", CubicTargetType.class, CubicTargetType.TARGET);
	}
	
	public int getId()
	{
		return _id;
	}
	
	public int getLevel()
	{
		return _level;
	}
	
	public int getDuration()
	{
		return _duration;
	}
	
	public int getDelay()
	{
		return _delay;
	}
	
	public int getMaxCount()
	{
		return _maxCount;
	}
	
	public double getPower()
	{
		return _power;
	}
	
	public CubicTargetType getTargetType()
	{
		return _targetType;
	}
	
	public List<CubicSkill> getSkills()
	{
		return _skills;
	}
	
	@Override
	public boolean validateConditions(CubicInstance cubic, Creature owner, GameObject target)
	{
		return _conditions.isEmpty() || _conditions.stream().allMatch(condition -> condition.test(cubic, owner, target));
	}
	
	@Override
	public void addConditions(List<ICubicCondition> condition)
	{
		_conditions.addAll(condition);
	}
}
