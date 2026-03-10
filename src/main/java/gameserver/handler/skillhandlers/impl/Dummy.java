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

import gameserver.handler.skillhandlers.ISkillHandler;
import gameserver.instancemanager.HandysBlockCheckerManager;
import gameserver.model.GameObject;
import gameserver.model.ShotType;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Player;
import gameserver.model.actor.instance.BlockInstance;
import gameserver.model.skills.Skill;
import gameserver.model.skills.SkillType;
import gameserver.model.stats.Env;
import gameserver.model.stats.Formulas;
import gameserver.network.SystemMessageId;

public class Dummy implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS =
	{
	        SkillType.DUMMY
	};

	@Override
	public void useSkill(Creature activeChar, Skill skill, GameObject[] targets, double cubicPower)
	{
		switch (skill.getId())
		{
			case 5852 :
			case 5853 :
			{
				final var obj = targets[0];
				if (obj != null)
				{
					useBlockCheckerSkill(activeChar.getActingPlayer(), skill, obj);
				}
				break;
			}
			case 254 :
			case 302 :
				if (skill.hasEffects())
				{
					for (final var tgt : targets)
					{
						if (tgt == null || !tgt.isCreature())
						{
							continue;
						}
						final var target = (Creature) tgt;
						if (target != null)
						{
							skill.getEffects(activeChar, target, true, true);
						}
					}
				}
				break;
			default :
			{
				if (skill.isDebuff() && !skill.isIgnoreCalcChance())
				{
					final boolean ss = cubicPower > 0 ? false : skill.useSoulShot() && activeChar.isChargedShot(ShotType.SOULSHOTS);
					final boolean sps = cubicPower > 0 ? false : skill.useSpiritShot() && activeChar.isChargedShot(ShotType.SPIRITSHOTS);
					final boolean bss = cubicPower > 0 ? false : skill.useSpiritShot() && activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOTS);
					byte shld = 0;
					for (final var tgt : targets)
					{
						if (tgt == null || !tgt.isCreature())
						{
							continue;
						}
						var target = (Creature) tgt;
						if (target != null)
						{
							if (Formulas.calcSkillReflect(target, skill) == Formulas.SKILL_REFLECT_SUCCEED)
							{
								target = activeChar;
							}
							
							shld = Formulas.calcShldUse(activeChar, target, skill);
							if (Formulas.calcSkillSuccess(activeChar, target, skill, null, skill.getPower(), shld, ss, sps, bss))
							{
								skill.getEffects(activeChar, target, new Env(shld, ss, sps, bss), true, true);
							}
							else
							{
								activeChar.sendPacket(SystemMessageId.ATTACK_FAILED);
							}
							Formulas.calcLethalHit(activeChar, target, skill);
						}
					}
				}
				else
				{
					if (skill.hasEffects())
					{
						for (final var tgt : targets)
						{
							if (tgt == null || !tgt.isCreature())
							{
								continue;
							}
							final var target = (Creature) tgt;
							if (target != null)
							{
								skill.getEffects(activeChar, target, true, true);
							}
						}
					}
				}
				break;
			}
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
		
		if (skill.useSpiritShot())
		{
			activeChar.setChargedShot(activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOTS) ? ShotType.BLESSED_SPIRITSHOTS : ShotType.SPIRITSHOTS, false);
		}
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
	
	private final void useBlockCheckerSkill(Player activeChar, Skill skill, GameObject target)
	{
		if (!(target instanceof BlockInstance))
		{
			return;
		}
		
		final var block = (BlockInstance) target;
		
		final int arena = activeChar.getBlockCheckerArena();
		if (arena != -1)
		{
			final var holder = HandysBlockCheckerManager.getInstance().getHolder(arena);
			if (holder == null)
			{
				return;
			}
			
			final int team = holder.getPlayerTeam(activeChar);
			final int color = block.getColorEffect();
			if ((team == 0) && (color == 0x00))
			{
				block.changeColor(activeChar, holder, team);
			}
			else if ((team == 1) && (color == 0x53))
			{
				block.changeColor(activeChar, holder, team);
			}
		}
	}
}