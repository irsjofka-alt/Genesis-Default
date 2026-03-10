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
package scripts.ai.crypts_of_disgrace;

import l2e.commons.util.Rnd;
import gameserver.ai.model.CtrlEvent;
import gameserver.ai.npc.Fighter;
import gameserver.model.Location;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;
import gameserver.utils.NpcUtils;

/**
 * Created by LordWinter 08.03.2025
 */
public class ContaminatedBaturCommander extends Fighter
{
	public ContaminatedBaturCommander(final Attackable actor)
	{
        super(actor);
    }

    @Override
	protected void onEvtDead(final Creature killer)
	{
		final var actor = getActiveChar();
		final int chance = actor.getTemplate().getParameter("helpersSpawnChance", 0);
		if (Rnd.chance(chance))
        {
			final var npc = NpcUtils.spawnSingle(22707, Location.findPointToStay(actor, 100, 120, true));
			if (npc != null)
			{
				final var player = killer.getActingPlayer();
				if (player != null)
				{
					if (killer.isSummon())
					{
						npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, killer, Rnd.get(2, 100));
					}
					npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, player, Rnd.get(1, 100));
				}
			}
        }
        super.onEvtDead(killer);
    }
}
