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

import gameserver.ai.npc.Mystic;
import gameserver.model.Location;
import gameserver.model.actor.Attackable;
import gameserver.network.NpcStringId;
import gameserver.network.serverpackets.NpcSay;
import gameserver.utils.NpcUtils;

/**
 * Created by LordWinter 10.12.2018
 */
public class BladeOtis extends Mystic
{
	private long _spawnTimer = 0L;
	private int _spawnCounter = 0;
	private final static long _spawnInterval = 60000L;
	private final static int _spawnLimit = 10;

	public BladeOtis(Attackable actor)
	{
		super(actor);
	}

	@Override
	protected void thinkAttack()
	{
		final Attackable actor = getActiveChar();

		if (_spawnTimer == 0)
		{
			_spawnTimer = System.currentTimeMillis();
		}

		if ((actor.getCurrentHpPercents() < 60) && (_spawnCounter < _spawnLimit) && ((_spawnTimer + _spawnInterval) < System.currentTimeMillis()))
		{
			NpcUtils.spawnSingle(18563, Location.findPointToStay(actor.getLocation(), 200, true), actor.getReflection(), 0);
			_spawnTimer = System.currentTimeMillis();
			_spawnCounter++;
			actor.broadcastPacketToOthers(2000, new NpcSay(actor.getObjectId(), 0, actor.getId(), NpcStringId.IF_YOU_THOUGHT_THAT_MY_SUBORDINATES_WOULD_BE_SO_FEW_YOU_ARE_MISTAKEN));
		}
		super.thinkAttack();
	}
}