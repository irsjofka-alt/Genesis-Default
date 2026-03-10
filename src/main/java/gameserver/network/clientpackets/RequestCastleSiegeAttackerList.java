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

import gameserver.instancemanager.CHSiegeManager;
import gameserver.instancemanager.CastleManager;
import gameserver.instancemanager.FortManager;
import gameserver.network.serverpackets.CastleSiegeAttackerList;

public final class RequestCastleSiegeAttackerList extends GameClientPacket
{
	private int _castleId;
	
	@Override
	protected void readImpl()
	{
		_castleId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final var castle = CastleManager.getInstance().getCastleById(_castleId);
		if (castle != null)
		{
			final var sal = new CastleSiegeAttackerList(castle);
			sendPacket(sal);
		}
		else
		{
			final var fort = FortManager.getInstance().getFort(_castleId);
			if (fort != null)
			{
				final var sal = new CastleSiegeAttackerList(fort);
				sendPacket(sal);
			}
			else
			{
				final var hall = CHSiegeManager.getInstance().getSiegableHall(_castleId);
				if (hall != null)
				{
					final var sal = new CastleSiegeAttackerList(hall);
					sendPacket(sal);
				}
			}
		}
	}
}