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

import java.text.SimpleDateFormat;
import java.util.Calendar;

import gameserver.Config;
import gameserver.GameTimeController;
import gameserver.Shutdown;
import gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import gameserver.model.GameObjectsStorage;
import gameserver.model.actor.Player;
import gameserver.network.serverpackets.NpcHtmlMessage;
import gameserver.utils.Util;

public class ShutdownMenu implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
	        "admin_server_shutdown", "admin_server_restart", "admin_server_abort"
	};

	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.startsWith("admin_server_shutdown"))
		{
			try
			{
				final String val = command.substring(22);
				if (Util.isDigit(val))
				{
					serverShutdown(activeChar, Integer.valueOf(val), false);
				}
				else
				{
					activeChar.sendMessage("Usage: //server_shutdown <seconds>");
					sendHtmlForm(activeChar);
				}
			}
			catch (final StringIndexOutOfBoundsException _)
			{
				sendHtmlForm(activeChar);
			}
		}
		else if (command.startsWith("admin_server_restart"))
		{
			try
			{
				final String val = command.substring(21);
				if (Util.isDigit(val))
				{
					serverShutdown(activeChar, Integer.parseInt(val), true);
				}
				else
				{
					activeChar.sendMessage("Usage: //server_restart <seconds>");
					sendHtmlForm(activeChar);
				}
			}
			catch (final StringIndexOutOfBoundsException _)
			{
				sendHtmlForm(activeChar);
			}
		}
		else if (command.startsWith("admin_server_abort"))
		{
			serverAbort(activeChar);
		}
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void sendHtmlForm(Player activeChar)
	{
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		final int t = GameTimeController.getInstance().getGameTime();
		final int h = t / 60;
		final int m = t % 60;
		final SimpleDateFormat format = new SimpleDateFormat("h:mm a");
		final Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, h);
		cal.set(Calendar.MINUTE, m);
		adminReply.setFile(activeChar, activeChar.getLang(), "data/html/admin/shutdown.htm");
		adminReply.replace("%count%", String.valueOf(GameObjectsStorage.getAllPlayersCount()));
		adminReply.replace("%used%", String.valueOf(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
		adminReply.replace("%xp%", String.valueOf(Util.convertDouble("%.2f".formatted(Config.RATE_XP_BY_LVL[activeChar.getLevel()] * activeChar.getPremiumBonus().getRateXp()))));
		adminReply.replace("%sp%", String.valueOf(Util.convertDouble("%.2f".formatted(Config.RATE_SP_BY_LVL[activeChar.getLevel()] * activeChar.getPremiumBonus().getRateSp()))));
		adminReply.replace("%adena%", String.valueOf(Util.convertDouble("%.2f".formatted(Config.RATE_DROP_ADENA * activeChar.getPremiumBonus().getDropAdena()))));
		adminReply.replace("%drop%", String.valueOf(Util.convertDouble("%.2f".formatted(Config.RATE_DROP_ITEMS))));
		adminReply.replace("%time%", String.valueOf(format.format(cal.getTime())));
		activeChar.sendPacket(adminReply);
	}
	
	private void serverShutdown(Player activeChar, int seconds, boolean restart)
	{
		Shutdown.getInstance().startShutdown(activeChar, seconds, restart);
	}
	
	private void serverAbort(Player activeChar)
	{
		Shutdown.getInstance().abort(activeChar);
	}
}