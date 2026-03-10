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

import gameserver.ai.model.CtrlIntention;
import gameserver.geodata.GeoEngine;
import gameserver.model.actor.Creature;
import gameserver.model.actor.instance.DefenderInstance;
import gameserver.model.actor.instance.FortCommanderInstance;
import gameserver.model.actor.instance.NpcInstance;
import gameserver.model.actor.instance.SiegeFlagInstance;
import gameserver.model.actor.instance.SiegeSummonInstance;
import gameserver.model.skills.effects.Effect;
import gameserver.model.skills.effects.EffectFlag;
import gameserver.model.skills.effects.EffectTemplate;
import gameserver.model.skills.effects.EffectType;
import gameserver.model.stats.Env;
import gameserver.utils.PositionUtils;

public class Fear extends Effect
{
	private final int _range;
	
	public Fear(Env env, EffectTemplate template)
	{
		super(env, template);
		
		_range = template.getParameters().getInteger("range", 500);
	}
	
	@Override
	public int getEffectFlags()
	{
		return EffectFlag.FEAR.getMask();
	}

	@Override
	public EffectType getEffectType()
	{
		return EffectType.FEAR;
	}
	
	@Override
	public boolean onStart()
	{
		final var effected = getEffected();
		if (effected == null || effected.isRaid() || effected.isEpicRaid() || effected.isAfraid() || (effected instanceof NpcInstance) || (effected instanceof DefenderInstance) || (effected instanceof FortCommanderInstance) || (effected instanceof SiegeFlagInstance) || (effected instanceof SiegeSummonInstance))
		{
			return false;
		}

		effected.startFear();
		effectRunTask(effected);
		return true;
	}
	
	@Override
	public void onExit()
	{
		final var effected = getEffected();
		if (effected != null)
		{
			effected.sendActionFailed();
		}
		super.onExit();
	}
	
	private void effectRunTask(Creature effected)
	{
		final double angle = Math.toRadians(PositionUtils.calculateAngleFrom(getEffector(), effected));
		final int oldX = effected.getX();
		final int oldY = effected.getY();
		
		final var range = Math.max(_range, effected.getRunSpeed() * getTimeLeft());
		
		final int x = oldX + (int) (range * Math.cos(angle));
		final int y = oldY + (int) (range * Math.sin(angle));
		if (!effected.isPet() && !effected.isRunning())
		{
			effected.setRunning();
		}
		effected.getAI().setIntention(CtrlIntention.MOVING, GeoEngine.getInstance().moveCheck(effected, effected.getX(), effected.getY(), effected.getZ(), x, y, effected.getZ(), effected.getReflection()), 0);
	}
}