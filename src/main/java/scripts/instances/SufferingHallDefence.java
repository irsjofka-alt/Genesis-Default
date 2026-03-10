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

import gameserver.ThreadPoolManager;
import gameserver.ai.model.CtrlIntention;
import gameserver.model.GameObject;
import gameserver.model.Location;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.actor.templates.reflection.ReflectionTemplate;
import gameserver.model.entity.Reflection;
import gameserver.model.skills.Skill;
import gameserver.model.skills.effects.EffectType;
import gameserver.network.NpcStringId;
import gameserver.network.serverpackets.ExShowScreenMessage;

/**
 * Rework by LordWinter 29.09.2020
 */
public class SufferingHallDefence extends AbstractReflection
{
	private static final int[] monsters =
	{
	        22509, 22510, 22511, 22512, 22513, 22514, 22515, 18704
	};

	public SufferingHallDefence()
	{
		super(116);

		addStartNpc(32530, 32537);
		addTalkId(32530, 32537);
		addAttackId(25665, 25666);
		addSkillSeeId(22509, 22510, 22511, 22512, 22513, 22514, 22515);
		addKillId(18704, 22509, 22510, 22511, 22512, 22513, 22514, 22515, 25665, 25666);
	}
	
	private final synchronized void enterInstance(Player player, Npc npc)
	{
		if (enterReflection(player, npc, 116))
		{
			final var r = player.getReflection();
			if (isInReflection(r))
			{
				r.setParam("startTime", System.currentTimeMillis());
				r.setParam("TAG", -1);
				r.setParam("tumorIndex", 300);
				r.setParam("doCountCoffinNotifications", false);
				startDefence(r);
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
	public String onSkillSee(Npc npc, Player caster, Skill skill, GameObject[] targets, boolean isSummon)
	{
		if (skill.hasEffectType(EffectType.REBALANCE_HP, EffectType.HEAL, EffectType.HEAL_PERCENT))
		{
			int hate = 2 * skill.getAggroPoints();
			if (hate < 2)
			{
				hate = 1000;
			}
			((Attackable) npc).addDamageHate(caster, 0, hate);
		}
		return super.onSkillSee(npc, caster, skill, targets, isSummon);
	}

	@Override
	public String onKill(Npc npc, Player player, boolean isSummon)
	{
		final var r = npc.getReflection();
		if (isInReflection(r))
		{
			if (ArrayUtils.contains(monsters, npc.getId()) && !checkAliveMonsters(r))
			{
				r.removeTimer("MONSTERS_SPAWN");
				r.addTimer("MONSTERS_SPAWN", ThreadPoolManager.getInstance().schedule(() -> spawnMonsters(r), 40000L));
			}
			
			if (npc.getId() == 18704)
			{
				npc.deleteMe();
				notifyCoffinActivity(npc, r);
				addSpawn(18705, -173704, 218092, -9562, 0, false, 0, false, r);
				r.setParam("tumorIndex", 300);
				r.setParam("doCountCoffinNotifications", true);
			}
			else if (npc.getId() == 25665)
			{
				r.addTimer("FINISH_REFLECTION", ThreadPoolManager.getInstance().schedule(() -> finishReflection(r), 10000L));
			}
		}
		return super.onKill(npc, player, isSummon);
	}
	
	private void finishReflection(Reflection r)
	{
		if (r != null)
		{
			r.removeTimer("MONSTERS_SPAWN");
			r.removeTimer("COFFIN_SPAWN");
			final var finishDiff = System.currentTimeMillis() - r.getParams().getLong("startTime", 0);
			if (finishDiff < 1260000)
			{
				r.setParam("TAG", 13777);
			}
			else if (finishDiff < 1380000)
			{
				r.setParam("TAG", 13778);
			}
			else if (finishDiff < 1500000)
			{
				r.setParam("TAG", 13779);
			}
			else if (finishDiff < 1620000)
			{
				r.setParam("TAG", 13780);
			}
			else if (finishDiff < 1740000)
			{
				r.setParam("TAG", 13781);
			}
			else if (finishDiff < 1860000)
			{
				r.setParam("TAG", 13782);
			}
			else if (finishDiff < 1980000)
			{
				r.setParam("TAG", 13783);
			}
			else if (finishDiff < 2100000)
			{
				r.setParam("TAG", 13784);
			}
			else if (finishDiff < 2220000)
			{
				r.setParam("TAG", 13785);
			}
			else
			{
				r.setParam("TAG", 13786);
			}
			r.spawnByGroup("soi_hos_defence_tepios");
			finishInstance(r, 300000, true);
		}
	}
	
	private void notifyCoffinActivity(Npc npc, Reflection r)
	{
		if (!r.getParams().getBool("doCountCoffinNotifications", false))
		{
			return;
		}
		
		final int index = r.getParams().getInteger("tumorIndex", 300) - 5;
		r.setParam("tumorIndex", index);

		if (index == 100)
		{
			r.broadcastPacket(new ExShowScreenMessage(NpcStringId.THE_AREA_NEAR_THE_TUMOR_IS_FULL_OF_OMINOUS_ENERGY, 2, 1, 8000));
		}
		else if (index == 30)
		{
			r.broadcastPacket(new ExShowScreenMessage(NpcStringId.YOU_CAN_FEEL_THE_SURGING_ENERGY_OF_DEATH_FROM_THE_TUMOR, 2, 1, 8000));
		}
		if (index <= 0)
		{
			final var tumor = getTumor(r, 18705);
			if (tumor != null)
			{
				tumor.deleteMe();
			}
			final var aliveTumor = spawnNpc(18704, new Location(-173704, 218092, -9562), 0, r);
			aliveTumor.setCurrentHp(aliveTumor.getMaxHp() * .4);
			r.setParam("doCountCoffinNotifications", false);
		}
	}
	
	private void startDefence(Reflection r)
	{
		r.spawnByGroup("soi_hos_defence_tumor");
		r.setParam("doCountCoffinNotifications", true);
		r.addTimer("COFFIN_SPAWN", ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> spawnCoffin(r), 1000L, 10000L));
		r.addTimer("MONSTERS_SPAWN", ThreadPoolManager.getInstance().schedule(() -> spawnMonsters(r), 60000L));
	}
	
	private void spawnCoffin(Reflection r)
	{
		if (r != null)
		{
			addSpawn(18706, -173704, 218092, -9562, 0, false, 0, false, r);
		}
	}
	
	private void spawnMonsters(Reflection r)
	{
		if (r != null)
		{
			if (r.getStatus() > 6)
			{
				return;
			}
			
			String group = null;
			switch (r.getStatus())
			{
				case 1 :
					group = "soi_hos_defence_mobs_1";
					getActivatedZone(r, 20035, true);
					break;
				case 2 :
					group = "soi_hos_defence_mobs_2";
					getActivatedZone(r, 20035, false);
					getActivatedZone(r, 20036, true);
					break;
				case 3 :
					group = "soi_hos_defence_mobs_3";
					getActivatedZone(r, 20036, false);
					getActivatedZone(r, 20037, true);
					break;
				case 4 :
					group = "soi_hos_defence_mobs_4";
					getActivatedZone(r, 20037, false);
					getActivatedZone(r, 20038, true);
					break;
				case 5 :
					group = "soi_hos_defence_mobs_5";
					getActivatedZone(r, 20038, false);
					getActivatedZone(r, 20039, true);
					break;
				case 6 :
					r.setParam("doCountCoffinNotifications", false);
					group = "soi_hos_defence_brothers";
					getActivatedZone(r, 20039, false);
					break;
				default :
					break;
			}
			r.incStatus();
			if (group != null)
			{
				r.spawnByGroup(group);
			}
			
			for (final var n : r.getNpcs())
			{
				if (!n.isDead() && n.isMonster() && ArrayUtils.contains(monsters, n.getId()))
				{
					n.setRunning();
					n.getAI().setIntention(CtrlIntention.MOVING, Location.findPointToStay(new Location(-173704, 218092, -9562), 200, true), 0);
				}
			}
		}
	}
	
	private boolean checkAliveMonsters(Reflection r)
	{
		if (r != null)
		{
			for (final Npc n : r.getNpcs())
			{
				if (ArrayUtils.contains(monsters, n.getId()) && !n.isDead())
				{
					return true;
				}
			}
			return false;
		}
		return false;
	}
	
	private Npc getTumor(Reflection r, int id)
	{
		if (r != null)
		{
			for (final Npc npc : r.getNpcs())
			{
				if (npc.getId() == id && !npc.isDead())
				{
					return npc;
				}
			}
		}
		return null;
	}
	
	void main()
	{
		new SufferingHallDefence();
	}
}