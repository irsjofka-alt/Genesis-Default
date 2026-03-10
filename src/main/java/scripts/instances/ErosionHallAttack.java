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
import java.util.Calendar;
import java.util.List;

import gameserver.ThreadPoolManager;
import gameserver.ai.model.CtrlIntention;
import gameserver.instancemanager.SoIManager;
import gameserver.instancemanager.ZoneManager;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.actor.instance.MonsterInstance;
import gameserver.model.actor.templates.reflection.ReflectionTemplate;
import gameserver.model.entity.Reflection;
import gameserver.model.skills.Skill;
import gameserver.model.zone.ZoneType;
import gameserver.network.NpcStringId;
import gameserver.network.clientpackets.Say2;
import gameserver.network.serverpackets.ExShowScreenMessage;
import gameserver.network.serverpackets.NpcSay;

/**
 * Rework by LordWinter 25.09.2020
 */
public class ErosionHallAttack extends AbstractReflection
{
	private static int[][] COHEMENES_SPAWN =
	{
	        {
	                25634, -178472, 211823, -12025, 0, 0, -1
			},
	        {
	                25634, -180926, 211887, -12029, 0, 0, -1
			},
	        {
	                25634, -180906, 206635, -12032, 0, 0, -1
			},
	        {
	                25634, -178492, 206426, -12023, 0, 0, -1
			}
	};

	public ErosionHallAttack()
	{
		super(119);

		addStartNpc(32535, 32537);
		addTalkId(32535, 32537);
		addAttackId(25634);
		addEnterZoneId(20014);
		addKillId(18708, 18711, 25634);
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
					r.addTimer("TIMER", ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> timerTask(r), 298000L, 300000L));
				}
			}
		}
		return super.onEnterZone(character, zone);
	}

	private synchronized void enterInstance(Player player, Npc npc)
	{
		if (enterReflection(player, npc, 119))
		{
			final var r = player.getReflection();
			if (isInReflection(r))
			{
				r.setParam("conquestBegun", false);
				r.setParam("conquestEnded", false);
				r.setParam("tumorRespawnTime", 180000);
				r.setParam("tumorCount", 4);
				r.setParam("isBossAttacked", false);
				r.setParam("TAG", -1);
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
			r.spawnByGroup("soi_hoe_attack_tumors");
			r.spawnByGroup("soi_hoe_attack_symbols");
			r.spawnByGroup("soi_hoe_attack_wards");
			r.spawnByGroup("soi_hoe_attack_mob_1");
			r.spawnByGroup("soi_hoe_attack_mob_2");
			r.spawnByGroup("soi_hoe_attack_mob_3");
			r.spawnByGroup("soi_hoe_attack_mob_4");
			r.spawnByGroup("soi_hoe_attack_mob_5");
			r.spawnByGroup("soi_hoe_attack_mob_6");
			r.spawnByGroup("soi_hoe_attack_mob_7");
			r.spawnByGroup("soi_hoe_attack_mob_8");
			for (final var n : r.getNpcs())
			{
				if (n.getId() == 18708)
				{
					n.setCurrentHp(n.getMaxHp() * .5);
				}
			}
			
			for (int zoneId = 20008; zoneId < 20029; zoneId++)
			{
				getActivatedZone(r, zoneId, true);
			}
			r.broadcastPacket(new ExShowScreenMessage(NpcStringId.YOU_CAN_HEAR_THE_UNDEAD_OF_EKIMUS_RUSHING_TOWARD_YOU_S1_S2_IT_HAS_NOW_BEGUN, 2, 1, 8000));
		}
	}

	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final var r = player.getReflection();
		if (isInReflection(r))
		{
			final var party = player.getParty();
			if (event.startsWith("warp"))
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
								if (member != null && member.isInsideRadius(npc, 500, true, false))
								{
									member.teleToLocation(loc, true, member.getReflection());
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
	public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon, Skill skill)
	{
		final var r = npc.getReflection();
		if (isInReflection(r))
		{
			final var isBossAttacked = r.getParams().getBool("isBossAttacked", false);
			if (!isBossAttacked)
			{
				r.setParam("isBossAttacked", true);
				final var reenter = Calendar.getInstance();
				reenter.add(Calendar.HOUR, 24);
				setReenterTime(r, reenter.getTimeInMillis(), r.isHwidCheck());
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon, skill);
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
				final var tumorCount = r.getParams().getInteger("tumorCount", 4) - 1;
				r.setParam("tumorCount", tumorCount);
				((MonsterInstance) npc).dropSingleItem(player, 13797, getRandom(2, 5));
				npc.deleteMe();
				final var deadTumor = spawnNpc(32535, loc, 0, r);
				final var deadTumors = r.getParams().getList("deadTumors", Npc.class);
				if (deadTumors != null)
				{
					deadTumors.add(deadTumor);
				}
				final var tag = r.getParams().getInteger("TAG", -1) + 1;
				r.setParam("TAG", tag);
				notifyTumorDeath(r, deadTumor);
				r.addTimer("TUMOR_REVIVAL", ThreadPoolManager.getInstance().schedule(() -> tumorRevival(r, deadTumor), r.getParams().getLong("tumorRespawnTime", 0)));
				r.addTimer("REGEN_COFFIN", ThreadPoolManager.getInstance().schedule(() -> regenerationCoffinSpawn(r, deadTumor), 20000));
			}
			else if (npc.getId() == 25634)
			{
				npc.broadcastPacketToOthers(new NpcSay(npc.getObjectId(), Say2.SHOUT, npc.getId(), NpcStringId.KEU_I_WILL_LEAVE_FOR_NOW_BUT_DONT_THINK_THIS_IS_OVER_THE_SEED_OF_INFINITY_CAN_NEVER_DIE));
				for (final var pl : r.getReflectionPlayers())
				{
					final var st = pl.getQuestState("_696_ConquertheHallofErosion");
					if (st != null && st.isCond(1))
					{
						st.set("cohemenes", "1");
					}
				}
				conquestConclusion(r, true);
				SoIManager.getInstance().notifyCohemenesKill();
			}

			if (npc.getId() == 18711)
			{
				final var tumorRespawnTime = r.getParams().getLong("tumorRespawnTime", 0) + 10000L;
				r.setParam("tumorRespawnTime", tumorRespawnTime);
			}
		}
		return super.onKill(npc, player, isSummon);
	}
	
	private void conquestConclusion(Reflection r, boolean win)
	{
		if (r != null)
		{
			r.removeTimer("TIMER");
			r.setParam("conquestEnded", true);
			r.despawnByGroup("soi_hoe_attack_symbols");
			r.despawnByGroup("soi_hoe_attack_wards");
			final var cohemenes = r.getParams().getObject("cohemenes", Npc.class);
			if (cohemenes != null && !cohemenes.isDead())
			{
				cohemenes.getMinionList().onMasterDelete();
				cohemenes.deleteMe();
			}
			finishInstance(r, 900000, false);
			
			if (win)
			{
				final var msg = new ExShowScreenMessage(NpcStringId.CONGRATULATIONS_YOU_HAVE_SUCCEEDED_AT_S1_S2_THE_INSTANCE_WILL_SHORTLY_EXPIRE, 2, 1, 8000);
				msg.addStringParameter("#" + NpcStringId.HALL_OF_EROSION.getId());
				msg.addStringParameter("#" + NpcStringId.ATTACK.getId());
				r.broadcastPacket(msg);
				handleReenterTime(r);
			}
			else
			{
				final var msg = new ExShowScreenMessage(NpcStringId.YOU_HAVE_FAILED_AT_S1_S2_THE_INSTANCE_WILL_SHORTLY_EXPIRE, 2, 1, 8000);
				msg.addStringParameter("#" + NpcStringId.HALL_OF_EROSION.getId());
				msg.addStringParameter("#" + NpcStringId.ATTACK.getId());
				r.broadcastPacket(msg);
			}
			
			for (final var npc : r.getNpcs())
			{
				if (npc.getId() == 18708 || npc.getId() == 32535)
				{
					npc.deleteMe();
				}
			}
		}
	}
	
	private void notifyTumorDeath(Reflection r, Npc tumor)
	{
		if (r != null)
		{
			final var tumorCount = r.getParams().getInteger("tumorCount", 4);
			final var cohem = r.getParams().getObject("cohemenes", Npc.class);
			if ((tumorCount == 0) && cohem == null)
			{
				final var msg = new ExShowScreenMessage(NpcStringId.ALL_THE_TUMORS_INSIDE_S1_HAVE_BEEN_DESTROYED_DRIVEN_INTO_A_CORNER_COHEMENES_APPEARS_CLOSE_BY, 2, 1, 8000);
				msg.addStringParameter("#" + NpcStringId.HALL_OF_EROSION.getId());
				r.broadcastPacket(msg);
				
				final int[] spawn = COHEMENES_SPAWN[getRandom(0, COHEMENES_SPAWN.length - 1)];
				final var cohemenes = addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, r);
				r.setParam("cohemenes", cohemenes);
				cohemenes.broadcastPacketToOthers(new NpcSay(cohemenes.getObjectId(), Say2.SHOUT, cohemenes.getId(), NpcStringId.CMON_CMON_SHOW_YOUR_FACE_YOU_LITTLE_RATS_LET_ME_SEE_WHAT_THE_DOOMED_WEAKLINGS_ARE_SCHEMING));
			}
			else
			{
				final var msg = new ExShowScreenMessage(NpcStringId.THE_TUMOR_INSIDE_S1_HAS_BEEN_DESTROYED_NIN_ORDER_TO_DRAW_OUT_THE_COWARDLY_COHEMENES_YOU_MUST_DESTROY_ALL_THE_TUMORS, 2, 1, 8000);
				msg.addStringParameter("#" + NpcStringId.HALL_OF_EROSION.getId());
				r.broadcastPacket(msg);
			}
			manageRegenZone(tumor, true);
		}
	}
	
	private void notifyTumorRevival(Reflection r, Npc tumor)
	{
		if (r != null)
		{
			final var tumorCount = r.getParams().getInteger("tumorCount", 4);
			final var cohemenes = r.getParams().getObject("cohemenes", Npc.class);
			if (tumorCount > 0 && cohemenes != null && !cohemenes.isDead())
			{
				cohemenes.getMinionList().onMasterDelete();
				cohemenes.deleteMe();
			}
			
			final var msg = new ExShowScreenMessage(NpcStringId.THE_TUMOR_INSIDE_S1_HAS_COMPLETELY_REVIVED_NTHE_RESTRENGTHENED_COHEMENES_HAS_FLED_DEEPER_INSIDE_THE_SEED, 2, 1, 8000);
			msg.addStringParameter("#" + NpcStringId.HALL_OF_EROSION.getId());
			r.broadcastPacket(msg);
			manageRegenZone(tumor, false);
		}
	}

	private void tumorRevival(Reflection r, Npc deadTumor)
	{
		if (r != null && deadTumor != null)
		{
			final var conquestEnded = r.getParams().getBool("conquestEnded", false);
			if (conquestEnded)
			{
				return;
			}
			final var tumor = spawnNpc(18708, deadTumor.getLocation(), 0, r);
			tumor.setCurrentHp(tumor.getMaxHp() * .25);
			final var tumorCount = r.getParams().getInteger("tumorCount", 4) + 1;
			r.setParam("tumorCount", tumorCount);
			notifyTumorRevival(r, deadTumor);
			final var deadTumors = r.getParams().getList("deadTumors", Npc.class);
			if (deadTumors != null)
			{
				deadTumors.remove(deadTumor);
			}
			final var tag = r.getParams().getInteger("TAG", 0) - 1;
			r.setParam("TAG", tag);
			deadTumor.deleteMe();
		}
	}

	private void regenerationCoffinSpawn(Reflection r, Npc deadTumor)
	{
		if (r != null && deadTumor != null)
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
	
	private void manageRegenZone(Npc npc, boolean doActivate)
	{
		final var r = npc.getReflection();
		if (isInReflection(r))
		{
			int zoneId = 0;
			if (ZoneManager.getInstance().isInsideZone(20000, npc))
			{
				zoneId = 20000;
			}
			else if (ZoneManager.getInstance().isInsideZone(20001, npc))
			{
				zoneId = 20001;
			}
			else if (ZoneManager.getInstance().isInsideZone(20002, npc))
			{
				zoneId = 20002;
			}
			else if (ZoneManager.getInstance().isInsideZone(20003, npc))
			{
				zoneId = 20003;
			}
			else if (ZoneManager.getInstance().isInsideZone(20004, npc))
			{
				zoneId = 20004;
			}
			else if (ZoneManager.getInstance().isInsideZone(20005, npc))
			{
				zoneId = 20005;
			}
			else if (ZoneManager.getInstance().isInsideZone(20006, npc))
			{
				zoneId = 20006;
			}
			else if (ZoneManager.getInstance().isInsideZone(20007, npc))
			{
				zoneId = 20007;
			}
			getActivatedZone(r, zoneId, doActivate);
		}
	}

	public static void main(String[] args)
	{
		new ErosionHallAttack();
	}
}
