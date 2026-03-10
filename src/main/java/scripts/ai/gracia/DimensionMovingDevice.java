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
package scripts.ai.gracia;

import java.util.ArrayList;
import java.util.List;

import gameserver.ai.DefaultAI;
import gameserver.ai.model.CtrlIntention;
import gameserver.model.Location;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Npc;
import gameserver.utils.NpcUtils;

public class DimensionMovingDevice extends DefaultAI
{
	private static final int MOBS_WAVE_DELAY = 5000;
	private boolean _isFirst = false;
	private long _spawnTime = 0;
	private int _count = 0;

	private static final int[] MOBS =
	{
	        22538, 22540, 22547, 22542, 22548
	};

	private final List<Npc> _npcs = new ArrayList<>();

	public DimensionMovingDevice(Attackable actor)
	{
		super(actor);
		
		actor.setIsImmobilized(true);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		_spawnTime = 0;
		_npcs.clear();
		super.onEvtDead(killer);
	}

	@Override
	protected boolean thinkActive()
	{
		final var actor = getActiveChar();
		if (actor.isDead())
		{
			return false;
		}
		
		final var r = actor.getReflection();
		if (!_isFirst)
		{
			_isFirst = true;
			for (final int id : MOBS)
			{
				final var mob = NpcUtils.spawnSingleNpc(id, actor.getLocation(), r, 0);
				((Attackable) mob).setSeeThroughSilentMove(true);
				mob.setRunning();
				_npcs.add(mob);
				if (!r.isDefault() && (r.getStatus() < 7))
				{
					mob.getAI().setIntention(CtrlIntention.MOVING, new Location(-251432, 214905, -12088, 16384), 0);
				}
			}
			_spawnTime = System.currentTimeMillis();
			return true;
		}

		if (_spawnTime + MOBS_WAVE_DELAY < System.currentTimeMillis())
		{
			if(_npcs.size() < 100)
			{
				_count++;
				int id = 0;
				switch (_count)
				{
					case 1 :
						id = MOBS[0];
						break;
					case 2 :
						id = MOBS[1];
						break;
					case 3 :
						id = MOBS[2];
						break;
					case 4 :
						id = MOBS[3];
						break;
					case 5 :
						id = MOBS[4];
						break;
				}
				
				final var mob = NpcUtils.spawnSingleNpc(id, actor.getLocation(), r, 0);
				((Attackable) mob).setSeeThroughSilentMove(true);
				mob.setRunning();
				_npcs.add(mob);
				
				if (!r.isDefault() && (r.getStatus() < 7))
				{
					mob.getAI().setIntention(CtrlIntention.MOVING, new Location(-251432, 214905, -12088, 16384), 0);
				}
			}
			
			if (_count >= 5)
			{
				_count = 0;
			}
			_spawnTime = System.currentTimeMillis();
			return true;
		}
		return true;
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{}

	@Override
	protected void onEvtAggression(Creature target, int aggro)
	{}
}