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
import gameserver.instancemanager.SoIManager;
import gameserver.instancemanager.ZoneManager;
import gameserver.model.Location;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.actor.instance.MonsterInstance;
import gameserver.model.actor.templates.reflection.ReflectionTemplate;
import gameserver.model.entity.Reflection;
import gameserver.model.skills.Skill;
import gameserver.model.skills.funcs.FuncGet;
import gameserver.model.stats.Stats;
import gameserver.network.NpcStringId;
import gameserver.network.clientpackets.Say2;
import gameserver.network.serverpackets.ExShowScreenMessage;
import gameserver.network.serverpackets.NpcSay;

/**
 * Rework by LordWinter 30.09.2020
 */
public class HeartInfinityAttack extends AbstractReflection
{
	public HeartInfinityAttack()
	{
		super(121);

		addStartNpc(32535, 32536, 32540);
		addTalkId(32535, 32536, 32540);
		addSpawnId(32535);
		addAttackId(29150);
		addKillId(18708, 18711, 29150);
	}
	
	private synchronized void enterInstance(Player player, Npc npc)
	{
		if (enterReflection(player, npc, 121))
		{
			final var r = player.getReflection();
			if (isInReflection(r))
			{
				r.spawnByGroup("soi_hoi_attack_init");
				r.setParam("conquestBegun", false);
				r.setParam("conquestEnded", false);
				r.setParam("houndBlocked", false);
				r.setParam("faildAnnounce", false);
				r.setParam("TAG", -1);
				r.setParam("tumorCount", 6);
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
			final Location teleLoc = template.getTeleportCoord();
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
			final Location teleLoc = template.getTeleportCoord();
			player.teleToLocation(teleLoc, true, r);
			if (player.hasSummon())
			{
				player.getSummon().getAI().setIntention(CtrlIntention.IDLE);
				player.getSummon().teleToLocation(teleLoc, true, r);
			}
		}
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		var st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}
		
		if (npc.getId() == 32540)
		{
			enterInstance(player, npc);
		}
		return "";
	}

	private void notifyEchmusEntrance(final Reflection r, Player player)
	{
		final var conquestBegun = r.getParams().getBool("conquestBegun", false);
		if (conquestBegun)
		{
			return;
		}

		r.setParam("conquestBegun", true);
		r.setParam("invoker", player);
		final ExShowScreenMessage msg = new ExShowScreenMessage(NpcStringId.YOU_WILL_PARTICIPATE_IN_S1_S2_SHORTLY_BE_PREPARED_FOR_ANYTHING, 2, 1, 8000);
		msg.addStringParameter("#" + NpcStringId.HEART_OF_IMMORTALITY.getId());
		msg.addStringParameter("#" + NpcStringId.ATTACK.getId());
		r.broadcastPacket(msg);
		r.addTimer("SHOW_MOVIE", ThreadPoolManager.getInstance().schedule(() -> showMovie(r), 20000));
	}
	
	private void showMovie(Reflection r)
	{
		if (r != null)
		{
			for (final var player : r.getReflectionPlayers())
			{
				player.showQuestMovie(2);
			}
			r.addTimer("BEGIN", ThreadPoolManager.getInstance().schedule(() -> conquestBegins(r), 62500));
		}
	}

	private void conquestBegins(Reflection r)
	{
		if (r != null)
		{
			r.despawnByGroup("soi_hoi_attack_init");
			r.spawnByGroup("soi_hoi_attack_mob_1");
			r.spawnByGroup("soi_hoi_attack_mob_2");
			r.spawnByGroup("soi_hoi_attack_mob_3");
			r.spawnByGroup("soi_hoi_attack_mob_4");
			r.spawnByGroup("soi_hoi_attack_mob_5");
			r.spawnByGroup("soi_hoi_attack_mob_6");
			r.spawnByGroup("soi_hoi_attack_tumors");
			for (final var n : r.getNpcs())
			{
				if (n != null && n.getId() == 18708)
				{
					n.setCurrentHp(n.getMaxHp() * .5);
				}
			}
			r.spawnByGroup("soi_hoi_attack_wards");
			r.setParam("tumorRespawnTime", 150000L);
			
			final var ekimus = addSpawn(29150, -179537, 208854, -15504, 16384, false, 0, false, r);
			r.setParam("ekimus", ekimus);
			
			final List<Npc> hounds = new ArrayList<>(2);
			hounds.add(addSpawn(29151, -179224, 209624, -15504, 16384, false, 0, false, r));
			hounds.add(addSpawn(29151, -179880, 209464, -15504, 16384, false, 0, false, r));
			r.setParam("hounds", hounds);
			
			r.setParam("lastAction", System.currentTimeMillis());
			r.addTimer("EKIMUS_ACTIVITY", ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> notifyEkimusActivity(r), 60000, 60000));
			handleEkimusStats(r);
			for (int zoneId = 20040; zoneId < 20046; zoneId++)
			{
				getActivatedZone(r, zoneId, true);
			}
			r.getDoor(14240102).openMe();
			r.broadcastPacket(new ExShowScreenMessage(NpcStringId.YOU_CAN_HEAR_THE_UNDEAD_OF_EKIMUS_RUSHING_TOWARD_YOU_S1_S2_IT_HAS_NOW_BEGUN, 2, 1, 8000));
			
			final var invoker = r.getParams().getObject("invoker", Player.class);
			if (invoker != null)
			{
				ekimus.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, invoker, 50000);
				final var cs = new NpcSay(ekimus.getObjectId(), Say2.SHOUT, ekimus.getId(), NpcStringId.I_SHALL_ACCEPT_YOUR_CHALLENGE_S1_COME_AND_DIE_IN_THE_ARMS_OF_IMMORTALITY);
				cs.addStringParameter(invoker.getName(null));
				ekimus.broadcastPacketToOthers(cs);
			}
			r.setParam("startTime", System.currentTimeMillis());
			r.addTimer("TIMER", ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> timerTask(r), 298000L, 300000L));
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
				notifyEchmusEntrance(r, player);
			}
			else if (event.startsWith("reenterechmus"))
			{
				player.destroyItemByItemId("SOI", 13797, 3, player, true);
				notifyEkimusRoomEntrance(r);
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
						msg.addStringParameter(party != null ? party.getLeader().getName(null) : player.getName(null));
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
	public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon, Skill skill)
	{
		final var r = npc.getReflection();
		if (isInReflection(r))
		{
			if (npc.getId() == 29150)
			{
				final var faildAnnounce = r.getParams().getBool("faildAnnounce", false);
				if (faildAnnounce)
				{
					r.setParam("faildAnnounce", false);
				}
				r.setParam("lastAction", System.currentTimeMillis());
				final var hounds = r.getParams().getList("hounds", Npc.class);
				if (hounds != null && !hounds.isEmpty())
				{
					for (final var mob : hounds)
					{
						if (mob != null && !mob.isDead())
						{
							((MonsterInstance) mob).addDamageHate(attacker, 0, 500);
							mob.setRunning();
							mob.getAI().setIntention(CtrlIntention.ATTACK, attacker);
						}
					}
				}
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon, skill);
	}

	@Override
	public final String onSpawn(Npc npc)
	{
		final var r = npc.getReflection();
		if (isInReflection(r))
		{
			if (npc.getId() == 32535)
			{
				final var tag = r.getParams().getInteger("TAG", 0) + 1;
				r.setParam("TAG", tag);
			}
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
				npc.deleteMe();
				final var tumorCount = r.getParams().getInteger("tumorCount", 6) - 1;
				r.setParam("tumorCount", tumorCount);
				notifyTumorDeath(r);
				final var deadTumor = spawnNpc(32535, npc.getLocation(), 0, r);
				final var deadTumors = r.getParams().getList("deadTumors", Npc.class);
				if (deadTumors != null)
				{
					deadTumors.add(deadTumor);
				}
				r.addTimer("TUMOR_REVIVAL", ThreadPoolManager.getInstance().schedule(() -> tumorRevival(deadTumor, r), r.getParams().getLong("tumorRespawnTime", 0)));
				r.addTimer("REGEN_COFFIN", ThreadPoolManager.getInstance().schedule(() -> regenerationCoffinSpawn(deadTumor, r), 20000));
			}
			else if (npc.getId() == 29150)
			{
				conquestConclusion(r, true);
				SoIManager.getInstance().notifyEkimusKill();
			}
			else if (npc.getId() == 18711)
			{
				final var tumorRespawnTime = r.getParams().getLong("tumorRespawnTime", 0) + 8000L;
				r.setParam("tumorRespawnTime", tumorRespawnTime);
			}
		}
		return super.onKill(npc, player, isSummon);
	}
	
	private void notifyTumorDeath(Reflection r)
	{
		if (r != null)
		{
			final var tumorCount = r.getParams().getInteger("tumorCount", 6);
			if (tumorCount < 1)
			{
				r.setParam("houndBlocked", true);
				final var hounds = r.getParams().getList("hounds", Npc.class);
				if (hounds != null && !hounds.isEmpty())
				{
					for (final var hound : hounds)
					{
						if (hound != null)
						{
							hound.block();
						}
					}
					r.broadcastPacket(new ExShowScreenMessage(NpcStringId.WITH_ALL_CONNECTIONS_TO_THE_TUMOR_SEVERED_EKIMUS_HAS_LOST_ITS_POWER_TO_CONTROL_THE_FERAL_HOUND, 2, 1, 8000));
				}
			}
			else
			{
				final var msg = new ExShowScreenMessage(NpcStringId.THE_TUMOR_INSIDE_S1_THAT_HAS_PROVIDED_ENERGY_N_TO_EKIMUS_IS_DESTROYED, 2, 1, 8000);
				msg.addStringParameter("#" + NpcStringId.HEART_OF_IMMORTALITY.getId());
				r.broadcastPacket(msg);
			}
			handleEkimusStats(r);
		}
	}
	
	private void notifyTumorRevival(Reflection r)
	{
		if (r != null)
		{
			final var tumorCount = r.getParams().getInteger("tumorCount", 6);
			final var houndBlocked = r.getParams().getBool("houndBlocked", false);
			if ((tumorCount == 1) && houndBlocked)
			{
				r.setParam("houndBlocked", false);
				final var hounds = r.getParams().getList("hounds", Npc.class);
				if (hounds != null && !hounds.isEmpty())
				{
					for (final var hound : hounds)
					{
						if (hound != null)
						{
							hound.unblock();
						}
					}
					r.broadcastPacket(new ExShowScreenMessage(NpcStringId.WITH_THE_CONNECTION_TO_THE_TUMOR_RESTORED_EKIMUS_HAS_REGAINED_CONTROL_OVER_THE_FERAL_HOUND, 2, 1, 8000));
				}
			}
			else
			{
				final var msg = new ExShowScreenMessage(NpcStringId.THE_TUMOR_INSIDE_S1_HAS_BEEN_COMPLETELY_RESURRECTED_N_AND_STARTED_TO_ENERGIZE_EKIMUS_AGAIN, 2, 1, 8000);
				msg.addStringParameter("#" + NpcStringId.HEART_OF_IMMORTALITY.getId());
				r.broadcastPacket(msg);
			}
			handleEkimusStats(r);
		}
	}

	private void tumorRevival(Npc deadTumor, Reflection r)
	{
		if (r != null)
		{
			final var conquestEnded = r.getParams().getBool("conquestEnded", false);
			if (conquestEnded)
			{
				return;
			}
			final var alivetumor = spawnNpc(18708, deadTumor.getLocation(), 0, r);
			alivetumor.setCurrentHp(alivetumor.getMaxHp() * .25);
			final var tumorCount = r.getParams().getInteger("tumorCount", 6) + 1;
			r.setParam("tumorCount", tumorCount);
			notifyTumorRevival(r);
			final var deadTumors = r.getParams().getList("deadTumors", Npc.class);
			if (deadTumors != null)
			{
				deadTumors.remove(deadTumor);
			}
			deadTumor.deleteMe();
			final var tag = r.getParams().getInteger("TAG", 0) - 1;
			r.setParam("TAG", tag);
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
				if (time == 20)
				{
					r.spawnByGroup("soi_hoi_attack_bosses");
				}
				final var msg = new ExShowScreenMessage(NpcStringId.S1_MINUTES_REMAINING, 2, 1, 8000);
				msg.addStringParameter(Integer.toString((int) time));
				r.broadcastPacket(msg);
			}
		}
	}

	private void regenerationCoffinSpawn(Npc deadTumor, Reflection r)
	{
		if (r != null)
		{
			final var conquestEnded = r.getParams().getBool("conquestEnded", false);
			if (conquestEnded)
			{
				return;
			}
			for (int i = 0; i < 4; i++)
			{
				spawnNpc(18710, deadTumor.getLocation(), 0, r);
			}
		}
	}

	private void handleEkimusStats(Reflection r)
	{
		if (r != null)
		{
			final double[] a = getStatMultiplier(r);
			final var ekimus = r.getParams().getObject("ekimus", Npc.class);
			if (ekimus != null)
			{
				ekimus.removeStatsOwner(this);
				ekimus.addStatFunc(new FuncGet(Stats.POWER_ATTACK, 0x30, this, ekimus.getTemplate().getBasePAtk() * 3));
				ekimus.addStatFunc(new FuncGet(Stats.MAGIC_ATTACK, 0x30, this, ekimus.getTemplate().getBaseMAtk() * 10));
				ekimus.addStatFunc(new FuncGet(Stats.POWER_DEFENCE, 0x30, this, ekimus.getTemplate().getBasePDef() * a[1]));
				ekimus.addStatFunc(new FuncGet(Stats.MAGIC_DEFENCE, 0x30, this, ekimus.getTemplate().getBaseMDef() * a[0]));
				ekimus.addStatFunc(new FuncGet(Stats.REGENERATE_HP_RATE, 0x30, this, ekimus.getTemplate().getBaseHpReg() * a[2]));
			}
		}
	}

	private double[] getStatMultiplier(Reflection r)
	{
		final double[] a = new double[3];
		switch (r.getParams().getInteger("tumorCount", 6))
		{
			case 6 :
				a[0] = 2;
				a[1] = 1;
				a[2] = 4;
				break;
			case 5 :
				a[0] = 1.9;
				a[1] = 0.9;
				a[2] = 3.5;
				break;
			case 4 :
				a[0] = 1.5;
				a[1] = 0.6;
				a[2] = 3.0;
				break;
			case 3 :
				a[0] = 1.0;
				a[1] = 0.4;
				a[2] = 2.5;
				break;
			case 2 :
				a[0] = 0.7;
				a[1] = 0.3;
				a[2] = 2.0;
				break;
			case 1 :
				a[0] = 0.3;
				a[1] = 0.15;
				a[2] = 1.0;
				break;
			case 0 :
				a[0] = 0.12;
				a[1] = 0.06;
				a[2] = 0.25;
				break;
		}
		return a;
	}
	
	private void notifyEkimusActivity(Reflection r)
	{
		if (r != null)
		{
			final var lastAction = r.getParams().getLong("lastAction", 0);
			final var faildAnnounce = r.getParams().getBool("faildAnnounce", false);
			if (lastAction >= 120000L && !faildAnnounce)
			{
				final var msg = new ExShowScreenMessage(NpcStringId.THERE_IS_NO_PARTY_CURRENTLY_CHALLENGING_EKIMUS_N_IF_NO_PARTY_ENTERS_WITHIN_S1_SECONDS_THE_ATTACK_ON_THE_HEART_OF_IMMORTALITY_WILL_FAIL, 2, 1, 8000);
				msg.addStringParameter("60");
				r.broadcastPacket(msg);
				r.setParam("faildAnnounce", true);
			}
			else if (lastAction >= 180000L)
			{
				r.addTimer("CONCLUSION", ThreadPoolManager.getInstance().schedule(() -> conquestConclusion(r, false), 8000L));
			}
		}
	}

	private void notifyEkimusRoomEntrance(final Reflection r)
	{
		if (r != null)
		{
			for (final var p : ZoneManager.getInstance().getZoneById(200032).getPlayersInside())
			{
				if (p != null)
				{
					p.teleToLocation(-179537, 211233, -15472, true, r);
				}
			}
			r.addTimer("EKIMUS_ANNOUNCE", ThreadPoolManager.getInstance().schedule(() -> notifyEkimusAnnounce(r), 10000));
		}
	}
	
	private void notifyEkimusAnnounce(final Reflection r)
	{
		if (r != null)
		{
			r.broadcastPacket(new ExShowScreenMessage(NpcStringId.EKIMUS_HAS_SENSED_ABNORMAL_ACTIVITY_NTHE_ADVANCING_PARTY_IS_FORCEFULLY_EXPELLED, 2, 1, 8000));
		}
	}

	private void conquestConclusion(Reflection r, boolean win)
	{
		if (r != null)
		{
			r.removeTimer("TIMER");
			r.removeTimer("EKIMUS_ACTIVITY");
			r.setParam("conquestEnded", true);
			
			r.despawnByGroup("soi_hoi_attack_wards");
			r.despawnByGroup("soi_hoi_attack_mob_1");
			r.despawnByGroup("soi_hoi_attack_mob_2");
			r.despawnByGroup("soi_hoi_attack_mob_3");
			r.despawnByGroup("soi_hoi_attack_mob_4");
			r.despawnByGroup("soi_hoi_attack_mob_5");
			r.despawnByGroup("soi_hoi_attack_mob_6");
			r.despawnByGroup("soi_hoi_attack_bosses");
			
			final var ekimus = r.getParams().getObject("ekimus", Npc.class);
			if (ekimus != null && !ekimus.isDead())
			{
				ekimus.deleteMe();
			}
			
			final var hounds = r.getParams().getList("hounds", Npc.class);
			if (hounds != null && !hounds.isEmpty())
			{
				for (final var mob : hounds)
				{
					if (mob != null && !mob.isDead())
					{
						mob.deleteMe();
					}
				}
			}
			
			if (win)
			{
				finishInstance(r, 900000, true);
				final var msg = new ExShowScreenMessage(NpcStringId.CONGRATULATIONS_YOU_HAVE_SUCCEEDED_AT_S1_S2_THE_INSTANCE_WILL_SHORTLY_EXPIRE, 2, 1, 8000);
				msg.addStringParameter("#" + NpcStringId.HEART_OF_IMMORTALITY.getId());
				msg.addStringParameter("#" + NpcStringId.ATTACK.getId());
				r.broadcastPacket(msg);
				handleReenterTime(r);
			}
			else
			{
				finishInstance(r, 900000, false);
				final var msg = new ExShowScreenMessage(NpcStringId.YOU_HAVE_FAILED_AT_S1_S2_THE_INSTANCE_WILL_SHORTLY_EXPIRE, 2, 1, 8000);
				msg.addStringParameter("#" + NpcStringId.HEART_OF_IMMORTALITY.getId());
				msg.addStringParameter("#" + NpcStringId.ATTACK.getId());
				r.broadcastPacket(msg);
			}
			
			for (final var npc : r.getNpcs())
			{
				if (npc != null)
				{
					if (npc.getId() == 18708 || npc.getId() == 32535 || npc.getId() == 18710)
					{
						npc.deleteMe();
					}
				}
			}
		}
	}

	public static void main(String[] args)
	{
		new HeartInfinityAttack();
	}
}
