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

import java.util.HashMap;
import java.util.Map;

import gameserver.model.skills.effects.Effect;
import gameserver.model.skills.effects.EffectFlag;
import gameserver.model.skills.effects.EffectTemplate;
import gameserver.model.skills.effects.EffectType;
import gameserver.model.stats.Env;
import gameserver.model.stats.Stats;

public class ServitorShare extends Effect
{
	private final Map<Stats, Double> stats = new HashMap<>();

	public ServitorShare(Env env, EffectTemplate template)
	{
		super(env, template);
		for (final String key : template.getParameters().keySet())
		{
			stats.put(Stats.valueOfXml(key), template.getParameters().getDouble(key, 1.));
		}
	}

	@Override
	public boolean canBeStolen()
	{
		return false;
	}

	@Override
	public int getEffectFlags()
	{
		return EffectFlag.SERVITOR_SHARE.getMask();
	}

	@Override
	public EffectType getEffectType()
	{
		return EffectType.BUFF;
	}
	
	@Override
	public boolean onStart()
	{
		super.onStart();
		
		final var player = getEffected().getActingPlayer();
		if (player == null)
		{
			return false;
		}
		player.setServitorShare(stats);
		final var summon = player.getSummon();
		if (summon != null)
		{
			summon.updateAndBroadcastStatus(1);
			summon.getStatus().startHpMpRegeneration();
		}
		return true;
	}

	@Override
	public void onExit()
	{
		final var player = getEffected().getActingPlayer();
		if (player == null)
		{
			return;
		}
		player.setServitorShare(null);
		final var summon = player.getSummon();
		if (summon != null)
		{
			if (summon.getCurrentHp() > summon.getMaxHp())
			{
				summon.setCurrentHp(summon.getMaxHp());
			}
			if (summon.getCurrentMp() > summon.getMaxMp())
			{
				summon.setCurrentMp(summon.getMaxMp());
			}
			summon.updateAndBroadcastStatus(1);
		}
	}
}