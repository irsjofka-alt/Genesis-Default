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

import gameserver.Config;
import gameserver.ai.model.CtrlIntention;
import gameserver.geodata.GeoEngine;
import gameserver.model.Location;
import gameserver.model.actor.Player;
import gameserver.model.zone.ZoneId;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.StopMove;
import gameserver.utils.Util;

public class MoveBackwardToLocation extends GameClientPacket
{
	private final Location _originLoc = new Location();
	private Location _targetLoc = new Location();
	private int _moveMovement;

	@Override
	protected void readImpl()
	{
		_targetLoc.setX(readD());
		_targetLoc.setY(readD());
		_targetLoc.setZ(readD());
		_originLoc.setX(readD());
		_originLoc.setY(readD());
		_originLoc.setZ(readD());
		if (_buf.hasRemaining())
		{
			_moveMovement = readD();
		}
		else
		{
			final Player activeChar = getClient().getActiveChar();
			Util.handleIllegalPlayerAction(activeChar, "" + activeChar.getName(null) + " is trying to use L2Walker!");
		}
	}

	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null || activeChar.isAfraid())
		{
			return;
		}
		
		if (((System.currentTimeMillis() - activeChar.getLastMovePacket()) < Config.MOVE_PACKET_DELAY) && _moveMovement != 0)
		{
			activeChar.sendActionFailed();
			return;
		}
		
		activeChar.isntAfk();
		
		if (_moveMovement == 0)
		{
			if (activeChar.isInsideZone(ZoneId.NO_WASD))
			{
				final double dx = activeChar.getX() - _originLoc.getX();
				final double dy = activeChar.getY() - _originLoc.getY();
				final double diffSq = ((dx * dx) + (dy * dy));
				if (diffSq > 18000)
				{
					activeChar.validateLocation(0);
				}
				activeChar.stopMove(activeChar.getLocation());
				activeChar.sendActionFailed();
				return;
			}
			if (_originLoc.getX() == _targetLoc.getX() && _originLoc.getY() == _targetLoc.getY() && _originLoc.getZ() == _targetLoc.getZ())
			{
				activeChar.stopMove(activeChar.getLocation());
				activeChar.sendActionFailed();
				return;
			}
			
			if (activeChar.isInFrontDoor(_targetLoc.getX(), _targetLoc.getY(), _originLoc.getZ(), activeChar.getReflection()))
			{
				activeChar.stopMove(null);
				activeChar.sendActionFailed();
				return;
			}
			
			final int curX = activeChar.getX();
			final int curY = activeChar.getY();
			final int curZ = activeChar.getZ();
			
			final double dx = _targetLoc.getX() - curX;
			final double dy = _targetLoc.getY() - curY;
			final double fullDist = Math.sqrt(dx * dx + dy * dy);
			if (fullDist > 1)
			{
				final double normalX = dx / fullDist;
				final double normalY = dy / fullDist;
				
				final int colRadius = (int) activeChar.getColRadius();
				for (int checkDist = 10; checkDist <= 30; checkDist += 10)
				{
					final int checkX = curX + (int) (normalX * checkDist);
					final int checkY = curY + (int) (normalY * checkDist);
					
					if (!GeoEngine.getInstance().canMoveToCoord(activeChar, curX, curY, curZ, checkX, checkY, curZ, activeChar.getReflection(), true))
					{
						activeChar.stopMove(activeChar.getLocation());
						activeChar.broadcastPacket(new StopMove(activeChar));
						activeChar.sendActionFailed();
						return;
					}
					
					final double perpX = -normalY;
					final double perpY = normalX;
					
					final int leftX = checkX + (int) (perpX * colRadius);
					final int leftY = checkY + (int) (perpY * colRadius);
					if (!GeoEngine.getInstance().canMoveToCoord(activeChar, curX, curY, curZ, leftX, leftY, curZ, activeChar.getReflection(), true))
					{
						activeChar.stopMove(activeChar.getLocation());
						activeChar.broadcastPacket(new StopMove(activeChar));
						activeChar.sendActionFailed();
						return;
					}
					
					final int rightX = checkX - (int) (perpX * colRadius);
					final int rightY = checkY - (int) (perpY * colRadius);
					if (!GeoEngine.getInstance().canMoveToCoord(activeChar, curX, curY, curZ, rightX, rightY, curZ, activeChar.getReflection(), true))
					{
						activeChar.stopMove(activeChar.getLocation());
						activeChar.broadcastPacket(new StopMove(activeChar));
						activeChar.sendActionFailed();
						return;
					}
				}
				
				final int checkX = curX + (int) (normalX * 20);
				final int checkY = curY + (int) (normalY * 20);
				final int futureZ = GeoEngine.getInstance().getHeight(checkX, checkY, curZ);
				if (Math.abs(futureZ - curZ) > 64)
				{
					activeChar.stopMove(activeChar.getLocation());
					return;
				}
			}
			
			final var locx = GeoEngine.getInstance().moveCheck(activeChar, activeChar.getLocation(), _targetLoc, activeChar.getReflection(), Math.abs(activeChar.getZ() - _targetLoc.getZ()) < 100);
			_targetLoc = locx;
			_targetLoc.setWasdMove(true);
			activeChar.setStartLocWASD(_originLoc);
		}
		else
		{
			activeChar.setLastMovePacket();
		}
		
		if (activeChar.isAttackingNow())
		{
			activeChar.getAI().setIntention(CtrlIntention.IDLE);
		}

		if (_moveMovement != 0)
		{
			activeChar.setLastMovePacket();
		}
		
		if ((Config.PLAYER_MOVEMENT_BLOCK_TIME > 0) && !activeChar.isGM() && (activeChar.getNotMoveUntil() > System.currentTimeMillis()))
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_MOVE_WHILE_SPEAKING_TO_AN_NPC);
			activeChar.sendActionFailed();
			return;
		}

		if ((_targetLoc.getX() == _originLoc.getX()) && (_targetLoc.getY() == _originLoc.getY()) && (_targetLoc.getZ() == _originLoc.getZ()))
		{
			if (_moveMovement == 0)
			{
				activeChar.stopMove(null);
			}
			else
			{
				activeChar.sendPacket(new StopMove(activeChar));
			}
			return;
		}
		_targetLoc.setZ((int) (_targetLoc.getZ() + activeChar.getColHeight()));

		if (activeChar.getTeleMode() > 0)
		{
			Location loc = null;
			if (activeChar.getTeleMode() == 1)
			{
				loc = GeoEngine.getInstance().moveCheck(activeChar, activeChar.getX(), activeChar.getY(), activeChar.getZ(), _targetLoc.getX(), _targetLoc.getY(), activeChar.getZ(), activeChar.getReflection());
				activeChar.setTeleMode(0);
			}
			else if (activeChar.getTeleMode() == 2)
			{
				loc = _targetLoc;
			}
			activeChar.sendActionFailed();
			activeChar.teleToLocation(loc.getX(), loc.getY(), loc.getZ(), false, activeChar.getReflection());
			return;
		}

		if (activeChar.isControllingFakePlayer())
		{
			final var fakePlayer = activeChar.getPlayerUnderControl();
			activeChar.sendActionFailed();
			fakePlayer.getAI().setIntention(CtrlIntention.MOVING, _targetLoc, 0);
			return;
		}
		
		final double dx = _targetLoc.getX() - activeChar.getX();
		final double dy = _targetLoc.getY() - activeChar.getY();
		
		if (activeChar.isOutOfControl() || (((dx * dx) + (dy * dy)) > 98010000))
		{
			activeChar.sendActionFailed();
			return;
		}
		activeChar.setFallingLoc(_targetLoc);
		activeChar.getAI().setIntention(CtrlIntention.MOVING, _targetLoc, 0);
		if (activeChar.isInDrawZone())
		{
			activeChar.addDrawCoords(_targetLoc);
		}
	}
}