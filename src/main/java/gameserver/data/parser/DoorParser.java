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
package gameserver.data.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import gameserver.data.DocumentParser;
import gameserver.idfactory.IdFactory;
import gameserver.instancemanager.MapRegionManager;
import gameserver.model.Location;
import gameserver.model.actor.instance.DoorInstance;
import gameserver.model.actor.templates.door.DoorTemplate;
import gameserver.model.entity.Reflection;
import gameserver.model.stats.StatsSet;

public class DoorParser extends DocumentParser
{
	private final Map<Integer, DoorInstance> _doors = new HashMap<>();
	private static final Map<String, Set<Integer>> _groups = new HashMap<>();
	private final Map<Integer, List<DoorInstance>> _regions = new HashMap<>();
	
	protected DoorParser()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_doors.clear();
		_groups.clear();
		_regions.clear();
		parseDatapackFile("data/stats/regions/doors.xml");
	}
	
	@Override
	protected void reloadDocument()
	{
	}
	
	@Override
	protected void parseDocument()
	{
		NamedNodeMap attrs;
		Node att;
		StatsSet set;
		for (Node a = getCurrentDocument().getFirstChild(); a != null; a = a.getNextSibling())
		{
			if ("list".equalsIgnoreCase(a.getNodeName()))
			{
				for (Node b = a.getFirstChild(); b != null; b = b.getNextSibling())
				{
					if ("door".equalsIgnoreCase(b.getNodeName()))
					{
						attrs = b.getAttributes();
						set = new StatsSet();
						set.set("baseHpMax", 1);
						for (int i = 0; i < attrs.getLength(); i++)
						{
							att = attrs.item(i);
							set.set(att.getNodeName(), att.getNodeValue());
						}
						makeDoor(set);
					}
				}
			}
		}
		info("Loaded " + _doors.size() + " door templates.");
	}
	
	public void insertCollisionData(StatsSet set)
	{
		int posX, posY, nodeX, nodeY, height;
		height = set.getInteger("height");
		String[] pos = set.getString("node1").split(",");
		nodeX = Integer.parseInt(pos[0]);
		nodeY = Integer.parseInt(pos[1]);
		pos = set.getString("node2").split(",");
		posX = Integer.parseInt(pos[0]);
		posY = Integer.parseInt(pos[1]);
		int collisionRadius;
		collisionRadius = Math.min(Math.abs(nodeX - posX), Math.abs(nodeY - posY));
		if (collisionRadius < 20)
		{
			collisionRadius = 20;
		}
		set.set("collision_radius", collisionRadius);
		set.set("collision_height", height / 4);
	}
	
	private void makeDoor(StatsSet set)
	{
		insertCollisionData(set);
		
		final DoorTemplate template = new DoorTemplate(set);
		final DoorInstance door = new DoorInstance(IdFactory.getInstance().getNextId(), template, set);
		door.setCurrentHp(door.getMaxHp());
		
		final int x = template.posX;
		final int y = template.posY;
		final int z = template.posZ;
		
		final int gz = z + 32;
		door.spawnMe(x, y, gz);
		putDoor(door, MapRegionManager.getInstance().getMapRegionLocId(door));
	}
	
	public DoorTemplate getDoorTemplate(int doorId)
	{
		if (_doors.containsKey(doorId))
		{
			return _doors.get(doorId).getTemplate();
		}
		return null;
	}
	
	public DoorInstance getDoor(int doorId)
	{
		return _doors.get(doorId);
	}
	
	public void putDoor(DoorInstance door, int region)
	{
		_doors.put(door.getDoorId(), door);
		if (!_regions.containsKey(region))
		{
			_regions.put(region, new ArrayList<>());
		}
		_regions.get(region).add(door);
	}
	
	public static void addDoorGroup(String groupName, int doorId)
	{
		Set<Integer> set = _groups.get(groupName);
		if (set == null)
		{
			set = new HashSet<>();
			_groups.put(groupName, set);
		}
		set.add(doorId);
	}
	
	public static Set<Integer> getDoorsByGroup(String groupName)
	{
		return _groups.get(groupName);
	}
	
	public Collection<DoorInstance> getDoors()
	{
		return _doors.values();
	}
	
	public List<DoorInstance> getRegionDoors(Location loc, Reflection r)
	{
		final var allDoors = !r.isDefault() ? r.getDoors() : _regions.get(MapRegionManager.getInstance().getMapRegionLocId(loc.getX(), loc.getY()));
		if (allDoors == null)
		{
			return Collections.emptyList();
		}
		return allDoors;
	}
	
	public static DoorParser getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final DoorParser _instance = new DoorParser();
	}
}