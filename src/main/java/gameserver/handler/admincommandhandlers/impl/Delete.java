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

import gameserver.data.holder.SpawnHolder;
import gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import gameserver.instancemanager.RaidBossSpawnManager;
import gameserver.model.GameObject;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.spawn.Spawner;

public class Delete implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
	        "admin_delete"
	};

	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.equals("admin_delete"))
		{
			handleDelete(activeChar);
		}
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private void handleDelete(Player activeChar)
	{
		final GameObject obj = activeChar.getTarget();
		if (obj instanceof Npc target)
		{
			final Spawner spawn = target.getSpawn();
			if (spawn != null)
			{
				spawn.stopRespawn();

				if (RaidBossSpawnManager.getInstance().isDefined(spawn.getId()))
				{
					RaidBossSpawnManager.getInstance().deleteSpawn(spawn, true);
				}
				else
				{
					SpawnHolder.getInstance().deleteSpawn(spawn, true);
				}
			}
			target.deleteMe();
			activeChar.sendMessage("Deleted " + target.getName(activeChar.getLang()) + " from " + target.getObjectId() + ".");
		}
		else
		{
			activeChar.sendMessage("Incorrect target.");
		}
	}
}