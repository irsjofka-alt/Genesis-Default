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
package gameserver.handler.bypasshandlers.impl;

import java.util.List;

import gameserver.handler.bypasshandlers.IBypassHandler;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.quest.Quest;
import gameserver.model.quest.QuestEventType;

public class ChatLink implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
	        "Chat"
	};

	@Override
	public boolean useBypass(String command, Player activeChar, Creature target)
	{
		if (!target.isNpc())
		{
			return false;
		}
		
		int val = 0;
		try
		{
			val = Integer.parseInt(command.substring(5));
		}
		catch (final Exception _)
		{}

		final Npc npc = (Npc) target;
		final List<Quest> firstTalk = npc.getTemplate().getEventQuests(QuestEventType.ON_FIRST_TALK);
		if ((val == 0) && (firstTalk != null) && (firstTalk.size() == 1))
		{
			firstTalk.getFirst().notifyFirstTalk(npc, activeChar);
		}
		else
		{
			npc.showChatWindow(activeChar, val);
		}
		return false;
	}
	
	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}