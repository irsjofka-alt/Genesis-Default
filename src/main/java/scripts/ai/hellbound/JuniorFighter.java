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
package scripts.ai.hellbound;

import gameserver.Config;
import gameserver.ai.model.CtrlIntention;
import gameserver.ai.npc.Fighter;
import gameserver.instancemanager.HellboundManager;
import gameserver.model.Location;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;
import gameserver.model.actor.instance.MonsterInstance;
import gameserver.network.NpcStringId;
import gameserver.network.clientpackets.Say2;
import gameserver.network.serverpackets.NpcSay;
import gameserver.taskmanager.DecayTaskManager;

public class JuniorFighter extends Fighter
{
	public JuniorFighter(Attackable actor)
	{
		super(actor);
		
		((MonsterInstance) actor).applyCondition(creature -> HellboundManager.getInstance().getLevel() < 5);
	}
	
	@Override
	protected void onEvtSpawn()
	{
		final var npc = getActiveChar();
		npc.setOnKillDelay(1000);
		super.onEvtSpawn();
	}
	
	@Override
	protected void onEvtDead(Creature killer)
	{
		final var npc = getActiveChar();
		final var ml = npc.getMinionList();
		if (ml != null)
		{
			if (ml.hasAliveMinions())
			{
				for (final var slave : ml.getAliveMinions())
				{
					if ((slave != null) && !slave.isDead())
					{
						slave.clearAggroList(false);
						slave.abortAttack();
						slave.abortCast();
						slave.broadcastPacketToOthers(2000, new NpcSay(slave.getObjectId(), Say2.NPC_ALL, slave.getId(), NpcStringId.THANK_YOU_FOR_SAVING_ME_FROM_THE_CLUTCHES_OF_EVIL));
						
						if ((HellboundManager.getInstance().getLevel() >= 1) && (HellboundManager.getInstance().getLevel() <= 2))
						{
							HellboundManager.getInstance().updateTrust(10, false);
						}
						
						final var ai = slave.getAI();
						if (ai != null)
						{
							slave.setRunning();
							slave.getAI().setIntention(CtrlIntention.MOVING, new Location(-25451, 252291, -3252, 3500), 0);
							DecayTaskManager.getInstance().addDecayTask(slave, (Config.NPC_DECAY_TIME * 1000L), true);
						}
					}
				}
			}
			ml.clearMinionList();
		}
		super.onEvtDead(killer);
	}
}
