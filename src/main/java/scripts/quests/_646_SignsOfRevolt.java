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
package scripts.quests;

import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.quest.Quest;
import gameserver.model.quest.QuestState;

/**
 * Created by LordWinter 20.06.2012 Based on L2J Eternity-World
 */
public final class _646_SignsOfRevolt extends Quest
{
	private _646_SignsOfRevolt()
	{
		super(646);
		
		addStartNpc(32016);
		addTalkId(32016);
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		final String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		if (npc.getId() == 32016)
		{
			st.exitQuest(true);
		}
		return "32016-00.htm";
	}
	
	public static void main(String[] args)
	{
		new _646_SignsOfRevolt();
	}
}