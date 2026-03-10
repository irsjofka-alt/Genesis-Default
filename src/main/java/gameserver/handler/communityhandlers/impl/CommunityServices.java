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
package gameserver.handler.communityhandlers.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import l2e.commons.dbutils.DbUtils;
import gameserver.Config;
import gameserver.data.holder.ClanHolder;
import gameserver.data.htm.HtmCache;
import gameserver.data.parser.ItemsParser;
import gameserver.data.parser.SkillsParser;
import gameserver.database.DatabaseFactory;
import gameserver.handler.communityhandlers.ICommunityBoardHandler;
import gameserver.instancemanager.DoubleSessionManager;
import gameserver.instancemanager.mods.TimeSkillsTaskManager;
import gameserver.listener.player.OnAnswerListener;
import gameserver.listener.player.impl.ChangeColorAnswerListener;
import gameserver.model.Clan;
import gameserver.model.actor.Player;
import gameserver.model.actor.templates.DonateSkillTemplate;
import gameserver.model.actor.templates.TimeSkillTemplate;
import gameserver.model.actor.templates.items.Item;
import gameserver.model.holders.ItemHolder;
import gameserver.model.strings.server.ServerMessage;
import gameserver.model.strings.server.ServerStorage;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.ExPCCafePointInfo;
import gameserver.network.serverpackets.NpcHtmlMessage;
import gameserver.network.serverpackets.ShowBoard;
import gameserver.network.serverpackets.SystemMessage;
import gameserver.utils.Util;
import gameserver.utils.comparators.TimeComparator;

public class CommunityServices extends AbstractCommunity implements ICommunityBoardHandler
{
	public CommunityServices()
	{
		if (Config.DEBUG)
		{
			_log.info(getClass().getSimpleName() + ": Loading all functions.");
		}
	}
	
	@Override
	public String[] getBypassCommands()
	{
		return new String[]
		{
		        "_bbs_service"
		};
	}
	
	@Override
	public void onBypassCommand(String command, Player activeChar)
	{
		if (!checkCondition(activeChar, new StringTokenizer(command, "_").nextToken(), false, false))
		{
			return;
		}
		
		if (command.startsWith("_bbs_service;nickname"))
		{
			changeName(activeChar);
		}
		else if (command.startsWith("_bbs_service;pledgename"))
		{
			changePledgeName(activeChar);
		}
		else if (command.startsWith("_bbs_service;nickcolor"))
		{
			changeNameColor(activeChar);
		}
		else if (command.startsWith("_bbs_service;titlecolor"))
		{
			changeTitleColor(activeChar);
		}
		else if (command.startsWith("_bbs_service;hardWareList"))
		{
			if (!Config.DOUBLE_SESSIONS_HWIDS)
			{
				return;
			}
			hardWareList(activeChar);
		}
		
		final StringTokenizer st = new StringTokenizer(command, " ");
		final String curCommand = st.nextToken();
		
		if (curCommand.startsWith("_bbs_service;timeskills"))
		{
			String isClan = null;
			String page = null;
			try
			{
				isClan = st.nextToken();
			}
			catch (final Exception _)
			{
			}
			
			try
			{
				page = st.nextToken();
			}
			catch (final Exception _)
			{
			}
			
			if (page != null && isClan != null)
			{
				timeSkillList(activeChar, Integer.valueOf(page), Integer.valueOf(isClan) == 1, false);
			}
		}
		else if (curCommand.startsWith("_bbs_service;infotimeskills"))
		{
			String id = null;
			String level = null;
			String time = null;
			String isClan = null;
			String genPage = null;
			String curPage = null;
			try
			{
				id = st.nextToken();
			}
			catch (final Exception _)
			{
			}
			
			try
			{
				level = st.nextToken();
			}
			catch (final Exception _)
			{
			}
			
			try
			{
				time = st.nextToken();
			}
			catch (final Exception _)
			{
			}
			
			try
			{
				isClan = st.nextToken();
			}
			catch (final Exception _)
			{
			}
			
			try
			{
				genPage = st.nextToken();
			}
			catch (final Exception _)
			{
			}
			
			try
			{
				curPage = st.nextToken();
			}
			catch (final Exception _)
			{
			}
			
			if (id != null && level != null && time != null && isClan != null && curPage != null && genPage != null)
			{
				final var tpl = TimeSkillsTaskManager.getInstance().getDonateSkill(Integer.parseInt(id), Integer.parseInt(level));
				if (tpl != null)
				{
					final int page = Integer.parseInt(curPage);
					final long times = Long.parseLong(time);
					if (!tpl.getTimes().contains(times))
					{
						return;
					}
					
					final List<ItemHolder> requestItems = tpl.getRequestItems().get(times);
					
					final int perpage = 4;
					final boolean isThereNextPage = requestItems.size() > perpage;
					
					final NpcHtmlMessage html = new NpcHtmlMessage(5);
					html.setFile(activeChar, activeChar.getLang(), "data/html/community/donate/timeSkills/items.htm");
					final String template = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/community/donate/timeSkills/items-template.htm");
					
					String block = "";
					String list = "";
					
					int countss = 0;
					
					for (int i = (page - 1) * perpage; i < requestItems.size(); i++)
					{
						final ItemHolder rItem = requestItems.get(i);
						if (rItem != null)
						{
							block = template;
							block = block.replace("%name%", Util.getItemName(activeChar, rItem.getId()));
							block = block.replace("%icon%", Util.getItemIcon(rItem.getId()));
							if (rItem.getId() == -300)
							{
								if (activeChar.getFame() < rItem.getCount())
								{
									block = block.replace("%amount%", "<font color=\"b02e31\">" + Util.getAmountFormat(rItem.getCount(), activeChar.getLang()) + "</font>");
								}
								else
								{
									block = block.replace("%amount%", "<font color=\"259a30\">" + Util.getAmountFormat(rItem.getCount(), activeChar.getLang()) + "</font>");
								}
							}
							else if (rItem.getId() == -200)
							{
								if (activeChar.getClan() == null || activeChar.getClan().getReputationScore() < rItem.getCount())
								{
									block = block.replace("%amount%", "<font color=\"b02e31\">" + Util.getAmountFormat(rItem.getCount(), activeChar.getLang()) + "</font>");
								}
								else
								{
									block = block.replace("%amount%", "<font color=\"259a30\">" + Util.getAmountFormat(rItem.getCount(), activeChar.getLang()) + "</font>");
								}
							}
							else if (rItem.getId() == -100)
							{
								if (activeChar.getPcBangPoints() < rItem.getCount())
								{
									block = block.replace("%amount%", "<font color=\"b02e31\">" + Util.getAmountFormat(rItem.getCount(), activeChar.getLang()) + "</font>");
								}
								else
								{
									block = block.replace("%amount%", "<font color=\"259a30\">" + Util.getAmountFormat(rItem.getCount(), activeChar.getLang()) + "</font>");
								}
							}
							else
							{
								if (activeChar.getInventory().getItemByItemId(rItem.getId()) == null || !activeChar.getInventory().haveItemsCountNotEquip(rItem.getId(), rItem.getCount()))
								{
									block = block.replace("%amount%", "<font color=\"b02e31\">" + Util.getAmountFormat(rItem.getCount(), activeChar.getLang()) + "</font>");
								}
								else
								{
									block = block.replace("%amount%", "<font color=\"259a30\">" + Util.getAmountFormat(rItem.getCount(), activeChar.getLang()) + "</font>");
								}
							}
							
							list += block;
							countss++;
							
							if (countss >= perpage)
							{
								break;
							}
						}
					}
					
					final double pages = (double) tpl.getRequestItems().size() / perpage;
					final int count = (int) Math.ceil(pages);
					html.replace("%list%", list);
					html.replace("%skillId%", tpl.getSkillId());
					html.replace("%skillLvl%", tpl.getSkillLevel());
					html.replace("%isClan%", isClan);
					html.replace("%page%", genPage);
					html.replace("%time%", time);
					final var skill = SkillsParser.getInstance().getInfo(tpl.getSkillId(), tpl.getSkillLevel());
					if (skill != null)
					{
						html.replace("%descr%", skill.getDescr(activeChar.getLang()));
						html.replace("%skillIcon%", skill.getIcon());
						html.replace("%skillName%", skill.getName(activeChar.getLang()));
					}
					html.replace("%navigation%", Util.getNavigationBlock(count, page, tpl.getRequestItems().size(), perpage, isThereNextPage, "_bbs_service;infotimeskills " + tpl.getSkillId() + " " + tpl.getSkillLevel() + " " + time + " " + isClan + " " + genPage + " %s"));
					activeChar.sendPacket(html);
				}
			}
		}
		else if (curCommand.startsWith("_bbs_service;buytimeskills"))
		{
			String id = null;
			String level = null;
			String time = null;
			String isClan = null;
			String curPage = null;
			try
			{
				id = st.nextToken();
			}
			catch (final Exception _)
			{
			}
			
			try
			{
				level = st.nextToken();
			}
			catch (final Exception _)
			{
			}
			
			try
			{
				time = st.nextToken();
			}
			catch (final Exception _)
			{
			}
			
			try
			{
				isClan = st.nextToken();
			}
			catch (final Exception _)
			{
			}
			
			try
			{
				curPage = st.nextToken();
			}
			catch (final Exception _)
			{
			}
			
			if (id != null && level != null && time != null && isClan != null && curPage != null)
			{
				final var tpl = TimeSkillsTaskManager.getInstance().getDonateSkill(Integer.parseInt(id), Integer.parseInt(level));
				if (tpl != null)
				{
					final long times = Long.parseLong(time);
					if (!tpl.getTimes().contains(times))
					{
						return;
					}
					final var sk = activeChar.getKnownSkill(tpl.getSkillId());
					if (sk != null && sk.getLevel() >= tpl.getSkillLevel())
					{
						activeChar.sendMessage("You already have this skill!");
						return;
					}
					
					if (tpl.isClanSkill() && activeChar.getClan() == null)
					{
						activeChar.sendMessage("You have no clan!");
						return;
					}
					
					final List<ItemHolder> requestItems = tpl.getRequestItems().get(times);
					if (!requestItems.isEmpty())
					{
						for (final var rItem : requestItems)
						{
							if (rItem != null)
							{
								if (rItem.getId() == -300)
								{
									if (activeChar.getFame() < rItem.getCount())
									{
										activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
										return;
									}
								}
								else if (rItem.getId() == -200)
								{
									if (activeChar.getClan() == null || activeChar.getClan().getReputationScore() < rItem.getCount())
									{
										activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
										return;
									}
								}
								else if (rItem.getId() == -100)
								{
									if (activeChar.getPcBangPoints() < rItem.getCount())
									{
										activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
										return;
									}
								}
								else
								{
									if (!activeChar.getInventory().haveItemsCountNotEquip(rItem.getId(), rItem.getCount()))
									{
										activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
										return;
									}
								}
							}
						}
						
						for (final var rItem : requestItems)
						{
							if (rItem != null)
							{
								if (rItem.getId() == -300)
								{
									activeChar.setFame((int) (activeChar.getFame() - rItem.getCount()));
									activeChar.sendUserInfo();
								}
								else if (rItem.getId() == -200)
								{
									activeChar.getClan().takeReputationScore((int) rItem.getCount(), true);
									final SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
									smsg.addItemNumber(rItem.getCount());
									activeChar.sendPacket(smsg);
								}
								else if (rItem.getId() == -100)
								{
									activeChar.setPcBangPoints((int) (activeChar.getPcBangPoints() - rItem.getCount()));
									final SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.USING_S1_PCPOINT);
									smsg.addNumber((int) rItem.getCount());
									activeChar.sendPacket(smsg);
									activeChar.sendPacket(new ExPCCafePointInfo(activeChar.getPcBangPoints(), (int) rItem.getCount(), false, false, 1));
								}
								else
								{
									activeChar.destroyItemWithoutEquip("Donate Skill", rItem.getId(), rItem.getCount(), activeChar, true);
								}
							}
						}
					}
					
					final var donateTpl = new TimeSkillTemplate(tpl.isClanSkill() ? activeChar.getClan().getId() : activeChar.getObjectId(), tpl.getSkillId(), tpl.getSkillLevel(), tpl.isClanSkill());
					if (donateTpl != null)
					{
						TimeSkillsTaskManager.getInstance().addTimeSkill(donateTpl, times * 60000L);
						timeSkillList(activeChar, Integer.valueOf(curPage), Integer.parseInt(isClan) == 1, true);
						activeChar.sendMessage("You have successfully learned skill!");
					}
				}
			}
		}
		else if (curCommand.startsWith("_bbs_service;changenickname"))
		{
			String name = null;
			try
			{
				name = st.nextToken();
			}
			catch (final Exception _)
			{}
			
			if (name != null)
			{
				playerSetNickName(activeChar, name);
			}
			else
			{
				activeChar.sendMessage((new ServerMessage("ServiceBBS.NOT_ENTER_NAME", activeChar.getLang())).toString());
			}
		}
		else if (command.startsWith("_bbs_service;changepledgename"))
		{
			String name = null;
			try
			{
				name = st.nextToken();
			}
			catch (final Exception _)
			{}
			
			if (name != null)
			{
				pledgeSetName(activeChar, name);
			}
			else
			{
				activeChar.sendMessage((new ServerMessage("ServiceBBS.NOT_ENTER_NAME", activeChar.getLang())).toString());
			}
		}
		else if (curCommand.startsWith("_bbs_service;colornamelist"))
		{
			playerColorList(activeChar, 1);
		}
		else if (curCommand.startsWith("_bbs_service;colortitlelist"))
		{
			playerColorList(activeChar, 2);
		}
		else if (curCommand.startsWith("_bbs_service;changenickcolor"))
		{
			String color = null;
			String days = null;
			try
			{
				color = st.nextToken();
			}
			catch (final Exception _)
			{}
			
			try
			{
				days = st.nextToken();
			}
			catch (final Exception _)
			{}
			
			if (color != null && days != null)
			{
				playerSetColor(activeChar, color, Integer.parseInt(days), 1);
			}
		}
		else if (curCommand.startsWith("_bbs_service;changetitlecolor"))
		{
			String color = null;
			String days = null;
			try
			{
				color = st.nextToken();
			}
			catch (final Exception _)
			{}
			
			try
			{
				days = st.nextToken();
			}
			catch (final Exception _)
			{}
			
			if (color != null && days != null)
			{
				playerSetColor(activeChar, color, Integer.parseInt(days), 2);
			}
		}
		else if (curCommand.startsWith("_bbs_service;addHardWare"))
		{
			String window = null;
			String time = null;
			try
			{
				window = st.nextToken();
			}
			catch (final Exception _)
			{
			}
			
			try
			{
				time = st.nextToken();
			}
			catch (final Exception _)
			{
			}
			
			if (window != null && time != null)
			{
				boolean found = false;
				final var info = DoubleSessionManager.getInstance().getHardWareInfo(activeChar.getHWID());
				if (info != null)
				{
					activeChar.sendMessage((new ServerMessage("ServiceBBS.HARDWARE_ACTIVE", activeChar.getLang())).toString());
					return;
				}
				
				long[] price = null;
				for (final String list : Config.HARDWARE_DONATE.split(";"))
				{
					final String select[] = list.split(",");
					if (select.length != 4)
					{
						continue;
					}
					if (Integer.parseInt(select[0]) == Integer.parseInt(window) && Integer.parseInt(select[1]) == Integer.parseInt(time))
					{
						price = new long[2];
						price[0] = Integer.parseInt(select[2]);
						price[1] = Long.parseLong(select[3]);
						found = true;
					}
				}
				
				if (found && price != null)
				{
					if ((activeChar.getInventory().getItemByItemId((int) price[0]) != null) && ((activeChar.getInventory().getItemByItemId((int) price[0]).getCount() >= price[1])))
					{
						final var msg = new ServerMessage("ServiceBBS.HARDWARE_CONFIRM", activeChar.getLang());
						msg.add(Integer.parseInt(window));
						msg.add(Integer.parseInt(time));
						activeChar.sendConfirmDlg(new AnswerHardWareBuy(activeChar, Integer.parseInt(window), Integer.parseInt(time), price), 30000, msg.toString());
					}
					else
					{
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
						onBypassCommand("_bbs_service;hardWareList", activeChar);
					}
				}
			}
		}
	}
	
	private void timeSkillList(Player player, int curPage, boolean isClanSkills, boolean isClose)
	{
		var dialog = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/donate/timeSkills/donate_time_skills.htm");
		final var template = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/donate/timeSkills/donate_time_skills-template.htm");
		var block = "";
		var list = "";
		final List<DonateSkillTemplate> skillList = TimeSkillsTaskManager.getInstance().getDonateSkills();
		if (skillList.isEmpty())
		{
			if (isClose)
			{
				player.sendPacket(new ShowBoard());
			}
			return;
		}
		
		final List<DonateSkillTemplate> availableSkills = new ArrayList<>();
		for (final var tpl : skillList)
		{
			if (tpl != null)
			{
				if ((isClanSkills && !tpl.isClanSkill()) || (!isClanSkills && tpl.isClanSkill()))
				{
					continue;
				}
				
				final var oldSkill = player.getKnownSkill(tpl.getSkillId());
				if (oldSkill != null)
				{
					if (oldSkill.getLevel() == (tpl.getSkillLevel() - 1))
					{
						availableSkills.add(tpl);
					}
				}
				else if (tpl.getSkillLevel() == 1)
				{
					availableSkills.add(tpl);
				}
			}
		}
		
		final var perpage = 6;
		final var isThereNextPage = availableSkills.size() > perpage;
		var counter = 0;
		final var page = curPage > 0 ? curPage : 1;
		
		if (availableSkills.isEmpty())
		{
			if (isClose)
			{
				player.sendPacket(new ShowBoard());
				return;
			}
			dialog = dialog.replace("%navigation%", "");
			dialog = dialog.replace("%skills%", "");
			return;
		}
		else
		{
			for (int i = (page - 1) * perpage; i < availableSkills.size(); i++)
			{
				block = template;
				
				final var tpl = availableSkills.get(i);
				final var skill = SkillsParser.getInstance().getInfo(tpl.getSkillId(), tpl.getSkillLevel());
				if (skill != null)
				{
					block = block.replace("%icon%", skill.getIcon());
					block = block.replace("%name%", skill.getName(player.getLang()));
					block = block.replace("%id%", String.valueOf(skill.getId()));
					block = block.replace("%lvl%", String.valueOf(skill.getLevel()));
					block = block.replace("%isClan%", String.valueOf(isClanSkills ? 1 : 0));
					block = block.replace("%page%", String.valueOf(page));
					String descr = skill.getDescr(player.getLang());
					
					if (descr.length() > 60)
					{
						descr = descr.substring(0, 60) + "...";
					}
					block = block.replace("%descr%", descr);
					String variants = "";
					
					Collections.sort(tpl.getTimes(), TimeComparator.getInstance());
					
					int count = 0;
					for (final var variant : tpl.getTimes())
					{
						if (count > 0)
						{
							variants += ";";
						}
						variants += "" + variant + "";
						count++;
					}
					block = block.replace("%time%", variants);
					
					list += block;
					
					counter++;
					if (counter >= perpage)
					{
						break;
						
					}
				}
			}
		}
		
		final var pages = (double) availableSkills.size() / perpage;
		final var count = (int) Math.ceil(pages);
		dialog = dialog.replace("%navigation%", Util.getNavigationBlock(count, page, availableSkills.size(), perpage, isThereNextPage, "_bbs_service;timeskills " + (isClanSkills ? 1 : 0) + " %s"));
		dialog = dialog.replace("%skills%", list);
		separateAndSend(dialog, player);
	}
	
	private void playerColorList(Player activeChar, int type)
	{
		List<String> colorList = switch (type)
		{
			case 1  -> Config.COLOR_NAME_LIST;
			case 2  -> Config.COLOR_TITLE_LIST;
			default -> null;
		};
		
		if (colorList == null || colorList.isEmpty())
		{
			return;
		}
		
		String nameList = "";
		String nameColorList = "";
		String bypassColorList = "";
		int i = 0;
		int a = 0;
		for (final var c : colorList)
		{
			if (i > 0)
			{
				nameList += ";";
			}
			
			if (a == 0)
			{
				nameColorList += "<tr>";
				bypassColorList += "<tr>";
			}
			
			final var name = ServerStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.COLOR_" + c);
			nameList += name;
			nameColorList += "<td fixwidth=100 align=center><font color=" + c + ">" + name + "</font></td>";
			if (type == 1)
			{
				bypassColorList += "<td fixwidth=100 align=center><a action=\"bypass -h _bbs_service;changenickcolor " + name + " $sdays\"><font color=" + c + ">" + name + "</font></a></td>";
			}
			else
			{
				bypassColorList += "<td fixwidth=100 align=center><a action=\"bypass -h _bbs_service;changetitlecolor " + name + " $sdays\"><font color=" + c + ">" + name + "</font></a></td>";
			}
			i++;
			a++;
			if (a % 3 == 0)
			{
				nameColorList += "</tr>";
				bypassColorList += "</tr>";
				a = 0;
			}
		}
		
		if (a != 0)
		{
			if (a == 1)
			{
				nameColorList += "<td fixwidth=100 align=center>&nbsp;</td>";
				bypassColorList += "<td fixwidth=100 align=center>&nbsp;</td>";
			}
			else if (a == 2)
			{
				nameColorList += "<td fixwidth=100 align=center>&nbsp;</td>";
				bypassColorList += "<td fixwidth=100 align=center>&nbsp;</td>";
			}
			nameColorList += "</tr>";
			bypassColorList += "</tr>";
		}
		
		final NpcHtmlMessage html = new NpcHtmlMessage(5);
		final String htm = (type == 1) ? "colorname.htm" : "colortitle.htm";
		html.setFile(activeChar, activeChar.getLang(), "data/html/community/donate/" + htm);
		html.replace("%list%", nameList);
		html.replace("%colors%", nameColorList);
		html.replace("%bypass_colors%", bypassColorList);
		html.replace("%info%", (type == 1) ? ServerStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.COLOR_NAME") : ServerStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.COLOR_TITLE"));
		activeChar.sendPacket(html);
	}
	
	private void playerSetColor(Player activeChar, String color, int days, int type)
	{
		List<String> colorList = switch (type)
		{
			case 1  -> Config.COLOR_NAME_LIST;
			case 2  -> Config.COLOR_TITLE_LIST;
			default -> null;
		};
		
		if (colorList == null || colorList.isEmpty())
		{
			return;
		}
		
		String colorh = null;
		for (final String c : colorList)
		{
			final var name = ServerStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.COLOR_" + c);
			if (color.equals(name))
			{
				colorh = c;
				break;
			}
		}
		
		if (colorh == null)
		{
			return;
		}
		
		colorh = new StringBuilder(colorh).reverse().toString();
		
		int itemId = 0;
		long amount = 0;
		boolean found = false;
		final long expireTime = System.currentTimeMillis() + (days * 86400000L);
		switch (type)
		{
			case 1 :
				for (final int day : Config.CHANGE_COLOR_NAME_LIST.keySet())
				{
					if (day == days)
					{
						found = true;
						final String[] price = Config.CHANGE_COLOR_NAME_LIST.get(day).split(":");
						if (price != null && price.length == 2)
						{
							itemId = Integer.parseInt(price[0]);
							amount = Long.parseLong(price[1]);
						}
						break;
					}
				}
				
				if (found)
				{
					if (itemId != 0)
					{
						final var msg = new ServerMessage("ServiceBBS.WANT_CHANGE_COLOR_NAME", activeChar.getLang());
						msg.add(Util.formatPay(activeChar, amount, itemId));
						activeChar.sendConfirmDlg(new ChangeColorAnswerListener(activeChar, colorh, color, days, type), 60000, msg.toString());
						return;
					}
					final int curColor = Integer.decode("0x" + colorh);
					activeChar.setVar("namecolor", Integer.toString(curColor), expireTime);
					activeChar.getAppearance().setNameColor(curColor);
					activeChar.broadcastUserInfo(true);
					activeChar.sendMessage("" + ServerStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.CHANGE_NAME") + " " + color);
				}
				break;
			case 2 :
				for (final int day : Config.CHANGE_COLOR_TITLE_LIST.keySet())
				{
					if (day == days)
					{
						found = true;
						final String[] price = Config.CHANGE_COLOR_TITLE_LIST.get(day).split(":");
						if (price != null && price.length == 2)
						{
							itemId = Integer.parseInt(price[0]);
							amount = Long.parseLong(price[1]);
						}
						break;
					}
				}
				
				if (found)
				{
					if (itemId != 0)
					{
						final var msg = new ServerMessage("ServiceBBS.WANT_CHANGE_COLOR_TITLE", activeChar.getLang());
						msg.add(Util.formatPay(activeChar, amount, itemId));
						activeChar.sendConfirmDlg(new ChangeColorAnswerListener(activeChar, colorh, color, days, type), 60000, msg.toString());
						return;
					}
					final int curColor1 = Integer.decode("0x" + colorh);
					activeChar.getAppearance().setTitleColor(curColor1);
					activeChar.setVar("titlecolor", Integer.toString(curColor1), expireTime);
					activeChar.broadcastUserInfo(true);
					activeChar.sendMessage("" + ServerStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.CHANGE_TITLE_COLOR") + " " + color);
				}
				break;
		}
	}
	
	private void playerSetNickName(Player activeChar, String name)
	{
		if ((name.length() < 3) || (name.length() > 16) || (!(isValidName(name, true))))
		{
			activeChar.sendMessage((new ServerMessage("ServiceBBS.CHANGE_NAME_COLOR", activeChar.getLang())).toString());
			changeName(activeChar);
		}
		else if ((activeChar.getInventory().getItemByItemId(Config.SERVICES_NAMECHANGE_ITEM[0]) != null) && ((activeChar.getInventory().getItemByItemId(Config.SERVICES_NAMECHANGE_ITEM[0]).getCount() >= Config.SERVICES_NAMECHANGE_ITEM[1])))
		{
			int existing = 0;
			
			Connection con = null;
			PreparedStatement statement = null;
			ResultSet rset = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("SELECT * FROM characters WHERE char_name=?");
				statement.setString(1, name);
				rset = statement.executeQuery();
				while (rset.next())
				{
					existing = rset.getInt("charId");
				}
			}
			catch (final Exception e)
			{
				IO.println("Error in check nick " + e);
			}
			finally
			{
				DbUtils.closeQuietly(con, statement, rset);
			}
			
			if (existing == 0)
			{
				activeChar.setGlobalName(name);
				activeChar.destroyItemByItemId("BBSChangeName", Config.SERVICES_NAMECHANGE_ITEM[0], Config.SERVICES_NAMECHANGE_ITEM[1], activeChar, false);
				Util.addServiceLog(activeChar.getName(null) + " buy change name service!");
				activeChar.broadcastUserInfo(true);
				activeChar.sendMessage("" + ServerStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.YOUR_NAME") + " " + name);
				activeChar.store();
				changeName(activeChar);
			}
			else
			{
				activeChar.sendMessage((new ServerMessage("ServiceBBS.ALREADY_USE", activeChar.getLang())).toString());
				changeName(activeChar);
			}
		}
		else
		{
			haveNoItems(activeChar, Config.SERVICES_NAMECHANGE_ITEM[0], Config.SERVICES_NAMECHANGE_ITEM[1]);
			changeName(activeChar);
		}
	}
	
	private void changeName(Player activeChar)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(activeChar, activeChar.getLang(), "data/html/community/donate/changename.htm");
		activeChar.sendPacket(html);
	}
	
	private void changeNameColor(Player activeChar)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(activeChar, activeChar.getLang(), "data/html/community/donate/colorname.htm");
		activeChar.sendPacket(html);
	}
	
	private void changePledgeName(Player activeChar)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(activeChar, activeChar.getLang(), "data/html/community/donate/changepledgename.htm");
		activeChar.sendPacket(html);
	}
	
	private void changeTitleColor(Player activeChar)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(activeChar, activeChar.getLang(), "data/html/community/donate/colortitle.htm");
		activeChar.sendPacket(html);
	}
	
	private void hardWareList(Player activeChar)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(activeChar, activeChar.getLang(), "data/html/community/donate/hardWareList.htm");
		final String template = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/community/donate/hardWare-template.htm");
		String info = "";
		String block = "";
		
		for (final String list : Config.HARDWARE_DONATE.split(";"))
		{
			final String select[] = list.split(",");
			if (select.length != 4)
			{
				continue;
			}
			block = template;
			block = block.replace("%window%", select[0]);
			block = block.replace("%time%", select[1]);
			block = block.replace("%item%", Util.getItemName(activeChar, Integer.parseInt(select[2])));
			block = block.replace("%count%", select[3]);
			
			info += block;
		}
		html.replace("%list%", info);
		activeChar.sendPacket(html);
	}
	
	private class AnswerHardWareBuy implements OnAnswerListener
	{
		private final Player _player;
		private final int _window;
		private final int _time;
		private final long[] _price;
		
		private AnswerHardWareBuy(Player player, int window, int time, long[] price)
		{
			_player = player;
			_window = window;
			_time = time;
			_price = price;
		}
		
		@Override
		public void sayYes()
		{
			if (_player != null)
			{
				if ((_player.getInventory().getItemByItemId((int) _price[0]) != null) && ((_player.getInventory().getItemByItemId((int) _price[0]).getCount() >= _price[1])))
				{
					final var time = System.currentTimeMillis() + (_time * 3600000L);
					if (DoubleSessionManager.getInstance().addHardWareLimit(_player.getHWID(), _window, time))
					{
						_player.destroyItemByItemId("HardWareService", (int) _price[0], _price[1], _player, false);
						Util.addServiceLog(_player.getName(null) + " buy hardWare service!");
						final var msg = new ServerMessage("ServiceBBS.HARDWARE_BUY", _player.getLang());
						msg.add(_window);
						msg.add(_time);
						_player.sendMessage(msg.toString());
					}
				}
				else
				{
					_player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
					onBypassCommand("_bbs_service;hardWareList", _player);
				}
			}
		}
		
		@Override
		public void sayNo()
		{
			onBypassCommand("_bbs_service;hardWareList", _player);
		}
	}
	
	private boolean isValidName(String text, boolean isCharName)
	{
		if (isCharName && Config.FORBIDDEN_NAMES.length > 1)
		{
			for (final String st : Config.FORBIDDEN_NAMES)
			{
				if (text.toLowerCase().contains(st.toLowerCase()))
				{
					return false;
				}
			}
		}
		
		boolean result = true;
		final String test = text;
		Pattern pattern;
		try
		{
			pattern = Pattern.compile(isCharName ? Config.SERVICES_NAMECHANGE_TEMPLATE : Config.CLAN_NAME_TEMPLATE);
		}
		catch (final PatternSyntaxException _)
		{
			_log.warn("ERROR : Character name pattern of config is wrong!");
			pattern = Pattern.compile(".*");
		}
		final Matcher regexp = pattern.matcher(test);
		if (!regexp.matches())
		{
			result = false;
		}
		return result;
	}
	
	private void pledgeSetName(Player activeChar, String name)
	{
		if (activeChar.getClan() == null)
		{
			return;
		}
		
		if (activeChar.getClan().getLeaderId() != activeChar.getObjectId())
		{
			return;
		}
		
		if ((name.length() < 3) || (name.length() > 16) || (!(Util.isAlphaNumeric(name))) || (!(isValidName(name, false))))
		{
			activeChar.sendPacket(SystemMessageId.CLAN_NAME_INCORRECT);
			changePledgeName(activeChar);
			return;
		}
		
		if (ClanHolder.getInstance().getClanByName(name) != null)
		{
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_EXISTS);
			sm.addString(name);
			activeChar.sendPacket(sm);
			changePledgeName(activeChar);
			return;
		}
		
		if (Config.SERVICES_CLANNAMECHANGE_ITEM[0] != 0)
		{
			if (activeChar.getInventory().getItemByItemId(Config.SERVICES_CLANNAMECHANGE_ITEM[0]) == null)
			{
				haveNoItems(activeChar, Config.SERVICES_CLANNAMECHANGE_ITEM[0], Config.SERVICES_CLANNAMECHANGE_ITEM[1]);
				changeTitleColor(activeChar);
				return;
			}
			if (activeChar.getInventory().getItemByItemId(Config.SERVICES_CLANNAMECHANGE_ITEM[0]).getCount() < Config.SERVICES_CLANNAMECHANGE_ITEM[1])
			{
				haveNoItems(activeChar, Config.SERVICES_CLANNAMECHANGE_ITEM[0], Config.SERVICES_CLANNAMECHANGE_ITEM[1]);
				changeTitleColor(activeChar);
				return;
			}
			activeChar.destroyItemByItemId("ClanNameChange", Config.SERVICES_CLANNAMECHANGE_ITEM[0], Config.SERVICES_CLANNAMECHANGE_ITEM[1], activeChar, false);
			Util.addServiceLog(activeChar.getName(null) + " buy change clan name service!");
		}
		
		final Clan clan = activeChar.getClan();
		if (clan != null)
		{
			clan.setName(name);
			clan.updateClanNameInDB();
			activeChar.broadcastUserInfo(true);
			activeChar.sendMessage("" + ServerStorage.getInstance().getString(activeChar.getLang(), "ServiceBBS.YOUR_NAME") + " " + name);
		}
	}
	
	private static void haveNoItems(Player player, int itemId, long amount)
	{
		final Item template = ItemsParser.getInstance().getTemplate(itemId);
		if (template != null)
		{
			final ServerMessage msg = new ServerMessage("Enchant.NEED_ITEMS", player.getLang());
			msg.add(amount);
			msg.add(template.getName(player.getLang()));
			player.sendMessage(msg.toString());
		}
	}
	
	@Override
	public void onWriteCommand(String command, String ar1, String ar2, String ar3, String ar4, String ar5, Player activeChar)
	{
	}
	
	public static CommunityServices getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final CommunityServices _instance = new CommunityServices();
	}
}