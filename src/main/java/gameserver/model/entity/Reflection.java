package gameserver.model.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;

import l2e.commons.util.Rnd;
import gameserver.Announcements;
import gameserver.ThreadPoolManager;
import gameserver.data.parser.DoorParser;
import gameserver.data.parser.NpcsParser;
import gameserver.idfactory.IdFactory;
import gameserver.instancemanager.ReflectionManager;
import gameserver.instancemanager.ZoneManager;
import gameserver.model.GameObject;
import gameserver.model.GameObjectsStorage;
import gameserver.model.Location;
import gameserver.model.TeleportWhereType;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.actor.instance.DoorInstance;
import gameserver.model.actor.templates.reflection.ReflectionTemplate;
import gameserver.model.holders.ReflectionReenterTimeHolder;
import gameserver.model.spawn.SpawnTemplate;
import gameserver.model.spawn.Spawner;
import gameserver.model.stats.StatsSet;
import gameserver.model.zone.type.ReflectionZone;
import gameserver.network.SystemMessageId;
import gameserver.network.clientpackets.Say2;
import gameserver.network.serverpackets.CreatureSay;
import gameserver.network.serverpackets.GameServerPacket;
import gameserver.network.serverpackets.SystemMessage;

public class Reflection
{
	private static final Logger _log = LogManager.getLogger(Reflection.class);
	
	private final int _id;
	private int _templateId = -1;
	private String _name;
	
	private final Set<GameObject> _objects = ConcurrentHashMap.newKeySet(1);
	private final List<Integer> _players = new CopyOnWriteArrayList<>();
	private final Set<Integer> _allowed = ConcurrentHashMap.newKeySet();
	private final List<Spawner> _spawns = new CopyOnWriteArrayList<>();
	private final IntObjectMap<DoorInstance> _doors = new HashIntObjectMap<>();
	private Map<String, List<Spawner>> _spawners = Collections.emptyMap();
	private final int[] _spawnsLoc = new int[3];
	private boolean _allowSummon = true;
	private long _emptyDestroyTime = -1;
	private long _lastLeft = -1;
	private long _instanceStartTime = -1;
	private long _instanceEndTime = -1;
	private boolean _isPvPInstance = false;
	private boolean _showTimer = false;
	private boolean _isTimerIncrease = true;
	private String _timerText = "";
	private Location _returnCoords = null;
	private boolean _disableMessages = false;
	private boolean _isHwidCheck = false;
	private final List<Integer> _zones = new ArrayList<>();
	
	private boolean _reuseUponEntry;
	private List<ReflectionReenterTimeHolder> _resetData = new ArrayList<>();
	private final Map<String, Future<?>> _timers = new ConcurrentHashMap<>();
	private StatsSet _params = new StatsSet();
	
	private Future<?> _checkTimeUpTask = null;
	
	public Reflection(int id)
	{
		_id = id;
		_instanceStartTime = System.currentTimeMillis();
	}
	
	public Reflection(int id, String name)
	{
		_id = id;
		_name = name;
		_instanceStartTime = System.currentTimeMillis();
	}
	
	public int getId()
	{
		return _id;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public void setName(String name)
	{
		_name = name;
	}
	
	public boolean isSummonAllowed()
	{
		return _allowSummon;
	}
	
	public void setAllowSummon(boolean b)
	{
		_allowSummon = b;
	}
	
	public boolean isPvPInstance()
	{
		return _isPvPInstance;
	}
	
	public void setPvPInstance(boolean b)
	{
		_isPvPInstance = b;
	}
	
	public void setDuration(int duration)
	{
		if (_checkTimeUpTask != null)
		{
			_checkTimeUpTask.cancel(true);
		}
		_checkTimeUpTask = ThreadPoolManager.getInstance().schedule(new CheckTimeUp(duration), 500);
		_instanceEndTime = System.currentTimeMillis() + duration + 500;
	}
	
	public void setEmptyDestroyTime(long time)
	{
		_emptyDestroyTime = time;
	}
	
	public void addAllowed(Player player)
	{
		if (!_allowed.contains(player.getObjectId()))
		{
			_allowed.add(player.getObjectId());
		}
	}
	
	public void removeAllowed(Player player)
	{
		if (_allowed.contains(player.getObjectId()))
		{
			_allowed.remove(player.getObjectId());
		}
	}
	
	public boolean isAllowed(int objId)
	{
		return _allowed.contains(objId);
	}
	
	public List<Player> getAllowed()
	{
		final List<Player> allowed = new ArrayList<>(_allowed.size());
		for (final int playerId : _allowed)
		{
			final var player = GameObjectsStorage.getPlayer(playerId);
			if (player != null)
			{
				allowed.add(player);
			}
		}
		return allowed;
	}
	
	public boolean containsPlayer(int objectId)
	{
		return _players.contains(objectId);
	}
	
	public void addObject(GameObject o)
	{
		_objects.add(o);
		if (o.isPlayer())
		{
			_players.add(o.getObjectId());
		}
	}
	
	public void removeObject(GameObject o)
	{
		if (!_objects.remove(o))
		{
			return;
		}
		
		if (o.isPlayer())
		{
			if (_players.contains(o.getObjectId()))
			{
				_players.remove(_players.indexOf(Integer.valueOf(o.getObjectId())));
			}
			
			if (_players.isEmpty() && (_emptyDestroyTime >= 0))
			{
				_lastLeft = System.currentTimeMillis();
				setDuration((int) (_instanceEndTime - System.currentTimeMillis() - 500));
			}
		}
	}
	
	public void addDoor(int doorId, StatsSet set)
	{
		if (_doors.containsKey(doorId))
		{
			_log.warn("Door ID " + doorId + " already exists in instance " + getId());
			return;
		}
		
		final var temp = DoorParser.getInstance().getDoorTemplate(doorId);
		if (temp != null)
		{
			final var newdoor = new DoorInstance(IdFactory.getInstance().getNextId(), temp, set);
			newdoor.setReflection(this);
			newdoor.setCurrentHp(newdoor.getMaxHp());
			
			final int gz = temp.posZ + 32;
			newdoor.spawnMe(temp.posX, temp.posY, gz);
			_doors.put(doorId, newdoor);
		}
	}
	
	public void addEventDoor(int doorId, StatsSet set)
	{
		if (_doors.containsKey(doorId))
		{
			_log.warn("Door ID " + doorId + " already exists in instance " + getId());
			return;
		}
		
		final var temp = DoorParser.getInstance().getDoorTemplate(doorId);
		if (temp != null)
		{
			final var newdoor = new DoorInstance(IdFactory.getInstance().getNextId(), temp, set);
			newdoor.setReflection(this);
			newdoor.setCurrentHp(newdoor.getMaxHp());
			
			final int gz = temp.posZ + 32;
			newdoor.spawnMe(temp.posX, temp.posY, gz);
			newdoor.openMe();
			_doors.put(doorId, newdoor);
		}
	}
	
	public List<Integer> getPlayers()
	{
		return _players;
	}
	
	public List<Player> getReflectionPlayers()
	{
		final List<Player> result = new ArrayList<>();
		for (final var objectId : _players)
		{
			final var player = GameObjectsStorage.getPlayer(objectId);
			if ((player != null) && (player.getReflectionId() == getId()))
			{
				result.add(player);
			}
		}
		return result;
	}
	
	public List<Npc> getNpcs()
	{
		final List<Npc> result = new ArrayList<>();
		for (final var o : _objects)
		{
			if (o != null && o.isNpc())
			{
				result.add((Npc) o);
			}
		}
		return result;
	}
	
	public List<Npc> getAliveNpcs(int... id)
	{
		final List<Npc> result = new ArrayList<>();
		for (final var o : _objects)
		{
			if (o != null && o.isNpc())
			{
				final var npc = (Npc) o;
				if (!npc.isDead() && ArrayUtils.contains(id, npc.getId()))
				{
					result.add(npc);
				}
			}
		}
		return result;
	}
	
	public boolean hasAliveNpcs(int... id)
	{
		for (final var o : _objects)
		{
			if (o != null && o.isNpc())
			{
				final var npc = (Npc) o;
				if (!npc.isDead() && ArrayUtils.contains(id, npc.getId()))
				{
					return true;
				}
			}
		}
		return false;
	}
	
	@SafeVarargs
	@SuppressWarnings("unchecked")
	public final <T extends Creature> List<T> getAliveNpcs(Class<T> clazz, int... ids)
	{
		final List<T> result = new ArrayList<>();
		for (final var o : _objects)
		{
			if (o != null && o.isNpc())
			{
				final var npc = (Npc) o;
				if ((((ids.length == 0) || ArrayUtils.contains(ids, npc.getId())) && (!npc.isDead())) && clazz.isInstance(npc))
				{
					result.add((T) npc);
				}
			}
		}
		return result;
	}
	
	public Npc getNpc(int id)
	{
		for (final var o : _objects)
		{
			if (o != null && o.isNpc())
			{
				final var npc = (Npc) o;
				if (!npc.isDead() && npc.getId() == id)
				{
					return npc;
				}
			}
		}
		return null;
	}
	
	public List<DoorInstance> getDoors()
	{
		return _doors.valueCollection().stream().toList();
	}
	
	public DoorInstance getDoor(int id)
	{
		return _doors.get(id);
	}
	
	public void openDoor(final int doorId)
	{
		final var door = _doors.get(doorId);
		if (door != null)
		{
			door.openMe();
		}
	}
	
	public void closeDoor(final int doorId)
	{
		final var door = _doors.get(doorId);
		if (door != null)
		{
			door.closeMe();
		}
	}
	
	public long getInstanceEndTime()
	{
		return _instanceEndTime;
	}
	
	public long getInstanceStartTime()
	{
		return _instanceStartTime;
	}
	
	public boolean isShowTimer()
	{
		return _showTimer;
	}
	
	public boolean isTimerIncrease()
	{
		return _isTimerIncrease;
	}
	
	public String getTimerText()
	{
		return _timerText;
	}
	
	public Location getReturnLoc()
	{
		return _returnCoords;
	}
	
	public void setReturnLoc(Location loc)
	{
		_returnCoords = loc;
	}
	
	public int[] getSpawnsLoc()
	{
		return _spawnsLoc;
	}
	
	public void setSpawnsLoc(int[] loc)
	{
		if ((loc == null) || (loc.length < 3))
		{
			return;
		}
		System.arraycopy(loc, 0, _spawnsLoc, 0, 3);
	}
	
	public void cleanupNpcs()
	{
		_spawns.stream().filter(s -> s != null).forEach(s -> s.deleteAll());
		for (final String group : _spawners.keySet())
		{
			despawnByGroup(group);
		}
	}
	
	public void loadReflectionTemplate(ReflectionTemplate template)
	{
		if (template != null)
		{
			_name = template.getName();
			_templateId = template.getId();
			if (template.getTimelimit() != 0)
			{
				_checkTimeUpTask = ThreadPoolManager.getInstance().schedule(new CheckTimeUp(template.getTimelimit() * 60000), 15000);
				_instanceEndTime = System.currentTimeMillis() + (template.getTimelimit() * 60000) + 15000;
			}
			_allowSummon = template.isSummonAllowed();
			_emptyDestroyTime = template.getCollapseIfEmpty() * 60000;
			_showTimer = template.isShowTimer();
			_isTimerIncrease = template.isTimerIncrease();
			_timerText = template.getTimerText();
			_isPvPInstance = template.isPvPInstance();
			_returnCoords = template.getReturnCoords();
			_reuseUponEntry = template.getReuseUponEntry();
			_resetData = template.getReenterData();
			_isHwidCheck = template.isHwidCheck();
			_params = template.getParams().clone();
			
			if (template.getDoorList() != null && !template.getDoorList().isEmpty())
			{
				for (final int doorId : template.getDoorList().keySet())
				{
					addDoor(doorId, template.getDoorList().get(doorId));
				}
			}
			
			if (template.getSpawnsInfo() != null && !template.getSpawnsInfo().isEmpty())
			{
				for (final var s : template.getSpawnsInfo())
				{
					switch (s.getSpawnType())
					{
						case 0 :
							for (final var loc : s.getCoords())
							{
								try
								{
									final var tpl = new SpawnTemplate("none", s.getCount(), s.getRespawnDelay(), s.getRespawnRnd());
									tpl.addSpawnRange(loc);
									
									final var c = new Spawner(NpcsParser.getInstance().getTemplate(s.getId()));
									c.setAmount(s.getCount());
									c.setSpawnTemplate(tpl);
									c.setLocation(c.calcSpawnRangeLoc(NpcsParser.getInstance().getTemplate(s.getId())));
									c.setReflection(this);
									c.setRespawnDelay(s.getRespawnDelay(), s.getRespawnRnd());
									if (s.getRespawnDelay() == 0)
									{
										c.stopRespawn();
									}
									else
									{
										c.startRespawn();
									}
									c.spawnOne(true);
									_spawns.add(c);
								}
								catch (final Exception e)
								{
									_log.warn(getClass().getSimpleName() + ": Spawn could not be initialized: " + e.getMessage(), e);
								}
							}
							break;
						case 1 :
							final var loc = s.getCoords().get(Rnd.get(s.getCoords().size()));
							try
							{
								final var tpl = new SpawnTemplate("none", s.getCount(), s.getRespawnDelay(), s.getRespawnRnd());
								tpl.addSpawnRange(loc);
								
								final var c = new Spawner(NpcsParser.getInstance().getTemplate(s.getId()));
								c.setAmount(1);
								c.setSpawnTemplate(tpl);
								c.setLocation(c.calcSpawnRangeLoc(NpcsParser.getInstance().getTemplate(s.getId())));
								c.setReflection(this);
								c.setRespawnDelay(s.getRespawnDelay(), s.getRespawnRnd());
								if (s.getRespawnDelay() == 0)
								{
									c.stopRespawn();
								}
								else
								{
									c.startRespawn();
								}
								c.spawnOne(true);
								_spawns.add(c);
							}
							catch (final Exception e)
							{
								_log.warn(getClass().getSimpleName() + ": Spawn could not be initialized: " + e.getMessage(), e);
							}
							break;
						case 2 :
							int totalCount = 0;
							while (totalCount < s.getCount())
							{
								try
								{
									final var tpl = new SpawnTemplate("none", s.getCount(), s.getRespawnDelay(), s.getRespawnRnd());
									tpl.addSpawnRange(s.getLoc());
									
									final var c = new Spawner(NpcsParser.getInstance().getTemplate(s.getId()));
									c.setAmount(1);
									c.setSpawnTemplate(tpl);
									c.setLocation(c.calcSpawnRangeLoc(NpcsParser.getInstance().getTemplate(s.getId())));
									c.setReflection(this);
									c.setRespawnDelay(s.getRespawnDelay(), s.getRespawnRnd());
									if (s.getRespawnDelay() == 0)
									{
										c.stopRespawn();
									}
									else
									{
										c.startRespawn();
									}
									c.spawnOne(true);
									_spawns.add(c);
									totalCount++;
								}
								catch (final Exception e)
								{
									_log.warn(getClass().getSimpleName() + ": Spawn could not be initialized: " + e.getMessage(), e);
								}
							}
							break;
					}
				}
			}
			
			if (template.getSpawns().size() > 0)
			{
				_spawners = new HashMap<>(template.getSpawns().size());
				for (final var entry : template.getSpawns().entrySet())
				{
					final List<Spawner> spawnList = new ArrayList<>(entry.getValue().getTemplates().size());
					_spawners.put(entry.getKey(), spawnList);
					
					for (final var c : entry.getValue().getTemplates())
					{
						final var spawn = c.clone();
						spawn.setReflection(this);
						spawnList.add(spawn);
					}
					
					if (entry.getValue().isSpawned())
					{
						spawnByGroup(entry.getKey());
					}
				}
			}
		}
	}
	
	protected void doCheckTimeUp(int remaining)
	{
		CreatureSay cs = null;
		int timeLeft;
		int interval;
		
		if (_players.isEmpty() && (_emptyDestroyTime == 0))
		{
			remaining = 0;
			interval = 500;
		}
		else if (_players.isEmpty() && (_emptyDestroyTime > 0))
		{
			
			final Long emptyTimeLeft = (_lastLeft + _emptyDestroyTime) - System.currentTimeMillis();
			if (emptyTimeLeft <= 0)
			{
				interval = 0;
				remaining = 0;
			}
			else if ((remaining > 300000) && (emptyTimeLeft > 300000))
			{
				interval = 300000;
				remaining = remaining - 300000;
			}
			else if ((remaining > 60000) && (emptyTimeLeft > 60000))
			{
				interval = 60000;
				remaining = remaining - 60000;
			}
			else if ((remaining > 30000) && (emptyTimeLeft > 30000))
			{
				interval = 30000;
				remaining = remaining - 30000;
			}
			else
			{
				interval = 10000;
				remaining = remaining - 10000;
			}
		}
		else if (remaining > 300000)
		{
			timeLeft = remaining / 60000;
			interval = 300000;
			if (!_disableMessages)
			{
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DUNGEON_EXPIRES_IN_S1_MINUTES);
				sm.addString(Integer.toString(timeLeft));
				Announcements.getInstance().announceToInstance(sm, getId());
			}
			remaining = remaining - 300000;
		}
		else if (remaining > 60000)
		{
			timeLeft = remaining / 60000;
			interval = 60000;
			if (!_disableMessages)
			{
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DUNGEON_EXPIRES_IN_S1_MINUTES);
				sm.addString(Integer.toString(timeLeft));
				Announcements.getInstance().announceToInstance(sm, getId());
			}
			remaining = remaining - 60000;
		}
		else if (remaining > 30000)
		{
			timeLeft = remaining / 1000;
			interval = 30000;
			if (!_disableMessages)
			{
				cs = new CreatureSay(0, Say2.ALLIANCE, "Notice", timeLeft + " seconds left.");
			}
			remaining = remaining - 30000;
		}
		else
		{
			timeLeft = remaining / 1000;
			interval = 10000;
			if (!_disableMessages)
			{
				cs = new CreatureSay(0, Say2.ALLIANCE, "Notice", timeLeft + " seconds left.");
			}
			remaining = remaining - 10000;
		}
		
		if (cs != null)
		{
			for (final Integer objectId : _players)
			{
				final Player player = GameObjectsStorage.getPlayer(objectId);
				if ((player != null) && (player.getReflectionId() == getId()))
				{
					player.sendPacket(cs);
				}
			}
		}
		
		cancelTimer();
		if (remaining >= 10000)
		{
			_checkTimeUpTask = ThreadPoolManager.getInstance().schedule(new CheckTimeUp(remaining), interval);
		}
		else
		{
			_checkTimeUpTask = ThreadPoolManager.getInstance().schedule(new TimeUp(), interval);
		}
	}
	
	public void cancelTimer()
	{
		final var task = _checkTimeUpTask;
		if (task != null)
		{
			task.cancel(true);
		}
	}
	
	public class CheckTimeUp implements Runnable
	{
		private final int _remaining;
		
		public CheckTimeUp(int remaining)
		{
			_remaining = remaining;
		}
		
		@Override
		public void run()
		{
			doCheckTimeUp(_remaining);
		}
	}
	
	public class TimeUp implements Runnable
	{
		@Override
		public void run()
		{
			collapse();
		}
	}
	
	public void disableMessages()
	{
		_disableMessages = true;
	}
	
	public void addSpawn(Spawner spawn)
	{
		_spawns.add(spawn);
	}
	
	public void spawnByGroup(final String name)
	{
		final var list = _spawners.get(name);
		if (list == null)
		{
			return;
		}
		list.stream().filter(s -> s != null).forEach(s -> s.spawnOne(true));
	}
	
	public void despawnByGroup(final String name)
	{
		final var list = _spawners.get(name);
		if (list == null)
		{
			return;
		}
		list.stream().filter(s -> s != null).forEach(s -> s.deleteAll());
	}
	
	public boolean getReuseUponEntry()
	{
		return _reuseUponEntry;
	}
	
	public List<ReflectionReenterTimeHolder> getReenterData()
	{
		return _resetData;
	}
	
	public boolean isHwidCheck()
	{
		return _isHwidCheck;
	}
	
	public StatsSet getParams()
	{
		return _params;
	}
	
	public void setParam(String key, Object value)
	{
		if (value == null)
		{
			_params.remove(key);
		}
		else
		{
			_params.set(key, value);
		}
	}
	
	public void removeZone(int id)
	{
		if (_zones.contains(id))
		{
			_zones.remove(_zones.indexOf(Integer.valueOf(id)));
		}
	}
	
	public void addZone(int id)
	{
		if (!_zones.contains(id))
		{
			_zones.add(id);
		}
	}
	
	public boolean isDefault()
	{
		return getId() <= 0;
	}
	
	public void collapse()
	{
		if (isDefault())
		{
			return;
		}
		
		final var task = _checkTimeUpTask;
		if (task != null)
		{
			task.cancel(true);
		}
		
		try
		{
			stopAllTimers();
			cleanupNpcs();
			for (final int zoneId : _zones)
			{
				final var zone = ZoneManager.getInstance().getZoneById(zoneId, ReflectionZone.class);
				if (zone != null)
				{
					zone.removeRef(getId());
				}
			}
			_doors.valueCollection().stream().filter(d -> d != null).forEach(d -> d.deleteMe());
			
			final List<Player> teleport = new ArrayList<>();
			final List<GameObject> delete = new ArrayList<>();
			
			for (final var o : _objects)
			{
				if (o == null)
				{
					continue;
				}
				
				if (o.isPlayer())
				{
					teleport.add((Player) o);
				}
				else if (!o.isPlayable())
				{
					delete.add(o);
				}
			}
			
			for (final var player : teleport)
			{
				if (player != null && equals(player.getReflection()))
				{
					if (player.getParty() != null && player.getParty().getCommandChannel() != null && player.getParty().getCommandChannel().isLeader(player))
					{
						player.getParty().getCommandChannel().setReflection(ReflectionManager.DEFAULT);
					}
					
					if (getReturnLoc() != null)
					{
						player.teleToLocation(getReturnLoc(), true, ReflectionManager.DEFAULT);
					}
					else
					{
						player.teleToLocation(TeleportWhereType.TOWN, true, ReflectionManager.DEFAULT);
					}
				}
			}
			delete.stream().filter(o -> o != null).forEach(o -> o.deleteMe());
			_params.clear();
		}
		finally
		{
			_objects.clear();
			_allowed.clear();
			_doors.clear();
			_players.clear();
			_spawns.clear();
			_spawners.clear();
			ReflectionManager.getInstance().destroyRef(getId());
		}
	}
	
	public int getTemplateId()
	{
		return _templateId;
	}
	
	public void broadcastPacket(GameServerPacket... packets)
	{
		for (final var player : getReflectionPlayers())
		{
			for (final var packet : packets)
			{
				player.sendPacket(packet);
			}
		}
	}
	
	public int getStatus()
	{
		return _params.getInteger("INSTANCE_STATUS", 0);
	}
	
	public boolean isStatus(int status)
	{
		return getStatus() == status;
	}
	
	public void setStatus(int value)
	{
		_params.set("INSTANCE_STATUS", value);
	}
	
	public int incStatus()
	{
		final int status = getStatus() + 1;
		setStatus(status);
		return status;
	}
	
	public int decrStatus()
	{
		final int status = getStatus() - 1;
		setStatus(status);
		return status;
	}
	
	public void addTimer(String name, Future<?> timer)
	{
		final var task = _timers.get(name);
		if (task != null && !task.isDone())
		{
			task.cancel(false);
		}
		_timers.put(name, timer);
	}
	
	public void removeTimer(String name)
	{
		final var task = _timers.get(name);
		if (task != null && !task.isDone())
		{
			task.cancel(false);
		}
		_timers.remove(name);
	}
	
	public void stopAllTimers()
	{
		for (final var timer : _timers.values())
		{
			if (timer != null)
			{
				timer.cancel(false);
			}
		}
		_timers.clear();
	}
}