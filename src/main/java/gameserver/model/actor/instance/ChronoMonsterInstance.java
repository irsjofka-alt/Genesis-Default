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

import static gameserver.ai.model.CtrlIntention.ACTIVE;
import static gameserver.ai.model.CtrlIntention.ATTACK;

import org.apache.commons.lang3.ArrayUtils;

import gameserver.ai.DefaultAI;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Player;
import gameserver.model.actor.templates.npc.NpcTemplate;
import gameserver.model.skills.Skill;

public class ChronoMonsterInstance extends MonsterInstance
{
	private Player _owner;
	private int _lvlUp;
	
	private static final int[] CHRONO_LIST =
	{
	        4202, 5133, 5817, 7058, 8350
	};
	
	public ChronoMonsterInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);

		setInstanceType(InstanceType.ChronoMonsterInstance);
		setAI(new L2ChronoAI(this));
		_lvlUp = 0;
	}
	
	@Override
	public void reduceCurrentHp(double damage, Creature attacker, boolean awake, boolean isDOT, Skill skill)
	{
		damage = Math.min(getCronoDamageLimit(attacker), damage);
		super.reduceCurrentHp(damage, attacker, awake, isDOT, skill);
	}
	
	@Override
	public int getCronoDamageLimit(Creature attacker)
	{
		var damage = 0;
		if (attacker == null)
		{
			return damage;
		}
		
		final var weapon = attacker.getActiveWeaponItem();
		if (weapon != null && ArrayUtils.contains(CHRONO_LIST, weapon.getId()))
		{
			damage = _lvlUp > 0 ? 2 : 1;
		}
		return damage;
	}

	public final Player getOwner()
	{
		return _owner;
	}

	public void setOwner(Player newOwner)
	{
		_owner = newOwner;
	}

	public void setLevelUp(int lvl)
	{
		_lvlUp = lvl;
	}
	
	public int getLevelUp()
	{
		return _lvlUp;
	}

	class L2ChronoAI extends DefaultAI
	{
		public L2ChronoAI(Attackable accessor)
		{
			super(accessor);
		}

		@Override
		protected void onEvtThink()
		{
			if (_actor.isAllSkillsDisabled())
			{
				return;
			}

			if (getIntention() == ATTACK)
			{
				setIntention(ACTIVE);
			}
		}
	}
	
	@Override
	public boolean isMonster()
	{
		return false;
	}
	
	@Override
	public boolean isCrono()
	{
		return true;
	}
}