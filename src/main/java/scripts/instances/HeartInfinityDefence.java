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
package scripts.instances;

import java.util.ArrayList;
import java.util.List;

import gameserver.ThreadPoolManager;
import gameserver.ai.model.CtrlIntention;
import gameserver.instancemanager.ZoneManager;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.actor.instance.MonsterInstance;
import gameserver.model.actor.templates.reflection.ReflectionTemplate;
import gameserver.model.entity.Reflection;
import gameserver.model.zone.ZoneType;
import gameserver.network.NpcStringId;
import gameserver.network.clientpackets.Say2;
import gameserver.network.serverpackets.ExShowScreenMessage;
import gameserver.network.serverpackets.NpcSay;

/**
 * Rework by LordWinter 30.09.2020
 */
public class HeartInfinityDefence extends AbstractReflection
{
	public HeartInfinityDefence()
	{
		super(122);

		addStartNpc(32535, 32536, 32539);
		addTalkId(32535, 32536, 32539);
		addEnterZoneId(200010);
		addKillId(18708, 18711);
	}
	
	private final synchronized void enterInstance(Player player, Npc npc)
	{
		if (enterReflection(player, npc, 122))
		{
			final var r = player.getReflection();
			if (isInReflection(r))
			{
				r.setParam("conquestEnded", false);
				r.setParam("tumorRespawnTime", 180000);
				r.setParam("wagonRespawnTime", 60000);
				r.setParam("coffinsCreated", 0);
				final List<Npc> deadTumors = new ArrayList<>();
				r.setParam("deadTumors", deadTumors);
				r.setParam("BEGIN", ThreadPoolManager.getInstance().schedule(() -> conquestBegins(r), 20000L));
			}
		}
	}
	
	@Override
	protected void onTeleportEnter(Player player, ReflectionTemplate template, Reflection r, boolean firstEntrance)
	{
		if (firstEntrance)
		{
			r.addAllowed(player);
			player.getAI().setIntention(CtrlIntention.IDLE);
			final var teleLoc = template.getTeleportCoord();
			player.teleToLocation(teleLoc, true, r);
			if (player.hasSummon())
			{
				player.getSummon().getAI().setIntention(CtrlIntention.IDLE);
				player.getSummon().teleToLocation(teleLoc, true, r);
			}
		}
		else
		{
			player.getAI().setIntention(CtrlIntention.IDLE);
			final var teleLoc = template.getTeleportCoord();
			player.teleToLocation(teleLoc, true, r);
			if (player.hasSummon())
			{
				player.getSummon().getAI().setIntention(CtrlIntention.IDLE);
				player.getSummon().teleToLocation(teleLoc, true, r);
			}
		}
	}

	private void conquestBegins(Reflection r)
	{
		if (r != null)
		{
			r.broadcastPacket(new ExShowScreenMessage(NpcStringId.YOU_CAN_HEAR_THE_UNDEAD_OF_EKIMUS_RUSHING_TOWARD_YOU_S1_S2_IT_HAS_NOW_BEGUN, 2, 1, 8000));
			r.spawnByGroup("soi_hoi_defence_mob_1");
			r.spawnByGroup("soi_hoi_defence_mob_2");
			r.spawnByGroup("soi_hoi_defence_mob_3");
			r.spawnByGroup("soi_hoi_defence_mob_4");
			r.spawnByGroup("soi_hoi_defence_mob_5");
			r.spawnByGroup("soi_hoi_defence_mob_6");
			r.spawnByGroup("soi_hoi_defence_tumors");
			r.spawnByGroup("soi_hoi_defence_wards");
			r.getDoor(14240102).openMe();
			for (int zoneId = 20040; zoneId < 20046; zoneId++)
			{
				getActivatedZone(r, zoneId, true);
			}
			final var preawakenedEchmus = addSpawn(29161, -179534, 208510, -15496, 16342, false, 0, false, r);
			r.setParam("preawakenedEchmus", preawakenedEchmus);
			
			r.addTimer("COFFIN_SPAWN", ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> coffinSpawn(r), 1000L, 60000L));
			r.addTimer("ALIVE_TUMOR_SPAWN", ThreadPoolManager.getInstance().schedule(() -> aliveTumorSpawn(r), r.getParams().getLong("tumorRespawnTime", 0)));
			r.addTimer("WAGON_SPAWN", ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> wagonSpawn(r), 1000L, r.getParams().getLong("wagonRespawnTime", 0)));
			
			r.setParam("startTime", System.currentTimeMillis());
			
			r.addTimer("TIMER", ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> timerTask(r), 298000L, 300000L));
		}
	}
	
	private void wagonSpawn(Reflection r)
	{
		if (r != null)
		{
			addSpawn(22523, -179544, 207400, -15496, 0, false, 0, false, r);
		}
	}
	
	private void coffinSpawn(Reflection r)
	{
		if (r != null)
		{
			final var conquestEnded = r.getParams().getBool("conquestEnded", false);
			if (!conquestEnded)
			{
				for (final var n : r.getNpcs())
				{
					if (n != null && n.getId() == 32535)
					{
						spawnNpc(18709, n.getLocation(), 0, r);
					}
				}
			}
		}
	}
	
	private void aliveTumorSpawn(Reflection r)
	{
		if (r != null)
		{
			final var conquestEnded = r.getParams().getBool("conquestEnded", false);
			if (!conquestEnded)
			{
				r.despawnByGroup("soi_hoi_defence_tumors");
				r.spawnByGroup("soi_hoi_defence_alivetumors");
				for (final var n : r.getNpcs())
				{
					if (n != null && n.getId() == 18708)
					{
						n.setCurrentHp(n.getMaxHp() * 0.5);
					}
				}
				final var msg = new ExShowScreenMessage(NpcStringId.THE_TUMOR_INSIDE_S1_HAS_COMPLETELY_REVIVED_NEKIMUS_STARTED_TO_REGAIN_HIS_ENERGY_AND_IS_DESPERATELY_LOOKING_FOR_HIS_PREY, 2, 1, 8000);
				msg.addStringParameter("#" + NpcStringId.HEART_OF_IMMORTALITY.getId());
				r.broadcastPacket(msg);
			}
		}
	}

	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final var r = player.getReflection();
		if (isInReflection(r))
		{
			final var party = player.getParty();
			if (event.startsWith("warpechmus"))
			{
				final var msg = new ExShowScreenMessage(NpcStringId.S1S_PARTY_HAS_MOVED_TO_A_DIFFERENT_LOCATION_THROUGH_THE_CRACK_IN_THE_TUMOR, 2, 1, 8000);
				msg.addStringParameter(party != null ? party.getLeader().getName(null) : player.getName(null));
				r.broadcastPacket(msg);
				if (party != null)
				{
					for (final var member : party.getMembers())
					{
						if (member != null && member.isInsideRadius(npc, 800, true, false))
						{
							member.teleToLocation(-179548, 209584, -15504, true, r);
						}
					}
				}
			}
			else if (event.startsWith("reenterechmus"))
			{
				player.destroyItemByItemId("SOI", 13797, 3, player, true);
				if (party != null)
				{
					for (final var member : party.getMembers())
					{
						if (member != null && member.isInsideRadius(npc, 400, true, false))
						{
							member.teleToLocation(-179548, 209584, -15504, true, r);
						}
					}
				}
			}
			else if (event.startsWith("warp"))
			{
				final var deadTumors = r.getParams().getList("deadTumors", Npc.class);
				if (deadTumors != null && !deadTumors.isEmpty())
				{
					player.destroyItemByItemId("SOI", 13797, 1, player, true);
					final var loc = deadTumors.get(getRandom(deadTumors.size())).getLocation();
					if (loc != null)
					{
						final var msg = new ExShowScreenMessage(NpcStringId.S1S_PARTY_HAS_MOVED_TO_A_DIFFERENT_LOCATION_THROUGH_THE_CRACK_IN_THE_TUMOR, 2, 1, 8000);
						msg.addStringParameter(player.getParty() != null ? player.getParty().getLeader().getName(null) : player.getName(null));
						r.broadcastPacket(msg);
						if (party != null)
						{
							for (final var member : party.getMembers())
							{
								if (member != null && member.isInsideRadius(player, 500, true, false))
								{
									member.teleToLocation(loc, true, r);
								}
							}
						}
					}
				}
			}
		}
		return "";
	}

	@Override
	public String onTalk(Npc npc, Player player)
	{
		var st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}

		if (npc.getId() == 32539)
		{
			enterInstance(player, npc);
		}
		return "";
	}

	@Override
	public String onKill(Npc npc, Player player, boolean isSummon)
	{
		final var r = npc.getReflection();
		if (isInReflection(r))
		{
			final var loc = npc.getLocation();
			if (npc.getId() == 18708)
			{
				((MonsterInstance) npc).dropSingleItem(player, 13797, getRandom(2, 5));
				npc.deleteMe();
				final var deadTumor = spawnNpc(32535, loc, 0, r);
				final var deadTumors = r.getParams().getList("deadTumors", Npc.class);
				if (deadTumors != null)
				{
					deadTumors.add(deadTumor);
				}
				
				final var wagonRespawnTime = r.getParams().getInteger("wagonRespawnTime", 0) + 10000;
				r.setParam("wagonRespawnTime", wagonRespawnTime);
				
				final var msg = new ExShowScreenMessage(NpcStringId.THE_TUMOR_INSIDE_S1_HAS_BEEN_DESTROYED_NTHE_SPEED_THAT_EKIMUS_CALLS_OUT_HIS_PREY_HAS_SLOWED_DOWN, 2, 1, 8000);
				msg.addStringParameter("#" + NpcStringId.HEART_OF_IMMORTALITY.getId());
				r.broadcastPacket(msg);
				
				r.addTimer("TUMOR_RESPAWN", ThreadPoolManager.getInstance().schedule(() -> tumorRespawn(r, deadTumor), r.getParams().getLong("tumorRespawnTime", 0)));
			}

			if (npc.getId() == 18711)
			{
				final var tumorRespawnTime = r.getParams().getLong("tumorRespawnTime", 0) + 5000L;
				r.setParam("tumorRespawnTime", tumorRespawnTime);
			}
		}
		return super.onKill(npc, player, isSummon);
	}

	private void tumorRespawn(Reflection r, Npc npc)
	{
		if (r != null && npc != null)
		{
			final var deadTumors = r.getParams().getList("deadTumors", Npc.class);
			if (deadTumors != null)
			{
				deadTumors.remove(npc);
			}
			
			final var alivetumor = spawnNpc(18708, npc.getLocation(), 0, r);
			if (alivetumor != null)
			{
				alivetumor.setCurrentHp(alivetumor.getMaxHp() * .25);
			}
			npc.deleteMe();
			
			final var wagonRespawnTime = r.getParams().getInteger("wagonRespawnTime", 0) - 10000;
			r.setParam("wagonRespawnTime", wagonRespawnTime);
			
			final var msg = new ExShowScreenMessage(NpcStringId.THE_TUMOR_INSIDE_S1_HAS_COMPLETELY_REVIVED_NEKIMUS_STARTED_TO_REGAIN_HIS_ENERGY_AND_IS_DESPERATELY_LOOKING_FOR_HIS_PREY, 2, 1, 8000);
			msg.addStringParameter("#" + NpcStringId.HEART_OF_IMMORTALITY.getId());
			r.broadcastPacket(msg);
		}
	}
	
	private void notifyWagonArrived(Reflection r)
	{
		if (r != null)
		{
			final var coffinsCreated = r.getParams().getInteger("coffinsCreated", 0) + 1;
			r.setParam("coffinsCreated", coffinsCreated);
			if (coffinsCreated >= 20)
			{
				conquestConclusion(r, false);
			}
			else
			{
				final var preawakenedEchmus = r.getParams().getObject("preawakenedEchmus", Npc.class);
				if (preawakenedEchmus != null)
				{
					final var cs = new NpcSay(preawakenedEchmus.getObjectId(), Say2.SHOUT, preawakenedEchmus.getId(), NpcStringId.BRING_MORE_MORE_SOULS);
					preawakenedEchmus.broadcastPacketToOthers(cs);
					final var message = new ExShowScreenMessage(NpcStringId.THE_SOUL_COFFIN_HAS_AWAKENED_EKIMUS, 2, 1, 8000);
					message.addStringParameter(Integer.toString(20 - coffinsCreated));
					r.broadcastPacket(message);
				}
				final int[] spawn = ZoneManager.getInstance().getZoneById(200032).getZone().getRandomPoint();
				addSpawn(18713, spawn[0], spawn[1], spawn[2], 0, false, 0, false, r);
			}
		}
	}

	private void timerTask(Reflection r)
	{
		if (r != null)
		{
			final var startTime = r.getParams().getLong("startTime", 0);
			final long time = ((startTime + 1500000L) - System.currentTimeMillis()) / 60000;
			if (time <= 0)
			{
				conquestConclusion(r, true);
			}
			else
			{
				if (time == 15)
				{
					r.spawnByGroup("soi_hoi_defence_bosses");
					final var msg = new ExShowScreenMessage(NpcStringId.S1_MINUTES_REMAINING, 2, 1, 8000);
					msg.addStringParameter(Integer.toString((int) time));
					r.broadcastPacket(msg);
				}
			}
		}
	}

	private void conquestConclusion(Reflection r, boolean win)
	{
		if (r != null)
		{
			final var conquestEnded = r.getParams().getBool("conquestEnded", false);
			if (conquestEnded)
			{
				return;
			}
			
			r.removeTimer("TIMER");
			r.removeTimer("COFFIN_SPAWN");
			r.removeTimer("ALIVE_TUMOR_SPAWN");
			r.removeTimer("WAGON_SPAWN");
			r.setParam("conquestEnded", true);
			
			if (win)
			{
				finishInstance(r, 900000, true);
				final ExShowScreenMessage msg = new ExShowScreenMessage(NpcStringId.CONGRATULATIONS_YOU_HAVE_SUCCEEDED_AT_S1_S2_THE_INSTANCE_WILL_SHORTLY_EXPIRE, 2, 1, 8000);
				msg.addStringParameter("#" + NpcStringId.HEART_OF_IMMORTALITY.getId());
				msg.addStringParameter("#" + NpcStringId.ATTACK.getId());
				r.broadcastPacket(msg);
				handleReenterTime(r);
				for (final var player : r.getReflectionPlayers())
				{
					final var st = player.getQuestState("_697_DefendtheHallofErosion");
					if (st != null && (st.isCond(1)))
					{
						st.set("defenceDone", 1);
					}
				}
			}
			else
			{
				finishInstance(r, 900000, false);
				final var msg = new ExShowScreenMessage(NpcStringId.YOU_HAVE_FAILED_AT_S1_S2_THE_INSTANCE_WILL_SHORTLY_EXPIRE, 2, 1, 8000);
				msg.addStringParameter("#" + NpcStringId.HEART_OF_IMMORTALITY.getId());
				msg.addStringParameter("#" + NpcStringId.ATTACK.getId());
				r.broadcastPacket(msg);
			}
		}
	}

	@Override
	public final String onEnterZone(Creature character, ZoneType zone)
	{
		if (character instanceof Attackable npc)
		{
			final var r = npc.getReflection();
			if (isInReflection(r))
			{
				if (npc.getId() == 22523)
				{
					notifyWagonArrived(r);
					npc.deleteMe();
				}
			}
		}
		return null;
	}

	void main()
	{
		new HeartInfinityDefence();
	}
}