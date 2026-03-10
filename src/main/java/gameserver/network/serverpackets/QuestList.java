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
package gameserver.network.serverpackets;

import java.util.LinkedList;
import java.util.List;

import gameserver.model.actor.Player;
import gameserver.model.quest.QuestState;

public class QuestList extends GameServerPacket
{
	private final List<QuestState> _quests;
	private final byte[] _oneTimeQuestMask;
	
	public QuestList(Player player)
	{
		_quests = new LinkedList<>();
		_oneTimeQuestMask = new byte[128];
		for (final var q : player.getAllQuestStates())
		{
			final int questId = q.getQuest().getId();
			if (((questId < 999 || questId > 10000) && questId > 0) && questId != 255)
			{
				if (q.isStarted())
				{
					_quests.add(q);
				}
				else if (q.isCompleted())
				{
					_oneTimeQuestMask[(questId % 10000) / 8] |= 1 << (questId % 8);
				}
			}
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeH(_quests.size());
		for (final var qs : _quests)
		{
			writeD(qs.getQuest().getId());
			final int states = qs.getInt("__compltdStateFlags");
			writeD(states != 0 ? states : qs.getCond());
		}
		writeB(_oneTimeQuestMask);
	}
}