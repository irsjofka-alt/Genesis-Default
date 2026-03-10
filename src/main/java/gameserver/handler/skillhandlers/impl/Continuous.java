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
import gameserver.ai.model.CtrlEvent;
import gameserver.ai.model.CtrlIntention;
import gameserver.data.parser.SkillsParser;
import gameserver.handler.skillhandlers.ISkillHandler;
import gameserver.model.GameObject;
import gameserver.model.ShotType;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Player;
import gameserver.model.actor.instance.ClanHallManagerInstance;
import gameserver.model.skills.Skill;
import gameserver.model.skills.SkillType;
import gameserver.model.stats.Env;
import gameserver.model.stats.Formulas;
import gameserver.network.SystemMessageId;

public class Continuous implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS =
	{
	        SkillType.BUFF, SkillType.DEBUFF, SkillType.DOT, SkillType.MDOT, SkillType.POISON, SkillType.BLEED, SkillType.FEAR, SkillType.CONT, SkillType.AGGDEBUFF, SkillType.FUSION
	};

	@Override
	public void useSkill(Creature activeChar, Skill skill, GameObject[] targets, double cubicPower)
	{
		boolean acted = true;

		Player player = null;
		if (activeChar.isPlayer())
		{
			player = activeChar.getActingPlayer();
		}

		if (skill.getEffectId() != 0)
		{
			final Skill sk = SkillsParser.getInstance().getInfo(skill.getEffectId(), skill.getEffectLvl() == 0 ? 1 : skill.getEffectLvl());

			if (sk != null)
			{
				skill = sk;
			}
		}

		final boolean ss = cubicPower > 0 ? false : skill.useSoulShot() && activeChar.isChargedShot(ShotType.SOULSHOTS);
		final boolean sps = cubicPower > 0 ? false : skill.useSpiritShot() && activeChar.isChargedShot(ShotType.SPIRITSHOTS);
		final boolean bss = cubicPower > 0 ? false : skill.useSpiritShot() && activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOTS);
		final var isBuff = cubicPower > 0 ? false : skill.getSkillType() == SkillType.BUFF;
		for (final var obj : targets)
		{
			if (!(obj instanceof Creature))
			{
				continue;
			}
			Creature target = (Creature) obj;
			
			byte shld = 0;
			
			if (target.isBuffImmune() && !skill.hasDebuffEffects())
			{
				continue;
			}
			final var cantRef = isBuff && target.isPlayer() && player != null && player.isFriend(target.getActingPlayer());
			if (!cantRef && Formulas.calcSkillReflect(target, skill) == Formulas.SKILL_REFLECT_SUCCEED)
			{
				target = activeChar;
			}

			if (isBuff && !(activeChar instanceof ClanHallManagerInstance))
			{
				if (target != activeChar)
				{
					if (target.isPlayer())
					{
						final Player trg = target.getActingPlayer();
						if (trg.isCursedWeaponEquipped())
						{
							continue;
						}
						else if (trg.getBlockCheckerArena() != -1)
						{
							continue;
						}
						else if (!target.equals(activeChar) && activeChar.isPlayer() && activeChar.getActingPlayer().isInFightEvent())
						{
							if (!activeChar.getActingPlayer().getFightEvent().canUsePositiveMagic(activeChar, target))
							{
								continue;
							}
						}
					}
					else if ((player != null) && player.isCursedWeaponEquipped())
					{
						continue;
					}
				}

				if (Config.ALLOW_BLOCKBUFFS_COMMAND && target.isPlayable() && activeChar.isPlayable() && !target.getActingPlayer().equals(activeChar))
				{
					final Player pTarget = target.getActingPlayer();
					if (pTarget.getBlockBuffs() && !pTarget.isInOlympiadMode() && !Skill.getBlockBuffConditions(activeChar, pTarget))
					{
						continue;
					}
				}
			}

			if (skill.isOffensive() || skill.isDebuff())
			{
				shld = Formulas.calcShldUse(activeChar, target, skill);
				acted = Formulas.calcSkillSuccess(activeChar, target, skill, null, skill.getPower(), shld, ss, sps, bss);
			}

			if (acted)
			{
				if (skill.isToggle())
				{
					final var effects = target.getAllEffects();
					if (effects != null)
					{
						for (final var e : effects)
						{
							if (e != null && e.getSkill().getId() == skill.getId())
							{
								e.exit(true);
								return;
							}
						}
					}
				}

				final var effects = skill.getEffects(activeChar, target, new Env(shld, ss, sps, bss), true, true);
				final var summon = target.getSummon();
				if (isBuff && (summon != null) && (summon != activeChar) && summon.isServitor() && effects.size() > 0 && skill.isApplyOnSummon())
				{
					if (effects.getFirst().canBeStolen() && !skill.isOffensive() && !skill.isTriggeredSkill() && !skill.isToggle())
					{
						skill.getEffects(activeChar, summon, new Env(shld, ss, sps, bss), true, true);
					}
				}

				if (skill.getSkillType() == SkillType.AGGDEBUFF)
				{
					if (target.isAttackable())
					{
						target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, (int) skill.getPower());
					}
					else if (target.isPlayable())
					{
						if (target.getTarget() == activeChar)
						{
							target.getAI().setIntention(CtrlIntention.ATTACK, activeChar);
						}
						else
						{
							target.setTarget(activeChar);
						}
					}
				}
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.ATTACK_FAILED);
			}
			Formulas.calcLethalHit(activeChar, target, skill);
		}

		if (skill.hasSelfEffects())
		{
			final var effect = activeChar.getFirstEffect(skill.getId());
			if ((effect != null) && effect.isSelfEffect())
			{
				effect.exit(false);
			}
			skill.getEffectsSelf(activeChar, true);
		}
		activeChar.setChargedShot(bss ? ShotType.BLESSED_SPIRITSHOTS : ShotType.SPIRITSHOTS, false);
	}

	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}