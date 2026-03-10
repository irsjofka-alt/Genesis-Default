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
package gameserver.network.clientpackets;

import gameserver.model.Location;
import gameserver.model.actor.Player;
import gameserver.model.actor.instance.AirShipInstance;
import gameserver.model.items.type.WeaponType;
import gameserver.network.serverpackets.ExMoveToLocationInAirShip;
import gameserver.network.serverpackets.StopMoveInVehicle;

public class RequestMoveToLocationInAirShip extends GameClientPacket
{
	private int _shipId;
	private int _targetX;
	private int _targetY;
	private int _targetZ;
	private int _originX;
	private int _originY;
	private int _originZ;

	@Override
	protected void readImpl()
	{
		_shipId = readD();
		_targetX = readD();
		_targetY = readD();
		_targetZ = readD();
		_originX = readD();
		_originY = readD();
		_originZ = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if ((_targetX == _originX) && (_targetY == _originY) && (_targetZ == _originZ))
		{
			activeChar.sendPacket(new StopMoveInVehicle(activeChar, _shipId));
			return;
		}
		
		if (activeChar.isAttackingNow() && (activeChar.getActiveWeaponItem() != null) && (activeChar.getActiveWeaponItem().getItemType() == WeaponType.BOW))
		{
			activeChar.sendActionFailed();
			return;
		}
		
		if (activeChar.isSitting() || activeChar.isMovementDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}
		
		if (!activeChar.isInAirShip())
		{
			activeChar.sendActionFailed();
			return;
		}
		
		final AirShipInstance airShip = activeChar.getAirShip();
		if (airShip.getObjectId() != _shipId)
		{
			activeChar.sendActionFailed();
			return;
		}
		
		activeChar.setInVehiclePosition(new Location(_targetX, _targetY, _targetZ));
		activeChar.broadcastPacket(new ExMoveToLocationInAirShip(activeChar));
	}
}