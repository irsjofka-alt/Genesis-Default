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
package fake.ai;

import java.util.List;

import gameserver.Config;
import fake.FakePlayer;
import fake.ai.addon.IConsumableSpender;
import fake.model.HealingSpell;
import fake.model.OffensiveSpell;
import fake.model.SupportSpell;
import gameserver.data.parser.FakeSkillsParser;

public class PhantomRangerAI extends CombatAI implements IConsumableSpender
{
	public PhantomRangerAI(FakePlayer character)
	{
		super(character);
	}

	@Override
	public void thinkAndAct()
	{
		super.thinkAndAct();
		setBusyThinking(true);
		handleShots();
		selfSupportBuffs();
		handleConsumable(_fakePlayer, getArrowId());
		tryAction(false);
		setBusyThinking(false);
	}

	@Override
	protected int changeOfUsingSkill()
	{
		return FakeSkillsParser.getInstance().getSkillsChance(_fakePlayer.getClassId());
	}

	@Override
	protected List<OffensiveSpell> getOffensiveSpells()
	{
		return FakeSkillsParser.getInstance().getOffensiveSkills(_fakePlayer.getClassId());
	}
	
	@Override
	protected List<HealingSpell> getHealingSpells()
	{
		return FakeSkillsParser.getInstance().getHealSkills(_fakePlayer.getClassId());
	}
	
	@Override
	protected List<SupportSpell> getSelfSupportSpells()
	{
		return FakeSkillsParser.getInstance().getSupportSkills(_fakePlayer.getClassId());
	}
	
	@Override
	protected int[][] getBuffs()
	{
		return Config.FAKE_FIGHTER_BUFFS;
	}
}