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

import gameserver.instancemanager.ReflectionManager;
import gameserver.model.Location;
import gameserver.model.skills.effects.Effect;
import gameserver.model.skills.effects.EffectTemplate;
import gameserver.model.skills.effects.EffectType;
import gameserver.model.stats.Env;

public class Teleport extends Effect
{
	private final Location _loc;

	public Teleport(Env env, EffectTemplate template)
	{
		super(env, template);
		_loc = new Location(template.getParameters().getInteger("x", 0), template.getParameters().getInteger("y", 0), template.getParameters().getInteger("z", 0));
	}

	@Override
	public EffectType getEffectType()
	{
		return EffectType.TELEPORT;
	}
	
	@Override
	public boolean onStart()
	{
		if (getEffected().isPlayer() && getEffected().getActingPlayer().isInKrateisCube())
		{
			getEffected().getActingPlayer().getArena().removePlayer(getEffected().getActingPlayer());
		}
		
		getEffected().getActingPlayer().setIsIn7sDungeon(false);
		getEffected().teleToLocation(_loc, true, ReflectionManager.DEFAULT);
		return true;
	}
}