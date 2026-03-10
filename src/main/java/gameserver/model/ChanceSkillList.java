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
package gameserver.model;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gameserver.Config;
import gameserver.data.parser.SkillTreesParser;
import gameserver.data.parser.SkillsParser;
import gameserver.handler.skillhandlers.SkillHandler;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Player;
import gameserver.model.interfaces.IChanceSkillTrigger;
import gameserver.model.skills.Skill;
import gameserver.model.skills.SkillType;
import gameserver.model.skills.effects.Effect;
import gameserver.model.skills.targets.TargetType;
import gameserver.network.serverpackets.MagicSkillLaunched;
import gameserver.network.serverpackets.MagicSkillUse;

public class ChanceSkillList extends ConcurrentHashMap<IChanceSkillTrigger, ChanceCondition>
{
	protected static final Logger _log = LogManager.getLogger(ChanceSkillList.class);
	@Serial
	private static final long serialVersionUID = 1L;

	private final Creature _owner;

	public ChanceSkillList(Creature owner)
	{
		super();

		_owner = owner;
	}

	public Creature getOwner()
	{
		return _owner;
	}

	public void onHit(Creature target, int damage, boolean ownerWasHit, boolean wasCrit)
	{
		int event;
		if (ownerWasHit)
		{
			event = ChanceCondition.EVT_ATTACKED | ChanceCondition.EVT_ATTACKED_HIT;
			if (wasCrit)
			{
				event |= ChanceCondition.EVT_ATTACKED_CRIT;
			}
		}
		else
		{
			event = ChanceCondition.EVT_HIT;
			if (wasCrit)
			{
				event |= ChanceCondition.EVT_CRIT;
			}
		}

		onEvent(event, damage, target, null, Elementals.NONE);
	}

	public void onEvadedHit(Creature attacker)
	{
		onEvent(ChanceCondition.EVT_EVADED_HIT, 0, attacker, null, Elementals.NONE);
	}

	public void onSkillHit(Creature target, double damage, Skill skill, boolean ownerWasHit)
	{
		int event;
		if (ownerWasHit)
		{
			event = ChanceCondition.EVT_HIT_BY_SKILL;
			if (skill.isOffensive())
			{
				event |= ChanceCondition.EVT_HIT_BY_OFFENSIVE_SKILL;
				event |= ChanceCondition.EVT_ATTACKED;
				event |= ChanceCondition.EVT_ATTACKED_HIT;
			}
			else
			{
				event |= ChanceCondition.EVT_HIT_BY_GOOD_MAGIC;
			}
		}
		else
		{
			event = ChanceCondition.EVT_CAST;
			event |= skill.isMagic() ? ChanceCondition.EVT_MAGIC : ChanceCondition.EVT_PHYSICAL;
			event |= skill.isOffensive() ? ChanceCondition.EVT_MAGIC_OFFENSIVE : ChanceCondition.EVT_MAGIC_GOOD;
		}

		onEvent(event, (int) damage, target, skill, skill.getElement());
	}

	public void onStart(byte element)
	{
		onEvent(ChanceCondition.EVT_ON_START, 0, _owner, null, element);
	}

	public void onActionTime(byte element)
	{
		onEvent(ChanceCondition.EVT_ON_ACTION_TIME, 0, _owner, null, element);
	}

	public void onExit(byte element)
	{
		onEvent(ChanceCondition.EVT_ON_EXIT, 0, _owner, null, element);
	}

	public void onEvent(int event, int damage, Creature target, Skill skill, byte element)
	{
		if (_owner.isDead())
		{
			return;
		}

		final boolean playable = target.isPlayable();
		for (final var entry : entrySet())
		{
			final var trigger = entry.getKey();
			final var cond = entry.getValue();
			
			if (cond != null && cond.trigger(event, damage, element, playable, skill))
			{
				if (trigger instanceof Skill skill1)
				{
					_owner.makeTriggerCast(skill1, target);
				}
				else
				{
					makeCast((Effect) trigger, target);
				}
			}
		}
	}

	private void makeCast(Effect effect, Creature target)
	{
		try
		{
			if ((effect == null) || !effect.triggersChanceSkill())
			{
				return;
			}

			final var triggered = SkillsParser.getInstance().getInfo(effect.getTriggeredChanceId(), effect.getTriggeredChanceLevel());
			if (triggered == null)
			{
				return;
			}
			
			final var isForSelf = triggered.getTargetType() == TargetType.SELF;
			final var isSubclassSkill = SkillTreesParser.getInstance().isSubClassSkill(effect.getSkill().getId());
			final var caster = isForSelf ? _owner : effect.getEffector();

			if ((caster == null) || (triggered.getSkillType() == SkillType.NOTDONE) || caster.isSkillDisabled(triggered) || caster.isSkillBlocked(triggered))
			{
				return;
			}

			if (triggered.getReuseDelay() > 0)
			{
				caster.disableSkill(triggered, triggered.getReuseDelay());
			}

			var targets = triggered.getTargetList(caster, false, target);
			if (targets.length == 0)
			{
				return;
			}

			final var firstTarget = (Creature) targets[0];
			if (Config.ALT_VALIDATE_TRIGGER_SKILLS && !isForSelf && caster.isPlayable() && (firstTarget != null) && firstTarget.isPlayable())
			{
				final Player player = caster.getActingPlayer();
				if (!player.checkPvpSkill(firstTarget, triggered, caster.isSummon()))
				{
					return;
				}
			}
			
			if (isSubclassSkill && isForSelf && _owner.hasServitor() && !_owner.getSummon().isDead())
			{
				final List<GameObject> tg = new ArrayList<>();
				tg.add(_owner);
				tg.add(_owner.getSummon());
				targets = tg.toArray(new GameObject[tg.size()]);
			}
			
			final var handler = SkillHandler.getInstance().getHandler(triggered.getSkillType());
			_owner.broadcastPacket(new MagicSkillLaunched(_owner, triggered.getDisplayId(), triggered.getDisplayLevel(), targets));
			_owner.broadcastPacket(new MagicSkillUse(_owner, firstTarget, triggered.getDisplayId(), triggered.getDisplayLevel(), 0, 0));

			if (handler != null)
			{
				handler.useSkill(caster, triggered, targets, 0);
			}
			else
			{
				triggered.useSkill(caster, targets, 0);
			}
		}
		catch (final Exception e)
		{
			_log.warn("", e);
		}
	}
}