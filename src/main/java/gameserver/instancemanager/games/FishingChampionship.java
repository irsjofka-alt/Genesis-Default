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
package gameserver.instancemanager.games;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import l2e.commons.log.LoggerObject;
import l2e.commons.util.Rnd;
import gameserver.Config;
import gameserver.ThreadPoolManager;
import gameserver.database.DatabaseFactory;
import gameserver.instancemanager.ServerVariables;
import gameserver.model.actor.Player;
import gameserver.model.strings.server.ServerMessage;
import gameserver.model.strings.server.ServerStorage;
import gameserver.network.serverpackets.NpcHtmlMessage;
import gameserver.utils.Util;

/**
 * Updated by LordWinter
 */
public class FishingChampionship extends LoggerObject
{
	private long _enddate = 0;
	
	private final List<String> _playersName = new ArrayList<>();
	private final List<String> _winPlayers = new ArrayList<>();
	private final List<Double> _fishLength = new ArrayList<>();
	private final List<Double> _winFishLength = new ArrayList<>();
	private final List<Fisher> _tmpPlayer = new ArrayList<>();
	private final List<Fisher> _winners = new ArrayList<>();
	private double _minFishLength = 0;
	protected long _needRefresh = 0;

	public FishingChampionship()
	{
		restoreData();
		refreshWinResult();
		setNewMin();
		if (_enddate <= System.currentTimeMillis())
		{
			_enddate = System.currentTimeMillis();
			new FinishTask().run();
		}
		else
		{
			ThreadPoolManager.getInstance().schedule(new FinishTask(), _enddate - System.currentTimeMillis());
		}
	}

	private class Fisher
	{
		public double _length = 0;
		public String _name;
		public int _rewarded = 0;
	}

	private class FinishTask implements Runnable
	{
		@Override
		public void run()
		{
			_winners.clear();
			for (final Fisher fisher : _tmpPlayer)
			{
				fisher._rewarded = 1;
				_winners.add(fisher);
			}
			_tmpPlayer.clear();
			refreshWinResult();
			setEndOfChamp();
			shutdown();
		}
	}

	private void setEndOfChamp()
	{
		final var finishtime = Calendar.getInstance();
		finishtime.setTimeInMillis(_enddate);
		finishtime.set(Calendar.MINUTE, 0);
		finishtime.set(Calendar.SECOND, 0);
		finishtime.add(Calendar.DAY_OF_MONTH, 6);
		finishtime.set(Calendar.DAY_OF_WEEK, 3);
		finishtime.set(Calendar.HOUR_OF_DAY, 19);
		_enddate = finishtime.getTimeInMillis();
		ServerVariables.set("fish_champion", _enddate);
		info("Period ends at " + new Date(_enddate));
	}

	private void restoreData()
	{
		_enddate = ServerVariables.getLong("fish_champion", 0);
		try (
		    var con = DatabaseFactory.getInstance().getConnection())
		{
			final var statement = con.prepareStatement("SELECT PlayerName,fishLength,rewarded FROM fishing_championship");
			final ResultSet rs = statement.executeQuery();
			while (rs.next())
			{
				final int rewarded = rs.getInt("rewarded");
				Fisher fisher;
				if (rewarded == 0)
				{
					fisher = new Fisher();
					fisher._name = rs.getString("PlayerName");
					fisher._length = rs.getFloat("fishLength");
					_tmpPlayer.add(fisher);
				}
				if (rewarded > 0)
				{
					fisher = new Fisher();
					fisher._name = rs.getString("PlayerName");
					fisher._length = rs.getFloat("fishLength");
					fisher._rewarded = rewarded;
					_winners.add(fisher);
				}
			}
			rs.close();
			statement.close();
		}
		catch (final Exception e)
		{
			warn("Can't get fishing championship info: " + e.getMessage(), e);
		}
	}

	public synchronized void newFish(Player player, int lureId)
	{
		double len = Rnd.get(60, 89) + (Rnd.get(0, 1000) / 1000.);
		if ((lureId >= 8484) && (lureId <= 8486))
		{
			len += Rnd.get(0, 3000) / 1000.;
		}
		
		if (_tmpPlayer.size() < 5)
		{
			for (int i = 0; i < _tmpPlayer.size(); i++)
			{
				if (_tmpPlayer.get(i)._name.equalsIgnoreCase(player.getName(null)))
				{
					if (_tmpPlayer.get(i)._length < len)
					{
						_tmpPlayer.get(i)._length = len;
						player.sendMessage((new ServerMessage("FishingChampionship.IMPROVED_RESULT", player.getLang())).toString());
						setNewMin();
					}
					return;
				}
			}
			final var newFisher = new Fisher();
			newFisher._name = player.getName(null);
			newFisher._length = len;
			_tmpPlayer.add(newFisher);
			player.sendMessage((new ServerMessage("FishingChampionship.GOT_TO_LIST", player.getLang())).toString());
			setNewMin();
		}
		else
		{
			if (_minFishLength >= len)
			{
				return;
			}
			for (int i = 0; i < _tmpPlayer.size(); i++)
			{
				if (_tmpPlayer.get(i)._name.equalsIgnoreCase(player.getName(null)))
				{
					if (_tmpPlayer.get(i)._length < len)
					{
						_tmpPlayer.get(i)._length = len;
						player.sendMessage((new ServerMessage("FishingChampionship.IMPROVED_RESULT", player.getLang())).toString());
						setNewMin();
					}
					return;
				}
			}
			Fisher minFisher = null;
			double minLen = 99999;
			for (final var a_tmpPlayer : _tmpPlayer)
			{
				if (a_tmpPlayer._length < minLen)
				{
					minFisher = a_tmpPlayer;
					minLen = minFisher._length;
				}
			}
			_tmpPlayer.remove(minFisher);
			final var newFisher = new Fisher();
			newFisher._name = player.getName(null);
			newFisher._length = len;
			_tmpPlayer.add(newFisher);
			player.sendMessage((new ServerMessage("FishingChampionship.GOT_TO_LIST", player.getLang())).toString());
			setNewMin();
		}
	}

	private void setNewMin()
	{
		double minLen = 99999;
		for (final Fisher a_tmpPlayer : _tmpPlayer)
		{
			if (a_tmpPlayer._length < minLen)
			{
				minLen = a_tmpPlayer._length;
			}
		}
		_minFishLength = minLen;
	}

	public int getTimeRemaining()
	{
		return (int) ((_enddate - System.currentTimeMillis()) / 1000L);
	}

	public String getWinnerName(Player player, int par)
	{
		if (_winPlayers.size() >= par)
		{
			return _winPlayers.get(par - 1);
		}
		return "" + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.NO") + "";
	}
	
	public String getCurrentName(Player player, int par)
	{
		if (_playersName.size() >= par)
		{
			return _playersName.get(par - 1);
		}
		return "" + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.NO") + "";
	}
	
	public int getCurrentFishLength(int par)
	{
		if (_fishLength.size() >= par)
		{
			return (int) Math.round(_fishLength.get(par - 1));
		}
		return 0;
	}

	public int getFishLength(int par)
	{
		if (_winFishLength.size() >= par)
		{
			return (int) Math.round(_winFishLength.get(par - 1));
		}
		return 0;
	}

	public void getReward(Player player)
	{
		final var html = new NpcHtmlMessage(player.getObjectId());
		String str = "<html><head><title>" + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.ROYAL_TOURNAMENT") + "</title></head>";
		str = str + "" + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.ACCEPT_CONGRATULATIONS") + "<br>";
		str = str + "" + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.HERE_YOUR_PRIZE") + "<br>";
		str = str + "" + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.GOOD_LUCK") + "";
		str = str + "</body></html>";
		html.setHtml(player, str);
		player.sendPacket(html);
		for (final var fisher : _winners)
		{
			if (fisher._name.equalsIgnoreCase(player.getName(null)) && (fisher._rewarded != 2))
			{
				String rewards = null;
				for (int x = 0; x < _winPlayers.size(); x++)
				{
					if (_winPlayers.get(x).equalsIgnoreCase(player.getName(null)))
					{
						rewards = Config.FISHING_REWARDS.get(x + 1);
					}
				}
				
				if (rewards != null)
				{
					int itemId = 0;
					long amount = 0;
					final String[] price = rewards.split(":");
					if (price != null && price.length == 2)
					{
						itemId = Integer.parseInt(price[0]);
						amount = Long.parseLong(price[1]);
					}
					
					if (itemId != 0)
					{
						fisher._rewarded = 2;
						player.addItem("reward", itemId, amount, player, true);
					}
				}
			}
		}
	}
	
	public void showMidResult(Player player)
	{
		if (refreshResult())
		{
			final var html = new NpcHtmlMessage(player.getObjectId());
			
			String str = "<html><head><title>" + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.ROYAL_TOURNAMENT") + "</title></head>";
			str = str + "" + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.NOW_PASS_COMPETITIONS") + "<br><br>";
			str = str + "" + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.UPON_CPMPETITIONS") + "<br>";
			str = str + "<table width=280 border=0 bgcolor=\"000000\"><tr><td width=70 align=center>" + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.PLACES") + "</td><td width=110 align=center>" + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.FISHERMAN") + "</td><td width=80 align=center>" + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.LENGTH") + "</td></tr></table><table width=280>";
			for (int x = 1; x <= 5; x++)
			{
				str = str + "<tr><td width=70 align=center>" + x + " " + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.PLACES") + ":</td>";
				str = str + "<td width=110 align=center>" + getCurrentName(player, x) + "</td>";
				str = str + "<td width=80 align=center>" + getCurrentFishLength(x) + "</td></tr>";
			}
			str = str + "<td width=80 align=center>0</td></tr></table><br>";
			str = str + "" + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.PRIZES_LIST") + "<br><table width=280 border=0 bgcolor=\"000000\"><tr><td width=70 align=center>" + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.PLACES") + "</td><td width=110 align=center>" + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.PRIZE") + "</td><td width=80 align=center>" + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.AMOUNT") + "</td></tr></table><table width=280>";
			for (int i = 1; i <= 5; i++)
			{
				final var rewards = Config.FISHING_REWARDS.get(i);
				if (rewards != null)
				{
					int itemId = 0;
					long amount = 0;
					final String[] price = rewards.split(":");
					if (price != null && price.length == 2)
					{
						itemId = Integer.parseInt(price[0]);
						amount = Long.parseLong(price[1]);
					}
					
					if (itemId != 0)
					{
						str = str + "<tr><td width=70 align=center>" + i + " " + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.PLACES") + ":</td><td width=110 align=center>" + Util.getItemName(player, itemId) + "</td><td width=80 align=center>" + amount + "</td></tr>";
					}
				}
			}
			str = str + "</table></body></html>";
			html.setHtml(player, str);
			player.sendPacket(html);
		}
	}

	public void shutdown()
	{
		PreparedStatement statement;
		try (
		    var con = DatabaseFactory.getInstance().getConnection())
		{
			statement = con.prepareStatement("DELETE FROM fishing_championship");
			statement.execute();
			statement.close();

			for (final var fisher : _winners)
			{
				statement = con.prepareStatement("INSERT INTO fishing_championship(PlayerName,fishLength,rewarded) VALUES (?,?,?)");
				statement.setString(1, fisher._name);
				statement.setDouble(2, fisher._length);
				statement.setInt(3, fisher._rewarded);
				statement.execute();
				statement.close();
			}
			for (final var fisher : _tmpPlayer)
			{
				statement = con.prepareStatement("INSERT INTO fishing_championship(PlayerName,fishLength,rewarded) VALUES (?,?,?)");
				statement.setString(1, fisher._name);
				statement.setDouble(2, fisher._length);
				statement.setInt(3, 0);
				statement.execute();
				statement.close();
			}
		}
		catch (final Exception e)
		{
			warn("Can't update player vitality: " + e.getMessage(), e);
		}
	}
	
	private synchronized boolean refreshResult()
	{
		if (_needRefresh > System.currentTimeMillis())
		{
			return true;
		}
		_needRefresh = System.currentTimeMillis() + 60000L;
		_playersName.clear();
		_fishLength.clear();
		
		Fisher fisher1;
		Fisher fisher2;
		
		for (int x = 0; x <= (_tmpPlayer.size() - 1); x++)
		{
			for (int y = 0; y <= (_tmpPlayer.size() - 2); y++)
			{
				fisher1 = _tmpPlayer.get(y);
				fisher2 = _tmpPlayer.get(y + 1);
				if (fisher1._length < fisher2._length)
				{
					_tmpPlayer.set(y, fisher2);
					_tmpPlayer.set(y + 1, fisher1);
				}
			}
		}
		
		for (int x = 0; x <= (_tmpPlayer.size() - 1); x++)
		{
			_playersName.add(_tmpPlayer.get(x)._name);
			_fishLength.add(_tmpPlayer.get(x)._length);
		}
		return true;
	}

	private void refreshWinResult()
	{
		_winPlayers.clear();
		_winFishLength.clear();
		Fisher fisher1, fisher2;
		for (int x = 0; x <= (_winners.size() - 1); x++)
		{
			for (int y = 0; y <= (_winners.size() - 2); y++)
			{
				fisher1 = _winners.get(y);
				fisher2 = _winners.get(y + 1);
				if (fisher1._length < fisher2._length)
				{
					_winners.set(y, fisher2);
					_winners.set(y + 1, fisher1);
				}
			}
		}

		for (int i = 0; i <= (_winners.size() - 1); i++)
		{
			_winPlayers.add(_winners.get(i)._name);
			_winFishLength.add(_winners.get(i)._length);
		}
	}
	
	public static final FishingChampionship getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final FishingChampionship _instance = new FishingChampionship();
	}
}