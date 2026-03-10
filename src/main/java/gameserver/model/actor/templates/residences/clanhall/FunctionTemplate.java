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

import gameserver.Config;
import gameserver.model.stats.StatsSet;

public class FunctionTemplate
{
	private final int _level;
	private final StatsSet _params;
	
	public FunctionTemplate(int id, StatsSet params)
	{
		_level = id;
		_params = params;
	}
	
	public int getLevel()
	{
		return _level;
	}
	
	public StatsSet getParams()
	{
		return _params;
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
	
	public int getPower()
	{
		return _params.getInteger("power", 0);
	}
	
	public long[] getPrice()
	{
		final String[] price = _params.getString("price", "").split("-");
		if (price.length > 1)
		{
			return new long[]
			{
			        Integer.parseInt(price[0]), Long.parseLong(price[1])
			};
		}
		return null;
	}
	
	public int getDuration()
	{
		return _params.getInteger("duration", 1);
	}
}