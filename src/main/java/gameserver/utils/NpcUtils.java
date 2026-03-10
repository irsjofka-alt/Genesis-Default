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
package gameserver.utils;

import l2e.commons.util.Rnd;
import gameserver.data.parser.NpcsParser;
import gameserver.instancemanager.ReflectionManager;
import gameserver.model.Location;
import gameserver.model.actor.Npc;
import gameserver.model.entity.Reflection;

public class NpcUtils
{
	public static Npc spawnSingle(int npcId, int x, int y, int z)
	{
		return spawnSingle(npcId, new Location(x, y, z, -1));
	}
	
	public static Npc spawnSingleNpc(int npcId, int x, int y, int z)
	{
		return spawnSingleNpc(npcId, new Location(x, y, z, -1));
	}
	
	public static Npc spawnSingle(int npcId, int x, int y, int z, int h)
	{
		return spawnSingle(npcId, new Location(x, y, z, h));
	}
	
	public static Npc spawnSingleNpc(int npcId, int x, int y, int z, int h)
	{
		return spawnSingleNpc(npcId, new Location(x, y, z, h));
	}
	
	public static Npc spawnSingle(int npcId, Location loc)
	{
		return spawnSingle(npcId, loc, ReflectionManager.DEFAULT, 0, false);
	}
	
	public static Npc spawnSingle(int npcId, Location loc, boolean animation)
	{
		return spawnSingle(npcId, loc, ReflectionManager.DEFAULT, 0, animation);
	}
	
	public static Npc spawnSingleNpc(int npcId, Location loc)
	{
		return spawnSingleNpc(npcId, loc, ReflectionManager.DEFAULT, 0);
	}
	
	public static Npc spawnSingle(int npcId, Location loc, long despawnTime)
	{
		return spawnSingle(npcId, loc, ReflectionManager.DEFAULT, despawnTime, false);
	}
	
	public static Npc spawnSingleNpc(int npcId, Location loc, long despawnTime)
	{
		return spawnSingleNpc(npcId, loc, ReflectionManager.DEFAULT, despawnTime);
	}
	
	public static Npc spawnSingle(int npcId, Location loc, Reflection r, long despawnTime)
	{
		return spawnSingle(npcId, loc, ReflectionManager.DEFAULT, despawnTime, false);
	}
	
	public static Npc spawnSingle(int npcId, Location loc, Reflection r, long despawnTime, boolean animation)
	{
		final var template = NpcsParser.getInstance().getTemplate(npcId);
		if (template == null)
		{
			throw new NullPointerException("Npc template id : " + npcId + " not found!");
		}
		
		if (loc == null)
		{
			return null;
		}
		
		final var npc = template.getNewInstance();
		npc.setShowSummonAnimation(animation);
		npc.setHeading(loc.getHeading() < 0 ? Rnd.get(0xFFFF) : loc.getHeading());
		npc.setLocation(loc);
		npc.setReflection(r);
		npc.setSpawnedLoc(loc);
		npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp());
		npc.spawnMe(loc.getX(), loc.getY(), loc.getZ());
		if (despawnTime > 0)
		{
			npc.scheduleDespawn(despawnTime);
		}
		return npc;
	}
	
	public static Npc spawnSingleNpc(int npcId, Location loc, Reflection r, long despawnTime)
	{
		final var template = NpcsParser.getInstance().getTemplate(npcId);
		if (template == null)
		{
			throw new NullPointerException("Npc template id : " + npcId + " not found!");
		}
		
		if (loc == null)
		{
			return null;
		}
		
		final var npc = template.getNewInstance();
		npc.setHeading(loc.getHeading() < 0 ? Rnd.get(0xFFFF) : loc.getHeading());
		npc.setLocation(loc);
		npc.setReflection(r);
		npc.setSpawnedLoc(loc);
		npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp());
		npc.spawnMe(loc);
		if (despawnTime > 0)
		{
			npc.scheduleDespawn(despawnTime);
		}
		return npc;
	}
}
