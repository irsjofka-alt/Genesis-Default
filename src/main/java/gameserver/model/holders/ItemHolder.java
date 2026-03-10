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
package gameserver.model.holders;

import gameserver.model.interfaces.IIdentifiable;

public class ItemHolder implements IIdentifiable
{
	private final int _id;
	private final int _objectId;
	private long _count;
	private long _countMax;
	private final double _chance;
	private int _enchantLevel = 0;

	public ItemHolder(int id, long count)
	{
		_id = id;
		_objectId = -1;
		_count = count;
		_countMax = count;
		_chance = 100.;
	}
	
	public ItemHolder(int id, long count, long countMax)
	{
		_id = id;
		_objectId = -1;
		_count = count;
		_countMax = countMax;
		_chance = 100.;
	}

	public ItemHolder(int id, int objectId, long count)
	{
		_id = id;
		_objectId = objectId;
		_count = count;
		_countMax = count;
		_chance = 100.;
	}
	
	public ItemHolder(int id, long count, double chance)
	{
		_id = id;
		_objectId = -1;
		_count = count;
		_countMax = count;
		_chance = chance;
	}
	
	public ItemHolder(int id, long count, double chance, int enchantLevel)
	{
		_id = id;
		_objectId = -1;
		_count = count;
		_countMax = count;
		_chance = chance;
		_enchantLevel = enchantLevel;
	}
	
	public ItemHolder(int id, long count, long countMax, double chance, int enchantLevel)
	{
		_id = id;
		_objectId = -1;
		_count = count;
		_countMax = countMax;
		_chance = chance;
		_enchantLevel = enchantLevel;
	}

	@Override
	public int getId()
	{
		return _id;
	}

	public int getObjectId()
	{
		return _objectId;
	}

	public long getCount()
	{
		return _count;
	}
	
	public void setCount(long count)
	{
		_count = count;
	}
	
	public void setCountMax(long count)
	{
		_countMax = count;
	}
	
	public long getCountMax()
	{
		return _countMax;
	}
	
	public double getChance()
	{
		return _chance;
	}

	public void setEnchatLevel(int enchantLevel)
	{
		_enchantLevel = enchantLevel;
	}
	
	public int getEnchantLevel()
	{
		return _enchantLevel;
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + ": Id: " + _id + " Count: " + _count;
	}
	
	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}
		
		final ItemHolder that = (ItemHolder) o;
		
		if (_count != that._count)
		{
			return false;
		}
		if (_id != that._id)
		{
			return false;
		}
		
		return true;
	}
	
	@Override
	public int hashCode()
	{
		int result = _id;
		result = 31 * result + (int) (_count ^ (_count >>> 32));
		return result;
	}
}