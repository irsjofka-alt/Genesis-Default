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

import java.util.Collections;
import java.util.List;

import gameserver.data.htm.HtmCache;
import gameserver.data.parser.SkillsParser;
import gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import gameserver.instancemanager.mods.TimeSkillsTaskManager;
import gameserver.model.actor.Player;
import gameserver.model.actor.templates.TimeSkillTemplate;
import gameserver.network.serverpackets.NpcHtmlMessage;
import gameserver.utils.TimeUtils;
import gameserver.utils.Util;
import gameserver.utils.comparators.TimeSkillComparator;

public class TimeSkills implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
	        "timeskills"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if (!TimeSkillsTaskManager.getInstance().isActive())
		{
			return false;
		}
		
		if (command.startsWith("timeskills"))
		{
			final String[] params = command.split(" ");
			String curPage = "1";
			try
			{
				curPage = params[1];
			}
			catch (final Exception e)
			{
			}
			
			final List<TimeSkillTemplate> templates = TimeSkillsTaskManager.getInstance().getTemplates(activeChar, true, true);
			if (templates.isEmpty())
			{
				activeChar.sendMessage("You have no active temporary skills!");
				return false;
			}
			
			Collections.sort(templates, TimeSkillComparator.getInstance());
			
			final var page = Integer.valueOf(curPage);
			final var perpage = 6;
			final var isThereNextPage = templates.size() > perpage;
			
			final var html = new NpcHtmlMessage(5);
			html.setFile(activeChar, activeChar.getLang(), "data/html/community/donate/timeSkills/myskills.htm");
			final String template = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/community/donate/timeSkills/myskills-template.htm");
			
			var block = "";
			var list = "";
			
			var countss = 0;
			final var now = System.currentTimeMillis();
			for (int i = (page - 1) * perpage; i < templates.size(); i++)
			{
				final var tpl = templates.get(i);
				if (tpl != null)
				{
					block = template;
					
					final var sk = SkillsParser.getInstance().getInfo(tpl.getSkillId(), tpl.getSkillLevel());
					if (sk != null)
					{
						block = block.replace("%name%", sk.getName(activeChar.getLang()));
						block = block.replace("%level%", String.valueOf(sk.getLevel()));
						block = block.replace("%icon%", sk.getIcon());
						block = block.replace("%time%", TimeUtils.formatTime(activeChar, (int) ((tpl.getTime() - now) / 1000), false));
					}
					list += block;
					countss++;
					
					if (countss >= perpage)
					{
						break;
					}
				}
			}
			
			final var pages = (double) templates.size() / perpage;
			final var count = (int) Math.ceil(pages);
			
			html.replace("%list%", list);
			html.replace("%navigation%", Util.getNavigationBlock(count, page, templates.size(), perpage, isThereNextPage, "voiced_timeskills %s"));
			activeChar.sendPacket(html);
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}