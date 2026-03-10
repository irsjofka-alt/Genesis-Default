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
package gameserver.listener.player.impl;

import gameserver.listener.player.OnAnswerListener;
import gameserver.model.actor.Player;

public class OfflineFarmAnswerListener implements OnAnswerListener
{
	private final Player _player;
		
	public OfflineFarmAnswerListener(Player player)
	{
		_player = player;
	}
		
	@Override
	public void sayYes()
	{
		if (_player != null && _player.isOnline())
		{
			_player.setInOfflineFarmMode(true);
		}
	}
	
	@Override
	public void sayNo()
	{
		if (_player != null && _player.isOnline())
		{
			_player.setInOfflineFarmMode(false);
		}
	}
}