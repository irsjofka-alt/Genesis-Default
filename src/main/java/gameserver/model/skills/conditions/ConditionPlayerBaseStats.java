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
package gameserver.model.skills.conditions;

import gameserver.model.actor.Creature;
import gameserver.model.actor.Player;
import gameserver.model.stats.Env;

public class ConditionPlayerBaseStats extends Condition
{
	private final BaseStat _stat;
	private final int _value;
	
	public ConditionPlayerBaseStats(Creature player, BaseStat stat, int value)
	{
		super();
		_stat = stat;
		_value = value;
	}

	@Override
	public boolean testImpl(Env env)
	{
		if (env.getPlayer() == null)
		{
			return false;
		}
		final Player player = env.getPlayer();
		return switch (_stat)
		{
			case Int  -> player.getINT() >= _value;
			case Str  -> player.getSTR() >= _value;
			case Con  -> player.getCON() >= _value;
			case Dex  -> player.getDEX() >= _value;
			case Men  -> player.getMEN() >= _value;
			case Wit  -> player.getWIT() >= _value;
		};
	}
}

enum BaseStat
{
	Int, Str, Con, Dex, Men, Wit
}
