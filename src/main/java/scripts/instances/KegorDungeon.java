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

import gameserver.ai.model.CtrlIntention;
import gameserver.instancemanager.ReflectionManager;
import gameserver.model.Location;
import gameserver.model.World;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.actor.templates.reflection.ReflectionTemplate;
import gameserver.model.entity.Reflection;
import gameserver.model.holders.SkillHolder;
import gameserver.network.NpcStringId;
import gameserver.network.clientpackets.Say2;

/**
 * Rework by LordWinter 02.10.2020
 */
public final class KegorDungeon extends AbstractReflection
{
	private static final Location[] MOB_SPAWNS = new Location[]
	{
	        new Location(185216, -184112, -3308, -15396), new Location(185456, -184240, -3308, -19668), new Location(185712, -184384, -3308, -26696), new Location(185920, -184544, -3308, -32544), new Location(185664, -184720, -3308, 27892)
	};

	private KegorDungeon()
	{
		super(138);

		addFirstTalkId(18846);
		addKillId(18846, 22766);
		addStartNpc(32654, 32653);
		addTalkId(32654, 32653, 18846);
	}
	
	private final synchronized void enterInstance(Player player, Npc npc)
	{
		enterReflection(player, npc, 138);
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

	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final var r = npc.getReflection();
		if (isInReflection(r))
		{
			switch (event)
			{
				case "BUFF" :
				{
					if ((player != null && npc != null) && !player.isDead() && !npc.isDead() && npc.isInsideRadius(player, 1000, true, false) && npc.isScriptValue(1))
					{
						npc.setTarget(player);
						npc.doCast(new SkillHolder(6286, 1).getSkill());
					}
					startQuestTimer("BUFF", 30000, npc, player);
					break;
				}
				case "TIMER" :
				{
					for (final var loc : MOB_SPAWNS)
					{
						final var spawnedMob = (Attackable) addSpawn(22766, loc, false, 0, false, r);
						spawnedMob.setScriptValue(1);
						spawnedMob.setIsRunning(true);
						spawnedMob.getAI().setIntention(CtrlIntention.ATTACK, npc);
						spawnedMob.addDamageHate(npc, 0, 999999);
					}
					break;
				}
				case "FINISH" :
				{
					for (final var kegor : World.getAroundNpc(player))
					{
						if (kegor.getId() == 18846)
						{
							kegor.setScriptValue(2);
							kegor.setWalking();
							kegor.setTarget(player);
							kegor.getAI().setIntention(CtrlIntention.FOLLOW, player);
							broadcastNpcSay(kegor, Say2.NPC_ALL, NpcStringId.I_CAN_FINALLY_TAKE_A_BREATHER_BY_THE_WAY_WHO_ARE_YOU_HMM_I_THINK_I_KNOW_WHO_SENT_YOU);
							break;
						}
					}
					r.setDuration(3000);
					break;
				}
			}
		}
		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		final var r = npc.getReflection();
		if (isInReflection(r))
		{
			final var qs = player.getQuestState("_10284_AcquisitionOfDivineSword");
			if ((qs != null))
			{
				if (qs.isMemoState(2))
				{
					return npc.isScriptValue(0) ? "18846.htm" : "18846-01.htm";
				}
				else if (qs.isMemoState(3))
				{
					r.removeAllowed(player);
					player.teleToLocation(new Location(178823, -184303, -347, 0), 0, true, ReflectionManager.DEFAULT);
					qs.rewardItems(57, 296425);
					qs.addExpAndSp(921805, 82230);
					qs.exitQuest(false, true);
					return "18846-03.htm";
				}
			}
		}
		return super.onFirstTalk(npc, player);
	}

	@Override
	public String onKill(Npc npc, Player player, boolean isSummon)
	{
		final var r = npc.getReflection();
		if (isInReflection(r))
		{
			if (npc.getId() == 18846)
			{
				r.setParam("MOB_COUNT", -5);
				broadcastNpcSay(npc, Say2.NPC_ALL, NpcStringId.HOW_COULD_I_FALL_IN_A_PLACE_LIKE_THIS);
				r.setDuration(1000);
			}
			else if (npc.isScriptValue(1))
			{
				final int count = r.getParams().getInteger("MOB_COUNT", 0) + 1;
				r.setParam("MOB_COUNT", count);
				if (count >= 5)
				{
					final var qs = player.getQuestState("_10284_AcquisitionOfDivineSword");
					if ((qs != null) && qs.isMemoState(2))
					{
						qs.setMemoState(3);
						qs.setCond(6, true);
						startQuestTimer("FINISH", 3000, npc, player);
					}
				}
			}
		}
		return super.onKill(npc, player, isSummon);
	}

	@Override
	public String onTalk(Npc npc, Player talker)
	{
		switch (npc.getId())
		{
			case 32654 :
			case 32653 :
			{
				final var qs = talker.getQuestState("_10284_AcquisitionOfDivineSword");
				if ((qs != null) && qs.isMemoState(2))
				{
					if (!qs.hasQuestItems(15514))
					{
						qs.giveItems(15514, 1);
					}
					qs.setCond(4, true);
					enterInstance(talker, npc);
				}
				break;
			}
			case 18846 :
			{
				final var qs = talker.getQuestState("_10284_AcquisitionOfDivineSword");
				if ((qs != null) && qs.isMemoState(2) && qs.hasQuestItems(15514) && npc.isScriptValue(0))
				{
					qs.takeItems(15514, -1);
					qs.setCond(5, true);
					npc.setScriptValue(1);
					startQuestTimer("TIMER", 3000, npc, talker);
					startQuestTimer("BUFF", 3500, npc, talker);
					return "18846-02.htm";
				}
				break;
			}
		}
		return super.onTalk(npc, talker);
	}

	public static void main(String[] args)
	{
		new KegorDungeon();
	}
}
