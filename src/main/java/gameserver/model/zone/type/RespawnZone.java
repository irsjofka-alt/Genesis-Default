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

import java.util.HashMap;
import java.util.Map;

import gameserver.model.actor.Creature;
import gameserver.model.actor.Player;
import gameserver.model.base.Race;
import gameserver.model.zone.ZoneType;

public class RespawnZone extends ZoneType
{
	private final Map<Race, String> _raceRespawnPoint = new HashMap<>();

	public RespawnZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(Creature character)
	{
	}

	@Override
	protected void onExit(Creature character)
	{
	}

	public void addRaceRespawnPoint(String race, String point)
	{
		_raceRespawnPoint.put(Race.valueOf(race), point);
	}

	public Map<Race, String> getAllRespawnPoints()
	{
		return _raceRespawnPoint;
	}

	public String getRespawnPoint(Player activeChar)
	{
		return _raceRespawnPoint.get(activeChar.getRace());
	}
}
