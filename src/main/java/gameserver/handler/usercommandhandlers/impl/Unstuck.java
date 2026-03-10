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
package gameserver.handler.usercommandhandlers.impl;

import gameserver.ai.model.CtrlIntention;
import gameserver.data.parser.SkillsParser;
import gameserver.handler.usercommandhandlers.IUserCommandHandler;
import gameserver.model.actor.Player;
import gameserver.model.entity.events.cleft.AerialCleftEvent;

public class Unstuck implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
	        52
	};

	@Override
	public boolean useUserCommand(int id, Player activeChar)
	{
		for (final var e : activeChar.getFightEvents())
		{
			if (e != null && !e.canUseEscape(activeChar))
			{
				activeChar.sendActionFailed();
				return false;
			}
		}

		if (!AerialCleftEvent.getInstance().onEscapeUse(activeChar.getObjectId()))
		{
			activeChar.sendActionFailed();
			return false;
		}

		if (activeChar.isJailed())
		{
			activeChar.sendMessage("You cannot use this function while you are jailed.");
			return false;
		}

		if (activeChar.isCastingNow() || activeChar.isAfraid() || activeChar.isMuted() || activeChar.isMovementDisabled() || activeChar.isMuted() || activeChar.isAlikeDead() || activeChar.isInOlympiadMode() || activeChar.inObserverMode() || activeChar.isCombatFlagEquipped())
		{
			return false;
		}
		
		final var escape = SkillsParser.getInstance().getInfo(activeChar.isGM() ? 2100 : 2099, 1);
		if (escape != null)
		{
			activeChar.getAI().setIntention(CtrlIntention.IDLE);
			activeChar.doCast(escape);
		}
		return true;
	}

	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}