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
package scripts.ai.hellbound;

import gameserver.ai.model.CtrlEvent;
import gameserver.ai.npc.Fighter;
import gameserver.data.parser.DoorParser;
import gameserver.instancemanager.HellboundManager;
import gameserver.model.World;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Npc;
import gameserver.model.actor.instance.DoorInstance;

/**
 * Created by LordWinter 19.09.2018
 */
public class OutpostCaptain extends Fighter
{
	public OutpostCaptain(Attackable actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		final Attackable actor = getActiveChar();

		if ((attacker == null) || (attacker.getActingPlayer() == null))
		{
			return;
		}

		for (final Npc minion : World.getAroundNpc(actor, 3000, 200))
		{
			if ((minion.getId() == 22358) || (minion.getId() == 22357))
			{
				minion.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 5000);
			}
		}
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		if (HellboundManager.getInstance().getLevel() == 8)
		{
			HellboundManager.getInstance().setLevel(9);
		}
		super.onEvtDead(killer);
	}

	@Override
	protected void onEvtSpawn()
	{
		final Attackable actor = getActiveChar();

		actor.setIsNoRndWalk(true);
		final DoorInstance door = DoorParser.getInstance().getDoor(20250001);
		if (door != null)
		{
			door.closeMe();
		}
		super.onEvtSpawn();
	}
}
