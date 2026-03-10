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
package gameserver.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gameserver.Config;
import gameserver.data.parser.SkillsParser;
import gameserver.model.base.AcquireSkillType;
import gameserver.model.base.Race;
import gameserver.model.base.SocialClass;
import gameserver.model.holders.ItemHolder;
import gameserver.model.holders.SkillHolder;
import gameserver.model.stats.StatsSet;

public final class SkillLearn
{
	private final String _skillName;
	private final int _skillId;
	private final int _skillLvl;
	private final int _hashCode;
	private final int _getLevel;
	private final boolean _autoGet;
	private final boolean _blockAutoGet;
	private final int _levelUpSp;
	private final List<ItemHolder> _requiredItems = new ArrayList<>();
	private final List<Race> _races = new ArrayList<>();
	private final List<SkillHolder> _preReqSkills = new ArrayList<>();
	private SocialClass _socialClass;
	private final boolean _residenceSkill;
	private final List<Integer> _residenceIds = new ArrayList<>();
	private final List<SubClassData> _subClassLvlNumber = new ArrayList<>();
	private final boolean _learnedByNpc;
	private final boolean _learnedByFS;

	public class SubClassData
	{
		private final int slot;
		private final int lvl;

		public SubClassData(int pSlot, int pLvl)
		{
			slot = pSlot;
			lvl = pLvl;
		}

		public int getSlot()
		{
			return slot;
		}

		public int getLvl()
		{
			return lvl;
		}
	}

	public SkillLearn(StatsSet set)
	{
		_skillName = set.getString("skillName");
		_skillId = set.getInteger("skillId");
		_skillLvl = set.getInteger("skillLvl");
		_hashCode = SkillsParser.getSkillHashCode(_skillId, _skillLvl);
		_getLevel = set.getInteger("getLevel");
		_blockAutoGet = set.getBool("blockAutoGet", false);
		_autoGet = set.getBool("autoGet", false);
		_levelUpSp = set.getInteger("levelUpSp", 0);
		_residenceSkill = set.getBool("residenceSkill", false);
		_learnedByNpc = set.getBool("learnedByNpc", false);
		_learnedByFS = set.getBool("learnedByFS", false);
	}

	public String getName()
	{
		return _skillName;
	}

	public int getId()
	{
		return _skillId;
	}

	public int getLvl()
	{
		return _skillLvl;
	}

	public int getGetLevel()
	{
		return _getLevel;
	}

	public int getLevelUpSp()
	{
		return _levelUpSp;
	}

	public boolean isAutoGet()
	{
		return _autoGet;
	}

	public List<ItemHolder> getRequiredItems(AcquireSkillType type)
	{
		if (Config.DISABLED_ITEMS_FOR_ACQUIRE_TYPES.contains(type))
		{
			return Collections.emptyList();
		}
		return _requiredItems;
	}

	public void addRequiredItem(ItemHolder item)
	{
		_requiredItems.add(item);
	}

	public List<Race> getRaces()
	{
		return _races;
	}

	public void addRace(Race race)
	{
		_races.add(race);
	}

	public List<SkillHolder> getPreReqSkills()
	{
		return _preReqSkills;
	}

	public void addPreReqSkill(SkillHolder skill)
	{
		_preReqSkills.add(skill);
	}

	public SocialClass getSocialClass()
	{
		return _socialClass;
	}

	public void setSocialClass(SocialClass socialClass)
	{
		if (_socialClass == null)
		{
			_socialClass = socialClass;
		}
	}

	public boolean isResidencialSkill()
	{
		return _residenceSkill;
	}

	public List<Integer> getResidenceIds()
	{
		return _residenceIds;
	}

	public void addResidenceId(Integer id)
	{
		_residenceIds.add(id);
	}

	public List<SubClassData> getSubClassConditions()
	{
		return _subClassLvlNumber;
	}

	public void addSubclassConditions(int slot, int lvl)
	{
		_subClassLvlNumber.add(new SubClassData(slot, lvl));
	}

	public boolean isLearnedByNpc()
	{
		return _learnedByNpc;
	}

	public boolean isLearnedByFS()
	{
		return _learnedByFS;
	}

	public int getCalculatedLevelUpSp()
	{
		return _levelUpSp;
	}
	
	public boolean isBlockAutoGet()
	{
		return _blockAutoGet;
	}
	
	@Override
	public int hashCode()
	{
		return _hashCode;
	}
}