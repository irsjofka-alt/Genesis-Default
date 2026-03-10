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

public class Survivor extends Quest
{
	private Survivor()
	{
		super(-1);
		
		addStartNpc(32632);
		addTalkId(32632);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}
		
		if (!event.isEmpty())
		{
			if (player.getLevel() < 75)
			{
				event = "32632-3.htm";
			}
			else if (st.getQuestItemsCount(57) >= 150000)
			{
				st.takeItems(57, 150000);
				player.teleToLocation(-149406, 255247, -80, true, player.getReflection());
			}
		}
		
		return event;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return null;
		}
		return "32632-1.htm";
	}
	
	public static void main(String[] args)
	{
		new Survivor();
	}
}