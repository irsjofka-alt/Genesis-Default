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
package gameserver.network.serverpackets;

import gameserver.model.actor.Creature;
import gameserver.model.actor.Player;
import gameserver.model.actor.instance.ControllableAirShipInstance;

public class MyTargetSelected extends GameServerPacket
{
	private final int _objectId;
	private final int _color;

	public MyTargetSelected(Player player, Creature target)
	{
		_objectId = (target instanceof ControllableAirShipInstance casi) ? casi.getHelmObjectId() : target.getObjectId();
		_color = target.isAutoAttackable(player, false) ? (player.getLevel() - target.getLevel()) : 0;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeD(_objectId);
		writeH(_color);
		writeD(0x00);
	}
}