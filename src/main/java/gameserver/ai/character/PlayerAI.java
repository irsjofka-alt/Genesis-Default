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
package gameserver.ai.character;

import static gameserver.ai.model.CtrlIntention.ATTACK;
import static gameserver.ai.model.CtrlIntention.CAST;
import static gameserver.ai.model.CtrlIntention.IDLE;
import static gameserver.ai.model.CtrlIntention.INTERACT;
import static gameserver.ai.model.CtrlIntention.MOVING;
import static gameserver.ai.model.CtrlIntention.PICK_UP;
import static gameserver.ai.model.CtrlIntention.REST;

import gameserver.ai.model.CtrlEvent;
import gameserver.ai.model.CtrlIntention;
import gameserver.ai.model.NextAction;
import gameserver.ai.model.NextAction.NextActionCallback;
import gameserver.geodata.GeoEngine;
import gameserver.model.Location;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Player;
import gameserver.model.actor.instance.StaticObjectInstance;
import gameserver.model.skills.Skill;
import gameserver.model.skills.targets.TargetType;
import gameserver.network.SystemMessageId;

public class PlayerAI extends PlayableAI
{
	private boolean _thinking;
	
	IntentionCommand _nextIntention = null;
	
	public PlayerAI(Player accessor)
	{
		super(accessor);
	}
	
	void saveNextIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		_nextIntention = new IntentionCommand(intention, arg0, arg1);
	}
	
	@Override
	public IntentionCommand getNextIntention()
	{
		return _nextIntention;
	}

	@Override
	protected synchronized void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		if ((intention != CAST) || ((arg0 != null) && !((Skill) arg0).isToggle()))
		{
			_nextIntention = null;
			super.changeIntention(intention, arg0, arg1);
			return;
		}
		
		if ((intention == _intention) && (arg0 == _intentionArg0) && (arg1 == _intentionArg1))
		{
			super.changeIntention(intention, arg0, arg1);
			return;
		}
		saveNextIntention(_intention, _intentionArg0, _intentionArg1);
		super.changeIntention(intention, arg0, arg1);
	}
	
	@Override
	protected void onEvtReadyToAct()
	{
		final var next = _nextIntention;
		if (next != null)
		{
			setIntention(next._crtlIntention, next._arg0, next._arg1);
			_nextIntention = null;
		}
		super.onEvtReadyToAct();
	}
	
	@Override
	protected void onEvtCancel()
	{
		_nextIntention = null;
		super.onEvtCancel();
	}
	
	@Override
	protected void onEvtFinishCasting()
	{
		if (getIntention() == CAST)
		{
			final var nextIntention = _nextIntention;
			if (nextIntention != null)
			{
				if (nextIntention._crtlIntention != CAST)
				{
					setIntention(nextIntention._crtlIntention, nextIntention._arg0, nextIntention._arg1);
				}
				else
				{
					setIntention(IDLE);
				}
			}
			else
			{
				setIntention(IDLE);
			}
		}
	}
	
	@Override
	protected void onIntentionRest()
	{
		if (getIntention() != REST)
		{
			changeIntention(REST, null, null);
			setTarget(null);
			if (getAttackTarget() != null)
			{
				setAttackTarget(null);
			}
			clientStopMoving(null);
		}
	}
	
	@Override
	protected void onIntentionActive()
	{
		setIntention(IDLE);
	}
	
	@Override
	protected void onIntentionMoveTo(Location loc, int offset)
	{
		final var actor = getActor();
		if (actor.isSitting())
		{
			clientActionFailed();
			final var nextAction = new NextAction(CtrlEvent.EVT_STAND_UP, CtrlIntention.MOVING, new NextActionCallback()
			{
				@Override
				public void doWork()
				{
					actor.getAI().setIntention(CtrlIntention.MOVING, loc, offset);
				}
			});
			actor.getAI().setNextAction(nextAction);
			return;
		}
		
		if (actor.isAllSkillsDisabled() || actor.isActionsDisabled())
		{
			clientActionFailed();
			saveNextIntention(MOVING, loc, offset);
			return;
		}
		
		changeIntention(MOVING, loc, offset);
		
		actor.abortAttack();
		
		moveTo(loc, offset);
	}
	
	private void thinkAttack()
	{
		final var actor = getActor();
		final var target = getAttackTarget();
		if (actor == null || target == null)
		{
			return;
		}
		
		if (actor.isActionsDisabled())
		{
			actor.sendActionFailed();
			return;
		}
		
		var range = actor.getPhysicalAttackRange();
		final var canSee = GeoEngine.getInstance().canSeeTarget(actor, target);
		
		final var isBehind = actor.isBehind(target) && target.isMoving();
		if (isBehind || actor.isMovementDisabled())
		{
			range += 50;
		}
		
		if (!canSee && ((range > 200) || (Math.abs(actor.getZ() - target.getZ()) > 200)))
		{
			clientStopMoving(null);
			actor.getAI().setIntention(CtrlIntention.ACTIVE);
			actor.sendActionFailed();
			return;
		}
		
		if (actor.isInRangeZ(actor, target, range))
		{
			if (!canSee)
			{
				actor.sendPacket(SystemMessageId.CANT_SEE_TARGET);
				actor.getAI().setIntention(CtrlIntention.ACTIVE);
				actor.sendActionFailed();
				return;
			}
			
			if (checkTargetLostOrDead(target))
			{
				setAttackTarget(null);
				return;
			}
			if (maybeMoveToPawn(target, range, isBehind))
			{
				return;
			}
			clientStopMoving(null);
			actor.doAttack(target);
		}
		else
		{
			if (checkTargetLostOrDead(target))
			{
				setAttackTarget(null);
				return;
			}
			
			if (maybeMoveToPawn(target, range, isBehind))
			{
				return;
			}
			clientStopMoving(null);
			actor.doAttack(target);
		}
	}
	
	@Override
	protected void onEvtDead(Creature killer)
	{
		_clientMovingToPawnOffset = 0;
		_clientMoving = false;
		super.onEvtDead(killer);
	}
	
	private void thinkCast()
	{
		final var actor = getActor();
		final var target = getCastTarget();
		if (actor == null || target == null)
		{
			return;
		}
		
		var range = actor.getMagicalAttackRange(_skill);
		final var canSee = (_skill.isDisableGeoCheck() || GeoEngine.getInstance().canSeeTarget(actor, target));
		
		final var isBehind = actor.isBehind(target) && target.isMoving();
		if (isBehind)
		{
			range += 50;
		}
		
		if ((_skill.getCastRange() > 0 || _skill.getEffectRange() > 0) && (actor.isInRangeZ(actor, target, range) || !canSee))
		{
			if (!canSee)
			{
				actor.setIsCastingNow(false);
				actor.getAI().setIntention(CtrlIntention.ACTIVE);
				actor.sendActionFailed();
				return;
			}
			
			if (_skill.getTargetType() == TargetType.GROUND)
			{
				if (maybeMoveToPosition(actor.getCurrentSkillWorldPosition(), range))
				{
					actor.setIsCastingNow(false);
					return;
				}
			}
			else
			{
				if (checkTargetLost(target))
				{
					if (_skill.isOffensive() && (getAttackTarget() != null))
					{
						setCastTarget(null);
					}
					actor.setIsCastingNow(false);
					return;
				}
				
				if ((target != null) && maybeMoveToPawn(target, range, isBehind))
				{
					actor.setIsCastingNow(false);
					return;
				}
			}

			final var oldTarget = actor.getTarget();
			if ((oldTarget != null) && (target != null) && (oldTarget != target))
			{
				actor.setFastTarget(target);
				actor.doCast(_skill);
				actor.setTarget(oldTarget);
			}
			else
			{
				actor.doCast(_skill);
			}
		}
		else
		{
			
			if (_skill.getTargetType() == TargetType.GROUND)
			{
				if (maybeMoveToPosition(actor.getCurrentSkillWorldPosition(), range))
				{
					actor.setIsCastingNow(false);
					return;
				}
			}
			else
			{
				if (checkTargetLost(target))
				{
					if (_skill.isOffensive() && (getAttackTarget() != null))
					{
						setCastTarget(null);
					}
					actor.setIsCastingNow(false);
					return;
				}

				if ((target != null) && maybeMoveToPawn(target, range, isBehind))
				{
					actor.setIsCastingNow(false);
					return;
				}
			}

			final var oldTarget = actor.getTarget();
			if ((oldTarget != null) && (target != null) && (oldTarget != target))
			{
				actor.setFastTarget(target);
				actor.doCast(_skill);
				actor.setTarget(oldTarget);
			}
			else
			{
				actor.doCast(_skill);
			}
		}
	}
	
	private void thinkPickUp()
	{
		final var actor = getActor();
		if (actor.isAllSkillsDisabled() || actor.isActionsDisabled())
		{
			return;
		}
		final var target = getTarget();
		if (checkTargetLost(target))
		{
			return;
		}
		if (maybeMoveToPawn(target, 36, false))
		{
			return;
		}
		setIntention(IDLE);
		actor.doPickupItem(target);
	}
	
	private void thinkInteract()
	{
		final var actor = getActor();
		if (actor.isAllSkillsDisabled() || actor.isActionsDisabled() || actor.isSitting())
		{
			return;
		}
		
		final var target = getTarget();
		if (checkTargetLost(target))
		{
			return;
		}
		if (maybeMoveToPawn(target, 36, false))
		{
			return;
		}
		if (!(target instanceof StaticObjectInstance))
		{
			actor.doInteract((Creature) target);
		}
		setIntention(IDLE);
	}
	
	@Override
	protected void onEvtThink()
	{
		if (_thinking && (getIntention() != CAST))
		{
			return;
		}
		
		_thinking = true;
		try
		{
			if (getIntention() == ATTACK)
			{
				thinkAttack();
			}
			else if (getIntention() == CAST)
			{
				thinkCast();
			}
			else if (getIntention() == PICK_UP)
			{
				thinkPickUp();
			}
			else if (getIntention() == INTERACT)
			{
				thinkInteract();
			}
		}
		finally
		{
			_thinking = false;
		}
	}
	
	@Override
	public Player getActor()
	{
		return (Player) super.getActor();
	}
}