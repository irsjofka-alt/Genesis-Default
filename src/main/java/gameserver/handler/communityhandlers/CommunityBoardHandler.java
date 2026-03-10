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
package gameserver.handler.communityhandlers;

import java.util.HashMap;
import java.util.Map;

import l2e.commons.log.LoggerObject;
import gameserver.Config;
import gameserver.handler.communityhandlers.impl.CommunityAcademy;
import gameserver.handler.communityhandlers.impl.CommunityAuction;
import gameserver.handler.communityhandlers.impl.CommunityBalancer;
import gameserver.handler.communityhandlers.impl.CommunityBalancerSkill;
import gameserver.handler.communityhandlers.impl.CommunityBuffer;
import gameserver.handler.communityhandlers.impl.CommunityCertification;
import gameserver.handler.communityhandlers.impl.CommunityClan;
import gameserver.handler.communityhandlers.impl.CommunityClassMaster;
import gameserver.handler.communityhandlers.impl.CommunityEvents;
import gameserver.handler.communityhandlers.impl.CommunityForge;
import gameserver.handler.communityhandlers.impl.CommunityFriend;
import gameserver.handler.communityhandlers.impl.CommunityGeneral;
import gameserver.handler.communityhandlers.impl.CommunityLink;
import gameserver.handler.communityhandlers.impl.CommunityNpcCalc;
import gameserver.handler.communityhandlers.impl.CommunityPunishment;
import gameserver.handler.communityhandlers.impl.CommunityRaidBoss;
import gameserver.handler.communityhandlers.impl.CommunityRanking;
import gameserver.handler.communityhandlers.impl.CommunityServices;
import gameserver.handler.communityhandlers.impl.CommunityTeleport;
import gameserver.handler.communityhandlers.impl.CommunityTopic;
import gameserver.model.entity.auction.AuctionsManager;

public class CommunityBoardHandler extends LoggerObject
{
	private final Map<String, ICommunityBoardHandler> _handlers;

	private CommunityBoardHandler()
	{
		_handlers = new HashMap<>();
		
		registerHandler(new CommunityAcademy());
		registerHandler(new CommunityGeneral());
		registerHandler(new CommunityForge());
		registerHandler(new CommunityRaidBoss());
		registerHandler(new CommunityBuffer());
		registerHandler(new CommunityClan());
		registerHandler(new CommunityClassMaster());
		registerHandler(new CommunityEvents());
		registerHandler(new CommunityFriend());
		registerHandler(new CommunityLink());
		registerHandler(new CommunityServices());
		registerHandler(new CommunityRanking());
		registerHandler(new CommunityTeleport());
		registerHandler(new CommunityTopic());
		registerHandler(new CommunityAuction());
		AuctionsManager.getInstance();
		registerHandler(new CommunityBalancer());
		registerHandler(new CommunityBalancerSkill());
		registerHandler(new CommunityCertification());
		registerHandler(new CommunityNpcCalc());
		registerHandler(new CommunityPunishment());
		
		info("Loaded " + _handlers.size() + " CommunityBoardHandlers.");
	}
	
	public void registerHandler(ICommunityBoardHandler commHandler)
	{
		for (final String bypass : commHandler.getBypassCommands())
		{
			if (_handlers.containsKey(bypass))
			{
				info("dublicate bypass registered! First handler: " + _handlers.get(bypass).getClass().getSimpleName() + " second: " + commHandler.getClass().getSimpleName());
				_handlers.remove(bypass);
			}
			_handlers.put(bypass, commHandler);
		}
	}
	
	public ICommunityBoardHandler getHandler(String bypass)
	{
		if (!Config.ALLOW_COMMUNITY || _handlers.isEmpty())
		{
			return null;
		}
		
		if (Config.DISABLE_COMMUNITY_BYPASSES.contains(bypass))
		{
			return null;
		}

		for (final Map.Entry<String, ICommunityBoardHandler> entry : _handlers.entrySet())
		{
			if (bypass.contains(entry.getKey()))
			{
				return entry.getValue();
			}
		}
		return null;
	}
	
	public int size()
	{
		return _handlers.size();
	}
	
	public static CommunityBoardHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final CommunityBoardHandler _instance = new CommunityBoardHandler();
	}
}