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
import java.util.concurrent.CopyOnWriteArrayList;

import l2e.commons.geometry.Polygon;
import l2e.commons.util.Rnd;
import gameserver.ThreadPoolManager;
import gameserver.ai.model.CtrlIntention;
import gameserver.instancemanager.ZoneManager;
import gameserver.model.Location;
import gameserver.model.MountType;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.actor.instance.GrandBossInstance;
import gameserver.model.actor.instance.QuestGuardInstance;
import gameserver.model.actor.templates.reflection.ReflectionTemplate;
import gameserver.model.entity.Reflection;
import gameserver.model.holders.SkillHolder;
import gameserver.model.skills.Skill;
import gameserver.model.spawn.SpawnTerritory;
import gameserver.model.zone.ZoneType;
import gameserver.network.NpcStringId;
import gameserver.network.serverpackets.EventTrigger;
import gameserver.network.serverpackets.ExChangeClientEffectInfo;
import gameserver.network.serverpackets.ExSendUIEvent;
import gameserver.network.serverpackets.ExShowScreenMessage;

/**
 * Rework by LordWinter 07.02.2020
 */
public class FreyaBattleHard extends AbstractReflection
{
	private static final SkillHolder SUICIDE_BREATH = new SkillHolder(6300, 1);
	private static final SkillHolder ANTI_STRIDER = new SkillHolder(4258, 1);

	private final ZoneType _zone = ZoneManager.getInstance().getZoneById(20503);
	private static final Location MIDDLE_POINT = new Location(114730, -114805, -11200);
	private static SpawnTerritory CENTRAL_ROOM = new SpawnTerritory().add(new Polygon().add(114264, -113672).add(113640, -114344).add(113640, -115240).add(114264, -115912).add(115176, -115912).add(115800, -115272).add(115800, -114328).add(115192, -113672).setZmax(-11225).setZmin(-11225));

	private static final Location[] STATUES_STAGE_1_LOC =
	{
	        new Location(113845, -116091, -11168, 8264), new Location(113381, -115622, -11168, 8264), new Location(113380, -113978, -11168, -8224), new Location(113845, -113518, -11168, -8224), new Location(115591, -113516, -11168, -24504), new Location(116053, -113981, -11168, -24504), new Location(116061, -115611, -11168, 24804), new Location(115597, -116080, -11168, 24804),
	};

	private static final Location[] STATUES_STAGE_2_LOC =
	{
	        new Location(112942, -115480, -10960, 52), new Location(112940, -115146, -10960, 52), new Location(112945, -114453, -10960, 52), new Location(112945, -114123, -10960, 52), new Location(116497, -114117, -10960, 32724), new Location(116499, -114454, -10960, 32724), new Location(116501, -115145, -10960, 32724), new Location(116502, -115473, -10960, 32724),
	};

	private FreyaBattleHard()
	{
		super(144);

		addStartNpc(32762, 18851, 18850);
		addTalkId(32762, 32781, 18851);
		addAttackId(29177, 29180, 18854, 18856);
		addKillId(29177, 25700, 29180, 18856);
		addSpawnId(25700, 29180, 18856);
		addSpellFinishedId(18854);
	}

	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (event.equals("enterHard"))
		{
			enterInstance(player, npc);
		}
		else
		{
			final var r = npc.getReflection();
			if (isInReflection(r))
			{
				var controller = r.getParams().getObject("controller", Npc.class);
				switch (event)
				{
					case "openDoor" :
					{
						if (npc.isScriptValue(0))
						{
							npc.setScriptValue(1);
							r.openDoor(23140101);
							controller = addSpawn(18919, new Location(114394, -112383, -11200), false, 0, true, r);
							controller.setIsInvul(true);
							r.setParam("controller", controller);
							
							final var knightStatues = r.getParams().getList("knightStatues", Npc.class);
							if (knightStatues != null)
							{
								for (final var loc : STATUES_STAGE_1_LOC)
								{
									final var statue = addSpawn(18919, loc, false, 0, false, r);
									knightStatues.add(statue);
								}
							}
							startQuestTimer("STAGE_1_MOVIE", 60000, controller, null);
						}
						break;
					}
					case "portInside" :
					{
						teleportPlayer(player, new Location(114694, -113700, -11200), r);
						break;
					}
					case "STAGE_1_MOVIE" :
					{
						r.closeDoor(23140101);
						r.setStatus(1);
						manageMovie(r, 15);
						startQuestTimer("STAGE_1_START", 53500, controller, null);
						break;
					}
					case "STAGE_1_START" :
					{
						final var freya = (GrandBossInstance) addSpawn(29177, new Location(114720, -117085, -11088, 15956), false, 0, true, r);
						r.setParam("freya", freya);
						r.broadcastPacket(new ExShowScreenMessage(NpcStringId.BEGIN_STAGE_1, 2, 6000));
						startQuestTimer("STAGE_1_SPAWN", 2000, freya, null);
						break;
					}
					case "STAGE_1_SPAWN" :
					{
						r.setParam("canSpawnMobs", true);
						notifyEvent("START_SPAWN", controller, null);
						final var freya = r.getParams().getObject("freya", GrandBossInstance.class);
						if (freya != null && !freya.isInCombat())
						{
							r.broadcastPacket(new ExShowScreenMessage(NpcStringId.FREYA_HAS_STARTED_TO_MOVE, 2, 6000));
							freya.setRunning();
							freya.getAI().setIntention(CtrlIntention.MOVING, new Location(114730, -114805, -11200), 0);
						}
						r.addTimer("GUARD_SPAWN", ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> guardSpawn(1, r), 2000L, (r.getParams().getInteger("guardsInterval") * 1000L)));
						break;
					}
					case "STAGE_1_FINISH" :
					{
						r.setParam("canSpawnMobs", false);
						r.removeTimer("GUARD_SPAWN");
						manageDespawnMinions(r);
						manageMovie(r, 16);
						startQuestTimer("STAGE_1_PAUSE", 24100 - 1000, controller, null);
						break;
					}
					case "STAGE_1_PAUSE" :
					{
						final var freya = (GrandBossInstance) addSpawn(29178, new Location(114723, -117502, -10672, 15956), false, 0, true, r);
						r.setParam("freya", freya);
						freya.setIsInvul(true);
						freya.block();
						freya.disableCoreAI(true);
						r.broadcastPacket(new ExShowScreenMessage(NpcStringId.TIME_REMAINING_UNTIL_NEXT_BATTLE, 2, 6000));
						r.setStatus(2);
						startQuestTimer("STAGE_2_START", 60000, controller, null);
						break;
					}
					case "STAGE_2_START" :
					{
						r.setParam("canSpawnMobs", true);
						notifyEvent("START_SPAWN", controller, null);
						r.broadcastPacket(new ExShowScreenMessage(NpcStringId.BEGIN_STAGE_2, 2, 6000));
						r.addTimer("GUARD_SPAWN", ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> guardSpawn(2, r), 2000L, (r.getParams().getInteger("guardsInterval") * 1000L)));
						final int timeLimit = r.getParams().getInteger("glakiasTimeLimit");
						startQuestTimer("STAGE_2_FAILED", (timeLimit * 1000), controller, null);
						manageTimer(r, timeLimit, NpcStringId.BATTLE_END_LIMIT_TIME);
						controller.getVariables().set("TIMER_END", System.currentTimeMillis() + (timeLimit * 1000));
						break;
					}
					case "STAGE_2_MOVIE" :
					{
						manageBlockMinions(r);
						manageMovie(r, 23);
						startQuestTimer("STAGE_2_GLAKIAS", 7000, controller, null);
						break;
					}
					case "STAGE_2_GLAKIAS" :
					{
						manageUnblockMinions(r);
						final var knightStatues = r.getParams().getList("knightStatues", Npc.class);
						if (knightStatues != null)
						{
							for (final var loc : STATUES_STAGE_2_LOC)
							{
								final var statue = addSpawn(18919, loc, false, 0, false, r);
								knightStatues.add(statue);
								startQuestTimer("SPAWN_KNIGHT", 5000, statue, null);
							}
						}
						addSpawn(25700, new Location(114707, -114799, -11199, 15956), false, 0, true, r);
						startQuestTimer("SHOW_GLAKIAS_TIMER", 3000, controller, null);
						break;
					}
					case "STAGE_2_FAILED" :
					{
						if (r.getStatus() <= 3)
						{
							doCleanup(r);
							manageMovie(r, 22);
							startQuestTimer("STAGE_2_FAILED2", 22000, controller, null);
						}
						break;
					}
					case "STAGE_2_FAILED2" :
					{
						r.collapse();
						break;
					}
					case "STAGE_3_MOVIE" :
					{
						final var freya = r.getParams().getObject("freya", GrandBossInstance.class);
						if (freya != null)
						{
							freya.deleteMe();
						}
						manageMovie(r, 17);
						startQuestTimer("STAGE_3_START", 21500, controller, null);
						break;
					}
					case "STAGE_3_START" :
					{
						r.broadcastPacket(ExChangeClientEffectInfo.STATIC_FREYA_DESTROYED, new EventTrigger(23140202, true), new EventTrigger(23140204, true), new EventTrigger(23140206, true), new EventTrigger(23140208, true), new EventTrigger(23140212, true), new EventTrigger(23140214, true), new EventTrigger(23140216, true), new ExShowScreenMessage(NpcStringId.BEGIN_STAGE_3, 2, 6000));
						r.setParam("canSpawnMobs", true);
						r.addTimer("GUARD_SPAWN", ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> guardSpawn(3, r), 2000L, (r.getParams().getInteger("guardsInterval") * 1000L)));
						
						final var freya = (GrandBossInstance) addSpawn(29180, new Location(114720, -117085, -11088, 15956), false, 0, true, r);
						r.setParam("freya", freya);
						notifyEvent("START_SPAWN", controller, null);
						if (!freya.isInCombat())
						{
							r.broadcastPacket(new ExShowScreenMessage(NpcStringId.FREYA_HAS_STARTED_TO_MOVE, 2, 6000));
							freya.setRunning();
							freya.getAI().setIntention(CtrlIntention.MOVING, new Location(114730, -114805, -11200), 0);
						}
						break;
					}
					case "SPAWN_SUPPORT" :
					{
						manageUnblockMinions(r);
						for (final var pl : r.getReflectionPlayers())
						{
							pl.setIsInvul(false);
							pl.unblock();
						}
						final var freya = r.getParams().getObject("freya", GrandBossInstance.class);
						if (freya != null)
						{
							freya.setIsInvul(false);
							freya.unblock();
							freya.disableCoreAI(false);
						}
						r.setParam("canSpawnMobs", true);
						r.broadcastPacket(new ExShowScreenMessage(NpcStringId.BEGIN_STAGE_4, 2, 6000));
						final var jinia = (QuestGuardInstance) addSpawn(18850, new Location(114751, -114781, -11205), false, 0, true, r);
						r.setParam("jinia", jinia);
						jinia.setRunning();
						jinia.setIsInvul(true);
						jinia.setCanReturnToSpawnPoint(false);

						final var kegor = (QuestGuardInstance) addSpawn(18851, new Location(114659, -114796, -11205), false, 0, true, r);
						r.setParam("kegor", kegor);
						kegor.setRunning();
						kegor.setIsInvul(true);
						kegor.setCanReturnToSpawnPoint(false);
						startQuestTimer("GIVE_SUPPORT", 1000, controller, null);
						break;
					}
					case "GIVE_SUPPORT" :
					{
						final var isSupportActive = r.getParams().getBool("isSupportActive", false);
						if (isSupportActive)
						{
							final var jinia = r.getParams().getObject("jinia", QuestGuardInstance.class);
							if (jinia != null)
							{
								jinia.doCast(new SkillHolder(6288, 1).getSkill());
							}
							
							final var kegor = r.getParams().getObject("kegor", QuestGuardInstance.class);
							if (kegor != null)
							{
								kegor.doCast(new SkillHolder(6289, 1).getSkill());
							}
						}
						break;
					}
					case "START_SPAWN" :
					{
						final var knightStatues = r.getParams().getList("knightStatues", Npc.class);
						if (knightStatues != null)
						{
							for (final var statues : knightStatues)
							{
								notifyEvent("SPAWN_KNIGHT", statues, null);
							}
						}
						break;
					}
					case "SPAWN_KNIGHT" :
					{
						final var canSpawnMobs = r.getParams().getBool("canSpawnMobs", false);
						if (canSpawnMobs)
						{
							final var spawnedMobs = r.getParams().getList("spawnedMobs", Attackable.class);
							if (spawnedMobs != null)
							{
								final var loc = new Location(MIDDLE_POINT.getX() + getRandom(-1000, 1000), MIDDLE_POINT.getY() + getRandom(-1000, 1000), MIDDLE_POINT.getZ());
								final var knight = (Attackable) addSpawn(18856, npc.getLocation(), false, 0, false, r);
								knight.getSpawn().setLocation(loc);
								spawnedMobs.add(knight);
							}
						}
						break;
					}
					case "ELEMENTAL_KILLED" :
					{
						if (npc.getVariables().getInteger("SUICIDE_ON") == 1)
						{
							npc.setTarget(npc);
							npc.doCast(SUICIDE_BREATH.getSkill());
						}
						break;
					}
					case "FINISH_WORLD" :
					{
						final var freya = r.getParams().getObject("freya", GrandBossInstance.class);
						if (freya != null)
						{
							freya.deleteMe();
						}
						r.broadcastPacket(ExChangeClientEffectInfo.STATIC_FREYA_DEFAULT);
						break;
					}
					case "SHOW_GLAKIAS_TIMER" :
					{
						if (controller != null)
						{
							final int time = (int) ((controller.getVariables().getLong("TIMER_END", 0) - System.currentTimeMillis()) / 1000);
							manageTimer(r, time, NpcStringId.BATTLE_END_LIMIT_TIME);
						}
						break;
					}
				}
			}
		}
		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onSpawn(Npc npc)
	{
		((Attackable) npc).setOnKillDelay(0);
		return super.onSpawn(npc);
	}

	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon, Skill skill)
	{
		final var r = npc.getReflection();
		if (isInReflection(r))
		{
			switch (npc.getId())
			{
				case 29180 :
				{
					final var isSupportActive = r.getParams().getBool("isSupportActive", false);
					if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.2)) && !isSupportActive)
					{
						r.setParam("isSupportActive", true);
						r.setParam("canSpawnMobs", false);
						final var freya = r.getParams().getObject("freya", GrandBossInstance.class);
						if (freya != null)
						{
							freya.setIsInvul(true);
							freya.block();
							freya.disableCoreAI(true);
						}
						manageBlockMinions(r);
						for (final var pl : r.getReflectionPlayers())
						{
							if (pl.isDead())
							{
								continue;
							}
							pl.setIsInvul(true);
							pl.block();
							pl.abortAttack();
						}
						manageMovie(r, 18);
						final var controller = r.getParams().getObject("controller", Npc.class);
						if (controller != null)
						{
							startQuestTimer("SPAWN_SUPPORT", 27000, controller, null);
						}
					}

					if ((attacker.getMountType() == MountType.STRIDER) && (attacker.getFirstEffect(ANTI_STRIDER.getId()) == null) && !npc.isCastingNow())
					{
						if (!npc.isSkillDisabled(ANTI_STRIDER.getSkill()))
						{
							npc.setTarget(attacker);
							npc.doCast(ANTI_STRIDER.getSkill());
						}
					}
					break;
				}
				case 18854 :
				{
					if ((npc.getCurrentHp() < (npc.getMaxHp() / 20)) && (npc.getVariables().getInteger("SUICIDE_ON", 0) == 0))
					{
						npc.getVariables().set("SUICIDE_ON", 1);
						startQuestTimer("ELEMENTAL_KILLED", 1000, npc, null);
					}
					break;
				}
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon, skill);
	}

	@Override
	public String onSpellFinished(Npc npc, Player player, Skill skill)
	{
		final var r = npc.getReflection();
		if (isInReflection(r))
		{
			switch (npc.getId())
			{
				case 18854 :
				{
					if (skill == SUICIDE_BREATH.getSkill())
					{
						npc.doDie(npc);
					}
					break;
				}
			}
		}
		return super.onSpellFinished(npc, player, skill);
	}

	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final var r = npc.getReflection();
		if (isInReflection(r))
		{
			final var controller = r.getParams().getObject("controller", Npc.class);
			switch (npc.getId())
			{
				case 29177 :
				{
					final var freya = r.getParams().getObject("freya", GrandBossInstance.class);
					if (freya != null)
					{
						freya.deleteMe();
						r.setParam("freya", null);
					}
					if (controller != null)
					{
						notifyEvent("STAGE_1_FINISH", controller, null);
					}
					break;
				}
				case 25700 :
				{
					if (controller != null)
					{
						r.removeTimer("GUARD_SPAWN");
						manageDespawnMinions(r);
						manageTimer(r, 60, NpcStringId.TIME_REMAINING_UNTIL_NEXT_BATTLE);
						cancelQuestTimer("STAGE_2_FAILED", controller, null);
						startQuestTimer("STAGE_3_MOVIE", 60000, controller, null);
						r.setStatus(4);
					}
					break;
				}
				case 29180 :
				{
					r.removeTimer("GUARD_SPAWN");
					r.setParam("canSpawnMobs", false);
					r.setParam("isSupportActive", false);
					doCleanup(r);
					manageMovie(r, 19);
					finishInstance(r, true);
					if (controller != null)
					{
						cancelQuestTimer("GIVE_SUPPORT", controller, null);
						startQuestTimer("FINISH_WORLD", 300000, controller, null);
					}
					
					final var kegor = r.getParams().getObject("kegor", QuestGuardInstance.class);
					if (kegor != null)
					{
						kegor.deleteMe();
					}
					final var jinia = r.getParams().getObject("jinia", QuestGuardInstance.class);
					if (jinia != null)
					{
						jinia.deleteMe();
					}
					break;
				}
				case 18856 :
				{
					if (controller != null)
					{
						final var var = controller.getVariables();
						int knightCount = var.getInteger("KNIGHT_COUNT");
						
						if ((knightCount < 10) && (r.isStatus(2)))
						{
							knightCount++;
							var.set("KNIGHT_COUNT", knightCount);
							
							if (knightCount == 10)
							{
								notifyEvent("STAGE_2_MOVIE", controller, null);
								r.setStatus(3);
							}
						}
					}
					break;
				}
			}
		}
		return super.onKill(npc, killer, isSummon);
	}

	private synchronized void enterInstance(Player player, Npc npc)
	{
		if (enterReflection(player, npc, 144))
		{
			final var r = player.getReflection();
			if (isInReflection(r))
			{
				r.setParam("isSupportActive", false);
				final List<Attackable> spawnedMobs = new CopyOnWriteArrayList<>();
				r.setParam("spawnedMobs", spawnedMobs);
				final List<Npc> knightStatues = new ArrayList<>();
				r.setParam("knightStatues", knightStatues);
				r.broadcastPacket(ExChangeClientEffectInfo.STATIC_FREYA_DEFAULT, new EventTrigger(23140202, false), new EventTrigger(23140204, false), new EventTrigger(23140206, false), new EventTrigger(23140208, false), new EventTrigger(23140212, false), new EventTrigger(23140214, false), new EventTrigger(23140216, false));
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
			final var teleLoc = r.isStatus(4) ? new Location(114694, -113700, -11200) : template.getTeleportCoord();
			player.getAI().setIntention(CtrlIntention.IDLE);
			player.teleToLocation(teleLoc, true, r);
			if (player.hasSummon())
			{
				player.getSummon().getAI().setIntention(CtrlIntention.IDLE);
				player.getSummon().teleToLocation(teleLoc, true, r);
			}
		}
	}

	private void manageDespawnMinions(Reflection r)
	{
		if (r != null)
		{
			final var spawnedMobs = r.getParams().getList("spawnedMobs", Attackable.class);
			if (spawnedMobs != null)
			{
				spawnedMobs.stream().filter(n -> n != null && !n.isDead()).forEach(n ->
				{
					cancelQuestTimers(n);
					n.deleteMe();
				});
				spawnedMobs.clear();
			}
		}
	}

	private void manageBlockMinions(Reflection r)
	{
		if (r != null)
		{
			final var spawnedMobs = r.getParams().getList("spawnedMobs", Attackable.class);
			if (spawnedMobs != null)
			{
				spawnedMobs.stream().filter(n -> n != null && !n.isDead()).forEach(n ->
				{
					n.block();
					n.disableCoreAI(true);
					n.abortAttack();
				});
			}
		}
	}
	
	private void manageUnblockMinions(Reflection r)
	{
		if (r != null)
		{
			final var spawnedMobs = r.getParams().getList("spawnedMobs", Attackable.class);
			if (spawnedMobs != null)
			{
				spawnedMobs.stream().filter(n -> n != null && !n.isDead()).forEach(n ->
				{
					n.unblock();
					n.disableCoreAI(false);
				});
			}
		}
	}

	private void manageTimer(Reflection r, int time, NpcStringId npcStringId)
	{
		if (r != null)
		{
			for (final var player : r.getReflectionPlayers())
			{
				player.sendPacket(new ExSendUIEvent(player, false, false, time, 0, npcStringId));
			}
		}
	}

	private void manageMovie(Reflection r, int movie)
	{
		if (r != null)
		{
			for (final var player : r.getReflectionPlayers())
			{
				player.showQuestMovie(movie);
			}
		}
	}

	private void guardSpawn(int mode, Reflection r)
	{
		if (r != null)
		{
			boolean canSpawn = false;
			final var players = r.getReflectionPlayers();
			if (!players.isEmpty())
			{
				for (final var activeChar : players)
				{
					if (_zone != null && _zone.isInsideZone(activeChar))
					{
						continue;
					}
					
					if (!activeChar.isDead())
					{
						canSpawn = true;
						break;
					}
				}
			}
			final var canSpawnMobs = r.getParams().getBool("canSpawnMobs", false);
			if (!canSpawnMobs)
			{
				canSpawn = false;
			}
			int knightsMin = 0, knightsMax = 0, breathMin = 0, breathMax = 0;
			if (canSpawn)
			{
				switch (mode)
					{
						case 1 :
							final String[] stage1 = r.getParams().getString("guardStage1").split(";");
							knightsMin = Integer.parseInt(stage1[0]);
							knightsMax = Integer.parseInt(stage1[1]);
							breathMin = Integer.parseInt(stage1[2]);
							breathMax = Integer.parseInt(stage1[3]);
							break;
						case 2 :
							final String[] stage2 = r.getParams().getString("guardStage2").split(";");
							knightsMin = Integer.parseInt(stage2[0]);
							knightsMax = Integer.parseInt(stage2[1]);
							breathMin = Integer.parseInt(stage2[2]);
							breathMax = Integer.parseInt(stage2[3]);
							break;
						case 3 :
							final String[] stage3 = r.getParams().getString("guardStage3").split(";");
							knightsMin = Integer.parseInt(stage3[0]);
							knightsMax = Integer.parseInt(stage3[1]);
							breathMin = Integer.parseInt(stage3[2]);
							breathMax = Integer.parseInt(stage3[3]);
							break;
					}
					
					final var spawnedMobs = r.getParams().getList("spawnedMobs", Attackable.class);
					
					for (int i = 0; i < Rnd.get(knightsMin, knightsMax); i++)
					{
						final var knight = (Attackable) addSpawn(18856, SpawnTerritory.getRandomLoc(CENTRAL_ROOM, false), false, 0, false, r);
						if (spawnedMobs != null)
						{
							spawnedMobs.add(knight);
						}
					}
					for (int i = 0; i < Rnd.get(breathMin, breathMax); i++)
					{
						final var breath = (Attackable) addSpawn(18854, SpawnTerritory.getRandomLoc(CENTRAL_ROOM, false), false, 0, false, r);
						if (spawnedMobs != null)
						{
							spawnedMobs.add(breath);
						}
					}
					if (Rnd.chance(60))
					{
						for (int i = 0; i < Rnd.get(1, 3); i++)
						{
							final var glacier = (Attackable) addSpawn(18853, SpawnTerritory.getRandomLoc(CENTRAL_ROOM, false), false, 0, false, r);
							if (spawnedMobs != null)
							{
								spawnedMobs.add(glacier);
							}
						}
					}
				}
		}
	}

	private void doCleanup(Reflection r)
	{
		if (r != null)
		{
			r.removeTimer("GUARD_SPAWN");
			manageDespawnMinions(r);
		}
	}

	public static void main(String[] args)
	{
		new FreyaBattleHard();
	}
}