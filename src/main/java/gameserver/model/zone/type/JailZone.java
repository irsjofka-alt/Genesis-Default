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
package gameserver.model.zone.type;

import gameserver.Config;
import gameserver.model.Location;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Player;
import gameserver.model.actor.instance.player.impl.TeleportTask;
import gameserver.model.zone.ZoneId;
import gameserver.model.zone.ZoneType;
import gameserver.network.SystemMessageId;

public class JailZone extends ZoneType
{
	private static final Location JAIL_IN_LOC = new Location(-114356, -249645, -2984);
	private static final Location JAIL_OUT_LOC = new Location(17836, 170178, -3507);

	public JailZone(int id)
	{
		super(id);
		addZoneId(ZoneId.JAIL);
		addZoneId(ZoneId.NO_SUMMON_FRIEND);
		if (Config.JAIL_IS_PVP)
		{
			addZoneId(ZoneId.PVP);
		}
	}
	
	@Override
	protected void onEnter(Creature character)
	{
		if (character.isPlayer())
		{
			if (Config.JAIL_IS_PVP)
			{
				character.sendPacket(SystemMessageId.ENTERED_COMBAT_ZONE);
			}
		}
	}

	@Override
	protected void onExit(Creature character)
	{
		if (character.isPlayer())
		{
			final Player player = character.getActingPlayer();

			if (Config.JAIL_IS_PVP)
			{
				character.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
			}

			if (player.isJailed())
			{
				player.getPersonalTasks().addTask(new TeleportTask(2000, JAIL_IN_LOC));
				character.sendMessage("You cannot cheat your way out of here. You must wait until your jail time is over.");
			}
		}
	}

	public static Location getLocationIn()
	{
		return JAIL_IN_LOC;
	}

	public static Location getLocationOut()
	{
		return JAIL_OUT_LOC;
	}
}