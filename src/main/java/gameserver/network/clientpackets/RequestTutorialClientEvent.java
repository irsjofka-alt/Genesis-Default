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
package gameserver.network.clientpackets;

import gameserver.Config;
import gameserver.model.actor.Player;
import gameserver.model.quest.QuestState;

public class RequestTutorialClientEvent extends GameClientPacket
{
	int eventId = 0;

	@Override
	protected void readImpl()
	{
		eventId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();

		if (player == null)
		{
			return;
		}

		if (!Config.DISABLE_TUTORIAL)
		{
			final QuestState qs = player.getQuestState("_255_Tutorial");
			if (qs != null)
			{
				qs.getQuest().notifyEvent("CE" + eventId + "", null, player);
			}
		}
	}
}