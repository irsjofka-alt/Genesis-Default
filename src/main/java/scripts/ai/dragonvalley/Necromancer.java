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
package scripts.ai.dragonvalley;

import l2e.commons.util.Rnd;
import gameserver.ai.model.CtrlEvent;
import gameserver.ai.npc.Mystic;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;
import gameserver.utils.NpcUtils;

/**
 * Created by LordWinter 22.11.2018
 */
public class Necromancer extends Mystic
{
	public Necromancer(Attackable actor)
	{
		super(actor);
	}
	
	@Override
	protected void onEvtDead(Creature killer)
	{
		super.onEvtDead(killer);
		if (killer != null)
		{
			final int chance = getActiveChar().getTemplate().getParameter("helpersSpawnChance", 0);
			if (Rnd.chance(chance))
			{
				final var npc = NpcUtils.spawnSingle(Rnd.chance(50) ? 22818 : 22819, getActiveChar().getLocation());
				if (npc != null)
				{
					npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, killer, 2);
				}
			}
		}
	}
}