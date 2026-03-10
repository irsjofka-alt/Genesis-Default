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

import gameserver.data.parser.SkillsParser;

public final class SkillUtils
{
	public static int generateSkillHashCode(int id, int level)
	{
		return id * 1000 + level;
	}

	public static int getSkillLevel(int enchantType, int enchantLevel)
	{
		return (100 * enchantType) + enchantLevel;
	}

	public static boolean isEnchantedSkill(int level)
	{
		return getSkillEnchantLevel(level) > 0;
	}

	public static int getSkillEnchantType(int level)
	{
		return level / 100;
	}

	public static int getSkillEnchantLevel(int level)
	{
		if(level > 100)
		{
			return level % 100;
		}
		return 0;
	}

	public static int getSkillLevelMask(int skillLevel, int subSkillLevel)
	{
		return skillLevel | (subSkillLevel << 16);
	}

	public static int getSkillLevelFromMask(int skillLevelMask)
	{
		final int mask = 0b1111111111111111;
		return mask & skillLevelMask;
	}

	public static int getSubSkillLevelFromMask(int skillLevelMask)
	{
		final int mask = 0b1111111111111111;
		return mask & skillLevelMask >>> 16;
	}

	public static int convertHFSkillLevelToGODMask(int id, int level)
	{
		final int enchantLevel = getSkillEnchantLevel(level);
		if(enchantLevel != 0)
		{
			final int baseLevel = SkillsParser.getInstance().getMaxLevel(id);
			final int subLevel = getSkillEnchantType(level) * 1000 + enchantLevel;
			return getSkillLevelMask(baseLevel, subLevel);
		}
		return level;
	}

	public static int convertGODSkillLevelToHF(int id, int levelMask)
	{
		return convertGODSkillLevelToHF(id, getSkillLevelFromMask(levelMask), getSubSkillLevelFromMask(levelMask));
	}

	public static int convertGODSkillLevelToHF(int id, int level, int subLevel)
	{
		if (subLevel == 0 || SkillsParser.getInstance().getMaxLevel(id) != level)
		{
			return level;
		}
		return (subLevel / 1000) * 100 + (subLevel % 1000);
	}
	
	public static int convertHFSkillLevelToGOD(int id, int level)
	{
		if (level > 100)
		{
			return SkillsParser.getInstance().getMaxLevel(id);
		}
		return level;
	}
	
	public static int convertHFSkillLevelToGODSubLevel(int id, int level)
	{
		final int enchantLevel = getSkillEnchantLevel(level);
		if (enchantLevel != 0)
		{
			return getSkillEnchantType(level) * 1000 + enchantLevel;
		}
		return 0;
	}
}
