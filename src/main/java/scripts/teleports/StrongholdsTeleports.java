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

public class StrongholdsTeleports extends Quest
{
	private StrongholdsTeleports()
	{
		super(-1);
		
		addFirstTalkId(32163, 32181, 32184, 32186);
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		String htmltext = "";
		if (player.getLevel() < 20)
		{
			htmltext = String.valueOf(npc.getId()) + ".htm";
		}
		else
		{
			htmltext = String.valueOf(npc.getId()) + "-no.htm";
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new StrongholdsTeleports();
	}
}