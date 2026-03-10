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

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;

import l2e.commons.geometry.Polygon;
import gameserver.Config;
import gameserver.data.DocumentParser;
import gameserver.instancemanager.ReflectionManager;
import gameserver.model.Location;
import gameserver.model.actor.Player;
import gameserver.model.actor.templates.reflection.ReflectionItemTemplate;
import gameserver.model.actor.templates.reflection.ReflectionTemplate;
import gameserver.model.actor.templates.reflection.ReflectionTemplate.ReflectionQuestType;
import gameserver.model.actor.templates.reflection.ReflectionTemplate.ReflectionRemoveType;
import gameserver.model.actor.templates.reflection.ReflectionTemplate.SpawnInfo;
import gameserver.model.holders.ReflectionReenterTimeHolder;
import gameserver.model.holders.SkillHolder;
import gameserver.model.spawn.SpawnTerritory;
import gameserver.model.stats.StatsSet;

public final class ReflectionParser extends DocumentParser
{
	private final IntObjectMap<ReflectionTemplate> _reflections = new HashIntObjectMap<>();
	
	protected ReflectionParser()
	{
		load();
	}

	@Override
	public synchronized void load()
	{
		_reflections.clear();
		parseDirectory("data/stats/instances", false);
		info("Loaded " + _reflections.size() + " reflection templates.");
	}
	
	@Override
	protected void reloadDocument()
	{
	}
	
	@Override
	protected void parseDocument()
	{
		for (var n = getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (var d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("instance".equalsIgnoreCase(d.getNodeName()))
					{
						var ref = d.getAttributes();

						String requiredQuest = null;
						final Map<Integer, StatsSet> doors = new ConcurrentHashMap<>();
						boolean setReuseUponEntry = false, checkAllSkills = false;
						int minLevel = 0, maxLevel = 0, minParty = 1, maxParty = 9, sharedReuseGroup = 0;
						List<Location> teleportLocs = Collections.emptyList();
						Location ret = null;
						int spawnType = 0;
						int mobId, respawn, respawnRnd, count = 0;
						SpawnInfo spawnDat = null;
						final List<ReflectionTemplate.SpawnInfo> spawns = new ArrayList<>();
						Map<String, ReflectionTemplate.SpawnInfo2> spawns2 = Collections.emptyMap();
						final List<ReflectionReenterTimeHolder> resetData = new ArrayList<>();
						
						ReflectionQuestType questType = null;
						
						final int id = Integer.parseInt(ref.getNamedItem("id").getNodeValue());
						final String name = ref.getNamedItem("name").getNodeValue();
						final int maxChannels = Integer.parseInt(ref.getNamedItem("maxChannels").getNodeValue());
						final int collapseIfEmpty = Integer.parseInt(ref.getNamedItem("collapseIfEmpty").getNodeValue());
						final int timelimit = Integer.parseInt(ref.getNamedItem("timelimit").getNodeValue());
						final boolean dispelBuffs = ref.getNamedItem("dispelBuffs") != null ? Boolean.parseBoolean(ref.getNamedItem("dispelBuffs").getNodeValue()) : false;
						final var params = new StatsSet();
						final List<ReflectionItemTemplate> requestItems = new ArrayList<>();
						final List<ReflectionItemTemplate> rewardItems = new ArrayList<>();
						final List<SkillHolder> skills = new ArrayList<>();
						
						for (var cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
						{
							ref = cd.getAttributes();

							if ("level".equalsIgnoreCase(cd.getNodeName()))
							{
								minLevel = ref.getNamedItem("min") == null ? 1 : Integer.parseInt(ref.getNamedItem("min").getNodeValue());
								maxLevel = ref.getNamedItem("max") == null ? Config.PLAYER_MAXIMUM_LEVEL : Math.min(Config.PLAYER_MAXIMUM_LEVEL, Integer.parseInt(ref.getNamedItem("max").getNodeValue()));
							}
							else if ("party".equalsIgnoreCase(cd.getNodeName()))
							{
								minParty = Integer.parseInt(ref.getNamedItem("min").getNodeValue());
								maxParty = Integer.parseInt(ref.getNamedItem("max").getNodeValue());
							}
							else if ("return".equalsIgnoreCase(cd.getNodeName()))
							{
								final int x = Integer.parseInt(ref.getNamedItem("x").getNodeValue());
								final int y = Integer.parseInt(ref.getNamedItem("y").getNodeValue());
								final int z = Integer.parseInt(ref.getNamedItem("z").getNodeValue());
								ret = new Location(x, y, z);
							}
							else if ("teleport".equalsIgnoreCase(cd.getNodeName()))
							{
								if(teleportLocs.isEmpty())
								{
									teleportLocs = new ArrayList<>(1);
								}
								final int x = Integer.parseInt(ref.getNamedItem("x").getNodeValue());
								final int y = Integer.parseInt(ref.getNamedItem("y").getNodeValue());
								final int z = Integer.parseInt(ref.getNamedItem("z").getNodeValue());
								teleportLocs.add(new Location(x, y, z));
							}
							else if ("remove".equalsIgnoreCase(cd.getNodeName()))
							{
								for (var item = cd.getFirstChild(); item != null; item = item.getNextSibling())
								{
									if ("item".equalsIgnoreCase(item.getNodeName()))
									{
										final int itemId = Integer.parseInt(item.getAttributes().getNamedItem("id").getNodeValue());
										final long amount = Long.parseLong(item.getAttributes().getNamedItem("count").getNodeValue());
										final boolean necessary = Boolean.parseBoolean(item.getAttributes().getNamedItem("necessary").getNodeValue());
										final var type = item.getAttributes().getNamedItem("type") != null ? ReflectionRemoveType.valueOf(item.getAttributes().getNamedItem("type").getNodeValue()) : ReflectionRemoveType.NONE;
										requestItems.add(new ReflectionItemTemplate(itemId, amount, necessary, type));
									}
								}
							}
							else if ("give".equalsIgnoreCase(cd.getNodeName()))
							{
								for (var item = cd.getFirstChild(); item != null; item = item.getNextSibling())
								{
									if ("item".equalsIgnoreCase(item.getNodeName()))
									{
										final int itemId = Integer.parseInt(item.getAttributes().getNamedItem("id").getNodeValue());
										final long amount = Long.parseLong(item.getAttributes().getNamedItem("count").getNodeValue());
										rewardItems.add(new ReflectionItemTemplate(itemId, amount, false, ReflectionRemoveType.NONE));
									}
								}
							}
							else if ("quest".equalsIgnoreCase(cd.getNodeName()))
							{
								requiredQuest = ref.getNamedItem("name") != null ? ref.getNamedItem("name").getNodeValue() : null;
								questType = ref.getNamedItem("type") != null ? ReflectionQuestType.valueOf(ref.getNamedItem("type").getNodeValue()) : ReflectionQuestType.STARTED;
							}
							else if ("doorlist".equalsIgnoreCase(cd.getNodeName()))
							{
								for (var door = cd.getFirstChild(); door != null; door = door.getNextSibling())
								{
									int doorId = 0;
									
									if ("door".equalsIgnoreCase(door.getNodeName()))
									{
										doorId = Integer.parseInt(door.getAttributes().getNamedItem("doorId").getNodeValue());
										final var set = new StatsSet();
										for (var bean = door.getFirstChild(); bean != null; bean = bean.getNextSibling())
										{
											if ("set".equalsIgnoreCase(bean.getNodeName()))
											{
												final var attrs = bean.getAttributes();
												final String setname = attrs.getNamedItem("name").getNodeValue();
												final String value = attrs.getNamedItem("val").getNodeValue();
												set.set(setname, value);
											}
										}
										doors.put(doorId, set);
									}
								}
							}
							else if ("reenter".equalsIgnoreCase(cd.getNodeName()))
							{
								setReuseUponEntry = ref.getNamedItem("setUponEntry") != null ? Boolean.parseBoolean(ref.getNamedItem("setUponEntry").getNodeValue()) : false;
								sharedReuseGroup = ref.getNamedItem("sharedReuseGroup") != null ? Integer.parseInt(ref.getNamedItem("sharedReuseGroup").getNodeValue()) : 0;
								for (var rt = cd.getFirstChild(); rt != null; rt = rt.getNextSibling())
								{
									if ("reset".equalsIgnoreCase(rt.getNodeName()))
									{
										final long time = rt.getAttributes().getNamedItem("time") != null ? Long.parseLong(rt.getAttributes().getNamedItem("time").getNodeValue()) : -1;
										if (time > 0)
										{
											resetData.add(new ReflectionReenterTimeHolder(time));
										}
										else if (time == -1)
										{
											final var day = rt.getAttributes().getNamedItem("day") != null ? DayOfWeek.valueOf(rt.getAttributes().getNamedItem("day").getNodeValue().toUpperCase()) : null;
											final int hour = rt.getAttributes().getNamedItem("hour") != null ? Integer.parseInt(rt.getAttributes().getNamedItem("hour").getNodeValue()) : -1;
											final int minute = rt.getAttributes().getNamedItem("minute") != null ? Integer.parseInt(rt.getAttributes().getNamedItem("minute").getNodeValue()) : -1;
											resetData.add(new ReflectionReenterTimeHolder(day, hour, minute));
										}
									}
								}
							}
							else if ("skills".equalsIgnoreCase(cd.getNodeName()))
							{
								checkAllSkills = ref.getNamedItem("checkAllSkills") != null ? Boolean.parseBoolean(ref.getNamedItem("checkAllSkills").getNodeValue()) : false;
								for (var sp = cd.getFirstChild(); sp != null; sp = sp.getNextSibling())
								{
									if ("skill".equalsIgnoreCase(sp.getNodeName()))
									{
										skills.add(new SkillHolder(Integer.parseInt(sp.getAttributes().getNamedItem("id").getNodeValue()), Integer.parseInt(sp.getAttributes().getNamedItem("level").getNodeValue())));
									}
								}
							}
							else if ("add_parameters".equalsIgnoreCase(cd.getNodeName()))
							{
								for (var sp = cd.getFirstChild(); sp != null; sp = sp.getNextSibling())
								{
									if ("set".equalsIgnoreCase(sp.getNodeName()))
									{
										params.set(sp.getAttributes().getNamedItem("name").getNodeValue(), sp.getAttributes().getNamedItem("value").getNodeValue());
									}
								}
							}
							else if ("spawns".equalsIgnoreCase(cd.getNodeName()))
							{
								for (var sp = cd.getFirstChild(); sp != null; sp = sp.getNextSibling())
								{
									if ("group".equalsIgnoreCase(sp.getNodeName()))
									{
										final var group = sp.getAttributes().getNamedItem("name").getNodeValue();
										final boolean spawned = sp.getAttributes().getNamedItem("spawned") != null && Boolean.parseBoolean(sp.getAttributes().getNamedItem("spawned").getNodeValue());
										final var templates = SpawnParser.getInstance().getSpawn(group);
										if (templates != null)
										{
											if (spawns2.isEmpty())
											{
												spawns2 = new Hashtable<>();
											}
											spawns2.put(group, new ReflectionTemplate.SpawnInfo2(templates, spawned));
										}
									}
									else if ("spawn".equalsIgnoreCase(sp.getNodeName()))
									{
										final var mobs = sp.getAttributes().getNamedItem("mobId").getNodeValue().split(" ");
										
										respawn = sp.getAttributes().getNamedItem("respawn") != null ? Integer.parseInt(sp.getAttributes().getNamedItem("respawn").getNodeValue()) : 0;
										respawnRnd = sp.getAttributes().getNamedItem("respawnRnd") != null ? Integer.parseInt(sp.getAttributes().getNamedItem("respawnRnd").getNodeValue()) : 0;
										count = sp.getAttributes().getNamedItem("count") != null ? Integer.parseInt(sp.getAttributes().getNamedItem("count").getNodeValue()) : 1;
										
										final List<Location> coords = new ArrayList<>();
										spawnType = 0;
										
										final var spawnTypeNode = sp.getAttributes().getNamedItem("type").getNodeValue();
										if (spawnTypeNode == null || spawnTypeNode.equalsIgnoreCase("point"))
										{
											spawnType = 0;
										}
										else if (spawnTypeNode.equalsIgnoreCase("rnd"))
										{
											spawnType = 1;
										}
										else if (spawnTypeNode.equalsIgnoreCase("loc"))
										{
											spawnType = 2;
										}
										
										for (var cs = sp.getFirstChild(); cs != null; cs = cs.getNextSibling())
										{
											if ("coords".equalsIgnoreCase(cs.getNodeName()))
											{
												coords.add(Location.parseLoc(cs.getAttributes().getNamedItem("loc").getNodeValue()));
											}
										}
										
										SpawnTerritory territory = null;
										if (spawnType == 2)
										{
											final var poly = new Polygon();
											for (final Location loc : coords)
											{
												poly.add(loc.getX(), loc.getY()).setZmin(loc.getZ()).setZmax(loc.getHeading());
											}
											
											if (!poly.validate())
											{
												warn("Invalid spawn territory for instance id : " + id + " - " + poly + "!");
											}
											
											territory = new SpawnTerritory().add(poly);
										}
										
										for (final var mob : mobs)
										{
											mobId = Integer.parseInt(mob);
											spawnDat = new ReflectionTemplate.SpawnInfo(spawnType, mobId, count, respawn, respawnRnd, coords, territory);
											spawns.add(spawnDat);
										}
									}
								}
							}
						}
						addReflection(new ReflectionTemplate(id, name, timelimit, dispelBuffs, minLevel, maxLevel, minParty, maxParty, teleportLocs, ret, collapseIfEmpty, maxChannels, requestItems, rewardItems, doors, spawns2, spawns, setReuseUponEntry, sharedReuseGroup, resetData, requiredQuest, questType, skills, checkAllSkills, params));
					}
				}
			}
		}
	}

	public void addReflection(ReflectionTemplate zone)
	{
		_reflections.put(zone.getId(), zone);
	}
	
	public ReflectionTemplate getReflectionId(int id)
	{
		return _reflections.get(id);
	}
	
	public long getMinutesToNextEntrance(int id, Player player)
	{
		final var zone = getReflectionId(id);
		if (zone == null)
		{
			return 0;
		}
		
		Long time = null;
		if (getSharedReuseInstanceIds(id) != null && !getSharedReuseInstanceIds(id).isEmpty())
		{
			final List<Long> reuses = new ArrayList<>();
			for (final int i : getSharedReuseInstanceIds(id))
			{
				final long reuse = ReflectionManager.getInstance().getReflectionTime(player, i);
				if (reuse > 0)
				{
					reuses.add(reuse);
				}
			}
			
			if (!reuses.isEmpty())
			{
				Collections.sort(reuses);
				time = reuses.getLast();
			}
		}
		else
		{
			time = ReflectionManager.getInstance().getReflectionTime(player, id);
		}
		
		if (time == null)
		{
			return 0;
		}
		return time;
	}
	
	public List<Integer> getSharedReuseInstanceIds(int id)
	{
		if (getReflectionId(id).getSharedReuseGroup() < 1)
		{
			return null;
		}
		
		final List<Integer> sharedInstanceIds = new ArrayList<>();
		for (final ReflectionTemplate iz : _reflections.valueCollection())
		{
			if (iz.getSharedReuseGroup() > 0 && getReflectionId(id).getSharedReuseGroup() > 0 && iz.getSharedReuseGroup() == getReflectionId(id).getSharedReuseGroup())
			{
				sharedInstanceIds.add(iz.getId());
			}
		}
		return sharedInstanceIds;
	}
	
	public List<Integer> getSharedReuseInstanceIdsByGroup(int groupId)
	{
		if (groupId < 1)
		{
			return null;
		}
		
		final List<Integer> sharedInstanceIds = new ArrayList<>();
		for (final ReflectionTemplate iz : _reflections.valueCollection())
		{
			if (iz.getSharedReuseGroup() > 0 && iz.getSharedReuseGroup() == groupId)
			{
				sharedInstanceIds.add(iz.getId());
			}
		}
		return sharedInstanceIds;
	}

	public static ReflectionParser getInstance()
	{
		return SingletonHolder._instance;
	}

	private static class SingletonHolder
	{
		protected static final ReflectionParser _instance = new ReflectionParser();
	}
}