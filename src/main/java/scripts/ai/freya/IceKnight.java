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
package scripts.ai.freya;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import l2e.commons.util.Rnd;
import gameserver.ThreadPoolManager;
import gameserver.ai.model.CtrlIntention;
import gameserver.ai.npc.Fighter;
import gameserver.instancemanager.ReflectionManager;
import gameserver.instancemanager.ZoneManager;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Player;
import gameserver.model.zone.ZoneType;

public class IceKnight extends Fighter
{
	private boolean _iced;
	private final ZoneType _zone = ZoneManager.getInstance().getZoneById(90578);
	private final ZoneType _hall = ZoneManager.getInstance().getZoneById(20503);
	private ScheduledFuture<?> _task;
	
	public IceKnight(Attackable actor)
	{
		super(actor);
	}
	
	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();
		final Attackable actor = getActiveChar();
		_iced = true;
		actor.setDisplayEffect(1);
		actor.block();
		actor.setIsImmobilized(true);
		actor.disableCoreAI(true);
		_task = ThreadPoolManager.getInstance().schedule(new ReleaseFromIce(), 6000L);
	}
	
	@Override
	protected boolean thinkActive()
	{
		if (Rnd.chance(10))
		{
			aggroPlayers(true);
		}
		return super.thinkActive();
	}
	
	private void aggroPlayers(boolean searchTarget)
	{
		final Attackable actor = getActiveChar();
		if (actor.isDead())
		{
			return;
		}
		
		final var r = actor.getReflection();
		if (!r.isDefault())
		{
			final List<Player> activeList = new ArrayList<>();
			for (final var activeChar : r.getReflectionPlayers())
			{
				if (_hall != null && _hall.isInsideZone(activeChar) || activeChar.isDead())
				{
					continue;
				}
				
				actor.addDamageHate(activeChar, 0, Rnd.get(100, 300));
				if (searchTarget)
				{
					activeList.add(activeChar);
				}
			}
			
			if (!activeList.isEmpty())
			{
				final var attacked = activeList.get(Rnd.get(activeList.size()));
				if (attacked != null)
				{
					actor.setTarget(attacked);
					actor.getAI().setIntention(CtrlIntention.ATTACK, attacked);
				}
			}
		}
		else
		{
			final List<Player> activeList = new ArrayList<>();
			final var players = _zone.getPlayersInside(ReflectionManager.DEFAULT);
			if (!players.isEmpty())
			{
				for (final var activeChar : players)
				{
					if (activeChar != null && !activeChar.isDead())
					{
						actor.addDamageHate(activeChar, 0, Rnd.get(100, 300));
						if (searchTarget)
						{
							activeList.add(activeChar);
						}
					}
				}
			}
			
			if (!activeList.isEmpty())
			{
				final var attacked = activeList.get(Rnd.get(activeList.size()));
				if (attacked != null)
				{
					actor.setTarget(attacked);
					actor.getAI().setIntention(CtrlIntention.ATTACK, attacked);
				}
			}
		}
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		final var actor = getActiveChar();
		if (attacker != null && _iced)
		{
			_iced = false;
			aggroPlayers(false);
			final var task = _task;
			if (task != null)
			{
				task.cancel(false);
				_task = null;
			}
			actor.disableCoreAI(false);
			actor.setIsImmobilized(false);
			actor.unblock();
			actor.setDisplayEffect(2);
		}
		super.onEvtAttacked(attacker, damage);
	}
	
	private class ReleaseFromIce implements Runnable
	{
		@Override
		public void run()
		{
			if (_iced)
			{
				_iced = false;
				getActiveChar().disableCoreAI(false);
				getActiveChar().setIsImmobilized(false);
				getActiveChar().setDisplayEffect(2);
				getActiveChar().unblock();
				aggroPlayers(true);
			}
		}
	}
}
