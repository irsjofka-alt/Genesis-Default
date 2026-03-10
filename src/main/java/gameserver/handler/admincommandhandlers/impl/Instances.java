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
import gameserver.instancemanager.ReflectionManager;
import gameserver.model.GameObject;
import gameserver.model.actor.Player;
import gameserver.model.actor.Summon;
import gameserver.model.entity.Reflection;

public class Instances implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
	        "admin_setinstance", "admin_ghoston", "admin_ghostoff", "admin_destroyinstance", "admin_listinstances"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		final StringTokenizer st = new StringTokenizer(command);
		st.nextToken();
		
		if (command.startsWith("admin_listinstances"))
		{
			for (final Reflection temp : ReflectionManager.getInstance().getReflections().values())
			{
				activeChar.sendMessage("Id: " + temp.getId() + " Name: " + temp.getName());
			}
		}
		else if (command.startsWith("admin_setinstance"))
		{
			try
			{
				final int val = Integer.parseInt(st.nextToken());
				final var ref = ReflectionManager.getInstance().getReflection(val);
				if (ref == null)
				{
					activeChar.sendMessage("Instance " + val + " doesnt exist.");
					return false;
				}

				final GameObject target = activeChar.getTarget();
				if (target == null || target instanceof Summon)
				{
					activeChar.sendMessage("Incorrect target.");
					return false;
				}
				if (target instanceof Player player)
				{
					player.sendMessage("Admin set your instance to:" + val);
					player.teleToLocation(player.getX(), player.getY(), player.getZ(), true, ref);
				}
				else
				{
					target.setReflection(ref);
				}
				activeChar.sendMessage("Moved " + target.getName(activeChar.getLang()) + " to instance " + target.getReflectionId() + ".");
				return true;
			}
			catch (final Exception _)
			{
				activeChar.sendMessage("Use //setinstance id");
			}
		}
		else if (command.startsWith("admin_destroyinstance"))
		{
			try
			{
				final int val = Integer.parseInt(st.nextToken());
				final var ref = ReflectionManager.getInstance().getReflection(val);
				if (ref != null)
				{
					ref.collapse();
					activeChar.sendMessage("Instance destroyed");
				}
			}
			catch (final Exception _)
			{
				activeChar.sendMessage("Use //destroyinstance id");
			}
		}
		else if (command.startsWith("admin_ghoston"))
		{
			activeChar.getAppearance().setGhostMode(true);
			activeChar.sendMessage("Ghost mode enabled");
			activeChar.broadcastUserInfo(true);
			activeChar.decayMe();
			activeChar.spawnMe();
		}
		else if (command.startsWith("admin_ghostoff"))
		{
			activeChar.getAppearance().setGhostMode(false);
			activeChar.sendMessage("Ghost mode disabled");
			activeChar.broadcastUserInfo(true);
			activeChar.decayMe();
			activeChar.spawnMe();
		}
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}