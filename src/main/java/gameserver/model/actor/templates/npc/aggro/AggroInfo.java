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

import gameserver.model.actor.Creature;

public final class AggroInfo
{
	private final int _attackerId;
	private long _hate = 0;
	private long _damage = 0;
	
	public AggroInfo(Creature attacker)
	{
		_attackerId = attacker.getObjectId();
	}
	
	public final int getAttackerId()
	{
		return _attackerId;
	}
	
	public final void setHate(long hate)
	{
		_hate = Math.min(Math.max(hate, 0), Long.MAX_VALUE);
	}
	
	public final long getHate()
	{
		return _hate;
	}
	
	public final void addHate(long value)
	{
		_hate = Math.min(Math.max(_hate + value, 0), Long.MAX_VALUE);
	}
	
	public final void reduceHate(long value)
	{
		_hate = Math.min(Math.max(_hate - value, 0), Long.MAX_VALUE);
	}
	
	public final void stopHate()
	{
		_hate = 0;
	}
	
	public final long getDamage()
	{
		return _damage;
	}
	
	public final void addDamage(long value)
	{
		_damage = Math.min(_damage + value, Long.MAX_VALUE);
	}
	
	@Override
	public final boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		
		if (obj instanceof AggroInfo info)
		{
			return (info.getAttackerId() == _attackerId);
		}
		return false;
	}
	
	@Override
	public final int hashCode()
	{
		return _attackerId;
	}
}