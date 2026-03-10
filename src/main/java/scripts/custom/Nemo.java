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
package scripts.custom;

import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.quest.Quest;

/**
 * Created by LordWinter 22.06.2012
 * Based on L2J Eternity-World
 */
public class Nemo extends Quest
{
	private static final int _nemo = 32735;

	public Nemo()
	{
		super(-1);
		
		addStartNpc(_nemo);
		addFirstTalkId(_nemo);
		addTalkId(_nemo);
	}
		
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		var st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}

		if (event.equalsIgnoreCase("request_collector"))
		{
			if(st.getQuestItemsCount(15487) > 0)
			{
				htmltext = "32735-2.htm";
			}
			else
			{
				player.addItem("Maguen", 15487, 1, null, true);
				return null;
			}
		}
		return htmltext;
	}

	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		var st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}

		if (npc.getId() == _nemo)
		{
			return "32735.htm";
		}
		return "";
	}
	
	void main()
	{
		new Nemo();
	}
}