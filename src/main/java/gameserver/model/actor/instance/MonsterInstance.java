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
package gameserver.model.actor.instance;

import java.util.function.Function;

import gameserver.Config;
import gameserver.model.Location;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Npc;
import gameserver.model.actor.templates.npc.MinionData;
import gameserver.model.actor.templates.npc.MinionTemplate;
import gameserver.model.actor.templates.npc.NpcTemplate;

public class MonsterInstance extends Attackable
{
	private boolean _canAgroWhileMoving = false;
	private boolean _isAutoAttackable = true;
	private boolean _isPassive = false;
	protected int _aggroRangeOverride = 0;
	private Function<Npc, Boolean> _conditions = null;
	
	public MonsterInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.MonsterInstance);
	}
	
	public int getKilledInterval(MonsterInstance minion)
	{
		final int respawnTime = Config.MINIONS_RESPAWN_TIME.containsKey(minion.getId()) ? Config.MINIONS_RESPAWN_TIME.get(minion.getId()) * 1000 : -1;
		return respawnTime < 0 ? 0 : respawnTime;
	}
	
	public int getMinionUnspawnInterval()
	{
		return 5000;
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker, boolean isPoleAttack)
	{
		return _isAutoAttackable && !isEventMob() && (attacker != null && !attacker.isMonster());
	}
	
	@Override
	public boolean isAggressive()
	{
		if (_isPassive)
		{
			return false;
		}
		return (getAggroRange() > 0) && !isEventMob();
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		if (!isTeleporting())
		{
			final var spawn = getSpawn();
			final var minions = getMinionList();
			if (spawn != null && spawn.getMinionList() != null && !minions.hasMinions() && isCanSupportMinion())
			{
				final int[] minionList = spawn.getMinionList();
				if (minionList != null)
				{
					minions.addMinion(new MinionData(new MinionTemplate(minionList[0], minionList[1])), false);
				}
			}
			spawnMinions();
		}
	}
	
	public void applyCondition(Function<Npc, Boolean> conditions)
	{
		_conditions = conditions;
	}
	
	protected int getMaintenanceInterval()
	{
		return 1000;
	}
	
	@Override
	protected void onDeath(Creature killer)
	{
		super.onDeath(killer);
		final var leader = getLeader();
		if (leader != null)
		{
			if (killer != null && killer.isPlayer())
			{
				killer.getActingPlayer().getCounters().addAchivementInfo("minionKiller", getId(), -1, false, true, false);
			}
			leader.notifyMinionDied(this);
		}
	}
	
	@Override
	public boolean isMonster()
	{
		return true;
	}
	
	@Override
	public Npc getActingNpc()
	{
		return this;
	}
	
	public final boolean canAgroWhileMoving()
	{
		return _canAgroWhileMoving;
	}
	
	public final void setCanAgroWhileMoving()
	{
		_canAgroWhileMoving = true;
	}
	
	public void setClanOverride(String newClan)
	{
	}
	
	public void setIsAggresiveOverride(int aggroR)
	{
		_aggroRangeOverride = aggroR;
	}
	
	@Override
	public void addDamageHate(Creature attacker, long damage, long aggro)
	{
		if (!_isPassive)
		{
			super.addDamageHate(attacker, damage, aggro);
		}
	}
	
	public void setPassive(boolean state)
	{
		_isPassive = state;
	}
	
	public boolean isPassive()
	{
		return _isPassive;
	}
	
	@Override
	public void setAutoAttackable(boolean state)
	{
		_isAutoAttackable = state;
	}
	
	@Override
	public boolean isWalker()
	{
		return ((getLeader() == null) ? super.isWalker() : getLeader().isWalker());
	}
	
	@Override
	public boolean isRunner()
	{
		return ((getLeader() == null) ? super.isRunner() : getLeader().isRunner());
	}
	
	@Override
	public boolean isEkimusFood()
	{
		return ((getLeader() == null) ? super.isEkimusFood() : getLeader().isEkimusFood());
	}

	@Override
	public boolean isSpecialCamera()
	{
		return ((getLeader() == null) ? super.isSpecialCamera() : getLeader().isSpecialCamera());
	}
	
	@Override
	public boolean giveRaidCurse()
	{
		return (isRaidMinion() && (getLeader() != null)) ? getLeader().giveRaidCurse() : super.giveRaidCurse();
	}
	
	private void spawnMinions()
	{
		final var minionList = getMinionList();
		if (minionList.hasMinions())
		{
			if (minionList.isRandomMinons())
			{
				minionList.onMasterDelete();
			}
			
			if (isDead())
			{
				return;
			}
			
			if (_conditions != null && !_conditions.apply(this))
			{
				return;
			}
			
			if (minionList.isRandomMinons())
			{
				minionList.spawnRndMinions();
			}
			else
			{
				minionList.spawnMinions();
			}
		}
	}
	
	@Override
	public Location getSpawnedLoc()
	{
		final var leader = getLeader();
		if (leader != null)
		{
			return leader.getSpawnedLoc();
		}
		return super.getSpawnedLoc();
	}
	
	@Override
	public boolean isLethalImmune()
	{
		return getId() == 22215 || getId() == 22216 || getId() == 22217 || super.isLethalImmune();
	}
}