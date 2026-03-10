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

import gameserver.instancemanager.HellboundManager;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.quest.Quest;

public class Solomon extends Quest
{
	private static final int SOLOMON = 32355;
	
	@Override
	public final String onFirstTalk(Npc npc, Player player)
	{
		if (HellboundManager.getInstance().getLevel() == 5)
		{
			return "32355-01.htm";
		}
		else if (HellboundManager.getInstance().getLevel() > 5)
		{
			return "32355-01a.htm";
		}
		return null;
	}
	
	private Solomon()
	{
		super(-1);
		
		addFirstTalkId(SOLOMON);
	}
	
	public static void main(String[] args)
	{
		new Solomon();
	}
}