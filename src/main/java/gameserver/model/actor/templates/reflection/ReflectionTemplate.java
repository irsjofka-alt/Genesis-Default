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
package gameserver.model.actor.templates.reflection;

import java.util.List;
import java.util.Map;

import l2e.commons.util.Rnd;
import gameserver.Config;
import gameserver.model.Location;
import gameserver.model.holders.ReflectionReenterTimeHolder;
import gameserver.model.holders.SkillHolder;
import gameserver.model.spawn.SpawnTerritory;
import gameserver.model.spawn.Spawner;
import gameserver.model.stats.StatsSet;

public class ReflectionTemplate
{
	private final int _id;
	private final String _name;
	private final int _timelimit;
	private final boolean _dispelBuffs;
	private final int _minLevel;
	private final int _maxLevel;
	private final int _minParty;
	private final int _maxParty;
	private final List<Location> _teleportCoords;
	private final Location _returnCoords;
	private final int _collapseIfEmpty;
	private final int _maxChannels;
	private final List<ReflectionItemTemplate> _requestItems;
	private final List<ReflectionItemTemplate> _rewardItems;
	private final List<SkillHolder> _skills;
	private final boolean _checkAllSkills;

	private ReflectionEntryType _entryType = null;
	
	private final Map<Integer, StatsSet> _doors;
	private final Map<String, SpawnInfo2> _spawns;
	private final List<SpawnInfo> _spawnsInfo;
	
	private final boolean _reuseUponEntry;
	private final int _sharedReuseGroup;
	final List<ReflectionReenterTimeHolder> _resetData;
	
	private final String _requiredQuest;
	private final ReflectionQuestType _questType;
	
	private final StatsSet _params;
	
	public enum ReflectionQuestType
	{
		STARTED, COMPLETED
	}
	
	public enum ReflectionRemoveType
	{
		NONE, LEADER, ALL
	}
	
	public enum ReflectionEntryType
	{
		SOLO, SOLO_PARTY, PARTY, EVENT, PARTY_COMMAND_CHANNEL, COMMAND_CHANNEL
	}
	
	public static class SpawnInfo2
	{
		private final List<Spawner> _template;
		private final boolean _spawned;
		
		public SpawnInfo2(List<Spawner> template, boolean spawned)
		{
			_template = template;
			_spawned = spawned;
		}
		
		public List<Spawner> getTemplates()
		{
			return _template;
		}
		
		public boolean isSpawned()
		{
			return _spawned;
		}
	}
	
	public static class SpawnInfo
	{
		private final int _spawnType;
		private final int _npcId;
		private final int _count;
		private final int _respawn;
		private final int _respawnRnd;
		private final List<Location> _coords;
		private final SpawnTerritory _territory;
		
		public SpawnInfo(int spawnType, int npcId, int count, int respawn, int respawnRnd, SpawnTerritory territory)
		{
			this(spawnType, npcId, count, respawn, respawnRnd, null, territory);
		}
		
		public SpawnInfo(int spawnType, int npcId, int count, int respawn, int respawnRnd, List<Location> coords)
		{
			this(spawnType, npcId, count, respawn, respawnRnd, coords, null);
		}
		
		public SpawnInfo(int spawnType, int npcId, int count, int respawn, int respawnRnd, List<Location> coords, SpawnTerritory territory)
		{
			_spawnType = spawnType;
			_npcId = npcId;
			_count = count;
			_respawn = respawn;
			_respawnRnd = respawnRnd;
			_coords = coords;
			_territory = territory;
		}
		
		public int getSpawnType()
		{
			return _spawnType;
		}
		
		public int getId()
		{
			return _npcId;
		}
		
		public int getCount()
		{
			return _count;
		}
		
		public int getRespawnDelay()
		{
			return _respawn;
		}
		
		public int getRespawnRnd()
		{
			return _respawnRnd;
		}
		
		public List<Location> getCoords()
		{
			return _coords;
		}
		
		public SpawnTerritory getLoc()
		{
			return _territory;
		}
	}

	public ReflectionTemplate(int id, String name, int timelimit, boolean dispelBuffs, int minLevel, int maxLevel, int minParty, int maxParty, List<Location> tele, Location ret, int collapseIfEmpty, int maxChannels, List<ReflectionItemTemplate> requestItems, List<ReflectionItemTemplate> rewardItems, Map<Integer, StatsSet> doors, Map<String, SpawnInfo2> spawns, List<SpawnInfo> spawnsInfo, boolean reuseUponEntry, int sharedReuseGroup, List<ReflectionReenterTimeHolder> resetData, String requiredQuest, ReflectionQuestType questType, List<SkillHolder> skills, boolean checkAllSkills, StatsSet params)
	{
		_id = id;
		_name = name;
		_timelimit = timelimit;
		_dispelBuffs = dispelBuffs;
		_minLevel = minLevel;
		_maxLevel = maxLevel;
		_teleportCoords = tele;
		_returnCoords = ret;
		_minParty = minParty;
		_maxParty = maxParty;
		_collapseIfEmpty = collapseIfEmpty;
		_maxChannels = maxChannels;
		_requestItems = requestItems;
		_rewardItems = rewardItems;
		
		_doors = doors;
		_spawnsInfo = spawnsInfo;
		_spawns = spawns;
		
		_reuseUponEntry = reuseUponEntry;
		_sharedReuseGroup = sharedReuseGroup;
		_resetData = resetData;
		
		_requiredQuest = requiredQuest;
		_questType = questType;
		_skills = skills;
		_checkAllSkills = checkAllSkills;
		_params = params;
		
		if (getMinParty() == 1 && getMaxParty() == 1)
		{
			_entryType = ReflectionEntryType.SOLO;
		}
		else if (getMinParty() == 1 && getMaxParty() <= Config.PARTY_LIMIT)
		{
			_entryType = ReflectionEntryType.SOLO_PARTY;
		}
		else if (getMinParty() > 1 && getMaxParty() <= Config.PARTY_LIMIT)
		{
			_entryType = ReflectionEntryType.PARTY;
		}
		else if (getMinParty() <= Config.PARTY_LIMIT && getMaxParty() > Config.PARTY_LIMIT)
		{
			_entryType = ReflectionEntryType.PARTY_COMMAND_CHANNEL;
		}
		else if (getMinParty() >= Config.PARTY_LIMIT && getMaxParty() > Config.PARTY_LIMIT)
		{
			_entryType = ReflectionEntryType.COMMAND_CHANNEL;
		}
		else if(getMaxParty() == 0)
		{
			_entryType = ReflectionEntryType.EVENT;
		}
		
		if (_entryType == null)
		{
			throw new IllegalArgumentException("Invalid type for reflection: " + _name);
		}
	}
	
	public int getId()
	{
		return _id;
	}

	public String getName()
	{
		return _name;
	}

	public boolean isDispelBuffs()
	{
		return _dispelBuffs;
	}

	public int getTimelimit()
	{
		return _timelimit;
	}
	
	public int getMinLevel()
	{
		return _minLevel;
	}

	public int getMaxLevel()
	{
		return _maxLevel;
	}

	public int getMinParty()
	{
		return _minParty;
	}

	public int getMaxParty()
	{
		return _maxParty;
	}

	public Location getTeleportCoord()
	{
		if(_teleportCoords == null || _teleportCoords.isEmpty())
		{
			return null;
		}
		if(_teleportCoords.size() == 1)
		{
			return _teleportCoords.getFirst();
		}
		return _teleportCoords.get(Rnd.get(_teleportCoords.size()));
	}
	
	public void setNewTeleportCoords(Location loc)
	{
		_teleportCoords.clear();
		_teleportCoords.add(loc);
	}

	public Location getReturnCoords()
	{
		return _returnCoords;
	}

	public int getCollapseIfEmpty()
	{
		return _collapseIfEmpty;
	}

	public List<ReflectionItemTemplate> getRequestItems()
	{
		return _requestItems;
	}
	
	public List<ReflectionItemTemplate> getRewardItems()
	{
		return _rewardItems;
	}

	public int getMaxChannels()
	{
		return _maxChannels;
	}

	public ReflectionEntryType getEntryType()
	{
		return _entryType;
	}

	public List<Location> getTeleportCoords()
	{
		return _teleportCoords;
	}
	
	public Map<Integer, StatsSet> getDoorList()
	{
		return _doors;
	}
	
	public List<SpawnInfo> getSpawnsInfo()
	{
		return _spawnsInfo;
	}
	
	public Map<String, SpawnInfo2> getSpawns()
	{
		return _spawns;
	}
	
	public boolean getReuseUponEntry()
	{
		return _reuseUponEntry;
	}
	
	public int getSharedReuseGroup()
	{
		return _sharedReuseGroup;
	}
	
	public List<ReflectionReenterTimeHolder> getReenterData()
	{
		return _resetData;
	}
	
	public String getRequiredQuest()
	{
		return _requiredQuest;
	}
	
	public ReflectionQuestType getQuestType()
	{
		return _questType;
	}
	
	public List<SkillHolder> getSkills()
	{
		return _skills;
	}
	
	public boolean checkAllSkills()
	{
		return _checkAllSkills;
	}
	
	public StatsSet getParams()
	{
		return _params;
	}
	
	public int getRebirth()
	{
		return _params.getInteger("minRebirth", 0);
	}
	
	public int getHwidsLimit()
	{
		return _params.getInteger("hwidsLimit", 0);
	}
	
	public int getIpsLimit()
	{
		return _params.getInteger("ipsLimit", 0);
	}
	
	public boolean isHwidCheck()
	{
		return _params.getBool("isHwidCheck", false);
	}
	
	public boolean isSummonAllowed()
	{
		return _params.getBool("isSummonAllowed", false);
	}
	
	public boolean isPvPInstance()
	{
		return _params.getBool("isPvPInstance", false);
	}
	
	public boolean isForPremium()
	{
		return _params.getBool("isForPremium", false);
	}
	
	public boolean isForNoble()
	{
		return _params.getBool("isForNoble", false);
	}
	
	public boolean isForHero()
	{
		return _params.getBool("isForHero", false);
	}
	
	public boolean isHasCastle()
	{
		return _params.getBool("isHasCastle", false);
	}
	
	public boolean isHasFort()
	{
		return _params.getBool("isHasFort", false);
	}
	
	public boolean isShowTimer()
	{
		return _params.getBool("showTimer", false);
	}
	
	public boolean isTimerIncrease()
	{
		return _params.getBool("isTimerIncrease", false);
	}
	
	public String getTimerText()
	{
		return _params.getString("timerText", "");
	}
}