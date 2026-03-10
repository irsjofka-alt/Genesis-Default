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

import gameserver.ai.model.CtrlEvent;
import gameserver.ai.npc.Mystic;
import gameserver.model.Location;
import gameserver.model.World;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Npc;
import gameserver.utils.Util;

/**
 * Created by LordWinter 27.01.2020
 */
public class TrapMystic extends Mystic
{
	private static final Location[] points1 =
	{
	        new Location(-251368, 217864, -12332), new Location(-251400, 216216, -12253), new Location(-251432, 214536, -21087), new Location(-251448, 211672, -11986), new Location(-250408, 211656, -11859), new Location(-250408, 210008, -11956), new Location(-250408, 208632, -11956), new Location(-250408, 207480, -11952)
	};
	
	private static final Location[] points2 =
	{
	        new Location(-250408, 210008, -11956), new Location(-250408, 208632, -11956), new Location(-250408, 207480, -11952)
	};
	private Location[] _points = null;
	private int current_point = -1;
	private long _moveInterval = 0L;
	
	public TrapMystic(Attackable actor)
	{
		super(actor);
		
		MAX_PURSUE_RANGE = Integer.MAX_VALUE - 10;
		actor.setIsGlobalAI(true);
	}
	
	@Override
	protected void onEvtSpawn()
	{
		final var r = getActiveChar().getReflection();
		if (!r.isDefault())
		{
			if (r.getStatus() > 8)
			{
				aggroPlayers();
			}
		}
		else
		{
			getActiveChar().setCanReturnToSpawnPoint(false);
			final int stage = SoDDefenceStage.getDefenceStage();
			if (stage != 0)
			{
				if (stage < 3)
				{
					_points = points1;
				}
				else
				{
					_points = points2;
				}
			}
		}
		super.onEvtSpawn();
	}
	
	@Override
	protected boolean thinkActive()
	{
		final Attackable actor = getActiveChar();
		if (actor.isDead())
		{
			return true;
		}
		
		final var r = actor.getReflection();
		if (!r.isDefault())
		{
			if (r.getStatus() > 8)
			{
				aggroPlayers();
				return true;
			}
		}
		else
		{
			final int stage = SoDDefenceStage.getDefenceStage();
			if (stage == 0)
			{
				getActiveChar().deleteMe();
				return true;
			}
			
			if (_moveInterval > System.currentTimeMillis())
			{
				return super.thinkActive();
			}
			
			if (aggroToController())
			{
				return true;
			}
			
			if (current_point < 0 || current_point >= _points.length)
			{
				startMoveTask();
			}
			else
			{
				final Location loc = _points[current_point];
				if (Util.checkIfInRange(60, loc.getX(), loc.getY(), loc.getZ(), actor, false))
				{
					startMoveTask();
				}
				else
				{
					_moveInterval = System.currentTimeMillis() + 1000L;
					moveTo(Location.findPointToStay(loc, 40, true));
					return true;
				}
			}
		}
		return super.thinkActive();
	}
	
	private void startMoveTask()
	{
		final Attackable actor = getActiveChar();
		if (_points == null)
		{
			return;
		}
		
		current_point++;
		
		if (current_point >= _points.length)
		{
			if (aggroToController())
			{
				return;
			}
		}
		
		Location loc = _points[current_point];
		if (loc == null)
		{
			current_point = _points.length - 1;
			loc = _points[current_point];
		}
		_moveInterval = System.currentTimeMillis() + 1000L;
		if (!actor.isRunning())
		{
			actor.setRunning();
		}
		moveTo(Location.findPointToStay(actor, loc, 0, 80, true));
	}
	
	private boolean aggroToController()
	{
		for (final Npc npc : World.getAroundNpc(getActiveChar(), 4000, 400))
		{
			if (npc.getId() == 18775 && !npc.isDead())
			{
				getActiveChar().setTarget(npc);
				setAttackTarget(npc);
				getActiveChar().getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, npc, 100000);
				return true;
			}
		}
		return false;
	}

	private void aggroPlayers()
	{
		final var r = getActiveChar().getReflection();
		if (!r.isDefault())
		{
			boolean found = false;
			for (final var activeChar : r.getReflectionPlayers())
			{
				getActiveChar().getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, 1000);
				found = true;
			}
			
			if (!found)
			{
				moveTo(new Location(-250403, 207273, -11952, 16384));
			}
		}
	}
	
	@Override
	public boolean checkAggression(Creature target)
	{
		final Attackable actor = getActiveChar();
		if (actor.isDead())
		{
			return true;
		}
		
		if (getActiveChar().getReflectionId() == 0)
		{
			if (target != null && target.isPlayer())
			{
				return false;
			}
		}
		return super.checkAggression(target);
	}
	
	@Override
	protected void returnHome(boolean clearAggro, boolean teleport)
	{
	}
	
	@Override
	protected void teleportHome()
	{
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		if (getActiveChar().getReflectionId() == 0)
		{
			if (attacker != null && attacker.isPlayer())
			{
				return;
			}
		}
		super.onEvtAttacked(attacker, damage);
	}
}