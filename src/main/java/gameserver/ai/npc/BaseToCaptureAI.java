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
package gameserver.ai.npc;

import gameserver.ai.character.CharacterAI;
import gameserver.model.GameObject;
import gameserver.model.Location;
import gameserver.model.actor.Creature;
import gameserver.model.actor.instance.BaseToCaptureInstance;
import gameserver.model.skills.Skill;

public class BaseToCaptureAI extends CharacterAI
{
	public BaseToCaptureAI(BaseToCaptureInstance character)
	{
		super(character);
	}
	
	private BaseToCaptureInstance getActiveChar()
	{
		return (BaseToCaptureInstance) _actor;
	}
	
	@Override
	protected void onEvtSeeSpell(Skill skill, Creature caster)
	{
		final var actor = getActiveChar();
		if (actor.isAutoAttackable(caster, false) && actor.isEventSkill(skill))
		{
			actor.finishEvent(caster);
		}
	}
	
	@Override
	protected void onIntentionRest()
	{
	}

	@Override
	protected void onIntentionAttack(Creature target, boolean shift)
	{
	}

	@Override
	protected void onIntentionCast(Skill skill, GameObject target)
	{
	}

	@Override
	protected void onIntentionMoveTo(Location destination, int offset)
	{
	}

	@Override
	protected void onIntentionFollow(Creature target)
	{
	}

	@Override
	protected void onIntentionPickUp(GameObject item)
	{
	}

	@Override
	protected void onIntentionInteract(GameObject object)
	{
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
	}

	@Override
	protected void onEvtAggression(Creature target, int aggro)
	{
	}

	@Override
	protected void onEvtStunned(Creature attacker)
	{
	}

	@Override
	protected void onEvtSleeping(Creature attacker)
	{
	}

	@Override
	protected void onEvtRooted(Creature attacker)
	{
	}

	@Override
	protected void onEvtReadyToAct()
	{
	}

	@Override
	protected void onEvtUserCmd(Object arg0, Object arg1)
	{
	}

	@Override
	protected void onEvtArrived()
	{
	}

	@Override
	protected void onEvtArrivedTarget()
	{
	}

	@Override
	protected void onEvtArrivedBlocked(Location blocked_at_loc)
	{
	}

	@Override
	protected void onEvtForgetObject(GameObject object)
	{
	}

	@Override
	protected void onEvtCancel()
	{
	}
}