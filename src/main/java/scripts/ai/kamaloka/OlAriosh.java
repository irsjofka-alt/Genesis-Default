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
package scripts.ai.kamaloka;

import gameserver.ai.model.CtrlEvent;
import gameserver.ai.npc.Fighter;
import gameserver.model.Location;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Npc;
import gameserver.network.NpcStringId;
import gameserver.network.serverpackets.NpcSay;
import gameserver.utils.NpcUtils;

/**
 * Created by LordWinter 10.12.2018
 */
public class OlAriosh extends Fighter
{
	private long _spawnTimer = 0L;
	private final static long _spawnInterval = 60000L;
	private Npc _follower = null;

	public OlAriosh(Attackable actor)
	{
		super(actor);
	}
	
	@Override
	protected void thinkAttack()
	{
		final Attackable actor = getActiveChar();

		if (((_follower == null) || _follower.isDead()) && ((_spawnTimer + _spawnInterval) < System.currentTimeMillis()))
		{
			_follower = NpcUtils.spawnSingle(18556, Location.findPointToStay(actor.getLocation(), 200, true), actor.getReflection(), 0);
			final var target = getAttackTarget();
			if (target != null)
			{
				_follower.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, target, 1000);
			}
			_spawnTimer = System.currentTimeMillis();
			actor.broadcastPacketToOthers(2000, new NpcSay(actor.getObjectId(), 0, actor.getId(), NpcStringId.WHAT_ARE_YOU_DOING_HURRY_UP_AND_HELP_ME));
		}
		super.thinkAttack();
	}
}