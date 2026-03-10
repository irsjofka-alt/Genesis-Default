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
package gameserver.handler.effecthandlers.impl;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import gameserver.model.skills.effects.Effect;
import gameserver.model.skills.effects.EffectTemplate;
import gameserver.model.skills.effects.EffectType;
import gameserver.model.stats.Env;

public class CancelBySlot extends Effect
{
	private final String _dispel;
	private final Map<String, Integer> _dispelAbnormals;
	
	public CancelBySlot(Env env, EffectTemplate template)
	{
		super(env, template);
		
		_dispel = template.getParameters().getString("dispel", null);
		if ((_dispel != null) && !_dispel.isEmpty())
		{
			_dispelAbnormals = new ConcurrentHashMap<>();
			for (final String ngtStack : _dispel.split(";"))
			{
				final String[] ngt = ngtStack.split(",");
				_dispelAbnormals.put(ngt[0], Integer.parseInt(ngt[1]));
			}
		}
		else
		{
			_dispelAbnormals = Collections.<String, Integer> emptyMap();
		}
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.CANCEL_BY_SLOT;
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public boolean onStart()
	{
		if (_dispelAbnormals.isEmpty())
		{
			return false;
		}
		
		final var target = getEffected();
		if ((target == null) || target.isDead())
		{
			return false;
		}
		
		boolean found = false;
		for (final var value : _dispelAbnormals.entrySet())
		{
			final String stackType = value.getKey();
			final float stackOrder = value.getValue();
			final int skillCast = getSkill().getId();
			for (final var e : target.getAllEffects())
			{
				if (!e.getSkill().canBeDispeled() && skillCast != 2060 && skillCast != 2530)
				{
					continue;
				}
				
				if (stackType.equalsIgnoreCase(e.getAbnormalType()) && (e.getSkill().getId() != skillCast))
				{
					if (e.getSkill() != null)
					{
						if (e.triggersChanceSkill())
						{
							target.removeChanceEffect(e);
						}
						
						if (stackOrder == -1)
						{
							target.stopSkillEffects(e.getSkill().getId(), false);
							found = true;
						}
						else if (stackOrder >= e.getAbnormalLvl())
						{
							target.stopSkillEffects(e.getSkill().getId(), false);
							found = true;
						}
					}
				}
			}
		}
		
		if (found)
		{
			target.updateEffectIcons();
		}
		return true;
	}
}
