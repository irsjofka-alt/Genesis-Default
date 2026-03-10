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

import gameserver.ThreadPoolManager;
import gameserver.ai.model.CtrlIntention;
import gameserver.instancemanager.ZoneManager;
import gameserver.model.GameObject;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.actor.templates.reflection.ReflectionTemplate;
import gameserver.model.entity.Reflection;
import gameserver.model.skills.Skill;
import gameserver.model.skills.effects.EffectType;

/**
 * Rework by LordWinter 29.09.2020
 */
public class SufferingHallAttack extends AbstractReflection
{
	public SufferingHallAttack()
	{
		super(115);

		addStartNpc(32530, 32537);
		addTalkId(32530, 32537);
		addAttackId(25665, 25666);
		addSkillSeeId(22509, 22510, 22511, 22512, 22513, 22514, 22515);
		addKillId(18704, 25665);
	}
	
	private final synchronized void enterInstance(Player player, Npc npc)
	{
		if (enterReflection(player, npc, 115))
		{
			final var r = player.getReflection();
			if (isInReflection(r))
			{
				r.setParam("startTime", System.currentTimeMillis());
				r.setParam("TAG", -1);
				spawnRoom(r, 1);
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
	
	private void spawnRoom(Reflection r, int id)
	{
		switch (id)
		{
			case 1 :
				r.spawnByGroup("soi_hos_attack_1");
				getActivatedZone(r, 20029, true);
				break;
			case 2 :
				r.spawnByGroup("soi_hos_attack_2");
				getActivatedZone(r, 20030, true);
				break;
			case 3 :
				r.spawnByGroup("soi_hos_attack_3");
				getActivatedZone(r, 20031, true);
				break;
			case 4 :
				r.spawnByGroup("soi_hos_attack_4");
				getActivatedZone(r, 20032, true);
				break;
			case 5 :
				r.spawnByGroup("soi_hos_attack_5");
				getActivatedZone(r, 20033, true);
				break;
			case 6 :
				r.spawnByGroup("soi_hos_attack_6");
				getActivatedZone(r, 20034, true);
				break;
			case 7 :
				r.spawnByGroup("soi_hos_attack_7");
				getActivatedZone(r, 20034, false);
				break;
		}
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
			switch (npc.getId())
			{
				case 18704 :
					npc.deleteMe();
					addSpawn(32531, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0, false, r);
					if (ZoneManager.getInstance().isInsideZone(20029, npc))
					{
						getActivatedZone(r, 20029, false);
						spawnRoom(r, 2);
					}
					else if (ZoneManager.getInstance().isInsideZone(20030, npc))
					{
						getActivatedZone(r, 20030, false);
						spawnRoom(r, 3);
					}
					else if (ZoneManager.getInstance().isInsideZone(20031, npc))
					{
						getActivatedZone(r, 20031, false);
						spawnRoom(r, 4);
					}
					else if (ZoneManager.getInstance().isInsideZone(20032, npc))
					{
						getActivatedZone(r, 20032, false);
						spawnRoom(r, 5);
					}
					else if (ZoneManager.getInstance().isInsideZone(20033, npc))
					{
						getActivatedZone(r, 20033, false);
						spawnRoom(r, 6);
					}
					break;
				case 25665 :
					r.addTimer("FINISH_REFLECTION", ThreadPoolManager.getInstance().schedule(() -> finishReflection(r), 10000L));
					break;
			}
		}
		return super.onKill(npc, player, isSummon);
	}
	
	private void finishReflection(Reflection r)
	{
		if (r != null)
		{
			spawnRoom(r, 7);
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
			finishInstance(r, 300000, true);
		}
	}
	
	public static void main(String[] args)
	{
		new SufferingHallAttack();
	}
}