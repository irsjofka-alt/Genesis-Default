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

import gameserver.model.actor.Player;

public final class RewardInfo
{
	private final Player _attacker;
	private long _damage = 0;
	
	public RewardInfo(Player attacker)
	{
		_attacker = attacker;
	}
	
	public Player getAttacker()
	{
		return _attacker;
	}
	
	public void addDamage(long damage)
	{
		_damage += damage;
	}
	
	public long getDamage()
	{
		return _damage;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		
		if (obj instanceof RewardInfo info)
		{
			return (info._attacker == _attacker);
		}
		return false;
	}
	
	@Override
	public int hashCode()
	{
		return _attacker.getObjectId();
	}
}