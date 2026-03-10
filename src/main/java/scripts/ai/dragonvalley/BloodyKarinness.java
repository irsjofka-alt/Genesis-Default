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
import gameserver.ai.npc.Fighter;
import gameserver.model.Location;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;
import gameserver.utils.NpcUtils;

/**
 * Created by LordWinter 15.12.2024
 */
public class BloodyKarinness extends Fighter
{
	public BloodyKarinness(Attackable actor)
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
			if (Rnd.chance(chance) && getActiveChar().isScriptValue(0))
			{
				final String[] amount = getActiveChar().getTemplate().getParameter("helpersRndAmount", "4;4").split(";");
				final int rnd = Rnd.get(Integer.parseInt(amount[0]), Integer.parseInt(amount[1]));
				for (int x = 0; x < rnd; x++)
				{
					final var npc = NpcUtils.spawnSingle(22856, Location.findAroundPosition(getActiveChar(), 60, 100));
					npc.setScriptValue(1);
					npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, killer, 2);
				}
            		}
        	}
    	}
}
