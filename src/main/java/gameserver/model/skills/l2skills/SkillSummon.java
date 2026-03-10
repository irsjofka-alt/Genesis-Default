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
package gameserver.model.skills.l2skills;

import gameserver.Config;
import gameserver.data.parser.ExperienceParser;
import gameserver.data.parser.NpcsParser;
import gameserver.idfactory.IdFactory;
import gameserver.model.GameObject;
import gameserver.model.actor.Creature;
import gameserver.model.actor.instance.ServitorInstance;
import gameserver.model.actor.instance.SiegeSummonInstance;
import gameserver.model.actor.instance.TreeInstance;
import gameserver.model.actor.templates.npc.NpcTemplate;
import gameserver.model.skills.Skill;
import gameserver.model.stats.StatsSet;

public class SkillSummon extends Skill
{
	private final float _expPenalty;
	
	private final int _summonTotalLifeTime;
	private final int _summonTimeLostIdle;
	private final int _summonTimeLostActive;

	private final int _itemConsumeTime;
	private final int _itemConsumeOT;
	private final int _itemConsumeIdOT;
	private final int _itemConsumeSteps;
	private final boolean _inheritElementals;
	private final double _elementalSharePercent;
	private final String _healSkillInfo;
	
	public SkillSummon(StatsSet set)
	{
		super(set);
		
		_expPenalty = set.getFloat("expPenalty", 0.f);
		
		_summonTotalLifeTime = set.getInteger("summonTotalLifeTime", 1200000);
		_summonTimeLostIdle = set.getInteger("summonTimeLostIdle", 0);
		_summonTimeLostActive = set.getInteger("summonTimeLostActive", 0);
		
		_itemConsumeOT = set.getInteger("itemConsumeCountOT", 0);
		_itemConsumeIdOT = set.getInteger("itemConsumeIdOT", 0);
		_itemConsumeTime = set.getInteger("itemConsumeTime", 0);
		_itemConsumeSteps = set.getInteger("itemConsumeSteps", 0);
		
		_inheritElementals = set.getBool("inheritElementals", false);
		_elementalSharePercent = set.getDouble("inheritPercent", 1);
		_healSkillInfo = set.getString("healSkill", null);
	}
	
	@Override
	public void useSkill(Creature caster, GameObject[] targets, double cubicPower)
	{
		if ((caster == null) || caster.isAlikeDead() || !caster.isPlayer())
		{
			return;
		}
		
		final var activeChar = caster.getActingPlayer();
		if (getNpcId() <= 0)
		{
			activeChar.sendMessage("Summon skill " + getId() + " not implemented yet.");
			return;
		}
		
		final var summonTemplate = NpcsParser.getInstance().getTemplate(getNpcId());
		if (summonTemplate == null)
		{
			_log.warn("Summon attempt for nonexisting NPC ID:" + getNpcId() + ", skill ID:" + getId());
			return;
		}
		
		var summon = activeChar.getSummon();
		if (summon != null)
		{
			summon.unSummonForce(activeChar);
		}
		
		final int id = IdFactory.getInstance().getNextId();
		if (summonTemplate.isType("SiegeSummon"))
		{
			summon = new SiegeSummonInstance(id, summonTemplate, activeChar, this);
		}
		else if (summonTemplate.isType("Tree"))
		{
			summon = new TreeInstance(id, summonTemplate, activeChar, this);
		}
		else
		{
			summon = new ServitorInstance(id, summonTemplate, activeChar, this);
		}
		
		for (final String lang : Config.MULTILANG_ALLOWED)
		{
			if (lang != null)
			{
				summon.setName(lang, summonTemplate.getName(lang) != null ? summonTemplate.getName(lang) : summonTemplate.getName(null));
			}
		}
		summon.setGlobalTitle(activeChar.getName(null));
		summon.setExpPenalty(_expPenalty);
		summon.setSharedElementals(_inheritElementals);
		summon.setSharedElementalsValue(_elementalSharePercent);
		summon.setPhysAttributteMod(summonTemplate.getPhysAttributteMod());
		summon.setMagicAttributteMod(summonTemplate.getMagicAttributteMod());
		if (summon.getLevel() >= ExperienceParser.getInstance().getMaxPetLevel())
		{
			summon.getStat().setExp(ExperienceParser.getInstance().getExpForLevel(ExperienceParser.getInstance().getMaxPetLevel() - 1));
			_log.warn("Summon (" + summon.getName(null) + ") NpcID: " + summon.getId() + " has a level above " + ExperienceParser.getInstance().getMaxPetLevel() + ". Please rectify.");
		}
		else
		{
			summon.getStat().setExp(ExperienceParser.getInstance().getExpForLevel(summon.getLevel() % ExperienceParser.getInstance().getMaxPetLevel()));
		}

		summon.setCurrentHp(summon.getMaxHp());
		summon.setCurrentMp(summon.getMaxMp());
		summon.setHeading(activeChar.getHeading());
		summon.setRunning();
		activeChar.setPet(summon);
		summon.spawnMe();
		summon.setSummonSkill(this);
	}
	
	public final int getTotalLifeTime()
	{
		return _summonTotalLifeTime;
	}
	
	public final int getTimeLostIdle()
	{
		return _summonTimeLostIdle;
	}
	
	public final int getTimeLostActive()
	{
		return _summonTimeLostActive;
	}
	
	public final int getItemConsumeOT()
	{
		return _itemConsumeOT;
	}
	
	public final int getItemConsumeIdOT()
	{
		return _itemConsumeIdOT;
	}
	
	public final int getItemConsumeSteps()
	{
		return _itemConsumeSteps;
	}
	
	public final int getItemConsumeTime()
	{
		return _itemConsumeTime;
	}
	
	public final float getExpPenalty()
	{
		return _expPenalty;
	}
	
	public final NpcTemplate getSummonTemplate()
	{
		final var summonTemplate = NpcsParser.getInstance().getTemplate(getNpcId());
		if (summonTemplate == null)
		{
			_log.warn("Summon attempt for nonexisting NPC ID:" + getNpcId() + ", skill ID:" + getId());
		}
		return summonTemplate;
	}
	
	public final boolean getInheritElementals()
	{
		return _inheritElementals;
	}
	
	public final double getElementalSharePercent()
	{
		return _elementalSharePercent;
	}
	
	public final String getHealSkillInfo()
	{
		return _healSkillInfo;
	}
}