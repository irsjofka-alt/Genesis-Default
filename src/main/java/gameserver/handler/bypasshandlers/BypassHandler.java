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
package gameserver.handler.bypasshandlers;

import java.util.HashMap;
import java.util.Map;

import l2e.commons.log.LoggerObject;
import gameserver.Config;
import gameserver.handler.bypasshandlers.impl.AgressionInfo;
import gameserver.handler.bypasshandlers.impl.Augment;
import gameserver.handler.bypasshandlers.impl.Buy;
import gameserver.handler.bypasshandlers.impl.BuyShadowItem;
import gameserver.handler.bypasshandlers.impl.ChatLink;
import gameserver.handler.bypasshandlers.impl.ClanWarehouse;
import gameserver.handler.bypasshandlers.impl.DropInfo;
import gameserver.handler.bypasshandlers.impl.EffectInfo;
import gameserver.handler.bypasshandlers.impl.ElcardiaBuff;
import gameserver.handler.bypasshandlers.impl.Exchanger;
import gameserver.handler.bypasshandlers.impl.Festival;
import gameserver.handler.bypasshandlers.impl.FortSiege;
import gameserver.handler.bypasshandlers.impl.Freight;
import gameserver.handler.bypasshandlers.impl.Hennas;
import gameserver.handler.bypasshandlers.impl.ItemAuctionLink;
import gameserver.handler.bypasshandlers.impl.Link;
import gameserver.handler.bypasshandlers.impl.Loto;
import gameserver.handler.bypasshandlers.impl.ManorManager;
import gameserver.handler.bypasshandlers.impl.Multisell;
import gameserver.handler.bypasshandlers.impl.Observation;
import gameserver.handler.bypasshandlers.impl.OlympiadManagerLink;
import gameserver.handler.bypasshandlers.impl.OlympiadObservation;
import gameserver.handler.bypasshandlers.impl.PlayerHelp;
import gameserver.handler.bypasshandlers.impl.PrivateWarehouse;
import gameserver.handler.bypasshandlers.impl.QuestLink;
import gameserver.handler.bypasshandlers.impl.QuestList;
import gameserver.handler.bypasshandlers.impl.ReleaseAttribute;
import gameserver.handler.bypasshandlers.impl.RentPet;
import gameserver.handler.bypasshandlers.impl.Rift;
import gameserver.handler.bypasshandlers.impl.SkillList;
import gameserver.handler.bypasshandlers.impl.SupportBlessing;
import gameserver.handler.bypasshandlers.impl.SupportMagic;
import gameserver.handler.bypasshandlers.impl.TerritoryStatus;
import gameserver.handler.bypasshandlers.impl.VoiceCommand;
import gameserver.handler.bypasshandlers.impl.Wear;
import gameserver.handler.voicedcommandhandlers.impl.SellBuff;

public class BypassHandler extends LoggerObject
{
	private final Map<String, IBypassHandler> _handlers;
	
	public static BypassHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected BypassHandler()
	{
		_handlers = new HashMap<>();

		registerHandler(new AgressionInfo());
		registerHandler(new Augment());
		registerHandler(new Buy());
		registerHandler(new BuyShadowItem());
		registerHandler(new ChatLink());
		registerHandler(new ClanWarehouse());
		registerHandler(new DropInfo());
		registerHandler(new EffectInfo());
		registerHandler(new ElcardiaBuff());
		registerHandler(new Exchanger());
		registerHandler(new Festival());
		registerHandler(new FortSiege());
		registerHandler(new Freight());
		registerHandler(new Hennas());
		registerHandler(new ItemAuctionLink());
		registerHandler(new Link());
		registerHandler(new Loto());
		registerHandler(new ManorManager());
		registerHandler(new Multisell());
		registerHandler(new Observation());
		registerHandler(new OlympiadManagerLink());
		registerHandler(new OlympiadObservation());
		registerHandler(new PlayerHelp());
		registerHandler(new PrivateWarehouse());
		registerHandler(new QuestLink());
		registerHandler(new QuestList());
		registerHandler(new ReleaseAttribute());
		registerHandler(new RentPet());
		registerHandler(new Rift());
		registerHandler(new SkillList());
		registerHandler(new SupportBlessing());
		registerHandler(new SupportMagic());
		registerHandler(new TerritoryStatus());
		registerHandler(new VoiceCommand());
		registerHandler(new Wear());
		registerHandler(new SellBuff());

		info("Loaded " + _handlers.size() + " BypassHandlers");
	}
	
	public void registerHandler(IBypassHandler handler)
	{
		for (final String element : handler.getBypassList())
		{
			if (_handlers.containsKey(element))
			{
				info("dublicate bypass registered! First handler: " + _handlers.get(element).getClass().getSimpleName() + " second: " + handler.getClass().getSimpleName());
				_handlers.remove(element);
			}
			_handlers.put(element.toLowerCase(), handler);
		}
	}
	
	public synchronized void removeHandler(IBypassHandler handler)
	{
		for (final String element : handler.getBypassList())
		{
			_handlers.remove(element.toLowerCase());
		}
	}
	
	public IBypassHandler getHandler(String BypassCommand)
	{
		String command = BypassCommand;
		
		if (BypassCommand.indexOf(" ") != -1)
		{
			command = BypassCommand.substring(0, BypassCommand.indexOf(" "));
		}
		
		if (Config.DEBUG)
		{
			_log.info("getting handler for command: " + command + " -> " + (_handlers.get(command.hashCode()) != null));
		}
		
		return _handlers.get(command.toLowerCase());
	}
	
	public int size()
	{
		return _handlers.size();
	}
	
	private static class SingletonHolder
	{
		protected static final BypassHandler _instance = new BypassHandler();
	}
}