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
package gameserver.model.actor.templates.residences.clanhall;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import gameserver.Config;
import gameserver.model.base.FunctionType;
import gameserver.model.stats.StatsSet;

public class ClanHallTemplate
{
	private final int _id;
	private final StatsSet _params;
	private final Map<FunctionType, Set<FunctionTemplate>> _functions;
	
	public ClanHallTemplate(int id, StatsSet params, Map<FunctionType, Set<FunctionTemplate>> functions)
	{
		_id = id;
		_params = params;
		_functions = functions;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public String getName(String lang)
	{
		try
		{
			return _params.getString(lang != null ? "name" + lang.substring(0, 1).toUpperCase() + lang.substring(1) : "name" + Config.MULTILANG_DEFAULT.substring(0, 1).toUpperCase() + Config.MULTILANG_DEFAULT.substring(1));
		}
		catch (final IllegalArgumentException e)
		{
			return "";
		}
	}
	
	public String getDescr(String lang)
	{
		try
		{
			return _params.getString(lang != null ? "descr" + lang.substring(0, 1).toUpperCase() + lang.substring(1) : "descr" + Config.MULTILANG_DEFAULT.substring(0, 1).toUpperCase() + Config.MULTILANG_DEFAULT.substring(1));
		}
		catch (final IllegalArgumentException e)
		{
			return "";
		}
	}
	
	public String getLocation(String lang)
	{
		try
		{
			return _params.getString(lang != null ? "loc" + lang.substring(0, 1).toUpperCase() + lang.substring(1) : "loc" + Config.MULTILANG_DEFAULT.substring(0, 1).toUpperCase() + Config.MULTILANG_DEFAULT.substring(1));
		}
		catch (final IllegalArgumentException e)
		{
			return "";
		}
	}
	
	public Map<FunctionType, Set<FunctionTemplate>> getFunctions()
	{
		return _functions;
	}
	
	public StatsSet getParams()
	{
		return _params;
	}
	
	public long getRentPrice()
	{
		return _params.getLong("rentPrice");
	}
	
	public long[] getFunctionPrice(FunctionType type, int power)
	{
		final var list = _functions.get(type);
		if (list != null && !list.isEmpty())
		{
			for (final var tpl : list)
			{
				if (tpl.getPower() == power)
				{
					return tpl.getPrice();
				}
			}
		}
		return null;
	}
	
	public int getFunctionDuration(FunctionType type, int power)
	{
		final var list = _functions.get(type);
		if (list != null && !list.isEmpty())
		{
			for (final var tpl : list)
			{
				if (tpl.getPower() == power)
				{
					return tpl.getDuration();
				}
			}
		}
		return 1;
	}
	
	public FunctionTemplate getFunction(FunctionType type, int power)
	{
		final var list = _functions.get(type);
		if (list != null && !list.isEmpty())
		{
			for (final var tpl : list)
			{
				if (tpl.getPower() == power || power <= 0)
				{
					return tpl;
				}
			}
		}
		return null;
	}
	
	public Set<FunctionTemplate> getFunctions(FunctionType type)
	{
		final var list = _functions.get(type);
		return list == null ? Collections.emptySet() : list;
	}
}