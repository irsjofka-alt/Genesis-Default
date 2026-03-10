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
package gameserver.handler.voicedcommandhandlers.impl;

import gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import gameserver.model.actor.Player;
import gameserver.model.entity.events.custom.achievements.AchievementManager;

public class Achievement implements IVoicedCommandHandler
{
	private static final String[] _voicedCommands =
	{
	        "ach", "acv"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player player, String target)
	{
		if (!AchievementManager.getInstance().isActive())
		{
			return false;
		}
		
		AchievementManager.getInstance().onBypass(player, "_bbs_achievements", null);
		return true;
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}