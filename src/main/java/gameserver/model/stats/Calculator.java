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
package gameserver.model.stats;

import java.util.EnumSet;
import java.util.Set;

import gameserver.model.skills.funcs.Func;

public final class Calculator
{
	private Func[] _functions;
	
	public Calculator()
	{
		_functions = new Func[0];
	}
	
	public Calculator(Calculator c)
	{
		_functions = c._functions;
	}
	
	public static boolean equalsCals(Calculator c1, Calculator c2)
	{
		if (c1 == c2)
		{
			return true;
		}
		
		if (c1 == null || c2 == null)
		{
			return false;
		}
		
		final var funcs1 = c1._functions;
		final var funcs2 = c2._functions;
		
		if (funcs1 == funcs2)
		{
			return true;
		}
		
		if (funcs1.length != funcs2.length)
		{
			return false;
		}
		
		if (funcs1.length == 0)
		{
			return true;
		}
		
		for (int i = 0; i < funcs1.length; i++)
		{
			if (funcs1[i] != funcs2[i])
			{
				return false;
			}
		}
		return true;
		
	}
	
	public int size()
	{
		return _functions.length;
	}
	
	public synchronized void addFunc(Func f)
	{
		final var funcs = _functions;
		final var tmp = new Func[funcs.length + 1];
		
		final int order = f.order;
		int i;
		
		for (i = 0; i < funcs.length && order >= funcs[i].order; i++)
		{
			tmp[i] = funcs[i];
		}
		
		tmp[i] = f;
		
		for (; i < funcs.length; i++)
		{
			tmp[i + 1] = funcs[i];
		}
		
		_functions = tmp;
	}
	
	public synchronized void removeFunc(Func f)
	{
		final var funcs = _functions;
		final var tmp = new Func[funcs.length - 1];
		
		int i;
		
		for (i = 0; i < funcs.length && f != funcs[i]; i++)
		{
			tmp[i] = funcs[i];
		}
		
		if (i == funcs.length)
		{
			return;
		}
		
		for (i++; i < funcs.length; i++)
		{
			tmp[i - 1] = funcs[i];
		}
		
		if (tmp.length == 0)
		{
			_functions = new Func[0];
		}
		else
		{
			_functions = tmp;
		}
	}
	
	public synchronized Set<Stats> removeOwner(Object owner)
	{
		final Set<Stats> modifiedStats = EnumSet.noneOf(Stats.class);
		for (final var func : _functions)
		{
			if (func.funcOwner == owner)
			{
				modifiedStats.add(func.stat);
				removeFunc(func);
			}
		}
		return modifiedStats;
		
	}
	
	public void calc(Env env)
	{
		for (final var func : _functions)
		{
			func.calc(env);
		}
	}
}