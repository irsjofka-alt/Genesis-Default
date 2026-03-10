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

import gameserver.ai.model.CtrlEvent;
import gameserver.ai.npc.Mystic;
import gameserver.model.Location;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;
import gameserver.utils.NpcUtils;
import gameserver.utils.PositionUtils;

/**
 * Created by LordWinter 22.11.2018
 */
public class LizardmanSummoner extends Mystic
{
	public LizardmanSummoner(Attackable actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		final Attackable actor = getActiveChar();
		if (attacker != null && actor.isScriptValue(0) && attacker.isPlayable())
		{
			actor.setScriptValue(1);
			for (int i = 0; i < 2; i++)
			{
				final var npc = NpcUtils.spawnSingle(22768, Location.findPointToStay(actor, 100, 120, true));
				if (npc != null)
				{
					npc.setHeading(PositionUtils.calculateHeadingFrom(npc, attacker));
					npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 1000);
				}
			}
		}
		super.onEvtAttacked(attacker, damage);
	}
}
