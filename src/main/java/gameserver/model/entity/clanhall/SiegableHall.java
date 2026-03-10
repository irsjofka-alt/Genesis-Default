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
package gameserver.model.entity.clanhall;

import java.sql.Connection;
import java.sql.PreparedStatement;

import l2e.commons.dbutils.DbUtils;
import l2e.commons.time.cron.SchedulingPattern;
import l2e.commons.time.cron.SchedulingPattern.InvalidPatternException;
import gameserver.database.DatabaseFactory;
import gameserver.model.Clan;
import gameserver.model.SiegeClan;
import gameserver.model.SiegeClan.SiegeClanType;
import gameserver.model.actor.Player;
import gameserver.model.actor.instance.DoorInstance;
import gameserver.model.actor.templates.residences.clanhall.ClanHallTemplate;
import gameserver.model.entity.ClanHall;
import gameserver.model.stats.StatsSet;
import gameserver.model.zone.type.SiegableHallZone;
import gameserver.model.zone.type.SiegeZone;
import gameserver.network.serverpackets.CastleSiegeInfo;

public final class SiegableHall extends ClanHall
{
	private final int _grade;
	private long _nextSiege;
	private final long _siegeTime;
	private final String _schedulePattern;
	
	private SiegeStatus _status = SiegeStatus.REGISTERING;
	private SiegeZone _siegeZone;
	private ClanHallSiegeEngine _siege;

	public SiegableHall(ClanHallTemplate template, StatsSet set)
	{
		super(template, set);

		_grade = set.getInteger("grade");
		_siegeTime = set.getLong("siegeTime");
		_schedulePattern = set.getString("siegeDate");
		if (getOwnerId() != 0)
		{
			loadFunctions();
		}

		final long nextSiege = set.getLong("nextSiege");
		if ((nextSiege - System.currentTimeMillis()) < 0)
		{
			updateNextSiege();
		}
		else
		{
			_nextSiege = nextSiege;
		}
	}

	public void spawnDoor()
	{
		spawnDoor(false);
	}

	public void spawnDoor(boolean isDoorWeak)
	{
		for (final DoorInstance door : getDoors())
		{
			if (door.isDead())
			{
				door.doRevive();
				if (isDoorWeak)
				{
					door.setCurrentHp(door.getMaxHp() / 2);
				}
				else
				{
					door.setCurrentHp(door.getMaxHp());
				}
			}

			if (door.getOpen())
			{
				door.closeMe();
			}
		}
	}

	@Override
	public final void updateDb()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE siegable_clanhall SET ownerId=?, nextSiege=? WHERE id=?");
			statement.setInt(1, getOwnerId());
			statement.setLong(2, getNextSiegeTime());
			statement.setInt(3, getId());
			statement.execute();
		}
		catch (final Exception e)
		{
			_log.warn("Exception: SiegableHall.updateDb(): " + e.getMessage(), e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public final void setSiege(final ClanHallSiegeEngine siegable)
	{
		_siege = siegable;
		_siegeZone.setSiegeInstance(siegable);
	}

	public final ClanHallSiegeEngine getSiege()
	{
		return _siege;
	}

	public final long getNextSiegeTime()
	{
		return _nextSiege;
	}

	public long getSiegeTime()
	{
		return _siegeTime;
	}

	public final void setNextSiegeDate(long time)
	{
		_nextSiege = time;
	}

	public final void updateNextSiege()
	{
		SchedulingPattern cronTime;
		try
		{
			cronTime = new SchedulingPattern(_schedulePattern);
		}
		catch (final InvalidPatternException e)
		{
			return;
		}
		
		final long siegeTime = cronTime.next(System.currentTimeMillis());
		setNextSiegeDate(siegeTime);
		updateDb();
	}

	public final void addAttacker(final Clan clan)
	{
		if (getSiege() != null)
		{
			getSiege().getAttackers().put(clan.getId(), new SiegeClan(clan.getId(), SiegeClanType.ATTACKER));
		}
	}

	public final void removeAttacker(final Clan clan)
	{
		if (getSiege() != null)
		{
			getSiege().getAttackers().remove(clan.getId());
		}
	}

	public final boolean isRegistered(Clan clan)
	{
		if (getSiege() == null)
		{
			return false;
		}

		return getSiege().checkIsAttacker(clan);
	}

	public SiegeStatus getSiegeStatus()
	{
		return _status;
	}

	public final boolean isRegistering()
	{
		return _status == SiegeStatus.REGISTERING;
	}

	public final boolean isInSiege()
	{
		return _status == SiegeStatus.RUNNING;
	}

	public final boolean isWaitingBattle()
	{
		return _status == SiegeStatus.WAITING_BATTLE;
	}

	public final void updateSiegeStatus(SiegeStatus status)
	{
		_status = status;
	}

	public final SiegeZone getSiegeZone()
	{
		return _siegeZone;
	}

	public final void setSiegeZone(SiegeZone zone)
	{
		_siegeZone = zone;
	}

	public final void updateSiegeZone(boolean active)
	{
		_siegeZone.setIsActive(active);
	}

	public final void showSiegeInfo(Player player)
	{
		player.sendPacket(new CastleSiegeInfo(this));
	}

	@Override
	public final int getGrade()
	{
		return _grade;
	}

	@Override
	public final boolean isSiegableHall()
	{
		return true;
	}

	@Override
	public SiegableHallZone getZone()
	{
		return (SiegableHallZone) super.getZone();
	}
}