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
package gameserver.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import gameserver.Config;
import gameserver.data.parser.AdminParser;
import gameserver.instancemanager.PunishmentManager;
import gameserver.model.actor.Player;
import gameserver.model.punishment.PunishmentAffect;
import gameserver.model.punishment.PunishmentSort;
import gameserver.model.punishment.PunishmentTemplate;
import gameserver.model.punishment.PunishmentType;
import gameserver.network.communication.AuthServerCommunication;
import gameserver.network.communication.gameserverpackets.ChangeAccessLevel;

public final class IllegalPlayerAction
{
	private static final Logger _log = LogManager.getLogger(IllegalPlayerAction.class);

	static
	{
		new File("log/IllegalActions").mkdirs();
	}
	
	public static void IllegalAction(Player actor, List<String> messages, int punishment)
	{
		if (messages == null || messages.isEmpty() || !Config.ALLOW_ILLEGAL_ACTIONS)
		{
			return;
		}
		
		String actions = "";
		final String lastActions = messages.getLast();
		for (final String action : messages)
		{
			actions += action + Config.EOL;
		}

		final File file = new File("log/IllegalActions/" + actor.getName(null) + ".txt");
		try (
		    FileWriter save = new FileWriter(file, true))
		{
			save.write(actions);
		}
		catch (final IOException e)
		{
			_log.error("IllegalAction for char " + actor.getName(null) + " could not be saved: ", e);
		}
		
		AdminParser.getInstance().broadcastMessageToGMs(lastActions);
		actor.getBannedActions().clear();
		
		switch (punishment)
		{
			case 1 :
				actor.sendMessage("You are using illegal actions!");
				break;
			case 2 :
				actor.sendMessage("You will be kicked for illegal actions! GM informed.");
				actor.logout();
				break;
			case 3 :
				actor.setAccessLevel(-1);
				final long banExpire = System.currentTimeMillis() + (Config.SECOND_AUTH_BAN_TIME * 60 * 1000);
				final int expire = (int) ((System.currentTimeMillis() / 1000) + (Config.SECOND_AUTH_BAN_TIME * 60));
				final int accessLvl = Config.SECOND_AUTH_BAN_TIME > 0 ? 0 : -1;
				AuthServerCommunication.getInstance().sendPacket(new ChangeAccessLevel(actor.getAccountName(), accessLvl, expire));
				actor.sendMessage("You are banned for illegal actions! GM informed.");
				PunishmentManager.getInstance().addPunishment(actor, null, new PunishmentTemplate(String.valueOf(actor.getObjectId()), actor.getName(null), PunishmentSort.CHARACTER, PunishmentAffect.CHARACTER, PunishmentType.BAN, banExpire, "IllegalPlayerAction!", actor.getName(null)), true);
				break;
			case 4 :
				actor.sendMessage("Illegal actions performed!");
				actor.sendMessage("You will be teleported to GM Consultation Service area and jailed.");
				PunishmentManager.getInstance().addPunishment(actor, null, new PunishmentTemplate(String.valueOf(actor.getObjectId()), actor.getName(null), PunishmentSort.CHARACTER, PunishmentAffect.CHARACTER, PunishmentType.JAIL, System.currentTimeMillis() + (Config.DEFAULT_PUNISH_PARAM * 1000), "IllegalPlayerAction!", actor.getName(null)), true);
				break;
		}
	}
}