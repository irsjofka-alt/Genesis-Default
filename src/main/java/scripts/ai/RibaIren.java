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
package scripts.ai;

import l2e.commons.util.Rnd;
import gameserver.ai.npc.Fighter;
import gameserver.model.GameObjectsStorage;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;

/**
 * Created by LordWinter 05.12.2024
 */
public class RibaIren extends Fighter
{
	private long _reuseTimer = 0;
	
	public RibaIren(Attackable actor)
	{
		super(actor);
	}
	
	@Override
	protected boolean thinkActive()
	{
		final Attackable actor = getActiveChar();
		if (actor.isDead())
		{
			return true;
		}
		
		if (_healSkills.length > 0 && _reuseTimer < System.currentTimeMillis())
		{
			_reuseTimer = System.currentTimeMillis() + 30000L;
			final var npc = GameObjectsStorage.getByNpcId(29014);
			if (npc != null && npc.getDistance(actor) < 2000 && npc.getCurrentHpPercents() < 80)
			{
				actor.setTarget(npc);
				actor.doCast(_healSkills[0]);
			}
		}
		return super.thinkActive();
	}
	
	@Override
	protected boolean createNewTask()
	{
		final var actor = getActiveChar();
		if (actor == null || actor.isDead())
		{
			return false;
		}
		
		if (_healSkills.length == 0)
		{
			return false;
		}
		
		if (_reuseTimer < System.currentTimeMillis())
		{
			_reuseTimer = System.currentTimeMillis() + 30000L;
			final var npc = GameObjectsStorage.getByNpcId(29014);
			if (npc != null && npc.getDistance(actor) < 2000 && npc.getCurrentHpPercents() < 80)
			{
				actor.setTarget(npc);
				actor.doCast(_healSkills[0]);
			}
		}
		return false;
	}

	@Override
	protected void onEvtClanAttacked(Creature attacker, int damage)
	{
		super.onEvtClanAttacked(attacker, damage);
		final var actor = getActiveChar();
		if (actor == null || actor.isDead())
		{
			return;
		}
		
		final var target = attacker.getTarget();
		if (target == null || !target.isAttackable() || _healSkills.length == 0)
		{
			return;
		}
		
		final var npc = (Attackable) target;
		if (npc.isDead() || npc.getCurrentHpPercents() > 80)
		{
			return;
		}

		final int healChance = npc.getId() == 29014 ? 90 : npc.getId() != 29018 ? 10 : 0;
		if (Rnd.chance(healChance) && canUseSkill(_healSkills[0], npc, -1))
		{
			actor.setTarget(npc);
			actor.doCast(_healSkills[0]);
		}
	}
}