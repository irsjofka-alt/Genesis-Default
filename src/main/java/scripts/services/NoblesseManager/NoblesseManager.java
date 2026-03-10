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
package services.NoblesseManager;

import gameserver.data.parser.ItemsParser;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.olympiad.Olympiad;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.NpcHtmlMessage;
import scripts.ai.AbstractNpcAI;

/**
 * Rework by LordWinter
 */
public class NoblesseManager extends AbstractNpcAI
{
	private final static int NPC = 9999;
	private static final int ItemId = 4037;
	private static final int ItemAmount = 1;
	private static final int Level = 76;
	
	public NoblesseManager()
	{
		addStartNpc(NPC);
		addFirstTalkId(NPC);
		addTalkId(NPC);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (player.getLevel() < Level)
		{
			player.sendMessage("Your level is too low to use this function, you must be at least " + Level + " level.");
			sendMainHtmlWindow(player, npc);
			return "";
		}
		
		if (event.startsWith("noblesse"))
		{
			if (player.isNoble())
			{
				player.sendMessage("You are already noblesse.");
				sendMainHtmlWindow(player, npc);
				return "";
			}
			
			if (player.destroyItemByItemId("noblesse", ItemId, ItemAmount, player, true))
			{
				player.addItem("Tiara", 7694, 1, null, true);
				player.setNoble(true);
				player.sendUserInfo(true);
				Olympiad.addNoble(player);
				player.sendMessage("Congratulations! You are now Noblesse!");
			}
			else
			{
				player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			}
		}
		sendMainHtmlWindow(player, npc);
		return "";
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		sendMainHtmlWindow(player, npc);
		return "";
	}
	
	private void sendMainHtmlWindow(Player player, Npc npc)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
		html.setFile(player, player.getLang(), "data/html/scripts/services/NoblesseManager/main.htm");
		html.replace("%player%", player.getName(null));
		html.replace("%itemAmount%", ItemAmount);
		html.replace("%itemName%", ItemsParser.getInstance().getTemplate(ItemId).getName(player.getLang()));
		html.replace("%minimumLevel%", Level);
		player.sendPacket(html);
	}
	
	public static void main(final String[] args)
	{
		new NoblesseManager();
	}
}