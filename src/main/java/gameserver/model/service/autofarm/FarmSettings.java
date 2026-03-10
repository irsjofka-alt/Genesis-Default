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
package gameserver.model.service.autofarm;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gameserver.Config;
import gameserver.utils.GameSettings;

public class FarmSettings
{
	public static boolean ALLOW_ADD_FARM_TIME;
	public static boolean FARM_ONLINE_TYPE;
	public static boolean PREMIUM_FARM_FREE;
	public static boolean ALLOW_AUTO_FARM;
	public static boolean AUTO_FARM_FOR_PREMIUM;
	public static boolean AUTO_FARM_FREE;
	public static boolean ALLOW_OFFLINE;
	public static Map<Integer, String> AUTO_FARM_PRICES;
	public static int ATTACK_SKILL_CHANCE;
	public static int ATTACK_SKILL_PERCENT;
	public static int CHANCE_SKILL_CHANCE;
	public static int CHANCE_SKILL_PERCENT;
	public static int SELF_SKILL_CHANCE;
	public static int SELF_SKILL_PERCENT;
	public static int HEAL_SKILL_CHANCE;
	public static int HEAL_SKILL_PERCENT;
	public static int SUMMON_ATTACK_SKILL_CHANCE;
	public static int SUMMON_ATTACK_SKILL_PERCENT;
	public static int SUMMON_SELF_SKILL_CHANCE;
	public static int SUMMON_SELF_SKILL_PERCENT;
	public static int SUMMON_HEAL_SKILL_CHANCE;
	public static int SUMMON_HEAL_SKILL_PERCENT;
	public static long SKILLS_EXTRA_DELAY;
	public static long KEEP_LOCATION_DELAY;
	public static long RUN_CLOSE_UP_DELAY;
	public static int RUN_CLOSE_UP_DISTANCE;
	public static int SHORTCUT_PAGE;
	public static int SEARCH_DISTANCE;
	public static int FARM_TYPE;
	public static int FARM_INTERVAL_TASK;
	public static int MAX_SKILLS;
	public static List<Integer> RESURRECTION_ITEMS;
	public static boolean ALLOW_FARM_FREE_TIME;
	public static boolean REFRESH_FARM_TIME;
	public static int FARM_FREE_TIME;
	public static boolean ALLOW_CHECK_HWID_LIMIT;
	public static int FARM_ACTIVE_LIMITS;
	public static int[] FARM_EXPEND_LIMIT_PRICE = new int[2];
	public static int WAIT_TIME;
	public static boolean ALLOW_RESURRECTION;
	public static Map<String, Integer> REGIONS_SEARCH;
	public static int OFFLINE_FARM_MAX_DAYS;
	public static boolean DOUBLE_SESSIONS_CONSIDER_OFFLINE_FARM;
	
	protected FarmSettings()
	{
	}
	
	public final void load()
	{
		if (!isAllowSystem())
		{
			return;
		}
		
		final var farmSettings = new GameSettings();
		final var file = new File(Config.AUTO_FARM_FILE);
		try (
		    var is = new FileInputStream(file))
		{
			farmSettings.load(is);
		}
		catch (final Exception e)
		{
		}
		
		ALLOW_AUTO_FARM = farmSettings.getProperty("AllowAutoFarm", false, false);
		ALLOW_ADD_FARM_TIME = farmSettings.getProperty("AllowIncreaseFarmTime", false, false);
		FARM_ONLINE_TYPE = farmSettings.getProperty("AutoFarmOnlineType", false, false);
		AUTO_FARM_FOR_PREMIUM = farmSettings.getProperty("AutoFarmOnlyForPremium", false, false);
		AUTO_FARM_FREE = farmSettings.getProperty("AutoFarmIsFree", false, false);
		PREMIUM_FARM_FREE = farmSettings.getProperty("AutoFarmIsFreeForPremium", false, false);
		
		final String[] priceSplits = farmSettings.getProperty("AutoFarmPriceList", "", false).split(";");
		AUTO_FARM_PRICES = new HashMap<>(priceSplits.length);
		for (final String price : priceSplits)
		{
			final String[] priceSplit = price.split(",");
			if (priceSplit.length == 2)
			{
				try
				{
					AUTO_FARM_PRICES.put(Integer.parseInt(priceSplit[0]), priceSplit[1]);
				}
				catch (final NumberFormatException nfe)
				{}
			}
		}
		ATTACK_SKILL_CHANCE = farmSettings.getProperty("AttackSkillChance", 100, false);
		ATTACK_SKILL_PERCENT = farmSettings.getProperty("AttackSkillPercent", 5, false);
		CHANCE_SKILL_CHANCE = farmSettings.getProperty("ChanceSkillChance", 100, false);
		CHANCE_SKILL_PERCENT = farmSettings.getProperty("ChanceSkillPercent", 5, false);
		SELF_SKILL_CHANCE = farmSettings.getProperty("SelfSkillChance", 100, false);
		SELF_SKILL_PERCENT = farmSettings.getProperty("SelfSkillPercent", 5, false);
		HEAL_SKILL_CHANCE = farmSettings.getProperty("HealSkillChance", 100, false);
		HEAL_SKILL_PERCENT = farmSettings.getProperty("HealSkillPercent", 30, false);
		SUMMON_ATTACK_SKILL_CHANCE = farmSettings.getProperty("SummonAttackSkillChance", 100, false);
		SUMMON_ATTACK_SKILL_PERCENT = farmSettings.getProperty("SummonAttackSkillPercent", 5, false);
		SUMMON_SELF_SKILL_CHANCE = farmSettings.getProperty("SummonSelfSkillChance", 100, false);
		SUMMON_SELF_SKILL_PERCENT = farmSettings.getProperty("SummonSelfSkillPercent", 5, false);
		SUMMON_HEAL_SKILL_CHANCE = farmSettings.getProperty("SummonHealSkillChance", 100, false);
		SUMMON_HEAL_SKILL_PERCENT = farmSettings.getProperty("SummonHealSkillPercent", 30, false);
		SHORTCUT_PAGE = farmSettings.getProperty("ShortCutPage", 10, false);
		SEARCH_DISTANCE = farmSettings.getProperty("SearchDistance", 2000, false);
		FARM_TYPE = farmSettings.getProperty("AutoFarmType", 0, false);
		FARM_INTERVAL_TASK = farmSettings.getProperty("AutoFarmIntervalTask", 500, false);
		SKILLS_EXTRA_DELAY = farmSettings.getProperty("SkillsExtraDelay", 5, false) * 1000L;
		KEEP_LOCATION_DELAY = farmSettings.getProperty("KeepLocationDelay", 5, false) * 1000L;
		RUN_CLOSE_UP_DELAY = farmSettings.getProperty("RunCloseUpDelay", 2, false) * 1000L;
		RUN_CLOSE_UP_DISTANCE = farmSettings.getProperty("RunCloseUpDistance", 100, false);
		ALLOW_FARM_FREE_TIME = farmSettings.getProperty("AllowFarmFreeTime", false, false);
		REFRESH_FARM_TIME = farmSettings.getProperty("AllowRefreshFarmTime", false, false);
		FARM_FREE_TIME = farmSettings.getProperty("FarmFreeTime", 3, false);
		ALLOW_CHECK_HWID_LIMIT = farmSettings.getProperty("AllowCheckHwidLimits", false, false);
		FARM_ACTIVE_LIMITS = farmSettings.getProperty("FarmActiveLimits", 3, false);
		final String[] propertyPrice = farmSettings.getProperty("FarmExpendLimitPrice", "4037,1", false).split(",");
		try
		{
			FARM_EXPEND_LIMIT_PRICE[0] = Integer.parseInt(propertyPrice[0]);
			FARM_EXPEND_LIMIT_PRICE[1] = Integer.parseInt(propertyPrice[1]);
		}
		catch (final NumberFormatException nfe)
		{}
		MAX_SKILLS = farmSettings.getProperty("FarmMaxSkills", 14, false);
		final String[] items = farmSettings.getProperty("RessurectionItemList", "737,3936", false).split(",");
		RESURRECTION_ITEMS = new ArrayList<>(items.length);
		for (final String item : items)
		{
			Integer itm = 0;
			try
			{
				itm = Integer.parseInt(item);
			}
			catch (final NumberFormatException nfe)
			{}
			
			if (itm != 0)
			{
				RESURRECTION_ITEMS.add(itm);
			}
		}
		WAIT_TIME = farmSettings.getProperty("WaitDelayTime", 15, false);
		ALLOW_RESURRECTION = farmSettings.getProperty("AllowRessurection", true, false);
		final String[] regions = farmSettings.getProperty("AutoFarmRegionSearch", "", false).split(";");
		REGIONS_SEARCH = new HashMap<>(regions.length);
		for (final String region : regions)
		{
			final String[] regionSplit = region.split(",");
			if (regionSplit.length == 2)
			{
				try
				{
					REGIONS_SEARCH.put(regionSplit[0], Integer.parseInt(regionSplit[1]));
				}
				catch (final NumberFormatException nfe)
				{
				}
			}
		}
		ALLOW_OFFLINE = farmSettings.getProperty("AllowOfflineFarm", false, false);
		OFFLINE_FARM_MAX_DAYS = farmSettings.getProperty("OfflineFarmMaxDays", 10, false);
		DOUBLE_SESSIONS_CONSIDER_OFFLINE_FARM = farmSettings.getProperty("ConsiderSessionOfflineFarm", false, false);
	}
	
	private static boolean isAllowSystem()
	{
		return new File(Config.DATAPACK_ROOT + "/data/scripts/services/AutoFarm.java").exists();
	}

	public static final FarmSettings getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final FarmSettings _instance = new FarmSettings();
	}
}