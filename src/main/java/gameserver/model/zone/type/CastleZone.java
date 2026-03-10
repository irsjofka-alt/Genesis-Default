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

import gameserver.instancemanager.CastleManager;
import gameserver.instancemanager.ReflectionManager;
import gameserver.model.TeleportWhereType;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Player;
import gameserver.model.entity.Castle;
import gameserver.model.zone.ZoneId;
import gameserver.model.zone.ZoneRespawn;

public class CastleZone extends ZoneRespawn
{
	private int _castleId;
	private Castle _castle = null;

	public CastleZone(int id)
	{
		super(id);
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("castleId"))
		{
			_castleId = Integer.parseInt(value);
			_castle = CastleManager.getInstance().getCastleById(_castleId);
			if (_castle != null)
			{
				addZoneId(ZoneId.CASTLE);
			}
		}
		else
		{
			super.setParameter(name, value);
		}
	}

	@Override
	protected void onEnter(Creature character)
	{
	}

	@Override
	protected void onExit(Creature character)
	{
	}

	@Override
	public void onDieInside(Creature character)
	{
	}

	@Override
	public void onReviveInside(Creature character)
	{
	}

	public void banishForeigners(int owningClanId)
	{
		final TeleportWhereType type = TeleportWhereType.TOWN;
		for (final Player temp : getPlayersInside())
		{
			if (temp.getClanId() == owningClanId && owningClanId != 0)
			{
				continue;
			}

			temp.teleToLocation(type, true, ReflectionManager.DEFAULT);
		}
	}

	public int getCastleId()
	{
		return _castleId;
	}
}