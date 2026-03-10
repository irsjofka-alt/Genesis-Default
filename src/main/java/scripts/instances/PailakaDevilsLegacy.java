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

import l2e.commons.util.Rnd;
import gameserver.ai.model.CtrlIntention;
import gameserver.data.parser.SkillsParser;
import gameserver.model.Location;
import gameserver.model.World;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.actor.instance.player.impl.TeleportTask;
import gameserver.model.actor.templates.reflection.ReflectionTemplate;
import gameserver.model.entity.Reflection;
import gameserver.model.quest.State;
import gameserver.model.zone.ZoneType;
import gameserver.network.serverpackets.MagicSkillUse;

/**
 * Rework by LordWinter 13.12.2020
 */
public class PailakaDevilsLegacy extends AbstractReflection
{
	public PailakaDevilsLegacy()
	{
		super(44);

		addStartNpc(32498);
		addTalkId(32498);
		
		addSpawnId(18634);
		addAttackId(18622, 18633, 32495);
		addKillId(18633, 18634, 32495, 18622);

		addEnterZoneId(20109);
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
	
	private final synchronized void enterInstance(Player player, Npc npc)
	{
		if (enterReflection(player, npc, 44))
		{
			final var r = player.getReflection();
			if (isInReflection(r))
			{
				final var lematan = addSpawn(18633, 88108, -209252, -3744, 64255, false, 0, false, r);
				r.setParam("lematan", lematan);
			}
		}
	}

	@Override
	public final String onAdvEvent(String event, Npc npc, Player player)
	{
		if (event.equalsIgnoreCase("enter"))
		{
			enterInstance(player, npc);
			final var qs = player.getQuestState("_129_PailakaDevilsLegacy");
			if ((qs != null) && qs.isCond(1))
			{
				qs.setCond(2, true);
			}
			return null;
		}
		else
		{
			final var r = npc.getReflection();
			if (isInReflection(r))
			{
				final var lematan = r.getParams().getObject("lematan", Npc.class);
				switch (event)
				{
					case "follower_cast" :
						if (!npc.isCastingNow() && !npc.isDead() && lematan != null && !lematan.isDead())
						{
							npc.setTarget(lematan);
							npc.doCast(SkillsParser.getInstance().getInfo(5712, 1));
						}
						startQuestTimer("follower_cast", 2000 + getRandom(100, 1000), npc, null);
						break;
					case "first_animation" :
						if (!npc.isCastingNow() && !npc.isDead() && lematan != null && !lematan.isDead())
						{
							npc.broadcastPacketToOthers(2000, new MagicSkillUse(npc, npc, 5756, 1, 2500, 0));
						}
						break;
					case "respawn_minions" :
						if (lematan != null && !lematan.isDead())
						{
							final int radius = 260;
							final int rnd = Rnd.get(6);
							final int x = (int) (radius * Math.cos(rnd * 0.918));
							final int y = (int) (radius * Math.sin(rnd * 0.918));
							addSpawn(18634, lematan.getX() + x, lematan.getY() + y, lematan.getZ(), 0, false, 0, true, r);
						}
						break;
					case "keg_trigger" :
						onAttack(npc, player, 600, false);
						break;
					case "lematan_teleport" :
						if (npc != null && !npc.isDead())
						{
							npc.teleToLocation(84982, -208690, -3337, true, r);
							npc.getSpawn().setX(84982);
							npc.getSpawn().setY(-208690);
							npc.getSpawn().setZ(-3337);
							((Attackable) npc).getAggroList().reduceHate(player, 9999, false);
							for (int i = 0; i < 6; i++)
							{
								final int radius = 260;
								final int x = (int) (radius * Math.cos(i * 0.918));
								final int y = (int) (radius * Math.sin(i * 0.918));
								addSpawn(18634, 84982 + x, -208690 + y, -3337, 0, false, 0, true, r);
							}
						}
						break;
				}
			}
		}
		return null;
	}

	@Override
	public final String onAttack(Npc npc, Player attacker, int damage, boolean isSummon)
	{
		final var r = npc.getReflection();
		if (isInReflection(r))
		{
			switch (npc.getId())
			{
				case 18622 :
					if ((damage > 0) && npc.isScriptValue(0))
					{
						npc.setScriptValue(1);
						npc.doCast(SkillsParser.getInstance().getInfo(5714, 1));
						for (final var target : World.getAroundAttackable(npc, 900, 200))
						{
							target.reduceCurrentHp(500 + getRandom(0, 200), npc, SkillsParser.getInstance().getInfo(5714, 1));
							if (target.getId() == 18622)
							{
								startQuestTimer("keg_trigger", 500, target, attacker);
							}
							else
							{
								target.setIsRunning(true);
								target.addDamageHate(isSummon ? attacker.getSummon() : attacker, 0, 999);
								target.getAI().setIntention(CtrlIntention.ATTACK, isSummon ? attacker.getSummon() : attacker);
							}
						}
						
						if (!npc.isDead())
						{
							npc.doDie(attacker);
						}
					}
					break;
				case 18633 :
					if (npc.isScriptValue(0) && (npc.getCurrentHp() < (npc.getMaxHp() / 2)))
					{
						npc.setScriptValue(1);
						((Attackable) npc).getAggroList().reduceHate(attacker, 9999, false);
						npc.abortAttack();
						npc.abortCast();
						npc.broadcastPacketToOthers(2000, new MagicSkillUse(npc, 2100, 1, 1000, 0));
						startQuestTimer("lematan_finish_teleport", 1500, npc, attacker);
					}
					break;
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}

	@Override
	public final String onKill(Npc npc, Player player, boolean isSummon)
	{
		final var st = player.getQuestState("_129_PailakaDevilsLegacy");
		if ((st == null) || (st.getState() != State.STARTED))
		{
			return null;
		}
		
		final var r = npc.getReflection();
		if (isInReflection(r))
		{
			switch (npc.getId())
			{
				case 18633 :
					r.getAliveNpcs(18634).forEach(Npc::deleteMe);
					st.setCond(4, true);
					addSpawn(32511, 84983, -208736, -3336, 49915, false, 0, false, r);
					break;
				case 18622 :
				case 32495 :
				case 18634 :
					final var lematan = r.getParams().getObject("lematan", Npc.class);
					if (lematan != null && !lematan.isDead() && lematan.isScriptValue(1))
					{
						startQuestTimer("respawn_minions", 10000, npc, null);
					}
					break;
			}
		}
		return super.onKill(npc, player, isSummon);
	}

	@Override
	public final String onSpawn(Npc npc)
	{
		final var r = npc.getReflection();
		if (isInReflection(r))
		{
			startQuestTimer("first_animation", 500, npc, null);
			startQuestTimer("follower_cast", 1000 + getRandom(100, 1000), npc, null);
			npc.disableCoreAI(true);
		}
		return super.onSpawn(npc);
	}

	@Override
	public String onEnterZone(Creature character, ZoneType zone)
	{
		if ((character.isPlayer()) && !character.isDead() && !character.isTeleporting() && ((Player) character).isOnline())
		{
			final var r = character.getReflection();
			if (isInReflection(r))
			{
				character.getActingPlayer().getPersonalTasks().addTask(new TeleportTask(1000, new Location(76428, -219038, -3752), r));
			}
		}
		return super.onEnterZone(character, zone);
	}

	public static void main(String[] args)
	{
		new PailakaDevilsLegacy();
	}
}
