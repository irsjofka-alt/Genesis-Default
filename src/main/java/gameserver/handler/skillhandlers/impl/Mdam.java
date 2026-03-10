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
package gameserver.handler.skillhandlers.impl;

import gameserver.Config;
import gameserver.handler.skillhandlers.ISkillHandler;
import gameserver.model.GameObject;
import gameserver.model.ShotType;
import gameserver.model.actor.Creature;
import gameserver.model.skills.Skill;
import gameserver.model.skills.SkillType;
import gameserver.model.skills.effects.Effect;
import gameserver.model.stats.Env;
import gameserver.model.stats.Formulas;
import gameserver.model.stats.Stats;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.SystemMessage;
import gameserver.utils.Log;

public class Mdam implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS =
	{
	        SkillType.MDAM, SkillType.DEATHLINK
	};

	@Override
	public void useSkill(Creature activeChar, Skill skill, GameObject[] targets, double cubicPower)
	{
		if (activeChar.isAlikeDead() && skill.getId() != 1155)
		{
			return;
		}
		
		final boolean ss = cubicPower > 0 ? false : skill.useSoulShot() && activeChar.isChargedShot(ShotType.SOULSHOTS);
		final boolean sps = cubicPower > 0 ? false : skill.useSpiritShot() && activeChar.isChargedShot(ShotType.SPIRITSHOTS);
		final boolean bss = cubicPower > 0 ? false : skill.useSpiritShot() && activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOTS);

		for (final Creature tgt : (Creature[]) targets)
		{
			if (tgt == null || !tgt.isCreature())
			{
				continue;
			}
			final var target = tgt;
			
			if (target.isPlayer() && target.getActingPlayer().isFakeDeathNow())
			{
				target.stopFakeDeath(true);
			}

			final boolean mcrit = Formulas.calcMCrit(activeChar.getMCriticalHit(target, skill));
			final byte shld = Formulas.calcShldUse(activeChar, target, skill);
			final byte reflect = Formulas.calcSkillReflect(target, skill);

			int damage = skill.isStaticDamage() ? (int) skill.getPower() : (int) Formulas.calcMagicDam(activeChar, target, skill, shld, sps, bss, mcrit, cubicPower);

			if (!skill.isStaticDamage() && skill.getDependOnTargetBuff())
			{
				damage *= (((target.getBuffCount() * 0.3) + 1.3) / 4);
			}

			if (!skill.isStaticDamage() && (skill.getMaxSoulConsumeCount() > 0) && activeChar.isPlayer())
			{
				final int chargedSouls = (activeChar.getActingPlayer().getChargedSouls() <= skill.getMaxSoulConsumeCount()) ? activeChar.getActingPlayer().getChargedSouls() : skill.getMaxSoulConsumeCount();
				damage *= 1 + (chargedSouls * 0.04);
			}

			Formulas.calcLethalHit(activeChar, target, skill);
			
			boolean allowDamage = true;
			if (skill.getId() == 1400 && (skill.getLevel() < 300 || skill.getLevel() > 330))
			{
				allowDamage = false;
			}
			
			if (!target.isAlikeDead() && (damage > 0 || skill.getId() == 1400))
			{
				final double drainPercent = activeChar.calcStat(Stats.DRAIN_PERCENT, 0, null, null);
				if (drainPercent != 0)
				{
					Formulas.calcDrainDamage(activeChar, target, damage, 0, drainPercent);
				}
				
				if (allowDamage)
				{
					if (!target.isRaid() && Formulas.calcAtkBreak(target, mcrit))
					{
						target.breakAttack();
						target.breakCast();
					}

					if ((reflect & Formulas.SKILL_REFLECT_VENGEANCE) != 0)
					{
						activeChar.reduceCurrentHp(damage, target, skill);
					}
					else
					{
						activeChar.sendDamageMessage(target, damage, skill, mcrit, false, false);
						target.reduceCurrentHp(damage, activeChar, skill);
					}
				}

				if (skill.hasEffects() && skill.getId() != 1155)
				{
					if ((reflect & Formulas.SKILL_REFLECT_SUCCEED) != 0)
					{
						activeChar.stopSkillEffects(skill.getId(), true);
						skill.getEffects(target, activeChar, true, true);
						final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
						sm.addSkillName(skill);
						activeChar.sendPacket(sm);
					}
					else
					{
						skill.getEffects(activeChar, target, new Env(shld, ss, sps, bss), true, true);
					}
				}

				if (Config.LOG_GAME_DAMAGE && activeChar.isPlayable() && (damage > Config.LOG_GAME_DAMAGE_THRESHOLD))
				{
					Log.addLogDamage(damage, "MDAM", skill, activeChar.getActingPlayer(), target);
				}
			}
			else
			{
				if (skill.getId() == 1155 && skill.hasEffects() && target.isAlikeDead())
				{
					skill.getEffects(activeChar, target, true, true);
				}
			}
		}

		if (skill.hasSelfEffects())
		{
			final Effect effect = activeChar.getFirstEffect(skill.getId());
			if ((effect != null) && effect.isSelfEffect())
			{
				effect.exit(false);
			}
			skill.getEffectsSelf(activeChar, true);
		}
		activeChar.setChargedShot(bss ? ShotType.BLESSED_SPIRITSHOTS : ShotType.SPIRITSHOTS, false);

		if (skill.isSuicideAttack())
		{
			activeChar.doDie(activeChar);
		}
	}

	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}