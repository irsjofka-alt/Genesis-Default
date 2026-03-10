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
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import l2e.commons.dbutils.DbUtils;
import gameserver.Announcements;
import gameserver.Config;
import gameserver.ThreadPoolManager;
import gameserver.data.holder.ClanHolder;
import gameserver.data.parser.NpcsParser;
import gameserver.database.DatabaseFactory;
import gameserver.instancemanager.CHSiegeManager;
import gameserver.instancemanager.MapRegionManager;
import gameserver.model.Clan;
import gameserver.model.GameObjectsStorage;
import gameserver.model.Location;
import gameserver.model.SiegeClan;
import gameserver.model.SiegeClan.SiegeClanType;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.actor.templates.npc.NpcTemplate;
import gameserver.model.entity.Siegable;
import gameserver.model.quest.Quest;
import gameserver.model.spawn.Spawner;
import gameserver.network.NpcStringId;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.NpcSay;
import gameserver.network.serverpackets.SystemMessage;

public abstract class ClanHallSiegeEngine extends Quest implements Siegable
{
	protected final Logger _log = LogManager.getLogger(getClass());
	
	public static final int FORTRESS_RESSISTANCE = 21;
	public static final int DEVASTATED_CASTLE = 34;
	public static final int BANDIT_STRONGHOLD = 35;
	public static final int RAINBOW_SPRINGS = 62;
	public static final int BEAST_FARM = 63;
	public static final int FORTRESS_OF_DEAD = 64;
	
	private final Map<Integer, SiegeClan> _attackers = new ConcurrentHashMap<>();
	private List<Spawner> _guards;
	
	public SiegableHall _hall;
	public ScheduledFuture<?> _siegeTask;
	public boolean _missionAccomplished = false;
	
	public ClanHallSiegeEngine(final int hallId)
	{
		super(-1);
		
		_hall = CHSiegeManager.getInstance().getSiegableHall(hallId);
		_hall.setSiege(this);
		
		_siegeTask = ThreadPoolManager.getInstance().schedule(new PrepareOwner(), _hall.getNextSiegeTime() - System.currentTimeMillis() - 3600000);
		_log.info(_hall.getTemplate().getName(null) + " Siege: Will begin at " + new Date(getSiegeStartTime()));
		loadAttackers();
	}
	
	public void loadAttackers()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT attacker_id FROM clanhall_siege_attackers WHERE clanhall_id = ?");
			statement.setInt(1, _hall.getId());
			rset = statement.executeQuery();
			while (rset.next())
			{
				final int id = rset.getInt("attacker_id");
				final SiegeClan clan = new SiegeClan(id, SiegeClanType.ATTACKER);
				_attackers.put(id, clan);
			}
		}
		catch (final Exception _)
		{
			_log.warn(getName() + ": Could not load siege attackers!:");
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}
	
	public final void saveAttackers()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM clanhall_siege_attackers WHERE clanhall_id = ?");
			statement.setInt(1, _hall.getId());
			statement.execute();
			statement.close();
			
			if (getAttackers().size() > 0)
			{
				statement = con.prepareStatement("INSERT INTO clanhall_siege_attackers VALUES (?,?)");
				for (final SiegeClan clan : getAttackers().values())
				{
					statement.setInt(1, _hall.getId());
					statement.setInt(2, clan.getClanId());
					statement.execute();
					statement.clearParameters();
				}
			}
			if (Config.DEBUG)
			{
				_log.info(getName() + ": Sucessfully saved attackers down to database!");
			}
		}
		catch (final Exception _)
		{
			_log.warn(getName() + ": Couldnt save attacker list!");
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	public final void loadGuards()
	{
		if (_guards == null)
		{
			_guards = new ArrayList<>();
			Connection con = null;
			PreparedStatement statement = null;
			ResultSet rset = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("SELECT * FROM clanhall_siege_guards WHERE clanHallId = ?");
				statement.setInt(1, _hall.getId());
				rset = statement.executeQuery();
				while (rset.next())
				{
					final int npcId = rset.getInt("npcId");
					final NpcTemplate template = NpcsParser.getInstance().getTemplate(npcId);
					final Spawner spawn = new Spawner(template);
					spawn.setX(rset.getInt("x"));
					spawn.setY(rset.getInt("y"));
					spawn.setZ(rset.getInt("z"));
					spawn.setHeading(rset.getInt("heading"));
					spawn.setRespawnDelay(rset.getInt("respawnDelay"));
					spawn.setAmount(1);
					_guards.add(spawn);
				}
			}
			catch (final Exception _)
			{
				_log.warn(getName() + ": Couldnt load siege guards!:");
			}
			finally
			{
				DbUtils.closeQuietly(con, statement, rset);
			}
		}
	}
	
	private final void spawnSiegeGuards()
	{
		for (final Spawner guard : _guards)
		{
			if (guard != null)
			{
				guard.init();
			}
		}
	}
	
	private final void unSpawnSiegeGuards()
	{
		if ((_guards != null) && (_guards.size() > 0))
		{
			for (final Spawner guard : _guards)
			{
				if (guard != null)
				{
					guard.stopRespawn();
					if (guard.getLastSpawn() != null)
					{
						guard.getLastSpawn().deleteMe();
					}
				}
			}
		}
	}
	
	@Override
	public List<Npc> getFlag(Clan clan)
	{
		List<Npc> result = null;
		final var sClan = getAttackerClan(clan);
		if (sClan != null)
		{
			result = sClan.getFlag();
		}
		return result;
	}
	
	public final Map<Integer, SiegeClan> getAttackers()
	{
		return _attackers;
	}
	
	@Override
	public boolean checkIsAttacker(Clan clan)
	{
		if (clan == null)
		{
			return false;
		}
		return _attackers.containsKey(clan.getId());
	}
	
	@Override
	public boolean checkIsDefender(Clan clan)
	{
		return false;
	}
	
	@Override
	public SiegeClan getAttackerClan(int clanId)
	{
		return _attackers.get(clanId);
	}
	
	@Override
	public SiegeClan getAttackerClan(Clan clan)
	{
		return getAttackerClan(clan.getId());
	}
	
	@Override
	public List<SiegeClan> getAttackerClans()
	{
		return new ArrayList<>(_attackers.values());
	}
	
	@Override
	public List<Player> getAttackersInZone()
	{
		final List<Player> attackers = new ArrayList<>();
		for (final var pc : _hall.getSiegeZone().getPlayersInside())
		{
			final var clan = pc.getClan();
			if ((clan != null) && getAttackers().containsKey(clan.getId()))
			{
				attackers.add(pc);
			}
		}
		return attackers;
	}
	
	@Override
	public SiegeClan getDefenderClan(int clanId)
	{
		return null;
	}
	
	@Override
	public SiegeClan getDefenderClan(Clan clan)
	{
		return null;
	}
	
	@Override
	public List<SiegeClan> getDefenderClans()
	{
		return null;
	}
	
	public void prepareOwner()
	{
		if (_hall.getOwnerId() > 0)
		{
			final var clan = new SiegeClan(_hall.getOwnerId(), SiegeClanType.ATTACKER);
			getAttackers().put(clan.getClanId(), new SiegeClan(clan.getClanId(), SiegeClanType.ATTACKER));
		}
		_hall.free();
		_hall.banishForeigners();
		final var msg = SystemMessage.getSystemMessage(SystemMessageId.REGISTRATION_TERM_FOR_S1_ENDED);
		msg.addString(getName());
		Announcements.getInstance().announceToAll(msg);
		_hall.updateSiegeStatus(SiegeStatus.WAITING_BATTLE);
		
		_siegeTask = ThreadPoolManager.getInstance().schedule(new SiegeStarts(), 3600000);
	}
	
	@Override
	public void startSiege()
	{
		if ((getAttackers().size() < 1) && (_hall.getId() != 21))
		{
			onSiegeEnds();
			getAttackers().clear();
			_hall.updateNextSiege();
			_siegeTask = ThreadPoolManager.getInstance().schedule(new PrepareOwner(), (_hall.getNextSiegeTime() - System.currentTimeMillis()));
			_hall.updateSiegeStatus(SiegeStatus.WAITING_BATTLE);
			final var sm = SystemMessage.getSystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST);
			sm.addString(_hall.getTemplate().getName(null));
			Announcements.getInstance().announceToAll(sm);
			return;
		}
		_hall.spawnDoor();
		loadGuards();
		spawnSiegeGuards();
		_hall.updateSiegeZone(true);
		
		final byte state = 1;
		for (final var sClan : getAttackerClans())
		{
			final var clan = ClanHolder.getInstance().getClan(sClan.getClanId());
			if (clan == null)
			{
				continue;
			}

			for (final var pc : clan.getOnlineMembers(0))
			{
				if (pc != null)
				{
					pc.setSiegeState(state);
					pc.broadcastUserInfo(true);
					pc.setIsInHideoutSiege(true);
				}
			}
		}
		_hall.updateSiegeStatus(SiegeStatus.RUNNING);
		onSiegeStarts();
		_siegeTask = ThreadPoolManager.getInstance().schedule(new SiegeEnds(), (_hall.getSiegeTime() * 60000L));
	}
	
	@Override
	public void endSiege()
	{
		final var end = SystemMessage.getSystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_ENDED);
		end.addString(_hall.getTemplate().getName(null));
		Announcements.getInstance().announceToAll(end);
		
		final var winner = getWinner();
		SystemMessage finalMsg = null;
		if (_missionAccomplished && (winner != null))
		{
			_hall.setOwner(winner);
			winner.setHideoutId(_hall.getId());
			finalMsg = SystemMessage.getSystemMessage(SystemMessageId.CLAN_S1_VICTORIOUS_OVER_S2_S_SIEGE);
			finalMsg.addString(winner.getName());
			finalMsg.addString(_hall.getTemplate().getName(null));
			Announcements.getInstance().announceToAll(finalMsg);
		}
		else
		{
			finalMsg = SystemMessage.getSystemMessage(SystemMessageId.SIEGE_S1_DRAW);
			finalMsg.addString(_hall.getTemplate().getName(null));
			Announcements.getInstance().announceToAll(finalMsg);
		}
		_missionAccomplished = false;
		
		_hall.updateSiegeZone(false);
		_hall.updateNextSiege();
		_hall.spawnDoor(false);
		_hall.banishForeigners();
		
		final byte state = 0;
		for (final var sClan : getAttackerClans())
		{
			final var clan = ClanHolder.getInstance().getClan(sClan.getClanId());
			if (clan == null)
			{
				continue;
			}
			
			for (final var player : clan.getOnlineMembers(0))
			{
				player.setSiegeState(state);
				player.broadcastUserInfo(true);
				player.setIsInHideoutSiege(false);
			}
		}
		
		for (final var player : _hall.getSiegeZone().getPlayersInside())
		{
			if ((player != null) && !player.isDead())
			{
				player.startPvPFlag();
			}
		}
		
		getAttackers().clear();
		
		onSiegeEnds();
		
		_siegeTask = ThreadPoolManager.getInstance().schedule(new PrepareOwner(), _hall.getNextSiegeTime() - System.currentTimeMillis() - 3600000);
		if (Config.DEBUG)
		{
			_log.info(_hall.getTemplate().getName(null) + " Siege: Will begin at " + new Date(_hall.getNextSiegeTime()));
		}
		
		_hall.updateSiegeStatus(SiegeStatus.REGISTERING);
		unSpawnSiegeGuards();
	}
	
	@Override
	public void updateSiege()
	{
		cancelSiegeTask();
		_siegeTask = ThreadPoolManager.getInstance().schedule(new PrepareOwner(), _hall.getNextSiegeTime() - 3600000);
		if (Config.DEBUG)
		{
			_log.info(_hall.getTemplate().getName(null) + " Siege: Will begin at " + new Date(_hall.getNextSiegeTime()));
		}
	}
	
	public void cancelSiegeTask()
	{
		if (_siegeTask != null)
		{
			_siegeTask.cancel(false);
		}
	}
	
	@Override
	public long getSiegeStartTime()
	{
		return _hall.getNextSiegeTime();
	}
	
	@Override
	public boolean giveFame()
	{
		return Config.CHS_ENABLE_FAME;
	}
	
	@Override
	public int getFameAmount()
	{
		return Config.CHS_FAME_AMOUNT;
	}
	
	@Override
	public int getFameFrequency()
	{
		return Config.CHS_FAME_FREQUENCY;
	}
	
	public final void broadcastNpcSay(final Npc npc, final int type, final NpcStringId messageId)
	{
		final var npcSay = new NpcSay(npc.getObjectId(), type, npc.getId(), messageId);
		final int sourceRegion = MapRegionManager.getInstance().getMapRegionLocId(npc);
		for (final var pc : GameObjectsStorage.getPlayers())
		{
			if ((pc != null) && (MapRegionManager.getInstance().getMapRegionLocId(pc) == sourceRegion))
			{
				pc.sendPacket(npcSay);
			}
		}
	}
	
	public Location getInnerSpawnLoc(Player player)
	{
		return null;
	}
	
	public boolean canPlantFlag()
	{
		return true;
	}
	
	public boolean doorIsAutoAttackable()
	{
		return true;
	}
	
	public void onSiegeStarts()
	{
	}
	
	public void onSiegeEnds()
	{
	}
	
	public abstract Clan getWinner();
	
	public class PrepareOwner implements Runnable
	{
		@Override
		public void run()
		{
			prepareOwner();
		}
	}
	
	public class SiegeStarts implements Runnable
	{
		@Override
		public void run()
		{
			startSiege();
		}
	}
	
	public class SiegeEnds implements Runnable
	{
		@Override
		public void run()
		{
			endSiege();
		}
	}
}