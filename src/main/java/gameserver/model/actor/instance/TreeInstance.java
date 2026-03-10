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
package gameserver.model.actor.instance;

import java.util.concurrent.Future;

import gameserver.ThreadPoolManager;
import gameserver.data.parser.SkillsParser;
import gameserver.model.actor.Player;
import gameserver.model.actor.Summon;
import gameserver.model.actor.templates.npc.NpcTemplate;
import gameserver.model.skills.Skill;
import gameserver.model.skills.l2skills.SkillSummon;

public class TreeInstance extends Summon
{
	private Future<?> _healTask;
	private final int _skillDelay;
	private final long _despawn;
	private long _despawnDelay;
	private final boolean _isTargetable;
	private Skill _healSkill = null;

	public TreeInstance(int objectId, NpcTemplate template, Player owner, Skill skill)
	{
		super(objectId, template, owner);
		
		setInstanceType(InstanceType.TreeInstance);
		setShowSummonAnimation(true);
		
		if (skill != null)
		{
			final var summonSkill = (SkillSummon) skill;
			final var healSkill = summonSkill.getHealSkillInfo();
			if (healSkill != null && !healSkill.isEmpty())
			{
				final var info = healSkill.split(";");
				final var sk = SkillsParser.getInstance().getInfo(Integer.parseInt(info[0]), Integer.parseInt(info[1]));
				if (sk != null)
				{
					_healSkill = sk;
				}
			}
		}
		_skillDelay = template.getParams().getInteger("skillDelay", 1) * 1000;
		_despawn = template.getParams().getInteger("despawnDelay", 30) * 1000;
		_isTargetable = template.getParams().getBool("noTargetable", false);
	}
	
	@Override
	public int getSummonType()
	{
		return 1;
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		_despawnDelay = System.currentTimeMillis() + _despawn;
		if (_healSkill != null)
		{
			_healTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> healTask(), 0L, _skillDelay);
		}
	}
	
	private void healTask()
	{
		if (System.currentTimeMillis() >= _despawnDelay)
		{
			unSummon(getOwner());
			return;
		}
		
		if (isSkillDisabled(_healSkill))
		{
			return;
		}
		useMagic(_healSkill, false, false, true);
	}
	
	@Override
	public void unSummon(Player owner)
	{
		stopHealTask();
		super.unSummon(owner);
	}
	
	private void stopHealTask()
	{
		final var task = _healTask;
		if (task != null)
		{
			task.cancel(false);
			_healTask = null;
		}
	}
	
	@Override
	public boolean isImmobilized()
	{
		return true;
	}
	
	@Override
	public boolean isInvul()
	{
		return true;
	}
	
	@Override
	public boolean isServitor()
	{
		return true;
	}
	
	@Override
	public void storeEffect(boolean storeEffects)
	{
	}
	
	@Override
	public void restoreEffects()
	{
	}
	
	@Override
	public int getLevel()
	{
		return (getTemplate() != null ? getTemplate().getLevel() : 0);
	}
	
	@Override
	public boolean isTargetable()
	{
		return _isTargetable;
	}
}