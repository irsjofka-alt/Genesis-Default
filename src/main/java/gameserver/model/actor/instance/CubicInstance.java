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
package gameserver.model.actor.instance;

import java.util.Comparator;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import l2e.commons.util.Rnd;
import gameserver.Config;
import gameserver.ThreadPoolManager;
import gameserver.handler.skillhandlers.SkillHandler;
import gameserver.model.GameObject;
import gameserver.model.World;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Player;
import gameserver.model.actor.templates.cubic.CubicSkill;
import gameserver.model.actor.templates.cubic.CubicTemplate;
import gameserver.model.base.CubicTargetType;
import gameserver.model.interfaces.IIdentifiable;
import gameserver.model.skills.Skill;
import gameserver.network.serverpackets.MagicSkillUse;
import gameserver.taskmanager.AttackStanceTaskManager;

public final class CubicInstance implements IIdentifiable
{
	private final Player _owner;
	private final Player _caster;
	private final CubicTemplate _template;
	private int _currentcount;
	private final long _cubicDisappearTime;
	private Future<?> _actionTask;

	public CubicInstance(Player owner, Player caster, CubicTemplate template)
	{
		_owner = owner;
		_caster = caster == null ? owner : caster;
		_template = template;
		_cubicDisappearTime = System.currentTimeMillis() + (_template.getDuration() * 1000);
		_actionTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(this::useSkill, 1000, _template.getDelay() * 1000);
	}
	
	public void deactivate(boolean isSingle)
	{
		final var task = _actionTask;
		if (task != null)
		{
			task.cancel(true);
		}
		_actionTask = null;
		_owner.getCubics().remove(_template.getId());
		if (isSingle)
		{
			_owner.broadcastUserInfo(true);
		}
	}
	
	private void useSkill()
	{
		if (_owner.isDead() || !_owner.isOnline() || System.currentTimeMillis() > _cubicDisappearTime)
		{
			deactivate(true);
			return;
		}
		
		if ((_template.getMaxCount() > -1) && (_currentcount >= _template.getMaxCount()))
		{
			return;
		}
		
		switch (_template.getTargetType())
		{
			case TARGET :
			{
				actionToCurrentTarget();
				break;
			}
			case BY_SKILL :
			{
				actionToTargetBySkill();
				break;
			}
			case HEAL :
			{
				actionHeal();
				break;
			}
			case MASTER :
			{
				actionToMaster();
				break;
			}
		}
	}
	
	private CubicSkill chooseSkill()
	{
		final double random = Rnd.nextDouble() * 100;
		double commulativeChance = 0;
		for (final var cubicSkill : _template.getSkills())
		{
			if ((commulativeChance += cubicSkill.getTriggerRate()) > random)
			{
				return cubicSkill;
			}
		}
		return null;
	}
	
	private void actionToCurrentTarget()
	{
		final var skill = chooseSkill();
		final var target = _owner.getTarget();
		if ((skill != null) && (target != null && target.isCreature()))
		{
			final var player = target.getActingPlayer();
			if (!AttackStanceTaskManager.getInstance().hasAttackStanceTask(_owner) || (player != null && _owner.isFriend(player, true)))
			{
				return;
			}
			tryToUseSkill((Creature) target, skill);
		}
	}
	
	private void actionToTargetBySkill()
	{
		final var skill = chooseSkill();
		if (skill != null)
		{
			switch (skill.getTargetType())
			{
				case TARGET :
				{
					final var target = _owner.getTarget();
					if (target != null && target.isCreature())
					{
						final var player = target.getActingPlayer();
						if (!AttackStanceTaskManager.getInstance().hasAttackStanceTask(_owner) || (player != null && _owner.isFriend(player, true)))
						{
							return;
						}
						tryToUseSkill((Creature) target, skill);
					}
					break;
				}
				case HEAL :
				{
					actionHeal();
					break;
				}
				case MASTER :
				{
					tryToUseSkill(_owner, skill);
					break;
				}
			}
		}
	}
	
	private void actionHeal()
	{
		final double random = Rnd.nextDouble() * 100;
		double commulativeChance = 0;
		for (final var cubicSkill : _template.getSkills())
		{
			if ((commulativeChance += cubicSkill.getTriggerRate()) > random)
			{
				final var skill = cubicSkill.getSkill();
				if ((skill != null) && (Rnd.get(100) < cubicSkill.getSuccessRate()))
				{
					final var party = _owner.getParty();
					Stream<Creature> stream = Stream.empty();
					if (party != null)
					{
						stream = World.getAroundPlayers(_owner, Config.ALT_PARTY_RANGE, 200).stream().filter(c -> (c.getParty() == party && _template.validateConditions(this, _owner, c) && cubicSkill.validateConditions(this, _owner, c))).map(Creature.class::cast);
					}

					final var summon = _owner.getSummon();
					if (summon != null && _template.validateConditions(this, _owner, summon) && cubicSkill.validateConditions(this, _owner, summon))
					{
						stream = Stream.concat(stream, Stream.of(summon));
					}
					
					if (_template.validateConditions(this, _owner, _owner) && cubicSkill.validateConditions(this, _owner, _owner))
					{
						stream = Stream.concat(stream, Stream.of(_owner));
					}
					
					final Creature target = stream.sorted(Comparator.comparingInt(Creature::getCurrentHpPercent)).findFirst().orElse(null);
					if ((target != null) && (!target.isDead()))
					{
						if (Rnd.nextDouble() > (target.getCurrentHp() / target.getMaxHp()))
						{
							activateCubicSkill(skill, target);
						}
						break;
					}
				}
			}
		}
	}
	
	private void actionToMaster()
	{
		final var skill = chooseSkill();
		if (skill != null)
		{
			tryToUseSkill(_owner, skill);
		}
	}
	
	private void tryToUseSkill(Creature firstTarget, CubicSkill cubicSkill)
	{
		GameObject[] targets = new Creature[]
		{
		        firstTarget
		};
		final Skill skill = cubicSkill.getSkill();
		if ((_template.getTargetType() != CubicTargetType.MASTER) && !((_template.getTargetType() == CubicTargetType.BY_SKILL) && (cubicSkill.getTargetType() == CubicTargetType.MASTER)))
		{
			targets = skill.getTargetList(_owner, false, firstTarget);
		}
		
		if (targets.length == 0)
		{
			return;
		}
		
		final var target = (Creature) targets[0];
		if (target != null)
		{
			if (target.isDoor() && !cubicSkill.canUseOnStaticObjects())
			{
				return;
			}
			
			if (_template.validateConditions(this, _owner, target) && cubicSkill.validateConditions(this, _owner, target) && (Rnd.get(100) < cubicSkill.getSuccessRate()))
			{
				activateCubicSkill(skill, target);
			}
		}
	}
	
	private void activateCubicSkill(Skill skill, Creature target)
	{
		if (!_owner.hasSkillReuse(skill.getReuseHashCode()))
		{
			_caster.broadcastPacket(new MagicSkillUse(_owner, target, skill.getDisplayId(), skill.getDisplayLevel(), 0, 0));
			final Creature[] targets =
			{
			        target
			};
			final var handler = SkillHandler.getInstance().getHandler(skill.getSkillType());
			if (handler != null)
			{
				handler.useSkill(_owner, skill, targets, _template.getPower());
			}
			else
			{
				skill.useSkill(_owner, targets, _template.getPower());
			}
			_currentcount++;
			_owner.addTimeStamp(skill, skill.getReuseDelay());
		}
	}

	@Override
	public int getId()
	{
		return _template.getId();
	}

	public Player getOwner()
	{
		return _owner;
	}
	
	public boolean isGivenByOther()
	{
		return _caster != _owner;
	}
}