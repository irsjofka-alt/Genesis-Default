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
import gameserver.ai.model.CtrlEvent;
import gameserver.ai.model.CtrlIntention;
import gameserver.model.World;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.actor.instance.MonsterInstance;
import gameserver.model.actor.instance.QuestGuardInstance;
import gameserver.model.actor.templates.reflection.ReflectionTemplate;
import gameserver.model.entity.Reflection;
import gameserver.model.zone.ZoneType;
import gameserver.network.NpcStringId;
import gameserver.network.clientpackets.Say2;
import gameserver.network.serverpackets.ExShowScreenMessage;
import gameserver.network.serverpackets.NpcSay;

/**
 * Rework by LordWinter 25.09.2020
 */
public class ErosionHallDefence extends AbstractReflection
{
	public ErosionHallDefence()
	{
		super(120);

		addStartNpc(32535, 32537);
		addTalkId(32535, 32537);
		addSpawnId(32535, 32541);
		addEnterZoneId(20014);
		addKillId(18708, 18711, 32541);
	}
	
	@Override
	public final String onEnterZone(Creature character, ZoneType zone)
	{
		if (character.isPlayer())
		{
			final var r = character.getReflection();
			if (isInReflection(r))
			{
				final var conquestBegun = r.getParams().getBool("conquestBegun", false);
				if (!conquestBegun)
				{
					r.setParam("conquestBegun", true);
					runTumors(r);
					r.setParam("startTime", System.currentTimeMillis());
					r.addTimer("TIMER", ThreadPoolManager.getInstance().schedule(() -> timerTask(r), 1200000L));
				}
			}
		}
		return super.onEnterZone(character, zone);
	}

	private synchronized void enterInstance(Player player, Npc npc)
	{
		if (enterReflection(player, npc, 120))
		{
			final var r = player.getReflection();
			if (isInReflection(r))
			{
				r.setParam("conquestBegun", false);
				r.setParam("conquestEnded", false);
				r.setParam("tumorRespawnTime", 180000);
				r.setParam("soulwagonSpawned", false);
				r.setParam("seedKills", 0);
				r.setParam("tumorKillCount", 0);
				r.setParam("TAG", -1);
				final List<Npc> alivetumor = new ArrayList<>();
				r.setParam("alivetumor", alivetumor);
				final List<Npc> deadTumors = new ArrayList<>();
				r.setParam("deadTumors", deadTumors);
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

	private void runTumors(Reflection r)
	{
		if (r != null)
		{
			r.spawnByGroup("soi_hoe_defence_lifeseed");
			r.spawnByGroup("soi_hoe_defence_tumor");
			r.spawnByGroup("soi_hoe_defence_wards");
			r.spawnByGroup("soi_hoe_defence_mob_1");
			r.spawnByGroup("soi_hoe_defence_mob_2");
			r.spawnByGroup("soi_hoe_defence_mob_3");
			r.spawnByGroup("soi_hoe_defence_mob_4");
			r.spawnByGroup("soi_hoe_defence_mob_5");
			r.spawnByGroup("soi_hoe_defence_mob_6");
			r.spawnByGroup("soi_hoe_defence_mob_7");
			r.spawnByGroup("soi_hoe_defence_mob_8");
			
			for (int zoneId = 20008; zoneId < 20029; zoneId++)
			{
				getActivatedZone(r, zoneId, true);
			}
			r.addTimer("AGGRO", ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> aggroMobs(r), 15000L, 25000L));
			r.addTimer("COFFIN_SPAWN", ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> coffinSpawn(r), 1000L, 60000L));
			r.addTimer("ALIVE_TUMOR_SPAWN", ThreadPoolManager.getInstance().schedule(() -> aliveTumorSpawn(r), r.getParams().getLong("tumorRespawnTime", 0)));
			r.broadcastPacket(new ExShowScreenMessage(NpcStringId.YOU_CAN_HEAR_THE_UNDEAD_OF_EKIMUS_RUSHING_TOWARD_YOU_S1_S2_IT_HAS_NOW_BEGUN, 2, 1, 8000));
		}
	}
	
	private void aliveTumorSpawn(Reflection r)
	{
		if (r != null)
		{
			final var conquestEnded = r.getParams().getBool("conquestEnded", false);
			if (!conquestEnded)
			{
				r.despawnByGroup("soi_hoe_defence_tumor");
				r.spawnByGroup("soi_hoe_defence_alivetumor");
				for (final var npc : r.getNpcs())
				{
					if (npc.getId() == 18708)
					{
						npc.setCurrentHp(npc.getMaxHp() * 0.5);
					}
				}
				final var msg = new ExShowScreenMessage(NpcStringId.THE_TUMOR_INSIDE_S1_HAS_COMPLETELY_REVIVED_NRECOVERED_NEARBY_UNDEAD_ARE_SWARMING_TOWARD_SEED_OF_LIFE, 2, 1, 8000);
				msg.addStringParameter("#" + NpcStringId.HALL_OF_EROSION.getId());
				r.broadcastPacket(msg);
			}
		}
	}
	
	private void aggroMobs(Reflection r)
	{
		if (r != null)
		{
			final var conquestEnded = r.getParams().getBool("conquestEnded", false);
			if (!conquestEnded)
			{
				for (final var npc : r.getNpcs())
				{
					final var seed = getNearestSeed(npc);
					if (seed != null)
					{
						if (npc.getAI().getIntention() == CtrlIntention.ACTIVE)
						{
							npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, seed, 100);
						}
					}
				}
			}
		}
	}
	
	private void coffinSpawn(Reflection r)
	{
		if (r != null)
		{
			final var conquestEnded = r.getParams().getBool("conquestEnded", false);
			if (!conquestEnded)
			{
				final var deadTumors = r.getParams().getList("deadTumors", Npc.class);
				if (deadTumors != null && !deadTumors.isEmpty())
				{
					for (final var npc : deadTumors)
					{
						if (npc != null)
						{
							spawnNpc(18709, npc.getLocation(), 0, r);
						}
					}
				}
			}
		}
	}

	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final var r = player.getReflection();
		if (isInReflection(r))
		{
			if (event.startsWith("warp"))
			{
				final var deadTumors = r.getParams().getList("deadTumors", Npc.class);
				if (deadTumors != null && !deadTumors.isEmpty())
				{
					player.destroyItemByItemId("SOI", 13797, 1, player, true);
					final var party = player.getParty();
					final var loc = deadTumors.get(getRandom(deadTumors.size())).getLocation();
					if (loc != null)
					{
						final var msg = new ExShowScreenMessage(NpcStringId.S1S_PARTY_HAS_MOVED_TO_A_DIFFERENT_LOCATION_THROUGH_THE_CRACK_IN_THE_TUMOR, 2, 1, 8000);
						msg.addStringParameter(party != null ? party.getLeader().getName(null) : player.getName(null));
						r.broadcastPacket(msg);
						
						if (party != null)
						{
							for (final var member : party.getMembers())
							{
								if (member != null && member.isInsideRadius(npc, 500, true, false))
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

		if (npc.getId() == 32537)
		{
			enterInstance(player, npc);
		}
		return null;
	}

	@Override
	public final String onSpawn(Npc npc)
	{
		switch (npc.getId())
		{
			case 32541 :
				((QuestGuardInstance) npc).setPassive(true);
				npc.setCurrentHp(500000);
				break;
			case 32535 :
				final var r = npc.getReflection();
				if (isInReflection(r))
				{
					final var deadTumors = r.getParams().getList("deadTumors", Npc.class);
					if (deadTumors != null)
					{
						deadTumors.add(npc);
					}
					final var tag = r.getParams().getInteger("TAG", -1) + 1;
					r.setParam("TAG", tag);
				}
				break;
		}
		return super.onSpawn(npc);
	}

	@Override
	public String onKill(Npc npc, Player player, boolean isSummon)
	{
		final var r = npc.getReflection();
		if (isInReflection(r))
		{
			if (npc.getId() == 18708)
			{
				((MonsterInstance) npc).dropSingleItem(player, 13797, getRandom(2, 5));
				final var alivetumor = r.getParams().getList("alivetumor", Npc.class);
				if (alivetumor != null)
				{
					alivetumor.remove(npc);
				}
				npc.deleteMe();
				notifyTumorDeath(r);
				final var n = spawnNpc(32535, npc.getLocation(), 0, r);
				final var msg = new ExShowScreenMessage(NpcStringId.THE_TUMOR_INSIDE_S1_HAS_BEEN_DESTROYED_NTHE_NEARBY_UNDEAD_THAT_WERE_ATTACKING_SEED_OF_LIFE_START_LOSING_THEIR_ENERGY_AND_RUN_AWAY, 2, 1, 8000);
				msg.addStringParameter("#" + NpcStringId.HALL_OF_EROSION.getId());
				r.broadcastPacket(msg);
				r.addTimer("TUMOR_RESPAWN", ThreadPoolManager.getInstance().schedule(() -> tumorRespawn(r, n), r.getParams().getLong("tumorRespawnTime", 0)));
			}
			else if (npc.getId() == 25636)
			{
				conquestConclusion(r, true);
			}
			if (npc.getId() == 18711)
			{
				final var tumorRespawnTime = r.getParams().getLong("tumorRespawnTime", 0) - 5000L;
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
			final Npc tumor = spawnNpc(18708, npc.getLocation(), 0, r);
			tumor.setCurrentHp(tumor.getMaxHp() * 0.25);
			npc.deleteMe();
			
			final var alivetumor = r.getParams().getList("alivetumor", Npc.class);
			if (alivetumor != null)
			{
				alivetumor.add(tumor);
			}
			
			final var tumorKillCount = r.getParams().getInteger("tumorKillCount", 0) - 1;
			r.setParam("tumorKillCount", tumorKillCount);
			final var msg = new ExShowScreenMessage(NpcStringId.THE_TUMOR_INSIDE_S1_HAS_COMPLETELY_REVIVED_NRECOVERED_NEARBY_UNDEAD_ARE_SWARMING_TOWARD_SEED_OF_LIFE, 2, 1, 8000);
			msg.addStringParameter("#" + NpcStringId.HALL_OF_EROSION.getId());
			r.broadcastPacket(msg);
		}
	}
	
	private void notifyTumorDeath(Reflection r)
	{
		if (r != null)
		{
			final var tumorKillCount = r.getParams().getInteger("tumorKillCount", 0) + 1;
			final var soulwagonSpawned = r.getParams().getBool("soulwagonSpawned", false);
			r.setParam("tumorKillCount", tumorKillCount);
			if (tumorKillCount >= 4 && !soulwagonSpawned)
			{
				r.setParam("soulwagonSpawned", true);
				r.spawnByGroup("soi_hoe_defence_soulwagon");
				for (final var npc : r.getNpcs())
				{
					if (npc.getId() == 25636)
					{
						final var cs = new NpcSay(npc.getObjectId(), Say2.SHOUT, npc.getId(), NpcStringId.HA_HA_HA);
						npc.broadcastPacketToOthers(cs);
						
						final var seed = getNearestSeed(npc);
						if (seed != null)
						{
							npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, seed, 100);
						}
						r.addTimer("FAILURE", ThreadPoolManager.getInstance().schedule(() -> conquestConclusion(r, false), 180000L));
					}
				}
			}
		}
	}

	@Override
	public String onKillByMob(Npc npc, Npc killer)
	{
		final var r = npc.getReflection();
		if (isInReflection(r))
		{
			final var seedKills = r.getParams().getInteger("seedKills", 0) + 1;
			r.setParam("seedKills", seedKills);
			if (seedKills >= 4)
			{
				conquestConclusion(r, false);
			}
		}
		return super.onKillByMob(npc, killer);
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
			r.setParam("conquestEnded", true);
			r.removeTimer("TIMER");
			r.removeTimer("AGGRO");
			r.removeTimer("COFFIN_SPAWN");
			r.removeTimer("ALIVE_TUMOR_SPAWN");
			r.removeTimer("FAILURE");
			finishInstance(r, 900000, false);
			if (win)
			{
				for (final var player : r.getReflectionPlayers())
				{
					final var st = player.getQuestState("_697_DefendtheHallofErosion");
					if (st != null && st.isCond(1))
					{
						st.set("defenceDone", 1);
					}
				}
				r.cleanupNpcs();
				final var msg = new ExShowScreenMessage(NpcStringId.CONGRATULATIONS_YOU_HAVE_SUCCEEDED_AT_S1_S2_THE_INSTANCE_WILL_SHORTLY_EXPIRE, 2, 1, 8000);
				msg.addStringParameter("#" + NpcStringId.HALL_OF_EROSION.getId());
				msg.addStringParameter("#" + NpcStringId.DEFEND.getId());
				r.broadcastPacket(msg);
				handleReenterTime(r);
			}
			else
			{
				final var msg = new ExShowScreenMessage(NpcStringId.YOU_HAVE_FAILED_AT_S1_S2_THE_INSTANCE_WILL_SHORTLY_EXPIRE, 2, 1, 8000);
				msg.addStringParameter("#" + NpcStringId.HALL_OF_EROSION.getId());
				msg.addStringParameter("#" + NpcStringId.DEFEND.getId());
				r.broadcastPacket(msg);
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
				conquestConclusion(r, false);
			}
			else
			{
				final var msg = new ExShowScreenMessage(NpcStringId.S1_MINUTES_REMAINING, 2, 1, 8000);
				msg.addStringParameter(Integer.toString((int) time));
				r.broadcastPacket(msg);
			}
		}
	}
	
	private static Npc getNearestSeed(Npc mob)
	{
		for (final var npc : World.getAroundNpc(mob, 900, 300))
		{
			if (npc.getId() == 32541)
			{
				return npc;
			}
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new ErosionHallDefence();
	}
}
