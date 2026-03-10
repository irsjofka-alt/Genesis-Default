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

import gameserver.model.actor.Creature;
import gameserver.model.actor.Npc;
import gameserver.model.actor.templates.npc.NpcTemplate;
import gameserver.model.entity.underground_coliseum.UCTeam;
import gameserver.model.skills.Skill;

public class UCTowerInstance extends Npc
{
	private UCTeam _team;
	
	public UCTowerInstance(UCTeam team, int objectId, NpcTemplate template)
	{
		super(objectId, template);
		
		_team = team;
	}
	
	@Override
	public boolean canBeAttacked()
	{
		return true;
	}
	
	@Override
	public boolean isAutoAttackable(Creature creature, boolean isPoleAttack)
	{
		return true;
	}
	
	@Override
	public void reduceCurrentHp(double damage, Creature attacker, boolean awake, boolean isDOT, Skill skill)
	{
		if (attacker.getTeam() == getTeam())
		{
			return;
		}
		
		if (damage < getStatus().getCurrentHp())
		{
			getStatus().setCurrentHp(getStatus().getCurrentHp() - damage);
		}
		else
		{
			doDie(attacker);
		}
	}
	
	@Override
	protected void onDeath(Creature killer)
	{
		if (_team != null)
		{
			_team.deleteTower();
			_team = null;
		}
		super.onDeath(killer);
	}
	
	@Override
	public int getTeam()
	{
		return _team.getIndex() + 1;
	}
}