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
package gameserver.handler.usercommandhandlers.impl;

import gameserver.handler.usercommandhandlers.IUserCommandHandler;
import gameserver.instancemanager.ReflectionManager;
import gameserver.model.actor.Player;
import gameserver.model.strings.server.ServerMessage;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.SystemMessage;

public class InstanceZone implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
	        114
	};

	@Override
	public boolean useUserCommand(int id, Player activeChar)
	{
		if (id != COMMAND_IDS[0])
		{
			return false;
		}
		
		final var useByServer = ReflectionManager.USE_SERVER_NAMES;
		
		final var manager = ReflectionManager.getInstance();
		final var ref = ReflectionManager.getInstance().getPlayerReflection(activeChar.getObjectId(), false);
		if (ref != null && ref.getTemplateId() > 0)
		{
			if (useByServer)
			{
				final var msg = new ServerMessage("Reflection.CURRENTLY_USE", activeChar.getLang());
				msg.add(manager.getReflectionName(activeChar, ref.getTemplateId()));
				activeChar.sendMessage(msg.toString());
			}
			else
			{
				final var sm = SystemMessage.getSystemMessage(SystemMessageId.INSTANT_ZONE_CURRENTLY_INUSE_S1);
				sm.addInstanceName(ref.getTemplateId());
				activeChar.sendPacket(sm);
			}
		}
		
		final var instanceTimes = manager.getAllReflectionTimes(activeChar);
		boolean firstMessage = true;
		if (instanceTimes != null)
		{
			for (final int instanceId : instanceTimes.keySet())
			{
				final long remainingTime = (instanceTimes.get(instanceId) - System.currentTimeMillis()) / 1000;
				if (remainingTime > 60)
				{
					if (firstMessage)
					{
						firstMessage = false;
						if (useByServer)
						{
							activeChar.sendMessage(new ServerMessage("Reflection.INSTANCE_ZONE_TIME_LIMIT", activeChar.getLang()).toString());
						}
						else
						{
							activeChar.sendPacket(SystemMessageId.INSTANCE_ZONE_TIME_LIMIT);
						}
					}
					
					final int hours = (int) (remainingTime / 3600);
					final int minutes = (int) ((remainingTime % 3600) / 60);
					if (useByServer)
					{
						ServerMessage msg = null;
						if (hours > 0)
						{
							msg = new ServerMessage("Reflection.AVAILABLE_AFTER_HOURS_MINS", activeChar.getLang());
							msg.add(manager.getReflectionName(activeChar, instanceId));
							msg.add(hours);
							msg.add(minutes);
						}
						else
						{
							msg = new ServerMessage("Reflection.AVAILABLE_AFTER_MINS", activeChar.getLang());
							msg.add(manager.getReflectionName(activeChar, instanceId));
							msg.add(minutes);
						}
						activeChar.sendMessage(msg.toString());
					}
					else
					{
						final var sm = SystemMessage.getSystemMessage(SystemMessageId.AVAILABLE_AFTER_S1_S2_HOURS_S3_MINUTES);
						sm.addInstanceName(instanceId);
						sm.addNumber(hours);
						sm.addNumber(minutes);
						activeChar.sendPacket(sm);
					}
				}
			}
		}
		
		if (firstMessage)
		{
			if (useByServer)
			{
				activeChar.sendMessage(new ServerMessage("Reflection.NO_INSTANCEZONE_TIME", activeChar.getLang()).toString());
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.NO_INSTANCEZONE_TIME_LIMIT);
			}
		}
		return true;
	}

	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}