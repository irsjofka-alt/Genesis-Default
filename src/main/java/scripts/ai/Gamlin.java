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
import gameserver.ai.npc.Fighter;
import gameserver.model.World;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Npc;
import gameserver.network.NpcStringId;
import gameserver.network.clientpackets.Say2;
import gameserver.network.serverpackets.NpcSay;

/**
 * Created by LordWinter 22.11.2018
 */
public class Gamlin extends Fighter
{
	public Gamlin(Attackable actor)
	{
		super(actor);
	}
	
	@Override
	public boolean checkAggression(Creature killer)
	{
		if (super.checkAggression(killer))
		{
			if (getActiveChar().isScriptValue(0))
			{
				getActiveChar().setScriptValue(1);
				getActiveChar().broadcastPacketToOthers(1000, new NpcSay(getActiveChar().getObjectId(), Say2.NPC_SHOUT, getActiveChar().getId(), NpcStringId.OH_GIANTS_AN_INTRUDER_HAS_BEEN_DISCOVERED));
				
				for (final Npc npc : World.getAroundNpc(getActiveChar(), 800, 200))
				{
					if (npc.isMonster() && !npc.isDead())
					{
						npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, killer, 5000);
					}
				}
			}
			return true;
		}
		return false;
	}
}