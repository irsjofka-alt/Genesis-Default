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
package gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import l2e.commons.dbutils.DbUtils;
import gameserver.Config;
import gameserver.data.DocumentParser;
import gameserver.data.holder.ClanHolder;
import gameserver.database.DatabaseFactory;
import gameserver.model.Clan;
import gameserver.model.GameObject;
import gameserver.model.actor.templates.residences.clanhall.ClanHallTemplate;
import gameserver.model.actor.templates.residences.clanhall.FunctionTemplate;
import gameserver.model.base.FunctionType;
import gameserver.model.entity.ClanHall;
import gameserver.model.entity.clanhall.AuctionableHall;
import gameserver.model.stats.StatsSet;
import gameserver.model.zone.type.ClanHallZone;

public final class ClanHallManager extends DocumentParser
{
	private final Map<Integer, AuctionableHall> _clanHall = new ConcurrentHashMap<>();
	private final Map<Integer, AuctionableHall> _freeClanHall = new ConcurrentHashMap<>();
	private final Map<Integer, AuctionableHall> _allAuctionableClanHalls = new ConcurrentHashMap<>();
	private final Map<Integer, ClanHall> _allClanHalls = new ConcurrentHashMap<>();
	private final Map<Integer, ClanHallTemplate> _templates = new ConcurrentHashMap<>();
	private boolean _loaded = false;
	
	protected ClanHallManager()
	{
		load();
	}
	
	@Override
	public final void load()
	{
		_templates.clear();
		_clanHall.clear();
		_freeClanHall.clear();
		_allAuctionableClanHalls.clear();
		_allClanHalls.clear();
		parseDirectory("data/stats/residences/clanhall", false);
		loadDbInfo();
		info("Loaded: " + _templates.size() + " templates. " + _clanHall.size() + " occupy and " + _freeClanHall.size() + " free clan halls.");
	}
	
	@Override
	protected void reloadDocument()
	{
	}
	
	@Override
	protected void parseDocument()
	{
		for (var n = getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (var d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("clanhall".equalsIgnoreCase(d.getNodeName()))
					{
						var attrs = d.getAttributes();
						final var params = new StatsSet();
						final Map<FunctionType, Set<FunctionTemplate>> functions = new ConcurrentHashMap<>();
						final int clanhallId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
						params.set("id", clanhallId);
						for (final String lang : Config.MULTILANG_ALLOWED)
						{
							if (lang != null)
							{
								final String name = "name" + lang.substring(0, 1).toUpperCase() + lang.substring(1);
								params.set(name, attrs.getNamedItem(name) != null ? attrs.getNamedItem(name).getNodeValue() : attrs.getNamedItem("nameEn") != null ? attrs.getNamedItem("nameEn").getNodeValue() : "");
							}
						}
						params.set("siegeDate", attrs.getNamedItem("siegeDate") != null ? attrs.getNamedItem("siegeDate").getNodeValue() : "");
						params.set("siegeTime", attrs.getNamedItem("siegeTime") != null ? attrs.getNamedItem("siegeTime").getNodeValue() : "0");
						
						for (var cat = d.getFirstChild(); cat != null; cat = cat.getNextSibling())
						{
							if ("set".equalsIgnoreCase(cat.getNodeName()))
							{
								attrs = cat.getAttributes();
								final String name = attrs.getNamedItem("name").getNodeValue();
								final String value = attrs.getNamedItem("value").getNodeValue();
								params.set(name, value);
							}
							else if ("function".equalsIgnoreCase(cat.getNodeName()))
							{
								attrs = cat.getAttributes();
								final var set = new StatsSet();
								final var type = FunctionType.valueOf(attrs.getNamedItem("type").getNodeValue());
								for (var fun = cat.getFirstChild(); fun != null; fun = fun.getNextSibling())
								{
									if ("set".equalsIgnoreCase(fun.getNodeName()))
									{
										attrs = fun.getAttributes();
										final String name = attrs.getNamedItem("name").getNodeValue();
										final String value = attrs.getNamedItem("value").getNodeValue();
										set.set(name, value);
									}
									else if ("level".equalsIgnoreCase(fun.getNodeName()))
									{
										attrs = fun.getAttributes();
										final var statSet = set.clone();
										final int level = Integer.parseInt(attrs.getNamedItem("value").getNodeValue());
										for (var inf = fun.getFirstChild(); inf != null; inf = inf.getNextSibling())
										{
											if ("set".equalsIgnoreCase(inf.getNodeName()))
											{
												attrs = inf.getAttributes();
												final String name = attrs.getNamedItem("name").getNodeValue();
												final String value = attrs.getNamedItem("value").getNodeValue();
												statSet.set(name, value);
											}
										}
										
										if (!functions.containsKey(type))
										{
											functions.put(type, new LinkedHashSet<>());
										}
										functions.get(type).add(new FunctionTemplate(level, statSet));
									}
								}
							}
						}
						final var template = new ClanHallTemplate(clanhallId, params, functions);
						_templates.put(clanhallId, template);
					}
				}
			}
		}
	}
	
	private final void loadDbInfo()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM clanhall ORDER BY id");
			rset = statement.executeQuery();
			while (rset.next())
			{
				final var template = _templates.get(rset.getInt("id"));
				if (template == null)
				{
					continue;
				}
				final var set = template.getParams();
				set.set("ownerId", rset.getInt("ownerId"));
				set.set("paidUntil", rset.getLong("paidUntil"));
				set.set("paid", rset.getBoolean("paid"));
				
				final var ch = new AuctionableHall(template, set);
				_allAuctionableClanHalls.put(template.getId(), ch);
				_allClanHalls.put(template.getId(), ch);
				if (ch.getOwnerId() > 0)
				{
					_clanHall.put(template.getId(), ch);
					continue;
				}
				_freeClanHall.put(template.getId(), ch);
				
				final var auc = AuctionManager.getInstance().getAuction(template.getId());
				if ((auc == null) && (ch.getRentPrice() > 0))
				{
					AuctionManager.getInstance().initNPC(template.getId());
				}
			}
			_loaded = true;
		}
		catch (final Exception e)
		{
			warn("load error: " + e.getMessage(), e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}
	
	public boolean loaded()
	{
		return _loaded;
	}
	
	public final Map<Integer, ClanHallTemplate> getClanHallTemplates()
	{
		return _templates;
	}
	
	public ClanHallTemplate getClanHallTemplate(int id)
	{
		return _templates.get(id);
	}
	
	public final Map<Integer, ClanHall> getAllClanHalls()
	{
		return _allClanHalls;
	}
	
	public final Map<Integer, AuctionableHall> getFreeClanHalls()
	{
		return _freeClanHall;
	}
	
	public final Map<Integer, AuctionableHall> getClanHalls()
	{
		return _clanHall;
	}
	
	public final Map<Integer, AuctionableHall> getAllAuctionableClanHalls()
	{
		return _allAuctionableClanHalls;
	}
	
	public final void addClanHall(ClanHall hall)
	{
		_allClanHalls.put(hall.getId(), hall);
	}
	
	public final boolean isFree(int chId)
	{
		return _freeClanHall.containsKey(chId);
	}
	
	public final synchronized void setFree(int chId)
	{
		_freeClanHall.put(chId, _clanHall.get(chId));
		final var clan = ClanHolder.getInstance().getClan(_freeClanHall.get(chId).getOwnerId());
		if (clan != null)
		{
			clan.setHideoutId(0);
		}
		_freeClanHall.get(chId).free();
		_clanHall.remove(chId);
	}
	
	public final synchronized void setOwner(int chId, Clan clan)
	{
		if (clan == null)
		{
			return;
		}
		
		if (!_clanHall.containsKey(chId))
		{
			_clanHall.put(chId, _freeClanHall.get(chId));
			_freeClanHall.remove(chId);
		}
		else
		{
			_clanHall.get(chId).free();
		}
		clan.setHideoutId(chId);
		_clanHall.get(chId).setOwner(clan);
	}
	
	public final ClanHall getClanHallById(int clanHallId)
	{
		return _allClanHalls.get(clanHallId);
	}
	
	public final AuctionableHall getAuctionableHallById(int clanHallId)
	{
		return _allAuctionableClanHalls.get(clanHallId);
	}
	
	public final ClanHall getClanHall(int x, int y, int z)
	{
		for (final var temp : getAllClanHalls().values())
		{
			if (temp.checkIfInZone(x, y, z))
			{
				return temp;
			}
		}
		return null;
	}
	
	public final ClanHall getClanHall(GameObject activeObject)
	{
		return getClanHall(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}
	
	public final AuctionableHall getNearbyClanHall(int x, int y, int maxDist)
	{
		ClanHallZone zone = null;
		for (final var ch : _clanHall.entrySet())
		{
			zone = ch.getValue().getZone();
			if ((zone != null) && (zone.getDistanceToZone(x, y) < maxDist))
			{
				return ch.getValue();
			}
		}
		for (final var ch : _freeClanHall.entrySet())
		{
			zone = ch.getValue().getZone();
			if ((zone != null) && (zone.getDistanceToZone(x, y) < maxDist))
			{
				return ch.getValue();
			}
		}
		return null;
	}
	
	public final ClanHall getNearbyAbstractHall(int x, int y, int maxDist)
	{
		ClanHallZone zone = null;
		for (final var ch : _allClanHalls.entrySet())
		{
			zone = ch.getValue().getZone();
			if ((zone != null) && (zone.getDistanceToZone(x, y) < maxDist))
			{
				return ch.getValue();
			}
		}
		return null;
	}
	
	public final AuctionableHall getClanHallByOwner(Clan clan)
	{
		for (final var ch : _clanHall.entrySet())
		{
			if (clan.getId() == ch.getValue().getOwnerId())
			{
				return ch.getValue();
			}
		}
		return null;
	}
	
	public final ClanHall getAbstractHallByOwner(Clan clan)
	{
		for (final var ch : _clanHall.entrySet())
		{
			if (clan.getId() == ch.getValue().getOwnerId())
			{
				return ch.getValue();
			}
		}
		for (final var ch : CHSiegeManager.getInstance().getConquerableHalls().entrySet())
		{
			if (clan.getId() == ch.getValue().getOwnerId())
			{
				return ch.getValue();
			}
		}
		return null;
	}
	
	public static ClanHallManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ClanHallManager _instance = new ClanHallManager();
	}
}