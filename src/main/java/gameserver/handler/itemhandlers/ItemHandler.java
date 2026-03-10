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
package gameserver.handler.itemhandlers;

import java.util.HashMap;
import java.util.Map;

import l2e.commons.log.LoggerObject;
import gameserver.handler.itemhandlers.impl.BeastSoulShot;
import gameserver.handler.itemhandlers.impl.BeastSpice;
import gameserver.handler.itemhandlers.impl.BeastSpiritShot;
import gameserver.handler.itemhandlers.impl.BlessedSpiritShot;
import gameserver.handler.itemhandlers.impl.Book;
import gameserver.handler.itemhandlers.impl.Bypass;
import gameserver.handler.itemhandlers.impl.Calculator;
import gameserver.handler.itemhandlers.impl.ChristmasTree;
import gameserver.handler.itemhandlers.impl.Community;
import gameserver.handler.itemhandlers.impl.Disguise;
import gameserver.handler.itemhandlers.impl.Elixir;
import gameserver.handler.itemhandlers.impl.EnchantAttribute;
import gameserver.handler.itemhandlers.impl.EnchantScrolls;
import gameserver.handler.itemhandlers.impl.EventItem;
import gameserver.handler.itemhandlers.impl.ExtractableItems;
import gameserver.handler.itemhandlers.impl.FishShots;
import gameserver.handler.itemhandlers.impl.Harvester;
import gameserver.handler.itemhandlers.impl.ItemSkills;
import gameserver.handler.itemhandlers.impl.ItemSkillsTemplate;
import gameserver.handler.itemhandlers.impl.ManaPotion;
import gameserver.handler.itemhandlers.impl.Maps;
import gameserver.handler.itemhandlers.impl.MercTicket;
import gameserver.handler.itemhandlers.impl.NevitHourglass;
import gameserver.handler.itemhandlers.impl.NicknameColor;
import gameserver.handler.itemhandlers.impl.Nobless;
import gameserver.handler.itemhandlers.impl.PetFood;
import gameserver.handler.itemhandlers.impl.Premium;
import gameserver.handler.itemhandlers.impl.QuestItems;
import gameserver.handler.itemhandlers.impl.Recipes;
import gameserver.handler.itemhandlers.impl.RollingDice;
import gameserver.handler.itemhandlers.impl.ScrollOfResurrection;
import gameserver.handler.itemhandlers.impl.Seed;
import gameserver.handler.itemhandlers.impl.SevenSignsRecord;
import gameserver.handler.itemhandlers.impl.SoulShots;
import gameserver.handler.itemhandlers.impl.SpecialXMas;
import gameserver.handler.itemhandlers.impl.SpiritShot;
import gameserver.handler.itemhandlers.impl.SummonItems;
import gameserver.handler.itemhandlers.impl.TeleportBookmark;
import gameserver.handler.itemhandlers.impl.TempHero;
import gameserver.handler.itemhandlers.impl.VisualItems;
import gameserver.model.actor.templates.items.EtcItem;

public class ItemHandler extends LoggerObject
{
	private final Map<String, IItemHandler> _handlers;
	
	public static ItemHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public int size()
	{
		return _handlers.size();
	}
	
	protected ItemHandler()
	{
		_handlers = new HashMap<>();
		
		registerHandler(new BeastSoulShot());
		registerHandler(new BeastSpice());
		registerHandler(new BeastSpiritShot());
		registerHandler(new BlessedSpiritShot());
		registerHandler(new Bypass());
		registerHandler(new Book());
		registerHandler(new Calculator());
		registerHandler(new ChristmasTree());
		registerHandler(new Community());
		registerHandler(new Disguise());
		registerHandler(new Elixir());
		registerHandler(new EnchantAttribute());
		registerHandler(new EnchantScrolls());
		registerHandler(new EventItem());
		registerHandler(new ExtractableItems());
		registerHandler(new FishShots());
		registerHandler(new Harvester());
		registerHandler(new ItemSkills());
		registerHandler(new ItemSkillsTemplate());
		registerHandler(new ManaPotion());
		registerHandler(new Maps());
		registerHandler(new MercTicket());
		registerHandler(new NevitHourglass());
		registerHandler(new NicknameColor());
		registerHandler(new Nobless());
		registerHandler(new PetFood());
		registerHandler(new Premium());
		registerHandler(new QuestItems());
		registerHandler(new Recipes());
		registerHandler(new RollingDice());
		registerHandler(new ScrollOfResurrection());
		registerHandler(new Seed());
		registerHandler(new SevenSignsRecord());
		registerHandler(new SoulShots());
		registerHandler(new SpecialXMas());
		registerHandler(new SpiritShot());
		registerHandler(new SummonItems());
		registerHandler(new TempHero());
		registerHandler(new TeleportBookmark());
		registerHandler(new VisualItems());
		
		info("Loaded " + _handlers.size() + " ItemHandlers.");
	}
	
	public void registerHandler(IItemHandler handler)
	{
		_handlers.put(handler.getClass().getSimpleName(), handler);
	}
	
	public synchronized void removeHandler(IItemHandler handler)
	{
		_handlers.remove(handler.getClass().getSimpleName());
	}
	
	public IItemHandler getHandler(EtcItem item)
	{
		if ((item == null) || (item.getHandlerName() == null))
		{
			return null;
		}
		return _handlers.get(item.getHandlerName());
	}
	
	private static class SingletonHolder
	{
		protected static final ItemHandler _instance = new ItemHandler();
	}
}