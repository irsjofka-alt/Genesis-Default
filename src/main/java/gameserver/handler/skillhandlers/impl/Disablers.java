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

import gameserver.ai.DefaultAI;
import gameserver.ai.model.CtrlEvent;
import gameserver.ai.model.CtrlIntention;
import gameserver.handler.skillhandlers.ISkillHandler;
import gameserver.model.GameObject;
import gameserver.model.ShotType;
import gameserver.model.World;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Player;
import gameserver.model.actor.Summon;
import gameserver.model.actor.instance.SiegeSummonInstance;
import gameserver.model.skills.Skill;
import gameserver.model.skills.SkillType;
import gameserver.model.skills.effects.Effect;
import gameserver.model.skills.effects.EffectType;
import gameserver.model.skills.targets.TargetType;
import gameserver.model.stats.Env;
import gameserver.model.stats.Formulas;
import gameserver.model.stats.Stats;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.SystemMessage;

public class Disablers implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS =
	{
	        SkillType.STUN, SkillType.ROOT, SkillType.SLEEP, SkillType.CONFUSION, SkillType.AGGDAMAGE, SkillType.AGGREDUCE, SkillType.AGGREDUCE_CHAR, SkillType.AGGREMOVE, SkillType.MUTE, SkillType.CONFUSE_MOB_ONLY, SkillType.PARALYZE, SkillType.ERASE, SkillType.BETRAY, SkillType.DISARM
	};

	@Override
	public void useSkill(Creature activeChar, Skill skill, GameObject[] targets, double cubicPower)
	{
		final SkillType type = skill.getSkillType();

		byte shld = 0;
		final boolean ss = cubicPower > 0 ? false : skill.useSoulShot() && activeChar.isChargedShot(ShotType.SOULSHOTS);
		final boolean sps = cubicPower > 0 ? false : skill.useSpiritShot() && activeChar.isChargedShot(ShotType.SPIRITSHOTS);
		final boolean bss = cubicPower > 0 ? false : skill.useSpiritShot() && activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOTS);

		for (final GameObject obj : targets)
		{
			if (!(obj instanceof Creature))
			{
				continue;
			}
			Creature target = (Creature) obj;
			if (target.isDead() || (target.isInvul() && !target.isParalyzed()))
			{
				continue;
			}

			shld = Formulas.calcShldUse(activeChar, target, skill);

			switch (type)
			{
				case BETRAY :
				{
					if (Formulas.calcSkillSuccess(activeChar, target, skill, null, skill.getPower(), shld, ss, sps, bss))
					{
						skill.getEffects(activeChar, target, new Env(shld, ss, sps, bss), true, true);
					}
					else
					{
						final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
						sm.addCharName(target);
						sm.addSkillName(skill);
						activeChar.sendPacket(sm);
					}
					break;
				}
				case ROOT :
				case DISARM :
				case STUN :
				case SLEEP :
				case PARALYZE :
				case CONFUSION :
				case MUTE :
				{
					if (Formulas.calcSkillReflect(target, skill) == Formulas.SKILL_REFLECT_SUCCEED)
					{
						target = activeChar;
					}

					if (Formulas.calcSkillSuccess(activeChar, target, skill, null, skill.getPower(), shld, ss, sps, bss))
					{
						skill.getEffects(activeChar, target, new Env(shld, ss, sps, bss), true, true);
					}
					else
					{
						if (activeChar.isPlayer())
						{
							final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
							sm.addCharName(target);
							sm.addSkillName(skill);
							activeChar.sendPacket(sm);
						}
					}
					break;
				}
				case CONFUSE_MOB_ONLY :
				{
					if (target.isAttackable())
					{
						if (Formulas.calcSkillSuccess(activeChar, target, skill, null, skill.getPower(), shld, ss, sps, bss))
						{
							skill.getEffects(activeChar, target, new Env(shld, ss, sps, bss), true, true);
						}
						else
						{
							if (activeChar.isPlayer())
							{
								final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
								sm.addCharName(target);
								sm.addSkillName(skill);
								activeChar.sendPacket(sm);
							}
						}
					}
					else
					{
						activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
					}
					break;
				}
				case AGGDAMAGE :
				{
					if (!target.isAutoAttackable(activeChar, false))
					{
						continue;
					}
					
					if (target.isNpc() && skill.getId() == 51)
					{
						final var ai = target.getAI();
						if (ai != null)
						{
							((DefaultAI) target.getAI()).setNotifyFriend(false);
						}
					}
					skill.getEffects(activeChar, target, new Env(shld, ss, sps, bss), true, true);
					Formulas.calcLethalHit(activeChar, target, skill);
					break;
				}
				case AGGREDUCE :
				{
					if (target.isAttackable())
					{
						skill.getEffects(activeChar, target, new Env(shld, ss, sps, bss), true, true);

						final double aggdiff = ((Attackable) target).getAggroList().getHating(activeChar) - target.calcStat(Stats.AGGRESSION, ((Attackable) target).getAggroList().getHating(activeChar), target, skill);

						if (skill.getPower() > 0)
						{
							((Attackable) target).getAggroList().reduceHate(null, (int) skill.getPower(), true);
						}
						else if (aggdiff > 0)
						{
							((Attackable) target).getAggroList().reduceHate(null, (int) aggdiff, true);
						}
					}
					break;
				}
				case AGGREDUCE_CHAR :
				{
					if (Formulas.calcSkillSuccess(activeChar, target, skill, null, skill.getPower(), shld, ss, sps, bss))
					{
						if (target.isAttackable())
						{
							final Attackable targ = (Attackable) target;
							targ.getAggroList().stopHating(activeChar);
							final var ai = targ.getAI();
							if ((targ.getAggroList().getMostHated() == null) && ai != null)
							{
								ai.setGlobalAggro(-25);
								targ.clearAggroList(false);
								ai.setIntention(CtrlIntention.ACTIVE);
								targ.setWalking();
							}
						}
						skill.getEffects(activeChar, target, new Env(shld, ss, sps, bss), true, true);
					}
					else
					{
						if (activeChar.isPlayer())
						{
							final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
							sm.addCharName(target);
							sm.addSkillName(skill);
							activeChar.sendPacket(sm);
						}
						target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar, 0);
					}
					break;
				}
				case AGGREMOVE :
				{
					final var isAddToBlockList = skill.hasUnAggroEffects();
					if (skill.getTargetType() == TargetType.SELF)
					{
						final int maxTargets = skill.getAffectLimit();
						int targetList = 0;
						for (final var npc : World.getAroundNpc(activeChar, skill.getAffectRange(), 200))
						{
							if (npc.isAttackable() && !npc.isRaid())
							{
								if ((maxTargets > 0) && (targetList >= maxTargets))
								{
									break;
								}
								targetList++;
								final var tgt = ((Attackable) npc);
								if (Formulas.calcSkillSuccess(activeChar, tgt, skill, null, skill.getPower(), shld, ss, sps, bss))
								{
									if (skill.getTargetType() == TargetType.UNDEAD || skill.getId() == 1034)
									{
										if (tgt.isUndead())
										{
											tgt.getAggroList().reduceHate(null, tgt.getAggroList().getHating(tgt.getAggroList().getMostHated()), !isAddToBlockList);
											if (skill.hasEffects())
											{
												skill.getEffects(activeChar, tgt, true, true);
											}
										}
									}
									else
									{
										tgt.getAggroList().reduceHate(null, tgt.getAggroList().getHating(tgt.getAggroList().getMostHated()), !isAddToBlockList);
										if (skill.hasEffects())
										{
											skill.getEffects(activeChar, tgt, true, true);
										}
									}
								}
								else
								{
									if (activeChar.isPlayer())
									{
										final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
										sm.addCharName(tgt);
										sm.addSkillName(skill);
										activeChar.sendPacket(sm);
									}
									
									if (!tgt.isInBlockList(activeChar.getObjectId()))
									{
										tgt.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar, 0);
									}
								}
							}
							else
							{
								npc.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar, 0);
							}
						}
					}
					else
					{
						if (target.isAttackable() && !target.isRaid())
						{
							final var tgt = ((Attackable) target);
							if (skill.getId() == 1049 && !tgt.isInBlockList(activeChar.getObjectId()))
							{
								continue;
							}
							
							if (Formulas.calcSkillSuccess(activeChar, tgt, skill, null, skill.getPower(), shld, ss, sps, bss))
							{
								if (skill.getTargetType() == TargetType.UNDEAD)
								{
									if (tgt.isUndead())
									{
										tgt.getAggroList().reduceHate(null, tgt.getAggroList().getHating(tgt.getAggroList().getMostHated()), !isAddToBlockList);
										if (skill.hasEffects())
										{
											skill.getEffects(activeChar, tgt, true, true);
										}
									}
								}
								else
								{
									tgt.getAggroList().reduceHate(null, tgt.getAggroList().getHating(tgt.getAggroList().getMostHated()), !isAddToBlockList);
									if (skill.hasEffects())
									{
										skill.getEffects(activeChar, tgt, true, true);
									}
								}
							}
							else
							{
								if (activeChar.isPlayer())
								{
									final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
									sm.addCharName(tgt);
									sm.addSkillName(skill);
									activeChar.sendPacket(sm);
								}
								
								if (!tgt.isInBlockList(activeChar.getObjectId()))
								{
									tgt.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar, 0);
								}
							}
						}
						else
						{
							target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar, 0);
						}
					}
					break;
				}
				case ERASE :
				{
					if (Formulas.calcSkillSuccess(activeChar, target, skill, null, skill.getPower(), shld, ss, sps, bss) && !(target instanceof SiegeSummonInstance))
					{
						final Player summonOwner = ((Summon) target).getOwner();
						final Summon summon = summonOwner.getSummon();
						if (summon != null)
						{
							if (summon.isPhoenixBlessed())
							{
								if (summon.isNoblesseBlessed())
								{
									summon.stopEffects(EffectType.NOBLESSE_BLESSING, true);
								}
							}
							else if (summon.isNoblesseBlessed())
							{
								summon.stopEffects(EffectType.NOBLESSE_BLESSING, true);
							}
							else
							{
								summon.stopAllEffectsExceptThoseThatLastThroughDeath();
							}
							summon.abortAttack();
							summon.abortCast();
							summon.unSummon(summonOwner);
							summonOwner.sendPacket(SystemMessageId.YOUR_SERVITOR_HAS_VANISHED);
						}
					}
					else
					{
						if (activeChar.isPlayer())
						{
							final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
							sm.addCharName(target);
							sm.addSkillName(skill);
							activeChar.sendPacket(sm);
						}
					}
					break;
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
	}

	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}