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

import gameserver.ai.character.CharacterAI;
import gameserver.ai.npc.Minions;
import gameserver.model.actor.Creature;
import gameserver.model.actor.templates.npc.NpcTemplate;

public class ControllableMobInstance extends MonsterInstance
{
	private boolean _isInvul;
	
	@Override
	public boolean isAggressive()
	{
		return true;
	}
	
	@Override
	public int getAggroRange()
	{
		return 500;
	}

	public ControllableMobInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.ControllableMobInstance);
		_ai = new Minions(this);
	}

	@Override
	public CharacterAI getAI()
	{
		return _ai;
	}
	
	@Override
	public boolean isInvul()
	{
		return _isInvul;
	}
	
	public void setInvul(boolean isInvul)
	{
		_isInvul = isInvul;
	}
	
	@Override
	protected void onDeath(Creature killer)
	{
		setAI(null);
		super.onDeath(killer);
	}
}