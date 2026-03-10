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

import gameserver.model.actor.Creature;
import gameserver.model.skills.effects.Effect;
import gameserver.model.skills.effects.EffectTemplate;
import gameserver.model.skills.effects.EffectType;
import gameserver.model.stats.Env;
import gameserver.model.stats.Formulas;

/**
 * Created by LordWinter
 */
public class CancelProbability extends Effect
{
	private final String _dispel;
	private final Map<String, Byte> _dispelAbnormals;
	private final int _rate;
	
	public CancelProbability(Env env, EffectTemplate template)
	{
		super(env, template);
		
		_dispel = template.getParameters().getString("dispel", null);
		_rate = template.getParameters().getInteger("rate", 0);
		if ((_dispel != null) && !_dispel.isEmpty())
		{
			_dispelAbnormals = new ConcurrentHashMap<>();
			for (final String ngtStack : _dispel.split(";"))
			{
				final String[] ngt = ngtStack.split(",");
				_dispelAbnormals.put(ngt[0], (ngt.length > 1) ? Byte.parseByte(ngt[1]) : Byte.MAX_VALUE);
			}
		}
		else
		{
			_dispelAbnormals = Collections.<String, Byte> emptyMap();
		}
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.CANCEL;
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
		
		final Creature target = getEffected();
		if ((target == null) || target.isDead())
		{
			return false;
		}
		
		final boolean found = false;
		for (final var value : _dispelAbnormals.entrySet())
		{
			final var stackType = value.getKey();
			final var stackOrder = value.getValue();
			final var skillCast = getSkill().getId();
			
			for (final var e : target.getAllEffects())
			{
				if (!e.getSkill().canBeDispeled())
				{
					continue;
				}
					
				if (!Formulas.calcStealSuccess(getEffector(), target, getSkill(), _rate))
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
						}
						else if (stackOrder >= e.getAbnormalLvl())
						{
							target.stopSkillEffects(e.getSkill().getId(), false);
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
