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

public class Budenka extends Quest
{
	private static final int BUDENKA = 32294;
	private static final int STANDART_CERT = 9851;
	private static final int PREMIUM_CERT = 9852;
	
	@Override
	public final String onFirstTalk(Npc npc, Player player)
	{
		if (player.getInventory().getInventoryItemCount(PREMIUM_CERT, -1, false) > 0)
		{
			return "32294-premium.htm";
		}
		if (player.getInventory().getInventoryItemCount(STANDART_CERT, -1, false) > 0)
		{
			return "32294-standart.htm";
		}
		
		npc.showChatWindow(player);
		return null;
	}
	
	private Budenka()
	{
		super(-1);
		
		addFirstTalkId(BUDENKA);
	}
	
	public static void main(String[] args)
	{
		new Budenka();
	}
}