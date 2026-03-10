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
package gameserver.handler.effecthandlers.impl;

import gameserver.model.actor.Creature;
import gameserver.model.skills.effects.Effect;
import gameserver.model.skills.effects.EffectTemplate;
import gameserver.model.skills.effects.EffectType;
import gameserver.model.stats.Env;
import gameserver.model.stats.Stats;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.StatusUpdate;
import gameserver.network.serverpackets.SystemMessage;

public class ManaHealByLevel extends Effect
{
	public ManaHealByLevel(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.MANAHEAL_BY_LEVEL;
	}
	
	@Override
	public boolean onStart()
	{
		final Creature target = getEffected();
		if (target == null)
		{
			return false;
		}
		
		if (target.isHealBlocked() || target.isDead() || target.isInvul())
		{
			return false;
		}
		
		double amount = calc();
		
		amount = target.calcStat(Stats.MANA_CHARGE, amount, null, null);
		if (target.getLevel() > getSkill().getMagicLevel())
		{
			final int lvlDiff = target.getLevel() - getSkill().getMagicLevel();

			if (lvlDiff == 6)
			{
				amount *= 0.9;
			}
			else if (lvlDiff == 7)
			{
				amount *= 0.8;
			}
			else if (lvlDiff == 8)
			{
				amount *= 0.7;
			}
			else if (lvlDiff == 9)
			{
				amount *= 0.6;
			}
			else if (lvlDiff == 10)
			{
				amount *= 0.5;
			}
			else if (lvlDiff == 11)
			{
				amount *= 0.4;
			}
			else if (lvlDiff == 12)
			{
				amount *= 0.3;
			}
			else if (lvlDiff == 13)
			{
				amount *= 0.2;
			}
			else if (lvlDiff == 14)
			{
				amount *= 0.1;
			}
			else if (lvlDiff >= 15)
			{
				amount = 0;
			}
		}
		amount = Math.max(Math.min(amount, target.getMaxRecoverableMp() - target.getCurrentMp()), 0);
		if (amount != 0)
		{
			target.setCurrentMp(amount + target.getCurrentMp());
			final var su = target.makeStatusUpdate(StatusUpdate.CUR_MP);
			target.sendPacket(su);
		}
		
		SystemMessage sm;
		if (getEffector().getObjectId() != target.getObjectId())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.S2_MP_RESTORED_BY_C1);
			sm.addCharName(getEffector());
		}
		else
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_MP_RESTORED);
		}
		sm.addNumber((int) amount);
		target.sendPacket(sm);
		
		return true;
	}
}