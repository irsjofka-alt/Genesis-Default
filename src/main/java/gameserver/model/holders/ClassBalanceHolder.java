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

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import gameserver.model.base.AttackType;
import gameserver.utils.comparators.AttackTypeComparator;

public class ClassBalanceHolder
{
	private final int _activeClass;
	private final int _targetClass;
	
	private final Map<AttackType, Double> _normalBalance = new ConcurrentHashMap<>();
	private final Map<AttackType, Double> _olyBalance = new ConcurrentHashMap<>();
	
	public ClassBalanceHolder(int activeClass, int target)
	{
		_activeClass = activeClass;
		_targetClass = target;
	}
	
	public void addNormalBalance(AttackType type, double value)
	{
		_normalBalance.put(type, value);
	}
	
	public void addOlyBalance(AttackType type, double value)
	{
		_olyBalance.put(type, value);
	}
	
	public int getTargetClass()
	{
		return _targetClass;
	}
	
	public int getActiveClass()
	{
		return _activeClass;
	}
	
	public Map<AttackType, Double> getNormalBalance()
	{
		final Map<AttackType, Double> map = new TreeMap<>(new AttackTypeComparator());
		map.putAll(_normalBalance);
		
		return map;
	}
	
	public void removeOlyBalance(AttackType type)
	{
		if (_olyBalance.containsKey(type))
		{
			_olyBalance.remove(type);
		}
	}
	
	public double getOlyBalanceValue(AttackType type)
	{
		if (!_olyBalance.containsKey(type))
		{
			return 1.0D;
		}
		return _olyBalance.get(type);
	}
	
	public double getBalanceValue(AttackType type)
	{
		if (!_normalBalance.containsKey(type))
		{
			return 1.0D;
		}
		return _normalBalance.get(type);
	}
	
	public void remove(AttackType type)
	{
		if (_normalBalance.containsKey(type))
		{
			_normalBalance.remove(type);
		}
	}
	
	public Map<AttackType, Double> getOlyBalance()
	{
		final Map<AttackType, Double> map = new TreeMap<>(AttackTypeComparator.getInstance());
		map.putAll(_olyBalance);
		return map;
	}
}