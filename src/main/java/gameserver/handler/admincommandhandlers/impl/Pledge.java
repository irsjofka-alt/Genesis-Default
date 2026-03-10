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

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import gameserver.data.holder.ClanHolder;
import gameserver.data.parser.ClanParser;
import gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import gameserver.model.Clan;
import gameserver.model.GameObject;
import gameserver.model.actor.Player;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.GMViewPledgeInfo;
import gameserver.network.serverpackets.NpcHtmlMessage;
import gameserver.network.serverpackets.SystemMessage;

public class Pledge implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
	        "admin_pledge", "admin_pledge_info"
	};

	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		final GameObject target = activeChar.getTarget();
		Player player = null;
		if (target instanceof Player player1)
		{
			player = player1;
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			showMainPage(activeChar);
			return false;
		}
		final String name = player.getName(null);
		if (command.equals("admin_pledge_info"))
		{
			showMainPage(activeChar);
			return true;
		}
		else if (command.startsWith("admin_pledge"))
		{
			String action = null;
			String parameter = null;
			final StringTokenizer st = new StringTokenizer(command);
			try
			{
				st.nextToken();
				action = st.nextToken();
				parameter = st.nextToken();
			}
			catch (final NoSuchElementException _)
			{
				return false;
			}
			if (action.equals("create"))
			{
				final long cet = player.getClanCreateExpiryTime();
				player.setClanCreateExpiryTime(0);
				final Clan clan = ClanHolder.getInstance().createClan(player, parameter);
				if (clan != null)
				{
					activeChar.sendMessage("Clan " + parameter + " created. Leader: " + player.getName(null));
				}
				else
				{
					player.setClanCreateExpiryTime(cet);
					activeChar.sendMessage("There was a problem while creating the clan.");
				}
			}
			else if (!player.isClanLeader())
			{
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_IS_NOT_A_CLAN_LEADER);
				sm.addString(name);
				activeChar.sendPacket(sm);
				showMainPage(activeChar);
				return false;
			}
			else if (action.equals("dismiss"))
			{
				ClanHolder.getInstance().destroyClan(player.getClanId());
				final Clan clan = player.getClan();
				if (clan == null)
				{
					activeChar.sendMessage("Clan disbanded.");
				}
				else
				{
					activeChar.sendMessage("There was a problem while destroying the clan.");
				}
			}
			else if (action.equals("info"))
			{
				activeChar.sendPacket(new GMViewPledgeInfo(player.getClan(), player));
			}
			else if (parameter == null)
			{
				activeChar.sendMessage("Usage: //pledge <setlevel|rep> <number>");
			}
			else if (action.equals("setlevel"))
			{
				final int level = Integer.parseInt(parameter);
				if ((level >= 0) && level <= ClanParser.getInstance().getMaxLevel())
				{
					player.getClan().changeLevel(level, true);
					activeChar.sendMessage("You set level " + level + " for clan " + player.getClan().getName());
				}
				else
				{
					activeChar.sendMessage("Level incorrect.");
				}
			}
			else if (action.startsWith("rep"))
			{
				try
				{
					final int points = Integer.parseInt(parameter);
					final Clan clan = player.getClan();
					if (clan.getLevel() < 5)
					{
						activeChar.sendMessage("Only clans of level 5 or above may receive reputation points.");
						showMainPage(activeChar);
						return false;
					}
					clan.addReputationScore(points, true);
					activeChar.sendMessage("You " + (points > 0 ? "add " : "remove ") + Math.abs(points) + " points " + (points > 0 ? "to " : "from ") + clan.getName() + "'s reputation. Their current score is " + clan.getReputationScore());
				}
				catch (final Exception _)
				{
					activeChar.sendMessage("Usage: //pledge <rep> <number>");
				}
			}
		}
		showMainPage(activeChar);
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void showMainPage(Player activeChar)
	{
		final NpcHtmlMessage adminhtm = new NpcHtmlMessage(5);
		adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/pledgeinfo.htm");
		activeChar.sendPacket(adminhtm);
	}
}