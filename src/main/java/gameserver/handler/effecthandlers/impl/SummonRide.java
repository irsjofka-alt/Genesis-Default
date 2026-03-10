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

import gameserver.data.parser.NpcsParser;
import gameserver.model.skills.effects.Effect;
import gameserver.model.skills.effects.EffectTemplate;
import gameserver.model.skills.effects.EffectType;
import gameserver.model.stats.Env;

public class SummonRide extends Effect
{
	private final int _npcId;
	
	public SummonRide(Env env, EffectTemplate template)
	{
		super(env, template);
		
		_npcId = template.getParameters().getInteger("npcId", 0);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.NONE;
	}
	
	@Override
	public boolean onStart()
	{
		if ((getEffected() == null) || !getEffected().isPlayer() || getEffected().isAlikeDead() || getEffected().getActingPlayer().inObserverMode())
		{
			return false;
		}
		
		if ((_npcId <= 0))
		{
			_log.warn(SummonRide.class.getSimpleName() + ": Invalid NPC Id at skill Id: " + getSkill().getId());
			return false;
		}
		
		final var player = getEffected().getActingPlayer();
		if (player.isMounted())
		{
			player.dismount();
		}
		
		final var npcTemplate = NpcsParser.getInstance().getTemplate(_npcId);
		if (npcTemplate == null)
		{
			_log.warn(SummonNpc.class.getSimpleName() + ": Spawn of the nonexisting NPC Id: " + _npcId + ", skill Id:" + getSkill().getId());
			return false;
		}
		
		if (player.isTransformed() || player.isInStance())
		{
			player.untransform();
		}
		
		player.mount(_npcId, 0, false);
		return true;
	}
}