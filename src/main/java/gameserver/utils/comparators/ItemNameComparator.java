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
package gameserver.utils.comparators;

import java.util.Comparator;

import gameserver.model.actor.Player;
import gameserver.model.entity.auction.Auction;

public class ItemNameComparator implements Comparator<Auction>
{
	private final boolean _rightOrder;
	private final Player _player;
	
	public ItemNameComparator(Player player, boolean rightOrder)
	{
		_player = player;
		_rightOrder = rightOrder;
	}
	
	@Override
	public int compare(Auction o1, Auction o2)
	{
		if (_rightOrder)
		{
			return (o1.getItem().getName(_player.getLang()).compareTo(o2.getItem().getName(_player.getLang())));
		}
		else
		{
			return (o2.getItem().getName(_player.getLang()).compareTo(o1.getItem().getName(_player.getLang())));
		}
	}
}