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
import gameserver.geodata.GeoEngine;
import gameserver.model.Location;
import gameserver.model.World;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Npc;
import gameserver.network.NpcStringId;
import gameserver.network.clientpackets.Say2;
import gameserver.network.serverpackets.CreatureSay;
import gameserver.network.serverpackets.NpcInfo;
import gameserver.network.serverpackets.updatetype.NpcInfoType;

/**
 * Created by LordWinter 08.12.2018
 */
public class SelMahumLeader extends Fighter
{
	private boolean _isBusy;
	private boolean _isImmobilized;
	
	private long _busyTimeout = 0;
	private long _idleTimeout = 0;
	private long _waitTime = 0;
	
	private static final NpcStringId[] _message =
	{
	        NpcStringId.COME_AND_EAT, NpcStringId.LOOKS_DELICIOUS, NpcStringId.LETS_GO_EAT
	};
	
	private static int NPC_ID_FIRE = 18927;
	private static int NPC_ID_FIRE_FEED = 18933;
	
	public SelMahumLeader(Attackable actor)
	{
		super(actor);
		
		actor.setIsGlobalAI(true);
	}

	@Override
	protected boolean thinkActive()
	{
		final Attackable actor = getActiveChar();
		if (actor.isDead())
		{
			return true;
		}
		
		if (!_isBusy)
		{
			final var now = System.currentTimeMillis();
			if (now > _idleTimeout && now > _waitTime)
			{
				_waitTime = now + 2000L;
				var npcs = World.getAroundNpc(actor, (int) (600 + actor.getColRadius()), 400);
				for (final Npc npc : npcs)
				{
					if ((npc.getId() == NPC_ID_FIRE_FEED) && GeoEngine.getInstance().canSeeTarget(actor, npc))
					{
						_isBusy = true;
						actor.setRunning();
						actor.setDisplayEffect(1);
						_busyTimeout = now + ((60 + Rnd.get(15)) * 1000L);
						moveTo(Location.findPointToStay(npc, 50, 150, true));
						if (Rnd.chance(2))
						{
							actor.broadcastPacketToOthers(800, new CreatureSay(actor.getObjectId(), Say2.ALL, actor.getName(null), _message[Rnd.get(2)]));
						}
						break;
					}
					else if ((npc.getId() == NPC_ID_FIRE) && (npc.getDisplayEffect() == 1) && GeoEngine.getInstance().canSeeTarget(actor, npc))
					{
						_isBusy = true;
						actor.setDisplayEffect(2);
						_busyTimeout = now + ((60 + Rnd.get(60)) * 1000L);
						moveTo(Location.findPointToStay(npc, 50, 150, true));
						break;
					}
				}
				npcs = null;
			}
		}
		else
		{
			if (System.currentTimeMillis() > _busyTimeout)
			{
				wakeUp();
				actor.setWalking();
				moveTo(actor.getSpawn().getLocation());
				return true;
			}
		}
		
		if (_isImmobilized)
		{
			return true;
		}
		return super.thinkActive();
	}
	
	private void wakeUp()
	{
		final Attackable actor = getActiveChar();
		if (_isBusy)
		{
			_isBusy = false;
			
			_busyTimeout = 0;
			_idleTimeout = System.currentTimeMillis() + (Rnd.get(180, 1200) * 1000L);
			
			if (_isImmobilized)
			{
				_isImmobilized = false;
				actor.setIsImmobilized(false);
				actor.setDisplayEffect(3);
				actor.setRHandId(0);
				var players = World.getAroundPlayers(actor);
				for (final var player : players)
				{
					if (player != null && player.isOnline())
					{
						final var packet = new NpcInfo.Info(actor, player);
						packet.addComponentType(NpcInfoType.EQUIPPED);
						player.sendPacket(packet);
					}
				}
				players = null;
			}
		}
	}
	
	@Override
	protected void onEvtArrived()
	{
		final Attackable actor = getActiveChar();

		super.onEvtArrived();
		
		if (_isBusy)
		{
			_isImmobilized = true;
			actor.setIsImmobilized(true);
			actor.setRHandId(15280);
			actor.broadcastInfo();
		}
	}
	
	@Override
	protected void onIntentionActive()
	{
		_idleTimeout = System.currentTimeMillis() + (Rnd.get(120, 900) * 1000L);
		super.onIntentionActive();
	}
	
	@Override
	protected void onIntentionAttack(Creature target, boolean shift)
	{
		wakeUp();
		super.onIntentionAttack(target, shift);
	}
}