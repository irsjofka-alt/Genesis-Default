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
package scripts.teleports;

import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.quest.Quest;
import gameserver.model.quest.QuestState;

public class TeleportWithCharm extends Quest
{
	private TeleportWithCharm()
	{
		super(-1);
		
		addStartNpc(30540, 30576);
		addTalkId(30540, 30576);
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		String htmltext = "";
		final QuestState st = player.getQuestState(getName());
		
		if (st == null)
		{
			return null;
		}
		
		switch (npc.getId())
		{
			case 30540 :
			{
				if (st.hasQuestItems(1659))
				{
					st.takeItems(1659, 1);
					player.teleToLocation(-80826, 149775, -3043, true, player.getReflection());
					st.exitQuest(true);
				}
				else
				{
					st.exitQuest(true);
					htmltext = "30540-01.htm";
				}
				break;
			}
			case 30576 :
			{
				if (st.hasQuestItems(1658))
				{
					st.takeItems(1658, 1);
					player.teleToLocation(-80826, 149775, -3043, true, player.getReflection());
					st.exitQuest(true);
				}
				else
				{
					st.exitQuest(true);
					htmltext = "30576-01.htm";
				}
				break;
			}
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new TeleportWithCharm();
	}
}