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
package gameserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nio.impl.SelectorConfig;

import l2e.commons.time.cron.SchedulingPattern;
import l2e.commons.util.Files;
import gameserver.data.parser.ExperienceParser;
import gameserver.model.base.AcquireSkillType;
import gameserver.model.service.academy.AcademyRewards;
import gameserver.model.service.autofarm.FarmSettings;
import gameserver.utils.FloodProtectorConfig;
import gameserver.utils.GameSettings;
import gameserver.utils.net.GameHostSettings;

public final class Config
{
	private static final Logger _log = LogManager.getLogger(Config.class);

	public static final String EOL = System.getProperty("line.separator");
	public static final int NCPUS = Runtime.getRuntime().availableProcessors();
	
	// --------------------------------------------------
	// L2J Eternity-World Property File Definitions
	// --------------------------------------------------
	// Game Server
	public static final String HITMAN_CONFIG = "./config/events/hitman_event.ini";
	public static final String UNDERGROUND_CONFIG_FILE = "./config/events/undergroundColiseum.ini";
	public static final String LEPRECHAUN_FILE = "./config/events/leprechaun_event.ini";
	public static final String AERIAL_CLEFT_FILE = "./config/events/aerialCleft.ini";
	public static final String FIGHT_EVENTS_FILE = "./config/events/fightEvents.ini";

	public static final String CHARACTER_CONFIG_FILE = "./config/main/character.ini";
	public static final String FEATURE_CONFIG_FILE = "./config/main/feature.ini";
	public static final String FORTSIEGE_CONFIGURATION_FILE = "./config/main/fortsiege.ini";
	public static final String GENERAL_CONFIG_FILE = "./config/main/general.ini";
	public static final String ID_CONFIG_FILE = "./config/main/idfactory.ini";
	public static final String NPC_CONFIG_FILE = "./config/main/npc.ini";
	public static final String PVP_CONFIG_FILE = "./config/main/pvp.ini";
	public static final String RATES_CONFIG_FILE = "./config/main/rates.ini";
	public static final String TW_CONFIGURATION_FILE = "./config/main/territorywar.ini";
	public static final String FLOOD_PROTECTOR_FILE = "./config/main/floodprotector.ini";
	public static final String MMO_CONFIG_FILE = "./config/main/mmo.ini";
	public static final String OLYMPIAD_CONFIG_FILE = "./config/main/olympiad.ini";
	public static final String GRANDBOSS_CONFIG_FILE = "./config/main/grandboss.ini";
	public static final String GRACIASEEDS_CONFIG_FILE = "./config/main/graciaSeeds.ini";
	public static final String CHAT_FILTER_FILE = "./config/main/chatfilter.txt";
	public static final String BROADCAST_CHAT_FILTER_FILE = "./config/main/broadcastfilter.txt";
	public static final String SECURITY_CONFIG_FILE = "./config/main/security.ini";
	public static final String CH_SIEGE_FILE = "./config/main/clanhallSiege.ini";
	public static final String LANGUAGE_FILE = "./config/main/language.ini";
	public static final String VOICE_CONFIG_FILE = "./config/main/voicecommands.ini";
	public static final String CUSTOM_FILE = "./config/main/custom.ini";
	public static final String PREMIUM_CONFIG_FILE = "./config/main/premiumAccount.ini";
	public static final String COMMUNITY_BOARD_CONFIG_FILE = "./config/main/communityBoard.ini";
	public static final String ENCHANT_CONFIG_FILE = "./config/main/enchant.ini";
	public static final String ITEM_MALL_CONFIG_FILE = "./config/main/itemMall.ini";
	public static final String GEO_CONFIG_FILE = "./config/main/geodata.ini";
	public static final String CHAT_CONFIG_FILE = "./config/main/chat.ini";
	public static final String PERSONAL_FILE = "./config/main/personal.ini";
	public static final String FORMULAS_FILE = "./config/main/formulas.ini";
	public static final String SCRIPTS_FILTER_FILE = "./config/main/scriptsfilter.ini";
	public static final String PCBANG_CONFIG_FILE = "./config/mods/pcPoints.ini";
	public static final String WEDDING_CONFIG_FILE = "./config/mods/wedding.ini";
	public static final String OFFLINE_TRADE_CONFIG_FILE = "./config/mods/offline_trade.ini";
	public static final String DOUBLE_SESSIONS_CONFIG_FILE = "./config/mods/doubleSessions.ini";
	public static final String OLY_ANTI_FEED_FILE = "./config/mods/olympiadAntiFeed.ini";
	public static final String ANTIBOT_CONFIG = "./config/mods/antiBot.ini";
	public static final String FAKES_CONFIG = "./config/mods/fakes/fakePlayers.ini";
	public static final String BOT_FILE = "./config/mods/botFunctions.ini";
	public static final String REVENGE_FILE = "./config/mods/revenge.ini";
	private static final String WEEKLY_TRADER_FILE = "./config/mods/weeklyTrader.ini";
	public static final String AUTO_FARM_FILE = "./config/mods/autoFarm.ini";
	public static final String SOMIK_FILE = "./config/mods/somikInteface.ini";

	public static final String CONFIGURATION_FILE = "./config/network/server.ini";

	public static final String IP_CONFIG_FILE = "./config/ipconfig.xml";

	// Personal Settings
	private final static HashMap<String, String> _personalConfigs = new HashMap<>();
	
	// mmocore settings
	public static SelectorConfig SELECTOR_CONFIG = new SelectorConfig();
	
	// Characters Settings
	public static boolean ALLOW_OPEN_CLOAK_SLOT;
	public static boolean ALLOW_UI_OPEN;
	public static boolean ALT_GAME_DELEVEL;
	public static boolean DECREASE_SKILL_LEVEL;
	public static boolean DECREASE_ENCHANT_SKILLS;
	public static int DEATH_PENALTY_CHANCE;
	public static boolean ENABLE_MODIFY_SKILL_DURATION;
	public static Map<Integer, Integer> SKILL_DURATION_LIST_SIMPLE;
	public static Map<Integer, Integer> SKILL_DURATION_LIST_PREMIUM;
	public static boolean ENABLE_MODIFY_SKILL_REUSE;
	public static Map<Integer, Integer> SKILL_REUSE_LIST;
	public static boolean AUTO_LEARN_SKILLS;
	public static int AUTO_LEARN_SKILLS_MAX_LEVEL;
	public static boolean AUTO_LEARN_FS_SKILLS;
	public static Set<AcquireSkillType> DISABLED_ITEMS_FOR_ACQUIRE_TYPES;
	public static boolean AUTO_LOOT_HERBS;
	public static int DEBUFFS_MAX_AMOUNT;
	public static int DEBUFFS_MAX_AMOUNT_PREMIUM;
	public static int BUFFS_MAX_AMOUNT;
	public static int BUFFS_MAX_AMOUNT_PREMIUM;
	public static int TRIGGERED_BUFFS_MAX_AMOUNT;
	public static int DANCES_MAX_AMOUNT;
	public static boolean DANCE_CANCEL_BUFF;
	public static boolean DANCE_CONSUME_ADDITIONAL_MP;
	public static boolean ALT_STORE_DANCES;
	public static boolean AUTO_LEARN_DIVINE_INSPIRATION;
	public static int PLAYER_FAKEDEATH_UP_PROTECTION;
	public static boolean SUBCLASS_STORE_SKILL_COOLTIME;
	public static boolean SUBCLASS_STORE_SKILL;
	public static boolean SUMMON_STORE_SKILL_COOLTIME;
	public static boolean ALLOW_ENTIRE_TREE;
	public static boolean ALTERNATE_CLASS_MASTER;
	public static boolean LIFE_CRYSTAL_NEEDED;
	public static boolean ES_SP_BOOK_NEEDED;
	public static boolean DIVINE_SP_BOOK_NEEDED;
	public static boolean COMPARE_SKILL_PRICE;
	public static boolean ALT_GAME_SUBCLASS_WITHOUT_QUESTS;
	public static int SUBCLASS_MIN_LEVEL;
	public static int CERT65_MIN_LEVEL;
	public static int CERT70_MIN_LEVEL;
	public static int CERT75_CLASS_MIN_LEVEL;
	public static int CERT75_MASTER_MIN_LEVEL;
	public static int CERT80_MIN_LEVEL;
	public static boolean ALT_GAME_SUBCLASS_EVERYWHERE;
	public static boolean ALT_GAME_SUBCLASS_ALL_CLASSES;
	public static boolean ALLOW_TRANSFORM_WITHOUT_QUEST;
	public static int FEE_DELETE_TRANSFER_SKILLS;
	public static int FEE_DELETE_SUBCLASS_SKILLS;
	public static boolean RESTORE_SERVITOR_ON_RECONNECT;
	public static boolean RESTORE_PET_ON_RECONNECT;
	public static boolean ALLOW_SUMMON_OWNER_ATTACK;
	public static boolean ALLOW_SUMMON_TELE_TO_LEADER;
	public static boolean ALLOW_PETS_RECHARGE_ONLY_COMBAT;
	public static int MAX_SUBCLASS;
	public static int BASE_SUBCLASS_LEVEL;
	public static int MAX_SUBCLASS_LEVEL;
	public static int PLAYER_MAXIMUM_LEVEL;
	public static int MAX_PVTSTORESELL_SLOTS_DWARF;
	public static int MAX_PVTSTORESELL_SLOTS_OTHER;
	public static int MAX_PVTSTOREBUY_SLOTS_DWARF;
	public static int MAX_PVTSTOREBUY_SLOTS_OTHER;
	public static int INVENTORY_MAXIMUM_NO_DWARF;
	public static int INVENTORY_MAXIMUM_DWARF;
	public static int INVENTORY_MAXIMUM_GM;
	public static int INVENTORY_MAXIMUM_QUEST_ITEMS;
	public static int WAREHOUSE_SLOTS_DWARF;
	public static int WAREHOUSE_SLOTS_NO_DWARF;
	public static int WAREHOUSE_SLOTS_CLAN;
	public static int ALT_FREIGHT_SLOTS;
	public static int MAX_AMOUNT_BY_MULTISELL;
	public static boolean ALLOW_MULTISELL_DEBUG;
	public static int ALT_FREIGHT_PRICE;
	public static int EXPAND_INVENTORY_LIMIT;
	public static int EXPAND_WAREHOUSE_LIMIT;
	public static int EXPAND_SELLSTORE_LIMIT;
	public static int EXPAND_BUYSTORE_LIMIT;
	public static int EXPAND_DWARFRECIPE_LIMIT;
	public static int EXPAND_COMMONRECIPE_LIMIT;
	public static int TELEPORT_BOOKMART_LIMIT;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_SHOP;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_TELEPORT;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_USE_GK;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_TRADE;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE;
	public static int MAX_PERSONAL_FAME_POINTS;
	public static int FORTRESS_ZONE_FAME_TASK_FREQUENCY;
	public static int FORTRESS_ZONE_FAME_AQUIRE_POINTS;
	public static int CASTLE_ZONE_FAME_TASK_FREQUENCY;
	public static int CASTLE_ZONE_FAME_AQUIRE_POINTS;
	public static boolean FAME_FOR_DEAD_PLAYERS;
	public static boolean IS_CRAFTING_ENABLED;
	public static boolean CRAFT_MASTERWORK;
	public static double CRAFT_DOUBLECRAFT_CHANCE;
	public static int DWARF_RECIPE_SLOTS;
	public static int COMMON_RECIPE_SLOTS;
	public static boolean ALT_GAME_CREATION;
	public static double ALT_GAME_CREATION_SPEED;
	public static double ALT_GAME_CREATION_XP_RATE;
	public static double ALT_GAME_CREATION_RARE_XPSP_RATE;
	public static double ALT_GAME_CREATION_SP_RATE;
	public static boolean ALT_BLACKSMITH_USE_RECIPES;
	public static int ALT_CLAN_DEFAULT_LEVEL;
	public static String ALT_CLAN_LEADER_DATE_CHANGE;
	public static boolean ALT_CLAN_LEADER_INSTANT_ACTIVATION;
	public static int ALT_CLAN_JOIN_DAYS;
	public static int ALT_CLAN_CREATE_DAYS;
	public static int ALT_CLAN_DISSOLVE_DAYS;
	public static int ALT_ALLY_JOIN_DAYS_WHEN_LEAVED;
	public static int ALT_ALLY_JOIN_DAYS_WHEN_DISMISSED;
	public static int ALT_ACCEPT_CLAN_DAYS_WHEN_DISMISSED;
	public static int ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED;
	public static int ALT_MAX_NUM_OF_CLANS_IN_ALLY;
	public static int ALT_CLAN_MEMBERS_FOR_WAR;
	public static boolean ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH;
	public static boolean REMOVE_CASTLE_CIRCLETS;
	public static int ALT_PARTY_RANGE;
	public static int PARTY_LIMIT;
	public static int ALT_PARTY_RANGE2;
	public static boolean ALT_LEAVE_PARTY_LEADER;
	public static long STARTING_ADENA;
	public static int STARTING_LEVEL;
	public static int STARTING_SP;
	public static long MAX_ADENA;
	public static boolean AUTO_LOOT;
	public static boolean AUTO_LOOT_RAIDS;
	public static int PLAYER_SPAWN_PROTECTION;
	public static List<Integer> SPAWN_PROTECTION_ALLOWED_ITEMS;
	public static int PLAYER_TELEPORT_PROTECTION;
	public static boolean RANDOM_RESPAWN_IN_TOWN_ENABLED;
	public static boolean OFFSET_ON_TELEPORT_ENABLED;
	public static int MAX_OFFSET_ON_TELEPORT;
	public static boolean ALLOW_SUMMON_TO_INSTANCE;
	public static boolean PETITIONING_ALLOWED;
	public static boolean NEW_PETITIONING_SYSTEM;
	public static int MAX_PETITIONS_PER_PLAYER;
	public static int MAX_PETITIONS_PENDING;
	public static boolean ALT_GAME_FREE_TELEPORT;
	public static int DELETE_DAYS;
	public static float ALT_GAME_EXPONENT_XP;
	public static float ALT_GAME_EXPONENT_SP;
	public static String PARTY_XP_CUTOFF_METHOD;
	public static double PARTY_XP_CUTOFF_PERCENT;
	public static int PARTY_XP_CUTOFF_LEVEL;
	public static int[][] PARTY_XP_CUTOFF_GAPS;
	public static int[] PARTY_XP_CUTOFF_GAP_PERCENTS;
	public static boolean DISABLE_TUTORIAL;
	public static boolean ENABLE_SPECIAL_TUTORIAL;
	public static boolean EXPERTISE_PENALTY;
	public static boolean STORE_RECIPE_SHOPLIST;
	public static boolean STORE_UI_SETTINGS;
	public static String[] FORBIDDEN_NAMES;
	public static boolean SILENCE_MODE_EXCLUDE;
	public static boolean ALT_VALIDATE_TRIGGER_SKILLS;
	public static boolean RESTORE_DISPEL_SKILLS;
	public static int RESTORE_DISPEL_SKILLS_TIME;
	public static boolean ALT_GAME_VIEWPLAYER;
	public static boolean TRADE_ONLY_IN_PEACE_ZONE;
	public static boolean ALLOW_TRADE_IN_ZONE;

	// --------------------------------------------------
	// Fortress Settings
	// --------------------------------------------------
	public static long FS_TELE_FEE_RATIO;
	public static int FS_TELE1_FEE;
	public static int FS_TELE2_FEE;
	public static long FS_MPREG_FEE_RATIO;
	public static int FS_MPREG1_FEE;
	public static int FS_MPREG2_FEE;
	public static long FS_HPREG_FEE_RATIO;
	public static int FS_HPREG1_FEE;
	public static int FS_HPREG2_FEE;
	public static long FS_EXPREG_FEE_RATIO;
	public static int FS_EXPREG1_FEE;
	public static int FS_EXPREG2_FEE;
	public static long FS_SUPPORT_FEE_RATIO;
	public static int FS_SUPPORT1_FEE;
	public static int FS_SUPPORT2_FEE;
	public static int FS_BLOOD_OATH_COUNT;
	public static int FS_UPDATE_FRQ;
	public static int FS_MAX_SUPPLY_LEVEL;
	public static int FS_FEE_FOR_CASTLE;
	public static int FS_MAX_OWN_TIME;

	// --------------------------------------------------
	// Feature Settings
	// --------------------------------------------------
	public static int TAKE_FORT_POINTS;
	public static int LOOSE_FORT_POINTS;
	public static int TAKE_CASTLE_POINTS;
	public static int LOOSE_CASTLE_POINTS;
	public static int CASTLE_DEFENDED_POINTS;
	public static int FESTIVAL_WIN_POINTS;
	public static int HERO_POINTS;
	public static int ROYAL_GUARD_COST;
	public static int KNIGHT_UNIT_COST;
	public static int KNIGHT_REINFORCE_COST;
	public static int BALLISTA_POINTS;
	public static int BLOODALLIANCE_POINTS;
	public static int BLOODOATH_POINTS;
	public static int KNIGHTSEPAULETTE_POINTS;
	public static int REPUTATION_SCORE_PER_KILL;
	public static int JOIN_ACADEMY_MIN_REP_SCORE;
	public static int JOIN_ACADEMY_MAX_REP_SCORE;
	public static int RAID_RANKING_1ST;
	public static int RAID_RANKING_2ND;
	public static int RAID_RANKING_3RD;
	public static int RAID_RANKING_4TH;
	public static int RAID_RANKING_5TH;
	public static int RAID_RANKING_6TH;
	public static int RAID_RANKING_7TH;
	public static int RAID_RANKING_8TH;
	public static int RAID_RANKING_9TH;
	public static int RAID_RANKING_10TH;
	public static int RAID_RANKING_UP_TO_50TH;
	public static int RAID_RANKING_UP_TO_100TH;
	public static int RANK_CLASS_FOR_CC;
	public static boolean ALLOW_WYVERN_ALWAYS;
	public static boolean ALLOW_WYVERN_DURING_SIEGE;
	public static boolean STOP_WAR_PVP;

	// --------------------------------------------------
	// General Settings
	// --------------------------------------------------
	public static int[] ALLOW_СREATE_RACES;
	public static boolean ALLOW_PRE_START_SYSTEM;
	public static long PRE_START_PATTERN;
	public static String SERVER_STAGE;
	public static boolean EVERYBODY_HAS_ADMIN_RIGHTS;
	public static int DEFAULT_ACCSESS_LEVEL;
	public static boolean LOG_CHAT;
	public static boolean LOG_ITEMS;
	public static boolean SERVICE_LOGS;
	public static boolean LOG_ITEM_ENCHANTS;
	public static boolean LOG_SKILL_ENCHANTS;
	public static boolean GMAUDIT;
	public static boolean LOG_GAME_DAMAGE;
	public static int LOG_GAME_DAMAGE_THRESHOLD;
	public static boolean DEBUG;
	public static boolean DEBUG_SPAWN;
	public static boolean TIME_ZONE_DEBUG;
	public static boolean SERVER_PACKET_HANDLER_DEBUG;
	public static boolean CLIENT_PACKET_HANDLER_DEBUG;
	public static boolean DEVELOPER;
	public static boolean ALT_DEV_NO_HANDLERS;
	public static boolean ALT_DEV_NO_SCRIPTS;
	public static boolean ALT_DEV_NO_SPAWNS;
	public static boolean ALT_CHEST_NO_SPAWNS;
	public static int SCHEDULED_THREAD_POOL_SIZE;
	public static int EXECUTOR_THREAD_POOL_SIZE;
	public static boolean ALLOW_DISCARDITEM;
	public static List<Integer> LIST_DISCARDITEM_ITEMS;
	public static int AUTODESTROY_ITEM_AFTER;
	public static int HERB_AUTO_DESTROY_TIME;
	public static List<Integer> LIST_PROTECTED_ITEMS;
	public static boolean DATABASE_CLEAN_UP;
	public static int CHAR_STORE_INTERVAL;
	public static int CHAR_PREMIUM_ITEM_INTERVAL;
	public static boolean LAZY_ITEMS_UPDATE;
	public static boolean UPDATE_ITEMS_ON_CHAR_STORE;
	public static boolean DESTROY_DROPPED_PLAYER_ITEM;
	public static boolean DESTROY_EQUIPABLE_PLAYER_ITEM;
	public static boolean AUTODELETE_INVALID_QUEST_DATA;
	public static boolean PRECISE_DROP_CALCULATION;
	public static boolean MULTIPLE_ITEM_DROP;
	public static boolean FORCE_INVENTORY_UPDATE;
	public static boolean ALLOW_CACHE;
	public static boolean CACHE_CHAR_NAMES;
	public static boolean ENABLE_FALLING_DAMAGE;
	public static int PEACE_ZONE_MODE;
	public static boolean ALLOW_WAREHOUSE;
	public static boolean WAREHOUSE_CACHE;
	public static int WAREHOUSE_CACHE_TIME;
	public static boolean ALLOW_REFUND;
	public static boolean ALLOW_MAIL;
	public static int MAIL_MIN_LEVEL;
	public static int MAIL_EXPIRATION;
	public static int MAIL_COND_EXPIRATION;
	public static boolean ALLOW_ATTACHMENTS;
	public static boolean ALLOW_WEAR;
	public static int WEAR_DELAY;
	public static int WEAR_PRICE;
	public static boolean ALLOW_LOTTERY;
	public static boolean ALLOW_RACE;
	public static boolean ALLOW_WATER;
	public static boolean ALLOW_RENTPET;
	public static boolean ALLOWFISHING;
	public static Map<Integer, String> FISHING_REWARDS;
	public static boolean ALLOW_BOAT;
	public static int BOAT_BROADCAST_RADIUS;
	public static boolean ALLOW_CURSED_WEAPONS;
	public static boolean ALLOW_MANOR;
	public static boolean ALLOW_PET_WALKERS;
	public static boolean SERVER_NEWS;
	public static boolean ALLOW_TRAINING_BATTLES;
	public static String ALT_OLY_TRAINING_TIME;
	public static int ALT_OLY_TPERIOD;
	public static String ALT_OLY_START_TIME;
	public static String OLYMPIAD_PERIOD;
	public static boolean ALLOW_REG_WITHOUT_NOBLE;
	public static int[] OLY_REG_PARAM = new int[2];
	public static boolean ALLOW_STOP_ALL_CUBICS;
	public static boolean ALLOW_UNSUMMON_ALL;
	public static boolean OLY_PRINT_CLASS_OPPONENT;
	public static boolean ALLOW_WINNER_ANNOUNCE;
	public static boolean AUTO_GET_HERO;
	public static boolean CHECK_CLASS_SKILLS;
	public static boolean ALLOW_PRINT_OLY_INFO;
	public static boolean ALLOW_OLY_HIT_SUMMON;
	public static boolean ALLOW_OLY_FAST_INVITE;
	public static boolean ALLOW_RESTART_AT_OLY;
	public static boolean OLY_PAUSE_BATTLES_AT_SIEGES;
	public static long ALT_OLY_CPERIOD;
	public static long ALT_OLY_BATTLE;
	public static int ALT_OLY_TELE_TO_TOWN;
	public static String OLYMPIAD_WEEKLY_PERIOD;
	public static long ALT_OLY_VPERIOD;
	public static int ALT_OLY_START_POINTS;
	public static int ALT_OLY_WEEKLY_POINTS;
	public static int ALT_OLY_DAILY_POINTS;
	public static int ALT_OLY_CLASSED;
	public static int ALT_OLY_NONCLASSED;
	public static int ALT_OLY_TEAMS;
	public static int ALT_OLY_REG_DISPLAY;
	public static int[][] ALT_OLY_CLASSED_REWARD;
	public static int[][] ALT_OLY_CLASSED_LOSE_REWARD;
	public static int[][] ALT_OLY_NONCLASSED_REWARD;
	public static int[][] ALT_OLY_NONCLASSED_LOSE_REWARD;
	public static int[][] ALT_OLY_TEAM_REWARD;
	public static int[][] ALT_OLY_TEAM_LOSE_REWARD;
	public static int ALT_OLY_COMP_RITEM;
	public static int ALT_OLY_MIN_MATCHES;
	public static int ALT_OLY_GP_PER_POINT;
	public static int ALT_OLY_HERO_POINTS;
	public static int ALT_OLY_RANK1_POINTS;
	public static int ALT_OLY_RANK2_POINTS;
	public static int ALT_OLY_RANK3_POINTS;
	public static int ALT_OLY_RANK4_POINTS;
	public static int ALT_OLY_RANK5_POINTS;
	public static int ALT_OLY_MAX_POINTS;
	public static int ALT_OLY_DIVIDER_CLASSED;
	public static int ALT_OLY_DIVIDER_NON_CLASSED;
	public static int ALT_OLY_MAX_WEEKLY_MATCHES;
	public static int ALT_OLY_MAX_WEEKLY_MATCHES_NON_CLASSED;
	public static int ALT_OLY_MAX_WEEKLY_MATCHES_CLASSED;
	public static int ALT_OLY_MAX_WEEKLY_MATCHES_TEAM;
	public static boolean ALT_OLY_SHOW_MONTHLY_WINNERS;
	public static boolean ALT_OLY_ANNOUNCE_GAMES;
	public static List<Integer> LIST_OLY_RESTRICTED_ITEMS;
	public static int ALT_OLY_WEAPON_ENCHANT_LIMIT;
	public static int ALT_OLY_ARMOR_ENCHANT_LIMIT;
	public static int ALT_OLY_ACCESSORY_ENCHANT_LIMIT;
	public static int ALT_OLY_WAIT_TIME;
	public static boolean BLOCK_VISUAL_OLY;
	public static boolean ALLOW_SOULHOOD_DOUBLE;
	public static boolean ALLOW_HIDE_OLY_POINTS;
	public static int ALT_MANOR_REFRESH_TIME;
	public static int ALT_MANOR_REFRESH_MIN;
	public static int ALT_MANOR_MAINTENANCE_MIN;
	public static int ALT_MANOR_APPROVE_TIME;
	public static int ALT_MANOR_APPROVE_MIN;
	public static boolean ALT_MANOR_SAVE_ALL_ACTIONS;
	public static long ALT_MANOR_SAVE_PERIOD_RATE;
	public static long ALT_LOTTERY_PRIZE;
	public static long ALT_LOTTERY_TICKET_PRICE;
	public static float ALT_LOTTERY_5_NUMBER_RATE;
	public static float ALT_LOTTERY_4_NUMBER_RATE;
	public static float ALT_LOTTERY_3_NUMBER_RATE;
	public static long ALT_LOTTERY_2_AND_1_NUMBER_PRIZE;
	public static boolean ALT_ITEM_AUCTION_ENABLED;
	public static boolean ALLOW_ITEM_AUCTION_ANNOUNCE;
	public static int ALT_ITEM_AUCTION_EXPIRED_AFTER;
	public static long ALT_ITEM_AUCTION_TIME_EXTENDS_ON_BID;
	public static int FS_TIME_ATTACK;
	public static int FS_TIME_COOLDOWN;
	public static int FS_TIME_ENTRY;
	public static int FS_TIME_WARMUP;
	public static int FS_PARTY_MEMBER_COUNT;
	public static boolean SAVE_GMSPAWN_ON_CUSTOM;
	public static int ALT_BIRTHDAY_GIFT;
	public static String ALT_BIRTHDAY_MAIL_SUBJECT;
	public static String ALT_BIRTHDAY_MAIL_TEXT;
	public static boolean ENABLE_BLOCK_CHECKER_EVENT;
	public static int MIN_BLOCK_CHECKER_TEAM_MEMBERS;
	public static boolean HBCE_FAIR_PLAY;
	public static boolean CLEAR_CREST_CACHE;
	public static int NORMAL_ENCHANT_COST_MULTIPLIER;
	public static int SAFE_ENCHANT_COST_MULTIPLIER;

	// --------------------------------------------------
	// FloodProtector Settings
	// --------------------------------------------------
	public static final List<FloodProtectorConfig> FLOOD_PROTECTORS = new ArrayList<>();

	// --------------------------------------------------
	// Mods Settings
	// --------------------------------------------------
	public static boolean ALLOW_WEDDING;
	public static int WEDDING_PRICE;
	public static boolean WEDDING_PUNISH_INFIDELITY;
	public static boolean WEDDING_TELEPORT;
	public static int WEDDING_TELEPORT_PRICE;
	public static int WEDDING_TELEPORT_DURATION;
	public static boolean WEDDING_FORMALWEAR;
	public static int WEDDING_DIVORCE_COSTS;
	public static int[] WEDDING_REWARD = new int[2];
	public static boolean HELLBOUND_STATUS;
	public static boolean BANKING_SYSTEM_ENABLED;
	public static int BANKING_SYSTEM_GOLDBARS;
	public static int BANKING_SYSTEM_ADENA;
	public static boolean ENABLE_WAREHOUSESORTING_CLAN;
	public static boolean ENABLE_WAREHOUSESORTING_PRIVATE;
	public static boolean OFFLINE_TRADE_ENABLE;
	public static int OFFLINE_TRADE_MIN_LVL;
	public static int[] OFFLINE_MODE_PRICE = new int[2];
	public static int OFFLINE_MODE_TIME;
	public static boolean OFFLINE_CRAFT_ENABLE;
	public static boolean OFFLINE_MODE_IN_PEACE_ZONE;
	public static boolean OFFLINE_MODE_NO_DAMAGE;
	public static boolean RESTORE_OFFLINERS;
	public static int OFFLINE_MAX_DAYS;
	public static boolean OFFLINE_DISCONNECT_FINISHED;
	public static boolean OFFLINE_SET_NAME_COLOR;
	public static boolean OFFLINE_SET_VISUAL_EFFECT;
	public static int OFFLINE_NAME_COLOR;
	public static boolean OFFLINE_FAME;
	public static boolean ENABLE_MANA_POTIONS_SUPPORT;
	public static boolean DISPLAY_SERVER_TIME;
	public static boolean WELCOME_MESSAGE_ENABLED;
	public static String WELCOME_MESSAGE_TEXT;
	public static int WELCOME_MESSAGE_TIME;
	public static boolean ANNOUNCE_PK_PVP;
	public static boolean ANNOUNCE_PK_PVP_NORMAL_MESSAGE;
	public static String ANNOUNCE_PK_MSG;
	public static String ANNOUNCE_PVP_MSG;
	public static boolean CHAT_ADMIN;
	public static boolean DEBUG_VOICE_COMMAND;
	public static boolean DOUBLE_SESSIONS_ENABLE;
	public static boolean DOUBLE_SESSIONS_HWIDS;
	public static boolean DOUBLE_SESSIONS_DISCONNECTED;
	public static int DOUBLE_SESSIONS_CHECK_MAX_PLAYERS;
	public static boolean DOUBLE_SESSIONS_CONSIDER_OFFLINE_TRADERS;
	public static int DOUBLE_SESSIONS_CHECK_MAX_OLYMPIAD_PARTICIPANTS;
	public static int DOUBLE_SESSIONS_CHECK_MAX_EVENT_PARTICIPANTS;
	public static boolean ALLOW_CHANGE_PASSWORD;
	public static Pattern GENERAL_BYPASS_ENCODE_IGNORE;
	public static Pattern REUSABLE_BYPASS_ENCODE;
	public static Set<String> EXACT_BYPASS_ENCODE_IGNORE;
	public static Set<String> INITIAL_BYPASS_ENCODE_IGNORE;
	public static Pattern BYPASS_TEMPLATE = Pattern.compile("\"(bypass +[-h ]*)(.+?)\"");

	// --------------------------------------------------
	// NPC Settings
	// --------------------------------------------------
	public static long NPC_ANIMATION_INTERVAL;
	public static double NPC_ANIMATION_CHANCE;
	public static double MONSTER_ANIMATION_CHANCE;
	public static long MIN_MONSTER_ANIMATION;
	public static long MAX_MONSTER_ANIMATION;
	public static List<String> DISABLE_NPC_BYPASSES;
	public static String NPC_SHIFT_COMMAND;
	public static int NPC_AI_TIME_TASK;
	public static int NPC_AI_FACTION_TASK;
	public static int NPC_AI_RNDWALK_CHANCE;
	public static int SOULSHOT_CHANCE;
	public static int SPIRITSHOT_CHANCE;
	public static int PLAYER_MOVEMENT_BLOCK_TIME;
	public static boolean ANNOUNCE_MAMMON_SPAWN;
	public static boolean ALT_MOB_AGRO_IN_PEACEZONE;
	public static boolean ALT_ATTACKABLE_NPCS;
	public static boolean ALT_GAME_VIEWNPC;
	public static int MAX_DRIFT_RANGE;
	public static boolean DEEPBLUE_DROP_RULES;
	public static int DEEPBLUE_DROP_MAXDIFF;
	public static int DEEPBLUE_DROP_RAID_MAXDIFF;
	public static boolean SHOW_NPC_SERVER_NAME;
	public static boolean SHOW_NPC_SERVER_TITLE;
	public static boolean SHOW_NPC_LVL;
	public static boolean SHOW_CREST_WITHOUT_QUEST;
	public static boolean ENABLE_RANDOM_ENCHANT_EFFECT;
	public static int NPC_DEAD_TIME_TASK;
	public static int NPC_DECAY_TIME;
	public static int RAID_BOSS_DECAY_TIME;
	public static int SPOILED_DECAY_TIME;
	public static int MAX_SWEEPER_TIME;
	public static boolean GUARD_ATTACK_AGGRO_MOB;
	public static boolean ALLOW_WYVERN_UPGRADER;
	public static List<Integer> LIST_PET_RENT_NPC;
	public static double RAID_HP_REGEN_MULTIPLIER;
	public static double RAID_MP_REGEN_MULTIPLIER;
	public static double RAID_MINION_RESPAWN_TIMER;
	public static Map<Integer, Integer> MINIONS_RESPAWN_TIME;
	public static float RAID_MIN_RESPAWN_MULTIPLIER;
	public static float RAID_MAX_RESPAWN_MULTIPLIER;
	public static boolean RAID_DISABLE_CURSE;
	public static int INVENTORY_MAXIMUM_PET;
	public static double PET_HP_REGEN_MULTIPLIER;
	public static double PET_MP_REGEN_MULTIPLIER;
	public static boolean LAKFI_ENABLED;
	public static int TIME_CHANGE_SPAWN;
	public static long MIN_ADENA_TO_EAT;
	public static int TIME_IF_NOT_FEED;
	public static int INTERVAL_EATING;
	public static int[] NPC_BLOCK_SHIFT_LIST;
	public static boolean EPAULETTE_ONLY_FOR_REG;
	public static boolean EPAULETTE_WITHOUT_PENALTY;
	
	public static boolean DRAGON_VORTEX_UNLIMITED_SPAWN;
	public static boolean ALLOW_RAIDBOSS_CHANCE_DEBUFF;
	public static double RAIDBOSS_CHANCE_DEBUFF;
	public static boolean ALLOW_GRANDBOSS_CHANCE_DEBUFF;
	public static double GRANDBOSS_CHANCE_DEBUFF;
	public static int[] RAIDBOSS_DEBUFF_SPECIAL;
	public static int[] GRANDBOSS_DEBUFF_SPECIAL;
	public static double RAIDBOSS_CHANCE_DEBUFF_SPECIAL;
	public static double GRANDBOSS_CHANCE_DEBUFF_SPECIAL;
	public static boolean ALWAYS_TELEPORT_HOME;
	public static int MAX_PURSUE_RANGE;
	public static int MAX_PURSUE_RANGE_RAID;
	public static boolean CALC_NPC_STATS;
	public static boolean CALC_RAID_STATS;
	public static boolean CALC_NPC_DEBUFFS_BY_STATS;
	public static boolean CALC_RAID_DEBUFFS_BY_STATS;
	public static boolean MONSTER_RACE_TP_TO_TOWN;
	public static double SKILLS_MOB_CHANCE;
	public static boolean ALLOW_NPC_LVL_MOD;
	public static boolean ALLOW_SUMMON_LVL_MOD;
	public static double PATK_HATE_MOD;
	public static double MATK_HATE_MOD;
	public static double PET_HATE_MOD;
	public static int[] RAIDBOSS_ANNOUNCE_LIST;
	public static int[] GRANDBOSS_ANNOUNCE_LIST;
	public static int[] RAIDBOSS_DEAD_ANNOUNCE_LIST;
	public static int[] GRANDBOSS_DEAD_ANNOUNCE_LIST;
	public static Map<Integer, Integer> RAIDBOSS_PRE_ANNOUNCE_LIST;
	public static Map<Integer, Integer> EPICBOSS_PRE_ANNOUNCE_LIST;
	public static boolean ALLOW_DAMAGE_LIMIT;
	public static int NPC_DROP_PROTECTION;
	public static int RAID_DROP_PROTECTION;
	public static double SPAWN_MULTIPLIER;
	public static double RESPAWN_MULTIPLIER;
	public static long DRAGON_MIGRATION_PERIOD;
	public static double DRAGON_MIGRATION_CHANCE;
	
	// --------------------------------------------------
	// PvP Settings
	// --------------------------------------------------
	public static int KARMA_MIN_KARMA;
	public static int KARMA_MAX_KARMA;
	public static int KARMA_XP_DIVIDER;
	public static int KARMA_LOST_BASE;
	public static boolean KARMA_DROP_GM;
	public static boolean KARMA_AWARD_PK_KILL;
	public static int KARMA_PK_LIMIT;
	public static int[] KARMA_LIST_NONDROPPABLE_PET_ITEMS;
	public static int[] KARMA_LIST_NONDROPPABLE_ITEMS;
	public static int DISABLE_ATTACK_IF_LVL_DIFFERENCE_OVER;

	// Premium Accounts Settings
	public static boolean USE_PREMIUMSERVICE;
	public static boolean SERVICES_WITHOUT_PREMIUM;
	public static boolean PREMIUMSERVICE_DOUBLE;
	public static boolean AUTO_GIVE_PREMIUM;
	public static int GIVE_PREMIUM_ID;
	public static boolean PREMIUM_PARTY_RATE;
	
	// --------------------------------------------------
	// Rate Settings
	// --------------------------------------------------
	public static int MAX_DROP_ITEMS_FROM_ONE_GROUP;
	public static int MAX_SPOIL_ITEMS_FROM_ONE_GROUP;
	public static int MAX_DROP_ITEMS_FROM_ONE_GROUP_RAIDS;
	public static double GROUP_CHANCE_MODIFIER;
	public static double RAID_GROUP_CHANCE_MOD;
	public static double RAID_ITEM_CHANCE_MOD;
	public static double[] RATE_XP_BY_LVL;
	public static double[] RATE_SP_BY_LVL;
	public static double[] RATE_RAID_XP_BY_LVL;
	public static double[] RATE_RAID_SP_BY_LVL;
	public static double RATE_PARTY_XP;
	public static double RATE_PARTY_SP;
	public static double RATE_DROP_ADENA;
	public static double RATE_DROP_ITEMS;
	public static double RATE_DROP_SPOIL;
	public static double RATE_DROP_RAIDBOSS;
	public static double RATE_DROP_EPICBOSS;
	public static double RATE_DROP_SIEGE_GUARD;
	public static boolean NO_RATE_EQUIPMENT;
	public static boolean ALLOW_MODIFIER_FOR_DROP;
	public static boolean ALLOW_MODIFIER_FOR_RAIDS;
	public static boolean ALLOW_MODIFIER_FOR_SPOIL;
	public static boolean NO_RATE_KEY_MATERIAL;
	public static boolean NO_RATE_RECIPES;
	public static int[] NO_RATE_ITEMS;
	public static boolean NO_RATE_GROUPS;
	public static double RATE_DROP_FISHING;
	public static double RATE_CHANCE_GROUP_DROP_ITEMS;
	public static double RATE_CHANCE_DROP_ITEMS;
	public static double RATE_CHANCE_ATTRIBUTE;
	public static double RATE_CHANCE_COMMON;
	public static double RATE_CHANCE_DROP_HERBS;
	public static double RATE_CHANCE_SPOIL;
	public static double RATE_CHANCE_SPOIL_WEAPON_ARMOR_ACCESSORY;
	public static double RATE_CHANCE_DROP_WEAPON_ARMOR_ACCESSORY;
	public static double RATE_CHANCE_DROP_EPOLET;
	public static double RATE_CONSUMABLE_COST;
	public static double RATE_EXTRACTABLE;
	public static double RATE_HB_TRUST_INCREASE;
	public static double RATE_HB_TRUST_DECREASE;
	public static boolean RATE_QUEST_REWARD_USE_MULTIPLIERS;
	public static double ADENA_FIXED_CHANCE;
	public static double RATE_DROP_MANOR;
	public static float RATE_QUEST_DROP;
	public static float RATE_QUEST_REWARD;
	public static float RATE_QUEST_REWARD_XP;
	public static float RATE_QUEST_REWARD_SP;
	public static float RATE_QUEST_REWARD_ADENA;
	public static float RATE_QUEST_REWARD_POTION;
	public static float RATE_QUEST_REWARD_SCROLL;
	public static float RATE_QUEST_REWARD_RECIPE;
	public static float RATE_QUEST_REWARD_MATERIAL;
	public static float RATE_KARMA_EXP_LOST;
	public static float RATE_SIEGE_GUARDS_PRICE;
	public static int PLAYER_DROP_LIMIT;
	public static int PLAYER_RATE_DROP;
	public static int PLAYER_RATE_DROP_ITEM;
	public static int PLAYER_RATE_DROP_EQUIP;
	public static int PLAYER_RATE_DROP_EQUIP_WEAPON;
	public static float PET_XP_RATE;
	public static int PET_FOOD_RATE;
	public static float SINEATER_XP_RATE;
	public static int KARMA_DROP_LIMIT;
	public static int KARMA_RATE_DROP;
	public static int KARMA_RATE_DROP_ITEM;
	public static int KARMA_RATE_DROP_EQUIP;
	public static int KARMA_RATE_DROP_EQUIP_WEAPON;
	public static List<Integer> DISABLE_ITEM_DROP_LIST;
	public static int RATE_TALISMAN_MULTIPLIER;
	public static int RATE_TALISMAN_ITEM_MULTIPLIER;
	public static List<Integer> NO_DROP_ITEMS_FOR_SWEEP;
	public static List<Integer> ALLOW_ONLY_THESE_DROP_ITEMS_ID;
	public static double RATE_NOBLE_STONES_COUNT_MIN;
	public static double RATE_LIFE_STONES_COUNT_MIN;
	public static double RATE_ENCHANT_SCROLLS_COUNT_MIN;
	public static double RATE_FORGOTTEN_SCROLLS_COUNT_MIN;
	public static double RATE_KEY_MATHETIRALS_COUNT_MIN;
	public static double RATE_RECEPIES_COUNT_MIN;
	public static double RATE_BELTS_COUNT_MIN;
	public static double RATE_BRACELETS_COUNT_MIN;
	public static double RATE_CLOAKS_COUNT_MIN;
	public static double RATE_CODEX_BOOKS_COUNT_MIN;
	public static double RATE_ATTRIBUTE_STONES_COUNT_MIN;
	public static double RATE_ATTRIBUTE_CRYSTALS_COUNT_MIN;
	public static double RATE_ATTRIBUTE_JEWELS_COUNT_MIN;
	public static double RATE_ATTRIBUTE_ENERGY_COUNT_MIN;
	public static double RATE_WEAPONS_COUNT_MIN;
	public static double RATE_ARMOR_COUNT_MIN;
	public static double RATE_ACCESSORY_COUNT_MIN;
	public static double RATE_SEAL_STONES_COUNT_MIN;
	public static double RATE_NOBLE_STONES_COUNT_MAX;
	public static double RATE_LIFE_STONES_COUNT_MAX;
	public static double RATE_ENCHANT_SCROLLS_COUNT_MAX;
	public static double RATE_FORGOTTEN_SCROLLS_COUNT_MAX;
	public static double RATE_KEY_MATHETIRALS_COUNT_MAX;
	public static double RATE_RECEPIES_COUNT_MAX;
	public static double RATE_BELTS_COUNT_MAX;
	public static double RATE_BRACELETS_COUNT_MAX;
	public static double RATE_CLOAKS_COUNT_MAX;
	public static double RATE_CODEX_BOOKS_COUNT_MAX;
	public static double RATE_ATTRIBUTE_STONES_COUNT_MAX;
	public static double RATE_ATTRIBUTE_CRYSTALS_COUNT_MAX;
	public static double RATE_ATTRIBUTE_JEWELS_COUNT_MAX;
	public static double RATE_ATTRIBUTE_ENERGY_COUNT_MAX;
	public static double RATE_WEAPONS_COUNT_MAX;
	public static double RATE_ARMOR_COUNT_MAX;
	public static double RATE_ACCESSORY_COUNT_MAX;
	public static double RATE_SEAL_STONES_COUNT_MAX;
	public static Map<Integer, Double> MAX_AMOUNT_CORRECTOR;

	// --------------------------------------------------
	// Seven Signs Settings
	// --------------------------------------------------
	public static boolean ALLOW_CHECK_SEVEN_SIGN_STATUS;
	public static boolean ALT_GAME_CASTLE_DAWN;
	public static boolean ALT_GAME_CASTLE_DUSK;
	public static boolean ALT_GAME_REQUIRE_CLAN_CASTLE;
	public static int ALT_FESTIVAL_MIN_PLAYER;
	public static int ALT_MAXIMUM_PLAYER_CONTRIB;
	public static long ALT_FESTIVAL_MANAGER_START;
	public static long ALT_FESTIVAL_LENGTH;
	public static long ALT_FESTIVAL_CYCLE_LENGTH;
	public static long ALT_FESTIVAL_FIRST_SPAWN;
	public static long ALT_FESTIVAL_FIRST_SWARM;
	public static long ALT_FESTIVAL_SECOND_SPAWN;
	public static long ALT_FESTIVAL_SECOND_SWARM;
	public static long ALT_FESTIVAL_CHEST_SPAWN;
	public static double ALT_SIEGE_DAWN_GATES_PDEF_MULT;
	public static double ALT_SIEGE_DUSK_GATES_PDEF_MULT;
	public static double ALT_SIEGE_DAWN_GATES_MDEF_MULT;
	public static double ALT_SIEGE_DUSK_GATES_MDEF_MULT;
	public static boolean ALT_STRICT_SEVENSIGNS;
	public static boolean ALT_SEVENSIGNS_LAZY_UPDATE;
	public static int SSQ_DAWN_TICKET_QUANTITY;
	public static int SSQ_DAWN_TICKET_PRICE;
	public static int SSQ_DAWN_TICKET_BUNDLE;
	public static int SSQ_MANORS_AGREEMENT_ID;
	public static int SSQ_JOIN_DAWN_ADENA_FEE;

	// --------------------------------------------------
	// Server Settings
	// --------------------------------------------------
	public static boolean c = true;
	public static int AI_TASK_MANAGER_COUNT;
	public static int EFFECT_TASK_MANAGER_COUNT;
	public static boolean ALLOW_MULILOGIN;
	public static String USER_NAME;
	public static String USER_KEY;
	public static String PROTECTION;
	public static String DATABASE_DRIVER;
	public static String DATABASE_URL;
	public static String DATABASE_LOGIN;
	public static String DATABASE_PASSWORD;
	public static int DATABASE_MAX_CONNECTIONS;
	public static int DATABASE_MAX_IDLE_TIMEOUT;
	public static int DATABASE_CONNECTION_LIFE_TIME;
	public static int DATABASE_CONNECTION_TIMEOUT;
	public static int MAXIMUM_ONLINE_USERS;
	public static String CNAME_TEMPLATE;
	public static String PET_NAME_TEMPLATE;
	public static String CLAN_NAME_TEMPLATE;
	public static int MAX_CHARACTERS_NUMBER_PER_ACCOUNT;
	public static File DATAPACK_ROOT;
	public static int REQUEST_ID;
	public static int PORT_GAME;
	public static boolean RESERVE_HOST_ON_LOGIN = false;
	public static List<Integer> PROTOCOL_LIST;
	public static boolean SERVER_LIST_BRACKET;
	public static boolean SERVER_LIST_IS_PVP;
	public static int SERVER_LIST_TYPE;
	public static int SERVER_LIST_AGE;
	public static boolean SERVER_GMONLY;
	public static boolean ALLOW_BACKUP_DATABASE;
	public static long USER_INFO_INTERVAL;
	public static long BROADCAST_CHAR_INFO_INTERVAL;
	public static long BROADCAST_STATUS_UPDATE_INTERVAL;
	public static long USER_STATS_UPDATE_INTERVAL;
	public static long INVENTORY_UPDATE_INTERVAL;
	public static long USER_ABNORMAL_EFFECTS_INTERVAL;
	public static long MOVE_PACKET_DELAY;
	public static long ATTACK_PACKET_DELAY;
	public static long REQUEST_MAGIC_PACKET_DELAY;
	public static int SHIFT_BY;
	public static int SHIFT_BY_Z;
	public static short MAP_MIN_Z;
	public static short MAP_MAX_Z;
	public static int BROADCAST_LIMIT_LENGHT;
	public static int BROADCAST_LIMIT_HEIGHT;

	// --------------------------------------------------
	// Vitality Settings
	// --------------------------------------------------
	public static boolean ENABLE_VITALITY;
	public static boolean RECOVER_VITALITY_ON_RECONNECT;
	public static float RATE_VITALITY_LEVEL_1;
	public static float RATE_VITALITY_LEVEL_2;
	public static float RATE_VITALITY_LEVEL_3;
	public static float RATE_VITALITY_LEVEL_4;
	public static float RATE_RECOVERY_VITALITY_PEACE_ZONE;
	public static float RATE_VITALITY_LOST;
	public static float RATE_VITALITY_GAIN;
	public static float RATE_RECOVERY_ON_RECONNECT;
	public static int STARTING_VITALITY_POINTS;
	public static double VITALITY_RAID_BONUS;
	public static double VITALITY_NEVIT_UP_POINT;
	public static double VITALITY_NEVIT_POINT;
	
	// Nevit System
	public static boolean ALLOW_NEVIT_SYSTEM;
	public static int NEVIT_ADVENT_TIME;
	public static int NEVIT_MAX_POINTS;
	public static int NEVIT_BONUS_EFFECT_TIME;
	
	// Nevit System
	public static boolean ALLOW_RECO_BONUS_SYSTEM;
	
	// --------------------------------------------------
	// No classification assigned to the following yet
	// --------------------------------------------------
	public static int MAX_ITEM_IN_PACKET;
	public static boolean CHECK_KNOWN;
	public static String EXTERNAL_HOSTNAME;
	public static int PVP_NORMAL_TIME;
	public static int PVP_PVP_TIME;
	public static boolean PVP_ABSORB_DAMAGE;

	public static enum IdFactoryType
	{
		Compaction, BitSet, Stack
	}

	public static IdFactoryType IDFACTORY_TYPE;
	public static boolean BAD_ID_CHECKING;

	public static int ELEMENT_ARMOR_LIMIT;
	public static double ENCHANT_CHANCE_ELEMENT_STONE;
	public static double ENCHANT_CHANCE_ELEMENT_CRYSTAL;
	public static double ENCHANT_CHANCE_ELEMENT_JEWEL;
	public static double ENCHANT_CHANCE_ELEMENT_ENERGY;
	public static boolean ENCHANT_ELEMENT_ALL_ITEMS;
	public static int[] ENCHANT_BLACKLIST;
	public static boolean SYSTEM_BLESSED_ENCHANT;
	public static int BLESSED_ENCHANT_SAVE;
	public static int[] SAVE_ENCHANT_BLACKLIST;
	public static boolean AUTO_LOOT_BY_ID_SYSTEM;
	public static int[] AUTO_LOOT_BY_ID;

	// GrandBoss Settings
	public static boolean ALLOW_DAMAGE_INFO;
	public static int DAMAGE_INFO_UPDATE;
	public static int DAMAGE_INFO_LIMIT_TIME;
	
	public static int EPIDOS_POINTS_NEED;
	
	// Antharas
	public static int ANTHARAS_WAIT_TIME;
	public static boolean ALLOW_ANTHARAS_MOVIE;
	public static String ANTHARAS_RESPAWN_PATTERN;

	// Valakas
	public static int VALAKAS_WAIT_TIME;
	public static boolean ALLOW_VALAKAS_MOVIE;
	public static String VALAKAS_RESPAWN_PATTERN;
	public static boolean VALAKAS_ATT_RESPAWN;
	public static String VALAKAS_ATT_RESPAWN_TIME;
	public static boolean VALAKAS_DAYS_RESPAWN;
	public static String VALAKAS_DAYS_RESPAWN_TIME;

	// Baium
	public static String BAIUM_RESPAWN_PATTERN;
	public static int BAIUM_SPAWN_DELAY;

	// Core
	public static String CORE_RESPAWN_PATTERN;

	// Orfen
	public static String ORFEN_RESPAWN_PATTERN;

	// Queen Ant
	public static String QUEEN_ANT_RESPAWN_PATTERN;

	// Beleth
	public static boolean ALLOW_BELETH_MOVIE;
	public static boolean ALLOW_BELETH_DROP_RING;
	public static boolean BELETH_NO_CC;
	public static int BELETH_MIN_PLAYERS;
	public static String BELETH_RESPAWN_PATTERN;
	public static int BELETH_SPAWN_DELAY;
	public static int BELETH_ZONE_CLEAN_DELAY;
	public static int BELETH_CLONES_RESPAWN;

	public static String SAILREN_RESPAWN_PATTERN;

	public static int CHANCE_SPAWN;
	public static int RESPAWN_TIME;

	// Gracia Seeds Settings
	public static int SOD_TIAT_KILL_COUNT;
	public static long SOD_STAGE_2_LENGTH;
	public static int SOI_EKIMUS_KILL_COUNT;
	public static int MIN_EKIMUS_PLAYERS;
	public static int MAX_EKIMUS_PLAYERS;
	public static String SOA_CHANGE_ZONE_TIME;

	// chatfilter
	public static ArrayList<String> FILTER_LIST;
	public static ArrayList<String> BROADCAST_FILTER_LIST;
	public static ArrayList<String> SCRIPTS_FILTER_LIST;

	// Security Settings
	public static boolean SECOND_AUTH_ENABLED;
	public static boolean SECOND_AUTH_STRONG_PASS;
	public static int SECOND_AUTH_MAX_ATTEMPTS;
	public static long SECOND_AUTH_BAN_TIME;
	public static boolean SECURITY_SKILL_CHECK;
	public static boolean SECURITY_SKILL_CHECK_CLEAR;
	public static boolean ENABLE_SAFE_ADMIN_PROTECTION;
	public static List<String> SAFE_ADMIN_NAMES;
	public static boolean SAFE_ADMIN_SHOW_ADMIN_ENTER;
	public static boolean BOTREPORT_ENABLE;
	public static String[] BOTREPORT_RESETPOINT_HOUR;
	public static long BOTREPORT_REPORT_DELAY;
	public static boolean BOTREPORT_ALLOW_REPORTS_FROM_SAME_CLAN_MEMBERS;
	public static int DEFAULT_PUNISH;
	public static int PUNISH_VALID_ATTEMPTS;
	public static boolean ALLOW_ILLEGAL_ACTIONS;
	public static int DEFAULT_PUNISH_PARAM;
	public static boolean ONLY_GM_ITEMS_FREE;
	public static boolean JAIL_IS_PVP;
	public static boolean JAIL_DISABLE_CHAT;

	// Email
	public static String EMAIL_SERVERINFO_NAME;
	public static String EMAIL_SERVERINFO_ADDRESS;
	public static boolean EMAIL_SYS_ENABLED;
	public static String EMAIL_SYS_HOST;
	public static int EMAIL_SYS_PORT;
	public static boolean EMAIL_SYS_SMTP_AUTH;
	public static String EMAIL_SYS_FACTORY;
	public static boolean EMAIL_SYS_FACTORY_CALLBACK;
	public static String EMAIL_SYS_USERNAME;
	public static String EMAIL_SYS_PASSWORD;
	public static String EMAIL_SYS_ADDRESS;
	public static String EMAIL_SYS_SELECTQUERY;
	public static String EMAIL_SYS_DBFIELD;

	// Conquerable Halls Settings
	public static int CHS_CLAN_MINLEVEL;
	public static int CHS_MAX_ATTACKERS;
	public static int CHS_MAX_FLAGS_PER_CLAN;
	public static boolean CHS_ENABLE_FAME;
	public static int CHS_FAME_AMOUNT;
	public static int CHS_FAME_FREQUENCY;
	public static int CLAN_HALL_HWID_LIMIT;

	// Multi-Language Settings
	public static boolean MULTILANG_ENABLE;
	public static List<String> MULTILANG_ALLOWED;
	public static String MULTILANG_DEFAULT;
	public static boolean MULTILANG_VOICED_ALLOW;

	// VoiceCommands Settings
	public static List<String> DISABLE_VOICE_BYPASSES;
	public static boolean ALLOW_OFFLINE_COMMAND;
	public static boolean ALLOW_EXP_GAIN_COMMAND;
	public static boolean ALLOW_AUTOLOOT_COMMAND;
	public static boolean VOICE_ONLINE_ENABLE;
	public static double FAKE_ONLINE;
	public static int FAKE_ONLINE_MULTIPLIER;
	public static boolean ALLOW_TELETO_LEADER;
	public static int TELETO_LEADER_ID;
	public static int TELETO_LEADER_COUNT;
	public static boolean ALLOW_REPAIR_COMMAND;
	public static boolean ALLOW_VISUAL_ARMOR_COMMAND;
	public static boolean ENABLE_VISUAL_BY_DEFAULT;
	public static boolean ALLOW_SEVENBOSSES_COMMAND;
	public static boolean ALLOW_ANCIENT_EXCHANGER_COMMAND;
	public static boolean ALLOW_SELLBUFFS_COMMAND;
	public static boolean ALLOW_SELLBUFFS_IN_PEACE;
	public static boolean ALLOW_SELLBUFFS_ZONE;
	public static boolean SELLBUFF_USED_MP;
	public static Map<String, Integer> SELLBUFF_CURRECY_LIST;
	public static int SELLBUFF_MIN_PRICE;
	public static int SELLBUFF_MAX_PRICE;
	public static int SELLBUFF_MAX_BUFFS;
	public static boolean FREE_SELLBUFF_FOR_SAME_CLAN;
	public static boolean ALLOW_SELLBUFFS_PETS;
	public static boolean ALLOW_STATS_COMMAND;
	public static boolean ALLOW_BLOCKBUFFS_COMMAND;
	public static boolean ALLOW_HIDE_TRADERS_COMMAND;
	public static boolean ALLOW_HIDE_BUFFS_ANIMATION_COMMAND;
	public static boolean ALLOW_BLOCK_TRADERS_COMMAND;
	public static boolean ALLOW_BLOCK_PARTY_COMMAND;
	public static boolean ALLOW_BLOCK_FRIEND_COMMAND;
	public static boolean ALLOW_MENU_COMMAND;
	public static boolean ALLOW_SECURITY_COMMAND;
	public static boolean ALLOW_IP_LOCK;
	public static boolean ALLOW_HWID_LOCK;
	public static boolean ALLOW_FIND_PARTY;
	public static boolean PARTY_LEADER_ONLY_CAN_INVITE;
	public static int FIND_PARTY_REFRESH_TIME;
	public static int FIND_PARTY_FLOOD_TIME;
	public static int FIND_PARTY_MIN_LEVEL;
	public static boolean ALLOW_ENCHANT_SERVICE;
	public static boolean ENCHANT_SERVICE_ONLY_FOR_PREMIUM;
	public static boolean ENCHANT_ALLOW_BELTS;
	public static boolean ENCHANT_ALLOW_SCROLLS;
	public static boolean ENCHANT_ALLOW_ATTRIBUTE;
	public static int ENCHANT_MAX_WEAPON;
	public static int ENCHANT_MAX_ARMOR;
	public static int ENCHANT_MAX_JEWELRY;
	public static int ENCHANT_MAX_ITEM_LIMIT;
	public static int ENCHANT_CONSUME_ITEM;
	public static int ENCHANT_CONSUME_ITEM_COUNT;
	public static int enchantServiceDefaultLimit;
	public static int enchantServiceDefaultEnchant;
	public static int enchantServiceDefaultAttribute;
	public static int ENCHANT_SCROLL_CHANCE_CORRECT;
	public static boolean ALLOW_RELOG_COMMAND;
	public static boolean ALLOW_PARTY_RANK_COMMAND;
	public static boolean ALLOW_PARTY_RANK_ONLY_FOR_CC;
	public static boolean PARTY_RANK_AUTO_OPEN;
	public static boolean ALLOW_RECOVERY_ITEMS;
	public static int RECOVERY_ITEMS_HOURS;
	public static boolean ALLOW_PROMOCODES_COMMAND;
	public static int PROMOCODES_USE_DELAY;

	// Custom Settings
	public static int EXP_ID;
	public static int SP_ID;
	public static boolean ALLOW_BLOCK_TRADE_ITEMS;
	public static boolean ALLOW_BLOCK_DEPOSIT_ITEMS;
	public static boolean ALLOW_BLOCK_DESTROY_ITEMS;
	public static boolean ALLOW_BLOCK_SELL_ITEMS;
	public static boolean ALLOW_BLOCK_DROP_ITEMS;
	public static boolean AUTO_COMBINE_TALISMANS;
	public static boolean ALLOW_AUTO_FISH_SHOTS;
	public static boolean ALLOW_CUSTOM_INTERFACE;
	public static boolean ALLOW_CUSTOM_BUTTONS;
	public static boolean ALLOW_INTERFACE_SHIFT_CLICK;
	public static boolean SWITCH_COLOR_NAME;
	public static boolean ALLOW_PRIVATE_INVENTORY;
	public static String SERVER_NAME;
	public static boolean ONLINE_PLAYERS_AT_STARTUP;
	public static int ONLINE_PLAYERS_ANNOUNCE_INTERVAL;
	public static boolean ALLOW_NEW_CHARACTER_TITLE;
	public static String NEW_CHARACTER_TITLE;
	public static boolean NEW_CHAR_IS_NOBLE;
	public static boolean NEW_CHAR_IS_HERO;
	public static boolean UNSTUCK_SKILL;
	public static boolean ALLOW_NEW_CHAR_CUSTOM_POSITION;
	public static int NEW_CHAR_POSITION_X;
	public static int NEW_CHAR_POSITION_Y;
	public static int NEW_CHAR_POSITION_Z;
	public static boolean ENABLE_NOBLESS_COLOR;
	public static int NOBLESS_COLOR_NAME;
	public static boolean ENABLE_NOBLESS_TITLE_COLOR;
	public static int NOBLESS_COLOR_TITLE_NAME;
	public static boolean INFINITE_SOUL_SHOT;
	public static boolean INFINITE_BEAST_SOUL_SHOT;
	public static boolean INFINITE_BEAST_SPIRIT_SHOT;
	public static boolean INFINITE_SPIRIT_SHOT;
	public static boolean INFINITE_BLESSED_SPIRIT_SHOT;
	public static boolean INFINITE_ARROWS;
	public static boolean ENTER_HELLBOUND_WITHOUT_QUEST;
	public static int AUTO_RESTART_TIME;
	public static String AUTO_RESTART_PATTERN;
	public static boolean SPEED_UP_RUN;
	public static int DISCONNECT_TIMEOUT;
	public static boolean DISCONNECT_SYSTEM_ENABLED;
	public static String DISCONNECT_TITLECOLOR;
	public static String DISCONNECT_TITLE;
	public static boolean CUSTOM_ENCHANT_ITEMS_ENABLED;
	public static Map<Integer, Integer> ENCHANT_ITEMS_ID;
	public static boolean ALLOW_UNLIM_ENTER_CATACOMBS;
	public static boolean AUTO_POINTS_SYSTEM;
	public static List<Integer> AUTO_HP_VALID_ITEMS;
	public static List<Integer> AUTO_MP_VALID_ITEMS;
	public static List<Integer> AUTO_CP_VALID_ITEMS;
	public static List<Integer> AUTO_SOUL_VALID_ITEMS;
	public static int DEFAULT_HP_PERCENT;
	public static int DEFAULT_MP_PERCENT;
	public static int DEFAULT_CP_PERCENT;
	public static int DEFAULT_SOUL_AMOUNT;
	public static boolean DISABLE_WITHOUT_POTIONS;
	public static double SELL_PRICE_MODIFIER;
	public static boolean ALT_KAMALOKA_SOLO_PREMIUM_ONLY;
	public static boolean ALT_KAMALOKA_ESSENCE_PREMIUM_ONLY;
	public static boolean ITEM_BROKER_ITEM_SEARCH;
	public static int ITEM_BROKER_ITEMS_PER_PAGE;
	public static int ITEM_BROKER_PAGES_PER_LIST;
	public static long ITEM_BROKER_TIME_UPDATE;
	public static boolean ALLOW_BLOCK_TRANSFORMS_AT_SIEGE;
	public static List<Integer> LIST_BLOCK_TRANSFORMS_AT_SIEGE;
	
	// PC Points Settings
	public static boolean PC_BANG_ENABLED;
	public static boolean PC_BANG_ONLY_FOR_PREMIUM;
	public static int PC_POINT_ID;
	public static int PC_BANG_MIN_LEVEL;
	public static int PC_BANG_POINTS_MIN;
	public static int PC_BANG_POINTS_PREMIUM_MIN;
	public static int PC_BANG_POINTS_MAX;
	public static int PC_BANG_POINTS_PREMIUM_MAX;
	public static int MAX_PC_BANG_POINTS;
	public static boolean ENABLE_DOUBLE_PC_BANG_POINTS;
	public static int DOUBLE_PC_BANG_POINTS_CHANCE;
	public static int PC_BANG_INTERVAL;

	// Community Board Settings
	public static boolean ALLOW_COMMUNITY;
	public static boolean BLOCK_COMMUNITY_IN_PVP_ZONE;
	public static List<String> DISABLE_COMMUNITY_BYPASSES;
	public static List<String> DISABLE_COMMUNITY_BYPASSES_COMBAT;
	public static List<String> DISABLE_COMMUNITY_BYPASSES_FLAG;
	public static String BBS_HOME_PAGE;
	public static String BBS_FAVORITE_PAGE;
	public static String BBS_LINK_PAGE;
	public static String BBS_REGION_PAGE;
	public static String BBS_CLAN_PAGE;
	public static String BBS_MEMO_PAGE;
	public static String BBS_MAIL_PAGE;
	public static String BBS_FRIENDS_PAGE;
	public static String BBS_ADDFAV_PAGE;
	public static boolean ALLOW_SENDING_IMAGES;
	public static boolean ALLOW_COMMUNITY_PEACE_ZONE;
	public static List<Integer> AVALIABLE_COMMUNITY_MULTISELLS;
	public static boolean ALLOW_COMMUNITY_BUFF_IN_SIEGE;
	public static boolean ALLOW_COMMUNITY_TELEPORT_IN_SIEGE;
	public static boolean BLOCK_TP_AT_SIEGES_FOR_ALL;
	public static boolean ALLOW_COMMUNITY_COORDS_TP;
	public static boolean ALLOW_COMMUNITY_TP_NO_RESTART_ZONES;
	public static boolean ALLOW_COMMUNITY_TP_SIEGE_ZONES;
	public static int COMMUNITY_TELEPORT_TABS;
	public static int COMMUNITY_FREE_TP_LVL;
	public static int COMMUNITY_FREE_BUFF_LVL;
	public static int INTERVAL_STATS_UPDATE;
	public static boolean ALLOW_BUFF_PEACE_ZONE;
	public static boolean ALLOW_SUMMON_AUTO_BUFF;
	public static boolean ALLOW_BUFF_WITHOUT_PEACE_FOR_PREMIUM;
	public static boolean FREE_ALL_BUFFS;
	public static boolean ALLOW_SCHEMES_FOR_PREMIUMS;
	public static int BUFF_ID_ITEM;
	public static boolean ALLOW_HEAL_ONLY_PEACE;
	public static int BUFF_AMOUNT;
	public static int CANCEL_BUFF_AMOUNT;
	public static int HPMPCP_BUFF_AMOUNT;
	public static int BUFF_MAX_SCHEMES;
	public static boolean SERVICES_LEVELUP_ENABLE;
	public static boolean SERVICES_DELEVEL_ENABLE;
	public static boolean LVLUP_SERVICE_STATIC_PRICE;
	public static int[] COMMUNITY_TELEPORT_ITEM = new int[2];
	public static int[] COMMUNITY_TAB_USE_ITEM = new int[2];
	public static int[] SERVICES_LEVELUP_ITEM = new int[2];
	public static int[] SERVICES_DELEVEL_ITEM = new int[2];
	public static int[] SERVICES_GIVENOOBLESS_ITEM = new int[2];
	public static int[] SERVICES_GIVESUBCLASS_ITEM = new int[2];
	public static int[] SERVICES_CHANGEGENDER_ITEM = new int[2];
	public static int[] SERVICES_GIVEHERO_ITEM = new int[2];
	public static int SERVICES_GIVEHERO_TIME;
	public static boolean SERVICES_GIVEHERO_SKILLS;
	public static int[] SERVICES_RECOVERYPK_ITEM = new int[2];
	public static int[] SERVICES_RECOVERYKARMA_ITEM = new int[2];
	public static int[] SERVICES_RECOVERYVITALITY_ITEM = new int[2];
	public static int[] SERVICES_GIVESP_ITEM = new int[2];
	public static int[] SERVICES_NAMECHANGE_ITEM = new int[2];
	public static String SERVICES_NAMECHANGE_TEMPLATE;
	public static int[] SERVICES_CLANNAMECHANGE_ITEM = new int[2];
	public static int[] SERVICES_UNBAN_ITEM = new int[2];
	public static int[] SERVICES_CLANLVL_ITEM = new int[2];
	public static int[] SERVICE_EXCHANGE_AUGMENT = new int[2];
	public static int[] SERVICE_EXCHANGE_ELEMENTS = new int[2];
	public static boolean LEARN_CLAN_SKILLS_MAX_LEVEL;
	public static boolean LEARN_CLAN_MAX_LEVEL;
	public static int[] SERVICES_CLANSKILLS_ITEM = new int[2];
	public static int[] SERVICES_GIVEREC_ITEM = new int[2];
	public static int[] SERVICES_GIVEREP_ITEM = new int[2];
	public static int SERVICES_REP_COUNT;
	public static int[] SERVICES_GIVEFAME_ITEM = new int[2];
	public static int[] SERVICES_CLAN_CREATE_PENALTY_ITEM = new int[2];
	public static int[] SERVICES_CLAN_JOIN_PENALTY_ITEM = new int[2];
	public static int SERVICES_FAME_COUNT;
	public static boolean SERVICES_AUGMENTATION_FORMATE;
	public static int[] SERVICES_AUGMENTATION_ITEM = new int[2];
	public static List<Integer> SERVICES_AUGMENTATION_AVAILABLE_LIST = new ArrayList<>();
	public static List<Integer> SERVICES_AUGMENTATION_DISABLED_LIST = new ArrayList<>();
	public static int BBS_FORGE_ENCHANT_ITEM;
	public static int BBS_FORGE_ENCHANT_START;
	public static int BBS_FORGE_FOUNDATION_ITEM;
	public static int[] BBS_FORGE_FOUNDATION_PRICE_ARMOR;
	public static int[] BBS_FORGE_FOUNDATION_PRICE_WEAPON;
	public static int[] BBS_FORGE_FOUNDATION_PRICE_JEWEL;
	public static int[] BBS_FORGE_ENCHANT_MAX;
	public static int[] BBS_FORGE_WEAPON_ENCHANT_LVL;
	public static int[] BBS_FORGE_ARMOR_ENCHANT_LVL;
	public static int[] BBS_FORGE_JEWELS_ENCHANT_LVL;
	public static int[] BBS_FORGE_ENCHANT_PRICE_WEAPON;
	public static int[] BBS_FORGE_ENCHANT_PRICE_ARMOR;
	public static int[] BBS_FORGE_ENCHANT_PRICE_JEWELS;
	public static int BBS_FORGE_WEAPON_ATTRIBUTE_MAX;
	public static int BBS_FORGE_ARMOR_ATTRIBUTE_MAX;
	public static int[] BBS_FORGE_ATRIBUTE_LVL_WEAPON;
	public static int[] BBS_FORGE_ATRIBUTE_LVL_ARMOR;
	public static int[] BBS_FORGE_ATRIBUTE_PRICE_ARMOR;
	public static int[] BBS_FORGE_ATRIBUTE_PRICE_WEAPON;
	public static int[] SERVICES_SOUL_CLOAK_TRANSFER_ITEM = new int[2];
	public static int SERVICES_OLF_STORE_ITEM;
	public static int SERVICES_OLF_STORE_0_PRICE;
	public static int SERVICES_OLF_STORE_6_PRICE;
	public static int SERVICES_OLF_STORE_7_PRICE;
	public static int SERVICES_OLF_STORE_8_PRICE;
	public static int SERVICES_OLF_STORE_9_PRICE;
	public static int SERVICES_OLF_STORE_10_PRICE;
	public static int[] SERVICES_OLF_TRANSFER_ITEM = new int[2];
	public static boolean ENABLE_MULTI_AUCTION_SYSTEM;
	public static long AUCTION_FEE;
	public static boolean ALLOW_AUCTION_OUTSIDE_TOWN;
	public static boolean ALLOW_ADDING_AUCTION_DELAY;
	public static int SECONDS_BETWEEN_ADDING_AUCTIONS;
	public static boolean AUCTION_PRIVATE_STORE_AUTO_ADDED;
	public static int[] BBS_BOSSES_TO_NOT_SHOW;
	public static int[] BBS_BOSSES_TO_SHOW;
	public static boolean ALLOW_BOSS_RESPAWN_TIME;
	public static int[] SERVICES_PREMIUM_VALID_ID;
	public static boolean ALLOW_CERT_DONATE_MODE;
	public static int CERT_MIN_LEVEL;
	public static String CERT_BLOCK_SKILL_LIST;
	public static int[] EMERGET_SKILLS_LEARN = new int[2];
	public static int[] MASTER_SKILLS_LEARN = new int[2];
	public static int[] TRANSFORM_SKILLS_LEARN = new int[2];
	public static int[] CLEAN_SKILLS_LEARN = new int[2];
	public static boolean ALLOW_TELEPORT_TO_RAID;
	public static int[] TELEPORT_TO_RAID_PRICE = new int[2];
	public static List<Integer> BLOCKED_RAID_LIST = new ArrayList<>();
	public static List<String> COLOR_NAME_LIST;
	public static List<String> COLOR_TITLE_LIST;
	public static Map<Integer, String> CHANGE_COLOR_TITLE_LIST;
	public static Map<Integer, String> CHANGE_COLOR_NAME_LIST;
	public static boolean CHANGE_MAIN_CLASS_WITHOUT_OLY_CHECK;
	public static int[] SERVICES_CHANGE_MAIN_CLASS = new int[2];
	public static int[] SERVICES_EXPAND_INVENTORY = new int[2];
	public static int[] SERVICES_EXPAND_TELE_TABS = new int[2];
	public static int[] SERVICES_EXPAND_WAREHOUSE = new int[2];
	public static int[] SERVICES_EXPAND_SELLSTORE = new int[2];
	public static int[] SERVICES_EXPAND_BUYSTORE = new int[2];
	public static int[] SERVICES_EXPAND_DWARFRECIPE = new int[2];
	public static int[] SERVICES_EXPAND_COMMONRECIPE = new int[2];
	public static int EXPAND_INVENTORY_STEP;
	public static int EXPAND_TELE_TABS_STEP;
	public static int EXPAND_WAREHOUSE_STEP;
	public static int EXPAND_SELLSTORE_STEP;
	public static int EXPAND_BUYSTORE_STEP;
	public static int EXPAND_DWARFRECIPE_STEP;
	public static int EXPAND_COMMONRECIPE_STEP;
	public static int SERVICES_EXPAND_INVENTORY_LIMIT;
	public static int SERVICES_EXPAND_TELE_TABS_LIMIT;
	public static int SERVICES_EXPAND_WAREHOUSE_LIMIT;
	public static int SERVICES_EXPAND_SELLSTORE_LIMIT;
	public static int SERVICES_EXPAND_BUYSTORE_LIMIT;
	public static int SERVICES_EXPAND_DWARFRECIPE_LIMIT;
	public static int SERVICES_EXPAND_COMMONRECIPE_LIMIT;
	public static String SERVICES_ACADEMY_REWARD;
	public static long ACADEMY_MIN_ADENA_AMOUNT;
	public static long ACADEMY_MAX_ADENA_AMOUNT;
	public static long MAX_TIME_IN_ACADEMY;
	public static int CLANS_PER_PAGE;
	public static int BUFFS_PER_PAGE;
	public static int MEMBERS_PER_PAGE;
	public static int PETITIONS_PER_PAGE;
	public static int SKILLS_PER_PAGE;
	public static int CLAN_PETITION_QUESTION_LEN;
	public static int CLAN_PETITION_ANSWER_LEN;
	public static int CLAN_PETITION_COMMENT_LEN;
	public static String HARDWARE_DONATE;

	// Hitman Event Settings
	public static boolean HITMAN_TAKE_KARMA;
	public static boolean HITMAN_ANNOUNCE;
	public static int HITMAN_MAX_PER_PAGE;
	public static List<Integer> HITMAN_CURRENCY;
	public static boolean HITMAN_SAME_TEAM;
	public static int HITMAN_TARGETS_LIMIT;
	public static int HITMAN_SAVE_TARGET;

	// Leprechaun Event Settings
	public static boolean ENABLED_LEPRECHAUN;
	public static int LEPRECHAUN_ID;
	public static int LEPRECHAUN_FIRST_SPAWN_DELAY;
	public static int LEPRECHAUN_RESPAWN_INTERVAL;
	public static int LEPRECHAUN_SPAWN_TIME;
	public static int LEPRECHAUN_ANNOUNCE_INTERVAL;
	public static boolean SHOW_NICK;
	public static boolean SHOW_REGION;
	public static int[] LEPRECHAUN_REWARD_ID;
	public static int[] LEPRECHAUN_REWARD_COUNT;
	public static int[] LEPRECHAUN_REWARD_CHANCE;

	// Underground Coliseum Settings
	public static String UC_START_TIME;
	public static int UC_TIME_PERIOD;
	public static boolean UC_ANNOUNCE_BATTLES;
	public static int UC_PARTY_LIMIT;
	public static int UC_RESS_TIME;

	// ItemMall Settings
	public static int GAME_POINT_ITEM_ID;
	
	// Aerial Cleft Event Settings
	public static int CLEFT_MIN_TEAM_PLAYERS;
	public static boolean CLEFT_BALANCER;
	public static int CLEFT_WAR_TIME;
	public static int CLEFT_COLLECT_TIME;
	public static int CLEFT_REWARD_ID;
	public static int CLEFT_REWARD_COUNT_WINNER;
	public static int CLEFT_REWARD_COUNT_LOOSER;
	public static int CLEFT_MIN_PLAYR_EVENT_TIME;
	public static boolean CLEFT_WITHOUT_SEEDS;
	public static int CLEFT_MIN_LEVEL;
	public static int CLEFT_TIME_RELOAD_REG;
	public static int CLEFT_MAX_PLAYERS;
	public static int CLEFT_RESPAWN_DELAY;
	public static int CLEFT_LEAVE_DELAY;
	public static int LARGE_COMPRESSOR_POINT;
	public static int SMALL_COMPRESSOR_POINT;
	public static int TEAM_CAT_POINT;
	public static int TEAM_PLAYER_POINT;
	
	// Olympiad AntiFeed Settings
	public static boolean ENABLE_OLY_FEED;
	public static int OLY_ANTI_FEED_WEAPON_RIGHT;
	public static int OLY_ANTI_FEED_WEAPON_LEFT;
	public static int OLY_ANTI_FEED_GLOVES;
	public static int OLY_ANTI_FEED_CHEST;
	public static int OLY_ANTI_FEED_LEGS;
	public static int OLY_ANTI_FEED_FEET;
	public static int OLY_ANTI_FEED_CLOAK;
	public static int OLY_ANTI_FEED_RIGH_HAND_ARMOR;
	public static int OLY_ANTI_FEED_HAIR_MISC_1;
	public static int OLY_ANTI_FEED_HAIR_MISC_2;
	public static int OLY_ANTI_FEED_RACE;
	public static int OLY_ANTI_FEED_GENDER;
	public static int OLY_ANTI_FEED_CLASS_RADIUS;
	public static int OLY_ANTI_FEED_CLASS_HEIGHT;
	public static int OLY_ANTI_FEED_PLAYER_HAVE_RECS;

	// Geodata Settings
	public static int GEO_X_FIRST, GEO_Y_FIRST, GEO_X_LAST, GEO_Y_LAST;
	public static float LOW_WEIGHT, MEDIUM_WEIGHT, HIGH_WEIGHT, DIAGONAL_WEIGHT;
	public static boolean GEODATA;
	public static boolean PATHFIND_BOOST;
	public static String PATHFIND_BUFFERS;
	public static boolean ADVANCED_DIAGONAL_STRATEGY;
	public static int MAX_POSTFILTER_PASSES;
	public static boolean DEBUG_PATH;
	public static boolean COORD_SYNCHRONIZE;
	public static int GEO_MOVE_SPEED;
	public static boolean ALLOW_GEOMOVE_VALIDATE;
	public static boolean ALLOW_DOOR_VALIDATE;

	// AntiBot Settings
	public static boolean ENABLE_ANTI_BOT_SYSTEM;
	public static int ASK_ANSWER_DELAY;
	public static int MINIMUM_TIME_QUESTION_ASK;
	public static int MAXIMUM_TIME_QUESTION_ASK;
	public static int MINIMUM_BOT_POINTS_TO_STOP_ASKING;
	public static int MAXIMUM_BOT_POINTS_TO_STOP_ASKING;
	public static int MAX_BOT_POINTS;
	public static int MINIMAL_BOT_RATING_TO_BAN;
	public static boolean ANNOUNCE_AUTO_BOT_BAN;
	public static boolean ON_WRONG_QUESTION_KICK;

	// Fight Events Settings
	public static boolean ALLOW_FIGHT_EVENTS;
	public static boolean ALLOW_RESPAWN_PROTECT_PLAYER;
	public static boolean ALLOW_REG_CONFIRM_DLG;
	public static int FIGHT_EVENTS_REG_TIME;
	public static int[] DISALLOW_FIGHT_EVENTS;
	public static int FIGHT_EVENTS_REWARD_MULTIPLIER;
	public static int TIME_FIRST_TELEPORT;
	public static int TIME_PLAYER_TELEPORTING;
	public static int TIME_PREPARATION_BEFORE_FIRST_ROUND;
	public static int TIME_PREPARATION_BETWEEN_NEXT_ROUNDS;
	public static int TIME_AFTER_ROUND_END_TO_RETURN_SPAWN;
	public static int TIME_TELEPORT_BACK_TOWN;
	public static int TIME_MAX_SECONDS_OUTSIDE_ZONE;
	public static int TIME_TO_BE_AFK;
	public static int TIME_AFK_TO_KICK;
	public static int ITEMS_FOR_MINUTE_OF_AFK;
	
	// Fake Player Settings
	public static boolean ALLOW_FAKE_PLAYERS;
	public static boolean ALLOW_ENCHANT_WEAPONS;
	public static boolean ALLOW_ENCHANT_ARMORS;
	public static boolean ALLOW_ENCHANT_JEWERLYS;
	public static int[] RND_ENCHANT_WEAPONS = new int[2];
	public static int[] RND_ENCHANT_ARMORS = new int[2];
	public static int[] RND_ENCHANT_JEWERLYS = new int[2];
	public static int[][] FAKE_FIGHTER_BUFFS;
	public static int[][] FAKE_MAGE_BUFFS;
	public static boolean ALLOW_SPAWN_FAKE_PLAYERS;
	public static int ENCHANTERS_MAX_LVL;
	public static int FAKE_PLAYERS_AMOUNT;
	public static int FAKE_DELAY_TELEPORT_TO_FARM;
	public static long FAKE_SPAWN_DELAY;
	public static long FAKE_ACTIVE_INTERVAL;
	public static long FAKE_PASSIVE_INTERVAL;
	
	// Chat Settings
	public static String DEFAULT_GLOBAL_CHAT;
	public static String DEFAULT_TRADE_CHAT;
	public static boolean USE_SAY_FILTER;
	public static boolean USE_BROADCAST_SAY_FILTER;
	public static String CHAT_FILTER_CHARS;
	public static int[] BAN_CHAT_CHANNELS;
	public static boolean ALLOW_CUSTOM_CHAT;
	public static int CHECK_CHAT_VALID;
	public static int CHAT_MSG_SIMPLE;
	public static int CHAT_MSG_PREMIUM;
	public static int CHAT_MSG_ANNOUNCE;
	public static int MIN_LVL_GLOBAL_CHAT;
	
	// Weekly Trader Settings
	public static boolean WEEKLY_TRADER_ENABLE;
	public static int WEEKLY_TRADER_DAY_OF_WEEK;
	public static int WEEKLY_TRADER_HOUR_OF_DAY;
	public static int WEEKLY_TRADER_MINUTE_OF_DAY;
	public static int WEEKLY_TRADER_DURATION;
	public static int WEEKLY_TRADER_MULTISELL_ID;
	
	// Mods Settings
	public static boolean ALLOW_DAILY_REWARD;
	public static boolean ALLOW_DAILY_TASKS;
	public static boolean ALLOW_VISUAL_SYSTEM;
	public static boolean ALLOW_VIP_SYSTEM;
	public static boolean ALLOW_REVENGE_SYSTEM;
	public static boolean ALLOW_MUTIPROFF_SYSTEM;
	public static boolean ALLOW_DAILY_ITEMS;
	
	// Formulas Settings
	public static boolean ALLOW_POLE_FLAG_AROUND;
	public static boolean ENABLE_OLD_CAST;
	public static int REGEN_MAIN_INTERVAL;
	public static int REGEN_MIN_RND;
	public static int REGEN_MAX_RND;
	public static double TOGGLE_MOD_MP;
	public static double BLEED_VULN;
	public static double BOSS_VULN;
	public static double MENTAL_VULN;
	public static double GUST_VULN;
	public static double HOLD_VULN;
	public static double PARALYZE_VULN;
	public static double PHYSICAL_BLOCKADE_VULN;
	public static double POISON_VULN;
	public static double SHOCK_VULN;
	public static double SLEEP_VULN;
	public static double BUFF_VULN;
	public static double DEBUFF_VULN;
	public static double STUN_VULN;
	public static double ROOT_VULN;
	public static double CANCEL_VULN;
	public static double BLEED_PROF;
	public static double MENTAL_PROF;
	public static double HOLD_PROF;
	public static double PARALYZE_PROF;
	public static double POISON_PROF;
	public static double SHOCK_PROF;
	public static double SLEEP_PROF;
	public static double DEBUFF_PROF;
	public static double STUN_PROF;
	public static double ROOT_PROF;
	public static double CANCEL_PROF;
	public static int BASE_STR_LIMIT;
	public static int BASE_INT_LIMIT;
	public static int BASE_DEX_LIMIT;
	public static int BASE_WIT_LIMIT;
	public static int BASE_CON_LIMIT;
	public static int BASE_MEN_LIMIT;
	public static int BASE_RESET_STR;
	public static int BASE_RESET_INT;
	public static int BASE_RESET_DEX;
	public static int BASE_RESET_WIT;
	public static int BASE_RESET_CON;
	public static int BASE_RESET_MEN;
	public static double MAX_BONUS_EXP;
	public static double MAX_BONUS_SP;
	public static String POLE_ATTACK_MOD;
	public static boolean ALLOW_DEBUFF_RES_TIME;
	public static double SKILLS_CHANCE_MOD;
	public static double SKILLS_CHANCE_POW;
	public static double STUN_CHANCE_MOD;
	public static double STUN_CHANCE_CRIT_MOD;
	public static double SKILL_BREAK_MOD;
	public static double SKILL_BREAK_CRIT_MOD;
	public static int MIN_ABNORMAL_STATE_SUCCESS_RATE;
	public static int MAX_ABNORMAL_STATE_SUCCESS_RATE;
	public static boolean CHECK_ATTACK_STATUS_TO_MOVE;
	public static int MIN_HIT_TIME;
	public static double ALT_WEIGHT_LIMIT;
	public static int RUN_SPD_BOOST;
	public static double RESPAWN_RESTORE_CP;
	public static double RESPAWN_RESTORE_HP;
	public static double RESPAWN_RESTORE_MP;
	public static double HP_REGEN_MULTIPLIER;
	public static double MP_REGEN_MULTIPLIER;
	public static double CP_REGEN_MULTIPLIER;
	public static boolean ALT_GAME_CANCEL_CAST;
	public static boolean ALLOW_RND_DAMAGE_BY_SKILLS;
	public static boolean EFFECT_CANCELING;
	public static boolean ALT_GAME_MAGICFAILURES;
	public static boolean SKILL_CHANCE_SHOW;
	public static boolean DEBUFF_REOVERLAY;
	public static String[] EFFECTS_REOVERLAY;
	public static boolean DEBUFF_REOVERLAY_ONLY_PVE;
	public static boolean ALLOW_DEBUFF_INFO;
	public static boolean STORE_SKILL_COOLTIME;
	public static boolean ALT_GAME_SHIELD_BLOCKS;
	public static int ALT_PERFECT_SHLD_BLOCK;
	public static double ALT_SHLD_BLOCK_MODIFIER;
	public static boolean DISPLAY_MESSAGE;
	public static boolean ATTACK_STANCE_MAGIC;
	public static boolean ALLOW_SKILL_END_CAST;
	public static boolean ALLOW_ZONES_LIMITS;
	public static long ALT_REUSE_CORRECTION;
	public static boolean BALANCER_ALLOW;
	public static float ALT_DAGGER_DMG_VS_HEAVY;
	public static float ALT_DAGGER_DMG_VS_ROBE;
	public static float ALT_DAGGER_DMG_VS_LIGHT;
	public static float ALT_BOW_DMG_VS_HEAVY;
	public static float ALT_BOW_DMG_VS_ROBE;
	public static float ALT_BOW_DMG_VS_LIGHT;
	public static float ALT_MAGES_PHYSICAL_DAMAGE_MULTI;
	public static float ALT_MAGES_MAGICAL_DAMAGE_MULTI;
	public static float ALT_FIGHTERS_PHYSICAL_DAMAGE_MULTI;
	public static float ALT_FIGHTERS_MAGICAL_DAMAGE_MULTI;
	public static float ALT_PETS_PHYSICAL_DAMAGE_MULTI;
	public static float ALT_PETS_MAGICAL_DAMAGE_MULTI;
	public static float ALT_NPC_PHYSICAL_DAMAGE_MULTI;
	public static float ALT_NPC_MAGICAL_DAMAGE_MULTI;
	public static float PATK_SPEED_MULTI;
	public static float MATK_SPEED_MULTI;
	public static boolean ALLOW_REFLECT_DAMAGE;
	
	public static String INTERFACE_SETTINGS_1;
	public static String INTERFACE_SETTINGS_2;
	
	public static void load()
	{
		_log.info("Loading configuration files...");
		final InputStream is = null;
		try
		{
			loadPersonalSettings(_personalConfigs);
			GameHostSettings.getInstance().loadGameSettings();
			loadServerSettings(is);
			loadSecuritySettings(is);
			loadFeatureSettings(is);
			loadCreaureSettings(is);
			loadMmoSettings(is);
			loadIdFactorySettings(is);
			loadGeneralSettings(is);
			loadFloodProtectorSettings(is);
			loadNpcsSettings(is);
			loadRatesSettings(is);
			loadPvpSettings(is);
			loadOlympiadSettings(is);
			loadEpicsSettings(is);
			loadGraciaSettings(is);
			loadFilterSettings();
			loadBroadCastFilterSettings();
			loadScriptsFilterSettings();
			loadClanhallSiegeSettings(is);
			loadLanguageSettings(is);
			loadVoiceSettings(is);
			loadCustomSettings(is);
			loadPcBangSettings(is);
			loadPremiumSettings(is);
			loadCommunitySettings(is);
			loadFormulasSettings(is);
			loadWeddingSettings(is);
			loadOfflineTradeSettings(is);
			loadDualSessionSettings(is);
			loadEnchantSettings(is);
			loadHitmanSettings(is);
			loadUndergroundColliseumSettings(is);
			loadItemMallSettings(is);
			loadLeprechaunSettings(is);
			loadAerialCleftSettings(is);
			loadOlyAntiFeedSettings(is);
			loadGeodataSettings(is);
			loadAntiBotSettings(is);
			loadFakeSettings(is);
			loadFightEventsSettings(is);
			loadChatSettings(is);
			loadWeeklyTraderSettings(is);
			loadSomikIntefaceSettings(is);
			loadModsSettings();
			FarmSettings.getInstance().load();
			AcademyRewards.getInstance().load();
		}
		finally
		{
			try
			{
				if (is != null)
				{
					is.close();
				}
			}
			catch (final Exception e)
			{}
		}
	}
	
	private static void loadPersonalSettings(HashMap<String, String> map)
	{
		map.clear();
		final Pattern LINE_PATTERN = Pattern.compile("^(((?!=).)+)=(.*?)$");
		Scanner scanner = null;
		try
		{
			final File file = new File(PERSONAL_FILE);
			final String content = Files.readFile(file);
			scanner = new Scanner(content);
			
			String line;
			while (scanner.hasNextLine())
			{
				line = scanner.nextLine();
				if (line.startsWith("#"))
				{
					continue;
				}
				
				final Matcher m = LINE_PATTERN.matcher(line);
				if (m.find())
				{
					final String name = m.group(1).trim();
					final String value = m.group(3).trim();
					map.put(name, value);
				}
			}
		}
		catch (final IOException e1)
		{
			_log.warn("Config: " + e1.getMessage());
			throw new Error("Failed to Load " + PERSONAL_FILE + " File.");
		}
		finally
		{
			try
			{
				scanner.close();
			}
			catch (final Exception e)
			{}
		}
	}
	
	private static void loadFormulasSettings(InputStream is)
	{
		try
		{
			final GameSettings formulaSettings = new GameSettings();
			is = new FileInputStream(new File(FORMULAS_FILE));
			formulaSettings.load(is);
			
			ALLOW_POLE_FLAG_AROUND = formulaSettings.getProperty("AllowPoleFlagAround", false);
			ENABLE_OLD_CAST = formulaSettings.getProperty("AllowOldCastFormula", false);
			REGEN_MAIN_INTERVAL = formulaSettings.getProperty("RegenInterval", 3000);
			REGEN_MIN_RND = formulaSettings.getProperty("RegenRandomMin", 300);
			REGEN_MAX_RND = formulaSettings.getProperty("RegenRandomMax", 600);
			TOGGLE_MOD_MP = formulaSettings.getProperty("ToggleMpModifier", 1.0);
			BLEED_VULN = formulaSettings.getProperty("BleedVuln", 100.);
			BOSS_VULN = formulaSettings.getProperty("BossVuln", 100.);
			MENTAL_VULN = formulaSettings.getProperty("MentalVuln", 100.);
			GUST_VULN = formulaSettings.getProperty("GustVuln", 100.);
			HOLD_VULN = formulaSettings.getProperty("HoldVuln", 100.);
			PARALYZE_VULN = formulaSettings.getProperty("ParalyzeVuln", 100.);
			PHYSICAL_BLOCKADE_VULN = formulaSettings.getProperty("PhisicalBlockadeVuln", 100.);
			POISON_VULN = formulaSettings.getProperty("PoisonVuln", 100.);
			SHOCK_VULN = formulaSettings.getProperty("ShockVuln", 100.);
			SLEEP_VULN = formulaSettings.getProperty("SleepVuln", 100.);
			BUFF_VULN = formulaSettings.getProperty("BuffVuln", 100.);
			DEBUFF_VULN = formulaSettings.getProperty("DebuffVuln", 100.);
			STUN_VULN = formulaSettings.getProperty("StunVuln", 100.);
			ROOT_VULN = formulaSettings.getProperty("RootVuln", 100.);
			CANCEL_VULN = formulaSettings.getProperty("CancelVuln", 100.);
			BLEED_PROF = formulaSettings.getProperty("BleedProf", 100.);
			MENTAL_PROF = formulaSettings.getProperty("MentalProf", 100.);
			HOLD_PROF = formulaSettings.getProperty("HoldProf", 100.);
			PARALYZE_PROF = formulaSettings.getProperty("ParalyzeProf", 100.);
			POISON_PROF = formulaSettings.getProperty("PoisonProf", 100.);
			SHOCK_PROF = formulaSettings.getProperty("ShockProf", 100.);
			SLEEP_PROF = formulaSettings.getProperty("SleepProf", 100.);
			DEBUFF_PROF = formulaSettings.getProperty("DebuffProf", 100.);
			STUN_PROF = formulaSettings.getProperty("StunProf", 100.);
			ROOT_PROF = formulaSettings.getProperty("RootProf", 100.);
			CANCEL_PROF = formulaSettings.getProperty("CancelProf", 100.);
			BASE_STR_LIMIT = formulaSettings.getProperty("BaseStrLimit", 100);
			BASE_INT_LIMIT = formulaSettings.getProperty("BaseIntLimit", 100);
			BASE_DEX_LIMIT = formulaSettings.getProperty("BaseDexLimit", 100);
			BASE_WIT_LIMIT = formulaSettings.getProperty("BaseWitLimit", 100);
			BASE_CON_LIMIT = formulaSettings.getProperty("BaseConLimit", 100);
			BASE_MEN_LIMIT = formulaSettings.getProperty("BaseMenLimit", 100);
			BASE_RESET_STR = formulaSettings.getProperty("BaseResetStr", 1);
			BASE_RESET_INT = formulaSettings.getProperty("BaseResetInt", 1);
			BASE_RESET_DEX = formulaSettings.getProperty("BaseResetDex", 1);
			BASE_RESET_WIT = formulaSettings.getProperty("BaseResetWit", 1);
			BASE_RESET_CON = formulaSettings.getProperty("BaseResetCon", 1);
			BASE_RESET_MEN = formulaSettings.getProperty("BaseResetMen", 1);
			MAX_BONUS_EXP = formulaSettings.getProperty("MaxExpBonus", 3.5);
			MAX_BONUS_SP = formulaSettings.getProperty("MaxSpBonus", 3.5);
			POLE_ATTACK_MOD = formulaSettings.getProperty("PoleAttackModifier", "1,100;2,90;3,80;4,70");
			MIN_ABNORMAL_STATE_SUCCESS_RATE = formulaSettings.getProperty("MinAbnormalStateSuccessRate", 10);
			MAX_ABNORMAL_STATE_SUCCESS_RATE = formulaSettings.getProperty("MaxAbnormalStateSuccessRate", 90);
			ALLOW_DEBUFF_RES_TIME = formulaSettings.getProperty("CorrectDebuffTimeWithResist", true);
			SKILLS_CHANCE_MOD = formulaSettings.getProperty("SkillsChanceMod", 11.);
			SKILLS_CHANCE_POW = formulaSettings.getProperty("SkillsChancePow", 0.5);
			STUN_CHANCE_MOD = formulaSettings.getProperty("StunChanceMod", 10.0);
			STUN_CHANCE_CRIT_MOD = formulaSettings.getProperty("StunChanceCritMod", 75.0);
			SKILL_BREAK_MOD = formulaSettings.getProperty("SkillBreakChanceMod", 10.0);
			SKILL_BREAK_CRIT_MOD = formulaSettings.getProperty("SkillBreakChanceCritMod", 75.0);
			CHECK_ATTACK_STATUS_TO_MOVE = formulaSettings.getProperty("CheckAttackToMove", true);
			MIN_HIT_TIME = formulaSettings.getProperty("MinHitTime", 500);
			ALT_WEIGHT_LIMIT = formulaSettings.getProperty("AltWeightLimit", 1);
			RUN_SPD_BOOST = formulaSettings.getProperty("RunSpeedBoost", 0);
			RESPAWN_RESTORE_CP = formulaSettings.getProperty("RespawnRestoreCP", 0.) / 100;
			RESPAWN_RESTORE_HP = formulaSettings.getProperty("RespawnRestoreHP", 65.) / 100;
			RESPAWN_RESTORE_MP = formulaSettings.getProperty("RespawnRestoreMP", 0.) / 100;
			HP_REGEN_MULTIPLIER = formulaSettings.getProperty("HpRegenMultiplier", 100.) / 100;
			MP_REGEN_MULTIPLIER = formulaSettings.getProperty("MpRegenMultiplier", 100.) / 100;
			CP_REGEN_MULTIPLIER = formulaSettings.getProperty("CpRegenMultiplier", 100.) / 100;
			ALT_GAME_CANCEL_CAST = formulaSettings.getProperty("AltGameCancelByHit", true);
			ALLOW_RND_DAMAGE_BY_SKILLS = formulaSettings.getProperty("AllowRndDamageBySkills", true);
			EFFECT_CANCELING = formulaSettings.getProperty("CancelLesserEffect", true);
			ALT_GAME_MAGICFAILURES = formulaSettings.getProperty("MagicFailures", true);
			SKILL_CHANCE_SHOW = formulaSettings.getProperty("SkillChanceShow", false);
			DEBUFF_REOVERLAY = formulaSettings.getProperty("DebuffReOverlay", true);
			EFFECTS_REOVERLAY = formulaSettings.getProperty("EffectsReOverlay", "empty").split(",");
			DEBUFF_REOVERLAY_ONLY_PVE = formulaSettings.getProperty("DebuffReOverlayPveOnly", false);
			ALLOW_DEBUFF_INFO = formulaSettings.getProperty("AllowDebuffInfo", false);
			STORE_SKILL_COOLTIME = formulaSettings.getProperty("StoreSkillCooltime", true);
			ALT_GAME_SHIELD_BLOCKS = formulaSettings.getProperty("AltShieldBlocks", false);
			ALT_PERFECT_SHLD_BLOCK = formulaSettings.getProperty("AltPerfectShieldBlockRate", 10);
			ALT_SHLD_BLOCK_MODIFIER = formulaSettings.getProperty("AltShieldBlockModifier", 1.0);
			DISPLAY_MESSAGE = formulaSettings.getProperty("DisplayEffectMessageThroughDeath", true);
			ATTACK_STANCE_MAGIC = formulaSettings.getProperty("DisableAttackStanceFromMagic", false);
			ALLOW_SKILL_END_CAST = formulaSettings.getProperty("AllowSkillEndCast", false);
			ALLOW_ZONES_LIMITS = formulaSettings.getProperty("AllowZonesLimits", false);
			ALT_REUSE_CORRECTION = formulaSettings.getProperty("SkillReuseCorrection", 0);
			BALANCER_ALLOW = formulaSettings.getProperty("BalancerAllow", false);
			ALT_DAGGER_DMG_VS_HEAVY = formulaSettings.getProperty("DaggerVSHeavy", 2.50F);
			ALT_DAGGER_DMG_VS_ROBE = formulaSettings.getProperty("DaggerVSRobe", 1.80F);
			ALT_DAGGER_DMG_VS_LIGHT = formulaSettings.getProperty("DaggerVSLight", 2.00F);
			ALT_BOW_DMG_VS_HEAVY = formulaSettings.getProperty("ArcherVSHeavy", 1.00F);
			ALT_BOW_DMG_VS_ROBE = formulaSettings.getProperty("ArcherVSRobe", 1.00F);
			ALT_BOW_DMG_VS_LIGHT = formulaSettings.getProperty("ArcherVSLight", 1.00F);
			ALT_MAGES_PHYSICAL_DAMAGE_MULTI = formulaSettings.getProperty("AltPDamageMages", 1.00F);
			ALT_MAGES_MAGICAL_DAMAGE_MULTI = formulaSettings.getProperty("AltMDamageMages", 1.00F);
			ALT_FIGHTERS_PHYSICAL_DAMAGE_MULTI = formulaSettings.getProperty("AltPDamageFighters", 1.00F);
			ALT_FIGHTERS_MAGICAL_DAMAGE_MULTI = formulaSettings.getProperty("AltMDamageFighters", 1.00F);
			ALT_PETS_PHYSICAL_DAMAGE_MULTI = formulaSettings.getProperty("AltPDamagePets", 1.00F);
			ALT_PETS_MAGICAL_DAMAGE_MULTI = formulaSettings.getProperty("AltMDamagePets", 1.00F);
			ALT_NPC_PHYSICAL_DAMAGE_MULTI = formulaSettings.getProperty("AltPDamageNpc", 1.00F);
			ALT_NPC_MAGICAL_DAMAGE_MULTI = formulaSettings.getProperty("AltMDamageNpc", 1.00F);
			PATK_SPEED_MULTI = formulaSettings.getProperty("AltAttackSpeed", 1.00F);
			MATK_SPEED_MULTI = formulaSettings.getProperty("AltCastingSpeed", 1.00F);
			ALLOW_REFLECT_DAMAGE = formulaSettings.getProperty("ReflectByL2OFF", false);
		}
		catch (final Exception e)
		{
			_log.warn("Config: " + e.getMessage());
			throw new Error("Failed to Load " + FORMULAS_FILE + " File.");
		}
		
	}
	
	private static void loadServerSettings(InputStream is)
	{
		try
		{
			final GameSettings serverSettings = new GameSettings();
			is = new FileInputStream(new File(CONFIGURATION_FILE));
			serverSettings.load(is);
			EFFECT_TASK_MANAGER_COUNT = serverSettings.getProperty("EffectTaskManagers", 2);
			AI_TASK_MANAGER_COUNT = serverSettings.getProperty("NpcAiTaskManagers", 5);
			USER_NAME = serverSettings.getProperty("UserName");
			USER_KEY = serverSettings.getProperty("UserKey");
			PROTECTION = serverSettings.getProperty("Protection");
			ALLOW_MULILOGIN = serverSettings.getProperty("AllowMultilogin", false);
			DATABASE_DRIVER = serverSettings.getProperty("Driver", "org.mariadb.jdbc.Driver");
			DATABASE_URL = serverSettings.getProperty("URL", "jdbc:mariadb://l2e?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=UTC");
			DATABASE_LOGIN = serverSettings.getProperty("Login", "root");
			DATABASE_PASSWORD = serverSettings.getProperty("Password", "");
			DATABASE_MAX_CONNECTIONS = serverSettings.getProperty("MaximumDbConnections", 10);
			DATABASE_MAX_IDLE_TIMEOUT = serverSettings.getProperty("MaxIdleConnectionTimeout", 60);
			DATABASE_CONNECTION_LIFE_TIME = serverSettings.getProperty("MaxConnectionLifeTime", 600);
			DATABASE_CONNECTION_TIMEOUT = serverSettings.getProperty("MaxConnectionTimeout", 10);
			SERVER_LIST_BRACKET = serverSettings.getProperty("ServerListBrackets", false);
			SERVER_LIST_IS_PVP = serverSettings.getProperty("ServerListIsPvp", false);
			SERVER_LIST_TYPE = getServerTypeId(serverSettings.getProperty("ServerListType", "Normal").split(","));
			SERVER_LIST_AGE = serverSettings.getProperty("ServerListAge", 0);
			SERVER_GMONLY = serverSettings.getProperty("ServerGMOnly", false);
			ALLOW_BACKUP_DATABASE = serverSettings.getProperty("AllowBakUpDatabase", false);
			try
			{
				DATAPACK_ROOT = new File(serverSettings.getProperty("DatapackRoot", ".").replaceAll("\\\\", "/")).getCanonicalFile();
			}
			catch (final IOException e)
			{
				_log.warn("Error setting datapack root!", e);
				DATAPACK_ROOT = new File(".");
			}
			
			CNAME_TEMPLATE = serverSettings.getProperty("CnameTemplate", ".*");
			PET_NAME_TEMPLATE = serverSettings.getProperty("PetNameTemplate", ".*");
			CLAN_NAME_TEMPLATE = serverSettings.getProperty("ClanNameTemplate", ".*");
			
			MAX_CHARACTERS_NUMBER_PER_ACCOUNT = serverSettings.getProperty("CharMaxNumber", 7);
			MAXIMUM_ONLINE_USERS = serverSettings.getProperty("MaximumOnlineUsers", 100);
			PROTOCOL_LIST = serverSettings.getIntegerProperty("AllowedProtocolRevisions", "267;268;271;273", ";");
			USER_INFO_INTERVAL = serverSettings.getProperty("BroadcastUserInfoInterval", 100);
			BROADCAST_CHAR_INFO_INTERVAL = serverSettings.getProperty("BroadcastCharInfoInterval", 100);
			BROADCAST_STATUS_UPDATE_INTERVAL = serverSettings.getProperty("BroadcastStatusUpdateInterval", 100);
			USER_STATS_UPDATE_INTERVAL = serverSettings.getProperty("BroadcastStatsUpdateInterval", 100);
			INVENTORY_UPDATE_INTERVAL = serverSettings.getProperty("InventoryUpdateInterval", 100);
			USER_ABNORMAL_EFFECTS_INTERVAL = serverSettings.getProperty("BroadcastEffectsInterval", 100);
			MOVE_PACKET_DELAY = serverSettings.getProperty("MovePacketInterval", 100);
			ATTACK_PACKET_DELAY = serverSettings.getProperty("AttackPacketInterval", 100);
			REQUEST_MAGIC_PACKET_DELAY = serverSettings.getProperty("RequestMagicPacketInterval", 100);
			SHIFT_BY = serverSettings.getProperty("RegionWidthSize", 15);
			SHIFT_BY_Z = serverSettings.getProperty("RegionHeightSize", 11);
			MAP_MIN_Z = serverSettings.getProperty("WorldMapMinZ", Short.MIN_VALUE);
			MAP_MAX_Z = serverSettings.getProperty("WorldMapMaxZ", Short.MAX_VALUE);
			BROADCAST_LIMIT_LENGHT = 32768;
			BROADCAST_LIMIT_HEIGHT = 32768;
			for (int i = 1; i <= (15 - SHIFT_BY); i++)
			{
				BROADCAST_LIMIT_LENGHT /= 2;
			}
			for (int i = 1; i <= (15 - SHIFT_BY_Z); i++)
			{
				BROADCAST_LIMIT_HEIGHT /= 2;
			}
		}
		catch (final Exception e)
		{
			_log.warn("Config: " + e.getMessage());
			throw new Error("Failed to Load " + CONFIGURATION_FILE + " File.");
		}
	}
	
	private static void loadSecuritySettings(InputStream is)
	{
		try
		{
			final GameSettings securitySettings = new GameSettings();
			is = new FileInputStream(new File(SECURITY_CONFIG_FILE));
			securitySettings.load(is);
			
			SECOND_AUTH_ENABLED = securitySettings.getProperty("SecondAuthEnabled", false);
			SECOND_AUTH_STRONG_PASS = securitySettings.getProperty("SecondAuthStrongPassword", true);
			SECOND_AUTH_MAX_ATTEMPTS = securitySettings.getProperty("SecondAuthMaxAttempts", 5);
			SECOND_AUTH_BAN_TIME = securitySettings.getProperty("SecondAuthBanTime", 480);
			SECURITY_SKILL_CHECK = securitySettings.getProperty("SkillsCheck", false);
			SECURITY_SKILL_CHECK_CLEAR = securitySettings.getProperty("SkillsCheckClear", false);
			
			ENABLE_SAFE_ADMIN_PROTECTION = securitySettings.getProperty("EnableSafeAdminProtection", false);
			SAFE_ADMIN_NAMES = securitySettings.getListProperty("SafeAdminName", "", ",");
			SAFE_ADMIN_SHOW_ADMIN_ENTER = securitySettings.getProperty("SafeAdminShowAdminEnter", false);
			BOTREPORT_ENABLE = securitySettings.getProperty("EnableBotReport", false);
			BOTREPORT_RESETPOINT_HOUR = securitySettings.getProperty("BotReportPointsResetHour", "00:00").split(":");
			BOTREPORT_REPORT_DELAY = securitySettings.getProperty("BotReportDelay", 30) * 60000;
			BOTREPORT_ALLOW_REPORTS_FROM_SAME_CLAN_MEMBERS = securitySettings.getProperty("AllowReportsFromSameClanMembers", false);
			PUNISH_VALID_ATTEMPTS = securitySettings.getProperty("PunishValidAttempts", 5);
			ALLOW_ILLEGAL_ACTIONS = securitySettings.getProperty("AllowIllegalActions", false);
			DEFAULT_PUNISH = securitySettings.getProperty("DefaultPunish", 2);
			DEFAULT_PUNISH_PARAM = securitySettings.getProperty("DefaultPunishParam", 0);
			ONLY_GM_ITEMS_FREE = securitySettings.getProperty("OnlyGMItemsFree", true);
			JAIL_IS_PVP = securitySettings.getProperty("JailIsPvp", false);
			JAIL_DISABLE_CHAT = securitySettings.getProperty("JailDisableChat", true);
			GENERAL_BYPASS_ENCODE_IGNORE = Pattern.compile(securitySettings.getProperty("GeneralBypassEncodeIgnore", " ^(_diary|manor_menu_select|_match|_olympiad).*"), Pattern.DOTALL);
			REUSABLE_BYPASS_ENCODE = Pattern.compile(securitySettings.getProperty("ReusableBypassEncode", "^(_bbshtm|_bbsvoice|_bbsservice|_bbs_service|_bbspage|_bbslistclanskills|_bbscert).*"), Pattern.DOTALL);
			EXACT_BYPASS_ENCODE_IGNORE = new HashSet<>();
			for (final var cmd : securitySettings.getProperty("ExactBypassEncodeIgnore", "_bbshome|_bbsgetfav|_bbsloc|_bbsclan|_bbslink|_bbsmemo|_maillist_0_1_0_|_bbsfriends|_bbsaddfav|_friendlist_0_").split("|"))
			{
				if (cmd == null || cmd.isEmpty())
				{
					continue;
				}
				EXACT_BYPASS_ENCODE_IGNORE.add(cmd);
			}
			INITIAL_BYPASS_ENCODE_IGNORE = new HashSet<>();
			for (final var cmd : securitySettings.getProperty("InitialBypassEncodeIgnore", "voiced_autofarm|voiced_farmstartex|voiced_farminit|voiced_autofarmex|voiced_farmstartex|voiced_farmstopex|voiced_editFarmOptionex").split("|"))
			{
				if (cmd == null || cmd.isEmpty())
				{
					continue;
				}
				INITIAL_BYPASS_ENCODE_IGNORE.add(cmd);
			}
		}
		catch (final Exception e)
		{
			_log.warn("Config: " + e.getMessage());
			throw new Error("Failed to Load " + SECURITY_CONFIG_FILE + " File.");
		}
	}
	
	private static void loadFeatureSettings(InputStream is)
	{
		try
		{
			final GameSettings Feature = new GameSettings();
			is = new FileInputStream(new File(FEATURE_CONFIG_FILE));
			Feature.load(is);
			
			FS_TELE_FEE_RATIO = Feature.getProperty("FortressTeleportFunctionFeeRatio", 604800000);
			FS_TELE1_FEE = Feature.getProperty("FortressTeleportFunctionFeeLvl1", 1000);
			FS_TELE2_FEE = Feature.getProperty("FortressTeleportFunctionFeeLvl2", 10000);
			FS_SUPPORT_FEE_RATIO = Feature.getProperty("FortressSupportFunctionFeeRatio", 86400000);
			FS_SUPPORT1_FEE = Feature.getProperty("FortressSupportFeeLvl1", 7000);
			FS_SUPPORT2_FEE = Feature.getProperty("FortressSupportFeeLvl2", 17000);
			FS_MPREG_FEE_RATIO = Feature.getProperty("FortressMpRegenerationFunctionFeeRatio", 86400000);
			FS_MPREG1_FEE = Feature.getProperty("FortressMpRegenerationFeeLvl1", 6500);
			FS_MPREG2_FEE = Feature.getProperty("FortressMpRegenerationFeeLvl2", 9300);
			FS_HPREG_FEE_RATIO = Feature.getProperty("FortressHpRegenerationFunctionFeeRatio", 86400000);
			FS_HPREG1_FEE = Feature.getProperty("FortressHpRegenerationFeeLvl1", 2000);
			FS_HPREG2_FEE = Feature.getProperty("FortressHpRegenerationFeeLvl2", 3500);
			FS_EXPREG_FEE_RATIO = Feature.getProperty("FortressExpRegenerationFunctionFeeRatio", 86400000);
			FS_EXPREG1_FEE = Feature.getProperty("FortressExpRegenerationFeeLvl1", 9000);
			FS_EXPREG2_FEE = Feature.getProperty("FortressExpRegenerationFeeLvl2", 10000);
			FS_UPDATE_FRQ = Feature.getProperty("FortressPeriodicUpdateFrequency", 360);
			FS_BLOOD_OATH_COUNT = Feature.getProperty("FortressBloodOathCount", 1);
			FS_MAX_SUPPLY_LEVEL = Feature.getProperty("FortressMaxSupplyLevel", 6);
			FS_FEE_FOR_CASTLE = Feature.getProperty("FortressFeeForCastle", 25000);
			FS_MAX_OWN_TIME = Feature.getProperty("FortressMaximumOwnTime", 168);
			
			ALLOW_CHECK_SEVEN_SIGN_STATUS = Feature.getProperty("AllowCheckSevenSignStatus", true);
			ALT_GAME_CASTLE_DAWN = Feature.getProperty("AltCastleForDawn", true);
			ALT_GAME_CASTLE_DUSK = Feature.getProperty("AltCastleForDusk", true);
			ALT_GAME_REQUIRE_CLAN_CASTLE = Feature.getProperty("AltRequireClanCastle", false);
			ALT_FESTIVAL_MIN_PLAYER = Feature.getProperty("AltFestivalMinPlayer", 5);
			ALT_MAXIMUM_PLAYER_CONTRIB = Feature.getProperty("AltMaxPlayerContrib", 1000000);
			ALT_FESTIVAL_MANAGER_START = Feature.getProperty("AltFestivalManagerStart", 120000);
			ALT_FESTIVAL_LENGTH = Feature.getProperty("AltFestivalLength", 1080000);
			ALT_FESTIVAL_CYCLE_LENGTH = Feature.getProperty("AltFestivalCycleLength", 2280000);
			ALT_FESTIVAL_FIRST_SPAWN = Feature.getProperty("AltFestivalFirstSpawn", 120000);
			ALT_FESTIVAL_FIRST_SWARM = Feature.getProperty("AltFestivalFirstSwarm", 300000);
			ALT_FESTIVAL_SECOND_SPAWN = Feature.getProperty("AltFestivalSecondSpawn", 540000);
			ALT_FESTIVAL_SECOND_SWARM = Feature.getProperty("AltFestivalSecondSwarm", 720000);
			ALT_FESTIVAL_CHEST_SPAWN = Feature.getProperty("AltFestivalChestSpawn", 900000);
			ALT_SIEGE_DAWN_GATES_PDEF_MULT = Feature.getProperty("AltDawnGatesPdefMult", 1.1);
			ALT_SIEGE_DUSK_GATES_PDEF_MULT = Feature.getProperty("AltDuskGatesPdefMult", 0.8);
			ALT_SIEGE_DAWN_GATES_MDEF_MULT = Feature.getProperty("AltDawnGatesMdefMult", 1.1);
			ALT_SIEGE_DUSK_GATES_MDEF_MULT = Feature.getProperty("AltDuskGatesMdefMult", 0.8);
			ALT_STRICT_SEVENSIGNS = Feature.getProperty("StrictSevenSigns", true);
			ALT_SEVENSIGNS_LAZY_UPDATE = Feature.getProperty("AltSevenSignsLazyUpdate", true);
			
			SSQ_DAWN_TICKET_QUANTITY = Feature.getProperty("SevenSignsDawnTicketQuantity", 300);
			SSQ_DAWN_TICKET_PRICE = Feature.getProperty("SevenSignsDawnTicketPrice", 1000);
			SSQ_DAWN_TICKET_BUNDLE = Feature.getProperty("SevenSignsDawnTicketBundle", 10);
			SSQ_MANORS_AGREEMENT_ID = Feature.getProperty("SevenSignsManorsAgreementId", 6388);
			SSQ_JOIN_DAWN_ADENA_FEE = Feature.getProperty("SevenSignsJoinDawnFee", 50000);
			
			TAKE_FORT_POINTS = Feature.getProperty("TakeFortPoints", 200);
			LOOSE_FORT_POINTS = Feature.getProperty("LooseFortPoints", 0);
			TAKE_CASTLE_POINTS = Feature.getProperty("TakeCastlePoints", 1500);
			LOOSE_CASTLE_POINTS = Feature.getProperty("LooseCastlePoints", 3000);
			CASTLE_DEFENDED_POINTS = Feature.getProperty("CastleDefendedPoints", 750);
			FESTIVAL_WIN_POINTS = Feature.getProperty("FestivalOfDarknessWin", 200);
			HERO_POINTS = Feature.getProperty("HeroPoints", 1000);
			ROYAL_GUARD_COST = Feature.getProperty("CreateRoyalGuardCost", 5000);
			KNIGHT_UNIT_COST = Feature.getProperty("CreateKnightUnitCost", 10000);
			KNIGHT_REINFORCE_COST = Feature.getProperty("ReinforceKnightUnitCost", 5000);
			BALLISTA_POINTS = Feature.getProperty("KillBallistaPoints", 30);
			BLOODALLIANCE_POINTS = Feature.getProperty("BloodAlliancePoints", 500);
			BLOODOATH_POINTS = Feature.getProperty("BloodOathPoints", 200);
			KNIGHTSEPAULETTE_POINTS = Feature.getProperty("KnightsEpaulettePoints", 20);
			REPUTATION_SCORE_PER_KILL = Feature.getProperty("ReputationScorePerKill", 1);
			JOIN_ACADEMY_MIN_REP_SCORE = Feature.getProperty("CompleteAcademyMinPoints", 190);
			JOIN_ACADEMY_MAX_REP_SCORE = Feature.getProperty("CompleteAcademyMaxPoints", 650);
			RAID_RANKING_1ST = Feature.getProperty("1stRaidRankingPoints", 1250);
			RAID_RANKING_2ND = Feature.getProperty("2ndRaidRankingPoints", 900);
			RAID_RANKING_3RD = Feature.getProperty("3rdRaidRankingPoints", 700);
			RAID_RANKING_4TH = Feature.getProperty("4thRaidRankingPoints", 600);
			RAID_RANKING_5TH = Feature.getProperty("5thRaidRankingPoints", 450);
			RAID_RANKING_6TH = Feature.getProperty("6thRaidRankingPoints", 350);
			RAID_RANKING_7TH = Feature.getProperty("7thRaidRankingPoints", 300);
			RAID_RANKING_8TH = Feature.getProperty("8thRaidRankingPoints", 200);
			RAID_RANKING_9TH = Feature.getProperty("9thRaidRankingPoints", 150);
			RAID_RANKING_10TH = Feature.getProperty("10thRaidRankingPoints", 100);
			RAID_RANKING_UP_TO_50TH = Feature.getProperty("UpTo50thRaidRankingPoints", 25);
			RAID_RANKING_UP_TO_100TH = Feature.getProperty("UpTo100thRaidRankingPoints", 12);
			RANK_CLASS_FOR_CC = Feature.getProperty("CommandChannelRankClass", 5);
			ALLOW_WYVERN_ALWAYS = Feature.getProperty("AllowRideWyvernAlways", false);
			ALLOW_WYVERN_DURING_SIEGE = Feature.getProperty("AllowRideWyvernDuringSiege", true);
			STOP_WAR_PVP = Feature.getProperty("AllowStopWarByPvpStatus", false);
		}
		catch (final Exception e)
		{
			_log.warn("Config: " + e.getMessage());
			throw new Error("Failed to Load " + FEATURE_CONFIG_FILE + " File.");
		}
	}
	
	private static void loadCreaureSettings(InputStream is)
	{
		try
		{
			final GameSettings Character = new GameSettings();
			is = new FileInputStream(new File(CHARACTER_CONFIG_FILE));
			Character.load(is);
			ALLOW_OPEN_CLOAK_SLOT = Character.getProperty("AllowOpenCloakSlot", false);
			ALLOW_UI_OPEN = Character.getProperty("AllowBuySellUIClose", true);
			ALT_GAME_DELEVEL = Character.getProperty("Delevel", true);
			DECREASE_SKILL_LEVEL = Character.getProperty("DecreaseSkillOnDelevel", true);
			DECREASE_ENCHANT_SKILLS = Character.getProperty("DecreaseEnchantSkills", true);
			DEATH_PENALTY_CHANCE = Character.getProperty("DeathPenaltyChance", 20);
			ENABLE_MODIFY_SKILL_DURATION = Character.getProperty("EnableModifySkillDuration", false);
			if (ENABLE_MODIFY_SKILL_DURATION)
			{
				SKILL_DURATION_LIST_SIMPLE = Character.getMapProperty("SkillDurationList", "", ";");
				SKILL_DURATION_LIST_PREMIUM = Character.getMapProperty("SkillDurationListPremium", "", ";");
			}
			ENABLE_MODIFY_SKILL_REUSE = Character.getProperty("EnableModifySkillReuse", false);
			if (ENABLE_MODIFY_SKILL_REUSE)
			{
				SKILL_REUSE_LIST = Character.getMapProperty("SkillReuseList", "", ";");
			}
			AUTO_LEARN_SKILLS = Character.getProperty("AutoLearnSkills", false);
			AUTO_LEARN_SKILLS_MAX_LEVEL = Character.getProperty("AutoLearnSkillsMaxLevel", 85);
			AUTO_LEARN_FS_SKILLS = Character.getProperty("AutoLearnForgottenScrollSkills", false);
			DISABLED_ITEMS_FOR_ACQUIRE_TYPES = new HashSet<>();
			for (final String t : Character.getProperty("DisableItemsForAcquireTypes", "").split(";"))
			{
				if (t.trim().isEmpty())
				{
					continue;
				}
				DISABLED_ITEMS_FOR_ACQUIRE_TYPES.add(AcquireSkillType.valueOf(t.toUpperCase()));
			}
			AUTO_LOOT_HERBS = Character.getProperty("AutoLootHerbs", false);
			BUFFS_MAX_AMOUNT = Character.getProperty("MaxBuffAmount", 20);
			BUFFS_MAX_AMOUNT_PREMIUM = Character.getProperty("MaxBuffAmountForPremium", 24);
			DEBUFFS_MAX_AMOUNT = Character.getProperty("MaxDebuffAmount", 24);
			DEBUFFS_MAX_AMOUNT_PREMIUM = Character.getProperty("MaxDebuffAmountForPremium", 24);
			TRIGGERED_BUFFS_MAX_AMOUNT = Character.getProperty("MaxTriggeredBuffAmount", 12);
			DANCES_MAX_AMOUNT = Character.getProperty("MaxDanceAmount", 12);
			DANCE_CANCEL_BUFF = Character.getProperty("DanceCancelBuff", false);
			DANCE_CONSUME_ADDITIONAL_MP = Character.getProperty("DanceConsumeAdditionalMP", true);
			ALT_STORE_DANCES = Character.getProperty("AltStoreDances", false);
			AUTO_LEARN_DIVINE_INSPIRATION = Character.getProperty("AutoLearnDivineInspiration", false);
			PLAYER_FAKEDEATH_UP_PROTECTION = Character.getProperty("PlayerFakeDeathUpProtection", 0);
			SUBCLASS_STORE_SKILL_COOLTIME = Character.getProperty("SubclassStoreSkillCooltime", false);
			SUBCLASS_STORE_SKILL = Character.getProperty("SubclassSaveSkill", false);
			SUMMON_STORE_SKILL_COOLTIME = Character.getProperty("SummonStoreSkillCooltime", true);
			ALLOW_ENTIRE_TREE = Character.getProperty("AllowEntireTree", false);
			ALTERNATE_CLASS_MASTER = Character.getProperty("AlternateClassMaster", false);
			LIFE_CRYSTAL_NEEDED = Character.getProperty("LifeCrystalNeeded", true);
			ES_SP_BOOK_NEEDED = Character.getProperty("EnchantSkillSpBookNeeded", true);
			DIVINE_SP_BOOK_NEEDED = Character.getProperty("DivineInspirationSpBookNeeded", true);
			COMPARE_SKILL_PRICE = Character.getProperty("CompareSkillPrice", false);
			ALT_GAME_SUBCLASS_WITHOUT_QUESTS = Character.getProperty("AltSubClassWithoutQuests", false);
			SUBCLASS_MIN_LEVEL = Character.getProperty("SubClassMinLevel", 75);
			CERT65_MIN_LEVEL = Character.getProperty("Certification65MinLevel", 65);
			CERT70_MIN_LEVEL = Character.getProperty("Certification70MinLevel", 70);
			CERT75_CLASS_MIN_LEVEL = Character.getProperty("Certification75ClassMinLevel", 75);
			CERT75_MASTER_MIN_LEVEL = Character.getProperty("Certification75MasterMinLevel", 75);
			CERT80_MIN_LEVEL = Character.getProperty("Certification80MinLevel", 80);
			ALT_GAME_SUBCLASS_EVERYWHERE = Character.getProperty("AltSubclassEverywhere", false);
			ALT_GAME_SUBCLASS_ALL_CLASSES = Character.getProperty("AltSubClassAllClasses", false);
			RESTORE_SERVITOR_ON_RECONNECT = Character.getProperty("RestoreServitorOnReconnect", true);
			RESTORE_PET_ON_RECONNECT = Character.getProperty("RestorePetOnReconnect", true);
			ALLOW_SUMMON_OWNER_ATTACK = Character.getProperty("AllowAttackOwner", false);
			ALLOW_SUMMON_TELE_TO_LEADER = Character.getProperty("AllowTeleToOwner", false);
			ALLOW_PETS_RECHARGE_ONLY_COMBAT = Character.getProperty("PetsRechargeOnlyInCombat", false);
			ALLOW_TRANSFORM_WITHOUT_QUEST = Character.getProperty("AltTransformationWithoutQuest", false);
			FEE_DELETE_TRANSFER_SKILLS = Character.getProperty("FeeDeleteTransferSkills", 10000000);
			FEE_DELETE_SUBCLASS_SKILLS = Character.getProperty("FeeDeleteSubClassSkills", 10000000);
			ENABLE_VITALITY = Character.getProperty("EnableVitality", true);
			RECOVER_VITALITY_ON_RECONNECT = Character.getProperty("RecoverVitalityOnReconnect", true);
			STARTING_VITALITY_POINTS = Character.getProperty("StartingVitalityPoints", 20000);
			VITALITY_RAID_BONUS = Character.getProperty("VitalityRaidBonus", 2000);
			VITALITY_NEVIT_UP_POINT = Character.getProperty("VitalityNevitUpPoint", 10);
			VITALITY_NEVIT_POINT = Character.getProperty("VitalityNevitPoint", 10);
			MAX_SUBCLASS = Character.getProperty("MaxSubclass", 3);
			BASE_SUBCLASS_LEVEL = Character.getProperty("BaseSubclassLevel", 40);
			MAX_SUBCLASS_LEVEL = Character.getProperty("MaxSubclassLevel", 80);
			PLAYER_MAXIMUM_LEVEL = Character.getProperty("MaxPlayerLevel", 85) + 1;
			MAX_PVTSTORESELL_SLOTS_DWARF = Character.getProperty("SellStoreSlotsDwarf", 4);
			MAX_PVTSTORESELL_SLOTS_OTHER = Character.getProperty("SellStoreSlotsOther", 3);
			MAX_PVTSTOREBUY_SLOTS_DWARF = Character.getProperty("BuyStoreSlotsDwarf", 5);
			MAX_PVTSTOREBUY_SLOTS_OTHER = Character.getProperty("BuyStoreSlotsOther", 4);
			INVENTORY_MAXIMUM_NO_DWARF = Character.getProperty("InventorySlotsForNoDwarf", 80);
			INVENTORY_MAXIMUM_DWARF = Character.getProperty("InventorySlotsForDwarf", 100);
			INVENTORY_MAXIMUM_GM = Character.getProperty("InventorySlotsForGM", 250);
			INVENTORY_MAXIMUM_QUEST_ITEMS = Character.getProperty("InventorySlotsForQuestItems", 100);
			MAX_ITEM_IN_PACKET = Math.max(INVENTORY_MAXIMUM_NO_DWARF, Math.max(INVENTORY_MAXIMUM_DWARF, INVENTORY_MAXIMUM_GM));
			WAREHOUSE_SLOTS_DWARF = Character.getProperty("WarehouseSlotsForDwarf", 120);
			WAREHOUSE_SLOTS_NO_DWARF = Character.getProperty("WarehouseSlotsForNoDwarf", 100);
			WAREHOUSE_SLOTS_CLAN = Character.getProperty("WarehouseSlotsForClan", 150);
			ALT_FREIGHT_SLOTS = Character.getProperty("FreightSlots", 200);
			MAX_AMOUNT_BY_MULTISELL = Character.getProperty("MaximumItemsPerMultisell", 5000);
			ALT_FREIGHT_PRICE = Character.getProperty("FreightPrice", 1000);
			EXPAND_INVENTORY_LIMIT = Character.getProperty("ExpandInventoryLimit", 300);
			EXPAND_WAREHOUSE_LIMIT = Character.getProperty("ExpandWareHouseLimit", 200);
			EXPAND_SELLSTORE_LIMIT = Character.getProperty("ExpandSellStoreLimit", 10);
			EXPAND_BUYSTORE_LIMIT = Character.getProperty("ExpandBuyStoreLimit", 10);
			EXPAND_DWARFRECIPE_LIMIT = Character.getProperty("ExpandDwarfRecipeLimit", 100);
			EXPAND_COMMONRECIPE_LIMIT = Character.getProperty("ExpandCommonRecipeLimit", 100);
			TELEPORT_BOOKMART_LIMIT = Character.getProperty("TeleportBookMarkLimit", 9);
			ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE = Character.getProperty("AltKarmaPlayerCanBeKilledInPeaceZone", false);
			ALT_GAME_KARMA_PLAYER_CAN_SHOP = Character.getProperty("AltKarmaPlayerCanShop", true);
			ALT_GAME_KARMA_PLAYER_CAN_TELEPORT = Character.getProperty("AltKarmaPlayerCanTeleport", true);
			ALT_GAME_KARMA_PLAYER_CAN_USE_GK = Character.getProperty("AltKarmaPlayerCanUseGK", false);
			ALT_GAME_KARMA_PLAYER_CAN_TRADE = Character.getProperty("AltKarmaPlayerCanTrade", true);
			ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE = Character.getProperty("AltKarmaPlayerCanUseWareHouse", true);
			MAX_PERSONAL_FAME_POINTS = Character.getProperty("MaxPersonalFamePoints", 50000);
			FORTRESS_ZONE_FAME_TASK_FREQUENCY = Character.getProperty("FortressZoneFameTaskFrequency", 300);
			FORTRESS_ZONE_FAME_AQUIRE_POINTS = Character.getProperty("FortressZoneFameAquirePoints", 31);
			CASTLE_ZONE_FAME_TASK_FREQUENCY = Character.getProperty("CastleZoneFameTaskFrequency", 300);
			CASTLE_ZONE_FAME_AQUIRE_POINTS = Character.getProperty("CastleZoneFameAquirePoints", 125);
			FAME_FOR_DEAD_PLAYERS = Character.getProperty("FameForDeadPlayers", true);
			IS_CRAFTING_ENABLED = Character.getProperty("CraftingEnabled", true);
			CRAFT_MASTERWORK = Character.getProperty("CraftMasterwork", true);
			CRAFT_DOUBLECRAFT_CHANCE = Character.getProperty("CraftDoubleCraftChance", 3.);
			DWARF_RECIPE_SLOTS = Character.getProperty("DwarfRecipeSlots", 50);
			COMMON_RECIPE_SLOTS = Character.getProperty("CommonRecipeSlots", 50);
			ALT_GAME_CREATION = Character.getProperty("AltGameCreation", false);
			ALT_GAME_CREATION_SPEED = Character.getProperty("AltGameCreationSpeed", 1);
			ALT_GAME_CREATION_XP_RATE = Character.getProperty("AltGameCreationXpRate", 1);
			ALT_GAME_CREATION_SP_RATE = Character.getProperty("AltGameCreationSpRate", 1);
			ALT_GAME_CREATION_RARE_XPSP_RATE = Character.getProperty("AltGameCreationRareXpSpRate", 2);
			ALT_BLACKSMITH_USE_RECIPES = Character.getProperty("AltBlacksmithUseRecipes", true);
			ALT_CLAN_LEADER_DATE_CHANGE = Character.getProperty("AltClanLeaderDateChange", "0 0 * * 3");
			ALT_CLAN_DEFAULT_LEVEL = Character.getProperty("ClanDefaultLevel", 1);
			ALT_CLAN_LEADER_INSTANT_ACTIVATION = Character.getProperty("AltClanLeaderInstantActivation", false);
			ALT_CLAN_JOIN_DAYS = Character.getProperty("DaysBeforeJoinAClan", 24);
			ALT_CLAN_CREATE_DAYS = Character.getProperty("DaysBeforeCreateAClan", 240);
			ALT_CLAN_DISSOLVE_DAYS = Character.getProperty("DaysToPassToDissolveAClan", 168);
			ALT_ALLY_JOIN_DAYS_WHEN_LEAVED = Character.getProperty("DaysBeforeJoinAllyWhenLeaved", 24);
			ALT_ALLY_JOIN_DAYS_WHEN_DISMISSED = Character.getProperty("DaysBeforeJoinAllyWhenDismissed", 24);
			ALT_ACCEPT_CLAN_DAYS_WHEN_DISMISSED = Character.getProperty("DaysBeforeAcceptNewClanWhenDismissed", 24);
			ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED = Character.getProperty("DaysBeforeCreateNewAllyWhenDissolved", 24);
			ALT_MAX_NUM_OF_CLANS_IN_ALLY = Character.getProperty("AltMaxNumOfClansInAlly", 3);
			ALT_CLAN_MEMBERS_FOR_WAR = Character.getProperty("AltClanMembersForWar", 15);
			ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH = Character.getProperty("AltMembersCanWithdrawFromClanWH", false);
			REMOVE_CASTLE_CIRCLETS = Character.getProperty("RemoveCastleCirclets", true);
			ALT_PARTY_RANGE = Character.getProperty("AltPartyRange", 1600);
			ALT_PARTY_RANGE2 = Character.getProperty("AltPartyRange2", 1400);
			PARTY_LIMIT = Character.getProperty("PartyMembersLimit", 9);
			ALT_LEAVE_PARTY_LEADER = Character.getProperty("AltLeavePartyLeader", false);
			STARTING_ADENA = Character.getProperty("StartingAdena", 0);
			STARTING_LEVEL = Character.getProperty("StartingLevel", 1);
			STARTING_SP = Character.getProperty("StartingSP", 0);
			MAX_ADENA = Character.getProperty("MaxAdena", 99900000000L);
			if (MAX_ADENA < 0)
			{
				MAX_ADENA = Long.MAX_VALUE;
			}
			AUTO_LOOT = Character.getProperty("AutoLoot", false);
			AUTO_LOOT_RAIDS = Character.getProperty("AutoLootRaids", false);
			PLAYER_SPAWN_PROTECTION = Character.getProperty("PlayerSpawnProtection", 0);
			SPAWN_PROTECTION_ALLOWED_ITEMS = Character.getIntegerProperty("PlayerSpawnProtectionAllowedItems", "0", ",");
			PLAYER_TELEPORT_PROTECTION = Character.getProperty("PlayerTeleportProtection", 0);
			RANDOM_RESPAWN_IN_TOWN_ENABLED = Character.getProperty("RandomRespawnInTownEnabled", true);
			OFFSET_ON_TELEPORT_ENABLED = Character.getProperty("OffsetOnTeleportEnabled", true);
			MAX_OFFSET_ON_TELEPORT = Character.getProperty("MaxOffsetOnTeleport", 50);
			ALLOW_SUMMON_TO_INSTANCE = Character.getProperty("AllowSummonToInstance", true);
			PETITIONING_ALLOWED = Character.getProperty("PetitioningAllowed", true);
			NEW_PETITIONING_SYSTEM = Character.getProperty("NewPetitionSystem", false);
			MAX_PETITIONS_PER_PLAYER = Character.getProperty("MaxPetitionsPerPlayer", 5);
			MAX_PETITIONS_PENDING = Character.getProperty("MaxPetitionsPending", 25);
			ALT_GAME_FREE_TELEPORT = Character.getProperty("AltFreeTeleporting", false);
			DELETE_DAYS = Character.getProperty("DeleteCharAfterDays", 7);
			ALT_GAME_EXPONENT_XP = Character.getProperty("AltGameExponentXp", 0F);
			ALT_GAME_EXPONENT_SP = Character.getProperty("AltGameExponentSp", 0F);
			PARTY_XP_CUTOFF_METHOD = Character.getProperty("PartyXpCutoffMethod", "highfive");
			PARTY_XP_CUTOFF_PERCENT = Character.getProperty("PartyXpCutoffPercent", 3.);
			PARTY_XP_CUTOFF_LEVEL = Character.getProperty("PartyXpCutoffLevel", 20);
			PARTY_XP_CUTOFF_GAPS = Character.getDoubleIntProperty("PartyXpCutoffGaps", "0,9;10,14;15,99", ";");
			PARTY_XP_CUTOFF_GAP_PERCENTS = Character.getIntProperty("PartyXpCutoffGapPercent", "100;30;0", ";");
			DISABLE_TUTORIAL = Character.getProperty("DisableTutorial", false);
			ENABLE_SPECIAL_TUTORIAL = Character.getProperty("AllowSpecialTutorial", false);
			EXPERTISE_PENALTY = Character.getProperty("ExpertisePenalty", true);
			STORE_RECIPE_SHOPLIST = Character.getProperty("StoreRecipeShopList", false);
			STORE_UI_SETTINGS = Character.getProperty("StoreCharUiSettings", false);
			FORBIDDEN_NAMES = Character.getProperty("ForbiddenNames", "").split(",");
			SILENCE_MODE_EXCLUDE = Character.getProperty("SilenceModeExclude", false);
			ALT_VALIDATE_TRIGGER_SKILLS = Character.getProperty("AltValidateTriggerSkills", false);
			RESTORE_DISPEL_SKILLS = Character.getProperty("RestoreDispelSkills", false);
			RESTORE_DISPEL_SKILLS_TIME = Character.getProperty("RestoreDispelSkillsTime", 10);
			ALT_GAME_VIEWPLAYER = Character.getProperty("AltGameViewPlayer", false);
			AUTO_LOOT_BY_ID_SYSTEM = Character.getProperty("AutoLootByIdSystem", false);
			AUTO_LOOT_BY_ID = Character.getIntProperty("AutoLootById", "0", ",");
			Arrays.sort(AUTO_LOOT_BY_ID);
			TRADE_ONLY_IN_PEACE_ZONE = Character.getProperty("TradeOnlyInPeaceZones", false);
			ALLOW_TRADE_IN_ZONE = Character.getProperty("AllowTradeInTradeZones", false);
			ALLOW_NEVIT_SYSTEM = Character.getProperty("AllowNevitSystem", true);
			NEVIT_ADVENT_TIME = Character.getProperty("NevitAdventTime", 240);
			NEVIT_MAX_POINTS = Character.getProperty("NevitMaxPoints", 7200);
			NEVIT_BONUS_EFFECT_TIME = Character.getProperty("NevitBonusEffectTime", 180);
			ALLOW_RECO_BONUS_SYSTEM = Character.getProperty("AllowRecBonusSystem", true);
		}
		catch (final Exception e)
		{
			_log.warn("Config: " + e.getMessage());
			throw new Error("Failed to Load " + CHARACTER_CONFIG_FILE + " file.");
		}
	}
	
	private static void loadMmoSettings(InputStream is)
	{
		try
		{
			final GameSettings mmoSettings = new GameSettings();
			is = new FileInputStream(new File(MMO_CONFIG_FILE));
			mmoSettings.load(is);
			
			SELECTOR_CONFIG.SLEEP_TIME = mmoSettings.getProperty("SelectorSleepTime", 10);
			SELECTOR_CONFIG.INTEREST_DELAY = mmoSettings.getProperty("InterestDelay", 30);
			SELECTOR_CONFIG.MAX_SEND_PER_PASS = mmoSettings.getProperty("MaxSendPerPass", 32);
			SELECTOR_CONFIG.READ_BUFFER_SIZE = mmoSettings.getProperty("ReadBufferSize", 65536);
			SELECTOR_CONFIG.WRITE_BUFFER_SIZE = mmoSettings.getProperty("WriteBufferSize", 131072);
			SELECTOR_CONFIG.HELPER_BUFFER_COUNT = mmoSettings.getProperty("BufferPoolSize", 64);
		}
		catch (final Exception e)
		{
			_log.warn("Config: " + e.getMessage());
			throw new Error("Failed to Load " + MMO_CONFIG_FILE + " File.");
		}
	}
	
	private static void loadIdFactorySettings(InputStream is)
	{
		try
		{
			final GameSettings idSettings = new GameSettings();
			is = new FileInputStream(new File(ID_CONFIG_FILE));
			idSettings.load(is);
			
			IDFACTORY_TYPE = IdFactoryType.valueOf(idSettings.getProperty("IDFactory", "Compaction"));
			BAD_ID_CHECKING = idSettings.getProperty("BadIdChecking", true);
		}
		catch (final Exception e)
		{
			_log.warn("Config: " + e.getMessage());
			throw new Error("Failed to Load " + ID_CONFIG_FILE + " file.");
		}
	}
	
	private static void loadGeneralSettings(InputStream is)
	{
		try
		{
			final GameSettings General = new GameSettings();
			is = new FileInputStream(new File(GENERAL_CONFIG_FILE));
			General.load(is);
			
			ALLOW_СREATE_RACES = General.getIntProperty("AllowCreateRaces", "0,1,2,3,4,5,6", ",");
			Arrays.sort(ALLOW_СREATE_RACES);
			ALLOW_PRE_START_SYSTEM = General.getProperty("AllowPreStartSystem", false);
			PRE_START_PATTERN = ALLOW_PRE_START_SYSTEM ? new SchedulingPattern(General.getProperty("PreStartPattern", "* * * * *")).next(System.currentTimeMillis()) : -1;
			SERVER_STAGE = General.getProperty("ServerStage", "");
			EVERYBODY_HAS_ADMIN_RIGHTS = General.getProperty("EverybodyHasAdminRights", false);
			DEFAULT_ACCSESS_LEVEL = General.getProperty("DefaultAccessLevel", 0);
			LOG_CHAT = General.getProperty("LogChat", false);
			SERVICE_LOGS = General.getProperty("LogServices", false);
			LOG_ITEMS = General.getProperty("LogItems", false);
			LOG_ITEM_ENCHANTS = General.getProperty("LogItemEnchants", false);
			LOG_SKILL_ENCHANTS = General.getProperty("LogSkillEnchants", false);
			GMAUDIT = General.getProperty("GMAudit", false);
			LOG_GAME_DAMAGE = General.getProperty("LogGameDamage", false);
			LOG_GAME_DAMAGE_THRESHOLD = General.getProperty("LogGameDamageThreshold", 5000);
			DEBUG = General.getProperty("Debug", false);
			DEBUG_SPAWN = General.getProperty("DebugSpawn", false);
			TIME_ZONE_DEBUG = General.getProperty("TimeZonesDebug", false);
			SERVER_PACKET_HANDLER_DEBUG = General.getProperty("ServerPacketHandlerDebug", false);
			CLIENT_PACKET_HANDLER_DEBUG = General.getProperty("ClientPacketHandlerDebug", false);
			ALLOW_MULTISELL_DEBUG = General.getProperty("MutisellDebugPrice", false);
			DEVELOPER = General.getProperty("Developer", false);
			ALT_DEV_NO_HANDLERS = General.getProperty("AltDevNoHandlers", false);
			ALT_DEV_NO_SCRIPTS = General.getProperty("AltDevNoScripts", false);
			ALT_DEV_NO_SPAWNS = General.getProperty("AltDevNoSpawns", false);
			ALT_CHEST_NO_SPAWNS = General.getProperty("AltTreasureChestNoSpawns", false);
			SCHEDULED_THREAD_POOL_SIZE = General.getProperty("ScheduledThreadPoolSize", NCPUS * 4, false);
			EXECUTOR_THREAD_POOL_SIZE = General.getProperty("ExecutorThreadPoolSize", NCPUS * 2, false);
			ALLOW_DISCARDITEM = General.getProperty("AllowDiscardItem", true);
			LIST_DISCARDITEM_ITEMS = General.getIntegerProperty("ListOfDiscardItems", "0", ",");
			AUTODESTROY_ITEM_AFTER = General.getProperty("AutoDestroyDroppedItemAfter", 600);
			HERB_AUTO_DESTROY_TIME = General.getProperty("AutoDestroyHerbTime", 60);
			LIST_PROTECTED_ITEMS = General.getIntegerProperty("ListOfProtectedItems", "0", ",");
			DATABASE_CLEAN_UP = General.getProperty("DatabaseCleanUp", true);
			CHAR_STORE_INTERVAL = General.getProperty("CharacterDataStoreInterval", 15);
			CHAR_PREMIUM_ITEM_INTERVAL = General.getProperty("CharacterPremiumItemsInterval", 1);
			LAZY_ITEMS_UPDATE = General.getProperty("LazyItemsUpdate", false);
			UPDATE_ITEMS_ON_CHAR_STORE = General.getProperty("UpdateItemsOnCharStore", false);
			DESTROY_DROPPED_PLAYER_ITEM = General.getProperty("DestroyPlayerDroppedItem", false);
			DESTROY_EQUIPABLE_PLAYER_ITEM = General.getProperty("DestroyEquipableItem", false);
			AUTODELETE_INVALID_QUEST_DATA = General.getProperty("AutoDeleteInvalidQuestData", false);
			PRECISE_DROP_CALCULATION = General.getProperty("PreciseDropCalculation", true);
			MULTIPLE_ITEM_DROP = General.getProperty("MultipleItemDrop", true);
			FORCE_INVENTORY_UPDATE = General.getProperty("ForceInventoryUpdate", false);
			ALLOW_CACHE = General.getProperty("AllowHtmlCache", true);
			CACHE_CHAR_NAMES = General.getProperty("CacheCharNames", true);
			ENABLE_FALLING_DAMAGE = General.getProperty("EnableFallingDamage", true);
			PEACE_ZONE_MODE = General.getProperty("PeaceZoneMode", 0);
			ALLOW_WAREHOUSE = General.getProperty("AllowWarehouse", true);
			WAREHOUSE_CACHE = General.getProperty("WarehouseCache", false);
			WAREHOUSE_CACHE_TIME = General.getProperty("WarehouseCacheTime", 15);
			ALLOW_REFUND = General.getProperty("AllowRefund", true);
			ALLOW_MAIL = General.getProperty("AllowMail", true);
			MAIL_MIN_LEVEL = General.getProperty("MailMinLevel", 1);
			MAIL_EXPIRATION = General.getProperty("MailExpiration", 360);
			MAIL_COND_EXPIRATION = General.getProperty("MailCondExpiration", 12);
			ALLOW_ATTACHMENTS = General.getProperty("AllowAttachments", true);
			ALLOW_WEAR = General.getProperty("AllowWear", true);
			WEAR_DELAY = General.getProperty("WearDelay", 5);
			WEAR_PRICE = General.getProperty("WearPrice", 10);
			ALLOW_LOTTERY = General.getProperty("AllowLottery", true);
			ALLOW_RACE = General.getProperty("AllowRace", true);
			ALLOW_WATER = General.getProperty("AllowWater", true);
			ALLOW_RENTPET = General.getProperty("AllowRentPet", false);
			ALLOWFISHING = General.getProperty("AllowFishing", true);
			final String[] fishRewards = General.getProperty("FishingRewards", "1,57:800000;2,57:500000;3,57:300000;4,57:200000;5,57:100000").split(";");
			FISHING_REWARDS = new HashMap<>(fishRewards.length);
			for (final String rewards : fishRewards)
			{
				final String[] reward = rewards.split(",");
				if (reward.length == 2)
				{
					try
					{
						FISHING_REWARDS.put(Integer.parseInt(reward[0]), reward[1]);
					}
					catch (final NumberFormatException nfe)
					{
					}
				}
			}
			ALLOW_MANOR = General.getProperty("AllowManor", true);
			ALLOW_BOAT = General.getProperty("AllowBoat", true);
			BOAT_BROADCAST_RADIUS = General.getProperty("BoatBroadcastRadius", 20000);
			ALLOW_CURSED_WEAPONS = General.getProperty("AllowCursedWeapons", true);
			ALLOW_PET_WALKERS = General.getProperty("AllowPetWalkers", true);
			SERVER_NEWS = General.getProperty("ShowServerNews", false);
			ALT_MANOR_REFRESH_TIME = General.getProperty("AltManorRefreshTime", 20);
			ALT_MANOR_REFRESH_MIN = General.getProperty("AltManorRefreshMin", 00);
			ALT_MANOR_MAINTENANCE_MIN = General.getProperty("AltManorMaintenanceMin", 6);
			ALT_MANOR_APPROVE_TIME = General.getProperty("AltManorApproveTime", 4);
			ALT_MANOR_APPROVE_MIN = General.getProperty("AltManorApproveMin", 30);
			ALT_MANOR_SAVE_ALL_ACTIONS = General.getProperty("AltManorSaveAllActions", false);
			ALT_MANOR_SAVE_PERIOD_RATE = General.getProperty("AltManorSavePeriodRate", 2) * 3600000L;
			ALT_LOTTERY_PRIZE = General.getProperty("AltLotteryPrize", 50000);
			ALT_LOTTERY_TICKET_PRICE = General.getProperty("AltLotteryTicketPrice", 2000L);
			ALT_LOTTERY_5_NUMBER_RATE = General.getProperty("AltLottery5NumberRate", 0.6F);
			ALT_LOTTERY_4_NUMBER_RATE = General.getProperty("AltLottery4NumberRate", 0.2F);
			ALT_LOTTERY_3_NUMBER_RATE = General.getProperty("AltLottery3NumberRate", 0.2F);
			ALT_LOTTERY_2_AND_1_NUMBER_PRIZE = General.getProperty("AltLottery2and1NumberPrize", 200);
			ALT_ITEM_AUCTION_ENABLED = General.getProperty("AltItemAuctionEnabled", true);
			ALLOW_ITEM_AUCTION_ANNOUNCE = General.getProperty("AllowItemAuctionAnnounce", false);
			ALT_ITEM_AUCTION_EXPIRED_AFTER = General.getProperty("AltItemAuctionExpiredAfter", 14);
			ALT_ITEM_AUCTION_TIME_EXTENDS_ON_BID = 1000 * (long) General.getProperty("AltItemAuctionTimeExtendsOnBid", 0);
			FS_TIME_ATTACK = General.getProperty("TimeOfAttack", 50);
			FS_TIME_COOLDOWN = General.getProperty("TimeOfCoolDown", 5);
			FS_TIME_ENTRY = General.getProperty("TimeOfEntry", 3);
			FS_TIME_WARMUP = General.getProperty("TimeOfWarmUp", 2);
			FS_PARTY_MEMBER_COUNT = General.getProperty("NumberOfNecessaryPartyMembers", 4);
			if (FS_TIME_ATTACK <= 0)
			{
				FS_TIME_ATTACK = 50;
			}
			if (FS_TIME_COOLDOWN <= 0)
			{
				FS_TIME_COOLDOWN = 5;
			}
			if (FS_TIME_ENTRY <= 0)
			{
				FS_TIME_ENTRY = 3;
			}
			if (FS_TIME_ENTRY <= 0)
			{
				FS_TIME_ENTRY = 3;
			}
			if (FS_TIME_ENTRY <= 0)
			{
				FS_TIME_ENTRY = 3;
			}
			SAVE_GMSPAWN_ON_CUSTOM = General.getProperty("SaveGmSpawnOnCustom", false);
			ALT_BIRTHDAY_GIFT = General.getProperty("AltBirthdayGift", 22187);
			ALT_BIRTHDAY_MAIL_SUBJECT = General.getProperty("AltBirthdayMailSubject", "Happy Birthday!");
			ALT_BIRTHDAY_MAIL_TEXT = General.getProperty("AltBirthdayMailText", "Hello Adventurer!! Seeing as you're one year older now, I thought I would send you some birthday cheer :) Please find your birthday pack attached. May these gifts bring you joy and happiness on this very special day." + EOL + EOL + "Sincerely, Alegria");
			ENABLE_BLOCK_CHECKER_EVENT = General.getProperty("EnableBlockCheckerEvent", false);
			MIN_BLOCK_CHECKER_TEAM_MEMBERS = General.getProperty("BlockCheckerMinTeamMembers", 2);
			if (MIN_BLOCK_CHECKER_TEAM_MEMBERS < 1)
			{
				MIN_BLOCK_CHECKER_TEAM_MEMBERS = 1;
			}
			else if (MIN_BLOCK_CHECKER_TEAM_MEMBERS > 6)
			{
				MIN_BLOCK_CHECKER_TEAM_MEMBERS = 6;
			}
			HBCE_FAIR_PLAY = General.getProperty("HBCEFairPlay", false);
			CLEAR_CREST_CACHE = General.getProperty("ClearClanCache", false);
			NORMAL_ENCHANT_COST_MULTIPLIER = General.getProperty("NormalEnchantCostMultipiler", 1);
			SAFE_ENCHANT_COST_MULTIPLIER = General.getProperty("SafeEnchantCostMultipiler", 5);
			
		}
		catch (final Exception e)
		{
			_log.warn("Config: " + e.getMessage());
			throw new Error("Failed to Load " + GENERAL_CONFIG_FILE + " File.");
		}
	}
	
	private static void loadFloodProtectorSettings(InputStream is)
	{
		try
		{
			final GameSettings security = new GameSettings();
			is = new FileInputStream(new File(FLOOD_PROTECTOR_FILE));
			security.load(is);
			
			loadFloodProtectorConfigs(security);
		}
		catch (final Exception e)
		{
			_log.warn("Config: " + e.getMessage());
			throw new Error("Failed to Load " + FLOOD_PROTECTOR_FILE);
		}
	}
	
	private static void loadNpcsSettings(InputStream is)
	{
		try
		{
			final GameSettings NPC = new GameSettings();
			is = new FileInputStream(new File(NPC_CONFIG_FILE));
			NPC.load(is);
			
			NPC_ANIMATION_INTERVAL = NPC.getProperty("NpcAnimationInterval", 10) * 1000L;
			NPC_ANIMATION_CHANCE = NPC.getProperty("NpcAnimationChance", 10);
			MIN_MONSTER_ANIMATION = NPC.getProperty("MonsterAnimationMinInterval", 5) * 1000;
			MAX_MONSTER_ANIMATION = NPC.getProperty("MonsterAnimationMaxInterval", 20) * 1000;
			MONSTER_ANIMATION_CHANCE = NPC.getProperty("MonsterAnimationChance", 5);
			DISABLE_NPC_BYPASSES = NPC.getListProperty("DisableNpcBypassList", "", ",");
			NPC_SHIFT_COMMAND = NPC.getProperty("NpcShiftCommand", "");
			NPC_AI_TIME_TASK = NPC.getProperty("NpcAiTimeTask", 500);
			NPC_AI_FACTION_TASK = NPC.getProperty("NpcAiFactionTimeTask", 1000);
			NPC_AI_RNDWALK_CHANCE = NPC.getProperty("NpcRandomWalkChance", 1);
			PLAYER_MOVEMENT_BLOCK_TIME = NPC.getProperty("NpcTalkBlockingTime", 0) * 1000;
			ANNOUNCE_MAMMON_SPAWN = NPC.getProperty("AnnounceMammonSpawn", false);
			ALT_MOB_AGRO_IN_PEACEZONE = NPC.getProperty("AltMobAgroInPeaceZone", true);
			ALT_ATTACKABLE_NPCS = NPC.getProperty("AltAttackableNpcs", true);
			ALT_GAME_VIEWNPC = NPC.getProperty("AltGameViewNpc", false);
			MAX_DRIFT_RANGE = NPC.getProperty("MaxDriftRange", 300);
			DEEPBLUE_DROP_RULES = NPC.getProperty("UseDeepBlueDropRules", true);
			DEEPBLUE_DROP_MAXDIFF = NPC.getProperty("DeepBlueDropMaxDiff", 8);
			DEEPBLUE_DROP_RAID_MAXDIFF = NPC.getProperty("DeepBlueDropRaidMaxDiff", 2);
			SHOW_NPC_SERVER_NAME = NPC.getProperty("ShowNpcServerName", false);
			SHOW_NPC_SERVER_TITLE = NPC.getProperty("ShowNpcServerTitle", false);
			SHOW_NPC_LVL = NPC.getProperty("ShowNpcLevel", false);
			SHOW_CREST_WITHOUT_QUEST = NPC.getProperty("ShowCrestWithoutQuest", false);
			ENABLE_RANDOM_ENCHANT_EFFECT = NPC.getProperty("EnableRandomEnchantEffect", false);
			NPC_DEAD_TIME_TASK = NPC.getProperty("NpcDeadTimeTask", 3);
			NPC_DECAY_TIME = NPC.getProperty("NpcDecayTime", 7);
			RAID_BOSS_DECAY_TIME = NPC.getProperty("RaidBossDecayTime", 30);
			SPOILED_DECAY_TIME = NPC.getProperty("SpoiledDecayTime", 10);
			MAX_SWEEPER_TIME = NPC.getProperty("SweeperTimeTimeBeforeDecay", 2);
			GUARD_ATTACK_AGGRO_MOB = NPC.getProperty("GuardAttackAggroMob", false);
			ALLOW_WYVERN_UPGRADER = NPC.getProperty("AllowWyvernUpgrader", false);
			LIST_PET_RENT_NPC = NPC.getIntegerProperty("ListPetRentNpc", "30827", ",");
			RAID_HP_REGEN_MULTIPLIER = NPC.getProperty("RaidHpRegenMultiplier", 100.) / 100;
			RAID_MP_REGEN_MULTIPLIER = NPC.getProperty("RaidMpRegenMultiplier", 100.) / 100;
			RAID_MIN_RESPAWN_MULTIPLIER = NPC.getProperty("RaidMinRespawnMultiplier", 1.0F);
			RAID_MAX_RESPAWN_MULTIPLIER = NPC.getProperty("RaidMaxRespawnMultiplier", 1.0F);
			RAID_MINION_RESPAWN_TIMER = NPC.getProperty("RaidMinionRespawnTime", 300000);
			MINIONS_RESPAWN_TIME = NPC.getMapProperty("CustomMinionsRespawnTime", "", ";");
			RAID_DISABLE_CURSE = NPC.getProperty("DisableRaidCurse", false);
			INVENTORY_MAXIMUM_PET = NPC.getProperty("MaximumSlotsForPet", 12);
			PET_HP_REGEN_MULTIPLIER = NPC.getProperty("PetHpRegenMultiplier", 100.) / 100;
			PET_MP_REGEN_MULTIPLIER = NPC.getProperty("PetMpRegenMultiplier", 100.) / 100;
			LAKFI_ENABLED = NPC.getProperty("LakfiSpawnEnabled", true);
			TIME_CHANGE_SPAWN = NPC.getProperty("IntervalChangeSpawn", 20);
			MIN_ADENA_TO_EAT = NPC.getProperty("MinAdenaLakfiEat", 10000);
			TIME_IF_NOT_FEED = NPC.getProperty("TimeIfNotFeedDissapear", 10);
			INTERVAL_EATING = NPC.getProperty("IntervalBetweenEating", 15);
			DRAGON_VORTEX_UNLIMITED_SPAWN = NPC.getProperty("DragonVortexUnlimitedSpawn", false);
			ALLOW_RAIDBOSS_CHANCE_DEBUFF = NPC.getProperty("AllowRaidBossDebuff", true);
			RAIDBOSS_CHANCE_DEBUFF = NPC.getProperty("RaidBossChanceDebuff", 0.9);
			ALLOW_GRANDBOSS_CHANCE_DEBUFF = NPC.getProperty("AllowGrandBossDebuff", true);
			GRANDBOSS_CHANCE_DEBUFF = NPC.getProperty("GrandBossChanceDebuff", 0.3);
			RAIDBOSS_CHANCE_DEBUFF_SPECIAL = NPC.getProperty("RaidBossChanceDebuffSpecial", 0.4);
			GRANDBOSS_CHANCE_DEBUFF_SPECIAL = NPC.getProperty("GrandBossChanceDebuffSpecial", 0.1);
			RAIDBOSS_DEBUFF_SPECIAL = NPC.getIntProperty("SpecialRaidBossList", "29020,29068,29028", ",");
			Arrays.sort(RAIDBOSS_DEBUFF_SPECIAL);
			GRANDBOSS_DEBUFF_SPECIAL = NPC.getIntProperty("SpecialGrandBossList", "29020,29068,29028", ",");
			Arrays.sort(GRANDBOSS_DEBUFF_SPECIAL);
			SOULSHOT_CHANCE = NPC.getProperty("SoulShotsChance", 30);
			SPIRITSHOT_CHANCE = NPC.getProperty("SpiritShotsChance", 30);
			ALWAYS_TELEPORT_HOME = NPC.getProperty("AlwaysTeleportHome", false);
			MAX_PURSUE_RANGE = NPC.getProperty("MaxPursueRange", 4000);
			MAX_PURSUE_RANGE_RAID = NPC.getProperty("MaxPursueRangeRaid", 5000);
			CALC_NPC_STATS = NPC.getProperty("CalcNpcStats", false);
			CALC_RAID_STATS = NPC.getProperty("CalcRaidStats", false);
			CALC_NPC_DEBUFFS_BY_STATS = NPC.getProperty("CalcNpcDebuffByStats", false);
			CALC_RAID_DEBUFFS_BY_STATS = NPC.getProperty("CalcRaidDebuffByStats", false);
			MONSTER_RACE_TP_TO_TOWN = NPC.getProperty("MonsterRaceTeleToTown", true);
			NPC_BLOCK_SHIFT_LIST = NPC.getIntProperty("NpcBlockShiftList", "0", ",");
			Arrays.sort(NPC_BLOCK_SHIFT_LIST);
			EPAULETTE_ONLY_FOR_REG = NPC.getProperty("DropEpauletteForRegisterPlayers", false);
			EPAULETTE_WITHOUT_PENALTY = NPC.getProperty("DropEpauletteWithoutPenalty", false);
			SKILLS_MOB_CHANCE = NPC.getProperty("SkillsMobChance", 0.5);
			ALLOW_NPC_LVL_MOD = NPC.getProperty("AllowNpcLvlMod", false);
			ALLOW_SUMMON_LVL_MOD = NPC.getProperty("AllowSummonLvlMod", false);
			PATK_HATE_MOD = NPC.getProperty("PAtkHateModifier", 1.0);
			MATK_HATE_MOD = NPC.getProperty("MAtkHateModifier", 1.0);
			PET_HATE_MOD = NPC.getProperty("SummonAtkHateModifier", 1.0);
			RAIDBOSS_ANNOUNCE_LIST = NPC.getIntProperty("RaidAnnounceList", "0", ",");
			Arrays.sort(RAIDBOSS_ANNOUNCE_LIST);
			GRANDBOSS_ANNOUNCE_LIST = NPC.getIntProperty("EpicAnnounceList", "0", ",");
			Arrays.sort(GRANDBOSS_ANNOUNCE_LIST);
			RAIDBOSS_DEAD_ANNOUNCE_LIST = NPC.getIntProperty("RaidDeathAnnounceList", "0", ",");
			Arrays.sort(RAIDBOSS_DEAD_ANNOUNCE_LIST);
			GRANDBOSS_DEAD_ANNOUNCE_LIST = NPC.getIntProperty("EpicDeathAnnounceList", "0", ",");
			Arrays.sort(GRANDBOSS_DEAD_ANNOUNCE_LIST);
			EPICBOSS_PRE_ANNOUNCE_LIST = NPC.getMapProperty("EpicPreAnnounceList", "", ";");
			RAIDBOSS_PRE_ANNOUNCE_LIST = NPC.getMapProperty("RaidPreAnnounceList", "", ";");
			ALLOW_DAMAGE_LIMIT = NPC.getProperty("AllowDamageLimit", false);
			NPC_DROP_PROTECTION = NPC.getProperty("NpcDropProtection", 15);
			RAID_DROP_PROTECTION = NPC.getProperty("RaidDropProtection", 300);
			SPAWN_MULTIPLIER = NPC.getProperty("SpawnMultiplier", 1.0);
			RESPAWN_MULTIPLIER = NPC.getProperty("RespawnMultiplier", 1.0);
			DRAGON_MIGRATION_PERIOD = NPC.getProperty("DragonValleyMigrationPeriod", 60);
			DRAGON_MIGRATION_CHANCE = NPC.getProperty("DragonValleyMigrationChance", 30);
		}
		catch (final Exception e)
		{
			_log.warn("Config: " + e.getMessage());
			throw new Error("Failed to Load " + NPC_CONFIG_FILE + " File.");
		}
	}
	
	private static void loadRatesSettings(InputStream is)
	{
		try
		{
			final GameSettings ratesSettings = new GameSettings();
			is = new FileInputStream(new File(RATES_CONFIG_FILE));
			ratesSettings.load(is);
			
			MAX_DROP_ITEMS_FROM_ONE_GROUP = ratesSettings.getProperty("MaxDropItemsFromOneGroup", 1);
			MAX_SPOIL_ITEMS_FROM_ONE_GROUP = ratesSettings.getProperty("MaxSpoilItemsFromOneGroup", 1);
			MAX_DROP_ITEMS_FROM_ONE_GROUP_RAIDS = ratesSettings.getProperty("MaxRaidDropItemsFromOneGroup", 1);
			GROUP_CHANCE_MODIFIER = ratesSettings.getProperty("GroupChanceModifier", 0.1);
			RAID_GROUP_CHANCE_MOD = ratesSettings.getProperty("RaidGroupChanceModifier", 1.0);
			RAID_ITEM_CHANCE_MOD = ratesSettings.getProperty("RaidItemChanceModifier", 1.0);
			RATE_XP_BY_LVL = new double[ExperienceParser.getInstance().getMaxLevel()];
			double prevRateXp = 1.;
			for (int i = 1; i < RATE_XP_BY_LVL.length; i++)
			{
				final double rate = ratesSettings.getProperty("RateXpByLevel" + i, prevRateXp, false);
				RATE_XP_BY_LVL[i] = rate;
				if (rate != prevRateXp)
				{
					prevRateXp = rate;
				}
			}
			
			RATE_SP_BY_LVL = new double[ExperienceParser.getInstance().getMaxLevel()];
			double prevRateSp = 1.;
			for (int i = 1; i < RATE_SP_BY_LVL.length; i++)
			{
				final double rate = ratesSettings.getProperty("RateSpByLevel" + i, prevRateSp, false);
				RATE_SP_BY_LVL[i] = rate;
				if (rate != prevRateSp)
				{
					prevRateSp = rate;
				}
			}
			
			RATE_RAID_XP_BY_LVL = new double[ExperienceParser.getInstance().getMaxLevel()];
			double prevRateRaidXp = 1.;
			for (int i = 1; i < RATE_RAID_XP_BY_LVL.length; i++)
			{
				final double rate = ratesSettings.getProperty("RateRaidXpByLevel" + i, prevRateRaidXp, false);
				RATE_RAID_XP_BY_LVL[i] = rate;
				if (rate != prevRateRaidXp)
				{
					prevRateRaidXp = rate;
				}
			}
			
			RATE_RAID_SP_BY_LVL = new double[ExperienceParser.getInstance().getMaxLevel()];
			double prevRateRaidSp = 1.;
			for (int i = 1; i < RATE_RAID_SP_BY_LVL.length; i++)
			{
				final double rate = ratesSettings.getProperty("RateRaidSpByLevel" + i, prevRateRaidSp, false);
				RATE_RAID_SP_BY_LVL[i] = rate;
				if (rate != prevRateRaidSp)
				{
					prevRateRaidSp = rate;
				}
			}
			
			RATE_PARTY_XP = ratesSettings.getProperty("RatePartyXp", 1.);
			RATE_PARTY_SP = ratesSettings.getProperty("RatePartySp", 1.);
			RATE_DROP_ADENA = ratesSettings.getProperty("RateDropAdena", 1.);
			RATE_DROP_ITEMS = ratesSettings.getProperty("RateDropItems", 1.);
			RATE_CHANCE_ATTRIBUTE = ratesSettings.getProperty("RateChanceAttribute", 1.);
			RATE_CHANCE_COMMON = ratesSettings.getProperty("RateChanceDropCommonItems", 1.);
			RATE_DROP_SPOIL = ratesSettings.getProperty("RateDropSpoil", 1.);
			RATE_DROP_RAIDBOSS = ratesSettings.getProperty("RateRaidBoss", 1.);
			RATE_DROP_EPICBOSS = ratesSettings.getProperty("RateEpicBoss", 1.);
			RATE_CHANCE_GROUP_DROP_ITEMS = ratesSettings.getProperty("RateChanceGroupDropItems", 1.);
			RATE_CHANCE_DROP_ITEMS = ratesSettings.getProperty("RateChanceDropItems", 1.);
			RATE_CHANCE_DROP_HERBS = ratesSettings.getProperty("RateChanceDropHerbs", 1.);
			RATE_CHANCE_SPOIL = ratesSettings.getProperty("RateChanceSpoil", 1.);
			RATE_CHANCE_SPOIL_WEAPON_ARMOR_ACCESSORY = ratesSettings.getProperty("RateChanceSpoilWAA", 1.);
			RATE_CHANCE_DROP_WEAPON_ARMOR_ACCESSORY = ratesSettings.getProperty("RateChanceDropWAA", 1.);
			RATE_CHANCE_DROP_EPOLET = ratesSettings.getProperty("RateChanceDropEpolets", 1.);
			RATE_DROP_SIEGE_GUARD = ratesSettings.getProperty("RateSiegeGuard", 1.);
			RATE_DROP_FISHING = ratesSettings.getProperty("RateFishing", 1.);
			RATE_NOBLE_STONES_COUNT_MIN = ratesSettings.getProperty("ModifierNobleStonesMinCount", 1.);
			RATE_LIFE_STONES_COUNT_MIN = ratesSettings.getProperty("ModifierLifeStonesMinCount", 1.);
			RATE_ENCHANT_SCROLLS_COUNT_MIN = ratesSettings.getProperty("ModifierEnchantScrollsMinCount", 1.);
			RATE_FORGOTTEN_SCROLLS_COUNT_MIN = ratesSettings.getProperty("ModifierForgottenScrollsMinCount", 1.);
			RATE_KEY_MATHETIRALS_COUNT_MIN = ratesSettings.getProperty("ModifierMaterialsMinCount", 1.);
			RATE_RECEPIES_COUNT_MIN = ratesSettings.getProperty("ModifierRepicesMinCount", 1.);
			RATE_BELTS_COUNT_MIN = ratesSettings.getProperty("ModifierBeltsMinCount", 1.);
			RATE_BRACELETS_COUNT_MIN = ratesSettings.getProperty("ModifierBraceletsMinCount", 1.);
			RATE_CLOAKS_COUNT_MIN = ratesSettings.getProperty("ModifierCloaksMinCount", 1.);
			RATE_CODEX_BOOKS_COUNT_MIN = ratesSettings.getProperty("ModifierCodexBooksMinCount", 1.);
			RATE_ATTRIBUTE_STONES_COUNT_MIN = ratesSettings.getProperty("ModifierAttStonesMinCount", 1.);
			RATE_ATTRIBUTE_CRYSTALS_COUNT_MIN = ratesSettings.getProperty("ModifierAttCrystalsMinCount", 1.);
			RATE_ATTRIBUTE_JEWELS_COUNT_MIN = ratesSettings.getProperty("ModifierAttJewelsMinCount", 1.);
			RATE_ATTRIBUTE_ENERGY_COUNT_MIN = ratesSettings.getProperty("ModifierAttEnergyMinCount", 1.);
			RATE_WEAPONS_COUNT_MIN = ratesSettings.getProperty("ModifierWeaponsMinCount", 1.);
			RATE_ARMOR_COUNT_MIN = ratesSettings.getProperty("ModifierArmorsMinCount", 1.);
			RATE_ACCESSORY_COUNT_MIN = ratesSettings.getProperty("ModifierAccessoryesMinCount", 1.);
			RATE_SEAL_STONES_COUNT_MIN = ratesSettings.getProperty("ModifierSealStonesMinCount", 1.);
			RATE_NOBLE_STONES_COUNT_MAX = ratesSettings.getProperty("ModifierNobleStonesMaxCount", 1.);
			RATE_LIFE_STONES_COUNT_MAX = ratesSettings.getProperty("ModifierLifeStonesMaxCount", 1.);
			RATE_ENCHANT_SCROLLS_COUNT_MAX = ratesSettings.getProperty("ModifierEnchantScrollsMaxCount", 1.);
			RATE_FORGOTTEN_SCROLLS_COUNT_MAX = ratesSettings.getProperty("ModifierForgottenScrollsMaxCount", 1.);
			RATE_KEY_MATHETIRALS_COUNT_MAX = ratesSettings.getProperty("ModifierMaterialsMaxCount", 1.);
			RATE_RECEPIES_COUNT_MAX = ratesSettings.getProperty("ModifierRepicesMaxCount", 1.);
			RATE_BELTS_COUNT_MAX = ratesSettings.getProperty("ModifierBeltsMaxCount", 1.);
			RATE_BRACELETS_COUNT_MAX = ratesSettings.getProperty("ModifierBraceletsMaxCount", 1.);
			RATE_CLOAKS_COUNT_MAX = ratesSettings.getProperty("ModifierCloaksMaxCount", 1.);
			RATE_CODEX_BOOKS_COUNT_MAX = ratesSettings.getProperty("ModifierCodexBooksMaxCount", 1.);
			RATE_ATTRIBUTE_STONES_COUNT_MAX = ratesSettings.getProperty("ModifierAttStonesMaxCount", 1.);
			RATE_ATTRIBUTE_CRYSTALS_COUNT_MAX = ratesSettings.getProperty("ModifierAttCrystalsMaxCount", 1.);
			RATE_ATTRIBUTE_JEWELS_COUNT_MAX = ratesSettings.getProperty("ModifierAttJewelsMaxCount", 1.);
			RATE_ATTRIBUTE_ENERGY_COUNT_MAX = ratesSettings.getProperty("ModifierAttEnergyMaxCount", 1.);
			RATE_WEAPONS_COUNT_MAX = ratesSettings.getProperty("ModifierWeaponsMaxCount", 1.);
			RATE_ARMOR_COUNT_MAX = ratesSettings.getProperty("ModifierArmorsMaxCount", 1.);
			RATE_ACCESSORY_COUNT_MAX = ratesSettings.getProperty("ModifierAccessoryesMaxCount", 1.);
			RATE_SEAL_STONES_COUNT_MAX = ratesSettings.getProperty("ModifierSealStonesMaxCount", 1.);
			final BiFunction<String, String, Map<Integer, Double>> parseItemsRates = (paramName, defaultValue) ->
			{
				final String[] rates = ratesSettings.getProperty(paramName, defaultValue).split(";");
				final Map<Integer, Double> res = new HashMap<>(rates.length);
				for (final String entry : rates)
				{
					final String[] entrySplit = entry.split(",");
					if (entrySplit.length != 2)
					{
						_log.warn("[Config.load()]: invalid config property -> " + paramName + " " + entry + "");
						continue;
					}
					
					try
					{
						res.put(Integer.parseInt(entrySplit[0]), Double.parseDouble(entrySplit[1]));
					}
					catch (final Exception e)
					{
						_log.warn("[Config.load()]: " + paramName + " invalid params!");
					}
				}
				return res;
			};
			ALLOW_MODIFIER_FOR_DROP = ratesSettings.getProperty("AllowModifierForDrop", true);
			ALLOW_MODIFIER_FOR_RAIDS = ratesSettings.getProperty("AllowModifierForRaids", true);
			ALLOW_MODIFIER_FOR_SPOIL = ratesSettings.getProperty("AllowModifierForSpoil", true);
			NO_RATE_EQUIPMENT = ratesSettings.getProperty("NoRateEquipment", true);
			NO_RATE_KEY_MATERIAL = ratesSettings.getProperty("NoRateKeyMaterial", true);
			NO_RATE_RECIPES = ratesSettings.getProperty("NoRateRecipes", true);
			NO_RATE_ITEMS = ratesSettings.getIntProperty("NoRateItemIds", "6660,6662,6661,6659,6656,6658,8191,6657,10170,10314,16025,16026", ",");
			Arrays.sort(NO_RATE_ITEMS);
			NO_RATE_GROUPS = ratesSettings.getProperty("NoRateGroupsForNoRateItems", true);
			MAX_AMOUNT_CORRECTOR = parseItemsRates.apply("MaxAmountCorrectListMod", "2,0.5;5,0.4;10,0.3;50,0.25;100");
			RATE_CONSUMABLE_COST = ratesSettings.getProperty("RateConsumableCost", 1.);
			RATE_EXTRACTABLE = ratesSettings.getProperty("RateExtractable", 1.);
			RATE_DROP_MANOR = ratesSettings.getProperty("RateDropManor", 1.);
			RATE_QUEST_DROP = ratesSettings.getProperty("RateQuestDrop", 1F);
			RATE_QUEST_REWARD = ratesSettings.getProperty("RateQuestReward", 1F);
			RATE_QUEST_REWARD_XP = ratesSettings.getProperty("RateQuestRewardXP", 1F);
			RATE_QUEST_REWARD_SP = ratesSettings.getProperty("RateQuestRewardSP", 1F);
			RATE_QUEST_REWARD_ADENA = ratesSettings.getProperty("RateQuestRewardAdena", 1F);
			RATE_QUEST_REWARD_USE_MULTIPLIERS = ratesSettings.getProperty("UseQuestRewardMultipliers", false);
			RATE_QUEST_REWARD_POTION = ratesSettings.getProperty("RateQuestRewardPotion", 1F);
			RATE_QUEST_REWARD_SCROLL = ratesSettings.getProperty("RateQuestRewardScroll", 1F);
			RATE_QUEST_REWARD_RECIPE = ratesSettings.getProperty("RateQuestRewardRecipe", 1F);
			RATE_QUEST_REWARD_MATERIAL = ratesSettings.getProperty("RateQuestRewardMaterial", 1F);
			ADENA_FIXED_CHANCE = ratesSettings.getProperty("AdenaFixedChance", 0.);
			RATE_HB_TRUST_INCREASE = ratesSettings.getProperty("RateHellboundTrustIncrease", 1.);
			RATE_HB_TRUST_DECREASE = ratesSettings.getProperty("RateHellboundTrustDecrease", 1.);
			
			RATE_VITALITY_LEVEL_1 = ratesSettings.getProperty("RateVitalityLevel1", 1.5F);
			RATE_VITALITY_LEVEL_2 = ratesSettings.getProperty("RateVitalityLevel2", 2F);
			RATE_VITALITY_LEVEL_3 = ratesSettings.getProperty("RateVitalityLevel3", 2.5F);
			RATE_VITALITY_LEVEL_4 = ratesSettings.getProperty("RateVitalityLevel4", 3F);
			RATE_RECOVERY_VITALITY_PEACE_ZONE = ratesSettings.getProperty("RateRecoveryPeaceZone", 1F);
			RATE_VITALITY_LOST = ratesSettings.getProperty("RateVitalityLost", 1F);
			RATE_VITALITY_GAIN = ratesSettings.getProperty("RateVitalityGain", 1F);
			RATE_RECOVERY_ON_RECONNECT = ratesSettings.getProperty("RateRecoveryOnReconnect", 4F);
			RATE_KARMA_EXP_LOST = ratesSettings.getProperty("RateKarmaExpLost", 1F);
			RATE_SIEGE_GUARDS_PRICE = ratesSettings.getProperty("RateSiegeGuardsPrice", 1F);
			PLAYER_DROP_LIMIT = ratesSettings.getProperty("PlayerDropLimit", 3);
			PLAYER_RATE_DROP = ratesSettings.getProperty("PlayerRateDrop", 5);
			PLAYER_RATE_DROP_ITEM = ratesSettings.getProperty("PlayerRateDropItem", 70);
			PLAYER_RATE_DROP_EQUIP = ratesSettings.getProperty("PlayerRateDropEquip", 25);
			PLAYER_RATE_DROP_EQUIP_WEAPON = ratesSettings.getProperty("PlayerRateDropEquipWeapon", 5);
			PET_XP_RATE = ratesSettings.getProperty("PetXpRate", 1F);
			PET_FOOD_RATE = ratesSettings.getProperty("PetFoodRate", 1);
			SINEATER_XP_RATE = ratesSettings.getProperty("SinEaterXpRate", 1F);
			KARMA_DROP_LIMIT = ratesSettings.getProperty("KarmaDropLimit", 10);
			KARMA_RATE_DROP = ratesSettings.getProperty("KarmaRateDrop", 70);
			KARMA_RATE_DROP_ITEM = ratesSettings.getProperty("KarmaRateDropItem", 50);
			KARMA_RATE_DROP_EQUIP = ratesSettings.getProperty("KarmaRateDropEquip", 40);
			KARMA_RATE_DROP_EQUIP_WEAPON = ratesSettings.getProperty("KarmaRateDropEquipWeapon", 10);
			DISABLE_ITEM_DROP_LIST = ratesSettings.getIntegerProperty("DisableItemDropList", "0", ",");
			RATE_TALISMAN_MULTIPLIER = ratesSettings.getProperty("TalismanAmountMultiplier", 1);
			RATE_TALISMAN_ITEM_MULTIPLIER = ratesSettings.getProperty("TalismanItemAmountMultiplier", 1);
			NO_DROP_ITEMS_FOR_SWEEP = ratesSettings.getIntegerProperty("DisableItemSweepList", "0", ",");
			ALLOW_ONLY_THESE_DROP_ITEMS_ID = ratesSettings.getIntegerProperty("DropOnlyTheseItemList", "0", ",");
		}
		catch (final Exception e)
		{
			_log.warn("Config: " + e.getMessage());
			throw new Error("Failed to Load " + RATES_CONFIG_FILE + " File.");
		}
	}
	
	private static void loadPvpSettings(InputStream is)
	{
		try
		{
			final GameSettings pvpSettings = new GameSettings();
			is = new FileInputStream(new File(PVP_CONFIG_FILE));
			pvpSettings.load(is);
			
			KARMA_MIN_KARMA = pvpSettings.getProperty("MinKarma", 240);
			KARMA_MAX_KARMA = pvpSettings.getProperty("MaxKarma", 10000);
			KARMA_XP_DIVIDER = pvpSettings.getProperty("XPDivider", 260);
			KARMA_LOST_BASE = pvpSettings.getProperty("BaseKarmaLost", 0);
			KARMA_DROP_GM = pvpSettings.getProperty("CanGMDropEquipment", false);
			KARMA_AWARD_PK_KILL = pvpSettings.getProperty("AwardPKKillPVPPoint", true);
			KARMA_PK_LIMIT = pvpSettings.getProperty("MinimumPKRequiredToDrop", 5);
			KARMA_LIST_NONDROPPABLE_PET_ITEMS = pvpSettings.getIntProperty("ListOfPetItems", "2375,3500,3501,3502,4422,4423,4424,4425,6648,6649,6650,9882", ",");
			KARMA_LIST_NONDROPPABLE_ITEMS = pvpSettings.getIntProperty("ListOfNonDroppableItems", "57,1147,425,1146,461,10,2368,7,6,2370,2369,6842,6611,6612,6613,6614,6615,6616,6617,6618,6619,6620,6621,7694,8181,5575,7694,9388,9389,9390", ",");
			Arrays.sort(KARMA_LIST_NONDROPPABLE_PET_ITEMS);
			Arrays.sort(KARMA_LIST_NONDROPPABLE_ITEMS);
			PVP_NORMAL_TIME = pvpSettings.getProperty("PvPVsNormalTime", 120000);
			PVP_PVP_TIME = pvpSettings.getProperty("PvPVsPvPTime", 60000);
			PVP_ABSORB_DAMAGE = pvpSettings.getProperty("BlockAbsorbInPvP", false);
			DISABLE_ATTACK_IF_LVL_DIFFERENCE_OVER = pvpSettings.getProperty("DisableAttackIfLvlDifferenceOver", 0);
		}
		catch (final Exception e)
		{
			_log.warn("Config: " + e.getMessage());
			throw new Error("Failed to Load " + PVP_CONFIG_FILE + " File.");
		}
	}
	
	private static void loadOlympiadSettings(InputStream is)
	{
		try
		{
			final GameSettings olympiad = new GameSettings();
			is = new FileInputStream(new File(OLYMPIAD_CONFIG_FILE));
			olympiad.load(is);
			
			OLYMPIAD_PERIOD = olympiad.getProperty("OlympiadPeriod", "0 0 1 * *");
			ALLOW_STOP_ALL_CUBICS = olympiad.getProperty("AllowStopAllCubics", false);
			ALLOW_UNSUMMON_ALL = olympiad.getProperty("AllowUnSummonAll", false);
			CHECK_CLASS_SKILLS = olympiad.getProperty("CheckClassSkills", false);
			AUTO_GET_HERO = olympiad.getProperty("AllowAutoGetHero", false);
			ALLOW_PRINT_OLY_INFO = olympiad.getProperty("AllowPrintOlyInfo", false);
			ALLOW_OLY_HIT_SUMMON = olympiad.getProperty("AllowToHitSummon", false);
			OLY_PRINT_CLASS_OPPONENT = olympiad.getProperty("AllowPrintOpponentInfo", false);
			ALLOW_WINNER_ANNOUNCE = olympiad.getProperty("AnnounceBattleResult", false);
			ALLOW_OLY_FAST_INVITE = olympiad.getProperty("AllowConfirmDlgInvite", false);
			ALLOW_RESTART_AT_OLY = olympiad.getProperty("AllowRestartAtOly", true);
			OLY_PAUSE_BATTLES_AT_SIEGES = olympiad.getProperty("AllowPauseBattlesAtSieges", false);
			ALT_OLY_START_TIME = olympiad.getProperty("AltOlyStartTime", "0 18 * * *");
			ALT_OLY_CPERIOD = olympiad.getProperty("AltOlyCompetitionPeriod", 6);
			ALT_OLY_BATTLE = olympiad.getProperty("AltOlyBattle", 5);
			ALT_OLY_TELE_TO_TOWN = olympiad.getProperty("AltOlyTeleToTown", 40);
			OLYMPIAD_WEEKLY_PERIOD = olympiad.getProperty("AltOlyWeeklyPeriod", "0 12 * * 1");
			ALT_OLY_VPERIOD = olympiad.getProperty("AltOlyValidationPeriod", 12);
			ALT_OLY_START_POINTS = olympiad.getProperty("AltOlyStartPoints", 10);
			ALT_OLY_WEEKLY_POINTS = olympiad.getProperty("AltOlyWeeklyPoints", 10);
			ALT_OLY_DAILY_POINTS = olympiad.getProperty("AltOlyDailyPoints", 0);
			ALT_OLY_CLASSED = olympiad.getProperty("AltOlyClassedParticipants", 11);
			ALT_OLY_NONCLASSED = olympiad.getProperty("AltOlyNonClassedParticipants", 11);
			ALT_OLY_TEAMS = olympiad.getProperty("AltOlyTeamsParticipants", 6);
			ALT_OLY_REG_DISPLAY = olympiad.getProperty("AltOlyRegistrationDisplayNumber", 100);
			ALT_OLY_CLASSED_REWARD = olympiad.getDoubleIntProperty("AltOlyClassedReward", "13722,50", ";");
			ALT_OLY_CLASSED_LOSE_REWARD = olympiad.getDoubleIntProperty("AltOlyClassedLoseReward", "", ";");
			ALT_OLY_NONCLASSED_REWARD = olympiad.getDoubleIntProperty("AltOlyNonClassedReward", "13722,40", ";");
			ALT_OLY_NONCLASSED_LOSE_REWARD = olympiad.getDoubleIntProperty("AltOlyNonClassedLoseReward", "", ";");
			ALT_OLY_TEAM_REWARD = olympiad.getDoubleIntProperty("AltOlyTeamReward", "13722,85", ";");
			ALT_OLY_TEAM_LOSE_REWARD = olympiad.getDoubleIntProperty("AltOlyTeamLoseReward", "", ";");
			ALT_OLY_COMP_RITEM = olympiad.getProperty("AltOlyCompRewItem", 13722);
			ALT_OLY_MIN_MATCHES = olympiad.getProperty("AltOlyMinMatchesForPoints", 15);
			ALT_OLY_GP_PER_POINT = olympiad.getProperty("AltOlyGPPerPoint", 1000);
			ALT_OLY_HERO_POINTS = olympiad.getProperty("AltOlyHeroPoints", 200);
			ALT_OLY_RANK1_POINTS = olympiad.getProperty("AltOlyRank1Points", 100);
			ALT_OLY_RANK2_POINTS = olympiad.getProperty("AltOlyRank2Points", 75);
			ALT_OLY_RANK3_POINTS = olympiad.getProperty("AltOlyRank3Points", 55);
			ALT_OLY_RANK4_POINTS = olympiad.getProperty("AltOlyRank4Points", 40);
			ALT_OLY_RANK5_POINTS = olympiad.getProperty("AltOlyRank5Points", 30);
			ALT_OLY_MAX_POINTS = olympiad.getProperty("AltOlyMaxPoints", 10);
			ALT_OLY_DIVIDER_CLASSED = olympiad.getProperty("AltOlyDividerClassed", 5);
			ALT_OLY_DIVIDER_NON_CLASSED = olympiad.getProperty("AltOlyDividerNonClassed", 5);
			ALT_OLY_MAX_WEEKLY_MATCHES = olympiad.getProperty("AltOlyMaxWeeklyMatches", 70);
			ALT_OLY_MAX_WEEKLY_MATCHES_NON_CLASSED = olympiad.getProperty("AltOlyMaxWeeklyMatchesNonClassed", 60);
			ALT_OLY_MAX_WEEKLY_MATCHES_CLASSED = olympiad.getProperty("AltOlyMaxWeeklyMatchesClassed", 30);
			ALT_OLY_MAX_WEEKLY_MATCHES_TEAM = olympiad.getProperty("AltOlyMaxWeeklyMatchesTeam", 10);
			ALT_OLY_SHOW_MONTHLY_WINNERS = olympiad.getProperty("AltOlyShowMonthlyWinners", true);
			ALT_OLY_ANNOUNCE_GAMES = olympiad.getProperty("AltOlyAnnounceGames", true);
			LIST_OLY_RESTRICTED_ITEMS = olympiad.getIntegerProperty("AltOlyRestrictedItems", "6611,6612,6613,6614,6615,6616,6617,6618,6619,6620,6621,9388,9389,9390,17049,17050,17051,17052,17053,17054,17055,17056,17057,17058,17059,17060,17061,20759,20775,20776,20777,20778,14774", ",");
			ALT_OLY_WEAPON_ENCHANT_LIMIT = olympiad.getProperty("AltOlyEnchantWeaponLimit", -1);
			ALT_OLY_ARMOR_ENCHANT_LIMIT = olympiad.getProperty("AltOlyEnchantArmorLimit", -1);
			ALT_OLY_ACCESSORY_ENCHANT_LIMIT = olympiad.getProperty("AltOlyEnchantAccessoryLimit", -1);
			ALT_OLY_WAIT_TIME = olympiad.getProperty("AltOlyWaitTime", 120);
			BLOCK_VISUAL_OLY = olympiad.getProperty("AllowOlyBlockVisuals", true);
			ALLOW_SOULHOOD_DOUBLE = olympiad.getProperty("AllowSoulHoudDoubleHero", false);
			ALLOW_HIDE_OLY_POINTS = olympiad.getProperty("AllowHideOlyPoints", false);
			ALLOW_TRAINING_BATTLES = olympiad.getProperty("AllowTrainigBattles", false);
			ALT_OLY_TRAINING_TIME = olympiad.getProperty("TrainigBattlesStartTime", "0 12 * * *");
			ALT_OLY_TPERIOD = olympiad.getProperty("TrainigBattlesPeriod", 4);
			ALLOW_REG_WITHOUT_NOBLE = olympiad.getProperty("AllowRegWithouNobleStatus", false);
			OLY_REG_PARAM = olympiad.getIntProperty("ClassRegisterParams", "75,3", ",");
		}
		catch (final Exception e)
		{
			_log.warn("Config: " + e.getMessage());
			throw new Error("Failed to Load " + OLYMPIAD_CONFIG_FILE + " File.");
		}
	}
	
	private static void loadEpicsSettings(InputStream is)
	{
		try
		{
			final GameSettings grandbossSettings = new GameSettings();
			is = new FileInputStream(new File(GRANDBOSS_CONFIG_FILE));
			grandbossSettings.load(is);
			
			ALLOW_DAMAGE_INFO = grandbossSettings.getProperty("AllowDamageInfo", false);
			DAMAGE_INFO_UPDATE = grandbossSettings.getProperty("DamageInfoUpdateTime", 5);
			DAMAGE_INFO_LIMIT_TIME = grandbossSettings.getProperty("DamageInfoLimitTime", 5);
			
			EPIDOS_POINTS_NEED = grandbossSettings.getProperty("EpidosIndexLimit", 100);
			
			CHANCE_SPAWN = grandbossSettings.getProperty("ChanceSpawn", 50);
			RESPAWN_TIME = grandbossSettings.getProperty("RespawnTime", 4);
			
			ANTHARAS_WAIT_TIME = grandbossSettings.getProperty("AntharasWaitTime", 30);
			ALLOW_ANTHARAS_MOVIE = grandbossSettings.getProperty("AllowAntharasMovie", true);
			ANTHARAS_RESPAWN_PATTERN = grandbossSettings.getProperty("AntharasRespawnPattern", "");
			
			VALAKAS_WAIT_TIME = grandbossSettings.getProperty("ValakasWaitTime", 30);
			ALLOW_VALAKAS_MOVIE = grandbossSettings.getProperty("AllowValakasMovie", true);
			VALAKAS_RESPAWN_PATTERN = grandbossSettings.getProperty("ValakasRespawnPattern", "");
			
			BAIUM_RESPAWN_PATTERN = grandbossSettings.getProperty("BaiumRespawnPattern", "");
			BAIUM_SPAWN_DELAY = grandbossSettings.getProperty("BaiumSpawnDelay", 0);
			CORE_RESPAWN_PATTERN = grandbossSettings.getProperty("CoreRespawnPattern", "");
			ORFEN_RESPAWN_PATTERN = grandbossSettings.getProperty("OrfenRespawnPattern", "");
			QUEEN_ANT_RESPAWN_PATTERN = grandbossSettings.getProperty("QueenAntRespawnPattern", "");
			SAILREN_RESPAWN_PATTERN = grandbossSettings.getProperty("SailrenRespawnPattern", "");
			
			BELETH_RESPAWN_PATTERN = grandbossSettings.getProperty("BelethRespawnPattern", "");
			ALLOW_BELETH_MOVIE = grandbossSettings.getProperty("AllowBelethMovie", true);
			ALLOW_BELETH_DROP_RING = grandbossSettings.getProperty("AllowBelethDropRing", false);
			BELETH_MIN_PLAYERS = grandbossSettings.getProperty("BelethMinPlayers", 36);
			BELETH_SPAWN_DELAY = grandbossSettings.getProperty("BelethSpawnDelay", 5);
			BELETH_ZONE_CLEAN_DELAY = grandbossSettings.getProperty("BelethZoneCleanUpDelay", 5);
			BELETH_CLONES_RESPAWN = grandbossSettings.getProperty("RespawnTimeClones", 60);
			BELETH_NO_CC = grandbossSettings.getProperty("BelethNoCommandChannel", false);
			
		}
		catch (final Exception e)
		{
			_log.warn("Config: " + e.getMessage());
			throw new Error("Failed to Load " + GRANDBOSS_CONFIG_FILE + " File.");
		}
	}
	
	private static void loadGraciaSettings(InputStream is)
	{
		try
		{
			final GameSettings graciaseedsSettings = new GameSettings();
			is = new FileInputStream(new File(GRACIASEEDS_CONFIG_FILE));
			graciaseedsSettings.load(is);
			
			SOD_TIAT_KILL_COUNT = graciaseedsSettings.getProperty("TiatKillCountForNextState", 10);
			SOD_STAGE_2_LENGTH = graciaseedsSettings.getProperty("Stage2Length", 720) * 60000;
			SOI_EKIMUS_KILL_COUNT = graciaseedsSettings.getProperty("EkimusKillCount", 5);
			MIN_EKIMUS_PLAYERS = graciaseedsSettings.getProperty("MinEkimusPlayers", 18);
			MAX_EKIMUS_PLAYERS = graciaseedsSettings.getProperty("MaxEkimusPlayers", 27);
			SOA_CHANGE_ZONE_TIME = graciaseedsSettings.getProperty("SoaZoneTimePattern", "0 13 * * 1");
			
		}
		catch (final Exception e)
		{
			_log.warn("Config: " + e.getMessage());
			throw new Error("Failed to Load " + GRACIASEEDS_CONFIG_FILE + " File.");
		}
	}
	
	private static void loadFilterSettings()
	{
		try
		{
			FILTER_LIST = new ArrayList<>();
			final var lnr = new LineNumberReader(new BufferedReader(new FileReader(new File(CHAT_FILTER_FILE))));
			String line = null;
			while ((line = lnr.readLine()) != null)
			{
				if (line.trim().isEmpty() || (line.charAt(0) == '#'))
				{
					continue;
				}
				FILTER_LIST.add(line.trim());
			}
			lnr.close();
			_log.info("Loaded " + FILTER_LIST.size() + " Filter Words.");
		}
		catch (final Exception e)
		{
			_log.warn("Config: " + e.getMessage());
			throw new Error("Failed to Load " + CHAT_FILTER_FILE + " File.");
		}
	}
	
	private static void loadBroadCastFilterSettings()
	{
		try
		{
			BROADCAST_FILTER_LIST = new ArrayList<>();
			final var lnr = new LineNumberReader(new BufferedReader(new FileReader(new File(BROADCAST_CHAT_FILTER_FILE))));
			String line = null;
			while ((line = lnr.readLine()) != null)
			{
				if (line.trim().isEmpty() || (line.charAt(0) == '#'))
				{
					continue;
				}
				BROADCAST_FILTER_LIST.add(line.trim());
			}
			lnr.close();
			_log.info("Loaded " + BROADCAST_FILTER_LIST.size() + " BroadCast Filter Words.");
		}
		catch (final Exception e)
		{
			_log.warn("Config: " + e.getMessage());
			throw new Error("Failed to Load " + BROADCAST_CHAT_FILTER_FILE + " File.");
		}
	}
	
	private static void loadScriptsFilterSettings()
	{
		try
		{
			SCRIPTS_FILTER_LIST = new ArrayList<>();
			final var lnr = new LineNumberReader(new BufferedReader(new FileReader(new File(SCRIPTS_FILTER_FILE))));
			String line = null;
			while ((line = lnr.readLine()) != null)
			{
				if (line.trim().isEmpty() || (line.charAt(0) == '#'))
				{
					continue;
				}
				SCRIPTS_FILTER_LIST.add(line.trim());
			}
			lnr.close();
			if (SCRIPTS_FILTER_LIST.size() > 0)
			{
				_log.info("Loaded " + SCRIPTS_FILTER_LIST.size() + " Scripts filter to Load.");
			}
		}
		catch (final Exception e)
		{
			_log.warn("Config: " + e.getMessage());
			throw new Error("Failed to Load " + SCRIPTS_FILTER_FILE + " File.");
		}
	}
	
	private static void loadClanhallSiegeSettings(InputStream is)
	{
		try
		{
			final GameSettings chSiege = new GameSettings();
			is = new FileInputStream(new File(CH_SIEGE_FILE));
			chSiege.load(is);
			
			CHS_MAX_ATTACKERS = chSiege.getProperty("MaxAttackers", 500);
			CHS_CLAN_MINLEVEL = chSiege.getProperty("MinClanLevel", 4);
			CHS_MAX_FLAGS_PER_CLAN = chSiege.getProperty("MaxFlagsPerClan", 1);
			CHS_ENABLE_FAME = chSiege.getProperty("EnableFame", false);
			CHS_FAME_AMOUNT = chSiege.getProperty("FameAmount", 0);
			CHS_FAME_FREQUENCY = chSiege.getProperty("FameFrequency", 0);
			CLAN_HALL_HWID_LIMIT = chSiege.getProperty("ClanHallSiegeLimitPlayers", 0);
		}
		catch (final Exception e)
		{
			_log.warn("Config: " + e.getMessage());
			throw new Error("Failed to Load " + CH_SIEGE_FILE + " File.");
		}
	}
	
	private static void loadLanguageSettings(InputStream is)
	{
		try
		{
			final GameSettings LanguageSettings = new GameSettings();
			is = new FileInputStream(new File(LANGUAGE_FILE));
			LanguageSettings.load(is);
			
			MULTILANG_ENABLE = LanguageSettings.getProperty("MultiLangEnable", false);
			MULTILANG_ALLOWED = LanguageSettings.getListProperty("MultiLangAllowed", "en", ";");
			MULTILANG_DEFAULT = LanguageSettings.getProperty("MultiLangDefault", "en");
			if (!MULTILANG_ALLOWED.contains(MULTILANG_DEFAULT))
			{
				_log.warn("Default language: " + MULTILANG_DEFAULT + " is not in allowed list!");
			}
			MULTILANG_VOICED_ALLOW = LanguageSettings.getProperty("MultiLangVoiceCommand", true);
		}
		catch (final Exception e)
		{
			_log.warn("Config: " + e.getMessage());
			throw new Error("Failed to Load " + LANGUAGE_FILE + " File.");
		}
	}
	
	private static void loadVoiceSettings(InputStream is)
	{
		try
		{
			final GameSettings VoiceSettings = new GameSettings();
			is = new FileInputStream(new File(VOICE_CONFIG_FILE));
			VoiceSettings.load(is);
			
			DISABLE_VOICE_BYPASSES = VoiceSettings.getListProperty("DisableVoiceList", "", ",");
			ALLOW_OFFLINE_COMMAND = VoiceSettings.getProperty("AllowOfflineCommand", false);
			ALLOW_EXP_GAIN_COMMAND = VoiceSettings.getProperty("AllowExpGainCommand", false);
			ALLOW_AUTOLOOT_COMMAND = VoiceSettings.getProperty("AutoLootVoiceCommand", false);
			BANKING_SYSTEM_ENABLED = VoiceSettings.getProperty("BankingEnabled", false);
			BANKING_SYSTEM_GOLDBARS = VoiceSettings.getProperty("BankingGoldbarCount", 1);
			BANKING_SYSTEM_ADENA = VoiceSettings.getProperty("BankingAdenaCount", 500000000);
			CHAT_ADMIN = VoiceSettings.getProperty("ChatAdmin", false);
			HELLBOUND_STATUS = VoiceSettings.getProperty("HellboundStatus", false);
			DEBUG_VOICE_COMMAND = VoiceSettings.getProperty("DebugVoiceCommand", false);
			ALLOW_CHANGE_PASSWORD = VoiceSettings.getProperty("AllowChangePassword", false);
			VOICE_ONLINE_ENABLE = VoiceSettings.getProperty("OnlineEnable", false);
			FAKE_ONLINE = VoiceSettings.getProperty("FakeOnline", 1.0);
			FAKE_ONLINE_MULTIPLIER = VoiceSettings.getProperty("FakeOnlineMultiplier", 0);
			ALLOW_TELETO_LEADER = VoiceSettings.getProperty("AllowTeletoLeader", false);
			TELETO_LEADER_ID = VoiceSettings.getProperty("TeletoLeaderId", 57);
			TELETO_LEADER_COUNT = VoiceSettings.getProperty("TeletoLeaderCount", 1000);
			ALLOW_REPAIR_COMMAND = VoiceSettings.getProperty("AllowRepairCommand", false);
			ALLOW_VISUAL_ARMOR_COMMAND = VoiceSettings.getProperty("AllowVisualArmorCommand", false);
			ENABLE_VISUAL_BY_DEFAULT = VoiceSettings.getProperty("EnableVisualByDefault", false);
			ALLOW_SEVENBOSSES_COMMAND = VoiceSettings.getProperty("AllowSevenBossesCommand", false);
			ALLOW_ANCIENT_EXCHANGER_COMMAND = VoiceSettings.getProperty("AllowAncientExchangerCommand", false);
			ALLOW_SELLBUFFS_COMMAND = VoiceSettings.getProperty("AllowSellBuffsCommand", false);
			ALLOW_SELLBUFFS_IN_PEACE = VoiceSettings.getProperty("AllowSellBuffsInPeaceZone", false);
			ALLOW_SELLBUFFS_ZONE = VoiceSettings.getProperty("AllowSellBuffsInZone", false);
			SELLBUFF_USED_MP = VoiceSettings.getProperty("SellBuffsUseMp", false);
			SELLBUFF_CURRECY_LIST = VoiceSettings.getStringMapProperty("SellBuffCurrecyList", "adena,57", ";");
			SELLBUFF_MIN_PRICE = VoiceSettings.getProperty("SellBuffsMinPrice", 1);
			SELLBUFF_MAX_PRICE = VoiceSettings.getProperty("SellBuffsMaxPrice", 100000000);
			SELLBUFF_MAX_BUFFS = VoiceSettings.getProperty("SellBuffsMaxBuffs", 15);
			FREE_SELLBUFF_FOR_SAME_CLAN = VoiceSettings.getProperty("AllowFreeBuffForSameClan", false);
			ALLOW_SELLBUFFS_PETS = VoiceSettings.getProperty("AllowSellBuffSummons", false);
			ALLOW_STATS_COMMAND = VoiceSettings.getProperty("AllowStatsCommand", false);
			ALLOW_BLOCKBUFFS_COMMAND = VoiceSettings.getProperty("AllowBlockBuffsCommand", false);
			ALLOW_HIDE_TRADERS_COMMAND = VoiceSettings.getProperty("AllowHideTradersCommand", false);
			ALLOW_HIDE_BUFFS_ANIMATION_COMMAND = VoiceSettings.getProperty("AllowHideBuffsAnimCommand", false);
			ALLOW_BLOCK_TRADERS_COMMAND = VoiceSettings.getProperty("AllowBlockTradersCommand", false);
			ALLOW_BLOCK_PARTY_COMMAND = VoiceSettings.getProperty("AllowBlockPartyCommand", false);
			ALLOW_BLOCK_FRIEND_COMMAND = VoiceSettings.getProperty("AllowBlockFriendCommand", false);
			ALLOW_MENU_COMMAND = VoiceSettings.getProperty("AllowMenuCommand", false);
			ALLOW_SECURITY_COMMAND = VoiceSettings.getProperty("AllowSecurityCommand", false);
			ALLOW_IP_LOCK = VoiceSettings.getProperty("AllowIpLock", false);
			ALLOW_HWID_LOCK = VoiceSettings.getProperty("AllowHwidLock", false);
			ALLOW_FIND_PARTY = VoiceSettings.getProperty("AllowFindPartyCommand", false);
			PARTY_LEADER_ONLY_CAN_INVITE = VoiceSettings.getProperty("AllowLeaderOnlyCanInvite", false);
			FIND_PARTY_REFRESH_TIME = VoiceSettings.getProperty("FindPartyRefreshTime", 600);
			FIND_PARTY_FLOOD_TIME = VoiceSettings.getProperty("FindPartyFloodTime", 60);
			FIND_PARTY_MIN_LEVEL = VoiceSettings.getProperty("FindPartyMinLevel", 40);
			
			ALLOW_ENCHANT_SERVICE = VoiceSettings.getProperty("AllowEnchantService", false);
			ENCHANT_SERVICE_ONLY_FOR_PREMIUM = VoiceSettings.getProperty("EnchantServiceOnlyForPremium", false);
			ENCHANT_ALLOW_BELTS = VoiceSettings.getProperty("EnchantServiceAllowBelts", false);
			ENCHANT_ALLOW_SCROLLS = VoiceSettings.getProperty("EnchantServiceScrollsEnable", false);
			ENCHANT_ALLOW_ATTRIBUTE = VoiceSettings.getProperty("EnchantServiceAttributeEnable", false);
			ENCHANT_MAX_WEAPON = VoiceSettings.getProperty("EnchantServiceMaxWeapon", 20);
			ENCHANT_MAX_ARMOR = VoiceSettings.getProperty("EnchantServiceMaxArmor", 20);
			ENCHANT_MAX_JEWELRY = VoiceSettings.getProperty("EnchantServiceMaxJewelry", 20);
			ENCHANT_MAX_ITEM_LIMIT = VoiceSettings.getProperty("EnchantServiceMaxItemLimit", 40);
			ENCHANT_CONSUME_ITEM = VoiceSettings.getProperty("EnchantServiceConsumeItem", 57);
			ENCHANT_CONSUME_ITEM_COUNT = VoiceSettings.getProperty("EnchantServiceConsumeItemCount", 1000);
			enchantServiceDefaultLimit = VoiceSettings.getProperty("EnchantServiceDefaultLimit", 40);
			enchantServiceDefaultEnchant = VoiceSettings.getProperty("EnchantServiceDefaultEnchant", 20);
			enchantServiceDefaultAttribute = VoiceSettings.getProperty("EnchantServiceDefaultAttribute", 120);
			ENCHANT_SCROLL_CHANCE_CORRECT = VoiceSettings.getProperty("EnchantServiceScrollChanceCorrect", 0);
			ALLOW_RELOG_COMMAND = VoiceSettings.getProperty("AllowRelogCommand", false);
			ALLOW_PARTY_RANK_COMMAND = VoiceSettings.getProperty("AllowPartyRankCommand", false);
			ALLOW_PARTY_RANK_ONLY_FOR_CC = VoiceSettings.getProperty("PartyRankOnlyForCC", false);
			PARTY_RANK_AUTO_OPEN = VoiceSettings.getProperty("PartyRankAutoOpenWindow", false);
			ALLOW_RECOVERY_ITEMS = VoiceSettings.getProperty("AllowRecoveryItemCommand", false);
			RECOVERY_ITEMS_HOURS = VoiceSettings.getProperty("RecoveryItemHours", 24);
			ALLOW_PROMOCODES_COMMAND = VoiceSettings.getProperty("AllowPromoCodesCommand", false);
			PROMOCODES_USE_DELAY = VoiceSettings.getProperty("PromoCodesDelay", 60);
		}
		catch (final Exception e)
		{
			_log.warn("Config: " + e.getMessage());
			throw new Error("Failed to Load " + VOICE_CONFIG_FILE + " File.");
		}
	}
	
	private static void loadCustomSettings(InputStream is)
	{
		try
		{
			final GameSettings customSettings = new GameSettings();
			is = new FileInputStream(new File(CUSTOM_FILE));
			customSettings.load(is);
			
			ALLOW_CUSTOM_INTERFACE = customSettings.getProperty("AllowCustomInterface", false);
			ALLOW_CUSTOM_BUTTONS = customSettings.getProperty("AllowCustomInterfaceButtons", false);
			ALLOW_INTERFACE_SHIFT_CLICK = customSettings.getProperty("AllowInterfaceShiftClick", false);
			ALLOW_PRIVATE_INVENTORY = customSettings.getProperty("AllowPrivateInventory", false);
			SWITCH_COLOR_NAME = customSettings.getProperty("SwitchColorName", false);
			SERVER_NAME = customSettings.getProperty("ServerName", "L2J Eternity-World Server");
			ENABLE_MANA_POTIONS_SUPPORT = customSettings.getProperty("EnableManaPotionSupport", false);
			DISPLAY_SERVER_TIME = customSettings.getProperty("DisplayServerTime", false);
			ENABLE_WAREHOUSESORTING_CLAN = customSettings.getProperty("EnableWarehouseSortingClan", false);
			ENABLE_WAREHOUSESORTING_PRIVATE = customSettings.getProperty("EnableWarehouseSortingPrivate", false);
			WELCOME_MESSAGE_ENABLED = customSettings.getProperty("ScreenWelcomeMessageEnable", false);
			WELCOME_MESSAGE_TEXT = customSettings.getProperty("ScreenWelcomeMessageText", "Welcome to L2J server!");
			WELCOME_MESSAGE_TIME = customSettings.getProperty("ScreenWelcomeMessageTime", 10) * 1000;
			ANNOUNCE_PK_PVP = customSettings.getProperty("AnnouncePkPvP", false);
			ANNOUNCE_PK_PVP_NORMAL_MESSAGE = customSettings.getProperty("AnnouncePkPvPNormalMessage", true);
			ANNOUNCE_PK_MSG = customSettings.getProperty("AnnouncePkMsg", "$killer has slaughtered $target");
			ANNOUNCE_PVP_MSG = customSettings.getProperty("AnnouncePvpMsg", "$killer has defeated $target");
			ONLINE_PLAYERS_AT_STARTUP = customSettings.getProperty("ShowOnlinePlayersAtStartup", true);
			ONLINE_PLAYERS_ANNOUNCE_INTERVAL = customSettings.getProperty("OnlinePlayersAnnounceInterval", 900000);
			ALLOW_NEW_CHARACTER_TITLE = customSettings.getProperty("AllowNewCharacterTitle", false);
			NEW_CHARACTER_TITLE = customSettings.getProperty("NewCharacterTitle", "Newbie");
			NEW_CHAR_IS_NOBLE = customSettings.getProperty("NewCharIsNoble", false);
			NEW_CHAR_IS_HERO = customSettings.getProperty("NewCharIsHero", false);
			UNSTUCK_SKILL = customSettings.getProperty("UnstuckSkill", false);
			ALLOW_NEW_CHAR_CUSTOM_POSITION = customSettings.getProperty("AltSpawnNewChar", false);
			NEW_CHAR_POSITION_X = customSettings.getProperty("AltSpawnX", 0);
			NEW_CHAR_POSITION_Y = customSettings.getProperty("AltSpawnY", 0);
			NEW_CHAR_POSITION_Z = customSettings.getProperty("AltSpawnZ", 0);
			ENABLE_NOBLESS_COLOR = customSettings.getProperty("EnableNoblessColor", false);
			NOBLESS_COLOR_NAME = Integer.decode("0x" + customSettings.getProperty("NoblessColorName", "000000"));
			ENABLE_NOBLESS_TITLE_COLOR = customSettings.getProperty("EnableNoblessTitleColor", false);
			NOBLESS_COLOR_TITLE_NAME = Integer.decode("0x" + customSettings.getProperty("NoblessColorTitleName", "000000"));
			INFINITE_SOUL_SHOT = customSettings.getProperty("InfiniteSoulShot", false);
			INFINITE_BEAST_SOUL_SHOT = customSettings.getProperty("InfiniteBeastSoulShot", false);
			INFINITE_BEAST_SPIRIT_SHOT = customSettings.getProperty("InfiniteBeastSpiritShot", false);
			INFINITE_SPIRIT_SHOT = customSettings.getProperty("InfiniteSpiritShot", false);
			INFINITE_BLESSED_SPIRIT_SHOT = customSettings.getProperty("InfiniteBlessedSpiritShot", false);
			INFINITE_ARROWS = customSettings.getProperty("InfiniteArrows", false);
			ENTER_HELLBOUND_WITHOUT_QUEST = customSettings.getProperty("EnterHellBoundWithoutQuest", false);
			AUTO_RESTART_TIME = customSettings.getProperty("AutoRestartSeconds", 360);
			AUTO_RESTART_PATTERN = customSettings.getProperty("AutoRestartPattern", "* * * * *");
			SPEED_UP_RUN = customSettings.getProperty("SpeedUpRunInTown", false);
			DISCONNECT_SYSTEM_ENABLED = customSettings.getProperty("DisconnectSystemEnable", false);
			DISCONNECT_TIMEOUT = customSettings.getProperty("DisconnectTimeout", 15);
			DISCONNECT_TITLECOLOR = customSettings.getProperty("DisconnectColorTitle", "FF0000");
			DISCONNECT_TITLE = customSettings.getProperty("DisconnectTitle", "[NO CARRIER]");
			CUSTOM_ENCHANT_ITEMS_ENABLED = customSettings.getProperty("CustomEnchantSystemEnable", false);
			ENCHANT_ITEMS_ID = customSettings.getMapProperty("CustomEnchantItemsById", "", ";");
			ALLOW_UNLIM_ENTER_CATACOMBS = customSettings.getProperty("AllowUnlimEnterCatacombs", false);
			AUTO_POINTS_SYSTEM = customSettings.getProperty("AllowAutoPotions", false);
			AUTO_HP_VALID_ITEMS = customSettings.getIntegerProperty("ListOfValidHpPotions", "0", ",");
			AUTO_MP_VALID_ITEMS = customSettings.getIntegerProperty("ListOfValidMpPotions", "0", ",");
			AUTO_CP_VALID_ITEMS = customSettings.getIntegerProperty("ListOfValidCpPotions", "0", ",");
			AUTO_SOUL_VALID_ITEMS = customSettings.getIntegerProperty("ListOfValidSoulPotions", "0", ",");
			DEFAULT_HP_PERCENT = customSettings.getProperty("DefaultHpPercent", 70);
			DEFAULT_MP_PERCENT = customSettings.getProperty("DefaultMpPercent", 60);
			DEFAULT_CP_PERCENT = customSettings.getProperty("DefaultCpPercent", 90);
			DEFAULT_SOUL_AMOUNT = customSettings.getProperty("DefaultSoulAmount", 10);
			DISABLE_WITHOUT_POTIONS = customSettings.getProperty("DisableWithOutPotions", true);
			SELL_PRICE_MODIFIER = customSettings.getProperty("SellPriceModifier", 1.0);
			ALT_KAMALOKA_SOLO_PREMIUM_ONLY = customSettings.getProperty("SoloKamalokaPremiumOnly", true);
			ALT_KAMALOKA_ESSENCE_PREMIUM_ONLY = customSettings.getProperty("KamalokaEssencePremiumOnly", true);
			ITEM_BROKER_ITEM_SEARCH = customSettings.getProperty("AllowItemBrokerItemSearch", false);
			ITEM_BROKER_ITEMS_PER_PAGE = customSettings.getProperty("ItemBrokerItemsPerPage", 10);
			ITEM_BROKER_TIME_UPDATE = customSettings.getProperty("ItemBrokerUpdateTime", 30);
			ALLOW_BLOCK_TRANSFORMS_AT_SIEGE = customSettings.getProperty("AllowBlockTransformAtSiege", false);
			LIST_BLOCK_TRANSFORMS_AT_SIEGE = customSettings.getIntegerProperty("BlockTransformationList", "0,0", ",");
			ALLOW_AUTO_FISH_SHOTS = customSettings.getProperty("AllowAutoFishShots", false);
			EXP_ID = customSettings.getProperty("ExpItemId", 99998);
			SP_ID = customSettings.getProperty("SpItemId", 99999);
			AUTO_COMBINE_TALISMANS = customSettings.getProperty("AutoCombineTalismans", false);
			ALLOW_BLOCK_TRADE_ITEMS = customSettings.getProperty("BlockTradeAllItems", false);
			ALLOW_BLOCK_DEPOSIT_ITEMS = customSettings.getProperty("BlockDepositAllItems", false);
			ALLOW_BLOCK_DESTROY_ITEMS = customSettings.getProperty("BlockDestroyAllItems", false);
			ALLOW_BLOCK_SELL_ITEMS = customSettings.getProperty("BlockSellAllItems", false);
			ALLOW_BLOCK_DROP_ITEMS = customSettings.getProperty("BlockDropAllItems", false);
		}
		catch (final Exception e)
		{
			_log.warn("Config: " + e.getMessage());
			throw new Error("Failed to Load " + CUSTOM_FILE + " File.");
		}
	}
	
	private static void loadPcBangSettings(InputStream is)
	{
		try
		{
			final GameSettings pccaffeSettings = new GameSettings();
			is = new FileInputStream(new File(PCBANG_CONFIG_FILE));
			pccaffeSettings.load(is);
			
			PC_BANG_ENABLED = pccaffeSettings.getProperty("PcBangPointEnable", false);
			PC_BANG_ONLY_FOR_PREMIUM = pccaffeSettings.getProperty("PcBangPointOnlyForPremium", false);
			PC_POINT_ID = pccaffeSettings.getProperty("PcBangPointId", -100);
			MAX_PC_BANG_POINTS = pccaffeSettings.getProperty("MaxPcBangPoints", 200000);
			if (MAX_PC_BANG_POINTS < 0)
			{
				MAX_PC_BANG_POINTS = 0;
			}
			ENABLE_DOUBLE_PC_BANG_POINTS = pccaffeSettings.getProperty("DoublingAcquisitionPoints", false);
			DOUBLE_PC_BANG_POINTS_CHANCE = pccaffeSettings.getProperty("DoublingAcquisitionPointsChance", 1);
			if ((DOUBLE_PC_BANG_POINTS_CHANCE < 0) || (DOUBLE_PC_BANG_POINTS_CHANCE > 100))
			{
				DOUBLE_PC_BANG_POINTS_CHANCE = 1;
			}
			PC_BANG_MIN_LEVEL = pccaffeSettings.getProperty("PcBangPointMinLevel", 20);
			PC_BANG_POINTS_MIN = pccaffeSettings.getProperty("PcBangPointMinCount", 20);
			PC_BANG_POINTS_PREMIUM_MIN = pccaffeSettings.getProperty("PcBangPointPremiumMinCount", 20);
			PC_BANG_POINTS_MAX = pccaffeSettings.getProperty("PcBangPointMaxCount", 30);
			PC_BANG_POINTS_PREMIUM_MAX = pccaffeSettings.getProperty("PcBangPointPremiumMaxCount", 30);
			PC_BANG_INTERVAL = pccaffeSettings.getProperty("PcBangPointIntervalTime", 900);
		}
		catch (final Exception e)
		{
			_log.warn("Config: " + e.getMessage());
			throw new Error("Failed to Load " + PCBANG_CONFIG_FILE + " File.");
		}
	}
	
	private static void loadPremiumSettings(InputStream is)
	{
		try
		{
			final GameSettings premium = new GameSettings();
			is = new FileInputStream(new File(PREMIUM_CONFIG_FILE));
			premium.load(is);
			
			USE_PREMIUMSERVICE = premium.getProperty("UsePremiumServices", false);
			SERVICES_WITHOUT_PREMIUM = premium.getProperty("UseServicesWithuotPremium", false);
			PREMIUMSERVICE_DOUBLE = premium.getProperty("AllowBuyPremiumOver", false);
			AUTO_GIVE_PREMIUM = premium.getProperty("AutoGivePremium", false);
			GIVE_PREMIUM_ID = premium.getProperty("GivePremiumId", 1);
			PREMIUM_PARTY_RATE = premium.getProperty("AllowPremiumPartyRate", false);
			
		}
		catch (final Exception e)
		{
			_log.warn("Config: " + e.getMessage());
			throw new Error("Failed to Load " + PREMIUM_CONFIG_FILE + " File.");
		}
	}
	
	private static void loadCommunitySettings(InputStream is)
	{
		try
		{
			final GameSettings CommunityBoardSettings = new GameSettings();
			is = new FileInputStream(new File(COMMUNITY_BOARD_CONFIG_FILE));
			CommunityBoardSettings.load(is);
			
			ALLOW_COMMUNITY = CommunityBoardSettings.getProperty("AllowCommunity", true);
			DISABLE_COMMUNITY_BYPASSES_COMBAT = CommunityBoardSettings.getListProperty("BlockCommunityBypassCombatList", "", ",");
			DISABLE_COMMUNITY_BYPASSES_FLAG = CommunityBoardSettings.getListProperty("BlockCommunityBypassFlagList", "", ",");
			BLOCK_COMMUNITY_IN_PVP_ZONE = CommunityBoardSettings.getProperty("BlockCommunityAtPvpZones", false);
			DISABLE_COMMUNITY_BYPASSES = CommunityBoardSettings.getListProperty("DisableBypassList", "", ",");
			ALLOW_SENDING_IMAGES = CommunityBoardSettings.getProperty("AllowSendingImages", true);
			BBS_HOME_PAGE = CommunityBoardSettings.getProperty("bbshome", "_bbshome");
			BBS_FAVORITE_PAGE = CommunityBoardSettings.getProperty("bbsgetfav", "_bbsgetfav");
			BBS_LINK_PAGE = CommunityBoardSettings.getProperty("bbslink", "_bbslink");
			BBS_REGION_PAGE = CommunityBoardSettings.getProperty("bbsloc", "_bbsloc");
			BBS_CLAN_PAGE = CommunityBoardSettings.getProperty("bbsclan", "_bbsclan");
			BBS_MEMO_PAGE = CommunityBoardSettings.getProperty("bbsmemo", "_bbsmemo");
			BBS_MAIL_PAGE = CommunityBoardSettings.getProperty("bbsmail", "_maillist_0_1_0_");
			BBS_FRIENDS_PAGE = CommunityBoardSettings.getProperty("bbsfriends", "_friendlist_0_");
			BBS_ADDFAV_PAGE = CommunityBoardSettings.getProperty("bbsAddFav", "");
			ALLOW_COMMUNITY_PEACE_ZONE = CommunityBoardSettings.getProperty("AllowCommunityPeaceZone", false);
			AVALIABLE_COMMUNITY_MULTISELLS = CommunityBoardSettings.getIntegerProperty("AvaliableMultiSellList", "0", ",");
			INTERVAL_STATS_UPDATE = CommunityBoardSettings.getProperty("IntervalStatsUpdate", 60);
			ALLOW_COMMUNITY_COORDS_TP = CommunityBoardSettings.getProperty("AllowTeleportByCoords", false);
			ALLOW_COMMUNITY_TP_NO_RESTART_ZONES = CommunityBoardSettings.getProperty("AllowTeleportByNoRestartZones", false);
			ALLOW_COMMUNITY_TP_SIEGE_ZONES = CommunityBoardSettings.getProperty("AllowTeleportBySiegeZones", false);
			COMMUNITY_TELEPORT_TABS = CommunityBoardSettings.getProperty("CommunityTeleportTabsLimit", 5);
			ALLOW_COMMUNITY_BUFF_IN_SIEGE = CommunityBoardSettings.getProperty("AllowCommunityBuffInSiege", false);
			ALLOW_COMMUNITY_TELEPORT_IN_SIEGE = CommunityBoardSettings.getProperty("AllowCommunityTeleportInSiege", false);
			BLOCK_TP_AT_SIEGES_FOR_ALL = CommunityBoardSettings.getProperty("BlockCommunityPersonalTpInSieges", false);
			ALLOW_BUFF_PEACE_ZONE = CommunityBoardSettings.getProperty("AllowBufferInPeaceZone", false);
			ALLOW_SUMMON_AUTO_BUFF = CommunityBoardSettings.getProperty("AllowSummonAutoBuff", false);
			ALLOW_BUFF_WITHOUT_PEACE_FOR_PREMIUM = CommunityBoardSettings.getProperty("CanUsePremiumInPeaceZone", false);
			FREE_ALL_BUFFS = CommunityBoardSettings.getProperty("AllowFreeAllBuffs", false);
			ALLOW_SCHEMES_FOR_PREMIUMS = CommunityBoardSettings.getProperty("AllowSchemesForPremium", false);
			ALLOW_HEAL_ONLY_PEACE = CommunityBoardSettings.getProperty("AllowHealOnlyPeaceZone", false);
			BUFF_ID_ITEM = CommunityBoardSettings.getProperty("BuffPriceId", 57);
			BUFF_AMOUNT = CommunityBoardSettings.getProperty("BuffPriceAmount", 10000);
			CANCEL_BUFF_AMOUNT = CommunityBoardSettings.getProperty("CancelBuffPriceAmount", 10000);
			HPMPCP_BUFF_AMOUNT = CommunityBoardSettings.getProperty("RecoveryPriceAmount", 10000);
			BUFF_MAX_SCHEMES = CommunityBoardSettings.getProperty("BuffMaxSchemesPerChar", 4);
			SERVICES_LEVELUP_ENABLE = CommunityBoardSettings.getProperty("AllowLevelUpService", false);
			SERVICES_DELEVEL_ENABLE = CommunityBoardSettings.getProperty("AllowDeLevelService", false);
			LVLUP_SERVICE_STATIC_PRICE = CommunityBoardSettings.getProperty("AllowLvLServiceStaticPrice", false);
			COMMUNITY_TELEPORT_ITEM = CommunityBoardSettings.getIntProperty("TeleportTabPrice", "57,10000", ",");
			COMMUNITY_TAB_USE_ITEM = CommunityBoardSettings.getIntProperty("TeleportTabUsePrice", "57,10000", ",");
			SERVICES_LEVELUP_ITEM = CommunityBoardSettings.getIntProperty("LevelUpPrice", "4037,10", ",");
			SERVICES_DELEVEL_ITEM = CommunityBoardSettings.getIntProperty("DelevelPrice", "4037,10", ",");
			SERVICES_GIVENOOBLESS_ITEM = CommunityBoardSettings.getIntProperty("GiveNooblessPrice", "4037,30", ",");
			SERVICES_CHANGEGENDER_ITEM = CommunityBoardSettings.getIntProperty("ChangeGenderPrice", "4037,30", ",");
			SERVICES_GIVEHERO_ITEM = CommunityBoardSettings.getIntProperty("GiveHeroPrice", "4037,100", ",");
			SERVICES_GIVEHERO_TIME = CommunityBoardSettings.getProperty("GiveHeroTime", 60);
			SERVICES_GIVEHERO_SKILLS = CommunityBoardSettings.getProperty("GiveHeroSkills", false);
			SERVICES_RECOVERYPK_ITEM = CommunityBoardSettings.getIntProperty("RecoveryPkPrice", "4037,10", ",");
			SERVICES_RECOVERYKARMA_ITEM = CommunityBoardSettings.getIntProperty("RecoveryKarmaPrice", "4037,10", ",");
			SERVICES_RECOVERYVITALITY_ITEM = CommunityBoardSettings.getIntProperty("RecoveryVitalityPrice", "4037,10", ",");
			SERVICES_GIVESP_ITEM = CommunityBoardSettings.getIntProperty("GiveSpPrice", "4037,10", ",");
			SERVICES_NAMECHANGE_ITEM = CommunityBoardSettings.getIntProperty("NameChangePrice", "4037,100", ",");
			SERVICES_NAMECHANGE_TEMPLATE = CommunityBoardSettings.getProperty("ChangeNameTemplate", ".*");
			SERVICES_CLANNAMECHANGE_ITEM = CommunityBoardSettings.getIntProperty("ClanNameChangePrice", "4037,100", ",");
			SERVICES_UNBAN_ITEM = CommunityBoardSettings.getIntProperty("UnbanPrice", "4037,100", ",");
			SERVICES_CLANLVL_ITEM = CommunityBoardSettings.getIntProperty("ClanLvlUpPrice", "4037,10", ",");
			LEARN_CLAN_MAX_LEVEL = CommunityBoardSettings.getProperty("LearnMaxClanLevel", false);
			LEARN_CLAN_SKILLS_MAX_LEVEL = CommunityBoardSettings.getProperty("LearnMaxClanSkillLevel", false);
			SERVICES_CLANSKILLS_ITEM = CommunityBoardSettings.getIntProperty("ClanSkillsPrice", "4037,10", ",");
			SERVICES_GIVEREC_ITEM = CommunityBoardSettings.getIntProperty("GiveRecPrice", "4037,50", ",");
			SERVICE_EXCHANGE_AUGMENT = CommunityBoardSettings.getIntProperty("ExchangeAugment", "4037,50", ",");
			SERVICE_EXCHANGE_ELEMENTS = CommunityBoardSettings.getIntProperty("ExchangeElements", "4037,50", ",");
			SERVICES_REP_COUNT = CommunityBoardSettings.getProperty("ReputationCount", 40000);
			SERVICES_GIVEREP_ITEM = CommunityBoardSettings.getIntProperty("GiveRepPrice", "4037,30", ",");
			SERVICES_FAME_COUNT = CommunityBoardSettings.getProperty("FameCount", 15000);
			SERVICES_GIVEFAME_ITEM = CommunityBoardSettings.getIntProperty("GiveFamePrice", "4037,30", ",");
			SERVICES_AUGMENTATION_ITEM = CommunityBoardSettings.getIntProperty("AugmentationPrice", "4037,10", ",");
			SERVICES_AUGMENTATION_FORMATE = CommunityBoardSettings.getProperty("AugmentationAvailableFormat", false);
			SERVICES_AUGMENTATION_AVAILABLE_LIST = CommunityBoardSettings.getIntegerProperty("AugmentationAvailableList", "0", ",");
			SERVICES_AUGMENTATION_DISABLED_LIST = CommunityBoardSettings.getIntegerProperty("AugmentationDisabledList", "0", ",");
			BBS_FORGE_ENCHANT_ITEM = CommunityBoardSettings.getProperty("ItemID", 4037);
			BBS_FORGE_FOUNDATION_ITEM = CommunityBoardSettings.getProperty("FoundationItem", 4037);
			BBS_FORGE_FOUNDATION_PRICE_ARMOR = CommunityBoardSettings.getIntProperty("FoundationPriceArmor", "1,1,1,1,1,2,5,10", ",");
			BBS_FORGE_FOUNDATION_PRICE_WEAPON = CommunityBoardSettings.getIntProperty("FoundationPriceWeapon", "1,1,1,1,1,2,5,10", ",");
			BBS_FORGE_FOUNDATION_PRICE_JEWEL = CommunityBoardSettings.getIntProperty("FoundationPriceJewel", "1,1,1,1,1,2,5,10", ",");
			BBS_FORGE_ENCHANT_MAX = CommunityBoardSettings.getIntProperty("MaxEnchant", "12,12,12", ",");
			BBS_FORGE_WEAPON_ENCHANT_LVL = CommunityBoardSettings.getIntProperty("WeaponEnchantLvls", "6,7,8", ",");
			BBS_FORGE_ARMOR_ENCHANT_LVL = CommunityBoardSettings.getIntProperty("ArmorEnchantLvls", "6,7,8", ",");
			BBS_FORGE_JEWELS_ENCHANT_LVL = CommunityBoardSettings.getIntProperty("JewelryEnchantLvls", "6,7,8", ",");
			BBS_FORGE_ENCHANT_PRICE_WEAPON = CommunityBoardSettings.getIntProperty("EnchantWeaponPrice", "10,20,30", ",");
			BBS_FORGE_ENCHANT_PRICE_ARMOR = CommunityBoardSettings.getIntProperty("EnchantArmorPrice", "5,10,12", ",");
			BBS_FORGE_ENCHANT_PRICE_JEWELS = CommunityBoardSettings.getIntProperty("EnchantJewelryPrice", "4,6,8", ",");
			BBS_FORGE_ATRIBUTE_LVL_WEAPON = CommunityBoardSettings.getIntProperty("AtributeWeaponValue", "300", ",");
			BBS_FORGE_ATRIBUTE_PRICE_WEAPON = CommunityBoardSettings.getIntProperty("PriceForAtributeWeapon", "30", ",");
			BBS_FORGE_ATRIBUTE_LVL_ARMOR = CommunityBoardSettings.getIntProperty("AtributeArmorValue", "120", ",");
			BBS_FORGE_ATRIBUTE_PRICE_ARMOR = CommunityBoardSettings.getIntProperty("PriceForAtributeArmor", "6", ",");
			BBS_FORGE_WEAPON_ATTRIBUTE_MAX = CommunityBoardSettings.getProperty("MaxWeaponAttribute", 25);
			BBS_FORGE_ARMOR_ATTRIBUTE_MAX = CommunityBoardSettings.getProperty("MaxArmorAttribute", 25);
			SERVICES_SOUL_CLOAK_TRANSFER_ITEM = CommunityBoardSettings.getIntProperty("SoulCloakTransferItem", "4037,50", ",");
			SERVICES_OLF_STORE_ITEM = CommunityBoardSettings.getProperty("OlfStoreItemId", 4037);
			SERVICES_OLF_STORE_0_PRICE = CommunityBoardSettings.getProperty("OlfStoreEnchant0", 100);
			SERVICES_OLF_STORE_6_PRICE = CommunityBoardSettings.getProperty("OlfStoreEnchant6", 200);
			SERVICES_OLF_STORE_7_PRICE = CommunityBoardSettings.getProperty("OlfStoreEnchant7", 275);
			SERVICES_OLF_STORE_8_PRICE = CommunityBoardSettings.getProperty("OlfStoreEnchant8", 350);
			SERVICES_OLF_STORE_9_PRICE = CommunityBoardSettings.getProperty("OlfStoreEnchant9", 425);
			SERVICES_OLF_STORE_10_PRICE = CommunityBoardSettings.getProperty("OlfStoreEnchant10", 500);
			SERVICES_OLF_TRANSFER_ITEM = CommunityBoardSettings.getIntProperty("OlfTransferItem", "4037,50", ",");
			ENABLE_MULTI_AUCTION_SYSTEM = CommunityBoardSettings.getProperty("EnableMultiAuctionSystem", false);
			AUCTION_FEE = CommunityBoardSettings.getProperty("AuctionPrice", 10000);
			ALLOW_AUCTION_OUTSIDE_TOWN = CommunityBoardSettings.getProperty("AuctionOutsideTown", false);
			ALLOW_ADDING_AUCTION_DELAY = CommunityBoardSettings.getProperty("AllowAuctionDelay", false);
			SECONDS_BETWEEN_ADDING_AUCTIONS = CommunityBoardSettings.getProperty("AuctionAddDelay", 30);
			AUCTION_PRIVATE_STORE_AUTO_ADDED = CommunityBoardSettings.getProperty("AuctionPrivateStoreAutoAdded", true);
			BBS_BOSSES_TO_SHOW = CommunityBoardSettings.getIntProperty("RaidBossesToShow", "0", ",");
			BBS_BOSSES_TO_NOT_SHOW = CommunityBoardSettings.getIntProperty("RaidBossesNotShow", "25423,25010", ",");
			ALLOW_BOSS_RESPAWN_TIME = CommunityBoardSettings.getProperty("DisplayRespawnTime", false);
			SERVICES_PREMIUM_VALID_ID = CommunityBoardSettings.getIntProperty("PremiumValidTemplates", "1,2,3", ",");
			ALLOW_CERT_DONATE_MODE = CommunityBoardSettings.getProperty("AllowCertificationDonate", false);
			CERT_MIN_LEVEL = CommunityBoardSettings.getProperty("CertificationUseLevel", 80);
			CERT_BLOCK_SKILL_LIST = CommunityBoardSettings.getProperty("CertificationBlockSkills", "");
			EMERGET_SKILLS_LEARN = CommunityBoardSettings.getIntProperty("EmergentSkillsPrice", "4037,10", ",");
			MASTER_SKILLS_LEARN = CommunityBoardSettings.getIntProperty("MasterSkillsPrice", "4037,10", ",");
			TRANSFORM_SKILLS_LEARN = CommunityBoardSettings.getIntProperty("TransformSkillsPrice", "4037,10", ",");
			CLEAN_SKILLS_LEARN = CommunityBoardSettings.getIntProperty("CleanSkillsPrice", "4037,10", ",");
			ALLOW_TELEPORT_TO_RAID = CommunityBoardSettings.getProperty("AllowTeleportToRaid", false);
			TELEPORT_TO_RAID_PRICE = CommunityBoardSettings.getIntProperty("TeleportToRaidPrice", "57,10000", ",");
			BLOCKED_RAID_LIST = CommunityBoardSettings.getIntegerProperty("BlockedRaidList", "0", ",");
			SERVICES_CLAN_CREATE_PENALTY_ITEM = CommunityBoardSettings.getIntProperty("RemoveClanCreatePenaltyPrice", "4037,30", ",");
			SERVICES_CLAN_JOIN_PENALTY_ITEM = CommunityBoardSettings.getIntProperty("RemoveClanJoinPenaltyPrice", "4037,30", ",");
			COMMUNITY_FREE_TP_LVL = CommunityBoardSettings.getProperty("FreeTeleportsMaxLvl", 1);
			COMMUNITY_FREE_BUFF_LVL = CommunityBoardSettings.getProperty("FreeBuffsMaxLvl", 1);
			COLOR_TITLE_LIST = CommunityBoardSettings.getListProperty("ColorTitleList", "", ";");
			COLOR_NAME_LIST = CommunityBoardSettings.getListProperty("ColorNameList", "", ";");
			CHANGE_COLOR_NAME_LIST = CommunityBoardSettings.getMapStringProperty("ColorNamePriceList", "", ";");
			CHANGE_COLOR_TITLE_LIST = CommunityBoardSettings.getMapStringProperty("ColorTitlePriceList", "", ";");
			SERVICES_CHANGE_MAIN_CLASS = CommunityBoardSettings.getIntProperty("MainClassChangePrice", "4037,100", ",");
			CHANGE_MAIN_CLASS_WITHOUT_OLY_CHECK = CommunityBoardSettings.getProperty("MainClassChangeWithOutOlyCheck", false);
			SERVICES_EXPAND_INVENTORY = CommunityBoardSettings.getIntProperty("ExpandInventoryPrice", "4037,30", ",");
			SERVICES_EXPAND_TELE_TABS = CommunityBoardSettings.getIntProperty("ExpandTeleTabsPrice", "4037,30", ",");
			SERVICES_EXPAND_WAREHOUSE = CommunityBoardSettings.getIntProperty("ExpandWareHousePrice", "4037,30", ",");
			SERVICES_EXPAND_SELLSTORE = CommunityBoardSettings.getIntProperty("ExpandSellStorePrice", "4037,30", ",");
			SERVICES_EXPAND_BUYSTORE = CommunityBoardSettings.getIntProperty("ExpandBuyStorePrice", "4037,30", ",");
			SERVICES_EXPAND_DWARFRECIPE = CommunityBoardSettings.getIntProperty("ExpandDwarfRecipePrice", "4037,30", ",");
			SERVICES_EXPAND_COMMONRECIPE = CommunityBoardSettings.getIntProperty("ExpandCommonRecipePrice", "4037,30", ",");
			EXPAND_TELE_TABS_STEP = CommunityBoardSettings.getProperty("ExpandTeleTabsStep", 1);
			EXPAND_INVENTORY_STEP = CommunityBoardSettings.getProperty("ExpandInventoryStep", 1);
			EXPAND_WAREHOUSE_STEP = CommunityBoardSettings.getProperty("ExpandWareHouseStep", 1);
			EXPAND_SELLSTORE_STEP = CommunityBoardSettings.getProperty("ExpandSellStoreStep", 1);
			EXPAND_BUYSTORE_STEP = CommunityBoardSettings.getProperty("ExpandBuyStoreStep", 1);
			EXPAND_DWARFRECIPE_STEP = CommunityBoardSettings.getProperty("ExpandDwarfRecipeStep", 1);
			EXPAND_COMMONRECIPE_STEP = CommunityBoardSettings.getProperty("ExpandCommonRecipeStep", 1);
			SERVICES_EXPAND_TELE_TABS_LIMIT = CommunityBoardSettings.getProperty("CBExpandTeleTabsLimit", 5);
			SERVICES_EXPAND_INVENTORY_LIMIT = CommunityBoardSettings.getProperty("CBExpandInventoryLimit", 50);
			SERVICES_EXPAND_WAREHOUSE_LIMIT = CommunityBoardSettings.getProperty("CBExpandWareHouseLimit", 50);
			SERVICES_EXPAND_SELLSTORE_LIMIT = CommunityBoardSettings.getProperty("CBExpandSellStoreLimit", 50);
			SERVICES_EXPAND_BUYSTORE_LIMIT = CommunityBoardSettings.getProperty("CBExpandBuyStoreLimit", 50);
			SERVICES_EXPAND_DWARFRECIPE_LIMIT = CommunityBoardSettings.getProperty("CBExpandDwarfRecipeLimit", 50);
			SERVICES_EXPAND_COMMONRECIPE_LIMIT = CommunityBoardSettings.getProperty("CBExpandCommonRecipeLimit", 50);
			SERVICES_ACADEMY_REWARD = CommunityBoardSettings.getProperty("AcademyRewardsItemList", "1");
			ACADEMY_MIN_ADENA_AMOUNT = CommunityBoardSettings.getProperty("AcademyMinPrice", 1);
			ACADEMY_MAX_ADENA_AMOUNT = CommunityBoardSettings.getProperty("AcademyMaxPrice", 1000000000);
			MAX_TIME_IN_ACADEMY = CommunityBoardSettings.getProperty("AcademyKickDelay", 4320);
			CLANS_PER_PAGE = CommunityBoardSettings.getProperty("ClansPerPage", 6);
			BUFFS_PER_PAGE = CommunityBoardSettings.getProperty("BuffsPerPage", 18);
			MEMBERS_PER_PAGE = CommunityBoardSettings.getProperty("MembersPerPage", 9);
			PETITIONS_PER_PAGE = CommunityBoardSettings.getProperty("PetitionsPerPage", 9);
			SKILLS_PER_PAGE = CommunityBoardSettings.getProperty("SkillsPerPage", 5);
			CLAN_PETITION_QUESTION_LEN = CommunityBoardSettings.getProperty("PetitionQuestionLength", 300);
			CLAN_PETITION_ANSWER_LEN = CommunityBoardSettings.getProperty("PetitionAnswerLength", 300);
			CLAN_PETITION_COMMENT_LEN = CommunityBoardSettings.getProperty("PetitionCommentLength", 300);
			HARDWARE_DONATE = CommunityBoardSettings.getProperty("HardWareDonate", "1,24,4037,10;1,48,4037,20");
		}
		catch (final Exception e)
		{
			_log.warn("Config: " + e.getMessage());
			throw new Error("Failed to Load " + COMMUNITY_BOARD_CONFIG_FILE + " File.");
		}
	}
	
	private static void loadWeddingSettings(InputStream is)
	{
		try
		{
			final GameSettings WeddingSettings = new GameSettings();
			is = new FileInputStream(new File(WEDDING_CONFIG_FILE));
			WeddingSettings.load(is);
			
			ALLOW_WEDDING = WeddingSettings.getProperty("AllowWedding", false);
			WEDDING_PRICE = WeddingSettings.getProperty("WeddingPrice", 250000000);
			WEDDING_PUNISH_INFIDELITY = WeddingSettings.getProperty("WeddingPunishInfidelity", true);
			WEDDING_TELEPORT = WeddingSettings.getProperty("WeddingTeleport", true);
			WEDDING_TELEPORT_PRICE = WeddingSettings.getProperty("WeddingTeleportPrice", 50000);
			WEDDING_TELEPORT_DURATION = WeddingSettings.getProperty("WeddingTeleportDuration", 60);
			WEDDING_FORMALWEAR = WeddingSettings.getProperty("WeddingFormalWear", true);
			WEDDING_DIVORCE_COSTS = WeddingSettings.getProperty("WeddingDivorceCosts", 20);
			WEDDING_REWARD = WeddingSettings.getIntProperty("WeddingReward", "4037,10", ",");
		}
		catch (final Exception e)
		{
			_log.warn("Config: " + e.getMessage());
			throw new Error("Failed to Load " + WEDDING_CONFIG_FILE + " File.");
		}
	}
	
	private static void loadOfflineTradeSettings(InputStream is)
	{
		try
		{
			final GameSettings offtradeSettings = new GameSettings();
			is = new FileInputStream(new File(OFFLINE_TRADE_CONFIG_FILE));
			offtradeSettings.load(is);
			
			OFFLINE_TRADE_ENABLE = offtradeSettings.getProperty("OfflineTradeEnable", false);
			OFFLINE_TRADE_MIN_LVL = offtradeSettings.getProperty("OfflineTradeMinLevel", 1);
			OFFLINE_MODE_PRICE = offtradeSettings.getIntProperty("OfflineModePrice", "4037,1", ",");
			OFFLINE_MODE_TIME = offtradeSettings.getProperty("OfflineModTime", 24);
			OFFLINE_CRAFT_ENABLE = offtradeSettings.getProperty("OfflineCraftEnable", false);
			OFFLINE_MODE_IN_PEACE_ZONE = offtradeSettings.getProperty("OfflineModeInPaceZone", false);
			OFFLINE_MODE_NO_DAMAGE = offtradeSettings.getProperty("OfflineModeNoDamage", false);
			OFFLINE_SET_NAME_COLOR = offtradeSettings.getProperty("OfflineSetNameColor", false);
			OFFLINE_SET_VISUAL_EFFECT = offtradeSettings.getProperty("OfflineSetVisualEffect", false);
			OFFLINE_NAME_COLOR = Integer.decode("0x" + offtradeSettings.getProperty("OfflineNameColor", "808080"));
			OFFLINE_FAME = offtradeSettings.getProperty("OfflineFame", true);
			RESTORE_OFFLINERS = offtradeSettings.getProperty("RestoreOffliners", false);
			OFFLINE_MAX_DAYS = offtradeSettings.getProperty("OfflineMaxDays", 10);
			OFFLINE_DISCONNECT_FINISHED = offtradeSettings.getProperty("OfflineDisconnectFinished", true);
		}
		catch (final Exception e)
		{
			_log.warn("Config: " + e.getMessage());
			throw new Error("Failed to Load " + OFFLINE_TRADE_CONFIG_FILE + " File.");
		}
	}
	
	private static void loadDualSessionSettings(InputStream is)
	{
		try
		{
			final GameSettings DualSessionSettings = new GameSettings();
			is = new FileInputStream(new File(DOUBLE_SESSIONS_CONFIG_FILE));
			DualSessionSettings.load(is);
			
			DOUBLE_SESSIONS_ENABLE = DualSessionSettings.getProperty("AllowCheckSessions", false);
			DOUBLE_SESSIONS_HWIDS = DualSessionSettings.getProperty("AllowCheckSessionHwids", false);
			DOUBLE_SESSIONS_DISCONNECTED = DualSessionSettings.getProperty("AllowCheckDisconnectedSessions", true);
			DOUBLE_SESSIONS_CHECK_MAX_PLAYERS = DualSessionSettings.getProperty("SessionCheckMaxPlayers", 0);
			DOUBLE_SESSIONS_CONSIDER_OFFLINE_TRADERS = DualSessionSettings.getProperty("ConsiderSessionOfflineTraders", true);
			DOUBLE_SESSIONS_CHECK_MAX_OLYMPIAD_PARTICIPANTS = DualSessionSettings.getProperty("CheckMaxOlympiadParticipants", 0);
			DOUBLE_SESSIONS_CHECK_MAX_EVENT_PARTICIPANTS = DualSessionSettings.getProperty("CheckMaxFightEventParticipants", 0);
		}
		catch (final Exception e)
		{
			_log.warn("Config: " + e.getMessage());
			throw new Error("Failed to Load " + DOUBLE_SESSIONS_CONFIG_FILE + " File.");
		}
	}
	
	private static void loadEnchantSettings(InputStream is)
	{
		try
		{
			final GameSettings enchantSettings = new GameSettings();
			is = new FileInputStream(new File(ENCHANT_CONFIG_FILE));
			enchantSettings.load(is);
			
			ELEMENT_ARMOR_LIMIT = enchantSettings.getProperty("ElementArmorLimit", 3);
			ENCHANT_CHANCE_ELEMENT_STONE = enchantSettings.getProperty("EnchantChanceElementStone", 50);
			ENCHANT_CHANCE_ELEMENT_CRYSTAL = enchantSettings.getProperty("EnchantChanceElementCrystal", 30);
			ENCHANT_CHANCE_ELEMENT_JEWEL = enchantSettings.getProperty("EnchantChanceElementJewel", 20);
			ENCHANT_CHANCE_ELEMENT_ENERGY = enchantSettings.getProperty("EnchantChanceElementEnergy", 10);
			ENCHANT_ELEMENT_ALL_ITEMS = enchantSettings.getProperty("AllowEnchantElementAllItems", false);
			ENCHANT_BLACKLIST = enchantSettings.getIntProperty("EnchantBlackList", "7816,7817,7818,7819,7820,7821,7822,7823,7824,7825,7826,7827,7828,7829,7830,7831,13293,13294,13296", ",");
			Arrays.sort(ENCHANT_BLACKLIST);
			SYSTEM_BLESSED_ENCHANT = enchantSettings.getProperty("SystemBlessedEnchant", false);
			BLESSED_ENCHANT_SAVE = enchantSettings.getProperty("BlessedEnchantSave", 0);
			SAVE_ENCHANT_BLACKLIST = enchantSettings.getIntProperty("NotSaveEnchantBlackList", "0", ",");
			Arrays.sort(SAVE_ENCHANT_BLACKLIST);
		}
		catch (final Exception e)
		{
			_log.warn("Config: " + e.getMessage());
			throw new Error("Failed to Load " + ENCHANT_CONFIG_FILE + " File.");
		}
	}
	
	private static void loadHitmanSettings(InputStream is)
	{
		try
		{
			final GameSettings HitmanSettings = new GameSettings();
			is = new FileInputStream(new File(HITMAN_CONFIG));
			HitmanSettings.load(is);
			
			HITMAN_TAKE_KARMA = HitmanSettings.getProperty("HitmansTakekarma", true);
			HITMAN_ANNOUNCE = HitmanSettings.getProperty("HitmanAnnounce", false);
			HITMAN_MAX_PER_PAGE = HitmanSettings.getProperty("HitmanMaxPerPage", 20);
			HITMAN_CURRENCY = HitmanSettings.getIntegerProperty("HitmanCurrency", "57,4037,9143", ",");
			HITMAN_SAME_TEAM = HitmanSettings.getProperty("HitmanSameTeam", false);
			HITMAN_SAVE_TARGET = HitmanSettings.getProperty("HitmanSaveTarget", 15);
			HITMAN_TARGETS_LIMIT = HitmanSettings.getProperty("HitmanTargetsLimit", 5);
		}
		catch (final Exception e)
		{
			_log.warn("Config: " + e.getMessage());
			throw new Error("Failed to Load " + HITMAN_CONFIG + " File.");
		}
	}
	
	private static void loadUndergroundColliseumSettings(InputStream is)
	{
		try
		{
			final GameSettings undergroundcoliseum = new GameSettings();
			is = new FileInputStream(new File(UNDERGROUND_CONFIG_FILE));
			undergroundcoliseum.load(is);
			
			UC_START_TIME = undergroundcoliseum.getProperty("BattlesStartTime", "0 17 * * *");
			UC_TIME_PERIOD = undergroundcoliseum.getProperty("BattlesPeriod", 5);
			UC_ANNOUNCE_BATTLES = undergroundcoliseum.getProperty("AllowAnnouncePeriods", false);
			UC_PARTY_LIMIT = undergroundcoliseum.getProperty("PartyLimit", 7);
			UC_RESS_TIME = undergroundcoliseum.getProperty("RessTime", 10);
		}
		catch (final Exception e)
		{
			_log.warn("Config: " + e.getMessage());
			throw new Error("Failed to Load " + UNDERGROUND_CONFIG_FILE + " File.");
		}
	}
	
	private static void loadItemMallSettings(InputStream is)
	{
		try
		{
			final GameSettings itemmallSettings = new GameSettings();
			is = new FileInputStream(new File(ITEM_MALL_CONFIG_FILE));
			itemmallSettings.load(is);
			
			GAME_POINT_ITEM_ID = itemmallSettings.getProperty("GamePointItemId", -1);
		}
		catch (final Exception e)
		{
			_log.warn("Config: " + e.getMessage());
			throw new Error("Failed to Load " + ITEM_MALL_CONFIG_FILE + " File.");
		}
	}
	
	private static void loadLeprechaunSettings(InputStream is)
	{
		try
		{
			final GameSettings leprechaunEventSettings = new GameSettings();
			is = new FileInputStream(new File(LEPRECHAUN_FILE));
			leprechaunEventSettings.load(is);
			
			ENABLED_LEPRECHAUN = leprechaunEventSettings.getProperty("EnabledLeprechaun", false);
			LEPRECHAUN_ID = leprechaunEventSettings.getProperty("LeprechaunId", 7805);
			LEPRECHAUN_FIRST_SPAWN_DELAY = leprechaunEventSettings.getProperty("LeprechaunFirstSpawnDelay", 5);
			LEPRECHAUN_RESPAWN_INTERVAL = leprechaunEventSettings.getProperty("LeprechaunRespawnInterval", 60);
			LEPRECHAUN_SPAWN_TIME = leprechaunEventSettings.getProperty("LeprechaunSpawnTime", 30);
			LEPRECHAUN_ANNOUNCE_INTERVAL = leprechaunEventSettings.getProperty("LeprechaunAnnounceInterval", 5);
			SHOW_NICK = leprechaunEventSettings.getProperty("ShowNick", true);
			SHOW_REGION = leprechaunEventSettings.getProperty("ShowRegion", true);
			LEPRECHAUN_REWARD_ID = leprechaunEventSettings.getIntProperty("LeprechaunRewardId", "57,4037", ",");
			LEPRECHAUN_REWARD_COUNT = leprechaunEventSettings.getIntProperty("LeprechaunRewardCount", "1000000,10", ",");
			LEPRECHAUN_REWARD_CHANCE = leprechaunEventSettings.getIntProperty("LeprechaunRewardChance", "100,60", ",");
		}
		catch (final Exception e)
		{
			_log.warn("Config: " + e.getMessage());
			throw new Error("Failed to Load " + LEPRECHAUN_FILE + " File.");
		}
	}
	
	private static void loadAerialCleftSettings(InputStream is)
	{
		try
		{
			final GameSettings cleftSettings = new GameSettings();
			is = new FileInputStream(new File(AERIAL_CLEFT_FILE));
			cleftSettings.load(is);
			
			CLEFT_MIN_TEAM_PLAYERS = cleftSettings.getProperty("CleftMinTeamPlayers", 1);
			CLEFT_BALANCER = cleftSettings.getProperty("CleftTeamsBalancer", true);
			CLEFT_WAR_TIME = cleftSettings.getProperty("CleftEventTime", 25);
			CLEFT_COLLECT_TIME = cleftSettings.getProperty("CleftCollectTime", 5);
			CLEFT_REWARD_ID = cleftSettings.getProperty("CleftRewardId", 13749);
			CLEFT_REWARD_COUNT_WINNER = cleftSettings.getProperty("CleftRewardCountWinner", 50);
			CLEFT_REWARD_COUNT_LOOSER = cleftSettings.getProperty("CleftRewardCountLooser", 20);
			CLEFT_MIN_PLAYR_EVENT_TIME = cleftSettings.getProperty("CleftMinPlayerInEventTime", 15);
			CLEFT_WITHOUT_SEEDS = cleftSettings.getProperty("CleftWithountSeeds", false);
			CLEFT_MIN_LEVEL = cleftSettings.getProperty("CleftMinLevel", 75);
			CLEFT_TIME_RELOAD_REG = cleftSettings.getProperty("CleftTimeReloadTime", 60);
			CLEFT_MAX_PLAYERS = cleftSettings.getProperty("CleftMaximumPlayers", 18);
			CLEFT_RESPAWN_DELAY = cleftSettings.getProperty("CleftRespawnDelay", 10);
			CLEFT_LEAVE_DELAY = cleftSettings.getProperty("CleftLeaveDelay", 2);
			LARGE_COMPRESSOR_POINT = cleftSettings.getProperty("CleftLargeCompressorPoint", 100);
			SMALL_COMPRESSOR_POINT = cleftSettings.getProperty("CleftSmallCompressorPoint", 40);
			TEAM_CAT_POINT = cleftSettings.getProperty("CleftTeamCatPoint", 10);
			TEAM_PLAYER_POINT = cleftSettings.getProperty("CleftPlayerPoint", 1);
		}
		catch (final Exception e)
		{
			_log.warn("Config: " + e.getMessage());
			throw new Error("Failed to Load " + AERIAL_CLEFT_FILE + " File.");
		}
	}
	
	private static void loadOlyAntiFeedSettings(InputStream is)
	{
		try
		{
			final GameSettings antifeedoly = new GameSettings();
			is = new FileInputStream(new File(OLY_ANTI_FEED_FILE));
			antifeedoly.load(is);
			
			ENABLE_OLY_FEED = antifeedoly.getProperty("OlympiadAntiFeedEnable", false);
			OLY_ANTI_FEED_WEAPON_RIGHT = antifeedoly.getProperty("OlympiadAntiFeedRightWeapon", 0);
			OLY_ANTI_FEED_WEAPON_LEFT = antifeedoly.getProperty("OlympiadAntiFeedLeftWeapon", 0);
			OLY_ANTI_FEED_GLOVES = antifeedoly.getProperty("OlympiadAntiFeedGloves", 0);
			OLY_ANTI_FEED_CHEST = antifeedoly.getProperty("OlympiadAntiFeedChest", 0);
			OLY_ANTI_FEED_LEGS = antifeedoly.getProperty("OlympiadAntiFeedLegs", 0);
			OLY_ANTI_FEED_FEET = antifeedoly.getProperty("OlympiadAntiFeedFeet", 0);
			OLY_ANTI_FEED_CLOAK = antifeedoly.getProperty("OlympiadAntiFeedCloak", 0);
			OLY_ANTI_FEED_RIGH_HAND_ARMOR = antifeedoly.getProperty("OlympiadAntiFeedRightArmor", 0);
			OLY_ANTI_FEED_HAIR_MISC_1 = antifeedoly.getProperty("OlympiadAntiFeedHair1", 0);
			OLY_ANTI_FEED_HAIR_MISC_2 = antifeedoly.getProperty("OlympiadAntiFeedHair2", 0);
			OLY_ANTI_FEED_RACE = antifeedoly.getProperty("OlympiadAntiFeedRace", 0);
			OLY_ANTI_FEED_GENDER = antifeedoly.getProperty("OlympiadAntiFeedGender", 0);
			OLY_ANTI_FEED_CLASS_RADIUS = antifeedoly.getProperty("OlympiadAntiFeedClassRadius", 0);
			OLY_ANTI_FEED_CLASS_HEIGHT = antifeedoly.getProperty("OlympiadAntiFeedClassHeight", 0);
			OLY_ANTI_FEED_PLAYER_HAVE_RECS = antifeedoly.getProperty("OlympiadAntiFeedHaveRecs", 0);
		}
		catch (final Exception e)
		{
			_log.warn("Config: " + e.getMessage());
			throw new Error("Failed to Load " + OLY_ANTI_FEED_FILE + " File.");
		}
	}
	
	private static void loadGeodataSettings(InputStream is)
	{
		try
		{
			final GameSettings geoSettings = new GameSettings();
			is = new FileInputStream(new File(GEO_CONFIG_FILE));
			geoSettings.load(is);
			
			GEODATA = geoSettings.getProperty("AllowGeoData", false);
			GEO_X_FIRST = geoSettings.getProperty("GeoFirstX", 11);
			GEO_Y_FIRST = geoSettings.getProperty("GeoFirstY", 10);
			GEO_X_LAST = geoSettings.getProperty("GeoLastX", 26);
			GEO_Y_LAST = geoSettings.getProperty("GeoLastY", 26);
			PATHFIND_BOOST = geoSettings.getProperty("PathFindBoost", true);
			if (!GEODATA)
			{
				PATHFIND_BOOST = false;
			}
			PATHFIND_BUFFERS = geoSettings.getProperty("PathFindBuffers", "100x6;128x6;192x6;256x4;320x4;384x4;500x2");
			ADVANCED_DIAGONAL_STRATEGY = geoSettings.getProperty("PathFindDiagonal", true);
			MAX_POSTFILTER_PASSES = geoSettings.getProperty("MaxPostfilterPasses", 3);
			DEBUG_PATH = geoSettings.getProperty("DebugPath", false);
			COORD_SYNCHRONIZE = geoSettings.getProperty("CoordSynchronize", true);
			if (!GEODATA)
			{
				COORD_SYNCHRONIZE = false;
			}
			GEO_MOVE_SPEED = geoSettings.getProperty("GeoMoveSpeed", 200);
			ALLOW_GEOMOVE_VALIDATE = geoSettings.getProperty("AllowGeoMoveValidate", false);
			ALLOW_DOOR_VALIDATE = geoSettings.getProperty("AllowDoorValidate", false);
			LOW_WEIGHT = geoSettings.getProperty("LowWeight", 0.5F);
			MEDIUM_WEIGHT = geoSettings.getProperty("MediumWeight", 2.0F);
			HIGH_WEIGHT = geoSettings.getProperty("HighWeight", 3.0F);
			DIAGONAL_WEIGHT = geoSettings.getProperty("DiagonalWeight", 0.707F);
		}
		catch (final Exception e)
		{
			_log.warn("Config: " + e.getMessage());
			throw new Error("Failed to Load " + GEO_CONFIG_FILE + " File.");
		}
	}
	
	private static void loadAntiBotSettings(InputStream is)
	{
		try
		{
			final GameSettings antiBotSettings = new GameSettings();
			is = new FileInputStream(new File(ANTIBOT_CONFIG));
			antiBotSettings.load(is);
			
			ENABLE_ANTI_BOT_SYSTEM = antiBotSettings.getProperty("EnableAntiBotSystem", false);
			ASK_ANSWER_DELAY = antiBotSettings.getProperty("ASK_ANSWER_DELAY", 3);
			MINIMUM_TIME_QUESTION_ASK = antiBotSettings.getProperty("MinimumTimeQuestionAsk", 60);
			MAXIMUM_TIME_QUESTION_ASK = antiBotSettings.getProperty("MaximumTimeQuestionAsk", 120);
			MINIMUM_BOT_POINTS_TO_STOP_ASKING = antiBotSettings.getProperty("MinimumBotPointsToStopAsking", 10);
			MAXIMUM_BOT_POINTS_TO_STOP_ASKING = antiBotSettings.getProperty("MaximumBotPointsToStopAsking", 15);
			MAX_BOT_POINTS = antiBotSettings.getProperty("MaxBotPoints", 15);
			MINIMAL_BOT_RATING_TO_BAN = antiBotSettings.getProperty("MinimalBotPointsToBan", -5);
			ANNOUNCE_AUTO_BOT_BAN = antiBotSettings.getProperty("AnounceAutoBan", false);
			ON_WRONG_QUESTION_KICK = antiBotSettings.getProperty("IfWrongKick", false);
		}
		catch (final Exception e)
		{
			_log.warn("Config: " + e.getMessage());
			throw new Error("Failed to Load " + ANTIBOT_CONFIG + " File.");
		}
	}
	
	private static void loadFakeSettings(InputStream is)
	{
		try
		{
			final GameSettings fakeSettings = new GameSettings();
			is = new FileInputStream(new File(FAKES_CONFIG));
			fakeSettings.load(is);
			
			ALLOW_FAKE_PLAYERS = fakeSettings.getProperty("AllowFakePlayers", false);
			ALLOW_ENCHANT_WEAPONS = fakeSettings.getProperty("AllowEnchantWeapons", false);
			ALLOW_ENCHANT_ARMORS = fakeSettings.getProperty("AllowEnchantArmors", false);
			ALLOW_ENCHANT_JEWERLYS = fakeSettings.getProperty("AllowEnchantJewerlys", false);
			RND_ENCHANT_WEAPONS = fakeSettings.getIntProperty("RandomEnchatWeapon", "1,15", ",");
			RND_ENCHANT_ARMORS = fakeSettings.getIntProperty("RandomEnchatArmor", "1,15", ",");
			RND_ENCHANT_JEWERLYS = fakeSettings.getIntProperty("RandomEnchatJewerly", "1,15", ",");
			FAKE_FIGHTER_BUFFS = fakeSettings.getDoubleIntProperty("FakeFighterBuffs", "1204,2", ";");
			FAKE_MAGE_BUFFS = fakeSettings.getDoubleIntProperty("FakeMageBuffs", "1204,2", ";");
			ALLOW_SPAWN_FAKE_PLAYERS = fakeSettings.getProperty("AllowAutoSpawnFakes", false);
			ENCHANTERS_MAX_LVL = fakeSettings.getProperty("FakeMaxEnchantItems", 20);
			FAKE_PLAYERS_AMOUNT = fakeSettings.getProperty("FakePlayersAmount", 50);
			FAKE_DELAY_TELEPORT_TO_FARM = fakeSettings.getProperty("FakeDelayTeleToFarm", 5);
			FAKE_SPAWN_DELAY = fakeSettings.getProperty("FakeSpawnDelay", 30000);
			FAKE_ACTIVE_INTERVAL = fakeSettings.getProperty("ActiveFakeSpawnInterval", 2000);
			FAKE_PASSIVE_INTERVAL = fakeSettings.getProperty("PassiveFakeSpawnInterval", 10000);
		}
		catch (final Exception e)
		{
			_log.warn("Config: " + e.getMessage());
			throw new Error("Failed to Load " + FAKES_CONFIG + " File.");
		}
	}
	
	private static void loadFightEventsSettings(InputStream is)
	{
		try
		{
			final GameSettings fightEventSettings = new GameSettings();
			is = new FileInputStream(new File(FIGHT_EVENTS_FILE));
			fightEventSettings.load(is);

			ALLOW_FIGHT_EVENTS = fightEventSettings.getProperty("AllowFightEvents", false);
			ALLOW_RESPAWN_PROTECT_PLAYER = fightEventSettings.getProperty("AllowRespawnProtect", false);
			ALLOW_REG_CONFIRM_DLG = fightEventSettings.getProperty("AllowRegisterAtConfirmDlg", true);
			FIGHT_EVENTS_REG_TIME = fightEventSettings.getProperty("FightEventRegisterTime", 3);
			if (FIGHT_EVENTS_REG_TIME > 10)
			{
				FIGHT_EVENTS_REG_TIME = 10;
			}
			else if (FIGHT_EVENTS_REG_TIME < 1)
			{
				FIGHT_EVENTS_REG_TIME = 1;
			}
			DISALLOW_FIGHT_EVENTS = fightEventSettings.getIntProperty("NotAllowedFightEvents", "0", ",");
			Arrays.sort(DISALLOW_FIGHT_EVENTS);
			FIGHT_EVENTS_REWARD_MULTIPLIER = fightEventSettings.getProperty("RewardMultiplier", 2);
			TIME_FIRST_TELEPORT = fightEventSettings.getProperty("TimeFirstTeleport", 10);
			TIME_PLAYER_TELEPORTING = fightEventSettings.getProperty("TimeTeleportPlayers", 15);
			TIME_PREPARATION_BEFORE_FIRST_ROUND = fightEventSettings.getProperty("TimeBeforeFirstRound", 30);
			TIME_PREPARATION_BETWEEN_NEXT_ROUNDS = fightEventSettings.getProperty("TimeBeforeNextRound", 30);
			TIME_AFTER_ROUND_END_TO_RETURN_SPAWN = fightEventSettings.getProperty("TimeAfterRoundEnd", 15);
			TIME_TELEPORT_BACK_TOWN = fightEventSettings.getProperty("TeleportTimeBackTown", 30);
			TIME_MAX_SECONDS_OUTSIDE_ZONE = fightEventSettings.getProperty("TimeOutsideZone", 10);
			TIME_TO_BE_AFK = fightEventSettings.getProperty("TimeToBeAfk", 120);
			TIME_AFK_TO_KICK = fightEventSettings.getProperty("TimeAfkToKick", 60);
			ITEMS_FOR_MINUTE_OF_AFK = fightEventSettings.getProperty("ItemsForMinOfAfk", -1);
		}
		catch (final Exception e)
		{
			_log.warn("Config: " + e.getMessage());
			throw new Error("Failed to Load " + FIGHT_EVENTS_FILE + " File.");
		}
	}
	
	private static void loadChatSettings(InputStream is)
	{
		try
		{
			final GameSettings chatSettings = new GameSettings();
			is = new FileInputStream(new File(CHAT_CONFIG_FILE));
			chatSettings.load(is);
			
			DEFAULT_GLOBAL_CHAT = chatSettings.getProperty("GlobalChat", "ON");
			DEFAULT_TRADE_CHAT = chatSettings.getProperty("TradeChat", "ON");
			USE_SAY_FILTER = chatSettings.getProperty("UseChatFilter", false);
			USE_BROADCAST_SAY_FILTER = chatSettings.getProperty("UseBroadCastChatFilter", false);
			CHAT_FILTER_CHARS = chatSettings.getProperty("ChatFilterChars", "^_^");
			BAN_CHAT_CHANNELS = chatSettings.getIntProperty("BanChatChannels", "0;1;8;17", ";");
			ALLOW_CUSTOM_CHAT = chatSettings.getProperty("AllowCustomChat", false);
			CHECK_CHAT_VALID = chatSettings.getProperty("CheckValidCustomChat", 0);
			CHAT_MSG_SIMPLE = chatSettings.getProperty("ChatMessageSimpleAcc", 20);
			CHAT_MSG_PREMIUM = chatSettings.getProperty("ChatMessagePremiumAcc", 40);
			CHAT_MSG_ANNOUNCE = chatSettings.getProperty("ChatAnnounceMessage", 5);
			MIN_LVL_GLOBAL_CHAT = chatSettings.getProperty("GlobalChatMinLevel", 1);
		}
		catch (final Exception e)
		{
			_log.warn("Config: " + e.getMessage());
			throw new Error("Failed to Load " + CHAT_CONFIG_FILE + " File.");
		}
	}
	
	private static void loadSomikIntefaceSettings(InputStream is)
	{
		try
		{
			final GameSettings somikSettings = new GameSettings();
			is = new FileInputStream(new File(SOMIK_FILE));
			somikSettings.load(is);
			
			INTERFACE_SETTINGS_1 = "";
			INTERFACE_SETTINGS_2 = "";
			
			INTERFACE_SETTINGS_1 += " AutoAugment=";
			INTERFACE_SETTINGS_1 += somikSettings.getProperty("AutoAugment", false) ? "1" : "0";
			INTERFACE_SETTINGS_1 += " AutoAttribute=";
			INTERFACE_SETTINGS_1 += somikSettings.getProperty("AutoAttribute", false) ? "1" : "0";
			INTERFACE_SETTINGS_1 += " AutoEnchant=";
			INTERFACE_SETTINGS_1 += somikSettings.getProperty("AutoEnchant", false) ? "1" : "0";
			INTERFACE_SETTINGS_1 += " AutoSkillEnchant=";
			INTERFACE_SETTINGS_1 += somikSettings.getProperty("AutoSkillEnchant", false) ? "1" : "0";
			INTERFACE_SETTINGS_2 += " MinAutoItemEnchantSpeed=";
			INTERFACE_SETTINGS_2 += somikSettings.getProperty("MinAutoItemEnchantSpeed", 1000);
			INTERFACE_SETTINGS_2 += " MaxAutoItemEnchantSpeed=";
			INTERFACE_SETTINGS_2 += somikSettings.getProperty("MaxAutoItemEnchantSpeed", 2000);
			INTERFACE_SETTINGS_2 += " MinAutoSkillEnchantSpeed=";
			INTERFACE_SETTINGS_2 += somikSettings.getProperty("MinAutoSkillEnchantSpeed", 1000);
			INTERFACE_SETTINGS_2 += " MaxAutoSkillEnchantSpeed=";
			INTERFACE_SETTINGS_2 += somikSettings.getProperty("MaxAutoSkillEnchantSpeed", 2000);
			INTERFACE_SETTINGS_2 += " MinAutoIAttributeEnchantSpeed=";
			INTERFACE_SETTINGS_2 += somikSettings.getProperty("MinAutoIAttributeEnchantSpeed", 1000);
			INTERFACE_SETTINGS_2 += " MaxAutoAttributeEnchantSpeed=";
			INTERFACE_SETTINGS_2 += somikSettings.getProperty("MaxAutoAttributeEnchantSpeed", 2000);
			INTERFACE_SETTINGS_2 += " MinAutoAugmentEnchantSpeed=";
			INTERFACE_SETTINGS_2 += somikSettings.getProperty("MinAutoAugmentEnchantSpeed", 1000);
			INTERFACE_SETTINGS_2 += " MaxAutoAugmentEnchantSpeed=";
			INTERFACE_SETTINGS_2 += somikSettings.getProperty("MaxAutoAugmentEnchantSpeed", 2000);
			
			// Auto Item Use Features
			INTERFACE_SETTINGS_1 += " AutoSoulsAndForces=";
			INTERFACE_SETTINGS_1 += somikSettings.getProperty("AutoSoulsAndForces", false) ? "1" : "0";
			INTERFACE_SETTINGS_1 += " AutoPotions=";
			INTERFACE_SETTINGS_1 += somikSettings.getProperty("AutoPotions", false) ? "1" : "0";
			INTERFACE_SETTINGS_1 += " AutoShots=";
			INTERFACE_SETTINGS_1 += somikSettings.getProperty("AutoShots", false) ? "1" : "0";
			
			// Auto Retarget
			INTERFACE_SETTINGS_1 += " AntiMirage=";
			INTERFACE_SETTINGS_1 += somikSettings.getProperty("AntiMirage", false) ? "1" : "0";
			INTERFACE_SETTINGS_1 += " AutoAssist=";
			INTERFACE_SETTINGS_1 += somikSettings.getProperty("AutoAssist", false) ? "1" : "0";
			
			// Target Information
			INTERFACE_SETTINGS_1 += " TargetInfo=";
			INTERFACE_SETTINGS_1 += somikSettings.getProperty("TargetInfo", false) ? "1" : "0";
			INTERFACE_SETTINGS_1 += " EnemyCastInfo=";
			INTERFACE_SETTINGS_1 += somikSettings.getProperty("EnemyCastInfo", false) ? "1" : "0";
			
			// Auto Farm
			INTERFACE_SETTINGS_1 += " LoopMacros=";
			INTERFACE_SETTINGS_1 += somikSettings.getProperty("LoopMacros", false) ? "1" : "0";
			INTERFACE_SETTINGS_1 += " AutoPlay=";
			INTERFACE_SETTINGS_1 += somikSettings.getProperty("AutoPlay", false) ? "1" : "0";
			INTERFACE_SETTINGS_1 += " PremiumOnly_AutoPlay=";
			INTERFACE_SETTINGS_1 += somikSettings.getProperty("PremiumOnly_AutoPlay", false) ? "1" : "0";
			INTERFACE_SETTINGS_1 += " PremiumItem_ClassID=";
			INTERFACE_SETTINGS_1 += somikSettings.getProperty("PremiumItem_ClassID", 0);
			INTERFACE_SETTINGS_1 += " UsePremiumState=";
			INTERFACE_SETTINGS_1 += somikSettings.getProperty("UsePremiumState", false) ? "1" : "0";
			INTERFACE_SETTINGS_1 += " FlagSkill_AutoPlay=";
			INTERFACE_SETTINGS_1 += somikSettings.getProperty("FlagSkill_AutoPlay", false) ? "1" : "0";
			INTERFACE_SETTINGS_1 += " FlagSkill_ClassID=";
			INTERFACE_SETTINGS_1 += somikSettings.getProperty("FlagSkill_ClassID", 0);
			
			// Custom Chat Commands
			INTERFACE_SETTINGS_2 += " RemoveBuffCommands=";
			INTERFACE_SETTINGS_2 += somikSettings.getProperty("RemoveBuffCommands", false) ? "1" : "0";
			INTERFACE_SETTINGS_2 += " TargetNextLong=";
			INTERFACE_SETTINGS_2 += somikSettings.getProperty("TargetNextLong", false) ? "1" : "0";
			
			// Olympiad
			INTERFACE_SETTINGS_2 += " OlyTargetWindowClickable=";
			INTERFACE_SETTINGS_2 += somikSettings.getProperty("OlyTargetWindowClickable", false) ? "1" : "0";
			INTERFACE_SETTINGS_2 += " OlyTriggerTimers=";
			INTERFACE_SETTINGS_2 += somikSettings.getProperty("OlyTriggerTimers", false) ? "1" : "0";
			INTERFACE_SETTINGS_2 += " OlyStartInfo=";
			INTERFACE_SETTINGS_2 += somikSettings.getProperty("OlyStartInfo", false) ? "1" : "0";
			INTERFACE_SETTINGS_2 += " OlyDmgCounter=";
			INTERFACE_SETTINGS_2 += somikSettings.getProperty("OlyDmgCounter", false) ? "1" : "0";
			INTERFACE_SETTINGS_2 += " OlyTargetHealthNumbers=";
			INTERFACE_SETTINGS_2 += somikSettings.getProperty("OlyTargetHealthNumbers", false) ? "1" : "0";
			
			// Features
			INTERFACE_SETTINGS_2 += " AutoCacheClean=";
			INTERFACE_SETTINGS_2 += somikSettings.getProperty("AutoCacheClean", false) ? "1" : "0";
			
			// Watermark on the bottom right of the screen
			INTERFACE_SETTINGS_1 += " Watermark=";
			INTERFACE_SETTINGS_1 += somikSettings.getProperty("Watermark", false) ? "1" : "0";
			INTERFACE_SETTINGS_1 += " Watermark_Text=";
			INTERFACE_SETTINGS_1 += somikSettings.getProperty("Watermark_Text", "\u007FSomik v1.0 Patch\u007F");
			INTERFACE_SETTINGS_1 += " Watermark_R=";
			INTERFACE_SETTINGS_1 += somikSettings.getProperty("Watermark_R", 100);
			INTERFACE_SETTINGS_1 += " Watermark_G=";
			INTERFACE_SETTINGS_1 += somikSettings.getProperty("Watermark_G", 100);
			INTERFACE_SETTINGS_1 += " Watermark_B=";
			INTERFACE_SETTINGS_1 += somikSettings.getProperty("Watermark_B", 100);
			INTERFACE_SETTINGS_1 += " Watermark_A=";
			INTERFACE_SETTINGS_1 += somikSettings.getProperty("Watermark_A", 100);
			
			// 4 Buttons in the Options - Interface Tab
			INTERFACE_SETTINGS_1 += " OptionsButton1=";
			INTERFACE_SETTINGS_1 += somikSettings.getProperty("OptionsButton1", false) ? "1" : "0";
			INTERFACE_SETTINGS_1 += " OptionsButton1_Name=";
			INTERFACE_SETTINGS_1 += somikSettings.getProperty("OptionsButton1_Name", "Button1");
			INTERFACE_SETTINGS_1 += " OptionsButton1_URL=";
			INTERFACE_SETTINGS_1 += somikSettings.getProperty("OptionsButton1_URL", "");
			INTERFACE_SETTINGS_1 += " OptionsButton2=";
			INTERFACE_SETTINGS_1 += somikSettings.getProperty("OptionsButton2", false) ? "1" : "0";
			INTERFACE_SETTINGS_1 += " OptionsButton2_Name=";
			INTERFACE_SETTINGS_1 += somikSettings.getProperty("OptionsButton2_Name", "Button2");
			INTERFACE_SETTINGS_1 += " OptionsButton2_URL=";
			INTERFACE_SETTINGS_1 += somikSettings.getProperty("OptionsButton2_URL", "");
			INTERFACE_SETTINGS_1 += " OptionsButton3=";
			INTERFACE_SETTINGS_1 += somikSettings.getProperty("OptionsButton3", false) ? "1" : "0";
			INTERFACE_SETTINGS_1 += " OptionsButton3_Name=";
			INTERFACE_SETTINGS_1 += somikSettings.getProperty("OptionsButton3_Name", "Button3");
			INTERFACE_SETTINGS_1 += " OptionsButton3_URL=";
			INTERFACE_SETTINGS_1 += somikSettings.getProperty("OptionsButton3_URL", "");
			INTERFACE_SETTINGS_1 += " OptionsButton4=";
			INTERFACE_SETTINGS_1 += somikSettings.getProperty("OptionsButton4", false) ? "1" : "0";
			INTERFACE_SETTINGS_1 += " OptionsButton4_Name=";
			INTERFACE_SETTINGS_1 += somikSettings.getProperty("OptionsButton4_Name", "Button4");
			INTERFACE_SETTINGS_1 += " OptionsButton4_URL=";
			INTERFACE_SETTINGS_1 += somikSettings.getProperty("OptionsButton4_URL", "");
		}
		catch (final Exception e)
		{
			_log.warn("Config: " + e.getMessage());
			throw new Error("Failed to Load " + SOMIK_FILE + " File.");
		}
	}
	
	private static void loadWeeklyTraderSettings(InputStream is)
	{
		try
		{
			final GameSettings weeklySettings = new GameSettings();
			is = new FileInputStream(new File(WEEKLY_TRADER_FILE));
			weeklySettings.load(is);
			
			WEEKLY_TRADER_ENABLE = weeklySettings.getProperty("EnableWeeklyTrader", false);
			WEEKLY_TRADER_DAY_OF_WEEK = weeklySettings.getProperty("DayOfWeek", 0);
			WEEKLY_TRADER_HOUR_OF_DAY = weeklySettings.getProperty("HourOfDay", 0);
			WEEKLY_TRADER_MINUTE_OF_DAY = weeklySettings.getProperty("MinuteOfDay", 0);
			WEEKLY_TRADER_DURATION = weeklySettings.getProperty("Duration", 120);
			WEEKLY_TRADER_MULTISELL_ID = weeklySettings.getProperty("MultisellId", 9999999);
		}
		catch (final Exception e)
		{
			_log.warn("Config: " + e.getMessage());
			throw new Error("Failed to Load " + WEEKLY_TRADER_FILE + " File.");
		}
	}
	
	private static void loadModsSettings()
	{
		ALLOW_DAILY_REWARD = isAllowDailyReward();
		ALLOW_DAILY_TASKS = isAllowDailyTask();
		ALLOW_VISUAL_SYSTEM = isAllowVisual();
		ALLOW_VIP_SYSTEM = isAllowVipSystem();
		ALLOW_REVENGE_SYSTEM = isAllowRevengeSystem();
		ALLOW_MUTIPROFF_SYSTEM = isAllowMultiProffSystem();
		ALLOW_DAILY_ITEMS = isAllowDailyItems();
	}

	private static void loadFloodProtectorConfigs(final GameSettings properties)
	{
		FLOOD_PROTECTORS.clear();
		final String[] floodProtectorTypes = properties.getProperty("FLOOD_PROTECTORS_TYPES", "").split(";");
		for (final String type : floodProtectorTypes)
		{
			if (StringUtils.isEmpty(type))
			{
				continue;
			}
			
			final FloodProtectorConfig floodProtector = FloodProtectorConfig.load(type, properties);
			if (floodProtector == null)
			{
				continue;
			}
			FLOOD_PROTECTORS.add(floodProtector);
		}
	}

	public static int getServerTypeId(String[] serverTypes)
	{
		int tType = 0;
		for (String cType : serverTypes)
		{
			cType = cType.trim();
			if (cType.equalsIgnoreCase("Normal"))
			{
				tType |= 0x01;
			}
			else if (cType.equalsIgnoreCase("Relax"))
			{
				tType |= 0x02;
			}
			else if (cType.equalsIgnoreCase("Test"))
			{
				tType |= 0x04;
			}
			else if (cType.equalsIgnoreCase("NoLabel"))
			{
				tType |= 0x08;
			}
			else if (cType.equalsIgnoreCase("Restricted"))
			{
				tType |= 0x10;
			}
			else if (cType.equalsIgnoreCase("Event"))
			{
				tType |= 0x20;
			}
			else if (cType.equalsIgnoreCase("Free"))
			{
				tType |= 0x40;
			}
		}
		return tType;
	}

	private static boolean isAllowDailyTask()
	{
		return new File(Config.DATAPACK_ROOT + "/data/scripts/services/DailyTask.java").exists();
	}
	
	private static boolean isAllowDailyReward()
	{
		return new File(Config.DATAPACK_ROOT + "/data/scripts/services/DailyReward.java").exists();
	}
	
	private static boolean isAllowVisual()
	{
		return new File(Config.DATAPACK_ROOT + "/data/scripts/services/VisualMe.java").exists();
	}
	
	private static boolean isAllowVipSystem()
	{
		return new File(Config.DATAPACK_ROOT + "/data/scripts/services/CommunityVip.java").exists();
	}
	
	private static boolean isAllowRevengeSystem()
	{
		return new File(Config.DATAPACK_ROOT + "/data/scripts/services/RevengeCmd.java").exists();
	}
	
	private static boolean isAllowMultiProffSystem()
	{
		return new File(Config.DATAPACK_ROOT + "/data/scripts/services/CommunityMultiProff.java").exists();
	}
	
	private static boolean isAllowDailyItems()
	{
		return new File(Config.DATAPACK_ROOT + "/data/stats/services/dailyItems.xml").exists();
	}
	
	public static HashMap<String, String> getPersonalConfigs()
	{
		return _personalConfigs;
	}
}