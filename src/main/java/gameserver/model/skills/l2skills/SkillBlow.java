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
package gameserver.model.skills.l2skills;

import l2e.commons.util.Rnd;
import gameserver.Config;
import gameserver.model.GameObject;
import gameserver.model.ShotType;
import gameserver.model.actor.Creature;
import gameserver.model.skills.Skill;
import gameserver.model.skills.effects.Effect;
import gameserver.model.stats.Env;
import gameserver.model.stats.Formulas;
import gameserver.model.stats.Stats;
import gameserver.model.stats.StatsSet;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.SystemMessage;
import gameserver.utils.Log;

public class SkillBlow extends Skill
{
	public SkillBlow(StatsSet set)
	{
		super(set);
	}
	
	@Override
	public boolean calcCriticalBlow(Creature caster, Creature target)
	{
		return Formulas.calcBlowSuccess(caster, target, this);
	}
	
	@Override
	public void useSkill(Creature activeChar, GameObject[] targets, double cubicPower)
	{
		if (activeChar.isAlikeDead())
		{
			return;
		}

		final boolean ss = useSoulShot() && activeChar.isChargedShot(ShotType.SOULSHOTS);
		final boolean sps = useSpiritShot() && activeChar.isChargedShot(ShotType.SPIRITSHOTS);
		final boolean bss = useSpiritShot() && activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOTS);
		for (final var target : (Creature[]) targets)
		{
			if (target.isAlikeDead())
			{
				continue;
			}

			final boolean skillIsEvaded = Formulas.calcPhysicalSkillEvasion(activeChar, target, this);
			final var castSkill = activeChar.getCastingSkill();
			final var calcSuccess = activeChar.isCriticalBlowCastingSkill() && castSkill != null && castSkill == this;
			if (!skillIsEvaded && calcSuccess)
			{
				final byte reflect = Formulas.calcSkillReflect(target, this);
				if (hasEffects())
				{
					if (reflect == Formulas.SKILL_REFLECT_SUCCEED)
					{
						activeChar.stopSkillEffects(getId(), true);
						getEffects(target, activeChar, true, true);
						final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
						sm.addSkillName(this);
						activeChar.sendPacket(sm);
					}
					else
					{
						getEffects(activeChar, target, new Env(Formulas.calcShldUse(activeChar, target, this), ss, sps, bss), true, true);
					}
				}

				final byte shld = Formulas.calcShldUse(activeChar, target, this);
				double damage = isStaticDamage() ? getPower() : (int) Formulas.calcBlowDamage(activeChar, target, this, shld, ss);
				if (!isStaticDamage() && (getMaxSoulConsumeCount() > 0) && activeChar.isPlayer())
				{
					final int chargedSouls = (activeChar.getActingPlayer().getChargedSouls() <= getMaxSoulConsumeCount()) ? activeChar.getActingPlayer().getChargedSouls() : getMaxSoulConsumeCount();
					damage *= 1 + (chargedSouls * 0.04);
				}

				final boolean crit = Rnd.chance(Formulas.calcCrit(activeChar, target, this, true));
				if (!isStaticDamage() && crit)
				{
					damage *= 2;
				}

				if (Config.LOG_GAME_DAMAGE && activeChar.isPlayable() && (damage > Config.LOG_GAME_DAMAGE_THRESHOLD))
				{
					Log.addLogDamage((int) damage, "BLOW", this, activeChar.getActingPlayer(), target);
				}

				final double drainPercent = activeChar.calcStat(Stats.DRAIN_PERCENT, 0, null, null);
				if (drainPercent != 0)
				{
					Formulas.calcDrainDamage(activeChar, target, (int) damage, 0, drainPercent);
				}
				target.reduceCurrentHp(damage, activeChar, this);
				if (activeChar.isPlayer())
				{
					final var activePlayer = activeChar.getActingPlayer();
					activePlayer.sendDamageMessage(target, (int) damage, this, false, true, false);
				}
				
				if ((reflect & Formulas.SKILL_REFLECT_VENGEANCE) != 0)
				{
					Formulas.calcReflectDamage(activeChar, target, this, damage, crit);
				}

				if (!target.isRaid() && Formulas.calcAtkBreak(target, crit))
				{
					target.breakAttack();
					target.breakCast();
				}
			}

			Formulas.calcLethalHit(activeChar, target, this);

			if (hasSelfEffects())
			{
				final Effect effect = activeChar.getFirstEffect(getId());
				if ((effect != null) && effect.isSelfEffect())
				{
					effect.exit(false);
				}
				getEffectsSelf(activeChar, true);
			}
			activeChar.setChargedShot(ShotType.SOULSHOTS, false);
		}
	}
}