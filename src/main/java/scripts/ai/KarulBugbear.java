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
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;
import gameserver.network.NpcStringId;
import gameserver.network.serverpackets.NpcSay;

/**
 * Created by LordWinter 16.11.2018
 */
public class KarulBugbear extends Fighter
{
	public KarulBugbear(Attackable actor)
	{
		super(actor);
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		final Attackable actor = getActiveChar();
		if (attacker != null)
		{
			if (actor.isScriptValue(0))
			{
				actor.setScriptValue(1);
				if (Rnd.chance(25))
				{
					actor.broadcastPacketToOthers(1000, new NpcSay(actor.getObjectId(), 0, actor.getId(), NpcStringId.YOUR_REAR_IS_PRACTICALLY_UNGUARDED));
				}
			}
			else if (Rnd.chance(10))
			{
				actor.broadcastPacketToOthers(1000, new NpcSay(actor.getObjectId(), 0, actor.getId(), NpcStringId.S1_WATCH_YOUR_BACK));
			}
		}
		super.onEvtAttacked(attacker, damage);
	}
}
