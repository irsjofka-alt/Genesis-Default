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

import gameserver.ai.model.CtrlEvent;
import gameserver.model.actor.instance.MonsterInstance;
import gameserver.model.skills.effects.Effect;
import gameserver.model.skills.effects.EffectTemplate;
import gameserver.model.skills.effects.EffectType;
import gameserver.model.stats.Env;
import gameserver.model.stats.Formulas;
import gameserver.network.SystemMessageId;

public class Spoil extends Effect
{
	public Spoil(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public EffectType getEffectType()
	{
		return EffectType.SPOIL;
	}
	
	@Override
	public boolean onStart()
	{
		if (!getEffected().isMonster() || getEffected().isDead())
		{
			getEffector().sendPacket(SystemMessageId.INCORRECT_TARGET);
			return false;
		}

		final var target = (MonsterInstance) getEffected();
		if (target.isSpoiled())
		{
			getEffector().sendPacket(SystemMessageId.ALREADY_SPOILED);
			return false;
		}

		if (Formulas.calcMagicSuccess(getEffector(), target, getSkill(), false))
		{
			target.setSpoilerObjectId(getEffector().getObjectId());
			getEffector().sendPacket(SystemMessageId.SPOIL_SUCCESS);
		}
		target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, getEffector(), 0);
		return true;
	}
}