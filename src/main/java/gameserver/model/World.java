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
package gameserver.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import l2e.commons.collections.LazyArrayList;
import l2e.commons.log.LoggerObject;
import gameserver.Config;
import gameserver.data.parser.AdminParser;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Playable;
import gameserver.model.actor.Player;
import gameserver.model.actor.instance.TreasureChestInstance;

public final class World extends LoggerObject
{
	private static final Logger _log = LogManager.getLogger(World.class);
	
	public static final int GRACIA_MAX_X = -166168;
	public static final int GRACIA_MAX_Z = 6105;
	public static final int GRACIA_MIN_Z = -895;
	public static final int TILE_SIZE = 32768;
	
	public static final int TILE_X_MIN = Config.GEO_X_FIRST;
	public static final int TILE_Y_MIN = Config.GEO_Y_FIRST;
	public static final int TILE_X_MAX = Config.GEO_X_LAST;
	public static final int TILE_Y_MAX = Config.GEO_Y_LAST;
	public static final int TILE_ZERO_COORD_X = 20;
	public static final int TILE_ZERO_COORD_Y = 18;
	
	public static final int WORLD_SIZE_X = Config.GEO_X_LAST - Config.GEO_X_FIRST + 1;
	public static final int WORLD_SIZE_Y = Config.GEO_Y_LAST - Config.GEO_Y_FIRST + 1;
	
	public static final int MAP_MIN_X = Config.GEO_X_FIRST - 20 << 15;
	public static final int MAP_MAX_X = (Config.GEO_X_LAST - 20 + 1 << 15) - 1;
	public static final int MAP_MIN_Y = Config.GEO_Y_FIRST - 18 << 15;
	public static final int MAP_MAX_Y = (Config.GEO_Y_LAST - 18 + 1 << 15) - 1;
	public static final int MAP_MIN_Z = Config.MAP_MIN_Z;
	public static final int MAP_MAX_Z = Config.MAP_MAX_Z;
	
	public static final int SHIFT_BY = Config.SHIFT_BY;
	public static final int SHIFT_BY_Z = Config.SHIFT_BY_Z;
	
	public static final int OFFSET_X = Math.abs(MAP_MIN_X >> SHIFT_BY);
	public static final int OFFSET_Y = Math.abs(MAP_MIN_Y >> SHIFT_BY);
	public static final int OFFSET_Z = Math.abs(MAP_MIN_Z >> SHIFT_BY_Z);
	
	private static final int REGIONS_X = (MAP_MAX_X >> SHIFT_BY) + OFFSET_X;
	private static final int REGIONS_Y = (MAP_MAX_Y >> SHIFT_BY) + OFFSET_Y;
	private static final int REGIONS_Z = (MAP_MAX_Z >> SHIFT_BY_Z) + OFFSET_Z;
	
	private static volatile WorldRegion[][][] _worldRegions = new WorldRegion[REGIONS_X + 1][REGIONS_Y + 1][REGIONS_Z + 1];
	
	public static void init()
	{
		_log.info("World: Creating regions: [" + (REGIONS_X + 1) + "][" + (REGIONS_Y + 1) + "][" + (REGIONS_Z + 1) + "].");
	}
	
	private static WorldRegion[][][] getRegions()
	{
		return _worldRegions;
	}
	
	private static int validX(int x)
	{
		return (x > REGIONS_X) ? REGIONS_X : ((x < 0) ? 0 : x);
	}
	
	private static int validY(int y)
	{
		return (y > REGIONS_Y) ? REGIONS_Y : ((y < 0) ? 0 : y);
	}
	
	private static int validZ(int z)
	{
		return (z > REGIONS_Z) ? REGIONS_Z : ((z < 0) ? 0 : z);
	}
	
	private static int regionX(int x)
	{
		return (x >> SHIFT_BY) + OFFSET_X;
	}
	
	private static int regionY(int y)
	{
		return (y >> SHIFT_BY) + OFFSET_Y;
	}
	
	private static int regionZ(int z)
	{
		return (z >> SHIFT_BY_Z) + OFFSET_Z;
	}
	
	static boolean isNeighbour(int x1, int y1, int z1, int x2, int y2, int z2)
	{
		return x1 <= x2 + 1 && x1 >= x2 - 1 && y1 <= y2 + 1 && y1 >= y2 - 1 && z1 <= z2 + 1 && z1 >= z2 - 1;
	}
	
	public static List<Player> getAllGMs()
	{
		return AdminParser.getInstance().getAllGms(true);
	}
	
	public static List<Player> getAroundPlayers(GameObject object)
	{
		final var currentRegion = object.getWorldRegion();
		if (currentRegion == null)
		{
			return Collections.emptyList();
		}
		
		final int oid = object.getObjectId();
		final int rid = object.getReflectionId();
		
		final List<Player> result = new LazyArrayList<>(64);
		for(int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); x++)
		{
			for(int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); y++)
			{
				for(int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); z++)
				{
					for (final var obj : getRegion(x, y, z))
					{
        				if ((obj == null) || !obj.isPlayer() || obj.getObjectId() == oid || (obj.getReflectionId() != rid))
        				{
        					continue;
        				}
        				result.add((Player) obj);
					}
				}
			}
		}
		return result;
	}
	
	public static List<Player> getAroundTraders(GameObject object)
	{
		final var currentRegion = object.getWorldRegion();
		if (currentRegion == null)
		{
			return Collections.emptyList();
		}
		
		final int oid = object.getObjectId();
		final int rid = object.getReflectionId();
		
		final List<Player> result = new LazyArrayList<>(64);
		
		for(int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); x++)
		{
			for(int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); y++)
			{
				for(int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); z++)
				{
					for (final var obj : getRegion(x, y, z))
					{
        				if ((obj == null) || !obj.isPlayer() || obj.getObjectId() == oid || (obj.getReflectionId() != rid))
        				{
        					continue;
        				}
        				
        				if (obj.getActingPlayer().getPrivateStoreType() == Player.STORE_PRIVATE_NONE)
        				{
        					continue;
        				}
        				result.add((Player) obj);
					}
				}
			}
		}
		return result;
	}
	
	public static List<Player> getAroundPlayers(GameObject object, int radius, int height)
	{
		final var currentRegion = object.getWorldRegion();
		if (currentRegion == null)
		{
			return Collections.emptyList();
		}
		
		final int oid = object.getObjectId();
		final int rid = object.getReflectionId();
		final int ox = object.getX();
		final int oy = object.getY();
		final int oz = object.getZ();
		final int sqrad = radius * radius;
		
		final List<Player> result = new LazyArrayList<>(64);
		
		for (int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); x++)
		{
			for (int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); y++)
			{
				for (int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); z++)
				{
					for (final var obj : getRegion(x, y, z))
					{
						if ((obj == null) || !obj.isPlayer() || obj.getObjectId() == oid || obj.getReflectionId() != rid)
						{
							continue;
						}
						if (Math.abs(obj.getZ() - oz) > height)
						{
							continue;
						}
						final int dx = Math.abs(obj.getX() - ox);
						if (dx > radius)
						{
							continue;
						}
						final int dy = Math.abs(obj.getY() - oy);
						if (dy > radius)
						{
							continue;
						}
						if (dx * dx + dy * dy > sqrad)
						{
							continue;
						}
						result.add((Player) obj);
					}
				}
			}
		}
		return result;
	}
	
	public static List<GameObject> getAroundObjects(GameObject object)
	{
		final var currentRegion = object.getWorldRegion();
		if (currentRegion == null)
		{
			return Collections.emptyList();
		}
		
		final int oid = object.getObjectId();
		final int rid = object.getReflectionId();
		
		final List<GameObject> result = new LazyArrayList<>(128);
		
		for (int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); x++)
		{
			for (int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); y++)
			{
				for (int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); z++)
				{
					for (final var obj : getRegion(x, y, z))
					{
						if ((obj == null) || obj.getObjectId() == oid || obj.getReflectionId() != rid)
						{
							continue;
						}
						result.add(obj);
					}
				}
			}
		}
		return result;
	}
	
	public static GameObject getAroundObjectById(GameObject object, int objId)
	{
		final var currentRegion = object.getWorldRegion();
		if (currentRegion == null)
		{
			return null;
		}
		
		for (int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); x++)
		{
			for (int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); y++)
			{
				for (int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); z++)
				{
					for (final var obj : getRegion(x, y, z))
					{
						if (obj != null && obj.getObjectId() == objId && obj.getReflectionId() == object.getReflectionId())
						{
							return obj;
						}
					}
				}
			}
		}
		return null;
	}
	
	public static List<GameObject> getAroundObjects(GameObject object, int radius, int height)
	{
		final var currentRegion = object.getWorldRegion();
		if (currentRegion == null)
		{
			return Collections.emptyList();
		}
		
		final int oid = object.getObjectId();
		final int rid = object.getReflectionId();
		final int ox = object.getX();
		final int oy = object.getY();
		final int oz = object.getZ();
		final int sqrad = radius * radius;
		
		final List<GameObject> result = new LazyArrayList<>(128);
		for (int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); x++)
		{
			for (int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); y++)
			{
				for (int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); z++)
				{
					for (final var obj : getRegion(x, y, z))
					{
						if ((obj == null) || obj.getObjectId() == oid || (obj.getReflectionId() != rid))
						{
							continue;
						}
						
						if (Math.abs(obj.getZ() - oz) > height)
						{
							continue;
						}
						final int dx = Math.abs(obj.getX() - ox);
						if (dx > radius)
						{
							continue;
						}
						final int dy = Math.abs(obj.getY() - oy);
						if (dy > radius)
						{
							continue;
						}
						if (dx * dx + dy * dy > sqrad)
						{
							continue;
						}
						result.add(obj);
					}
				}
			}
		}
		return result;
	}
	
	public static List<Playable> getAroundPlayables(GameObject object)
	{
		final var currentRegion = object.getWorldRegion();
		if (currentRegion == null)
		{
			return Collections.emptyList();
		}
		
		final int oid = object.getObjectId();
		final int rid = object.getReflectionId();
		
		final List<Playable> result = new LazyArrayList<>(64);
		for (int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); x++)
		{
			for (int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); y++)
			{
				for (int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); z++)
				{
					for (final var obj : getRegion(x, y, z))
					{
						if ((obj == null) || obj.getObjectId() == oid || obj.getReflectionId() != rid || !obj.isPlayable())
						{
							continue;
						}
						result.add((Playable) obj);
					}
				}
			}
		}
		return result;
	}
	
	public static List<Playable> getAroundPlayables(GameObject object, int radius, int height)
	{
		final var currentRegion = object.getWorldRegion();
		if (currentRegion == null)
		{
			return Collections.emptyList();
		}
		
		final int oid = object.getObjectId();
		final int rid = object.getReflectionId();
		
		final int ox = object.getX();
		final int oy = object.getY();
		final int oz = object.getZ();
		final int sqrad = radius * radius;
		
		final List<Playable> result = new LazyArrayList<>(64);
		for (int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); x++)
		{
			for (int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); y++)
			{
				for (int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); z++)
				{
					for (final var obj : getRegion(x, y, z))
					{
						if ((obj == null) || !obj.isPlayable() || obj.getObjectId() == oid || obj.getReflectionId() != rid)
						{
							continue;
						}
						if (Math.abs(obj.getZ() - oz) > height)
						{
							continue;
						}
						final int dx = Math.abs(obj.getX() - ox);
						if (dx > radius)
						{
							continue;
						}
						final int dy = Math.abs(obj.getY() - oy);
						if (dy > radius)
						{
							continue;
						}
						if (dx * dx + dy * dy > sqrad)
						{
							continue;
						}
						result.add((Playable) obj);
					}
				}
			}
		}
		return result;
	}
	
	public static List<Creature> getAroundCharacters(GameObject object)
	{
		final var currentRegion = object.getWorldRegion();
		if (currentRegion == null)
		{
			return Collections.emptyList();
		}
		
		final int oid = object.getObjectId();
		final int rid = object.getReflectionId();
		
		final List<Creature> result = new LazyArrayList<>(64);
		for (int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); x++)
		{
			for (int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); y++)
			{
				for (int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); z++)
				{
					for (final var obj : getRegion(x, y, z))
					{
						if ((obj == null) || !obj.isCreature() || obj.getObjectId() == oid || obj.getReflectionId() != rid)
						{
							continue;
						}
						result.add((Creature) obj);
					}
				}
			}
		}
		return result;
	}
	
	public static List<Creature> getAroundCharacters(GameObject object, int radius, int height)
	{
		final var currentRegion = object.getWorldRegion();
		if (currentRegion == null)
		{
			return Collections.emptyList();
		}
		
		final int oid = object.getObjectId();
		final int rid = object.getReflectionId();
		
		final int ox = object.getX();
		final int oy = object.getY();
		final int oz = object.getZ();
		final int sqrad = radius * radius;
		
		final List<Creature> result = new LazyArrayList<>(64);
		for (int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); x++)
		{
			for (int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); y++)
			{
				for (int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); z++)
				{
					for (final var obj : getRegion(x, y, z))
					{
						if ((obj == null) || !obj.isCreature() || obj.getObjectId() == oid || obj.getReflectionId() != rid)
						{
							continue;
						}
						
						if (Math.abs(obj.getZ() - oz) > height)
						{
							continue;
						}
						
						final int dx = Math.abs(obj.getX() - ox);
						if (dx > radius)
						{
							continue;
						}
						
						final int dy = Math.abs(obj.getY() - oy);
						if (dy > radius)
						{
							continue;
						}
						
						if (dx * dx + dy * dy > sqrad)
						{
							continue;
						}
						result.add((Creature) obj);
					}
				}
			}
		}
		return result;
	}
	
	public static List<Creature> getAroundCharacters(Location loc, int radius, int height)
	{
		return getAroundCharacters(loc, radius, height, 0);
	}
	
	public static List<Creature> getAroundCharacters(Location loc, int radius, int height, int reflectionId)
	{
		final var currentRegion = getRegion(loc);
		if (currentRegion == null)
		{
			return Collections.emptyList();
		}
		
		final int ox = loc.getX();
		final int oy = loc.getY();
		final int oz = loc.getZ();
		final int sqrad = radius * radius;
		
		final List<Creature> result = new LazyArrayList<>(64);
		
		for (int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); x++)
		{
			for (int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); y++)
			{
				for (int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); z++)
				{
					for (final var obj : getRegion(x, y, z))
					{
						if ((obj == null) || !obj.isCreature() || obj.getReflectionId() != reflectionId)
						{
							continue;
						}
						
						if (Math.abs(obj.getZ() - oz) > height)
						{
							continue;
						}
						
						final int dx = Math.abs(obj.getX() - ox);
						if (dx > radius)
						{
							continue;
						}
						
						final int dy = Math.abs(obj.getY() - oy);
						if (dy > radius)
						{
							continue;
						}
						
						if (dx * dx + dy * dy > sqrad)
						{
							continue;
						}
						result.add((Creature) obj);
					}
				}
			}
		}
		return result;
	}
	
	public static List<Npc> getAroundNpc(GameObject object)
	{
		final var currentRegion = object.getWorldRegion();
		if (currentRegion == null)
		{
			return Collections.emptyList();
		}
		
		final int oid = object.getObjectId();
		final int rid = object.getReflectionId();
		
		final List<Npc> result = new LazyArrayList<>(64);
		for (int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); x++)
		{
			for (int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); y++)
			{
				for (int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); z++)
				{
					for (final var obj : getRegion(x, y, z))
					{
						if ((obj == null) || !obj.isNpc() || obj.getObjectId() == oid || obj.getReflectionId() != rid)
						{
							continue;
						}
						
						result.add((Npc) obj);
					}
				}
			}
		}
		return result;
	}
	
	public static List<Npc> getAroundNpc(GameObject object, int radius, int height)
	{
		final var currentRegion = object.getWorldRegion();
		if (currentRegion == null)
		{
			return Collections.emptyList();
		}
		
		final int oid = object.getObjectId();
		final int rid = object.getReflectionId();
		final int ox = object.getX();
		final int oy = object.getY();
		final int oz = object.getZ();
		final int sqrad = radius * radius;
		
		final List<Npc> result = new LazyArrayList<>(64);
		for (int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); x++)
		{
			for (int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); y++)
			{
				for (int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); z++)
				{
					for (final var obj : getRegion(x, y, z))
					{
						if ((obj == null) || !obj.isNpc() || obj.getObjectId() == oid || obj.getReflectionId() != rid)
						{
							continue;
						}
						if (Math.abs(obj.getZ() - oz) > height)
						{
							continue;
						}
						final int dx = Math.abs(obj.getX() - ox);
						if (dx > radius)
						{
							continue;
						}
						final int dy = Math.abs(obj.getY() - oy);
						if (dy > radius)
						{
							continue;
						}
						if (dx * dx + dy * dy > sqrad)
						{
							continue;
						}
						result.add((Npc) obj);
					}
				}
			}
		}
		return result;
	}
	
	public static List<Attackable> getAroundAttackable(GameObject object, int radius, int height)
	{
		final var currentRegion = object.getWorldRegion();
		if (currentRegion == null)
		{
			return Collections.emptyList();
		}
		
		final int oid = object.getObjectId();
		final int rid = object.getReflectionId();
		final int ox = object.getX();
		final int oy = object.getY();
		final int oz = object.getZ();
		final int sqrad = radius * radius;
		
		final List<Attackable> result = new LazyArrayList<>(64);
		
		for (int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); x++)
		{
			for (int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); y++)
			{
				for (int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); z++)
				{
					for (final var obj : getRegion(x, y, z))
					{
						if ((obj == null) || !obj.isAttackable() || obj.isIgnoreSearch() || obj.getObjectId() == oid || obj.getReflectionId() != rid)
						{
							continue;
						}
						if (Math.abs(obj.getZ() - oz) > height)
						{
							continue;
						}
						final int dx = Math.abs(obj.getX() - ox);
						if (dx > radius)
						{
							continue;
						}
						final int dy = Math.abs(obj.getY() - oy);
						if (dy > radius)
						{
							continue;
						}
						if (dx * dx + dy * dy > sqrad)
						{
							continue;
						}
						result.add((Attackable) obj);
					}
				}
			}
		}
		return result;
	}
	
	public static List<Attackable> getAroundFarmNpc(Player object, int radius, int height)
	{
		final var currentRegion = object.getWorldRegion();
		if (currentRegion == null)
		{
			return Collections.emptyList();
		}
		
		final int oid = object.getObjectId();
		final int rid = object.getReflectionId();
		final int ox = object.getX();
		final int oy = object.getY();
		final int oz = object.getZ();
		final int sqrad = radius * radius;
		
		final Map<Integer, Attackable> result = new TreeMap<>();
		
		final var farmAttackChampion = object.getFarmSystem().isAttackChampion();
		final var farmAttackRaid = object.getFarmSystem().isAttackRaid();
		
		for (int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); x++)
		{
			for (int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); y++)
			{
				for (int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); z++)
				{
					for (final var obj : getRegion(x, y, z))
					{
						if ((obj == null) || !obj.isNpc() || obj.getObjectId() == oid || obj.getReflectionId() != rid)
						{
							continue;
						}
						if (Math.abs(obj.getZ() - oz) > height)
						{
							continue;
						}
						final int dx = Math.abs(obj.getX() - ox);
						if (dx > radius)
						{
							continue;
						}
						final int dy = Math.abs(obj.getY() - oy);
						if (dy > radius)
						{
							continue;
						}
						if (dx * dx + dy * dy > sqrad)
						{
							continue;
						}
						
						if (obj instanceof Attackable npc)
						{
							
							if (!npc.hasAI() || npc.isBlockAutoFarmTarget() || (!farmAttackRaid && (npc.isRaid() || npc.isRaidMinion())) || (!farmAttackChampion && npc.getChampionTemplate() != null) || !npc.isMonster() || npc.isDead() || npc.isInvul() || !npc.isVisible() || (npc instanceof TreasureChestInstance))
							{
								continue;
							}
							result.put((int) object.getDistance3D(npc), npc);
						}
					}
				}
			}
		}
		return result.values().stream().toList();
	}
	
	public static int getAroundTraders(Player object, int radius, int height)
	{
		final var currentRegion = object.getWorldRegion();
		if (currentRegion == null)
		{
			return 0;
		}
		
		final int oid = object.getObjectId();
		final int rid = object.getReflectionId();
		final int ox = object.getX();
		final int oy = object.getY();
		final int oz = object.getZ();
		final int sqrad = radius * radius;
		
		for (int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); x++)
		{
			for (int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); y++)
			{
				for (int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); z++)
				{
					for (final var obj : getRegion(x, y, z))
					{
						if ((obj == null) || (!obj.isNpc() && !obj.isPlayer()) || obj.getObjectId() == oid || obj.getReflectionId() != rid)
						{
							continue;
						}
						if (Math.abs(obj.getZ() - oz) > height)
						{
							continue;
						}
						final int dx = Math.abs(obj.getX() - ox);
						if (dx > radius)
						{
							continue;
						}
						final int dy = Math.abs(obj.getY() - oy);
						if (dy > radius)
						{
							continue;
						}
						if (dx * dx + dy * dy > sqrad)
						{
							continue;
						}
						
						if (obj.isNpc() || (obj.isPlayer() && obj.getActingPlayer().isInStoreMode()))
						{
							return 1;
						}
					}
				}
			}
		}
		return 0;
	}
	
	public static WorldRegion getRegion(Location loc)
	{
		return getRegion(validX(regionX(loc.getX())), validY(regionY(loc.getY())), validZ(regionZ(loc.getZ())));
	}
	
	public static WorldRegion getRegion(GameObject obj)
	{
		return getRegion(validX(regionX(obj.getX())), validY(regionY(obj.getY())), validZ(regionZ(obj.getZ())));
	}
	
	public static WorldRegion getRegion(int x, int y, int z)
	{
		final var regions = getRegions();
		WorldRegion region = null;
		region = regions[x][y][z];
		if (region == null)
		{
			synchronized (regions)
			{
				region = regions[x][y][z];
				if(region == null)
				{
					region = regions[x][y][z] = new WorldRegion(x, y, z);
				}
			}
		}
		return region;
	}
	
	public static Player getPlayer(String name)
	{
		return GameObjectsStorage.getPlayer(name);
	}
	
	public static Player getPlayer(int objId)
	{
		return GameObjectsStorage.getPlayer(objId);
	}
	
	public static void addVisibleObject(GameObject object)
	{
		if (object == null)
		{
			return;
		}
		
		final var region = getRegion(object);
		final var currentRegion = object.getWorldRegion();
		
		if (currentRegion == region)
		{
			return;
		}
		
		if (currentRegion == null)
		{
			object.setWorldRegion(region);
			region.addObject(object);
			
			for (int x = validX(region.getX() - 1); x <= validX(region.getX() + 1); x++)
			{
				for (int y = validY(region.getY() - 1); y <= validY(region.getY() + 1); y++)
				{
					for (int z = validZ(region.getZ() - 1); z <= validZ(region.getZ() + 1); z++)
					{
						getRegion(x, y, z).addToVisible(object);
					}
				}
			}
		}
		else
		{
			currentRegion.removeObject(object);
			object.setWorldRegion(region);
			region.addObject(object);
			
			for (int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); x++)
			{
				for (int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); y++)
				{
					for (int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); z++)
					{
						if (!isNeighbour(region.getX(), region.getY(), region.getZ(), x, y, z))
						{
							getRegion(x, y, z).removeFromVisible(object);
						}
					}
				}
			}
			
			for (int x = validX(region.getX() - 1); x <= validX(region.getX() + 1); x++)
			{
				for (int y = validY(region.getY() - 1); y <= validY(region.getY() + 1); y++)
				{
					for (int z = validZ(region.getZ() - 1); z <= validZ(region.getZ() + 1); z++)
					{
						if (!isNeighbour(currentRegion.getX(), currentRegion.getY(), currentRegion.getZ(), x, y, z))
						{
							getRegion(x, y, z).addToVisible(object);
						}
					}
				}
			}
		}
	}
	
	public static void removeVisibleObject(GameObject object)
	{
		if (object == null)
		{
			return;
		}
		
		WorldRegion currentRegion;
		if ((currentRegion = object.getWorldRegion()) == null)
		{
			return;
		}
		
		object.setWorldRegion(null);
		currentRegion.removeObject(object);
		
		for (int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); x++)
		{
			for (int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); y++)
			{
				for (int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); z++)
				{
					getRegion(x, y, z).removeFromVisible(object);
				}
			}
		}
	}
	
	public static void forgetObject(GameObject object)
	{
		if (object == null)
		{
			return;
		}
		
		WorldRegion currentRegion;
		if ((currentRegion = object.getWorldRegion()) == null)
		{
			return;
		}
		
		for (int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); x++)
		{
			for (int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); y++)
			{
				for (int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); z++)
				{
					getRegion(x, y, z).forgetObject(object);
				}
			}
		}
	}
	
	public static boolean isNeighborsEmpty(WorldRegion region)
	{
		for(int x = validX(region.getX() - 1); x <= validX(region.getX() + 1); x++)
		{
			for(int y = validY(region.getY() - 1); y <= validY(region.getY() + 1); y++)
			{
				for(int z = validZ(region.getZ() - 1); z <= validZ(region.getZ() + 1); z++)
				{
					if(!getRegion(x, y, z).isEmpty())
					{
						return false;
					}
				}
			}
		}
		return true;
	}

	public static void activate(WorldRegion currentRegion)
	{
		for(int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); x++)
		{
			for(int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); y++)
			{
				for(int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); z++)
				{
					getRegion(x, y, z).setActive(true);
				}
			}
		}
	}

	public static void deactivate(WorldRegion currentRegion)
	{
		for(int x = validX(currentRegion.getX() - 1); x <= validX(currentRegion.getX() + 1); x++)
		{
			for(int y = validY(currentRegion.getY() - 1); y <= validY(currentRegion.getY() + 1); y++)
			{
				for(int z = validZ(currentRegion.getZ() - 1); z <= validZ(currentRegion.getZ() + 1); z++)
				{
					if(isNeighborsEmpty(getRegion(x, y, z)))
					{
						getRegion(x, y, z).setActive(false);
					}
				}
			}
		}
	}
	
	public static void deleteVisibleNpcSpawns()
	{
		_log.info("Deleting all visible NPC's.");
		WorldRegion region;
		for(int x = 0; x <= REGIONS_X; x++)
		{
			for(int y = 0; y <= REGIONS_Y; y++)
			{
				for(int z = 0; z <= REGIONS_Z; z++)
				{
					region = _worldRegions[x][y][z];
					if(region != null)
					{
						region.deleteVisibleNpcSpawns();
					}
				}
			}
		}
		_log.info("All visible NPC's deleted.");
	}
}