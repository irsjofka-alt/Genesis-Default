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
package gameserver.instancemanager;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import l2e.commons.log.LoggerObject;
import gameserver.database.DatabaseFactory;
import gameserver.model.Clan;
import gameserver.model.GameObject;
import gameserver.model.entity.Fort;

public class FortManager extends LoggerObject
{
	private final Map<Integer, Fort> _forts = new HashMap<>();
	
	public FortManager()
	{
	}
	
	public void load()
	{
		try (var con = DatabaseFactory.getInstance().getConnection())
		{
			final var statement = con.prepareStatement("SELECT id FROM fort ORDER BY id");
			final ResultSet rs = statement.executeQuery();
			
			int i = 0;
			while (rs.next())
			{
				final var id = rs.getInt("id");
				_forts.put(id, new Fort(id, i));
				i++;
			}
			
			rs.close();
			statement.close();
			
			info("Loaded: " + _forts.size() + " fortress");
			_forts.values().stream().filter(f -> f != null).forEach(f -> f.getSiege().getSiegeGuardManager().loadSiegeGuard());
		}
		catch (final Exception e)
		{
			warn("loadFortData(): " + e.getMessage(), e);
		}
	}
	
	public final int findNearestFortIndex(GameObject obj)
	{
		return findNearestFortIndex(obj, Long.MAX_VALUE);
	}
	
	public final int findNearestFortIndex(GameObject obj, long maxDistance)
	{
		int index = getFortIndex(obj);
		if (index < 0)
		{
			double distance;
			for (final var fort : _forts.values())
			{
				if (fort == null)
				{
					continue;
				}
				distance = fort.getDistance(obj);
				if (maxDistance > distance)
				{
					maxDistance = (long) distance;
					index = fort.getIndex();
				}
			}
		}
		return index;
	}
	
	public final List<Fort> getFortsById(int... fortId)
	{
		final List<Fort> forts = new ArrayList<>();
		for (final var i : fortId)
		{
			final var f = _forts.get(i);
			if (f != null)
			{
				forts.add(f);
			}
		}
		return forts;
	}
	
	public final Fort getFortByOwner(Clan clan)
	{
		for (final var fort : _forts.values())
		{
			if (fort != null && fort.getOwnerClan() == clan)
			{
				return fort;
			}
		}
		return null;
	}
	
	public final Fort getFort(String name)
	{
		for (final var fort : _forts.values())
		{
			if (fort != null && fort.getName().equalsIgnoreCase(name.trim()))
			{
				return fort;
			}
		}
		return null;
	}
	
	public final Fort getFort(int x, int y, int z)
	{
		for (final var fort : _forts.values())
		{
			if (fort != null && fort.checkIfInZone(x, y, z))
			{
				return fort;
			}
		}
		return null;
	}
	
	public final Fort getFort(GameObject activeObject)
	{
		return getFort(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}
	
	public final int getFortIndex(int fortId)
	{
		final var fort = _forts.get(fortId);
		if (fort != null)
		{
			return fort.getIndex();
		}
		return -1;
	}
	
	public final int getFortIndex(GameObject activeObject)
	{
		return getFortIndex(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}
	
	public final int getFortIndex(int x, int y, int z)
	{
		for (final var fort : _forts.values())
		{
			if ((fort != null) && fort.checkIfInZone(x, y, z))
			{
				return fort.getIndex();
			}
		}
		return -1;
	}
	
	public final List<Fort> getForts()
	{
		return _forts.values().stream().collect(Collectors.toList());
	}
	
	public final Fort getFort(int id)
	{
		return _forts.get(id);
	}
	
	public void activate()
	{
		_forts.values().stream().filter(f -> f != null).forEach(f -> f.activateInstance());
	}
	
	public static final FortManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final FortManager _instance = new FortManager();
	}
}