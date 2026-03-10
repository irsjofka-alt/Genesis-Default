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

import gameserver.data.parser.TransformParser;
import gameserver.model.skills.effects.Effect;
import gameserver.model.skills.effects.EffectTemplate;
import gameserver.model.skills.effects.EffectType;
import gameserver.model.stats.Env;

public class Transformation extends Effect
{
	public Transformation(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	public Transformation(Env env, Effect effect)
	{
		super(env, effect);
	}
	
	@Override
	public boolean canBeStolen()
	{
		return false;
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.TRANSFORMATION;
	}
	
	@Override
	public void onExit()
	{
		getEffected().stopTransformation(false);
	}
	
	@Override
	public boolean onStart()
	{
		if (!getEffected().isPlayer())
		{
			return false;
		}
		return TransformParser.getInstance().transformPlayer((int) calc(), getEffected().getActingPlayer());
	}
}