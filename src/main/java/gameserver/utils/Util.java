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

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import l2e.commons.util.DeclensionKey;
import l2e.commons.util.Rnd;
import l2e.commons.util.Strings;
import l2e.commons.util.Utils;
import gameserver.Config;
import gameserver.data.parser.ItemsParser;
import gameserver.geodata.GeoEngine;
import gameserver.model.GameObject;
import gameserver.model.Location;
import gameserver.model.World;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Player;
import gameserver.model.actor.templates.items.Item;
import gameserver.model.interfaces.ILocational;
import gameserver.model.stats.Stats;
import gameserver.model.strings.server.ServerStorage;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.ExPCCafePointInfo;
import gameserver.network.serverpackets.NpcHtmlMessage;
import gameserver.network.serverpackets.ShowBoard;
import gameserver.network.serverpackets.SystemMessage;

public class Util extends Utils
{
	public static void handleIllegalPlayerAction(Player actor, String message)
	{
		final SimpleDateFormat _formatter = new SimpleDateFormat("dd/MM/yyyy H:mm:ss");
		final String date = _formatter.format(new Date());
		actor.addBannedAction("[" + date + "] " + message);
		if (actor.getBannedActions().size() >= Config.PUNISH_VALID_ATTEMPTS)
		{
			IllegalPlayerAction.IllegalAction(actor, actor.getBannedActions(), Config.DEFAULT_PUNISH);
		}
	}
	
	public static void addServiceLog(String message)
	{
		if (!Config.SERVICE_LOGS)
		{
			return;
		}
		final SimpleDateFormat _formatter = new SimpleDateFormat("dd/MM/yyyy H:mm:ss");
		final String date = _formatter.format(new Date());
		message = "[" + date + "] " + message;
		ServiceLogs.addServiceLogs(message);
	}
	
	public final static boolean isOnAngle(GameObject actor, GameObject target, int direction, int maxAngle)
	{
		boolean value = false;
		double calcangle, angleToTarget, angleDiff, maxAngleDiff;
		angleToTarget = calculateAngleFrom(actor, target);
		calcangle = convertHeadingToDegree(actor.getHeading()) + direction;
		angleDiff = calcangle - angleToTarget;
		maxAngleDiff = maxAngle / 2d;
		if (angleDiff <= (-360 + maxAngleDiff))
		{
			angleDiff += 360;
		}
		if (angleDiff >= (360 - maxAngleDiff))
		{
			angleDiff -= 360;
		}
		if (Math.abs(angleDiff) <= maxAngleDiff)
		{
			value = true;
		}
		return value;
	}
	
	public static double calculateDistance(Location loc1, Location loc2)
	{
		return calculateDistance(loc1.getX(), loc1.getY(), loc1.getZ(), loc2.getX(), loc2.getY(), loc2.getZ(), false);
	}
	
	public static double calculateAngleFrom(GameObject obj1, GameObject obj2)
	{
		return calculateAngleFrom(obj1.getX(), obj1.getY(), obj2.getX(), obj2.getY());
	}
	
	public static final int calculateHeadingFrom(GameObject obj1, GameObject obj2)
	{
		return calculateHeadingFrom(obj1.getX(), obj1.getY(), obj2.getX(), obj2.getY());
	}
	
	public static double calculateDistance(GameObject obj1, GameObject obj2, boolean includeZAxis)
	{
		if ((obj1 == null) || (obj2 == null))
		{
			return 1000000;
		}
		return calculateDistance(obj1.getX(), obj1.getY(), obj1.getZ(), obj2.getX(), obj2.getY(), obj2.getZ(), includeZAxis);
	}
	
	public static boolean checkIfInRange(int range, int x, int y, int z, GameObject obj2, boolean includeZAxis)
	{
		if (obj2 == null)
		{
			return false;
		}
		if (range == -1)
		{
			return true;
		}
		int rad = 0;
		if (obj2 instanceof Creature creature)
		{
			rad += creature.getTemplate().getCollisionRadius();
		}
		
		final double dx = x - obj2.getX();
		final double dy = y - obj2.getY();
		
		if (includeZAxis)
		{
			final double dz = z - obj2.getZ();
			final double d = (dx * dx) + (dy * dy) + (dz * dz);
			
			return d <= ((range * range) + (2 * range * rad) + (rad * rad));
		}
		final double d = (dx * dx) + (dy * dy);
		
		return d <= ((range * range) + (2 * range * rad) + (rad * rad));
	}
	
	public static boolean checkIfInRange(int range, GameObject obj1, GameObject obj2, boolean includeZAxis)
	{
		if ((obj1 == null) || (obj2 == null))
		{
			return false;
		}
		if (obj1.getReflectionId() != obj2.getReflectionId())
		{
			return false;
		}
		if (range == -1)
		{
			return true;
		}
		
		int rad = 0;
		if (obj1 instanceof Creature creature)
		{
			rad += creature.getTemplate().getCollisionRadius();
		}
		if (obj2 instanceof Creature creature)
		{
			rad += creature.getTemplate().getCollisionRadius();
		}
		
		final double dx = obj1.getX() - obj2.getX();
		final double dy = obj1.getY() - obj2.getY();
		double d = (dx * dx) + (dy * dy);
		
		if (includeZAxis)
		{
			final double dz = obj1.getZ() - obj2.getZ();
			d += (dz * dz);
		}
		return d <= ((range * range) + (2 * range * rad) + (rad * rad));
	}
	
	public static boolean checkIfInShortRadius(int radius, GameObject obj1, GameObject obj2, boolean includeZAxis)
	{
		if ((obj1 == null) || (obj2 == null))
		{
			return false;
		}
		if (radius == -1)
		{
			return true;
		}
		
		final int dx = obj1.getX() - obj2.getX();
		final int dy = obj1.getY() - obj2.getY();
		
		if (includeZAxis)
		{
			final int dz = obj1.getZ() - obj2.getZ();
			return ((dx * dx) + (dy * dy) + (dz * dz)) <= (radius * radius);
		}
		return ((dx * dx) + (dy * dy)) <= (radius * radius);
	}
	
	public static void sendHtml(Player activeChar, String html)
	{
		final NpcHtmlMessage npcHtml = new NpcHtmlMessage(0);
		npcHtml.setHtml(activeChar, html);
		activeChar.sendPacket(npcHtml);
	}
	
	public static void fillMultiEditContent(Player activeChar, String text)
	{
		activeChar.sendPacket(new ShowBoard(Arrays.asList("0", "0", "0", "0", "0", "0", activeChar.getName(null), Integer.toString(activeChar.getObjectId()), activeChar.getAccountName(), "9", " ", " ", text.replaceAll("<br>", Config.EOL), "0", "0", "0", "0")));
	}
	
	public static int getPlayersCountInRadius(int range, GameObject npc, boolean playable, boolean invisible)
	{
		int count = 0;
		for (final Creature cha : World.getAroundCharacters(npc))
		{
			if ((cha != null) && (playable && (cha.isPlayable() || cha.isPet())))
			{
				if (!invisible && cha.isInvisible())
				{
					continue;
				}
				
				if (((cha.getZ() < (npc.getZ() - 100)) && (cha.getZ() > (npc.getZ() + 100))) || !(GeoEngine.getInstance().canSeeTarget(cha, npc)))
				{
					continue;
				}
				
				if (Util.checkIfInRange(range, npc, cha, true) && !cha.isDead())
				{
					count++;
				}
			}
		}
		return count;
	}
	
	public static String formatPay(Player player, long count, int item)
	{
		if (count > 0)
		{
			return formatAdena(count) + " " + getItemName(player, item);
		}
		return "" + ServerStorage.getInstance().getString(player.getLang(), "Util.FREE") + "";
	}
	
	public static String getItemName(Player player, int itemId)
	{
		switch (itemId)
		{
			case -300 :
				return ServerStorage.getInstance().getString(player.getLang(), "Util.FAME");
			case -200 :
				return ServerStorage.getInstance().getString(player.getLang(), "Util.REPUTATION");
			case -100 :
				return ServerStorage.getInstance().getString(player.getLang(), "Util.PC_BANG");
			case -1 :
				return ServerStorage.getInstance().getString(player.getLang(), "Util.PRIME_POINT");
			default :
				if (itemId == Config.EXP_ID)
				{
					return ServerStorage.getInstance().getString(player.getLang(), "Util.EXP");
				}
				else if (itemId == Config.SP_ID)
				{
					return ServerStorage.getInstance().getString(player.getLang(), "Util.SP");
				}
				else
				{
					final Item item = ItemsParser.getInstance().getTemplate(itemId);
					if (item != null)
					{
						return item.getName(player.getLang());
					}
				}
				return ("No Name");
		}
	}
	
	public static String getItemName(int itemId)
	{
		switch (itemId)
		{
			case -300 :
				return ServerStorage.getInstance().getString(Config.MULTILANG_DEFAULT, "Util.FAME");
			case -200 :
				return ServerStorage.getInstance().getString(Config.MULTILANG_DEFAULT, "Util.REPUTATION");
			case -100 :
				return ServerStorage.getInstance().getString(Config.MULTILANG_DEFAULT, "Util.PC_BANG");
			case -1 :
				return ServerStorage.getInstance().getString(Config.MULTILANG_DEFAULT, "Util.PRIME_POINT");
			default :
				if (itemId == Config.EXP_ID)
				{
					return ServerStorage.getInstance().getString(Config.MULTILANG_DEFAULT, "Util.EXP");
				}
				else if (itemId == Config.SP_ID)
				{
					return ServerStorage.getInstance().getString(Config.MULTILANG_DEFAULT, "Util.SP");
				}
				else
				{
					final var item = ItemsParser.getInstance().getTemplate(itemId);
					if (item != null)
					{
						return item.getName(null);
					}
				}
				return ("No Name");
		}
	}
	
	public static String getItemIcon(int itemId)
	{
		switch (itemId)
		{
			case -300 :
				return "icon.pvp_point_i00";
			case -200 :
				return "icon.skill0390";
			case -100 :
				return "icon.etc_pccafe_point_i00";
			case -1 :
				return "icon.etc_royal_membership_i00";
			default :
				if (itemId == Config.EXP_ID)
				{
					return "icon.etc_exp_point_i00";
				}
				else if (itemId == Config.SP_ID)
				{
					return "icon.etc_sp_point_i00";
				}
				else
				{
					final Item item = ItemsParser.getInstance().getTemplate(itemId);
					if (item != null)
					{
						return item.getIcon();
					}
				}
				return "icon.etc_question_mark_i00";
		}
	}
	
	public static String declension(Player player, long count, DeclensionKey word)
	{
		String one = "";
		String two = "";
		String five = "";
		switch (word)
		{
			case DAYS :
				one = new String("" + ServerStorage.getInstance().getString(player.getLang(), "Util.DAYS_ONE") + "");
				two = new String("" + ServerStorage.getInstance().getString(player.getLang(), "Util.DAYS_TWO") + "");
				five = new String("" + ServerStorage.getInstance().getString(player.getLang(), "Util.DAYS_FIVE") + "");
				break;
			case HOUR :
				one = new String("" + ServerStorage.getInstance().getString(player.getLang(), "Util.HOURS_ONE") + "");
				two = new String("" + ServerStorage.getInstance().getString(player.getLang(), "Util.HOURS_TWO") + "");
				five = new String("" + ServerStorage.getInstance().getString(player.getLang(), "Util.HOURS_FIVE") + "");
				break;
			case MINUTES :
				one = new String("" + ServerStorage.getInstance().getString(player.getLang(), "Util.MIN_ONE") + "");
				two = new String("" + ServerStorage.getInstance().getString(player.getLang(), "Util.MIN_TWO") + "");
				five = new String("" + ServerStorage.getInstance().getString(player.getLang(), "Util.MIN_FIVE") + "");
				break;
			case PIECE :
				one = new String("" + ServerStorage.getInstance().getString(player.getLang(), "Util.PIECES_ONE") + "");
				two = new String("" + ServerStorage.getInstance().getString(player.getLang(), "Util.PIECES_TWO") + "");
				five = new String("" + ServerStorage.getInstance().getString(player.getLang(), "Util.PIECES_FIVE") + "");
				break;
			case POINT :
				one = new String("" + ServerStorage.getInstance().getString(player.getLang(), "Util.POINTS_ONE") + "");
				two = new String("" + ServerStorage.getInstance().getString(player.getLang(), "Util.POINTS_TWO") + "");
				five = new String("" + ServerStorage.getInstance().getString(player.getLang(), "Util.POINTS_FIVE") + "");
		}
		if (count > 100L)
		{
			count %= 100L;
		}
		if (count > 20L)
		{
			count %= 10L;
		}
		if (count == 1L)
		{
			return one.toString();
		}
		if ((count == 2L) || (count == 3L) || (count == 4L))
		{
			return two.toString();
		}
		return five.toString();
	}
	
	public static String boolToString(Player player, boolean b)
	{
		return b ? "" + ServerStorage.getInstance().getString(player.getLang(), "Util.TRUE") + "" : "" + ServerStorage.getInstance().getString(player.getLang(), "Util.FALSE") + "";
	}
	
	public final static String className(Player player, int classId)
	{
		return className(player.getLang(), classId);
	}
	
	public final static String className(String lang, int classId)
	{
		return ServerStorage.getInstance().getString(lang, "ClassName." + classId + "");
	}
	
	public final static String className(Player player, String name)
	{
		final String lang = player.getLang();
		final Map<String, String> classList = new HashMap<>();
		
		classList.put("HumanFighter", "" + ServerStorage.getInstance().getString(lang, "ClassName.0") + "");
		classList.put("Warrior", "" + ServerStorage.getInstance().getString(lang, "ClassName.1") + "");
		classList.put("Gladiator", "" + ServerStorage.getInstance().getString(lang, "ClassName.2") + "");
		classList.put("Warlord", "" + ServerStorage.getInstance().getString(lang, "ClassName.3") + "");
		classList.put("HumanKnight", "" + ServerStorage.getInstance().getString(lang, "ClassName.4") + "");
		classList.put("Paladin", "" + ServerStorage.getInstance().getString(lang, "ClassName.5") + "");
		classList.put("DarkAvenger", "" + ServerStorage.getInstance().getString(lang, "ClassName.6") + "");
		classList.put("Rogue", "" + ServerStorage.getInstance().getString(lang, "ClassName.7") + "");
		classList.put("TreasureHunter", "" + ServerStorage.getInstance().getString(lang, "ClassName.8") + "");
		classList.put("Hawkeye", "" + ServerStorage.getInstance().getString(lang, "ClassName.9") + "");
		classList.put("HumanMystic", "" + ServerStorage.getInstance().getString(lang, "ClassName.10") + "");
		classList.put("HumanWizard", "" + ServerStorage.getInstance().getString(lang, "ClassName.11") + "");
		classList.put("Sorceror", "" + ServerStorage.getInstance().getString(lang, "ClassName.12") + "");
		classList.put("Necromancer", "" + ServerStorage.getInstance().getString(lang, "ClassName.13") + "");
		classList.put("Warlock", "" + ServerStorage.getInstance().getString(lang, "ClassName.14") + "");
		classList.put("Cleric", "" + ServerStorage.getInstance().getString(lang, "ClassName.15") + "");
		classList.put("Bishop", "" + ServerStorage.getInstance().getString(lang, "ClassName.16") + "");
		classList.put("Prophet", "" + ServerStorage.getInstance().getString(lang, "ClassName.17") + "");
		classList.put("ElvenFighter", "" + ServerStorage.getInstance().getString(lang, "ClassName.18") + "");
		classList.put("ElvenKnight", "" + ServerStorage.getInstance().getString(lang, "ClassName.19") + "");
		classList.put("TempleKnight", "" + ServerStorage.getInstance().getString(lang, "ClassName.20") + "");
		classList.put("Swordsinger", "" + ServerStorage.getInstance().getString(lang, "ClassName.21") + "");
		classList.put("ElvenScout", "" + ServerStorage.getInstance().getString(lang, "ClassName.22") + "");
		classList.put("Plainswalker", "" + ServerStorage.getInstance().getString(lang, "ClassName.23") + "");
		classList.put("SilverRanger", "" + ServerStorage.getInstance().getString(lang, "ClassName.24") + "");
		classList.put("ElvenMystic", "" + ServerStorage.getInstance().getString(lang, "ClassName.25") + "");
		classList.put("ElvenWizard", "" + ServerStorage.getInstance().getString(lang, "ClassName.26") + "");
		classList.put("Spellsinger", "" + ServerStorage.getInstance().getString(lang, "ClassName.27") + "");
		classList.put("ElementalSummoner", "" + ServerStorage.getInstance().getString(lang, "ClassName.28") + "");
		classList.put("ElvenOracle", "" + ServerStorage.getInstance().getString(lang, "ClassName.29") + "");
		classList.put("ElvenElder", "" + ServerStorage.getInstance().getString(lang, "ClassName.30") + "");
		classList.put("DarkElvenFighter", "" + ServerStorage.getInstance().getString(lang, "ClassName.31") + "");
		classList.put("PalusKnight", "" + ServerStorage.getInstance().getString(lang, "ClassName.32") + "");
		classList.put("ShillienKnight", "" + ServerStorage.getInstance().getString(lang, "ClassName.33") + "");
		classList.put("Bladedancer", "" + ServerStorage.getInstance().getString(lang, "ClassName.34") + "");
		classList.put("Assassin", "" + ServerStorage.getInstance().getString(lang, "ClassName.35") + "");
		classList.put("AbyssWalker", "" + ServerStorage.getInstance().getString(lang, "ClassName.36") + "");
		classList.put("PhantomRanger", "" + ServerStorage.getInstance().getString(lang, "ClassName.37") + "");
		classList.put("DarkElvenMystic", "" + ServerStorage.getInstance().getString(lang, "ClassName.38") + "");
		classList.put("DarkElvenWizard", "" + ServerStorage.getInstance().getString(lang, "ClassName.39") + "");
		classList.put("Spellhowler", "" + ServerStorage.getInstance().getString(lang, "ClassName.40") + "");
		classList.put("PhantomSummoner", "" + ServerStorage.getInstance().getString(lang, "ClassName.41") + "");
		classList.put("ShillienOracle", "" + ServerStorage.getInstance().getString(lang, "ClassName.42") + "");
		classList.put("ShillienElder", "" + ServerStorage.getInstance().getString(lang, "ClassName.43") + "");
		classList.put("OrcFighter", "" + ServerStorage.getInstance().getString(lang, "ClassName.44") + "");
		classList.put("OrcRaider", "" + ServerStorage.getInstance().getString(lang, "ClassName.45") + "");
		classList.put("Destroyer", "" + ServerStorage.getInstance().getString(lang, "ClassName.46") + "");
		classList.put("OrcMonk", "" + ServerStorage.getInstance().getString(lang, "ClassName.47") + "");
		classList.put("Tyrant", "" + ServerStorage.getInstance().getString(lang, "ClassName.48") + "");
		classList.put("OrcMystic", "" + ServerStorage.getInstance().getString(lang, "ClassName.49") + "");
		classList.put("OrcShaman", "" + ServerStorage.getInstance().getString(lang, "ClassName.50") + "");
		classList.put("Overlord", "" + ServerStorage.getInstance().getString(lang, "ClassName.51") + "");
		classList.put("Warcryer", "" + ServerStorage.getInstance().getString(lang, "ClassName.52") + "");
		classList.put("DwarvenFighter", "" + ServerStorage.getInstance().getString(lang, "ClassName.53") + "");
		classList.put("DwarvenScavenger", "" + ServerStorage.getInstance().getString(lang, "ClassName.54") + "");
		classList.put("BountyHunter", "" + ServerStorage.getInstance().getString(lang, "ClassName.55") + "");
		classList.put("DwarvenArtisan", "" + ServerStorage.getInstance().getString(lang, "ClassName.56") + "");
		classList.put("Warsmith", "" + ServerStorage.getInstance().getString(lang, "ClassName.57") + "");
		classList.put("duelist", "" + ServerStorage.getInstance().getString(lang, "ClassName.88") + "");
		classList.put("dreadnought", "" + ServerStorage.getInstance().getString(lang, "ClassName.89") + "");
		classList.put("phoenixKnight", "" + ServerStorage.getInstance().getString(lang, "ClassName.90") + "");
		classList.put("hellKnight", "" + ServerStorage.getInstance().getString(lang, "ClassName.91") + "");
		classList.put("sagittarius", "" + ServerStorage.getInstance().getString(lang, "ClassName.92") + "");
		classList.put("adventurer", "" + ServerStorage.getInstance().getString(lang, "ClassName.93") + "");
		classList.put("archmage", "" + ServerStorage.getInstance().getString(lang, "ClassName.94") + "");
		classList.put("soultaker", "" + ServerStorage.getInstance().getString(lang, "ClassName.95") + "");
		classList.put("arcanaLord", "" + ServerStorage.getInstance().getString(lang, "ClassName.96") + "");
		classList.put("cardinal", "" + ServerStorage.getInstance().getString(lang, "ClassName.97") + "");
		classList.put("hierophant", "" + ServerStorage.getInstance().getString(lang, "ClassName.98") + "");
		classList.put("evaTemplar", "" + ServerStorage.getInstance().getString(lang, "ClassName.99") + "");
		classList.put("swordMuse", "" + ServerStorage.getInstance().getString(lang, "ClassName.100") + "");
		classList.put("windRider", "" + ServerStorage.getInstance().getString(lang, "ClassName.101") + "");
		classList.put("moonlightSentinel", "" + ServerStorage.getInstance().getString(lang, "ClassName.102") + "");
		classList.put("mysticMuse", "" + ServerStorage.getInstance().getString(lang, "ClassName.103") + "");
		classList.put("elementalMaster", "" + ServerStorage.getInstance().getString(lang, "ClassName.104") + "");
		classList.put("evaSaint", "" + ServerStorage.getInstance().getString(lang, "ClassName.105") + "");
		classList.put("shillienTemplar", "" + ServerStorage.getInstance().getString(lang, "ClassName.106") + "");
		classList.put("spectralDancer", "" + ServerStorage.getInstance().getString(lang, "ClassName.107") + "");
		classList.put("ghostHunter", "" + ServerStorage.getInstance().getString(lang, "ClassName.108") + "");
		classList.put("ghostSentinel", "" + ServerStorage.getInstance().getString(lang, "ClassName.109") + "");
		classList.put("stormScreamer", "" + ServerStorage.getInstance().getString(lang, "ClassName.110") + "");
		classList.put("spectralMaster", "" + ServerStorage.getInstance().getString(lang, "ClassName.111") + "");
		classList.put("shillienSaint", "" + ServerStorage.getInstance().getString(lang, "ClassName.112") + "");
		classList.put("titan", "" + ServerStorage.getInstance().getString(lang, "ClassName.113") + "");
		classList.put("grandKhavatari", "" + ServerStorage.getInstance().getString(lang, "ClassName.114") + "");
		classList.put("dominator", "" + ServerStorage.getInstance().getString(lang, "ClassName.115") + "");
		classList.put("doomcryer", "" + ServerStorage.getInstance().getString(lang, "ClassName.116") + "");
		classList.put("fortuneSeeker", "" + ServerStorage.getInstance().getString(lang, "ClassName.117") + "");
		classList.put("maestro", "" + ServerStorage.getInstance().getString(lang, "ClassName.118") + "");
		classList.put("maleSoldier", "" + ServerStorage.getInstance().getString(lang, "ClassName.123") + "");
		classList.put("femaleSoldier", "" + ServerStorage.getInstance().getString(lang, "ClassName.124") + "");
		classList.put("trooper", "" + ServerStorage.getInstance().getString(lang, "ClassName.125") + "");
		classList.put("warder", "" + ServerStorage.getInstance().getString(lang, "ClassName.126") + "");
		classList.put("berserker", "" + ServerStorage.getInstance().getString(lang, "ClassName.127") + "");
		classList.put("maleSoulbreaker", "" + ServerStorage.getInstance().getString(lang, "ClassName.128") + "");
		classList.put("femaleSoulbreaker", "" + ServerStorage.getInstance().getString(lang, "ClassName.129") + "");
		classList.put("arbalester", "" + ServerStorage.getInstance().getString(lang, "ClassName.130") + "");
		classList.put("doombringer", "" + ServerStorage.getInstance().getString(lang, "ClassName.131") + "");
		classList.put("maleSoulhound", "" + ServerStorage.getInstance().getString(lang, "ClassName.132") + "");
		classList.put("femaleSoulhound", "" + ServerStorage.getInstance().getString(lang, "ClassName.133") + "");
		classList.put("trickster", "" + ServerStorage.getInstance().getString(lang, "ClassName.134") + "");
		classList.put("inspector", "" + ServerStorage.getInstance().getString(lang, "ClassName.135") + "");
		classList.put("judicator", "" + ServerStorage.getInstance().getString(lang, "ClassName.136") + "");
		
		return classList.get(name);
	}
	
	public static void setHtml(String text, Player self, Object... arg)
	{
		if (text == null || self == null)
		{
			return;
		}
		
		final NpcHtmlMessage msg = new NpcHtmlMessage(0);
		
		if (text.endsWith(".html") || text.endsWith(".htm"))
		{
			msg.setFile(self, text);
		}
		else
		{
			msg.setHtml(self, Strings.bbParse(text));
		}
		
		if (arg != null && arg.length % 2 == 0)
		{
			for (int i = 0; i < arg.length; i = +2)
			{
				msg.replace(String.valueOf(arg[i]), String.valueOf(arg[i + 1]));
			}
		}
		self.sendPacket(msg);
	}
	
	public static void setHtml(String text, boolean isWithReplace, Player self, Object... arg)
	{
		if (text == null || self == null)
		{
			return;
		}
		
		final NpcHtmlMessage msg = new NpcHtmlMessage(0);
		
		if (text.endsWith(".html") || text.endsWith(".htm"))
		{
			msg.setFile(self, self.getLang(), text, isWithReplace);
		}
		else
		{
			msg.setHtml(self, Strings.bbParse(text), isWithReplace);
		}
		
		if (arg != null && arg.length % 2 == 0)
		{
			for (int i = 0; i < arg.length; i = +2)
			{
				msg.replace(String.valueOf(arg[i]), String.valueOf(arg[i + 1]));
			}
		}
		self.sendPacket(msg);
	}
	
	public static boolean isFacing(Creature attacker, GameObject target, int maxAngle)
	{
		if (target == null)
		{
			return false;
		}
		double angleChar, angleTarget, angleDiff, maxAngleDiff;
		maxAngleDiff = maxAngle / 2;
		angleTarget = calculateAngleFrom(attacker, target);
		angleChar = convertHeadingToDegree(attacker.getHeading());
		angleDiff = angleChar - angleTarget;
		if (angleDiff <= -360 + maxAngleDiff)
		{
			angleDiff += 360;
		}
		if (angleDiff >= 360 - maxAngleDiff)
		{
			angleDiff -= 360;
		}
		if (Math.abs(angleDiff) <= maxAngleDiff)
		{
			return true;
		}
		return false;
	}
	
	public static Location getRandomPosition(ILocational loc, int minRange, int maxRange)
	{
		final int randomX = Rnd.get(minRange, maxRange);
		final int randomY = Rnd.get(minRange, maxRange);
		final double rndAngle = Math.toRadians(Rnd.get(360));
		final int newX = (int) (loc.getX() + (randomX * Math.cos(rndAngle)));
		final int newY = (int) (loc.getY() + (randomY * Math.sin(rndAngle)));
		return new Location(newX, newY, loc.getZ());
	}
	
	public static String getAmountFormat(long number, String lang)
	{
		if (number >= 1000000000)
		{
			final String line = forFinal("%.2f".formatted(number / 1000000000.0));
			return line + "" + ServerStorage.getInstance().getString(lang, "EpicDamageInfo.KKK");
		}
		if (number >= 1000000)
		{
			final String line = forFinal("%.2f".formatted(number / 1000000.0));
			return line + "" + ServerStorage.getInstance().getString(lang, "EpicDamageInfo.KK");
		}
		if (number >= 1000)
		{
			final String line = forFinal("%.2f".formatted(number / 1000.0));
			return line + "" + ServerStorage.getInstance().getString(lang, "EpicDamageInfo.K");
		}
		return String.valueOf(number);
	}
	
	private static String forFinal(String number)
	{
		final String[] info = number.split(",");
		if (info.length > 1 && info[1].equals("00"))
		{
			number = number.replace(",00", "");
		}
		
		if (info.length > 1 && info[1].equals("0"))
		{
			number = number.replace(",0", "");
		}
		
		number = number.replace(",", ".");
		if (number.endsWith("0"))
		{
			number = number.substring(0, number.length() - 1);
		}
		return number;
	}
	
	public static boolean isAvalibleWindows(List<Player> players, int hwidLimit, boolean isHwidCheck)
	{
		final Map<String, Integer> hwids = new HashMap<>();
		for (final var player : players)
		{
			if (player != null && player.getClient() != null)
			{
				final String info = isHwidCheck ? player.getHWID() : player.getIPAddress();
				if (hwids.containsKey(info))
				{
					final int value = hwids.get(info) + 1;
					if (value > hwidLimit)
					{
						hwids.clear();
						return false;
					}
				}
				else
				{
					hwids.put(info, 1);
				}
			}
		}
		hwids.clear();
		return true;
	}
	
	public static void getReward(Player player, int itemId, long amount)
	{
		if (itemId == -100)
		{
			if (player.getPcBangPoints() >= Config.MAX_PC_BANG_POINTS)
			{
				final var sm = SystemMessage.getSystemMessage(SystemMessageId.THE_MAXMIMUM_ACCUMULATION_ALLOWED_OF_PC_CAFE_POINTS_HAS_BEEN_EXCEEDED);
				player.sendPacket(sm);
				return;
			}
			
			if ((player.getPcBangPoints() + amount) > Config.MAX_PC_BANG_POINTS)
			{
				amount = Config.MAX_PC_BANG_POINTS - player.getPcBangPoints();
			}
			
			player.setPcBangPoints((int) (player.getPcBangPoints() + amount));
			final var smsg = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_ACQUIRED_S1_PC_CAFE_POINTS);
			smsg.addNumber((int) amount);
			player.sendPacket(smsg);
			player.sendPacket(new ExPCCafePointInfo(player.getPcBangPoints(), (int) amount, false, false, 1));
		}
		else if (itemId == -200)
		{
			player.getClan().addReputationScore((int) amount, true);
		}
		else if (itemId == -300)
		{
			player.setFame((int) (player.getFame() + amount));
			player.sendUserInfo();
		}
		else if (itemId > 0)
		{
			player.addItem("Reward", itemId, amount, player, true);
		}
	}
	
	public static double getCurrentValue(Player player, Stats stats)
	{
		switch (stats)
		{
			case STAT_STR :
				return player.getStat().getSTR();
			case STAT_CON :
				return player.getStat().getCON();
			case STAT_INT :
				return player.getStat().getINT();
			case STAT_DEX :
				return player.getStat().getDEX();
			case STAT_WIT :
				return player.getStat().getWIT();
			case STAT_MEN :
				return player.getStat().getMEN();
			case PVE_PHYSICAL_DMG :
			case PVE_PHYS_SKILL_DMG :
			case PVE_MAGICAL_DMG :
			case PVP_PHYSICAL_DMG :
			case PVP_MAGICAL_DMG :
			case PVP_PHYS_SKILL_DMG :
			case PVP_PHYSICAL_DEF :
			case PVP_MAGICAL_DEF :
			case PVP_PHYS_SKILL_DEF :
				return player.calcStat(stats, 1, null, null);
			case MAX_HP :
				return player.getStat().getMaxHp();
			case MAX_MP :
				return player.getStat().getMaxMp();
			case MAX_CP :
				return player.getStat().getMaxCp();
			case POWER_DEFENCE :
				return player.getStat().getPDef(null);
			case MAGIC_DEFENCE :
				return player.getStat().getMDef(null, null);
			case POWER_ATTACK :
				return player.getStat().getPAtk(null);
			case MAGIC_ATTACK :
				return player.getStat().getMAtk(null, null);
			case POWER_ATTACK_SPEED :
				return player.getStat().getPAtkSpd();
			case MAGIC_ATTACK_SPEED :
				return player.getStat().getMAtkSpd();
			case MOVE_SPEED :
				return player.getStat().getRunSpeed();
			case FIRE_RES :
				return player.getStat().calcStat(Stats.FIRE_RES, player.getTemplate().getBaseFireRes());
			case WIND_RES :
				return player.getStat().calcStat(Stats.FIRE_RES, player.getTemplate().getBaseWindRes());
			case WATER_RES :
				return player.getStat().calcStat(Stats.FIRE_RES, player.getTemplate().getBaseWaterRes());
			case EARTH_RES :
				return player.getStat().calcStat(Stats.FIRE_RES, player.getTemplate().getBaseEarthRes());
			case HOLY_RES :
				return player.getStat().calcStat(Stats.FIRE_RES, player.getTemplate().getBaseHolyRes());
			case DARK_RES :
				return player.getStat().calcStat(Stats.FIRE_RES, player.getTemplate().getBaseDarkRes());
			case ACCURACY_COMBAT :
				return player.getStat().getAccuracy();
		}
		return 0;
	}
	
	public static boolean isIntValue(Stats stats)
	{
		switch (stats)
		{
			case PVE_PHYSICAL_DMG :
			case PVE_PHYS_SKILL_DMG :
			case PVE_MAGICAL_DMG :
			case PVP_PHYSICAL_DMG :
			case PVP_MAGICAL_DMG :
			case PVP_PHYS_SKILL_DMG :
			case PVP_PHYSICAL_DEF :
			case PVP_MAGICAL_DEF :
			case PVP_PHYS_SKILL_DEF :
				return false;
		}
		return true;
	}
}