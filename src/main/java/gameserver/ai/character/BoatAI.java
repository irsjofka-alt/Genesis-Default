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
package gameserver.ai.character;

import gameserver.model.Location;
import gameserver.model.actor.Player;
import gameserver.model.actor.instance.BoatInstance;
import gameserver.network.serverpackets.VehicleDeparture;
import gameserver.network.serverpackets.VehicleInfo;
import gameserver.network.serverpackets.VehicleStarted;

public class BoatAI extends VehicleAI
{
	public BoatAI(BoatInstance accessor)
	{
		super(accessor);
	}
	
	@Override
	protected void moveTo(int x, int y, int z, int offset)
	{
		if (!_actor.isMovementDisabled())
		{
			if (!_clientMoving)
			{
				_actor.broadcastPacket(new VehicleStarted(getActor(), 1));
			}
			
			_clientMoving = true;
			_actor.moveToLocation(x, y, z, offset, false);
			_actor.broadcastPacket(new VehicleDeparture(getActor()));
		}
	}
	
	@Override
	protected void moveTo(Location loc, int offset)
	{
		if (!_actor.isMovementDisabled())
		{
			if (!_clientMoving)
			{
				_actor.broadcastPacket(new VehicleStarted(getActor(), 1));
			}
			
			_clientMoving = true;
			_actor.moveToLocation(loc.getX(), loc.getY(), loc.getZ(), offset, false);
			_actor.broadcastPacket(new VehicleDeparture(getActor()));
		}
	}
	
	@Override
	public void clientStopMoving(Location loc)
	{
		if (_actor.isMoving())
		{
			_actor.stopMove(loc);
		}
		
		if (_clientMoving || (loc != null))
		{
			_clientMoving = false;
			_actor.broadcastPacket(new VehicleStarted(getActor(), 0));
			_actor.broadcastPacket(new VehicleInfo(getActor()));
		}
	}
	
	@Override
	public void describeStateToPlayer(Player player)
	{
		if (_clientMoving)
		{
			player.sendPacket(new VehicleDeparture(getActor()));
		}
	}

	@Override
	public BoatInstance getActor()
	{
		return (BoatInstance) _actor;
	}
}