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
package gameserver.handler.admincommandhandlers.impl;

import java.util.StringTokenizer;

import gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import gameserver.instancemanager.QuestManager;
import gameserver.model.actor.Player;
import gameserver.model.entity.BloodAltarsEngine;
import gameserver.model.quest.Quest;
import gameserver.network.serverpackets.NpcHtmlMessage;
import gameserver.utils.StringUtil;

public class BloodAltars implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
	        "admin_altar_menu", "admin_altar_status"
	};

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (activeChar == null)
		{
			return false;
		}

		String altar_name = "";
		final StringTokenizer st = new StringTokenizer(command, " ");
		st.nextToken();
		if (st.hasMoreTokens())
		{
			altar_name = st.nextToken();
		}
		
		if (command.startsWith("admin_altar_menu"))
		{
			showAltarMenu(activeChar);
		}
		else if (command.startsWith("admin_altar_status"))
		{
			try
			{
				if (altar_name != null)
				{
					final BloodAltarsEngine altar = (BloodAltarsEngine) QuestManager.getInstance().getQuest(altar_name);
					if (altar != null)
					{
						altar.changeStatus(altar_name, altar.getChangeTime(), altar.getStatus());
						activeChar.sendMessage(altar_name + " blood altar tried to change status.");
						showAltarMenu(activeChar);
						return true;
					}
				}
			}
			catch (final Exception e)
			{
				activeChar.sendMessage("Usage: //altar_status <altarname>");
				e.printStackTrace();
				showAltarMenu(activeChar);
				return false;
			}
		}
		return false;
	}

	protected void showAltarMenu(Player activeChar)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(activeChar, activeChar.getLang(), "data/html/admin/blood-altars.htm");
		final StringBuilder cList = new StringBuilder(500);
		StringUtil.append(cList, "<table width=280><tr>", "<td width=120><font color=\"c1b33a\">Blood Altar</font></td><td width=60><font color=\"c1b33a\">Status</font></td><td width=100></td>", "</tr></table><img src=\"L2UI.squaregray\" width=\"280\" height=\"1\">");
		for (final Quest altar : QuestManager.getInstance().getQuests())
		{
			if (altar instanceof BloodAltarsEngine)
			{
				final BloodAltarsEngine _altar = (BloodAltarsEngine) QuestManager.getInstance().getQuest(altar.getName());
				String status = switch (_altar.getStatus())
				{
					case 0  -> "<font color=\"LEVEL\">Inactive</font>";
					case 1  -> "<font color=\"00FF00\">Active</font>";
					case 2  -> "<font color=\"FF0000\">Fighting</font>";
					default -> "";
				};
				StringUtil.append(cList, "<table width=280><tr><td width=120>" + altar.getName() + "</td><td width=60>" + status + "</td>", "<td width=100><button value=\"Change Status\" action=\"bypass -h admin_altar_status " + altar.getName() + "\" width=100 height=24 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>", "</tr></table><img src=\"L2UI.squaregray\" width=\"280\" height=\"1\">");
			}
		}
		html.replace("%LIST%", cList.toString());
		activeChar.sendPacket(html);
	}
}