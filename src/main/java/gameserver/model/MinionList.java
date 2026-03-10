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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import l2e.commons.util.Rnd;
import gameserver.data.parser.NpcsParser;
import gameserver.idfactory.IdFactory;
import gameserver.model.actor.Npc;
import gameserver.model.actor.instance.MonsterInstance;
import gameserver.model.actor.templates.npc.MinionData;

public class MinionList
{
	private final List<MinionData> _minionData = new CopyOnWriteArrayList<>();
	private final List<MonsterInstance> _minions = new CopyOnWriteArrayList<>();
	private final Npc _master;
	private boolean _isRandomMinons = false;
	private int _totalMinions;

	public MinionList(Npc master)
	{
		_master = master;
		_master.setIsMinionMaster(true);
		_isRandomMinons = _master.getTemplate().isRandomMinons();
		_minionData.addAll(_master.getTemplate().getMinionData());
	}
	
	public boolean isRandomMinons()
	{
		return _isRandomMinons;
	}

	public boolean addMinion(MinionData m, boolean spawn)
	{
		if (_minionData.add(m))
		{
			if (spawn)
			{
				spawnMinion(m);
			}
			return true;
		}
		return false;
	}
	
	public boolean addMinion(MonsterInstance m)
	{
		return _minions.add(m);
	}
	
	public boolean hasAliveMinions()
	{
		for (final var m : _minions)
		{
			if (m.isVisible() && !m.isDead())
			{
				return true;
			}
		}
		return false;
	}

	public boolean hasMinions()
	{
		return _minionData.size() > 0;
	}
	

	public List<MonsterInstance> getAliveMinions()
	{
		if (_minions == null || _minions.isEmpty())
		{
			return Collections.emptyList();
		}
		
		final List<MonsterInstance> result = new ArrayList<>(_minions.size());
		_minions.stream().filter(m -> m != null && m.isVisible() && !m.isDead()).forEach(m -> result.add(m));
		return result;
	}
	
	public void spawnMinion(MinionData minions)
	{
		_totalMinions = 0;
		for (final var m : minions.getMinions())
		{
			_totalMinions += m.getAmount();
		}
		
		if (_totalMinions == 0)
		{
			return;
		}
		
		final boolean isCanSupportMinions = _master.getSpawn() != null && _master.getSpawn().getMinionList() != null;
		for (final var minion : minions.getMinions())
		{
			final int minionId = minion.getMinionId();
			final int minionCount = minion.getAmount();
			
			for (int i = 0; i < minionCount; i++)
			{
				final var m = new MonsterInstance(IdFactory.getInstance().getNextId(), NpcsParser.getInstance().getTemplate(minionId));
				m.setLeader(_master);
				m.setIsNoRndWalk(true);
				if (isCanSupportMinions)
				{
					m.isCanSupportMinion(false);
				}
				m.setIsRaidMinion(_master.isRaid());
				_master.spawnMinion(_master, m);
			}
		}
		
	}

	public void spawnMinions()
	{
		int minionCount;
		int minionId;
		
		_totalMinions = 0;
		for (final var minions : _minionData)
		{
			for (final var minion : minions.getMinions())
			{
				_totalMinions += minion.getAmount();
			}
		}
		
		if (_totalMinions == 0)
		{
			return;
		}
		final boolean isCanSupportMinions = _master.getSpawn() != null && _master.getSpawn().getMinionList() != null;
		final var positions = _master.getMinionPositions(-1);
		if (_master.isRaid() && !positions.isEmpty())
		{
			final int delimiter = Math.max(1, Math.round(positions.size() / _totalMinions));
			int c = 0;
			var canGeneratePos = true;
			for (final var minions : _minionData)
			{
				for (final var minion : minions.getMinions())
				{
					minionId = minion.getMinionId();
					minionCount = minion.getAmount();
					
					for (int i = 0; i < minionCount; i++)
					{
						final var m = new MonsterInstance(IdFactory.getInstance().getNextId(), NpcsParser.getInstance().getTemplate(minionId));
						m.setLeader(_master);
						m.setIsNoRndWalk(true);
						if (isCanSupportMinions)
						{
							m.isCanSupportMinion(false);
						}
						
						if (canGeneratePos)
						{
							m.setMinionLocation(positions.get(c));
						}
						m.setIsRaidMinion(_master.isRaid());
						_master.spawnMinion(_master, m);
						_minions.add(m);
						c += delimiter;
						if (c >= positions.size())
						{
							canGeneratePos = false;
						}
					}
				}
			}
		}
		else
		{
			for (final var minions : _minionData)
			{
				for (final var minion : minions.getMinions())
				{
					minionId = minion.getMinionId();
					minionCount = minion.getAmount();
					
					for (final MonsterInstance m : _minions)
					{
						if (m.getId() == minionId)
						{
							minionCount--;
						}
						if (m.isDead() || !m.isVisible())
						{
							_master.spawnMinion(_master, m);
						}
					}
					
					for (int i = 0; i < minionCount; i++)
					{
						final var m = new MonsterInstance(IdFactory.getInstance().getNextId(), NpcsParser.getInstance().getTemplate(minionId));
						m.setLeader(_master);
						m.setIsNoRndWalk(true);
						if (isCanSupportMinions)
						{
							m.isCanSupportMinion(false);
						}
						m.setIsRaidMinion(_master.isRaid());
						_master.spawnMinion(_master, m);
						_minions.add(m);
					}
				}
			}
		}
	}
	
	public void spawnRndMinions()
	{
		final var data = _minionData.size() > 1 ? _minionData.get(Rnd.get(_minionData.size())) : _minionData.getFirst();
		if (data == null)
		{
			return;
		}
		
		_totalMinions = 0;
		for (final var minions : data.getMinions())
		{
			_totalMinions += minions.getAmount();
		}
		
		if (_totalMinions == 0)
		{
			return;
		}
		
		final boolean isCanSupportMinions = _master.getSpawn() != null && _master.getSpawn().getMinionList() != null;
		for (final var minions : data.getMinions())
		{
			final int minionId = minions.getMinionId();
			int minionCount = minions.getAmount();
			
			for (final var m : _minions)
			{
				if (m.getId() == minionId)
				{
					minionCount--;
				}
				if (m.isDead() || !m.isVisible())
				{
					_master.spawnMinion(_master, m);
				}
			}
			
			for (int i = 0; i < minionCount; i++)
			{
				final var m = new MonsterInstance(IdFactory.getInstance().getNextId(), NpcsParser.getInstance().getTemplate(minionId));
				m.setLeader(_master);
				m.setIsNoRndWalk(true);
				if (isCanSupportMinions)
				{
					m.isCanSupportMinion(false);
				}
				m.setIsRaidMinion(_master.isRaid());
				_master.spawnMinion(_master, m);
				_minions.add(m);
			}
		}
	}
	
	private void despawnMinions()
	{
		if (_minions == null || _minions.isEmpty())
		{
			return;
		}
		_minions.stream().filter(m -> m != null).forEach(m -> m.deleteMe());
	}
	
	public void onMasterDeath()
	{
		if (_master.isRaid())
		{
			despawnMinions();
		}
	}
	
	public void onMasterDelete()
	{
		despawnMinions();
		_minions.clear();
	}
	
	public void clearMinions()
	{
		_minionData.clear();
		_minions.clear();
	}
	
	public void clearMinionList()
	{
		_minions.clear();
	}
	
	public boolean hasNpcId(int npcId)
	{
		if (_master != null && _master.getId() == npcId)
		{
			return true;
		}
		
		if (_minions == null || _minions.isEmpty())
		{
			return false;
		}
		
		for (final var m : _minions)
		{
			if (m != null && m.getId() == npcId)
			{
				return true;
			}
		}
		return false;
	}
}