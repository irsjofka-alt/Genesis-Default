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
import gameserver.network.NpcStringId;
import gameserver.network.clientpackets.Say2;
import gameserver.network.serverpackets.NpcSay;

/**
 * Created by LordWinter 22.11.2018
 */
public class MoSMonk extends Fighter
{
	public MoSMonk(Attackable actor)
	{
		super(actor);
	}
	
	@Override
	protected boolean checkAggression(Creature target)
	{
		if (target.getActiveWeaponInstance() == null)
		{
			return false;
		}
		
		if (super.checkAggression(target))
		{
			if (getActiveChar().isScriptValue(0))
			{
				getActiveChar().setScriptValue(1);
				getActiveChar().broadcastPacketToOthers(1000, new NpcSay(getActiveChar().getObjectId(), Say2.NPC_ALL, getActiveChar().getId(), NpcStringId.YOU_CANNOT_CARRY_A_WEAPON_WITHOUT_AUTHORIZATION));
			}
			return true;
		}
		return false;
	}
}