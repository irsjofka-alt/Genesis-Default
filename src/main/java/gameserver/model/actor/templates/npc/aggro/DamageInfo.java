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
package gameserver.model.actor.templates.npc.aggro;

import gameserver.model.PlayerGroup;

public final class DamageInfo
{
	private final PlayerGroup _group;
	private long _damage = 0;
	
	public DamageInfo(PlayerGroup group)
	{
		_group = group;
	}
	
	public final PlayerGroup getGroup()
	{
		return _group;
	}
	
	public final long getDamage()
	{
		return _damage;
	}
	
	public final void addDamage(long l)
	{
		_damage = Math.min(_damage + l, 1000000000000000L);
	}
}