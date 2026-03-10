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
package scripts.ai.gracia;

import l2e.commons.util.Rnd;
import gameserver.ai.DefaultAI;
import gameserver.ai.model.CtrlEvent;
import gameserver.data.parser.SkillsParser;
import gameserver.model.Location;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;
import gameserver.utils.NpcUtils;

/**
 * Created by LordWinter 27.01.2020
 */
public class WardofDeath extends DefaultAI
{
	private static final int[] mobs =
	{
	        22516, 22520, 22522, 22524
	};

	public WardofDeath(Attackable actor)
	{
		super(actor);
		
		actor.setIsImmobilized(true);
	}

	@Override
	protected boolean checkAggression(Creature target)
	{
		final Attackable actor = getActiveChar();
		if (actor == null)
		{
			return false;
		}
		
		if (super.checkAggression(target))
		{
			if (actor.getId() == 18667)
			{
				actor.doCast(SkillsParser.getInstance().getInfo(Rnd.get(5423, 5424), 9));
				actor.doDie(null);
			}
			else if (actor.getId() == 18668)
			{
				for(int i = 0; i < Rnd.get(1, 4); i++)
				{
					final var n = NpcUtils.spawnSingle(mobs[Rnd.get(mobs.length)], Location.findAroundPosition(actor, 60, 100), actor.getReflection(), 0);
					if (target != null)
					{
						n.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, target, 100);
					}
				}
				actor.doDie(null);
			}
			return true;
		}
		return false;
	}
}