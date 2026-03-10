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
package scripts.ai.kamaloka;

import gameserver.ai.model.CtrlEvent;
import gameserver.ai.npc.Fighter;
import gameserver.model.Location;
import gameserver.model.actor.Attackable;
import gameserver.utils.NpcUtils;

/**
 * Created by LordWinter 10.12.2018
 */
public class Bilette extends Fighter
{
	private long _spawnTimer = 0L;
	private int _spawnCounter = 0;
	private final static long _spawnInterval = 60000L;
	private final static int _spawnLimit = 15;
	
	public Bilette(Attackable actor)
	{
		super(actor);
	}
	
	@Override
	protected void thinkAttack()
	{
		final Attackable actor = getActiveChar();

		if (_spawnTimer == 0)
		{
			_spawnTimer = System.currentTimeMillis();
		}

		if ((_spawnCounter < _spawnLimit) && ((_spawnTimer + _spawnInterval) < System.currentTimeMillis()))
		{
			final var follower = NpcUtils.spawnSingle(18574, Location.findPointToStay(actor.getLocation(), 200, true), actor.getReflection(), 0);
			final var target = getAttackTarget();
			if (target != null)
			{
				follower.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, target, 1000);
			}
			_spawnTimer = System.currentTimeMillis();
			_spawnCounter++;
		}
		super.thinkAttack();
	}
}