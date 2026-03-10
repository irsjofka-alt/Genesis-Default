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

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import gameserver.Config;
import gameserver.ThreadPoolManager;
import gameserver.data.holder.SpawnHolder;
import gameserver.data.parser.SpawnParser;
import gameserver.model.actor.Npc;

public final class WorldRegion implements Iterable<GameObject>
{
	private final int _tileX, _tileY, _tileZ;
	private volatile GameObject[] _objects = new GameObject[0];
	private volatile Npc[] _npcs = new Npc[0];
	private int _objectsCount = 0;
	private int _npcsCount = 0;
	private int _playersCount = 0;
	private boolean _active;
	private Future<?> _activateTask = null;
	private Future<?> _animationTask = null;
	private final Lock _lock = new ReentrantLock();

	WorldRegion(int pTileX, int pTileY, int pTileZ)
	{
		_tileX = pTileX;
		_tileY = pTileY;
		_tileZ = pTileZ;
	}
	
	private class ActivateTask implements Runnable
	{
		private final boolean _isActivating;
		
		public ActivateTask(boolean isActivating)
		{
			_isActivating = isActivating;
		}
		
		@Override
		public void run()
		{
			if (_isActivating)
			{
				World.activate(WorldRegion.this);
			}
			else
			{
				World.deactivate(WorldRegion.this);
			}
		}
	}

	void setActive(boolean value)
	{
		if (_active == value)
		{
			return;
		}
		_active = value;
		
		for (final var o : this)
		{
			if (o != null && o.isNpc())
			{
				o.wakeUp(value);
			}
		}
		
		if (_active && Config.NPC_ANIMATION_INTERVAL > 0 && _npcs.length > 0)
		{
			if (_animationTask == null)
			{
				_animationTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> Arrays.asList(_npcs).stream().filter(n -> n != null && n.isVisible()).forEach(n -> n.onRandomAnimation()), Math.max(1000, Config.NPC_ANIMATION_INTERVAL), Math.max(1000, Config.NPC_ANIMATION_INTERVAL));
			}
		}
		else
		{
			final var oldTask = _animationTask;
			if (oldTask != null)
			{
				oldTask.cancel(false);
				_animationTask = null;
			}
			_npcsCount = 0;
			_npcs = new Npc[0];
		}
	}
	
	void addToVisible(GameObject object)
	{
		if (object == null)
		{
			return;
		}
		
		final int oid = object.getObjectId();
		final int rid = object.getReflectionId();
		
		for (final var obj : this)
		{
			if (obj == null || obj.getObjectId() == oid || (obj.getReflectionId() != rid))
			{
				continue;
			}
			
			if (obj.isPlayer() && object.isPlayer())
			{
				if (obj.getActingPlayer().getAppearance().isGhost() || (obj.getActingPlayer().isInStoreNow() && object.getActingPlayer().getNotShowTraders()))
				{
					obj.addInfoObject(object);
					continue;
				}
			}
			obj.addInfoObject(object);
			object.addInfoObject(obj);
		}
	}
	
	void removeFromVisible(GameObject object)
	{
		if (object == null)
		{
			return;
		}
		
		final int oid = object.getObjectId();
		final int rid = object.getReflectionId();
		
		for (final var obj : this)
		{
			if (obj == null || obj.getObjectId() == oid || obj.getReflectionId() != rid)
			{
				continue;
			}
			obj.removeInfoObject(object);
			object.removeInfoObject(obj);
		}
	}
	
	void forgetObject(GameObject object)
	{
		if (object == null)
		{
			return;
		}
		
		final int oid = object.getObjectId();
		final int rid = object.getReflectionId();
		
		for (final var obj : this)
		{
			if (obj == null || obj.getObjectId() == oid || obj.getReflectionId() != rid)
			{
				continue;
			}
			obj.removeInfoObject(object);
			object.removeInfoObject(obj);
		}
	}
	
	public void addObject(GameObject obj)
	{
		if (obj == null)
		{
			return;
		}
		
		_lock.lock();
		try
		{
			var objects = _objects;
			final var resizedObjects = new GameObject[_objectsCount + 1];
			System.arraycopy(objects, 0, resizedObjects, 0, _objectsCount);
			objects = resizedObjects;
			objects[_objectsCount++] = obj;
			_objects = resizedObjects;
			
			if (obj.isPlayer())
			{
				if (_playersCount++ == 0)
				{
					final var task = _activateTask;
					if (task != null)
					{
						task.cancel(false);
					}
					_activateTask = ThreadPoolManager.getInstance().schedule(new ActivateTask(true), 1000L);
				}
			}
		}
		finally
		{
			_lock.unlock();
		}
	}
	
	public void removeObject(GameObject obj)
	{
		if (obj == null)
		{
			return;
		}
		
		_lock.lock();
		try
		{
			final var objects = _objects;
			int index = -1;
			for (int i = 0; i < _objectsCount; i++)
			{
				if (objects[i] == obj)
				{
					index = i;
					break;
				}
			}
			
			if (index == -1)
			{
				return;
			}
			
			_objectsCount--;
			
			final var resizedObjects = new GameObject[_objectsCount];
			objects[index] = objects[_objectsCount];
			System.arraycopy(objects, 0, resizedObjects, 0, _objectsCount);
			
			_objects = resizedObjects;
			
			if (obj.isPlayer())
			{
				if (--_playersCount == 0)
				{
					final var task = _activateTask;
					if (task != null)
					{
						task.cancel(false);
					}
					_activateTask = ThreadPoolManager.getInstance().schedule(new ActivateTask(false), 60000L);
				}
			}
		}
		finally
		{
			_lock.unlock();
		}
	}
	
	public int getObjectsSize()
	{
		return _objectsCount;
	}
	
	public int getPlayersCount()
	{
		return _playersCount;
	}
	
	public boolean isEmpty()
	{
		return _playersCount == 0;
	}

	public boolean isActive()
	{
		return _active;
	}

	public void addAnimationNpc(Npc npc)
	{
		if (!npc.isVisible())
		{
			return;
		}
		
		_lock.lock();
		try
		{
			var npcs = _npcs;
			final var resizedNpcs = new Npc[_npcsCount + 1];
			System.arraycopy(npcs, 0, resizedNpcs, 0, _npcsCount);
			npcs = resizedNpcs;
			npcs[_npcsCount++] = npc;
			_npcs = resizedNpcs;
		}
		finally
		{
			_lock.unlock();
		}
	}
	
	int getX()
	{
		return _tileX;
	}
	
	int getY()
	{
		return _tileY;
	}

	int getZ()
	{
		return _tileZ;
	}
	
	@Override
	public String toString()
	{
		return "(" + _tileX + ", " + _tileY + ", " + _tileZ + ")";
	}
	
	@Override
	public Iterator<GameObject> iterator()
	{
		return new InternalIterator(_objects);
	}
	
	private class InternalIterator implements Iterator<GameObject>
	{
		final GameObject[] objects;
		int cursor = 0;
		
		public InternalIterator(final GameObject[] objs)
		{
			objects = objs;
		}

		@Override
		public boolean hasNext()
		{
			if (cursor < objects.length)
			{
				return objects[cursor] != null;
			}
			return false;
		}
		
		@Override
		public GameObject next()
		{
			return objects[cursor++];
		}
		
		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}
	
	public void deleteVisibleNpcSpawns()
	{
		for (final var o : _objects)
		{
			if (o != null && o.isNpc())
			{
				final var target = (Npc) o;
				final var spawn = target.getSpawn();
				if (spawn != null)
				{
					spawn.stopRespawn();
					SpawnParser.getInstance().deleteSpawn(spawn);
					SpawnHolder.getInstance().deleteSpawn(spawn, false);
				}
				target.deleteMe();
			}
		}
	}
}