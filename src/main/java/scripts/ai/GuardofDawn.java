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

import gameserver.ai.npc.Fighter;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;
import gameserver.model.actor.instance.MonsterInstance;

/**
 * Created by LordWinter 27.10.2020
 */
public class GuardofDawn extends Fighter
{
	public GuardofDawn(Attackable actor)
	{
		super(actor);

		((MonsterInstance) actor).setCanAgroWhileMoving();
		((MonsterInstance) actor).setSeeThroughSilentMove(true);
		((MonsterInstance) actor).setCanReturnToSpawnPoint(false);
		actor.setIsInvul(true);
	}

	@Override
	protected void thinkAttack()
	{}

	@Override
	protected void onIntentionAttack(Creature target, boolean shift)
	{}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{}

	@Override
	protected void onEvtAggression(Creature attacker, int aggro)
	{}
}