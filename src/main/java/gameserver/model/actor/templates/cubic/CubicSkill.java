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
import gameserver.model.holders.SkillHolder;
import gameserver.model.stats.StatsSet;

public class CubicSkill extends SkillHolder implements ICubicConditionHolder
{
	private final int _triggerRate;
	private final int _successRate;
	private final boolean _canUseOnStaticObjects;
	private final CubicTargetType _targetType;
	private final List<ICubicCondition> _conditions = new ArrayList<>();
	private final boolean _targetDebuff;
	
	public CubicSkill(StatsSet set)
	{
		super(set.getInteger("id"), set.getInteger("level"));
		_triggerRate = set.getInteger("trigger", 100);
		_successRate = set.getInteger("chance", 100);
		_canUseOnStaticObjects = set.getBool("canUseOnStaticObjects", false);
		_targetType = set.getEnum("target", CubicTargetType.class, CubicTargetType.TARGET);
		_targetDebuff = set.getBool("targetDebuff", false);
	}
	
	public int getTriggerRate()
	{
		return _triggerRate;
	}
	
	public int getSuccessRate()
	{
		return _successRate;
	}
	
	public boolean canUseOnStaticObjects()
	{
		return _canUseOnStaticObjects;
	}
	
	public CubicTargetType getTargetType()
	{
		return _targetType;
	}
	
	public boolean isTargetingDebuff()
	{
		return _targetDebuff;
	}
	
	@Override
	public boolean validateConditions(CubicInstance cubic, Creature owner, GameObject target)
	{
		return (!_targetDebuff || (target.isCreature() && (((Creature) target).getEffectList().getDebuffs().size() > 0))) && (_conditions.isEmpty() || _conditions.stream().allMatch(condition -> condition.test(cubic, owner, target)));
	}
	
	@Override
	public void addConditions(List<ICubicCondition> list)
	{
		_conditions.addAll(list);
	}
}
