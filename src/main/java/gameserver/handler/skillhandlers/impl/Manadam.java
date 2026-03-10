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

import l2e.commons.util.Rnd;
import gameserver.handler.skillhandlers.ISkillHandler;
import gameserver.model.GameObject;
import gameserver.model.ShotType;
import gameserver.model.actor.Creature;
import gameserver.model.skills.Skill;
import gameserver.model.skills.SkillType;
import gameserver.model.skills.effects.Effect;
import gameserver.model.stats.Env;
import gameserver.model.stats.Formulas;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.StatusUpdate;
import gameserver.network.serverpackets.SystemMessage;

public class Manadam implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS =
	{
	        SkillType.MANADAM
	};
	
	@Override
	public void useSkill(Creature activeChar, Skill skill, GameObject[] targets, double cubicPower)
	{
		if (activeChar.isAlikeDead())
		{
			return;
		}

		final boolean ss = cubicPower > 0 ? false : skill.useSoulShot() && activeChar.isChargedShot(ShotType.SOULSHOTS);
		final boolean sps = cubicPower > 0 ? false : skill.useSpiritShot() && activeChar.isChargedShot(ShotType.SPIRITSHOTS);
		final boolean bss = cubicPower > 0 ? false : skill.useSpiritShot() && activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOTS);
		
		for (Creature target : (Creature[]) targets)
		{
			if (Formulas.calcSkillReflect(target, skill) == Formulas.SKILL_REFLECT_SUCCEED)
			{
				target = activeChar;
			}
			
			final int magicLevel = skill.getMagicLevel() == 0 ? activeChar.getLevel() : skill.getMagicLevel();
			final int rate = skill.isIgnoreCalcChance() ? 100 : (Rnd.get(30, 100) * target.getLevel() / magicLevel);
			final boolean acted = Rnd.chance(rate);
			if (target.isInvul() || !acted)
			{
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
				sm.addCharName(target);
				sm.addSkillName(skill);
				activeChar.sendPacket(sm);
			}
			else
			{
				if (skill.hasEffects())
				{
					skill.getEffects(activeChar, target, new Env(Formulas.calcShldUse(activeChar, target, skill), ss, sps, bss), true, true);
				}
				
				double damage = skill.isStaticDamage() ? skill.getPower() : Formulas.calcManaDam(activeChar, target, skill, ss, bss);
				
				if (!skill.isIgnoreCritDamage() && !skill.isStaticDamage() && Formulas.calcMCrit(activeChar.getMCriticalHit(target, skill)))
				{
					damage *= 3.;
					activeChar.sendPacket(SystemMessageId.CRITICAL_HIT_MAGIC);
				}
				
				final double mp = (damage > target.getCurrentMp() ? target.getCurrentMp() : damage);
				target.reduceCurrentMp(mp);
				if (damage > 0)
				{
					target.stopEffectsOnDamage(true);
				}
				
				if (target.isPlayer())
				{
					final var su = target.makeStatusUpdate(StatusUpdate.CUR_HP);
					target.sendPacket(su);
					
					final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_MP_HAS_BEEN_DRAINED_BY_C1);
					sm.addCharName(activeChar);
					sm.addNumber((int) mp);
					target.sendPacket(sm);
				}
				
				if (activeChar.isPlayer())
				{
					final SystemMessage sm2 = SystemMessage.getSystemMessage(SystemMessageId.YOUR_OPPONENTS_MP_WAS_REDUCED_BY_S1);
					sm2.addNumber((int) mp);
					activeChar.sendPacket(sm2);
				}
			}
		}
		
		if (skill.hasSelfEffects())
		{
			final Effect effect = activeChar.getFirstEffect(skill.getId());
			if (effect != null && effect.isSelfEffect())
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