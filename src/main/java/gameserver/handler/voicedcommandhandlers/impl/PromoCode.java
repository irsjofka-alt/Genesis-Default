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
package gameserver.handler.voicedcommandhandlers.impl;

import java.util.StringTokenizer;

import gameserver.Config;
import gameserver.data.htm.HtmCache;
import gameserver.data.parser.ItemsParser;
import gameserver.data.parser.PremiumAccountsParser;
import gameserver.data.parser.PromoCodeParser;
import gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import gameserver.model.actor.Player;
import gameserver.model.actor.templates.promocode.PromoCodeTemplate;
import gameserver.model.actor.templates.promocode.impl.AbstractCodeReward;
import gameserver.model.actor.templates.promocode.impl.AddLevelCodeReward;
import gameserver.model.actor.templates.promocode.impl.ExpCodeReward;
import gameserver.model.actor.templates.promocode.impl.FameCodeReward;
import gameserver.model.actor.templates.promocode.impl.ItemCodeReward;
import gameserver.model.actor.templates.promocode.impl.PcPointCodeReward;
import gameserver.model.actor.templates.promocode.impl.PremiumCodeReward;
import gameserver.model.actor.templates.promocode.impl.ReputationCodeReward;
import gameserver.model.actor.templates.promocode.impl.SetLevelCodeReward;
import gameserver.model.actor.templates.promocode.impl.SpCodeReward;
import gameserver.model.strings.server.ServerMessage;
import gameserver.network.serverpackets.MagicSkillUse;
import gameserver.network.serverpackets.NpcHtmlMessage;
import gameserver.utils.TimeUtils;
import gameserver.utils.Util;

public class PromoCode implements IVoicedCommandHandler
{
	private static final String[] _voicedCommands =
	{
	        "promo", "promocode", "code", "bonus", "giveCode"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player player, String target)
	{
		if (!Config.ALLOW_PROMOCODES_COMMAND)
		{
			return false;
		}
		
		if ("promo".equalsIgnoreCase(command) || "promocode".equalsIgnoreCase(command) || "code".equalsIgnoreCase(command) || "bonus".equalsIgnoreCase(command))
		{
			if (target != null && !(target.isEmpty()))
			{
				final StringTokenizer st = new StringTokenizer(target);
				String code = null;
				if (st.hasMoreTokens())
				{
					code = st.nextToken();
				}
				
				if (!player.checkFloodProtection("PROMOCODE", "promocode_delay"))
				{
					return false;
				}
				
				if (!PromoCodeParser.getInstance().isValidCheckTime(player, false))
				{
					showMainMenu(player);
					return false;
				}

				if (code != null)
				{
					final PromoCodeTemplate tpl = PromoCodeParser.getInstance().getPromoCode(code);
					if (tpl != null)
					{
						if (!PromoCodeParser.getInstance().isActivePromoCode(tpl, player, false))
						{
							showMainMenu(player);
							return false;
						}
						
						String html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/mods/promocodes/info.htm");
						final String template = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/mods/promocodes/template.htm");
						String block = "";
						String list = "";
						
						for (final AbstractCodeReward reward : tpl.getRewards())
						{
							block = template;
							block = block.replace("%icon%", reward.getIcon());
							switch (reward) {
								case AddLevelCodeReward codeReward8 -> {
									final ServerMessage msg = new ServerMessage("PromoCode.ADD_LEVEL", player.getLang());
									msg.add(codeReward8.getLevel());
									block = block.replace("%data%", msg.toString());
								}
								case ExpCodeReward codeReward7 -> {
									final ServerMessage msg = new ServerMessage("PromoCode.ADD_EXP", player.getLang());
									msg.add(codeReward7.getExp());
									block = block.replace("%data%", msg.toString());
								}
								case ItemCodeReward codeReward6 -> {
									final ServerMessage msg = new ServerMessage("PromoCode.ADD_ITEM", player.getLang());
									msg.add(ItemsParser.getInstance().getTemplate(codeReward6.getItemId()).getName(player.getLang()));
									msg.add(codeReward6.getCount());
									block = block.replace("%data%", msg.toString());
								}
								case PremiumCodeReward codeReward5 -> {
									final ServerMessage msg = new ServerMessage("PromoCode.ADD_PREMIUM", player.getLang());
									msg.add(TimeUtils.formatTime(player, (int) PremiumAccountsParser.getInstance().getPremiumTemplate(codeReward5.getPremiumId()).getTime()));
									block = block.replace("%data%", msg.toString());
								}
								case SetLevelCodeReward codeReward4 -> {
									final ServerMessage msg = new ServerMessage("PromoCode.CHANGE_LEVEL", player.getLang());
									msg.add(codeReward4.getLevel());
									block = block.replace("%data%", msg.toString());
								}
								case SpCodeReward codeReward3 -> {
									final ServerMessage msg = new ServerMessage("PromoCode.ADD_SP", player.getLang());
									msg.add(codeReward3.getSp());
									block = block.replace("%data%", msg.toString());
								}
								case FameCodeReward codeReward2 -> {
									final ServerMessage msg = new ServerMessage("PromoCode.ADD_FAME", player.getLang());
									msg.add(codeReward2.getFame());
									block = block.replace("%data%", msg.toString());
								}
								case PcPointCodeReward codeReward1 -> {
									final ServerMessage msg = new ServerMessage("PromoCode.ADD_PC_POINTS", player.getLang());
									msg.add(codeReward1.getPcPoints());
									block = block.replace("%data%", msg.toString());
								}
								case ReputationCodeReward codeReward -> {
									final ServerMessage msg = new ServerMessage("PromoCode.ADD_REPUTATION", player.getLang());
									msg.add(codeReward.getReputation());
									block = block.replace("%data%", msg.toString());
								}
								default -> {}
							}
							list += block;
						}
						
						html = html.replace("%name%", tpl.getName());
						html = html.replace("%list%", list);
						Util.setHtml(html, player);
					}
					else
					{
						showMainMenu(player);
						player.sendMessage((new ServerMessage("PromoCode.WRONG_PROMO", player.getLang())).toString());
						return false;
					}
				}
				else
				{
					showMainMenu(player);
					player.sendMessage((new ServerMessage("PromoCode.WRONG_PROMO", player.getLang())).toString());
					return false;
				}
			}
			else
			{
				showMainMenu(player);
				return true;
			}
		}
		else if ("giveCode".equalsIgnoreCase(command))
		{
			if (target != null && !(target.isEmpty()))
			{
				final StringTokenizer st = new StringTokenizer(target);
				String code = null;
				if (st.hasMoreTokens())
				{
					code = st.nextToken();
				}
				
				if (!player.checkFloodProtection("PROMOCODE", "promocode_delay"))
				{
					return false;
				}
				
				if (!PromoCodeParser.getInstance().isValidCheckTime(player, true))
				{
					return false;
				}
				
				if (code != null)
				{
					final PromoCodeTemplate tpl = PromoCodeParser.getInstance().getPromoCode(code);
					if (tpl != null)
					{
						if (PromoCodeParser.getInstance().isActivePromoCode(tpl, player, true))
						{
							for (final AbstractCodeReward reward : tpl.getRewards())
							{
								reward.giveReward(player);
							}
							player.broadcastPacket(new MagicSkillUse(player, player, 6234, 1, 1000, 0));
						}
					}
				}
			}
		}
		return true;
	}
	
	private void showMainMenu(Player player)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(player.getObjectId());
		html.setFile(player, player.getLang(), "data/html/mods/promocodes/index.htm");
		player.sendPacket(html);
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}