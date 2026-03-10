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

import gameserver.model.actor.instance.SiegeSummonInstance;
import gameserver.model.skills.effects.Effect;
import gameserver.model.skills.effects.EffectTemplate;
import gameserver.model.skills.effects.EffectType;
import gameserver.model.stats.Env;
import gameserver.network.serverpackets.FinishRotatings;
import gameserver.network.serverpackets.StartRotation;

public class Bluff extends Effect
{
	public Bluff(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public EffectType getEffectType()
	{
		return EffectType.NONE;
	}
	
	@Override
	public boolean onStart()
	{
		final var effected = getEffected();
		final var effector = getEffector();
		if (effector == null || effected == null || effected.getId() == 35062 || effected instanceof SiegeSummonInstance || effected.isRaid() || effected.isRaidMinion())
		{
			return false;
		}
		effected.broadcastPacket(new StartRotation(effected.getObjectId(), effected.getHeading(), 1, 65535), new FinishRotatings(effected.getObjectId(), effector.getHeading(), 65535));
		effected.setHeading(effector.getHeading());
		return true;
	}
}