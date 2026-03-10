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
package gameserver.ai.guard;

import static gameserver.ai.model.CtrlIntention.ACTIVE;
import static gameserver.ai.model.CtrlIntention.ATTACK;

import java.util.List;
import java.util.concurrent.Future;

import l2e.commons.util.Rnd;
import gameserver.Config;
import gameserver.ThreadPoolManager;
import gameserver.ai.character.CharacterAI;
import gameserver.ai.model.CtrlEvent;
import gameserver.ai.model.CtrlIntention;
import gameserver.geodata.GeoEngine;
import gameserver.model.World;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Playable;
import gameserver.model.actor.Player;
import gameserver.model.actor.instance.DefenderInstance;
import gameserver.model.skills.Skill;
import gameserver.taskmanager.AiTaskManager;
import gameserver.utils.Util;

public class GuardAI extends CharacterAI implements Runnable
{
	private Future<?> _aiTask;
	private long _attackTimeout;
	private int _globalAggro;
	private boolean _thinking;
	private final int _attackRange;
	
	private final Skill[] _damSkills, _debuffSkills, _healSkills, _buffSkills, _stunSkills;

	public GuardAI(Attackable actor)
	{
		super(actor);

		_attackTimeout = Long.MAX_VALUE;
		_globalAggro = -10;
		_attackRange = actor.getPhysicalAttackRange();
		_damSkills = actor.getTemplate().getDamageSkills();
		_debuffSkills = actor.getTemplate().getDebuffSkills();
		_buffSkills = actor.getTemplate().getBuffSkills();
		_stunSkills = actor.getTemplate().getStunSkills();
		_healSkills = actor.getTemplate().getHealSkills();
	}
	
	@Override
	public void run()
	{
		onEvtThink();
	}

	protected boolean checkAggression(Creature target)
	{
		final var me = getActiveChar();
		if (target == null || me == null)
		{
			return false;
		}
		
		if (target.isAlikeDead())
		{
			return false;
		}
		
		final var player = target.getActingPlayer();
		if (player == null || (player.getClan() != null && (player.getClan().getCastleId() == me.getCastle().getId())))
		{
			return false;
		}

		if (target.isSummon() && me.isInsideRadius(player, 1000, true, false))
		{
			target = player;
		}

		if (target instanceof Playable playable)
		{
			if (playable.isSilentMoving() && Rnd.chance(90))
			{
				return false;
			}
		}
		return (me.isAutoAttackable(target, false) && GeoEngine.getInstance().canSeeTarget(me, target));

	}

	@Override
	protected void onIntentionAttack(Creature target, boolean shift)
	{
		if (!isActive())
		{
			startAITask();
		}
		_attackTimeout = System.currentTimeMillis() + 30000L;
		super.onIntentionAttack(target, shift);
	}
	
	protected void thinkActive()
	{
		final var npc = getActiveChar();
		if (npc.isActionsDisabled())
		{
			return;
		}
		
		var aggroRange = 0;
		if (npc.getFaction().isNone())
		{
			aggroRange = _attackRange;
		}
		else
		{
			aggroRange = npc.getFaction().getRange();
		}

		if (_globalAggro != 0)
		{
			if (_globalAggro < 0)
			{
				_globalAggro++;
			}
			else
			{
				_globalAggro--;
			}
		}

		if (_globalAggro >= 0)
		{
			List<Playable> targets = null;
			try
			{
				targets = World.getAroundPlayables(npc, aggroRange, 300);
				for (final var target : targets)
				{
					if (target == null)
					{
						continue;
					}
					
					if (checkAggression(target))
					{
						final var hating = npc.getAggroList().getHating(target);
						if (hating == 0)
						{
							npc.addDamageHate(target, 0, 1);
						}
					}
				}
			}
			finally
			{
				targets = null;
			}
			
			Creature hated;
			if (npc.isConfused())
			{
				hated = getAttackTarget();
			}
			else
			{
				hated = npc.getAggroList().getMostHated();
			}

			if (hated != null)
			{
				final var aggro = npc.getAggroList().getHating(hated);

				if ((aggro + _globalAggro) > 0)
				{
					if (!npc.isRunning())
					{
						npc.setRunning();
					}
					setIntention(CtrlIntention.ATTACK, hated);
				}
				return;
			}

		}
		((DefenderInstance) npc).returnHome();
	}

	protected void thinkAttack()
	{
		final var npc = getActiveChar();
		if (npc.isActionsDisabled())
		{
			return;
		}
		
		if (_attackTimeout < System.currentTimeMillis())
		{
			if (npc.isRunning())
			{
				npc.setWalking();
				_attackTimeout = System.currentTimeMillis() + 30000L;
			}
		}
		
		final var attackTarget = getAttackTarget();
		if ((attackTarget == null) || attackTarget.isAlikeDead() || (_attackTimeout < System.currentTimeMillis()))
		{
			if (attackTarget != null)
			{
				npc.getAggroList().stopHating(attackTarget);
			}
			_attackTimeout = Long.MAX_VALUE;
			setAttackTarget(null);
			setIntention(ACTIVE);
			npc.setWalking();
			return;
		}
		factionNotifyAndSupport();
		attackPrepare();
	}

	private final void factionNotifyAndSupport()
	{
		final var npc = getActiveChar();
		final var target = getAttackTarget();
		if ((npc.getFaction().isNone()) || (target == null))
		{
			return;
		}
		
		if (target.isInvul())
		{
			return;
		}
		
		final var faction_id = npc.getFaction().getName();
		Creature select = null;
		Skill skill = null;
		List<Creature> targets = null;
		try
		{
			targets = World.getAroundCharacters(npc, 1000, 200);
			for (final var cha : targets)
			{
				if (cha == null)
				{
					continue;
				}
				
				if (!(cha instanceof Npc))
				{
					if (_healSkills.length != 0 && (cha instanceof Player player) && (npc.getCastle().getSiege().checkIsDefender(player.getClan())))
					{
						if (!npc.isAttackingDisabled() && (cha.getCurrentHp() < (cha.getMaxHp() * 0.6)) && (npc.getCurrentHp() > (npc.getMaxHp() / 2)) && (npc.getCurrentMp() > (npc.getMaxMp() / 2)) && cha.isInCombat())
						{
							for (final var sk : _healSkills)
							{
								if (npc.getCurrentMp() < sk.getMpConsume())
								{
									continue;
								}
								if (npc.isSkillDisabled(sk))
								{
									continue;
								}
								if (!Util.checkIfInRange(sk.getCastRange(), npc, cha, true))
								{
									continue;
								}
								
								final var chance = 5;
								if (chance >= Rnd.get(100))
								{
									continue;
								}
								if (!GeoEngine.getInstance().canSeeTarget(npc, cha))
								{
									break;
								}
								select = cha;
								skill = sk;
								break;
							}
						}
					}
					if (select != null)
					{
						break;
					}
					else
					{
						continue;
					}
				}
				
				final var n = (Npc) cha;
				if (!faction_id.equals(n.getFaction().getName()))
				{
					continue;
				}
				
				if (n.getAI() != null)
				{
					if (!n.isDead() && (Math.abs(target.getZ() - n.getZ()) < 600) && ((n.getAI().getIntention() == CtrlIntention.IDLE) || (n.getAI().getIntention() == CtrlIntention.ACTIVE)) && target.isInsideRadius(n, 1500, true, false) && GeoEngine.getInstance().canSeeTarget(n, target))
					{
						n.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, target, 1);
						return;
					}
					
					if (_healSkills.length != 0 && !npc.isAttackingDisabled() && (n.getCurrentHp() < (n.getMaxHp() * 0.6)) && (npc.getCurrentHp() > (npc.getMaxHp() / 2)) && (npc.getCurrentMp() > (npc.getMaxMp() / 2)) && n.isInCombat())
					{
						for (final var sk : _healSkills)
						{
							if (npc.getCurrentMp() < sk.getMpConsume())
							{
								continue;
							}
							if (npc.isSkillDisabled(sk))
							{
								continue;
							}
							if (!Util.checkIfInRange(sk.getCastRange(), npc, n, true))
							{
								continue;
							}
							
							final var chance = 4;
							if (chance >= Rnd.get(100))
							{
								continue;
							}
							if (!GeoEngine.getInstance().canSeeTarget(npc, n))
							{
								break;
							}
							select = cha;
							skill = sk;
							break;
						}
					}
				}
			}
		}
		finally
		{
			targets = null;
		}
		
		if (select != null)
		{
			final var OldTarget = npc.getTarget();
			npc.setTarget(select);
			clientStopMoving(null);
			npc.doCast(skill);
			npc.setTarget(OldTarget);
		}
	}
	
	private void attackPrepare()
	{
		var dist2 = 0.;
		var range = 0;
		final var npc = getActiveChar();
		final var sGuard = (DefenderInstance) npc;
		var target = getAttackTarget();
		
		try
		{
			npc.setTarget(target);
			final var combinedCollision = (int) (npc.getColRadius() + target.getColRadius());
			final var dist = Math.sqrt(npc.getPlanDistanceSq(target.getX(), target.getY()));
			dist2 = (int) dist - npc.getColRadius();
			range = _attackRange + combinedCollision;
			if (target.isMoving())
			{
				range += 25;
				if (npc.isMoving())
				{
					range = range + 25;
				}
			}
		}
		catch (final NullPointerException _)
		{
			npc.setTarget(null);
			setIntention(ACTIVE);
			return;
		}
		
		if ((target instanceof Player player) && sGuard.getCastle().getSiege().checkIsDefender(player.getClan()))
		{
			sGuard.getAggroList().stopHating(target);
			npc.setTarget(null);
			setIntention(ACTIVE);
			return;
		}
		
		if (!GeoEngine.getInstance().canSeeTarget(npc, target))
		{
			sGuard.getAggroList().stopHating(target);
			npc.setTarget(null);
			setIntention(ACTIVE);
			return;
		}
		
		if (dist2 > range)
		{
			if (checkSkills(dist2))
			{
				return;
			}
			
			if (!(npc.isAttackingNow()) && (npc.getRunSpeed() == 0))
			{
				npc.setTarget(null);
				setIntention(ACTIVE);
			}
			else
			{
				final var dx = npc.getX() - target.getX();
				final var dy = npc.getY() - target.getY();
				final var dz = npc.getZ() - target.getZ();
				final var homeX = target.getX() - sGuard.getSpawn().getX();
				final var homeY = target.getY() - sGuard.getSpawn().getY();
				
				if ((((dx * dx) + (dy * dy)) > 10000) && (((homeX * homeX) + (homeY * homeY)) > 3240000))
				{
					npc.setTarget(null);
					setIntention(ACTIVE);
				}
				else
				{
					Creature hated = null;
					if (npc.isConfused())
					{
						hated = target;
					}
					else
					{
						hated = npc.getAggroList().getMostHated();
					}
					
					if (hated == null)
					{
						setIntention(ACTIVE);
						return;
					}
					
					if (hated != target)
					{
						target = hated;
					}
					
					if ((dz * dz) < (170 * 170))
					{
						if (target != null)
						{
							moveTo(target.getLocation());
						}
					}
				}
			}
			return;
		}
			
		_attackTimeout = System.currentTimeMillis() + 30000L;
		
		if (Rnd.nextInt(100) <= 5)
		{
			if (checkSkills(dist2))
			{
				return;
			}
		}
		
		if (!npc.getTemplate().getAI().equalsIgnoreCase("Priest"))
		{
			npc.doAttack(target);
		}
	}

	protected boolean checkSkills(double distance)
	{
		final var npc = getActiveChar();
		if (npc.isMuted())
		{
			return false;
		}
		
		final var target = getAttackTarget();
		
		if (target != null && _debuffSkills.length != 0 && Rnd.chance(20))
		{
			final var skill = _debuffSkills[Rnd.get(_debuffSkills.length)];
			final var castRange = skill.getCastRange();
			final var mpConsume = npc.getStat().getMpConsume(skill);
			if ((distance <= castRange) && !npc.isSkillDisabled(skill) && (npc.getCurrentMp() >= mpConsume) && !skill.isPassive())
			{
				if (target.getFirstEffect(skill) == null)
				{
					clientStopMoving(null);
					npc.setTarget(npc.getTarget());
					npc.doCast(skill);
					return true;
				}
			}
		}
		
		if (_damSkills.length != 0)
		{
			final var skill = _damSkills[Rnd.get(_damSkills.length)];
			final var castRange = skill.getCastRange();
			final var mpConsume = npc.getStat().getMpConsume(skill);
			if ((distance <= castRange) && !npc.isSkillDisabled(skill) && (npc.getCurrentMp() >= mpConsume) && !skill.isPassive())
			{
				clientStopMoving(null);
				npc.setTarget(npc.getTarget());
				npc.doCast(skill);
				return true;
			}
		}
		
		if (target != null && _stunSkills.length != 0 && Rnd.chance(20))
		{
			final var skill = _stunSkills[Rnd.get(_stunSkills.length)];
			final var castRange = (int) (target.getColRadius() + npc.getColRadius());
			final var mpConsume = npc.getStat().getMpConsume(skill);
			if ((distance <= castRange) && !npc.isSkillDisabled(skill) && (npc.getCurrentMp() >= mpConsume) && !skill.isPassive())
			{
				clientStopMoving(null);
				npc.setTarget(npc.getTarget());
				npc.doCast(skill);
				return true;
			}
		}

		if (_buffSkills.length != 0)
		{
			final var OldTarget = npc.getTarget();
			for (final var skill : _buffSkills)
			{
				var useSkillSelf = true;
				final var effects = npc.getAllEffects();
				for (int i = 0; (effects != null) && (i < effects.length); i++)
				{
					final var effect = effects[i];
					if (effect.getSkill() == skill)
					{
						useSkillSelf = false;
					}
				}

				if (useSkillSelf)
				{
					npc.setTarget(npc);
					clientStopMoving(null);
					npc.doCast(skill);
					npc.setTarget(OldTarget);
					return true;
				}
			}
		}
		
		if (_healSkills.length != 0)
		{
			if (npc.getCurrentHp() < (npc.getMaxHp() / 2))
			{
				final var skill = _healSkills[Rnd.get(_healSkills.length)];
				final var mpConsume = npc.getStat().getMpConsume(skill);
				if (!npc.isSkillDisabled(skill) && (npc.getCurrentMp() >= mpConsume) && !skill.isPassive())
				{
					final var OldTarget = npc.getTarget();
					
					npc.setTarget(npc);
					clientStopMoving(null);
					npc.doCast(skill);
					npc.setTarget(OldTarget);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	protected void onEvtThink()
	{
		if (_thinking || _actor == null || _actor.isActionsDisabled() || _actor.isAfraid() || _actor.isAllSkillsDisabled())
		{
			return;
		}
		
		_thinking = true;
		try
		{
			switch (getIntention())
			{
				case ACTIVE :
					thinkActive();
					break;
				case ATTACK :
					thinkAttack();
					break;
			}
		}
		finally
		{
			_thinking = false;
		}
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		final var npc = getActiveChar();
		if (attacker == null || npc.isDead())
		{
			return;
		}
		_attackTimeout = System.currentTimeMillis() + 30000L;
		
		if (_globalAggro < 0)
		{
			_globalAggro = 0;
		}
		
		npc.addDamageHate(attacker, 0, 1);
		
		if (!npc.isRunning())
		{
			npc.setRunning();
		}
		
		if (getIntention() != ATTACK)
		{
			setIntention(CtrlIntention.ATTACK, attacker);
		}
		super.onEvtAttacked(attacker, damage);
	}
	
	@Override
	protected void onEvtAggression(Creature target, int aggro)
	{
		final var me = getActiveChar();
		if (me.isDead())
		{
			return;
		}
		
		if (target != null)
		{
			me.addDamageHate(target, 0, aggro);
			
			final var aggr = me.getAggroList().getHating(target);
			
			if (aggr <= 0)
			{
				if (me.getAggroList().getMostHated() == null)
				{
					_globalAggro = -25;
					me.clearAggroList(false);
					setIntention(ACTIVE);
				}
				return;
			}
			
			if (getIntention() != CtrlIntention.ATTACK)
			{
				if (!me.isRunning())
				{
					me.setRunning();
				}
				
				final var sGuard = (DefenderInstance) me;
				final var homeX = target.getX() - sGuard.getSpawn().getX();
				final var homeY = target.getY() - sGuard.getSpawn().getY();
				
				if (((homeX * homeX) + (homeY * homeY)) < 3240000)
				{
					setIntention(CtrlIntention.ATTACK, target);
				}
			}
		}
		else
		{
			if (aggro >= 0)
			{
				return;
			}
			
			final var mostHated = me.getAggroList().getMostHated();
			if (mostHated == null)
			{
				_globalAggro = -25;
				return;
			}
			
			var list = me.getAggroList().getCharMap();
			for (final var aggroed : list.keySet())
			{
				me.addDamageHate(aggroed, 0, aggro);
			}
			list = null;
			final var aggr = me.getAggroList().getHating(mostHated);
			if (aggr <= 0)
			{
				_globalAggro = -25;
				me.clearAggroList(false);
				setIntention(ACTIVE);
			}
		}
	}
	
	@Override
	public void startAITask()
	{
		if (_aiTask == null)
		{
			_aiTask = Config.AI_TASK_MANAGER_COUNT > 0 ? AiTaskManager.getInstance().scheduleAtFixedRate(this, 0L, Config.NPC_AI_TIME_TASK) : ThreadPoolManager.getInstance().scheduleAtFixedRate(this, 0L, Config.NPC_AI_TIME_TASK);
		}
	}

	@Override
	public void stopAITask()
	{
		final var task = _aiTask;
		if (task != null)
		{
			task.cancel(false);
			_aiTask = null;
		}
		super.stopAITask();
	}
	
	public Attackable getActiveChar()
	{
		return (Attackable) _actor;
	}
}