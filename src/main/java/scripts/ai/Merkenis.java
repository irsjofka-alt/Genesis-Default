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
import gameserver.ai.npc.Mystic;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;
import gameserver.network.NpcStringId;
import gameserver.network.serverpackets.NpcSay;

/**
 * Created by LordWinter 16.09.2018
 */
public class Merkenis extends Mystic
{
	public Merkenis(Attackable actor)
	{
		super(actor);
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		if (attacker != null && Rnd.chance(10))
		{
			getActiveChar().broadcastPacketToOthers(1000, new NpcSay(getActiveChar().getObjectId(), 0, getActiveChar().getId(), NpcStringId.ILL_CAST_YOU_INTO_AN_ETERNAL_NIGHTMARE));
		}
		super.onEvtAttacked(attacker, damage);
	}
	
	@Override
	protected void onEvtDead(Creature killer)
	{
		getActiveChar().broadcastPacketToOthers(2000, new NpcSay(getActiveChar().getObjectId(), 0, getActiveChar().getId(), NpcStringId.SEND_MY_SOUL_TO_LICH_KING_ICARUS));
		super.onEvtDead(killer);
	}
}
