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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import l2e.commons.collections.MultiValueSet;
import l2e.commons.geometry.Polygon;
import l2e.commons.time.cron.SchedulingPattern;
import gameserver.Config;
import gameserver.data.DocumentParser;
import gameserver.instancemanager.BloodAltarManager;
import gameserver.instancemanager.DayNightSpawnManager;
import gameserver.instancemanager.RaidBossSpawnManager;
import gameserver.instancemanager.ReflectionManager;
import gameserver.model.Location;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.actor.templates.npc.NpcTemplate;
import gameserver.model.spawn.SpawnNpcInfo;
import gameserver.model.spawn.SpawnTemplate;
import gameserver.model.spawn.SpawnTerritory;
import gameserver.model.spawn.Spawner;
import gameserver.model.stats.StatsSet;
import gameserver.taskmanager.RaidBossTaskManager;

public final class SpawnParser extends DocumentParser
{
	private final Set<Spawner> _spawnParser = ConcurrentHashMap.newKeySet();
	private final Map<String, List<Spawner>> _spawns = new ConcurrentHashMap<>();
	private final Map<Integer, List<Location>> _spawnLocationsByNpcId = new HashMap<>();

	protected SpawnParser()
	{
		if (!Config.ALT_DEV_NO_SPAWNS)
		{
			load();
		}
	}
	
	@Override
	public final void load()
	{
		_spawnParser.clear();
		_spawns.clear();
		_spawnLocationsByNpcId.clear();
		parseDirectory("data/stats/npcs/spawns", true, false);
		if (size() > 0)
		{
			info("Loaded " + size() + " npc spawn templates.");
			RaidBossTaskManager.getInstance().recalcAll();
		}
	}
	
	@Override
	protected void reloadDocument()
	{
	}
	
	@Override
	protected void parseDocument()
	{
		final Map<String, SpawnTerritory> territories = new HashMap<>();
		for (var c = getCurrentDocument().getFirstChild(); c != null; c = c.getNextSibling())
		{
			if ("list".equalsIgnoreCase(c.getNodeName()))
			{
				for (var n = c.getFirstChild(); n != null; n = n.getNextSibling())
				{
					if ("territory".equalsIgnoreCase(n.getNodeName()))
					{
						final var list = n.getAttributes();

						final var terName = list.getNamedItem("name").getNodeValue();
						final var territory = parseTerritory(terName, n, list);
						territories.put(terName, territory);
					}
					else if ("spawn".equalsIgnoreCase(n.getNodeName()))
					{
						final var list = n.getAttributes();

						boolean spawned;

						final int count = list.getNamedItem("count") == null ? 1 : Integer.parseInt(list.getNamedItem("count").getNodeValue());
						final int respawn = list.getNamedItem("respawn") == null ? 60 : Integer.parseInt(list.getNamedItem("respawn").getNodeValue());
						final int respawnRandom = list.getNamedItem("respawn_random") == null ? 0 : Integer.parseInt(list.getNamedItem("respawn_random").getNodeValue());
						final String respawnPattern = list.getNamedItem("respawn_pattern") == null ? null : list.getNamedItem("respawn_pattern").getNodeValue();
						final String respawnTime = list.getNamedItem("respawn_time") == null ? null : list.getNamedItem("respawn_time").getNodeValue();
						final String periodOfDay = list.getNamedItem("period_of_day") == null ? "none" : list.getNamedItem("period_of_day").getNodeValue();
						final String minionList = list.getNamedItem("minionList") == null ? null : list.getNamedItem("minionList").getNodeValue();
						final String group;
						String territoryName = "";
						boolean allowSpawnModifier = true;
						if (list.getNamedItem("group") == null)
						{
							group = periodOfDay;
							spawned = true;
						}
						else
						{
							group = list.getNamedItem("group").getNodeValue();
							spawned = false;
						}
						final var template = new SpawnTemplate(periodOfDay, count, respawn, respawnRandom);
						int npcId = 0;
						int x = 0;
						int y = 0;
						int z = 0;
						int h = -1;
						for (var d = n.getFirstChild(); d != null; d = d.getNextSibling())
						{
							if ("debug".equalsIgnoreCase(d.getNodeName()))
							{
								final var debug = d.getAttributes();
								if (debug.getNamedItem("name") != null)
								{
									territoryName = debug.getNamedItem("val") == null ? "No Name" : debug.getNamedItem("val").getNodeValue();
								}
							}
							else if ("point".equalsIgnoreCase(d.getNodeName()))
							{
								final var point = d.getAttributes();

								x = Integer.parseInt(point.getNamedItem("x").getNodeValue());
								y = Integer.parseInt(point.getNamedItem("y").getNodeValue());
								z = Integer.parseInt(point.getNamedItem("z").getNodeValue());
								h = point.getNamedItem("h") == null ? -1 : Integer.parseInt(point.getNamedItem("h").getNodeValue());
								
								template.addSpawnRange(new Location(x, y, z, h));
								allowSpawnModifier = false;
							}
							else if ("territory".equalsIgnoreCase(d.getNodeName()))
							{
								final var territory = d.getAttributes();
								final var terName = territory.getNamedItem("name") == null ? null : territory.getNamedItem("name").getNodeValue();
								if (terName != null)
								{
									final var g = territories.get(terName);
									if (g == null)
									{
										warn("Invalid territory name: " + terName + "; " + getClass().getSimpleName());
										continue;
									}
									template.addSpawnRange(g);
								}
								else
								{
									final var temp = parseTerritory(null, d, territory);
									template.addSpawnRange(temp);
								}
							}
							else if ("npc".equalsIgnoreCase(d.getNodeName()))
							{
								npcId = Integer.parseInt(d.getAttributes().getNamedItem("id").getNodeValue());
								final int amount = d.getAttributes().getNamedItem("count") == null ? count : Integer.parseInt(d.getAttributes().getNamedItem("count").getNodeValue());
								final int perRespawn = d.getAttributes().getNamedItem("respawn") == null ? respawn : Integer.parseInt(d.getAttributes().getNamedItem("respawn").getNodeValue());
								final int perRespawnRandom = d.getAttributes().getNamedItem("respawn_random") == null ? respawnRandom : Integer.parseInt(d.getAttributes().getNamedItem("respawn_random").getNodeValue());
								final var perRespawnPattern = d.getAttributes().getNamedItem("respawn_pattern") == null ? respawnPattern : d.getAttributes().getNamedItem("respawn_pattern").getNodeValue();
								final var perRespawnTime = d.getAttributes().getNamedItem("respawn_time") == null ? respawnTime : d.getAttributes().getNamedItem("respawn_time").getNodeValue();
								final int max = d.getAttributes().getNamedItem("max") == null ? 0 : Integer.parseInt(d.getAttributes().getNamedItem("max").getNodeValue());
								final var name = d.getAttributes().getNamedItem("name") == null ? null : d.getAttributes().getNamedItem("name").getNodeValue();
								final var value = d.getAttributes().getNamedItem("value") == null ? null : d.getAttributes().getNamedItem("value").getNodeValue();
								MultiValueSet<String> parameters = StatsSet.EMPTY;
								for (var e = d.getFirstChild(); e != null; e = e.getNextSibling())
								{
									if (parameters.isEmpty())
									{
										parameters = new MultiValueSet<>();
									}
									parameters.set(name, value);
								}
								template.addNpc(new SpawnNpcInfo(npcId, max, parameters));
								parseNpcSpawn(npcId, amount, group, template, territoryName, perRespawn, perRespawnRandom, perRespawnPattern, perRespawnTime, periodOfDay, minionList, spawned, allowSpawnModifier);
							}
						}
					}
				}
			}
		}
	}
	
	private void parseNpcSpawn(int npcId, int count, String group, SpawnTemplate temp, String territoryName, int respawn, int respawnRandom, String respawnPattern, String respawnTime, String periodOfDay, String minionList, boolean spawned, boolean allowSpawnModifier)
	{
		int totalCount = 0;
		
		final var npcTemplate = NpcsParser.getInstance().getTemplate(npcId);
		if (npcTemplate != null)
		{
			if (npcTemplate.isType("SiegeGuard"))
			{}
			else if (!ClassMasterParser.getInstance().isAllowClassMaster() && npcTemplate.isType("ClassMaster"))
			{
				return;
			}
			else if (Config.ALT_CHEST_NO_SPAWNS && npcTemplate.isType("TreasureChest"))
			{
				return;
			}
			else
			{
				if (npcTemplate.isType("Monster"))
				{
					if (allowSpawnModifier)
					{
						count *= Config.SPAWN_MULTIPLIER;
					}
					respawn *= Config.RESPAWN_MULTIPLIER;
					respawnRandom *= Config.RESPAWN_MULTIPLIER;
				}
				while (totalCount < count)
				{
					try
					{
						final var spawnDat = new Spawner(npcTemplate);
						spawnDat.setAmount(1);
						spawnDat.setTerritoryName(territoryName);
						spawnDat.setSpawnTemplate(temp);
						spawnDat.setLocation(spawnDat.calcSpawnRangeLoc(npcTemplate));
						if (minionList != null)
						{
							spawnDat.setMinionList(minionList);
						}
						
						spawnDat.setRespawnPattern(respawnPattern == null || respawnPattern.isEmpty() ? null : new SchedulingPattern(respawnPattern));
						spawnDat.setRespawnTime(respawnTime);
						spawnDat.setRespawnDelay(respawn, respawnRandom);
						if (npcTemplate.isType("RaidBoss") || npcTemplate.isType("FlyRaidBoss"))
						{
							RaidBossSpawnManager.getInstance().addNewSpawn(spawnDat, false);
							spawned = false;
							if (Config.DEBUG_SPAWN)
							{
								final var npc = spawnDat.getLastSpawn();
								if (npc != null)
								{
									if (!npc.isInRangeZ(npc.getSpawn().getLocation(), Config.MAX_DRIFT_RANGE))
									{
										warn("npcId[" + npc.getId() + "] z coords bug! [" + npc.getZ() + "] != [" + spawnDat.getZ() + "] - [" + spawnDat.getX() + " " + spawnDat.getY() + " " + spawnDat.getZ() + "]");
									}
								}
							}
						}
						else
						{
							if (respawn == 0)
							{
								spawnDat.stopRespawn();
							}
							else
							{
								spawnDat.startRespawn();
							}
						}
						
						final var spawnLoc = spawnDat.calcSpawnRangeLoc(npcTemplate);
						if (!_spawnLocationsByNpcId.containsKey(npcTemplate.getId()))
						{
							_spawnLocationsByNpcId.put(npcTemplate.getId(), new ArrayList<>());
						}
						_spawnLocationsByNpcId.get(npcTemplate.getId()).add(spawnLoc);
						
						totalCount++;
						
						switch (periodOfDay)
						{
							case "none" :
								if (spawned)
								{
									spawnDat.doSpawn();
									if (Config.DEBUG_SPAWN)
									{
										final var npc = spawnDat.getLastSpawn();
										if (npc != null && npc.isMonster())
										{
											if (!npc.isInRangeZ(npc.getSpawn().getLocation(), Config.MAX_DRIFT_RANGE))
											{
												warn("npcId[" + npc.getId() + "] z coords bug! [" + npc.getZ() + "] != [" + spawnDat.getZ() + "] - [" + spawnDat.getX() + " " + spawnDat.getY() + " " + spawnDat.getZ() + "]");
											}
										}
									}
								}
								break;
							case "day" :
								if (spawned)
								{
									DayNightSpawnManager.getInstance().addDayCreature(spawnDat);
								}
								break;
							case "night" :
								if (spawned)
								{
									DayNightSpawnManager.getInstance().addNightCreature(spawnDat);
								}
								break;
						}
						addSpawn(group, spawnDat);
						addNewSpawn(spawnDat);
					}
					catch (final Exception e)
					{
						warn("Spawn could not be initialized: " + e.getMessage(), e);
					}
				}
			}
		}
	}

	private SpawnTerritory parseTerritory(String name, Node n, NamedNodeMap attrs)
	{
		final var t = new SpawnTerritory();
		t.add(parsePolygon0(name, n, attrs));
		for (var b = n.getFirstChild(); b != null; b = b.getNextSibling())
		{
			if ("banned_territory".equalsIgnoreCase(b.getNodeName()))
			{
				t.addBanned(parsePolygon0(name, b, b.getAttributes()));
			}
		}
		return t;
	}

	private Polygon parsePolygon0(String name, Node n, NamedNodeMap attrs)
	{
		final var temp = new Polygon();
		for (var cd = n.getFirstChild(); cd != null; cd = cd.getNextSibling())
		{
			if ("add".equalsIgnoreCase(cd.getNodeName()))
			{
				attrs = cd.getAttributes();
				final int x = Integer.parseInt(attrs.getNamedItem("x").getNodeValue());
				final int y = Integer.parseInt(attrs.getNamedItem("y").getNodeValue());
				final int zmin = Integer.parseInt(attrs.getNamedItem("zmin").getNodeValue());
				final int zmax = Integer.parseInt(attrs.getNamedItem("zmax").getNodeValue());
				temp.add(x, y).setZmin(zmin).setZmax(zmax);
			}
		}

		if (!temp.validate())
		{
			warn("Invalid polygon: " + name + "{" + temp + "}. File: " + getClass().getSimpleName());
		}
		return temp;
	}
	
	public void reloadAll()
	{
		load();
	}

	public void addSpawn(String group, Spawner spawn)
	{
		var spawns = _spawns.get(group);
		if (spawns == null)
		{
			_spawns.put(group, (spawns = new ArrayList<>()));
		}
		spawns.add(spawn);
	}
	
	public List<Spawner> getSpawn(String name)
	{
		final var template = _spawns.get(name);
		return template == null ? Collections.<Spawner> emptyList() : template;
	}
	
	public int size()
	{
		int i = 0;
		for (final var l : _spawns.values())
		{
			i += l.size();
		}
		return i;
	}
	
	public Map<String, List<Spawner>> getSpawns()
	{
		return _spawns;
	}
	
	public void addNewSpawn(Spawner spawn)
	{
		if (!_spawnParser.contains(spawn))
		{
			_spawnParser.add(spawn);
		}
	}

	public boolean deleteSpawn(Spawner spawn)
	{
		return _spawnParser.remove(spawn);
	}
	
	public Collection<Spawner> getAllSpawns()
	{
		return _spawnParser;
	}
	
	public Set<Spawner> getSpawnData()
	{
		return _spawnParser;
	}
	
	public void spawnGroup(String... groups)
	{
		for (final var group : groups)
		{
			final var spawnerList = _spawns.get(group);
			if (spawnerList == null)
			{
				return;
			}
			
			int npcSpawnCount = 0;
			
			for (final var spawner : spawnerList)
			{
				if (spawner.getTemplate().getParameter("isDestructionBoss", false))
				{
					BloodAltarManager.getInstance().addBossSpawn(spawner);
				}
				else
				{
					npcSpawnCount += spawner.init();
				}
			}
			
			if (Config.DEBUG)
			{
				info("Spawned " + npcSpawnCount + " npcs for group: " + group);
			}
		}
	}
	
	public void spawnCheckGroup(String group, List<Integer> npcId)
	{
		final var spawnerList = _spawns.get(group);
		if (spawnerList == null)
		{
			return;
		}
		
		int npcSpawnCount = 0;
		
		for (final var spawner : spawnerList)
		{
			if (npcId != null && npcId.contains(spawner.getId()))
			{
				continue;
			}

			if (spawner.getTemplate().getParameter("isDestructionBoss", false))
			{
				BloodAltarManager.getInstance().addBossSpawn(spawner);
			}
			else
			{
				npcSpawnCount += spawner.init();
			}
		}
		
		if (Config.DEBUG)
		{
			info("Spawned " + npcSpawnCount + " npcs for group: " + group);
		}
	}

	public void despawnGroup(String... groups)
	{
		for (final var group : groups)
		{
			final var spawnerList = _spawns.get(group);
			if (spawnerList == null)
			{
				return;
			}
			
			int npcDespawnSpawn = 0;
			
			for (final var spawner : spawnerList)
			{
				if (spawner.getTemplate().getParameter("isDestructionBoss", false))
				{
					BloodAltarManager.getInstance().removeBossSpawn(spawner);
				}
				spawner.stopRespawn();
				final var last = spawner.getLastSpawn();
				if (last != null)
				{
					npcDespawnSpawn++;
					last.onDecay();
				}
			}
			
			if (npcDespawnSpawn != 0 && Config.DEBUG)
			{
				info("Despawned " + npcDespawnSpawn + " npcs for group: " + group);
			}
		}
	}
	
	public int getSpawnedCountByNpc(int npcId)
	{
		if (!_spawnLocationsByNpcId.containsKey(npcId))
		{
			return 0;
		}
		return _spawnLocationsByNpcId.get(npcId).size();
	}

	public List<Location> getRandomSpawnsByNpc(int npcId)
	{
		return _spawnLocationsByNpcId.get(npcId);
	}

	public void addRandomSpawnByNpc(Spawner spawnDat, NpcTemplate npcTemplate)
	{
		final var spawnLoc = spawnDat.calcSpawnRangeLoc(npcTemplate);
		if (!_spawnLocationsByNpcId.containsKey(npcTemplate.getId()))
		{
			_spawnLocationsByNpcId.put(npcTemplate.getId(), new ArrayList<>());
		}
		_spawnLocationsByNpcId.get(npcTemplate.getId()).add(spawnLoc);
	}
	
	public void removeRandomSpawnByNpc(Npc npc)
	{
		final var spawnLoc = npc.getSpawn().calcSpawnRangeLoc(npc.getTemplate());
		if (_spawnLocationsByNpcId.containsKey(npc.getId()))
		{
			_spawnLocationsByNpcId.get(npc.getId()).remove(spawnLoc);
		}
	}
	
	public void removeAllRandomSpawnByNpc(Integer... npcId)
	{
		for (final var i : npcId)
		{
			if (_spawnLocationsByNpcId.containsKey(i))
			{
				_spawnLocationsByNpcId.get(i).clear();
			}
		}
	}
	
	public void findNPCInstances(Player activeChar, int npcId, int teleportIndex, boolean showposition)
	{
		int index = 0;
		for (final var spawn : _spawnParser)
		{
			if (spawn != null && npcId == spawn.getId())
			{
				index++;
				final var _npc = spawn.getLastSpawn();
				if (teleportIndex > -1)
				{
					if (teleportIndex == index)
					{
						if (showposition && (_npc != null))
						{
							activeChar.teleToLocation(_npc.getX(), _npc.getY(), _npc.getZ(), true, ReflectionManager.DEFAULT);
						}
						else
						{
							activeChar.teleToLocation(spawn.getX(), spawn.getY(), spawn.getZ(), true, ReflectionManager.DEFAULT);
						}
					}
				}
				else
				{
					if (showposition && (_npc != null))
					{
						activeChar.sendMessage(index + " - " + spawn.getTemplate().getName(activeChar.getLang()) + " (" + spawn + "): " + _npc.getX() + " " + _npc.getY() + " " + _npc.getZ());
					}
					else
					{
						activeChar.sendMessage(index + " - " + spawn.getTemplate().getName(activeChar.getLang()) + " (" + spawn + "): " + spawn.getX() + " " + spawn.getY() + " " + spawn.getZ());
					}
				}
			}
		}
		
		if (index == 0)
		{
			activeChar.sendMessage("No current spawns found.");
		}
	}
	
	public static SpawnParser getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final SpawnParser _instance = new SpawnParser();
	}
}
