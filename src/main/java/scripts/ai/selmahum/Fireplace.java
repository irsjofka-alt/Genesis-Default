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
package scripts.ai.selmahum;

import l2e.commons.util.Rnd;
import gameserver.ai.npc.Fighter;
import gameserver.model.World;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Npc;
import gameserver.utils.NpcUtils;

/**
 * Created by LordWinter 08.12.2018
 */
public class Fireplace extends Fighter
{
	private static final long delay = 5 * 60 * 1000L;
	private long _idleTimeout = 0;
	
	public Fireplace(Attackable actor)
	{
		super(actor);
		
		actor.setIsInvul(true);
		actor.setIsRunner(true);
		
		actor.setIsGlobalAI(true);
	}

	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();
		if (Rnd.chance(60))
		{
			getActiveChar().setDisplayEffect(1);
		}
	}
	
	@Override
	protected boolean thinkActive()
	{
		final Attackable actor = getActiveChar();
		if (actor.isDead())
		{
			return true;
		}
		
		if (System.currentTimeMillis() > _idleTimeout)
		{
			_idleTimeout = System.currentTimeMillis() + 5000L;
			for (final Npc npc : World.getAroundNpc(actor, 100, 400))
			{
				if (npc != null && npc.getId() == 18908)
				{
					switchType();
				}
			}
		}
		return true;
	}

	private void switchType()
	{
		_idleTimeout = System.currentTimeMillis() + 120000L;
		final Attackable actor = getActiveChar();
		if (actor.getDisplayEffect() == 1)
		{
			actor.setDisplayEffect(0);
		}
		else
		{
			actor.setDisplayEffect(1);
			if (Rnd.chance(50))
			{
				NpcUtils.spawnSingle(18933, actor.getLocation(), delay / 2);
			}
		}
	}
}