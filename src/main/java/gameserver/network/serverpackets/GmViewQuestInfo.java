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

import java.util.Collection;

import gameserver.model.actor.Player;
import gameserver.model.quest.Quest;

public class GmViewQuestInfo extends GameServerPacket
{
	private final Player _player;
	private final Collection<Quest> _questList;
	
	public GmViewQuestInfo(Player player)
	{
		_player = player;
		_questList = player.getAllActiveQuests();
	}

	@Override
	protected final void writeImpl()
	{
		writeS(_player.getName(null));
		writeH(_questList.size());
		for (final var q : _questList)
		{
			final var qs = _player.getQuestState(q.getName());
			writeD(q.getId());
			writeD(qs == null ? 0 : qs.getCond());
		}
	}
}