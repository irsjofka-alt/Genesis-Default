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
package gameserver.model.entity.events.model.template;

import java.util.List;

import l2e.commons.collections.MultiValueSet;
import gameserver.Config;

public class FightEventSet
{
	private final MultiValueSet<String> _set;
	private final List<FightEventItem> _items;
	
	public FightEventSet(MultiValueSet<String> set, List<FightEventItem> items)
    {
		_set = set;
		_items = items;
    }
	
	public int getId()
	{
		return _set.getInteger("id");
	}

	public MultiValueSet<String> getSet()
	{
		return _set;
	}
	
	public String getName(String lang)
	{
		try
		{
			return _set.getString(lang != null ? "name" + lang.substring(0, 1).toUpperCase() + lang.substring(1) : "name" + Config.MULTILANG_DEFAULT.substring(0, 1).toUpperCase() + Config.MULTILANG_DEFAULT.substring(1));
		}
		catch (final IllegalArgumentException _)
		{
			return "";
		}
	}
	
	public List<FightEventItem> getItems()
	{
		return _items;
	}
}