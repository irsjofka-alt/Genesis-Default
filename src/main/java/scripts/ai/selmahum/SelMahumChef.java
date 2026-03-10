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

import java.util.ArrayList;
import java.util.List;

import l2e.commons.util.Rnd;
import gameserver.ai.npc.Fighter;
import gameserver.geodata.GeoEngine;
import gameserver.model.Location;
import gameserver.model.World;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Npc;
import gameserver.utils.Util;

/**
 * Created by LordWinter 08.12.2018
 */
public class SelMahumChef extends Fighter
{
	private Location _targetLoc;
	private long _waitTime = 0;
	private boolean _isInLoc = true;
	private long _moveInterval = 0L;
	
	public SelMahumChef(Attackable actor)
	{
		super(actor);

		actor.setIsRunner(true);
		actor.setCanReturnToSpawnPoint(false);
		actor.setIsGlobalAI(true);
	}
	
	@Override
	protected boolean thinkActive()
	{
		final Attackable actor = getActiveChar();
		if (actor.isDead() || _moveInterval > System.currentTimeMillis())
		{
			return true;
		}
		
		if (_targetLoc != null && !Util.checkIfInRange(40, _targetLoc.getX(), _targetLoc.getY(), _targetLoc.getZ(), actor, false))
		{
			_moveInterval = System.currentTimeMillis() + 2000L;
			actor.setWalking();
			moveTo(_targetLoc);
		}
		else
		{
			if (_targetLoc != null && !_isInLoc)
			{
				if (Util.checkIfInRange(40, _targetLoc.getX(), _targetLoc.getY(), _targetLoc.getZ(), actor, false))
				{
					_waitTime = System.currentTimeMillis() + 5000;
					_isInLoc = true;
				}
				return true;
			}
			
			if (System.currentTimeMillis() > _waitTime)
			{
				_targetLoc = findFirePlace(actor);
				if (_targetLoc != null)
				{
					_waitTime = System.currentTimeMillis() + 5000;
					_isInLoc = false;
				}
				return true;
			}
		}
		return true;
	}
	
	private Location findFirePlace(Attackable actor)
	{
		Location loc = null;
		List<Npc> list = new ArrayList<>();
		var npcs = World.getAroundNpc(actor, (int) (3000 + actor.getColRadius()), 400);
		for (final Npc npc : npcs)
		{
			if ((npc.getId() == 18927) && GeoEngine.getInstance().canSeeTarget(actor, npc))
			{
				list.add(npc);
			}
		}

		if (!list.isEmpty())
		{
			loc = list.get(Rnd.get(list.size())).getLocation();
		}
		else
		{
			loc = Location.findPointToStay(actor, 1000, 1500, true);
		}
		list = null;
		npcs = null;
		return loc;
	}
}