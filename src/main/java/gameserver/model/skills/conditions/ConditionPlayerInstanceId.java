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

import java.util.ArrayList;

import gameserver.model.stats.Env;

public class ConditionPlayerInstanceId extends Condition
{
	private final ArrayList<Integer> _instanceIds;
	
	public ConditionPlayerInstanceId(ArrayList<Integer> instanceIds)
	{
		_instanceIds = instanceIds;
	}

	@Override
	public boolean testImpl(Env env)
	{
		final var player = env.getPlayer();
		if (player == null)
		{
			return false;
		}
		final var r = player.getReflection();
		if (r.isDefault())
		{
			return false;
		}
		return _instanceIds.contains(r.getTemplateId());
	}
}