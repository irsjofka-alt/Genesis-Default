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
package scripts.ai;

import gameserver.ai.npc.Fighter;
import gameserver.instancemanager.QuestManager;
import gameserver.model.World;
import gameserver.model.actor.Attackable;
import gameserver.network.NpcStringId;
import gameserver.network.clientpackets.Say2;
import gameserver.network.serverpackets.NpcSay;

/**
 * Created by LordWinter 01.08.2022
 */
public class Dorian extends Fighter
{
	public Dorian(Attackable actor)
	{
		super(actor);
	}

	@Override
	protected boolean thinkActive()
	{
		final var q = QuestManager.getInstance().getQuest(24);
		if (q != null)
		{
			for (final var player : World.getAroundPlayers(getActiveChar(), 300, 200))
			{
				final var st = player.getQuestState(q.getName());
				if(st != null && st.isCond(3) && st.getQuestItemsCount(7153) >= 1)
				{
					st.takeItems(7153, -1);
					st.giveItems(7154, 1);
					st.setCond(4, true);
					getActiveChar().broadcastPacketToOthers(2000, new NpcSay(getActiveChar().getObjectId(), Say2.NPC_ALL, getActiveChar().getId(), NpcStringId.THAT_SIGN));
				}
			}
		}
		return super.thinkActive();
	}
}