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
package gameserver.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import gameserver.Config;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Player;
import gameserver.model.items.instance.ItemInstance;
import gameserver.model.skills.Skill;
import gameserver.network.clientpackets.Say2;

public class Log
{
	private static final Logger LOGGER = LogManager.getLogger(Log.class);
	
	private static final Marker _logChatAll = MarkerManager.getMarker("CHATALL");
	private static final Marker _logChatAlliance = MarkerManager.getMarker("CHATALLIANCE");
	private static final Marker _logChatBattlefield = MarkerManager.getMarker("CHATBATTLEFIELD");
	private static final Marker _logChatClan = MarkerManager.getMarker("CHATCLAN");
	private static final Marker _logChatHero = MarkerManager.getMarker("CHATHERO");
	private static final Marker _logChatMpccRoom = MarkerManager.getMarker("CHATMPCCROOM");
	private static final Marker _logChatParty = MarkerManager.getMarker("CHATPARTY");
	private static final Marker _logChatPartyMatchRoom = MarkerManager.getMarker("CHATPARTYMATCHROOM");
	private static final Marker _logChatPartyRoomAll = MarkerManager.getMarker("CHATPARTYROOMALL");
	private static final Marker _logChatPartyRoomCommander = MarkerManager.getMarker("CHATPARTYROOMCOMMANDER");
	private static final Marker _logChatPetition = MarkerManager.getMarker("CHATPETITION");
	private static final Marker _logChatShout = MarkerManager.getMarker("CHATSHOUT");
	private static final Marker _logChatTell = MarkerManager.getMarker("CHATTELL");
	private static final Marker _logChatTrade = MarkerManager.getMarker("CHATTRADE");
	private static final Marker _logChatFriend = MarkerManager.getMarker("CHATFRIEND");
	private static final Marker _logItems = MarkerManager.getMarker("ITEM");
	private static final Marker _logDamage = MarkerManager.getMarker("DAMAGE");
	private static final Marker _logEnchantSkill = MarkerManager.getMarker("ENCHANTSKILL");
	private static final Marker _logEnchantItem = MarkerManager.getMarker("ENCHANTITEM");
	private static final Marker _logGame = MarkerManager.getMarker("GAME");
	private static final Marker _logAutofarm = MarkerManager.getMarker("AUTOFARM");
	
	public static void addLogFarm(String cat, String text, Player player)
	{
		final StringBuilder output = new StringBuilder();
		
		output.append(cat);
		output.append(' ');
		output.append(text);
		if (player != null)
		{
			output.append(' ');
			output.append(player);
		}
		LOGGER.debug(_logAutofarm, output.toString());
	}
	
	public static void addLogGame(String cat, String text, String name)
	{
		final StringBuilder output = new StringBuilder();
		
		output.append(cat);
		if (name != null)
		{
			output.append(' ');
			output.append(name);
		}
		output.append(' ');
		output.append(text);
		LOGGER.debug(_logGame, output.toString());
	}
	
	public static void addLogDamage(int damage, String cat, Skill skill, Player player, Creature target)
	{
		final StringBuilder output = new StringBuilder();
		output.append(cat);
		if (player != null)
		{
			output.append(' ');
			output.append(player);
		}
		output.append(" did ");
		output.append(damage);
		output.append(" damage by skill ");
		output.append("[" + skill.getName(null) + "]");
		output.append(" to ");
		if (target != null)
		{
			output.append(target);
		}
		LOGGER.debug(_logDamage, output.toString());
	}
	
	public static void AddLogChat(int type, String player, String target, String text)
	{
		final StringBuilder output = new StringBuilder();
		output.append('[');
		output.append(player);
		if (target != null)
		{
			output.append(" -> ");
			output.append(target);
		}
		output.append(']');
		output.append(' ');
		output.append(text);
		
		switch (type)
		{
			case Say2.ALL :
				LOGGER.debug(_logChatAll, output.toString());
				break;
			case Say2.ALLIANCE :
				LOGGER.debug(_logChatAlliance, output.toString());
				break;
			case Say2.BATTLEFIELD :
				LOGGER.debug(_logChatBattlefield, output.toString());
				break;
			case Say2.CLAN :
				LOGGER.debug(_logChatClan, output.toString());
				break;
			case Say2.HERO_VOICE :
				LOGGER.debug(_logChatHero, output.toString());
				break;
			case Say2.MPCC_ROOM :
				LOGGER.debug(_logChatMpccRoom, output.toString());
				break;
			case Say2.PARTY :
				LOGGER.debug(_logChatParty, output.toString());
				break;
			case Say2.PARTYMATCH_ROOM :
				LOGGER.debug(_logChatPartyMatchRoom, output.toString());
				break;
			case Say2.PARTYROOM_ALL :
				LOGGER.debug(_logChatPartyRoomAll, output.toString());
				break;
			case Say2.PARTYROOM_COMMANDER :
				LOGGER.debug(_logChatPartyRoomCommander, output.toString());
				break;
			case Say2.PETITION_PLAYER :
			case Say2.PETITION_GM :
				LOGGER.debug(_logChatPetition, output.toString());
				break;
			case Say2.SHOUT :
				LOGGER.debug(_logChatShout, output.toString());
				break;
			case Say2.TELL :
				LOGGER.debug(_logChatTell, output.toString());
				break;
			case Say2.TRADE :
				LOGGER.debug(_logChatTrade, output.toString());
				break;
			case Say2.L2FRIEND :
				LOGGER.debug(_logChatFriend, output.toString());
				break;
		}
	}
	
	public static void addLogEnchantSkill(String param, Skill skill, Player player, ItemInstance item, int rate, boolean isEnchant)
	{
		final StringBuilder output = new StringBuilder();
		output.append(param);
		if (player != null)
		{
			output.append(' ');
			output.append(player);
		}
		if (isEnchant)
		{
			output.append(" enchant ");
			output.append("[" + skill.getName(null) + "]");
			output.append(" to ");
			output.append(skill.getLevel() > 100 ? "[+" + skill.getLevel() % 100 + "]" : "[+0]");
			output.append(" by ");
			output.append(item);
			output.append(" chance: ");
			output.append("[" + rate + "%]");
		}
		else
		{
			output.append("[" + skill.getName(null) + "]");
			output.append(" by ");
			output.append(item);
			if (rate > 0)
			{
				output.append(" chance: ");
				output.append("[" + rate + "%]");
			}
		}
		LOGGER.debug(_logEnchantSkill, output.toString());
	}
	
	public static void addLogEnchantItem(String param, ItemInstance enchantItem, Player player, ItemInstance item)
	{
		final StringBuilder output = new StringBuilder();
		output.append(param);
		if (player != null)
		{
			output.append(' ');
			output.append(player);
		}
		output.append(" enchant ");
		output.append("[" + enchantItem + "]");
		output.append(" to ");
		output.append("[+" + enchantItem.getEnchantLevel() + "]");
		output.append(" by ");
		output.append(item);
		LOGGER.debug(_logEnchantItem, output.toString());
	}
	
	public static void addLogItem(String param, String action, String name, long count, int enchantLevel, String finalAction)
	{
		if (Config.LOG_ITEMS)
		{
			final StringBuilder output = new StringBuilder();
			output.append('[');
			output.append(param);
			output.append(']');
			output.append(' ');
			output.append(action);
			output.append(' ');
			output.append("[" + count + "]");
			output.append(' ');
			output.append(name);
			if (enchantLevel > 0)
			{
				output.append(' ');
				output.append("(+" + enchantLevel + ")");
			}
			if (finalAction != null)
			{
				output.append(' ');
				output.append(finalAction);
			}
			LOGGER.debug(_logItems, output.toString());
		}
	}
}