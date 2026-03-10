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

import l2e.commons.util.Rnd;
import gameserver.ai.model.CtrlIntention;
import gameserver.ai.npc.Fighter;
import gameserver.instancemanager.ReflectionManager;
import gameserver.instancemanager.ZoneManager;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Player;
import gameserver.model.zone.ZoneType;
import gameserver.network.NpcStringId;
import gameserver.network.clientpackets.Say2;
import gameserver.network.serverpackets.NpcSay;

public class Glakias extends Fighter
{
	private final ZoneType _zone = ZoneManager.getInstance().getZoneById(90578);
	private final ZoneType _hall = ZoneManager.getInstance().getZoneById(20503);
	
	public Glakias(Attackable actor)
	{
		super(actor);
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		final Attackable actor = getActiveChar();
		
		if (attacker != null && actor.getCurrentHp() < (actor.getMaxHp() * 0.2))
		{
			if (actor.isScriptValue(0))
			{
				NpcStringId stringId = switch (Rnd.get(4))
				{
					case 0  -> NpcStringId.ARCHER_GIVE_YOUR_BREATH_FOR_THE_INTRUDER;
					case 1  -> NpcStringId.MY_KNIGHTS_SHOW_YOUR_LOYALTY;
					case 2  -> NpcStringId.I_CAN_TAKE_IT_NO_LONGER;
					case 3  -> NpcStringId.ARCHER_HEED_MY_CALL;
					default -> null;
				};
				actor.broadcastPacketToOthers(2000, new NpcSay(actor.getObjectId(), Say2.SHOUT, actor.getId(), stringId));
				actor.setScriptValue(1);
			}
		}
		super.onEvtAttacked(attacker, damage);
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
	
	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();
		aggroPlayers(true);
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
}
