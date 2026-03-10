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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import gameserver.Config;
import gameserver.model.actor.instance.player.impl.BuffsBackTask;
import gameserver.model.skills.Skill;
import gameserver.model.skills.effects.Effect;
import gameserver.model.skills.effects.EffectTemplate;
import gameserver.model.skills.effects.EffectType;
import gameserver.model.stats.Env;
import gameserver.model.stats.Formulas;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.SystemMessage;

public class StealBuffs extends Effect
{
	private final String _slot;
	private final int _rate;
	private final int _min;
	private final int _max;

	public StealBuffs(Env env, EffectTemplate template)
	{
		super(env, template);

		_slot = template.getParameters().getString("slot", null);
		_rate = template.getParameters().getInteger("rate", 0);
		_min = template.getParameters().getInteger("min", 0);
		_max = template.getParameters().getInteger("max", 0);
	}

	@Override
	public boolean canBeStolen()
	{
		return false;
	}

	@Override
	public EffectType getEffectType()
	{
		return EffectType.NONE;
	}

	@Override
	public boolean onStart()
	{
		final var effector = getEffector();
		final var effected = getEffected();
		if (effector != null && (effected != null && effected.isPlayer()) && (effector != effected))
		{
			final var toSteal = Formulas.calcCancelStealEffects(effector, effected, getSkill(), _slot, _rate, _min, _max, false, true, true);
			if (toSteal.isEmpty())
			{
				return false;
			}
			
			final boolean isBuffSlot = _slot.equalsIgnoreCase("buff");
			final List<Effect> effects = new ArrayList<>(toSteal.size());
			
			final Map<Skill, Effect> skillIds = new ConcurrentHashMap<>();
			for (final var eff : toSteal)
			{
				final var skill = eff.getSkill();
				if (!skillIds.containsKey(skill))
				{
					skillIds.put(skill, eff);
				}
			}
			
			final Env env = new Env();
			env.setCharacter(effected);
			env.setTarget(effector);

			for (final var stats : skillIds.entrySet())
			{
				final var skill = stats.getKey();
				final var effect = stats.getValue();
				if (skill.hasEffects())
				{
					env.setSkill(skill);
					Effect ef;
					for (final var et : skill.getEffectTemplates())
					{
						ef = et.getEffect(env);
						if (ef != null)
						{
							if (Config.RESTORE_DISPEL_SKILLS && isBuffSlot)
							{
								if (skill.hasEffectType(EffectType.HEAL_OVER_TIME) || skill.hasEffectType(EffectType.CPHEAL_OVER_TIME) || skill.hasEffectType(EffectType.MANA_HEAL_OVER_TIME))
								{
									continue;
								}
								final Effect efR = ef.getEffectTemplate().getEffect(new Env(effect.getEffector(), effect.getEffected(), effect.getSkill()));
								efR.setCount(effect.getTickCount());
								efR.setAbnormalTime(effect.getAbnormalTime());
								efR.setFirstTime(effect.getTime());
								effects.add(efR);
							}
							ef.setCount(effect.getTickCount());
							ef.setAbnormalTime(effect.getAbnormalTime());
							ef.setFirstTime(effect.getTime());
							ef.scheduleEffect(true, false);
							
							if (ef.isIconDisplay() && effector.isPlayer())
							{
								final var sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
								sm.addSkillName(effect);
								effector.sendPacket(sm);
							}
						}
					}
				}
				if (!isBuffSlot && effect.triggersChanceSkill())
				{
					effected.removeChanceEffect(effect);
				}
				effected.stopSkillEffects(skill.getId(), false);
			}
			effector.updateEffectIcons();
			effected.updateEffectIcons();
			if (Config.RESTORE_DISPEL_SKILLS && !effects.isEmpty())
			{
				effected.getActingPlayer().getPersonalTasks().addTask(new BuffsBackTask(Config.RESTORE_DISPEL_SKILLS_TIME * 1000, effects));
			}
			return true;
		}

		if (getSkill().hasSelfEffects())
		{
			final var effect = effector.getFirstEffect(getSkill().getId());
			if ((effect != null) && effect.isSelfEffect())
			{
				effect.exit(false);
			}
			getSkill().getEffectsSelf(effector, true);
		}
		return false;
	}
}