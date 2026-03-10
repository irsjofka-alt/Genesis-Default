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
 * Created by LordWinter 15.11.2018
 */
public class BrekaOrcOverlord extends Fighter
{
	private static final NpcStringId[] SHOUT =
	{
	        NpcStringId.S1_SHOW_YOUR_STRENGTH, NpcStringId.IM_THE_STRONGEST_I_LOST_EVERYTHING_TO_WIN, NpcStringId.USING_A_SPECIAL_SKILL_HERE_COULD_TRIGGER_A_BLOODBATH
	};
	
	public BrekaOrcOverlord(Attackable actor)
	{
		super(actor);
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		final Attackable actor = getActiveChar();
		
		if (attacker != null && actor.isScriptValue(0) && Rnd.chance(25))
		{
			actor.setScriptValue(1);
			actor.broadcastPacketToOthers(1000, new NpcSay(actor.getObjectId(), 0, actor.getId(), SHOUT[Rnd.get(3)]));
		}
		super.onEvtAttacked(attacker, damage);
	}
}
