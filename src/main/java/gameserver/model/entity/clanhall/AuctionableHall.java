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
import gameserver.ThreadPoolManager;
import gameserver.data.holder.ClanHolder;
import gameserver.database.DatabaseFactory;
import gameserver.instancemanager.AuctionManager;
import gameserver.instancemanager.ClanHallManager;
import gameserver.model.Clan;
import gameserver.model.actor.templates.residences.clanhall.ClanHallTemplate;
import gameserver.model.entity.ClanHall;
import gameserver.model.stats.StatsSet;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.SystemMessage;

public final class AuctionableHall extends ClanHall
{
	protected long _paidUntil;
	private final int _grade;
	protected boolean _paid;
	private final int _rentId;
	private final long _rentPrice;
	protected final int _chRate = 604800000;
	
	public AuctionableHall(ClanHallTemplate template, StatsSet set)
	{
		super(template, set);
		
		_paidUntil = set.getLong("paidUntil");
		_grade = set.getInteger("grade");
		_paid = set.getBool("paid");
		final String[] price = set.getString("rentPrice").split("-");
		_rentId = Integer.parseInt(price[0]);
		_rentPrice = Long.parseLong(price[1]);
		
		if (getOwnerId() != 0)
		{
			_isFree = false;
			initialyzeTask(false);
			loadFunctions();
		}
	}
	
	public final boolean getPaid()
	{
		return _paid;
	}
	
	@Override
	public final int getRentId()
	{
		return _rentId;
	}
	
	@Override
	public final long getRentPrice()
	{
		return _rentPrice;
	}
	
	@Override
	public final long getPaidUntil()
	{
		return _paidUntil;
	}
	
	@Override
	public final int getGrade()
	{
		return _grade;
	}
	
	@Override
	public final void free()
	{
		super.free();
		_paidUntil = 0;
		_paid = false;
	}
	
	@Override
	public final void setOwner(Clan clan)
	{
		super.setOwner(clan);
		_paidUntil = System.currentTimeMillis();
		initialyzeTask(true);
	}
	
	private final void initialyzeTask(boolean forced)
	{
		final long currentTime = System.currentTimeMillis();
		if (_paidUntil > currentTime)
		{
			ThreadPoolManager.getInstance().schedule(new FeeTask(), _paidUntil - currentTime);
		}
		else if (!_paid && !forced)
		{
			if ((System.currentTimeMillis() + (3600000 * 24)) <= (_paidUntil + _chRate))
			{
				ThreadPoolManager.getInstance().schedule(new FeeTask(), System.currentTimeMillis() + (3600000 * 24));
			}
			else
			{
				ThreadPoolManager.getInstance().schedule(new FeeTask(), (_paidUntil + _chRate) - System.currentTimeMillis());
			}
		}
		else
		{
			ThreadPoolManager.getInstance().schedule(new FeeTask(), 0);
		}
	}
	
	protected class FeeTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				final long _time = System.currentTimeMillis();
				if (isFree())
				{
					return;
				}
				
				if (_paidUntil > _time)
				{
					ThreadPoolManager.getInstance().schedule(new FeeTask(), _paidUntil - _time);
					return;
				}

				final var clan = ClanHolder.getInstance().getClan(getOwnerId());
				if (clan == null)
				{
					return;
				}
				if (clan.getWarehouse().getInventoryItemCount(getRentId(), 0) >= getRentPrice())
				{
					if (_paidUntil != 0)
					{
						while (_paidUntil <= _time)
						{
							_paidUntil += _chRate;
						}
					}
					else
					{
						_paidUntil = _time + _chRate;
					}
					clan.getWarehouse().destroyItemByItemId("CH_rental_fee", getRentId(), getRentPrice(), null, null);
					ThreadPoolManager.getInstance().schedule(new FeeTask(), _paidUntil - _time);
					_paid = true;
					updateDb();
				}
				else
				{
					_paid = false;
					if (_time > (_paidUntil + _chRate))
					{
						if (ClanHallManager.getInstance().loaded())
						{
							AuctionManager.getInstance().initNPC(getId());
							ClanHallManager.getInstance().setFree(getId());
							clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.THE_CLAN_HALL_FEE_IS_ONE_WEEK_OVERDUE_THEREFORE_THE_CLAN_HALL_OWNERSHIP_HAS_BEEN_REVOKED));
						}
						else
						{
							ThreadPoolManager.getInstance().schedule(new FeeTask(), 3000);
						}
					}
					else
					{
						updateDb();
						final var sm = SystemMessage.getSystemMessage(SystemMessageId.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW);
						sm.addNumber((int) getRentPrice());
						clan.broadcastToOnlineMembers(sm);
						if ((_time + (3600000 * 24)) <= (_paidUntil + _chRate))
						{
							ThreadPoolManager.getInstance().schedule(new FeeTask(), _time + (3600000 * 24));
						}
						else
						{
							ThreadPoolManager.getInstance().schedule(new FeeTask(), (_paidUntil + _chRate) - _time);
						}
						
					}
				}
			}
			catch (final Exception e)
			{
				_log.warn("", e);
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
			statement = con.prepareStatement("UPDATE clanhall SET ownerId=?, paidUntil=?, paid=? WHERE id=?");
			statement.setInt(1, getOwnerId());
			statement.setLong(2, getPaidUntil());
			statement.setInt(3, (getPaid()) ? 1 : 0);
			statement.setInt(4, getId());
			statement.execute();
		}
		catch (final Exception e)
		{
			_log.warn("Exception: updateOwnerInDB(Clan clan): " + e.getMessage(), e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
}