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

import org.apache.commons.lang3.ArrayUtils;

import l2e.commons.util.Rnd;
import gameserver.ThreadPoolManager;
import gameserver.ai.model.CtrlEvent;
import gameserver.ai.model.CtrlIntention;
import gameserver.data.parser.SkillsParser;
import gameserver.instancemanager.SoDManager;
import gameserver.model.Location;
import gameserver.model.World;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.actor.instance.MonsterInstance;
import gameserver.model.actor.instance.TrapInstance;
import gameserver.model.actor.templates.npc.MinionData;
import gameserver.model.actor.templates.npc.MinionTemplate;
import gameserver.model.actor.templates.reflection.ReflectionTemplate;
import gameserver.model.entity.Reflection;
import gameserver.model.skills.Skill;
import gameserver.network.NpcStringId;
import gameserver.network.serverpackets.ExShowScreenMessage;

/**
 * Rework by LordWinter 07.02.2020
 */
public class Stage1 extends AbstractReflection
{
	private static final int[] TRAP_18771_NPCS =
	{
	        22541, 22544, 22541, 22544
	};
	
	private static final int[] TRAP_OTHER_NPCS =
	{
	        22546, 22546, 22538, 22537
	};
	
	private static final int[] TIAT_MINION_IDS =
	{
	        29162, 22538, 22540, 22547, 22542, 22548
	};
	
	private static final int[] ATTACKABLE_DOORS =
	{
	        12240005, 12240006, 12240007, 12240008, 12240009, 12240010, 12240013, 12240014, 12240015, 12240016, 12240017, 12240018, 12240021, 12240022, 12240023, 12240024, 12240025, 12240026, 12240028, 12240029, 12240030
	};
	
	private static final int[] ENTRANCE_ROOM_DOORS =
	{
	        12240001, 12240002
	};
	
	private static final int[] SQUARE_DOORS =
	{
	        12240003, 12240004, 12240011, 12240012, 12240019, 12240020
	};
	
	private static final NpcStringId[] TIAT_TEXT =
	{
	        NpcStringId.YOULL_REGRET_CHALLENGING_ME, NpcStringId.HA_HA_YES_DIE_SLOWLY_WRITHING_IN_PAIN_AND_AGONY
	};
	
	private static final int[] npcsStage1 =
	{
	        22550, 22551, 22552, 22596
	};
	
	private static final int[] npcsStage2 =
	{
	        22540, 22541, 22542, 22543, 22544, 22547
	};
	
	private static final int[] npcsStage5 =
	{
	        18777
	};
	
	private static final int[] npcsStage6 =
	{
	        18778
	};
	
	public Stage1()
	{
		super(110);
		
		addStartNpc(32526, 32601);
		addTalkId(32526, 32601);
		
		addAttackId(18776, 29163);
		addAggroRangeEnterId(29169);
		
		addSpawnId(18776, 18777, 18778, 22593, 22596, 22597, 29162, 29163, 29169);
		for (int i = 22536; i <= 22552; i++)
		{
			addSpawnId(i);
		}
		for (int i = 18720; i <= 18774; i++)
		{
			addSpawnId(i);
		}
		for (int i = 18771; i <= 18774; i++)
		{
			addTrapActionId(i);
		}
		addKillId(18776, 18777, 18778, 22540, 22541, 22542, 22543, 22544, 22547, 22550, 22551, 22552, 22596, 29162, 29163);
	}
	
	private synchronized void enterInstance(Player player, Npc npc)
	{
		if (enterReflection(player, npc, 110))
		{
			final var r = player.getReflection();
			if (isInReflection(r))
			{
				if (r.getParams().getBool("onlyRaidFight", false))
				{
					r.setStatus(7);
					startQuestTimer("SpawnTiad", 5000, null, player);
				}
				else
				{
					spawnState(r);
					for (final var door : r.getDoors())
					{
						if (ArrayUtils.contains(ATTACKABLE_DOORS, door.getDoorId()))
						{
							door.setIsAttackableDoor(true);
						}
					}
				}
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
			final var teleLoc = r.getParams().getBool("onlyRaidFight", false) ? new Location(-251440, 212068, -11984) : template.getTeleportCoord();
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
	
	private boolean spawnState(Reflection r)
	{
		if (r != null)
		{
			try
			{
				final var lockSpawn = r.getParams().getBool("lockSpawn", false);
				if (lockSpawn)
				{
					return false;
				}
				r.setParam("lockSpawn", true);
				switch (r.getStatus())
				{
					case 0 :
						r.spawnByGroup("seed_of_destruction_1");
						break;
					case 1 :
						r.broadcastPacket(new ExShowScreenMessage(NpcStringId.THE_ENEMIES_HAVE_ATTACKED_EVERYONE_COME_OUT_AND_FIGHT_URGH, 2, 5000));
						for (final int i : ENTRANCE_ROOM_DOORS)
						{
							r.openDoor(i);
						}
						r.spawnByGroup("seed_of_destruction_2");
						break;
					case 2 :
					case 3 :
						return true;
					case 4 :
						r.broadcastPacket(new ExShowScreenMessage(NpcStringId.OBELISK_HAS_COLLAPSED_DONT_LET_THE_ENEMIES_JUMP_AROUND_WILDLY_ANYMORE, 2, 5000));
						for (final int i : SQUARE_DOORS)
						{
							r.openDoor(i);
						}
						r.spawnByGroup("seed_of_destruction_5");
						break;
					case 5 :
						r.openDoor(12240027);
						r.spawnByGroup("seed_of_destruction_4");
						r.spawnByGroup("seed_of_destruction_6");
						break;
					case 6 :
						r.openDoor(12240031);
						break;
					case 7 :
						r.spawnByGroup("seed_of_destruction_8");
						break;
					case 8 :
						r.broadcastPacket(new ExShowScreenMessage(NpcStringId.COME_OUT_WARRIORS_PROTECT_SEED_OF_DESTRUCTION, 2, 5000));
						r.spawnByGroup("seed_of_destruction_9");
						break;
					case 9 :
						break;
				}
				r.incStatus();
				return true;
			}
			finally
			{
				r.setParam("lockSpawn", false);
			}
		}
		return false;
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		final var r = npc.getReflection();
		if (isInReflection(r))
		{
			npc.setIsNoRndWalk(true);
			if (npc instanceof Attackable)
			{
				((Attackable) npc).setSeeThroughSilentMove(true);
			}
			
			switch (npc.getId())
			{
				case 29162 :
					final var players = r.getReflectionPlayers();
					if (players.size() > 0)
					{
						npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, players.get(Rnd.get(players.size())), 30000);
					}
					break;
				case 29163 :
					npc.setIsImmobilized(true);
					final var tiat = (MonsterInstance) npc;
					r.setParam("tiat", tiat);
					tiat.getMinionList().addMinion(new MinionData(new MinionTemplate(29162, 5)), false);
					break;
				case 29169 :
					startQuestTimer("DoorCheck", 10000, npc, null);
					break;
				case 18776 :
				case 18777 :
				case 18778 :
					npc.disableCoreAI(true);
					break;
			}
		}
		return super.onSpawn(npc);
	}
	
	@Override
	public String onAggroRangeEnter(Npc npc, Player player, boolean isSummon)
	{
		if ((isSummon == false) && (player != null))
		{
			final var r = player.getReflection();
			if (isInReflection(r))
			{
				if (r.isStatus(7))
				{
					if (spawnState(r))
					{
						for (final var pl : r.getReflectionPlayers())
						{
							pl.showQuestMovie(5);
						}
						npc.deleteMe();
					}
				}
			}
		}
		return super.onAggroRangeEnter(npc, player, isSummon);
	}
	
	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon, Skill skill)
	{
		final var r = npc.getReflection();
		if (isInReflection(r))
		{
			if (r.isStatus(2) && (npc.getId() == 18776))
			{
				r.setStatus(4);
				r.spawnByGroup("seed_of_destruction_4");
			}
			else if (r.isStatus(3) && (npc.getId() == 18776))
			{
				r.setStatus(4);
				r.spawnByGroup("seed_of_destruction_3");
			}
			else if ((r.getStatus() <= 8) && (npc.getId() == 29163))
			{
				if (npc.getCurrentHp() < (npc.getMaxHp() / 2))
				{
					if (spawnState(r))
					{
						if (npc.isImmobilized())
						{
							npc.setIsImmobilized(false);
						}
						npc.setTarget(npc);
						npc.setIsInvul(true);
						npc.doCast(SkillsParser.getInstance().getInfo(5974, 1));
						handleReenterTime(r);
						ThreadPoolManager.getInstance().schedule(new Runnable()
						{
							@Override
							public void run()
							{
								npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp());
								npc.setIsInvul(false);
							}
						}, SkillsParser.getInstance().getInfo(5974, 1).getHitTime());
					}
				}
				else
				{
					final var lastFactionNotifyTime = r.getParams().getLong("lastFactionNotifyTime", 0);
					if (lastFactionNotifyTime < System.currentTimeMillis())
					{
						r.setParam("lastFactionNotifyTime", (System.currentTimeMillis() + 10000L));
						for (final var mob : World.getAroundNpc(npc, (int) (4000 + npc.getColRadius()), 200))
						{
							if (ArrayUtils.contains(TIAT_MINION_IDS, mob.getId()))
							{
								mob.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 30000);
							}
						}
						
						if (Rnd.chance(5))
						{
							r.broadcastPacket(new ExShowScreenMessage(TIAT_TEXT[Rnd.get(TIAT_TEXT.length)], 2, 5000));
						}
					}
				}
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon, skill);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
			if (event.equalsIgnoreCase("DoorCheck"))
			{
				final var r = npc.getReflection();
				if (isInReflection(r))
				{
					final var tmp = r.getDoor(12240030);
					if (tmp.getCurrentHp() < tmp.getMaxHp())
					{
						r.spawnByGroup("seed_of_destruction_7");
						r.broadcastPacket(new ExShowScreenMessage(NpcStringId.ENEMIES_ARE_TRYING_TO_DESTROY_THE_FORTRESS_EVERYONE_DEFEND_THE_FORTRESS, 2, 5000));
					}
					else
					{
						startQuestTimer("DoorCheck", 10000, npc, null);
					}
				}
			}
			else if (event.equalsIgnoreCase("SpawnTiad"))
			{
				final var r = player.getReflection();
				{
					if (isInReflection(r))
					{
						if (r.isStatus(7))
						{
							if (spawnState(r))
							{
								for (final var pl : r.getReflectionPlayers())
								{
									pl.showQuestMovie(5);
								}
							}
						}
					}
			}
		}
		return "";
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isSummon)
	{
		final var r = npc.getReflection();
		if (isInReflection(r))
		{
			if (r.isStatus(1) && ArrayUtils.contains(npcsStage1, npc.getId()))
			{
				if (!r.hasAliveNpcs(npcsStage1))
				{
					spawnState(r);
				}
			}
			else if (r.isStatus(2) && ArrayUtils.contains(npcsStage2, npc.getId()))
			{
				if (!r.hasAliveNpcs(npcsStage2))
				{
					r.incStatus();
				}
			}
			else if ((r.isStatus(4)) && (npc.getId() == 18776))
			{
				spawnState(r);
			}
			else if ((r.isStatus(5)) && ArrayUtils.contains(npcsStage5, npc.getId()))
			{
				if (!r.hasAliveNpcs(npcsStage5))
				{
					spawnState(r);
				}
			}
			else if ((r.isStatus(6)) && ArrayUtils.contains(npcsStage6, npc.getId()))
			{
				if (!r.hasAliveNpcs(npcsStage6))
				{
					spawnState(r);
				}
			}
			else if (r.getStatus() >= 7)
			{
				if (npc.getId() == 29163)
				{
					final var tiat = r.getParams().getObject("tiat", MonsterInstance.class);
					if (tiat != null)
					{
						final var ml = tiat.getMinionList();
						if (ml != null)
						{
							ml.onMasterDelete();
						}
					}
					r.setParam("tiat", null);
					r.incStatus();
					for (final var n : r.getNpcs())
					{
						n.deleteMe();
					}
					for (final var pl : r.getReflectionPlayers())
					{
						pl.showQuestMovie(6);
					}
					SoDManager.getInstance().addTiatKill();
					finishInstance(r, 900000, false);
				}
				else if (npc.getId() == 29162)
				{
					final var tiat = r.getParams().getObject("tiat", MonsterInstance.class);
					if (tiat != null && !tiat.isDead())
					{
						tiat.getMinionList().addMinion(new MinionData(new MinionTemplate(29162, 1)), true);
					}
				}
			}
		}
		return super.onKill(npc, player, isSummon);
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		final int npcId = npc.getId();
		var st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}
		if (npcId == 32526)
		{
			if (SoDManager.getInstance().isAttackStage())
			{
				enterInstance(player, npc);
			}
			else if (!SoDManager.getInstance().isAttackStage())
			{
				SoDManager.getInstance().teleportIntoSeed(player);
			}
		}
		else if (npcId == 32601)
		{
			teleportPlayer(player, new Location(-245802, 220528, -12104), player.getReflection(), false);
		}
		return "";
	}
	
	@Override
	public String onTrapAction(TrapInstance trap, Creature trigger, TrapAction action)
	{
		final var r = trap.getReflection();
		if (isInReflection(r))
		{
			switch (action)
			{
				case TRAP_TRIGGERED :
					if (trap.getId() == 18771)
					{
						for (final int npcId : TRAP_18771_NPCS)
						{
							addSpawn(npcId, trap.getX(), trap.getY(), trap.getZ(), trap.getHeading(), true, 0, true, r);
						}
					}
					else
					{
						for (final int npcId : TRAP_OTHER_NPCS)
						{
							addSpawn(npcId, trap.getX(), trap.getY(), trap.getZ(), trap.getHeading(), true, 0, true, r);
						}
					}
					break;
			}
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new Stage1();
	}
}