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
package scripts.hellbound;

import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.quest.Quest;
import gameserver.model.quest.QuestState;

public class Deltuva extends Quest
{
	private Deltuva()
	{
		super(-1);
		
		addStartNpc(32313);
		addTalkId(32313);
	}
	
	@Override
	public final String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = null;
		if (event.equalsIgnoreCase("teleport"))
		{
			final QuestState hostQuest = player.getQuestState("_132_MatrasCuriosity");
			if ((hostQuest == null) || !hostQuest.isCompleted())
			{
				htmltext = "32313-02.htm";
			}
			else
			{
				player.teleToLocation(17934, 283189, -9701, true, player.getReflection());
			}
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new Deltuva();
	}
}