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

import java.util.List;
import java.util.StringTokenizer;

import gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import gameserver.instancemanager.FortManager;
import gameserver.model.Clan;
import gameserver.model.GameObject;
import gameserver.model.actor.Player;
import gameserver.model.entity.Fort;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.NpcHtmlMessage;
import gameserver.utils.StringUtil;

public class FortSiege implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
	        "admin_fortsiege", "admin_add_fortattacker", "admin_list_fortsiege_clans", "admin_clear_fortsiege_list", "admin_spawn_fortdoors", "admin_endfortsiege", "admin_startfortsiege", "admin_setfort", "admin_removefort"
	};

	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		final StringTokenizer st = new StringTokenizer(command, " ");
		command = st.nextToken();
		
		Fort fort = null;
		int fortId = 0;
		if (st.hasMoreTokens())
		{
			fortId = Integer.parseInt(st.nextToken());
			fort = FortManager.getInstance().getFort(fortId);
		}
		
		if (((fort == null) || (fortId == 0)))
		{
			showFortSelectPage(activeChar);
		}
		else
		{
			final GameObject target = activeChar.getTarget();
			Player player = null;
			if (target instanceof Player)
			{
				player = (Player) target;
			}
			
			if (command.equalsIgnoreCase("admin_add_fortattacker"))
			{
				if (player == null)
				{
					activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				}
				else
				{
					if (fort.getSiege().checkIfCanRegister(player))
					{
						fort.getSiege().registerAttacker(player, true);
					}
				}
			}
			else if (command.equalsIgnoreCase("admin_clear_fortsiege_list"))
			{
				fort.getSiege().clearSiegeClan();
			}
			else if (command.equalsIgnoreCase("admin_endfortsiege"))
			{
				fort.getSiege().endSiege();
			}
			else if (command.equalsIgnoreCase("admin_list_fortsiege_clans"))
			{
				activeChar.sendMessage("Not implemented yet.");
			}
			else if (command.equalsIgnoreCase("admin_setfort"))
			{
				if ((player == null) || (player.getClan() == null))
				{
					activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				}
				else
				{
					fort.setOwner(player.getClan(), false);
				}
			}
			else if (command.equalsIgnoreCase("admin_removefort"))
			{
				final Clan clan = fort.getOwnerClan();
				if (clan != null)
				{
					fort.removeOwner(true);
				}
				else
				{
					activeChar.sendMessage("Unable to remove fort");
				}
			}
			else if (command.equalsIgnoreCase("admin_spawn_fortdoors"))
			{
				fort.resetDoors();
			}
			else if (command.equalsIgnoreCase("admin_startfortsiege"))
			{
				fort.getSiege().startSiege();
			}
			
			showFortSiegePage(activeChar, fort);
		}
		return true;
	}
	
	private void showFortSelectPage(Player activeChar)
	{
		int i = 0;
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile(activeChar, activeChar.getLang(), "data/html/admin/forts.htm", false);
		
		final List<Fort> forts = FortManager.getInstance().getForts();
		final StringBuilder cList = new StringBuilder(forts.size() * 100);
		
		for (final Fort fort : forts)
		{
			if (fort != null)
			{
				StringUtil.append(cList, "<td fixwidth=90><a action=\"bypass -h admin_fortsiege ", String.valueOf(fort.getId()), "\">", fort.getName(), " id: ", String.valueOf(fort.getId()), "</a></td>");
				i++;
			}
			
			if (i > 2)
			{
				cList.append("</tr><tr>");
				i = 0;
			}
		}
		adminReply.replace("%forts%", cList.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private void showFortSiegePage(Player activeChar, Fort fort)
	{
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile(activeChar, activeChar.getLang(), "data/html/admin/fort.htm");
		adminReply.replace("%fortName%", fort.getName());
		adminReply.replace("%fortId%", String.valueOf(fort.getId()));
		activeChar.sendPacket(adminReply);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}