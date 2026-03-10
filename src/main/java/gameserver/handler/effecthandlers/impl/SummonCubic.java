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
package gameserver.handler.effecthandlers.impl;

import l2e.commons.util.Rnd;
import gameserver.data.parser.CubicParser;
import gameserver.model.actor.instance.CubicInstance;
import gameserver.model.skills.effects.Effect;
import gameserver.model.skills.effects.EffectTemplate;
import gameserver.model.skills.effects.EffectType;
import gameserver.model.stats.Env;

public class SummonCubic extends Effect
{
	private final int _cubicId;
	
	public SummonCubic(Env env, EffectTemplate template)
	{
		super(env, template);
		
		_cubicId = template.getParameters().getInteger("cubicId", -1);
		if (_cubicId > 0)
		{
			template.setCubicId(_cubicId);
		}
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.SUMMON_CUBIC;
	}
	
	@Override
	public boolean onStart()
	{
		if ((getEffected() == null) || !getEffected().isPlayer() || getEffected().isAlikeDead() || getEffected().getActingPlayer().inObserverMode())
		{
			return false;
		}
		
		if (_cubicId <= 0)
		{
			_log.warn(SummonCubic.class.getSimpleName() + ": Invalid NPC Id:" + _cubicId + " in skill Id: " + getSkill().getId());
			return false;
		}
		
		final var player = getEffected().getActingPlayer();
		if (player.inObserverMode() || player.isMounted())
		{
			return false;
		}
		
		int cubicLevel = getSkill().getLevel();
		if (cubicLevel > 100)
		{
			cubicLevel = ((getSkill().getLevel() - 100) / 7) + 8;
		}
		
		final var template = CubicParser.getInstance().getCubicTemplate(_cubicId, cubicLevel);
		if (template == null)
		{
			_log.warn("Attempting to summon cubic without existing template id: " + _cubicId + " level: " + cubicLevel);
			return false;
		}
		
		if (!player.removeCubicById(_cubicId))
		{
			final var cubicMastery = player.getFirstPassiveEffect(EffectType.CUBIC_MASTERY);
			final int cubicCount = (int) (cubicMastery != null ? (cubicMastery.calc() - 1) : 0);
			final var cubicSize = player.getCubicsSize();
			if (cubicSize > cubicCount)
			{
				final int random = Rnd.get(cubicSize);
				final var removedCubic = player.getCubicByPosition(random);
				if (removedCubic != null)
				{
					removedCubic.deactivate(false);
				}
			}
		}
		player.addCubic(new CubicInstance(player, getEffector().getActingPlayer(), template));
		player.broadcastUserInfo(true);
		return true;
	}
}