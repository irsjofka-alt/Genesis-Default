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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.napile.primitive.pair.IntIntPair;

import l2e.commons.collections.MultiValueSet;
import gameserver.model.GameObjectsStorage;
import gameserver.model.actor.Player;
import gameserver.model.entity.mods.votereward.VoteRewardRecord;
import gameserver.model.entity.mods.votereward.VoteRewardSite;

public abstract class AbstractAutoRewardSite extends VoteRewardSite
{
	private boolean _locked = false;
	
	public AbstractAutoRewardSite(MultiValueSet<String> parameters)
	{
		super(parameters);
	}
	
	@Override
	public final void run()
	{
		if (_locked)
		{
			return;
		}
		_locked = true;
		try
		{
			final Map<String, List<IntIntPair>> votesCache = new HashMap<>();
			parseVotes(votesCache);
			
			for (final Player player : GameObjectsStorage.getPlayers())
			{
				if (player.isInOfflineMode())
				{
					continue;
				}
				
				final List<IntIntPair> voteInfos = votesCache.get(getIdentifier(player).toLowerCase());
				if (voteInfos == null)
				{
					continue;
				}
				
				int availableVotes = 0;
				
				final VoteRewardRecord record = getRecord(player);
				for (final IntIntPair info : voteInfos)
				{
					if (info.getKey() > record.getLastVoteTime())
					{
						availableVotes += info.getValue();
					}
				}
				
				if (availableVotes > 0)
				{
					final long lastVoteTime = (record.getLastVoteTime() * 1000L);
					final long nextVoteTime = lastVoteTime + TimeUnit.HOURS.toMillis(12);
					if (System.currentTimeMillis() < nextVoteTime)
					{
						continue;
					}
					record.onReceiveReward(availableVotes, System.currentTimeMillis());
					giveRewards(player, availableVotes);
				}
			}
		}
		finally
		{
			_locked = false;
		}
	}
	
	protected String getIdentifier(Player player)
	{
		return player.getName(null);
	}
	
	protected abstract void parseVotes(Map<String, List<IntIntPair>> votesCache);
}