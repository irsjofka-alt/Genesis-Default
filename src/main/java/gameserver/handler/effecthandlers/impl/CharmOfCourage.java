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

import gameserver.model.skills.effects.Effect;
import gameserver.model.skills.effects.EffectFlag;
import gameserver.model.skills.effects.EffectTemplate;
import gameserver.model.skills.effects.EffectType;
import gameserver.model.stats.Env;
import gameserver.network.serverpackets.EtcStatusUpdate;

public class CharmOfCourage extends Effect
{
	public CharmOfCourage(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public int getEffectFlags()
	{
		return EffectFlag.CHARM_OF_COURAGE.getMask();
	}

	@Override
	public EffectType getEffectType()
	{
		return EffectType.CHARMOFCOURAGE;
	}

	@Override
	public boolean onStart()
	{
		if (getEffected().isPlayer())
		{
			getEffected().sendPacket(new EtcStatusUpdate(getEffected().getActingPlayer()));
			return true;
		}
		return false;
	}

	@Override
	public void onExit()
	{
		if (getEffected().isPlayer())
		{
			getEffected().sendPacket(new EtcStatusUpdate(getEffected().getActingPlayer()));
		}
	}
}