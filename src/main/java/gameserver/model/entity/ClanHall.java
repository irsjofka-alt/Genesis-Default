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
package gameserver.model.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import l2e.commons.dbutils.DbUtils;
import gameserver.ThreadPoolManager;
import gameserver.data.holder.ClanHolder;
import gameserver.data.parser.SkillTreesParser;
import gameserver.data.parser.SkillsParser;
import gameserver.database.DatabaseFactory;
import gameserver.model.Clan;
import gameserver.model.actor.Player;
import gameserver.model.actor.instance.DoorInstance;
import gameserver.model.actor.templates.residences.clanhall.ClanHallTemplate;
import gameserver.model.skills.Skill;
import gameserver.model.stats.StatsSet;
import gameserver.model.zone.type.ClanHallZone;
import gameserver.network.serverpackets.pledge.PledgeShowInfoUpdate;

public abstract class ClanHall
{
	public static final Logger _log = LogManager.getLogger(ClanHall.class);
	
	private final int _clanHallId;
	private final ClanHallTemplate _template;
	private ArrayList<DoorInstance> _doors;
	private int _ownerId;
	private ClanHallZone _zone;
	protected final int _chRate = 604800000;
	protected boolean _isFree = true;
	private final Map<Integer, ClanHallFunction> _functions;
	private final List<Skill> _residentialSkills = new CopyOnWriteArrayList<>();
	
	public static final int FUNC_TELEPORT = 1;
	public static final int FUNC_ITEM_CREATE = 2;
	public static final int FUNC_RESTORE_HP = 3;
	public static final int FUNC_RESTORE_MP = 4;
	public static final int FUNC_RESTORE_EXP = 5;
	public static final int FUNC_SUPPORT = 6;
	public static final int FUNC_DECO_FRONTPLATEFORM = 7;
	public static final int FUNC_DECO_CURTAINS = 8;
	
	public class ClanHallFunction
	{
		private final int _type;
		private int _lvl;
		protected long[] _price;
		protected int _tempFee;
		private final long _rate;
		private long _endDate;
		protected boolean _inDebt;
		public boolean _cwh;
		
		public ClanHallFunction(int type, int lvl, long[] price, int tempLease, long rate, long time, boolean cwh)
		{
			_type = type;
			_lvl = lvl;
			_price = price;
			_tempFee = tempLease;
			_rate = rate;
			_endDate = time;
			initializeTask(cwh);
		}
		
		public int getType()
		{
			return _type;
		}
		
		public int getLvl()
		{
			return _lvl;
		}
		
		public long[] getPrice()
		{
			return _price;
		}
		
		public long getRate()
		{
			return _rate;
		}
		
		public long getEndTime()
		{
			return _endDate;
		}
		
		public void setLvl(int lvl)
		{
			_lvl = lvl;
		}
		
		public void setPrice(long[] price)
		{
			_price = price;
		}
		
		public void setEndTime(long time)
		{
			_endDate = time;
		}
		
		private void initializeTask(boolean cwh)
		{
			if (_isFree)
			{
				return;
			}
			final long currentTime = System.currentTimeMillis();
			if (_endDate > currentTime)
			{
				ThreadPoolManager.getInstance().schedule(new FunctionTask(cwh), _endDate - currentTime);
			}
			else
			{
				ThreadPoolManager.getInstance().schedule(new FunctionTask(cwh), 0);
			}
		}
		
		private class FunctionTask implements Runnable
		{
			public FunctionTask(boolean cwh)
			{
				_cwh = cwh;
			}
			
			@Override
			public void run()
			{
				try
				{
					if (_isFree)
					{
						return;
					}
					final var clan = ClanHolder.getInstance().getClan(getOwnerId());
					if (clan == null)
					{
						return;
					}
					
					if (clan.getWarehouse().getInventoryItemCount((int) _price[0], 0) >= _price[1] || !_cwh)
					{
						long price = _price[1];
						if (getEndTime() == -1)
						{
							price = _tempFee;
						}
						
						setEndTime(System.currentTimeMillis() + (getRate() * 86400000L));
						dbSave();
						if (_cwh)
						{
							clan.getWarehouse().destroyItemByItemId("CH_function_fee", (int) _price[0], price, null, null);
						}
						ThreadPoolManager.getInstance().schedule(new FunctionTask(true), (getRate() * 86400000L));
					}
					else
					{
						removeFunction(getType());
					}
				}
				catch (final Exception e)
				{
					_log.warn("", e);
				}
			}
		}
		
		public void dbSave()
		{
			Connection con = null;
			PreparedStatement statement = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("REPLACE INTO clanhall_functions (hall_id, type, lvl, lease, rate, endTime) VALUES (?,?,?,?,?,?)");
				statement.setInt(1, getId());
				statement.setInt(2, getType());
				statement.setInt(3, getLvl());
				statement.setString(4, "" + getPrice()[0] + "-" + getPrice()[1] + "");
				statement.setLong(5, getRate());
				statement.setLong(6, getEndTime());
				statement.execute();
			}
			catch (final Exception e)
			{
				_log.warn("Exception: ClanHall.updateFunctions(int type, int lvl, int lease, long rate, long time, boolean addNew): " + e.getMessage(), e);
			}
			finally
			{
				DbUtils.closeQuietly(con, statement);
			}
		}
	}
	
	public ClanHall(ClanHallTemplate template, StatsSet set)
	{
		_clanHallId = set.getInteger("id");
		_template = template;
		_ownerId = set.getInteger("ownerId");
		_functions = new ConcurrentHashMap<>();
		final var residentialSkills = SkillTreesParser.getInstance().getAvailableResidentialSkills(getId());
		for (final var s : residentialSkills)
		{
			final var sk = SkillsParser.getInstance().getInfo(s.getId(), s.getLvl());
			if (sk != null)
			{
				_residentialSkills.add(sk);
			}
		}
		
		if (_ownerId > 0)
		{
			final Clan clan = ClanHolder.getInstance().getClan(_ownerId);
			if (clan != null)
			{
				clan.setHideoutId(getId());
			}
			else
			{
				free();
			}
		}
	}
	
	public final int getId()
	{
		return _clanHallId;
	}
	
	public final int getOwnerId()
	{
		return _ownerId;
	}
	
	public final Clan getOwnerClan()
	{
		return ClanHolder.getInstance().getClan(getOwnerId());
	}
	
	public final ArrayList<DoorInstance> getDoors()
	{
		if (_doors == null)
		{
			_doors = new ArrayList<>();
		}
		return _doors;
	}
	
	public final DoorInstance getDoor(int doorId)
	{
		if (doorId <= 0)
		{
			return null;
		}
		for (final var door : getDoors())
		{
			if (door.getDoorId() == doorId)
			{
				return door;
			}
		}
		return null;
	}
	
	public ClanHallFunction getFunction(int type)
	{
		if (_functions.get(type) != null)
		{
			return _functions.get(type);
		}
		return null;
	}
	
	public void setZone(ClanHallZone zone)
	{
		_zone = zone;
	}
	
	public boolean checkIfInZone(int x, int y, int z)
	{
		return getZone().isInsideZone(x, y, z);
	}
	
	public ClanHallZone getZone()
	{
		return _zone;
	}
	
	public void free()
	{
		_ownerId = 0;
		_isFree = true;
		for (final var fc : _functions.keySet())
		{
			removeFunction(fc);
		}
		_functions.clear();
		updateDb();
	}
	
	public void setOwner(Clan clan)
	{
		if ((_ownerId > 0) || (clan == null))
		{
			final var oldOwner = ClanHolder.getInstance().getClan(getOwnerId());
			if (oldOwner != null)
			{
				if (!getResidentialSkills().isEmpty())
				{
					for (final var member : oldOwner.getOnlineMembers(0))
					{
						removeResidentialSkills(member);
						member.sendSkillList(false);
					}
				}
			}
			return;
		}
		_ownerId = clan.getId();
		_isFree = false;
		clan.setHideoutId(getId());
		if (!getResidentialSkills().isEmpty())
		{
			for (final var member : clan.getOnlineMembers(0))
			{
				giveResidentialSkills(member);
				member.sendSkillList(false);
			}
		}
		clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
		updateDb();
	}
	
	public void openCloseDoor(Player activeChar, int doorId, boolean open)
	{
		if ((activeChar != null) && (activeChar.getClanId() == getOwnerId()))
		{
			openCloseDoor(doorId, open);
		}
	}
	
	public void openCloseDoor(int doorId, boolean open)
	{
		openCloseDoor(getDoor(doorId), open);
	}
	
	public void openCloseDoor(DoorInstance door, boolean open)
	{
		if (door != null)
		{
			if (open)
			{
				door.openMe();
			}
			else
			{
				door.closeMe();
			}
		}
	}
	
	public void openCloseDoors(Player activeChar, boolean open)
	{
		if ((activeChar != null) && (activeChar.getClanId() == getOwnerId()))
		{
			openCloseDoors(open);
		}
	}
	
	public void openCloseDoors(boolean open)
	{
		for (final var door : getDoors())
		{
			if (door != null)
			{
				if (open)
				{
					door.openMe();
				}
				else
				{
					door.closeMe();
				}
			}
		}
	}
	
	public void banishForeigners()
	{
		if (_zone != null)
		{
			_zone.banishForeigners(getOwnerId());
		}
	}
	
	protected void loadFunctions()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM clanhall_functions WHERE hall_id = ?");
			statement.setInt(1, getId());
			rs = statement.executeQuery();
			while (rs.next())
			{
				final var lease = rs.getString("lease");
				long[] price = new long[]
				{
				        0L, 0L
				};
				final String[] infoPrice = lease.split("-");
				if (infoPrice.length > 1)
				{
					price = new long[]
					{
					        Long.parseLong(infoPrice[0]), Long.parseLong(infoPrice[1])
					};
				}
				_functions.put(rs.getInt("type"), new ClanHallFunction(rs.getInt("type"), rs.getInt("lvl"), price, 0, rs.getLong("rate"), rs.getLong("endTime"), true));
			}
		}
		catch (final Exception e)
		{
			_log.warn("Exception: ClanHall.loadFunctions(): " + e.getMessage(), e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rs);
		}
	}
	
	public void removeFunction(int functionType)
	{
		_functions.remove(functionType);
		
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM clanhall_functions WHERE hall_id=? AND type=?");
			statement.setInt(1, getId());
			statement.setInt(2, functionType);
			statement.execute();
		}
		catch (final Exception e)
		{
			_log.warn("Exception: ClanHall.removeFunctions(int functionType): " + e.getMessage(), e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public boolean updateFunctions(Player player, int type, int lvl, long[] price, long rate, boolean addNew)
	{
		if (player == null)
		{
			return false;
		}
		if (price[1] > 0)
		{
			if (!player.destroyItemByItemId("Consume", (int) price[0], price[1], null, true))
			{
				return false;
			}
		}
		if (addNew)
		{
			_functions.put(type, new ClanHallFunction(type, lvl, price, 0, rate, 0, false));
		}
		else
		{
			if ((lvl == 0) && (price[1] == 0))
			{
				removeFunction(type);
			}
			else
			{
				final long diffLease = price[1] - _functions.get(type).getPrice()[1];
				if (diffLease > 0)
				{
					_functions.remove(type);
					_functions.put(type, new ClanHallFunction(type, lvl, price, 0, rate, -1, false));
				}
				else
				{
					final var function = _functions.get(type);
					if (function != null)
					{
						function.setPrice(price);
						function.setLvl(lvl);
						function.dbSave();
					}
				}
			}
		}
		return true;
	}
	
	public int getGrade()
	{
		return 0;
	}
	
	public long getPaidUntil()
	{
		return 0;
	}
	
	public int getRentId()
	{
		return 0;
	}
	
	public long getRentPrice()
	{
		return 0;
	}
	
	public boolean isSiegableHall()
	{
		return false;
	}
	
	public boolean isFree()
	{
		return _isFree;
	}
	
	public abstract void updateDb();
	
	public List<Skill> getResidentialSkills()
	{
		return _residentialSkills;
	}
	
	public void giveResidentialSkills(Player player)
	{
		if (!_residentialSkills.isEmpty())
		{
			_residentialSkills.stream().filter(s -> (s != null)).forEach(s -> player.addSkill(s, false));
		}
	}
	
	public void removeResidentialSkills(Player player)
	{
		if (!_residentialSkills.isEmpty())
		{
			_residentialSkills.stream().filter(s -> (s != null)).forEach(s -> player.removeSkill(s, false, true));
		}
	}
	
	public ClanHallTemplate getTemplate()
	{
		return _template;
	}
}