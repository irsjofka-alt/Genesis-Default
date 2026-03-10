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

import java.util.Map;

import gameserver.Config;
import gameserver.ai.character.CharacterAI;
import gameserver.ai.guard.FortGuardAI;
import gameserver.ai.guard.GuardAI;
import gameserver.ai.guard.SpecialGuardAI;
import gameserver.ai.model.CtrlIntention;
import gameserver.instancemanager.CastleManager;
import gameserver.instancemanager.FortManager;
import gameserver.instancemanager.TerritoryWarManager;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Playable;
import gameserver.model.actor.Player;
import gameserver.model.actor.Summon;
import gameserver.model.actor.templates.npc.NpcTemplate;
import gameserver.model.entity.Castle;
import gameserver.model.entity.Fort;
import gameserver.model.entity.clanhall.SiegableHall;
import gameserver.model.reward.RewardList;
import gameserver.model.reward.RewardType;
import gameserver.taskmanager.ItemsAutoDestroy;

public class DefenderInstance extends Attackable
{
	private Castle _castle = null;
	private Fort _fort = null;
	private SiegableHall _hall = null;

	public DefenderInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.DefenderInstance);
		if ((getConquerableHall() == null) && (getCastle(10000) == null))
		{
			_ai = new FortGuardAI(this);
		}
		else if (getCastle(10000) != null)
		{
			_ai = new GuardAI(this);
		}
		else
		{
			_ai = new SpecialGuardAI(this);
		}
		setIsSiegeGuard(true);
		setIsGlobalAI(true);
	}
	
	@Override
	public CharacterAI getAI()
	{
		return _ai;
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker, boolean isPoleAttack)
	{
		if (!(attacker instanceof Playable))
		{
			return false;
		}
		
		final Player player = attacker.getActingPlayer();
		
		if (((_fort != null) && _fort.getZone().isActive()) || ((_castle != null) && _castle.getZone().isActive()) || ((_hall != null) && _hall.getSiegeZone().isActive()))
		{
			final int activeSiegeId = (_fort != null ? _fort.getId() : (_castle != null ? _castle.getId() : (_hall != null ? _hall.getId() : 0)));
			if ((player != null) && (((player.getSiegeState() == 2) && !player.isRegisteredOnThisSiegeField(activeSiegeId)) || ((player.getSiegeState() == 1) && !TerritoryWarManager.getInstance().isAllyField(player, activeSiegeId)) || (player.getSiegeState() == 0)))
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	protected void onDeath(Creature killer)
	{
		super.onDeath(killer);
		if (killer != null && killer.isPlayer())
		{
			killer.getActingPlayer().getCounters().addAchivementInfo("siegeGuardKiller", getId(), -1, false, true, false);
		}
		
		if (killer != null && (killer.isSummon() || killer.isPlayer()) && Config.EPAULETTE_ONLY_FOR_REG)
		{
			final Player player = killer.isSummon() ? ((Summon) killer).getOwner() : killer.getActingPlayer();
			if (player == null)
			{
				super.onDeath(killer);
				return;
			}
			final int activeSiegeId = (_fort != null ? _fort.getId() : (_castle != null ? _castle.getId() : (_hall != null ? _hall.getId() : 0)));
			if ((TerritoryWarManager.getInstance().isTWInProgress() && (player.getSiegeState() == 1) && !TerritoryWarManager.getInstance().isAllyField(player, activeSiegeId)) || (player.getClan() != null && ((_fort != null && _fort.getId() > 0 && _fort.getSiege().checkIsAttacker(player.getClan())) || (_castle != null && _castle.getId() > 0 && _castle.getSiege().checkIsAttacker(player.getClan())))))
			{
				final Creature topDamager = getAggroList().getTopDamager(killer);
				for (final Map.Entry<RewardType, RewardList> entry : getTemplate().getRewards().entrySet())
				{
					rollRewards(entry, killer, topDamager != null ? topDamager : killer);
				}
				ItemsAutoDestroy.getInstance().tryRecalcTime();
			}
		}
	}

	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}
	
	@Override
	public void returnHome()
	{
		if (getWalkSpeed() <= 0)
		{
			return;
		}
		if (getSpawn() == null)
		{
			return;
		}
		if (!isInsideRadius(getSpawn().getX(), getSpawn().getY(), 40, false))
		{
			if (Config.DEBUG)
			{
				_log.info(getObjectId() + ": moving home");
			}
			setisReturningToSpawnPoint(true);
			clearAggroList(true);
			
			final var ai = getAI();
			if (ai != null)
			{
				ai.setIntention(CtrlIntention.MOVING, getSpawn().getLocation(), 0);
			}
		}
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		
		_fort = FortManager.getInstance().getFort(getX(), getY(), getZ());
		_castle = CastleManager.getInstance().getCastle(getX(), getY(), getZ());
		_hall = getConquerableHall();
		if ((_fort == null) && (_castle == null) && (_hall == null))
		{
			_log.warn("DefenderInstance spawned outside of Fortress, Castle or Siegable hall Zone! NpcId: " + getId() + " x=" + getX() + " y=" + getY() + " z=" + getZ());
		}
	}
	
	@Override
	public void onAction(Player player, boolean interact, boolean shift)
	{
		if (!canTarget(player))
		{
			player.sendActionFailed();
			return;
		}
		
		if (this != player.getTarget())
		{
			if (Config.DEBUG)
			{
				_log.info("new target selected:" + getObjectId());
			}
			player.setTarget(this);
		}
		else if (interact)
		{
			if (isAutoAttackable(player, false) && !isAlikeDead())
			{
				if (Math.abs(player.getZ() - getZ()) < 600)
				{
					player.getAI().setIntention(CtrlIntention.ATTACK, this);
				}
			}
			if (!isAutoAttackable(player, false))
			{
				if (!canInteract(player))
				{
					player.getAI().setIntention(CtrlIntention.INTERACT, this);
				}
			}
		}
		player.sendActionFailed();
	}
	
	@Override
	public void addDamageHate(Creature attacker, long damage, long aggro)
	{
		if (attacker == null)
		{
			return;
		}
		
		if (!(attacker instanceof DefenderInstance))
		{
			if ((damage == 0) && (aggro <= 1) && (attacker instanceof Playable))
			{
				final Player player = attacker.getActingPlayer();
				
				if (((_fort != null) && _fort.getZone().isActive()) || ((_castle != null) && _castle.getZone().isActive()) || ((_hall != null) && _hall.getSiegeZone().isActive()))
				{
					final int activeSiegeId = (_fort != null ? _fort.getId() : (_castle != null ? _castle.getId() : (_hall != null ? _hall.getId() : 0)));
					if ((player != null) && (((player.getSiegeState() == 2) && player.isRegisteredOnThisSiegeField(activeSiegeId)) || ((player.getSiegeState() == 1) && TerritoryWarManager.getInstance().isAllyField(player, activeSiegeId))))
					{
						return;
					}
				}
			}
			super.addDamageHate(attacker, damage, aggro);
		}
	}
}