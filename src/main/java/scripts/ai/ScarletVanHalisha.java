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
package scripts.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import l2e.commons.util.Rnd;
import gameserver.ai.DefaultAI;
import gameserver.data.parser.SkillsParser;
import gameserver.geodata.GeoEngine;
import gameserver.model.World;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Playable;
import gameserver.model.skills.Skill;

/**
 * Created by LordWinter 11.08.2025
 */
public class ScarletVanHalisha extends DefaultAI
{
	private long _newTarget = 0L;
	private long _lastRangedSkillTime;
	private final int _rangedSkillMinCoolTime = 60000;
	
	public ScarletVanHalisha(Attackable actor)
	{
		super(actor);
		
		actor.setIsGlobalAI(true);
	}

	@Override
	protected void onEvtSpawn()
	{
		_newTarget = System.currentTimeMillis();
		super.onEvtSpawn();
	}
	
	@Override
	protected void thinkAttack()
	{
		final var actor = getActiveChar();
		if (actor == null || actor.isDead())
		{
			return;
		}
		
		if ((_newTarget + 20000L) < System.currentTimeMillis())
		{
			final List<Playable> alive = new ArrayList<>();
			for (final var target : actor.getReflection().isDefault() ? World.getAroundPlayables(actor, 4000, 200) : actor.getReflection().getReflectionPlayers())
			{
				if (target != null)
				{
					if (!target.isDead() && !target.isInvisible() && GeoEngine.getInstance().canSeeTarget(actor, target))
					{
						alive.add(target);
					}
				}
			}
			
			if (alive == null || alive.isEmpty())
			{
				super.thinkAttack();
				return;
			}
			
			final var rndTarget = alive.get(Rnd.get(alive.size()));
			if (rndTarget != null)
			{
				final var mostHate = actor.getAggroList().getMostHated();
				if (mostHate != null)
				{
					actor.addDamageHate(rndTarget, 0, (actor.getAggroList().getHating(mostHate) + 500));
				}
				else
				{
					actor.addDamageHate(rndTarget, 0, 2000);
				}
				actor.setTarget(rndTarget);
				setAttackTarget(rndTarget);
			}
			_newTarget = System.currentTimeMillis();
		}
		super.thinkAttack();
	}

	@Override
	protected boolean createNewTask()
	{
		final var actor = getActiveChar();
		if (actor == null || actor.isDead())
		{
			return true;
		}

		final var target = actor.getTarget();
		if (target == null)
		{
			return false;
		}
		
		if (actor.isMoving())
		{
			return true;
		}

		Skill select = null;
		if(actor.isMovementDisabled())
		{
			select = SkillsParser.getInstance().getInfo(5014, 1);
			return cast(select);
		}
		else
		{
			if (Rnd.get(100) < 30)
			{
				final Map<Skill, Integer> skillList = new HashMap<>();
				final double distance = actor.getDistance(target);
				if (actor.getId() == 29046)
				{
					if (Rnd.get(100) < 10)
					{
						addDesiredSkill(skillList, (Creature) target, distance, SkillsParser.getInstance().getInfo(5015, 2));
					}
					else if (Rnd.get(100) < 10)
					{
						addDesiredSkill(skillList, (Creature) target, distance, SkillsParser.getInstance().getInfo(5015, 5));
					}
					
					if (Rnd.get(100) < 2)
					{
						addDesiredSkill(skillList, (Creature) target, distance, SkillsParser.getInstance().getInfo(5016, 1));
					}
					else
					{
						addDesiredSkill(skillList, (Creature) target, distance, SkillsParser.getInstance().getInfo(5014, 2));
					}
				}
				else
				{
					if (Rnd.get(100) < 10)
					{
						addDesiredSkill(skillList, (Creature) target, distance, SkillsParser.getInstance().getInfo(5015, 3));
					}
					else if (Rnd.get(100) < 10)
					{
						addDesiredSkill(skillList, (Creature) target, distance, SkillsParser.getInstance().getInfo(5015, 6));
					}
					else if (Rnd.get(100) < 10)
					{
						addDesiredSkill(skillList, (Creature) target, distance, SkillsParser.getInstance().getInfo(5015, 2));
					}
					
					if (((_lastRangedSkillTime + _rangedSkillMinCoolTime) < System.currentTimeMillis()) && (Rnd.get(100) < 10))
					{
						addDesiredSkill(skillList, (Creature) target, distance, SkillsParser.getInstance().getInfo(5019, 1));
					}
					else if (((_lastRangedSkillTime + _rangedSkillMinCoolTime) < System.currentTimeMillis()) && (Rnd.get(100) < 10))
					{
						addDesiredSkill(skillList, (Creature) target, distance, SkillsParser.getInstance().getInfo(5018, 1));
					}
					
					if (Rnd.get(100) < 2)
					{
						addDesiredSkill(skillList, (Creature) target, distance, SkillsParser.getInstance().getInfo(5016, 1));
					}
					else
					{
						addDesiredSkill(skillList, (Creature) target, distance, SkillsParser.getInstance().getInfo(5014, 3));
					}
				}
				select = selectTopSkill(skillList);
				
				if (select == null)
				{
					select = SkillsParser.getInstance().getInfo(5014, 1);
				}
				
				if (select.getId() == 5018 || select.getId() == 5019)
				{
					_lastRangedSkillTime = System.currentTimeMillis();
				}
				return cast(select);
			}
		}
		return false;
	}
}