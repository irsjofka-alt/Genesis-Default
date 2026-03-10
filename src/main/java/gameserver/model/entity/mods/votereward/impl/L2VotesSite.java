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
package gameserver.model.entity.mods.votereward.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import l2e.commons.collections.MultiValueSet;
import gameserver.model.actor.Player;
import gameserver.model.entity.mods.votereward.VoteRewardRecord;
import gameserver.model.entity.mods.votereward.VoteRewardSite;

public class L2VotesSite extends VoteRewardSite
{
	private final String _apiKey;
	private final long _siteDelay;
	private final long _voteDelay;
	
	public L2VotesSite(MultiValueSet<String> parameters)
	{
		super(parameters);
		
		_apiKey = parameters.getString("api_key");
		_siteDelay = parameters.getInteger("vote_delay", 12);
		_voteDelay = TimeUnit.HOURS.toMillis(_siteDelay);
	}
	
	@Override
	public boolean isEnabled()
	{
		if (StringUtils.isEmpty(_apiKey))
		{
			return false;
		}
		return super.isEnabled();
	}
	
	@Override
	public boolean tryGiveRewards(Player player)
	{
		if (!getApiResponse(player))
		{
			return false;
		}
		
		final VoteRewardRecord record = getRecord(player);
		
		final long lastVoteTime = (record.getLastVoteTime() * 1000L);
		
		final long nextVoteTime = lastVoteTime + _voteDelay;
		if (System.currentTimeMillis() < nextVoteTime)
		{
			return false;
		}
		
		record.onReceiveReward(1, System.currentTimeMillis());
		giveRewards(player, 1);
		return true;
	}
	
	private boolean getApiResponse(Player player)
	{
		try
		{
			final var obj = new URL("https://l2votes.com/api.php?apiKey=%s&ip=%s".formatted(_apiKey, player.getIPAddress()));
			final var con = obj.openConnection();
			con.addRequestProperty("Host", obj.getHost());
			con.addRequestProperty("Accept", "*/*");
			con.addRequestProperty("Connection", "close");
			con.addRequestProperty("User-Agent", "Eternity");
			con.setConnectTimeout(5000);
			
			InputStream stream = null;
			InputStreamReader reader = null;
			try
			{
				stream = con.getInputStream();
				final var sb = new StringBuilder();
				final var array = new char[64];
				reader = new InputStreamReader(stream);
				int i = -1;
				while (((i = reader.read(array)) > 0) && (sb.length() < 1024))
				{
					sb.append(array, 0, i);
				}
				final var str = sb.toString().toLowerCase();
				if (str.indexOf("\"status\":\"1\"") > 0)
				{
					return true;
				}
				return false;
			}
			finally
			{
				if (reader != null)
				{
					try
					{
						reader.close();
					}
					catch (final IOException _)
					{
					}
				}
				if (stream != null)
				{
					try
					{
						stream.close();
					}
					catch (final IOException _)
					{
					}
				}
			}
		}
		catch (final Exception e)
		{
			warn("Check request failed", e);
		}
		return false;
	}
	
	@Override
	public long getVoteDelay()
	{
		return _voteDelay;
	}
}