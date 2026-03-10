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

import gameserver.ai.model.CtrlIntention;
import gameserver.data.parser.SkillsParser;
import gameserver.instancemanager.ReflectionManager;
import gameserver.model.Location;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.actor.instance.MonsterInstance;
import gameserver.model.actor.templates.reflection.ReflectionTemplate;
import gameserver.model.entity.Reflection;
import gameserver.model.quest.QuestState;
import gameserver.model.quest.State;
import gameserver.model.skills.Skill;
import gameserver.model.zone.ZoneType;
import gameserver.utils.Util;

/**
 * Rework by LordWinter 02.10.2020
 */
public class NornilsGarden extends AbstractReflection
{
	private static final int[] _final_gates =
	{
	        32260, 32261, 32262
	};
	
	private static final int[][] _auto_gates =
	{
	        {
	                20110, 16200001
			},
			{
			        20111, 16200004
			},
			{
			        20112, 16200013
			}
	};
	
	private static final Skill skill1 = SkillsParser.getInstance().getInfo(4322, 1);
	private static final Skill skill2 = SkillsParser.getInstance().getInfo(4327, 1);
	private static final Skill skill3 = SkillsParser.getInstance().getInfo(4329, 1);
	private static final Skill skill4 = SkillsParser.getInstance().getInfo(4324, 1);
	
	private static final int[][] _gatekeepers =
	{
	        {
	                18352, 9703, 0
			},
			{
			        18353, 9704, 0
			},
			{
			        18354, 9705, 0
			},
			{
			        18355, 9706, 0
			},
			{
			        18356, 9707, 16200024
			},
			{
			        18357, 9708, 16200025
			},
			{
			        18358, 9713, 0
			},
			{
			        18359, 9709, 16200023
			},
			{
			        18360, 9710, 0
			},
			{
			        18361, 9711, 0
			},
			{
			        25528, 9712, 0
			}
	};
	
	private static final int[][] _group_1 =
	{
	        {
	                18363, -109899, 74431, -12528, 16488
			},
			{
			        18483, -109701, 74501, -12528, 24576
			},
			{
			        18483, -109892, 74886, -12528, 0
			},
			{
			        18363, -109703, 74879, -12528, 49336
			}
			
	};
	
	private static final int[][] _group_2 =
	{
	        {
	                18363, -110393, 78276, -12848, 49152
			},
			{
			        18363, -110561, 78276, -12848, 49152
			},
			{
			        18362, -110414, 78495, -12905, 48112
			},
			{
			        18362, -110545, 78489, -12903, 48939
			},
			{
			        18483, -110474, 78601, -12915, 49488
			},
			{
			        18362, -110474, 78884, -12915, 49338
			},
			{
			        18483, -110389, 79131, -12915, 48539
			},
			{
			        18483, -110551, 79134, -12915, 49151
			}
	};
	
	private static final int[][] _group_3 =
	{
	        {
	                18483, -107798, 80721, -12912, 0
			},
			{
			        18483, -107798, 80546, -12912, 0
			},
			{
			        18347, -108033, 80644, -12912, 0
			},
			{
			        18363, -108520, 80647, -12912, 0
			},
			{
			        18483, -108740, 80752, -12912, 0
			},
			{
			        18363, -109016, 80642, -12912, 0
			},
			{
			        18483, -108740, 80546, -12912, 0
			}
	};
	
	private static final int[][] _group_4 =
	{
	        {
	                18362, -110082, 83998, -12928, 0
			},
			{
			        18362, -110082, 84210, -12928, 0
			},
			{
			        18363, -109963, 84102, -12896, 0
			},
			{
			        18347, -109322, 84102, -12880, 0
			},
			{
			        18362, -109131, 84097, -12880, 0
			},
			{
			        18483, -108932, 84101, -12880, 0
			},
			{
			        18483, -109313, 84488, -12880, 0
			},
			{
			        18362, -109122, 84490, -12880, 0
			},
			{
			        18347, -108939, 84489, -12880, 0
			}
	};
	
	public NornilsGarden()
	{
		super(11);
		
		addStartNpc(32330);
		addFirstTalkId(32330);
		addTalkId(32330);
		
		for (final int i[] : _gatekeepers)
		{
			addKillId(i[0]);
		}
		
		for (final int i[] : _auto_gates)
		{
			addEnterZoneId(i[0]);
		}
		
		addTalkId(_final_gates);
		addAttackId(18362);
	}
	
	private final synchronized void enterInstance(Player player, Npc npc)
	{
		if (enterReflection(player, npc, 11))
		{
			final var r = player.getReflection();
			if (isInReflection(r))
			{
				final var first = addSpawn(18362, -109702, 74696, -12528, 49568, false, 0, false, r);
				r.setParam("first", first);
				r.setParam("spawned1", false);
				r.setParam("spawned2", false);
				r.setParam("spawned3", false);
				r.setParam("spawned4", false);
				final var door = r.getDoor(16200010);
				if (door != null)
				{
					door.setTargetable(false);
					door.setMeshIndex(2);
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
		
		boolean update = false;
		if (skill1 != null)
		{
			skill1.getEffects(player, player, false, false);
			update = true;
		}
		if (skill2 != null)
		{
			skill2.getEffects(player, player, false, false);
			update = true;
		}
		if (skill3 != null)
		{
			skill3.getEffects(player, player, false, false);
			update = true;
		}
		if (skill4 != null)
		{
			skill4.getEffects(player, player, false, false);
			update = true;
		}
		
		if (update)
		{
			player.updateEffectIcons();
		}
	}
	
	
	private void spawn1(Npc npc)
	{
		final var r = npc.getReflection();
		if (isInReflection(r))
		{
			final var first = r.getParams().getObject("first", Npc.class);
			final var spawned1 = r.getParams().getBool("spawned1", false);
			if (first != null && first.getObjectId() == npc.getObjectId() && !spawned1)
			{
				r.setParam("spawned1", true);
				for (final int mob[] : _group_1)
				{
					addSpawn(mob[0], mob[1], mob[2], mob[3], mob[4], false, 0, false, r);
				}
			}
		}
	}
	
	private void spawn2(Npc npc)
	{
		final var r = npc.getReflection();
		if (isInReflection(r))
		{
			final var spawned2 = r.getParams().getBool("spawned2", false);
			if (!spawned2)
			{
				r.setParam("spawned2", true);
				for (final int mob[] : _group_2)
				{
					addSpawn(mob[0], mob[1], mob[2], mob[3], mob[4], false, 0, false, r);
				}
			}
		}
	}
	
	private void spawn3(Creature cha)
	{
		final var r = cha.getReflection();
		if (isInReflection(r))
		{
			final var spawned3 = r.getParams().getBool("spawned3", false);
			if (!spawned3)
			{
				r.setParam("spawned3", true);
				for (final int mob[] : _group_3)
				{
					addSpawn(mob[0], mob[1], mob[2], mob[3], mob[4], false, 0, false, r);
				}
			}
		}
	}
	
	private void spawn4(Creature cha)
	{
		final var r = cha.getReflection();
		if (isInReflection(r))
		{
			final var spawned4 = r.getParams().getBool("spawned4", false);
			if (!spawned4)
			{
				r.setParam("spawned4", true);
				for (final int mob[] : _group_4)
				{
					addSpawn(mob[0], mob[1], mob[2], mob[3], mob[4], false, 0, false, r);
				}
			}
		}
	}
	
	public void openDoor(QuestState st, Player player, int doorId)
	{
		st.unset("correct");
		final var r = player.getReflection();
		if (isInReflection(r))
		{
			r.openDoor(doorId);
		}
	}
	
	@Override
	public String onEnterZone(Creature character, ZoneType zone)
	{
		if ((character.isPlayer()) && !character.isDead() && !character.isTeleporting())
		{
			final var r = character.getReflection();
			if (isInReflection(r))
			{
				for (final int _auto[] : _auto_gates)
				{
					if (zone.getId() == _auto[0])
					{
						r.openDoor(_auto[1]);
					}
					
					if (zone.getId() == 20111)
					{
						spawn3(character);
					}
					else if (zone.getId() == 20112)
					{
						spawn4(character);
					}
				}
			}
		}
		return super.onEnterZone(character, zone);
	}
	
	@Override
	public final String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		final var st = player.getQuestState(getName());
		if (st == null)
		{
			return getNoQuestMsg(player);
		}
		
		if ((npc.getId() == 32330) && event.equalsIgnoreCase("enter_instance"))
		{
			enterInstance(player, npc);
		}
		else if ((npc.getId() == 32258) && event.equalsIgnoreCase("exit"))
		{
			final var r = player.getReflection();
			if (isInReflection(r))
			{
				r.removeAllowed(player);
			}
			teleportPlayer(player, new Location(-74058, 52040, -3680), ReflectionManager.DEFAULT);
		}
		else if (ArrayUtils.contains(_final_gates, npc.getId()))
		{
			if (event.equalsIgnoreCase("32260-02.html") || event.equalsIgnoreCase("32261-02.html") || event.equalsIgnoreCase("32262-02.html"))
			{
				st.unset("correct");
			}
			else if (Util.isDigit(event))
			{
				int correct = st.getInt("correct");
				correct++;
				st.set("correct", String.valueOf(correct));
				htmltext = npc.getId() + "-0" + String.valueOf(correct + 2) + ".html";
			}
			else if (event.equalsIgnoreCase("check"))
			{
				final int correct = st.getInt("correct");
				if ((npc.getId() == 32260) && (correct == 3))
				{
					openDoor(st, player, 16200014);
				}
				else if ((npc.getId() == 32261) && (correct == 3))
				{
					openDoor(st, player, 16200015);
				}
				else if ((npc.getId() == 32262) && (correct == 4))
				{
					openDoor(st, player, 16200016);
				}
				else
				{
					return npc.getId() + "-00.html";
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public final String onTalk(Npc npc, Player player)
	{
		if (ArrayUtils.contains(_final_gates, npc.getId()))
		{
			final var cst = player.getQuestState("_179_IntoTheLargeCavern");
			if ((cst != null) && (cst.getState() == State.STARTED))
			{
				return npc.getId() + "-01.html";
			}
			return getNoQuestMsg(player);
		}
		
		return null;
	}
	
	@Override
	public final String onFirstTalk(Npc npc, Player player)
	{
		var st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}
		return npc.getId() + ".html";
	}
	
	@Override
	public final String onAttack(Npc npc, Player attacker, int damage, boolean isSummon)
	{
		if ((npc.getId() == 18362) && (npc.getReflectionId() > 0))
		{
			spawn1(npc);
		}
		return null;
	}
	
	@Override
	public final String onKill(Npc npc, Player player, boolean isSummon)
	{
		final var st = player.getQuestState(getName());
		if (st == null)
		{
			return null;
		}
		
		for (final int _gk[] : _gatekeepers)
		{
			if (npc.getId() == _gk[0])
			{
				((MonsterInstance) npc).dropSingleItem(player, _gk[1], 1);
				if (_gk[2] > 0)
				{
					final var r = player.getReflection();
					if (isInReflection(r))
					{
						r.openDoor(_gk[2]);
					}
				}
			}
			if (npc.getId() == 18355)
			{
				spawn2(npc);
			}
		}
		return super.onKill(npc, player, isSummon);
	}
	
	void main()
	{
		new NornilsGarden();
	}
}
