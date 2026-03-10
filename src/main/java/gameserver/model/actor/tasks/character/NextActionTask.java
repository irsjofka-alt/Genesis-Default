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
package gameserver.model.actor.tasks.character;

import gameserver.ai.model.CtrlIntention;
import gameserver.model.GameObject;
import gameserver.model.actor.Creature;

public final class NextActionTask implements Runnable
{
	private final Creature _creature;
	private final GameObject _target;
	
	public NextActionTask(Creature creature, GameObject target)
	{
		_creature = creature;
		_target = target;
	}

	@Override
	public void run()
	{
		if (_creature != null)
		{
			final var target = _creature.getTarget();
			if (_target == null || target == null || _creature.isCastingNow())
			{
				return;
			}
			
			if ((target.isCreature()) && (target != _creature) && (target == _target) && _target.isAutoAttackable(_creature, false))
			{
				_creature.getAI().setIntention(CtrlIntention.ATTACK, _target);
			}
		}
	}
}