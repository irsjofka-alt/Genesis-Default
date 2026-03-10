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

import gameserver.instancemanager.HandysBlockCheckerManager;
import gameserver.model.actor.Player;

public final class RequestExBlockGameEnter extends GameClientPacket
{
	int _arena;
	int _team;
	
	@Override
	protected void readImpl()
	{
		_arena = readD() + 1;
		_team = readD();
	}

	@Override
	public void runImpl()
	{
		if (HandysBlockCheckerManager.getInstance().arenaIsBeingUsed(_arena))
		{
			return;
		}
		final Player player = getClient().getActiveChar();
		
		switch (_team)
		{
			case 0 :
			case 1 :
				HandysBlockCheckerManager.getInstance().changePlayerToTeam(player, _arena, _team);
				break;
			case -1 :
			{
				final int team = HandysBlockCheckerManager.getInstance().getHolder(_arena).getPlayerTeam(player);
				
				if (team > -1)
				{
					HandysBlockCheckerManager.getInstance().removePlayer(player, _arena, team);
				}
				break;
			}
			default :
				_log.warn("Wrong Cube Game Team ID: " + _team);
				break;
		}
	}
}