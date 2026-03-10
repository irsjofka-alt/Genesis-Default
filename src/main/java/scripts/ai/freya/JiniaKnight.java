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
package scripts.ai.freya;

import gameserver.ai.model.CtrlEvent;
import gameserver.ai.npc.Fighter;
import gameserver.model.World;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Npc;

public class JiniaKnight extends Fighter
{
	public JiniaKnight(Attackable actor)
	{
		super(actor);
	}

	@Override
	protected boolean thinkActive()
	{
		final Attackable actor = getActiveChar();
		if (actor.isDead())
		{
			return false;
		}

		for (final Npc npc : World.getAroundNpc(getActiveChar(), 5000, 1000))
		{
			if (npc != null && npc.getId() == 22767 && !npc.isDead())
			{
				actor.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, npc, 300);
			}
		}
		return true;
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		if ((attacker == null) || attacker.isPlayable())
		{
			return;
		}
		super.onEvtAttacked(attacker, damage);
	}

	@Override
	protected boolean checkAggression(Creature target)
	{
		if (target.isPlayable())
		{
			return false;
		}
		for (final Npc npc : World.getAroundNpc(getActiveChar(), 5000, 1000))
		{
			if (npc != null && npc.getId() == 22767 && !npc.isDead())
			{
				getActiveChar().getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, npc, 300);
			}
		}
		return super.checkAggression(target);
	}
}
