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
package services.WeeklyTrader;

import gameserver.Config;
import gameserver.data.parser.MultiSellParser;
import gameserver.instancemanager.WeeklyTraderManager;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.network.serverpackets.NpcHtmlMessage;
import gameserver.utils.TimeUtils;
import scripts.ai.AbstractNpcAI;

public class WeeklyTrader extends AbstractNpcAI
{
	public WeeklyTrader()
	{
		addTalkId(WeeklyTraderManager.getInstance().getValidNpcId());
		addStartNpc(WeeklyTraderManager.getInstance().getValidNpcId());
		addFirstTalkId(WeeklyTraderManager.getInstance().getValidNpcId());
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (WeeklyTraderManager.getInstance().isTradingPeriod() && event.contains("trade"))
		{
			MultiSellParser.getInstance().separateAndSend(Config.WEEKLY_TRADER_MULTISELL_ID, player, npc, false);
		}
		return "";
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
		if (WeeklyTraderManager.getInstance().isTradingPeriod())
		{
			final long timeLeft = (WeeklyTraderManager.getInstance().getEndingTime() - System.currentTimeMillis()) / 1000;
			html.setFile(player, player.getLang(), "data/html/scripts/services/WeeklyTrader/periodon.htm");
			html.replace("%time%", TimeUtils.formatTime(player, (int) timeLeft, false));
			player.sendPacket(html);
		}
		else
		{
			final long timeLeft = (WeeklyTraderManager.getInstance().getStartTime() - System.currentTimeMillis()) / 1000;
			html.setFile(player, player.getLang(), "data/html/scripts/services/WeeklyTrader/periodoff.htm");
			html.replace("%time%", TimeUtils.formatTime(player, (int) timeLeft, false));
			player.sendPacket(html);
		}
		return "";
	}
	
	public static void main(String[] args)
	{
		new WeeklyTrader();
	}
}
