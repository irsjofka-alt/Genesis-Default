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
package gameserver.handler.targethandlers;

import java.util.HashMap;
import java.util.Map;

import l2e.commons.log.LoggerObject;
import gameserver.handler.targethandlers.impl.AllyAll;
import gameserver.handler.targethandlers.impl.Area;
import gameserver.handler.targethandlers.impl.AreaCorpseMob;
import gameserver.handler.targethandlers.impl.AreaFriendly;
import gameserver.handler.targethandlers.impl.AreaSummon;
import gameserver.handler.targethandlers.impl.Aura;
import gameserver.handler.targethandlers.impl.AuraCorpseMob;
import gameserver.handler.targethandlers.impl.AuraDeadMob;
import gameserver.handler.targethandlers.impl.AuraDoor;
import gameserver.handler.targethandlers.impl.AuraFriendly;
import gameserver.handler.targethandlers.impl.AuraFriendlySummon;
import gameserver.handler.targethandlers.impl.AuraMob;
import gameserver.handler.targethandlers.impl.AuraUndeadEnemy;
import gameserver.handler.targethandlers.impl.BehindArea;
import gameserver.handler.targethandlers.impl.BehindAura;
import gameserver.handler.targethandlers.impl.ClanAll;
import gameserver.handler.targethandlers.impl.ClanMember;
import gameserver.handler.targethandlers.impl.CommandChannel;
import gameserver.handler.targethandlers.impl.CorpseAlly;
import gameserver.handler.targethandlers.impl.CorpseClan;
import gameserver.handler.targethandlers.impl.CorpseFriendly;
import gameserver.handler.targethandlers.impl.CorpseMob;
import gameserver.handler.targethandlers.impl.CorpsePet;
import gameserver.handler.targethandlers.impl.CorpsePlayer;
import gameserver.handler.targethandlers.impl.EnemySummon;
import gameserver.handler.targethandlers.impl.FlagPole;
import gameserver.handler.targethandlers.impl.FrontArea;
import gameserver.handler.targethandlers.impl.FrontAura;
import gameserver.handler.targethandlers.impl.Ground;
import gameserver.handler.targethandlers.impl.Holy;
import gameserver.handler.targethandlers.impl.One;
import gameserver.handler.targethandlers.impl.OwnerPet;
import gameserver.handler.targethandlers.impl.Party;
import gameserver.handler.targethandlers.impl.PartyClan;
import gameserver.handler.targethandlers.impl.PartyMember;
import gameserver.handler.targethandlers.impl.PartyNotMe;
import gameserver.handler.targethandlers.impl.PartyOther;
import gameserver.handler.targethandlers.impl.Pet;
import gameserver.handler.targethandlers.impl.Self;
import gameserver.handler.targethandlers.impl.Servitor;
import gameserver.handler.targethandlers.impl.Summon;
import gameserver.handler.targethandlers.impl.Unlockable;
import gameserver.model.skills.targets.TargetType;

public class TargetHandler extends LoggerObject
{
	private final Map<Enum<TargetType>, ITargetTypeHandler> _handlers;
	
	public static TargetHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected TargetHandler()
	{
		_handlers = new HashMap<>();

		registerHandler(new AllyAll());
		registerHandler(new Area());
		registerHandler(new AreaCorpseMob());
		registerHandler(new AreaFriendly());
		registerHandler(new AreaSummon());
		registerHandler(new Aura());
		registerHandler(new AuraCorpseMob());
		registerHandler(new AuraDeadMob());
		registerHandler(new AuraDoor());
		registerHandler(new AuraFriendly());
		registerHandler(new AuraMob());
		registerHandler(new AuraFriendlySummon());
		registerHandler(new AuraUndeadEnemy());
		registerHandler(new BehindArea());
		registerHandler(new BehindAura());
		registerHandler(new ClanAll());
		registerHandler(new ClanMember());
		registerHandler(new CommandChannel());
		registerHandler(new CorpseAlly());
		registerHandler(new CorpseClan());
		registerHandler(new CorpseFriendly());
		registerHandler(new CorpseMob());
		registerHandler(new CorpsePet());
		registerHandler(new CorpsePlayer());
		registerHandler(new EnemySummon());
		registerHandler(new FlagPole());
		registerHandler(new FrontArea());
		registerHandler(new FrontAura());
		registerHandler(new Ground());
		registerHandler(new Holy());
		registerHandler(new One());
		registerHandler(new OwnerPet());
		registerHandler(new Party());
		registerHandler(new PartyClan());
		registerHandler(new PartyMember());
		registerHandler(new PartyNotMe());
		registerHandler(new PartyOther());
		registerHandler(new Pet());
		registerHandler(new Self());
		registerHandler(new Servitor());
		registerHandler(new Summon());
		registerHandler(new Unlockable());

		info("Loaded " + _handlers.size() + " TargetHandlers");
	}
	
	public void registerHandler(ITargetTypeHandler handler)
	{
		_handlers.put(handler.getTargetType(), handler);
	}
	
	public synchronized void removeHandler(ITargetTypeHandler handler)
	{
		_handlers.remove(handler.getTargetType());
	}
	
	public ITargetTypeHandler getHandler(Enum<TargetType> targetType)
	{
		return _handlers.get(targetType);
	}
	
	public int size()
	{
		return _handlers.size();
	}
	
	private static class SingletonHolder
	{
		protected static final TargetHandler _instance = new TargetHandler();
	}
}