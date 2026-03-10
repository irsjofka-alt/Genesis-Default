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

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import gameserver.model.MinionList;
import gameserver.model.World;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.actor.instance.MonsterInstance;
import gameserver.model.actor.templates.npc.MinionData;
import gameserver.model.actor.templates.npc.MinionTemplate;
import gameserver.model.skills.Skill;
import gameserver.network.NpcStringId;
import gameserver.network.clientpackets.Say2;
import gameserver.network.serverpackets.NpcSay;
import scripts.ai.AbstractNpcAI;

public class Ranku extends AbstractNpcAI
{
	private static final int RANKU = 25542;
	private static final int MINION = 32305;
	private static final int MINION_2 = 25543;

	private static final Set<Integer> MY_TRACKING_SET = ConcurrentHashMap.newKeySet();

	private Ranku()
	{
		addAttackId(RANKU);
		addKillId(RANKU, MINION);
	}
	
	@Override
	public final String onAdvEvent(String event, Npc npc, Player player)
	{
		if (event.equalsIgnoreCase("checkup") && (npc.getId() == RANKU) && !npc.isDead())
		{
			final MinionList ml = npc.getMinionList();
			if (ml != null && ml.hasAliveMinions())
			{
				for (final MonsterInstance minion : ml.getAliveMinions())
				{
					if ((minion != null) && !minion.isDead() && MY_TRACKING_SET.contains(minion.getObjectId()))
					{
						final List<Player> players = World.getAroundPlayers(minion);
						final Player killer = players.get(getRandom(players.size()));
						minion.reduceCurrentHp(minion.getMaxHp() / 100, killer, null);
					}
				}
			}
			startQuestTimer("checkup", 1000, npc, null);
		}
		return null;
	}

	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon, Skill skill)
	{
		if (npc.getId() == RANKU)
		{
			final MinionList ml = npc.getMinionList();
			if (ml != null && ml.hasAliveMinions())
			{
				for (final MonsterInstance minion : ml.getAliveMinions())
				{
					if ((minion != null) && !minion.isDead() && !MY_TRACKING_SET.contains(minion.getObjectId()))
					{
						minion.broadcastPacketToOthers(2000, new NpcSay(minion.getObjectId(), Say2.NPC_ALL, minion.getId(), NpcStringId.DONT_KILL_ME_PLEASE_SOMETHINGS_STRANGLING_ME));
						startQuestTimer("checkup", 1000, npc, null);
						MY_TRACKING_SET.add(minion.getObjectId());
					}
				}
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon, skill);
	}

	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		if (npc.getId() == MINION)
		{
			if (MY_TRACKING_SET.contains(npc.getObjectId()))
			{
				MY_TRACKING_SET.remove(npc.getObjectId());
			}

			final Npc master = npc.getLeader();
			if ((master != null) && !master.isDead())
			{
				master.getMinionList().addMinion(new MinionData(new MinionTemplate(MINION_2, 1)), true);
			}
		}
		else if (npc.getId() == RANKU)
		{
			final MinionList ml = npc.getMinionList();
			if (ml != null)
			{
				if (ml.hasAliveMinions())
				{
					for (final MonsterInstance minion : ml.getAliveMinions())
					{
						if (minion != null && MY_TRACKING_SET.contains(minion.getObjectId()))
						{
							MY_TRACKING_SET.remove(minion.getObjectId());
						}
					}
				}
				ml.onMasterDelete();
			}
		}
		return super.onKill(npc, killer, isSummon);
	}

	public static void main(String[] args)
	{
		new Ranku();
	}
}