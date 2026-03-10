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

import gameserver.ai.npc.Fighter;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;
import gameserver.model.holders.SkillHolder;
import gameserver.network.NpcStringId;
import gameserver.network.clientpackets.Say2;
import gameserver.network.serverpackets.NpcSay;

public class AncientBox extends Fighter
{
	public AncientBox(Attackable actor)
	{
		super(actor);
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		final Attackable npc = getActiveChar();
		
		if (attacker != null && npc.isScriptValue(0))
		{
			npc.setScriptValue(1);
			npc.broadcastPacketToOthers(1000, new NpcSay(npc, Say2.NPC_ALL, NpcStringId.YOU_WILL_BE_CURSED_FOR_SEEKING_THE_TREASURE));
			npc.setTarget(attacker);
			npc.doCast(new SkillHolder(6033, 1).getSkill());
		}
		super.onEvtAttacked(attacker, damage);
	}
}
