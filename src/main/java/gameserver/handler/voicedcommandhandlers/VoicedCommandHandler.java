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
package gameserver.handler.voicedcommandhandlers;

import java.util.HashMap;
import java.util.Map;

import l2e.commons.log.LoggerObject;
import gameserver.Config;
import gameserver.handler.voicedcommandhandlers.impl.Achievement;
import gameserver.handler.voicedcommandhandlers.impl.Acp;
import gameserver.handler.voicedcommandhandlers.impl.AncientAdenaExchanger;
import gameserver.handler.voicedcommandhandlers.impl.AutoLoot;
import gameserver.handler.voicedcommandhandlers.impl.Banking;
import gameserver.handler.voicedcommandhandlers.impl.BlockBuffs;
import gameserver.handler.voicedcommandhandlers.impl.ChangePasswords;
import gameserver.handler.voicedcommandhandlers.impl.CheckPremium;
import gameserver.handler.voicedcommandhandlers.impl.CombineTalismans;
import gameserver.handler.voicedcommandhandlers.impl.Debug;
import gameserver.handler.voicedcommandhandlers.impl.DressMe;
import gameserver.handler.voicedcommandhandlers.impl.Enchant;
import gameserver.handler.voicedcommandhandlers.impl.ExpGain;
import gameserver.handler.voicedcommandhandlers.impl.FindParty;
import gameserver.handler.voicedcommandhandlers.impl.Hellbound;
import gameserver.handler.voicedcommandhandlers.impl.HideBuffsAnimation;
import gameserver.handler.voicedcommandhandlers.impl.HideTraders;
import gameserver.handler.voicedcommandhandlers.impl.Lang;
import gameserver.handler.voicedcommandhandlers.impl.Menu;
import gameserver.handler.voicedcommandhandlers.impl.Offline;
import gameserver.handler.voicedcommandhandlers.impl.Online;
import gameserver.handler.voicedcommandhandlers.impl.OpenAtod;
import gameserver.handler.voicedcommandhandlers.impl.Ping;
import gameserver.handler.voicedcommandhandlers.impl.PromoCode;
import gameserver.handler.voicedcommandhandlers.impl.Ranking;
import gameserver.handler.voicedcommandhandlers.impl.RecoveryItem;
import gameserver.handler.voicedcommandhandlers.impl.Relog;
import gameserver.handler.voicedcommandhandlers.impl.Repair;
import gameserver.handler.voicedcommandhandlers.impl.SchemeBuffs;
import gameserver.handler.voicedcommandhandlers.impl.Security;
import gameserver.handler.voicedcommandhandlers.impl.SellBuff;
import gameserver.handler.voicedcommandhandlers.impl.SevenRaidBosses;
import gameserver.handler.voicedcommandhandlers.impl.ShiftClick;
import gameserver.handler.voicedcommandhandlers.impl.TeleToLeader;
import gameserver.handler.voicedcommandhandlers.impl.TimeSkills;
import gameserver.handler.voicedcommandhandlers.impl.VoteReward;
import gameserver.handler.voicedcommandhandlers.impl.Wedding;
import gameserver.handler.voicedcommandhandlers.impl.WhoAmI;

public class VoicedCommandHandler extends LoggerObject
{
	private final Map<String, IVoicedCommandHandler> _handlers;

	public static VoicedCommandHandler getInstance()
	{
		return SingletonHolder._instance;
	}

	protected VoicedCommandHandler()
	{
		_handlers = new HashMap<>();
		registerHandler(new Achievement());
		registerHandler(new Acp());
		registerHandler(new AncientAdenaExchanger());
		registerHandler(new AutoLoot());
		registerHandler(new Banking());
		registerHandler(new BlockBuffs());
		registerHandler(new ChangePasswords());
		registerHandler(new CombineTalismans());
		registerHandler(new Menu());
		registerHandler(new Online());
		registerHandler(new OpenAtod());
		registerHandler(new Ping());
		registerHandler(new Repair());
		registerHandler(new Debug());
		registerHandler(new ExpGain());
		registerHandler(new Hellbound());
		registerHandler(new HideBuffsAnimation());
		registerHandler(new HideTraders());
		registerHandler(new Lang());
		registerHandler(new Ranking());
		registerHandler(new SevenRaidBosses());
		registerHandler(new TeleToLeader());
		registerHandler(new Wedding());
		registerHandler(new DressMe());
		DressMe.parseWeapon();
		registerHandler(new WhoAmI());
		registerHandler(new SellBuff());
		registerHandler(new VoteReward());
		registerHandler(new FindParty());
		registerHandler(new Security());
		registerHandler(new Enchant());
		registerHandler(new RecoveryItem());
		registerHandler(new CheckPremium());
		registerHandler(new Offline());
		registerHandler(new SchemeBuffs());
		registerHandler(new Relog());
		registerHandler(new PromoCode());
		registerHandler(new ShiftClick());
		registerHandler(new TimeSkills());
		info("Loaded " + _handlers.size() + " VoicedHandlers");
	}
	
	public void registerHandler(IVoicedCommandHandler handler)
	{
		final String[] ids = handler.getVoicedCommandList();
		for (final String id : ids)
		{
			if (_handlers.containsKey(id))
			{
				info("dublicate bypass registered! First handler: " + _handlers.get(id).getClass().getSimpleName() + " second: " + handler.getClass().getSimpleName());
				_handlers.remove(id);
			}
			_handlers.put(id, handler);
		}
	}
	
	public synchronized void removeHandler(IVoicedCommandHandler handler)
	{
		final String[] ids = handler.getVoicedCommandList();
		for (final String id : ids)
		{
			_handlers.remove(id);
		}
	}
	
	public IVoicedCommandHandler getHandler(String voicedCommand)
	{
		String command = voicedCommand;
		if (voicedCommand.indexOf(" ") != -1)
		{
			command = voicedCommand.substring(0, voicedCommand.indexOf(" "));
		}
		if (Config.DEBUG)
		{
			_log.info("getting handler for command: " + command + " -> " + (_handlers.get(command.hashCode()) != null));
		}
		return _handlers.get(command);
	}
	
	public int size()
	{
		return _handlers.size();
	}

	private static class SingletonHolder
	{
		protected static final VoicedCommandHandler _instance = new VoicedCommandHandler();
	}
}