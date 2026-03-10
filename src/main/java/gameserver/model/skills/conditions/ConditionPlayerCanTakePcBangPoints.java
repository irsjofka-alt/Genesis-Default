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

import gameserver.Config;
import gameserver.model.actor.Player;
import gameserver.model.stats.Env;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.SystemMessage;

public class ConditionPlayerCanTakePcBangPoints extends Condition
{
	private final boolean _val;
	
	public ConditionPlayerCanTakePcBangPoints(boolean val)
	{
		_val = val;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		boolean canTakePoints = true;
		final Player player = env.getPlayer();
		if (player == null)
		{
			canTakePoints = false;
		}
		else if (player.getPcBangPoints() >= Config.MAX_PC_BANG_POINTS)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_MAXMIMUM_ACCUMULATION_ALLOWED_OF_PC_CAFE_POINTS_HAS_BEEN_EXCEEDED));
			canTakePoints = false;
		}
		return (_val == canTakePoints);
	}
}