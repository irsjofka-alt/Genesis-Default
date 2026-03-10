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
package gameserver.handler.bypasshandlers.impl;

import gameserver.handler.bypasshandlers.IBypassHandler;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Player;
import gameserver.model.entity.events.cleft.AerialCleftEvent;
import gameserver.model.olympiad.Olympiad;
import gameserver.model.olympiad.OlympiadGameManager;
import gameserver.model.olympiad.OlympiadManager;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.ExReceiveOlympiadList;

public class OlympiadObservation implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
	        "watchmatch", "arenachange"
	};
	
	@Override
	public final boolean useBypass(String command, Player activeChar, Creature target)
	{
		try
		{
			final var olymanager = activeChar.getLastFolkNPC();
			
			if (command.startsWith(COMMANDS[0]))
			{
				if (!Olympiad.getInstance().inCompPeriod())
				{
					activeChar.sendPacket(SystemMessageId.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
					return false;
				}
				activeChar.sendPacket(new ExReceiveOlympiadList.OlympiadList());
			}
			else
			{
				if (!activeChar.inObserverMode() && (olymanager == null || !activeChar.isInsideRadius(olymanager, 300, false, false)))
				{
					return false;
				}
				
				if (OlympiadManager.getInstance().isRegisteredInComp(activeChar))
				{
					activeChar.sendPacket(SystemMessageId.WHILE_YOU_ARE_ON_THE_WAITING_LIST_YOU_ARE_NOT_ALLOWED_TO_WATCH_THE_GAME);
					return false;
				}

				if (!Olympiad.getInstance().inCompPeriod())
				{
					activeChar.sendPacket(SystemMessageId.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
					return false;
				}

				if (activeChar.isInKrateisCube() || activeChar.getUCState() > 0 || activeChar.isInFightEvent() || AerialCleftEvent.getInstance().isPlayerParticipant(activeChar.getObjectId()))
				{
					activeChar.sendMessage("You can not observe games while registered for Event");
					return false;
				}
				
				final int arenaId = Integer.parseInt(command.substring(12).trim());
				final var nextArena = OlympiadGameManager.getInstance().getOlympiadTask(arenaId);
				if (nextArena != null)
				{
					activeChar.enterOlympiadObserverMode(nextArena.getZone().getSpectatorSpawns().getFirst(), arenaId);
					activeChar.setReflection(OlympiadGameManager.getInstance().getOlympiadTask(arenaId).getZone().getReflection());
				}
			}
			return true;
			
		}
		catch (final Exception e)
		{
			_log.warn("Exception in " + getClass().getSimpleName(), e);
		}
		return false;
	}
	
	@Override
	public final String[] getBypassList()
	{
		return COMMANDS;
	}
}