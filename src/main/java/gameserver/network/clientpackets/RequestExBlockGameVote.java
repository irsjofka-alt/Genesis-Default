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

public final class RequestExBlockGameVote extends GameClientPacket
{
	int _arena;
	int _answer;
	
	@Override
	protected void readImpl()
	{
		_arena = readD() + 1;
		_answer = readD();
	}

	@Override
	public void runImpl()
	{
		final Player player = getClient().getActiveChar();
		
		if (player == null)
		{
			return;
		}
		
		switch (_answer)
		{
			case 0 :
				break;
			case 1 :
				HandysBlockCheckerManager.getInstance().increaseArenaVotes(_arena);
				break;
			default :
				_log.warn("Unknown Cube Game Answer ID: " + _answer);
				break;
		}
	}
}