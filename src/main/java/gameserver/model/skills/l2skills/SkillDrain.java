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

import gameserver.Config;
import gameserver.model.GameObject;
import gameserver.model.ShotType;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Npc;
import gameserver.model.skills.Skill;
import gameserver.model.skills.effects.Effect;
import gameserver.model.skills.targets.TargetType;
import gameserver.model.stats.Formulas;
import gameserver.model.stats.StatsSet;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.SystemMessage;
import gameserver.utils.Log;

public class SkillDrain extends Skill
{
	private final float _absorbPart;
	private final int _absorbAbs;
	
	public SkillDrain(StatsSet set)
	{
		super(set);

		_absorbPart = set.getFloat("absorbPart", 0.f);
		_absorbAbs = set.getInteger("absorbAbs", 0);
	}

	@Override
	public void useSkill(Creature activeChar, GameObject[] targets, double cubicPower)
	{
		if (activeChar.isAlikeDead())
		{
			return;
		}
		
		final boolean ss = cubicPower > 0 ? false : useSoulShot() && activeChar.isChargedShot(ShotType.SOULSHOTS);
		final boolean sps = cubicPower > 0 ? false : useSpiritShot() && activeChar.isChargedShot(ShotType.SPIRITSHOTS);
		final boolean bss = cubicPower > 0 ? false : useSpiritShot() && activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOTS);

		for (final Creature target : (Creature[]) targets)
		{
			if (target.isAlikeDead() && (getTargetType() != TargetType.CORPSE_MOB))
			{
				continue;
			}

			if ((activeChar != target) && target.isInvul())
			{
				continue;
			}

			final boolean mcrit = Formulas.calcMCrit(activeChar.getMCriticalHit(target, this));
			final byte shld = Formulas.calcShldUse(activeChar, target, this);
			final int damage = isStaticDamage() ? (int) getPower() : (int) Formulas.calcMagicDam(activeChar, target, this, shld, sps, bss, mcrit, cubicPower);

			Formulas.calcDrainDamage(activeChar, target, damage, _absorbAbs, _absorbPart);
			
			if ((damage > 0) && (!target.isDead() || (getTargetType() != TargetType.CORPSE_MOB)))
			{
				if (!target.isRaid() && Formulas.calcAtkBreak(target, mcrit))
				{
					target.breakAttack();
					target.breakCast();
				}

				activeChar.sendDamageMessage(target, damage, this, mcrit, false, false);

				if (Config.LOG_GAME_DAMAGE && activeChar.isPlayable() && damage > Config.LOG_GAME_DAMAGE_THRESHOLD)
				{
					Log.addLogDamage(damage, "PDAM", this, activeChar.getActingPlayer(), target);
				}

				if (hasEffects() && (getTargetType() != TargetType.CORPSE_MOB))
				{
					if ((Formulas.calcSkillReflect(target, this) & Formulas.SKILL_REFLECT_SUCCEED) > 0)
					{
						activeChar.stopSkillEffects(getId(), true);
						getEffects(target, activeChar, true, true);
						final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
						sm.addSkillName(getId());
						activeChar.sendPacket(sm);
					}
					else
					{
						target.stopSkillEffects(getId(), true);
						if (Formulas.calcSkillSuccess(activeChar, target, this, null, getPower(), shld, ss, sps, bss))
						{
							getEffects(activeChar, target, true, true);
						}
						else
						{
							final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
							sm.addCharName(target);
							sm.addSkillName(this);
							activeChar.sendPacket(sm);
						}
					}
				}

				target.reduceCurrentHp(damage, activeChar, this);
			}

			if (target.isDead() && getTargetType() == TargetType.CORPSE_MOB && target.isNpc())
			{
				((Npc) target).endDecayTask();
			}
		}
		
		final Effect effect = activeChar.getFirstEffect(getId());
		if ((effect != null) && effect.isSelfEffect())
		{
			effect.exit(false);
		}
		getEffectsSelf(activeChar, true);
		activeChar.setChargedShot(bss ? ShotType.BLESSED_SPIRITSHOTS : ShotType.SPIRITSHOTS, false);
	}
}