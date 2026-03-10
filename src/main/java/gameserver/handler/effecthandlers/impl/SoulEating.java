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

import l2e.commons.listener.Listener;
import gameserver.listener.player.OnExperienceReceivedListener;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.skills.effects.Effect;
import gameserver.model.skills.effects.EffectTemplate;
import gameserver.model.skills.effects.EffectType;
import gameserver.model.stats.Env;
import gameserver.model.stats.Stats;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.ExSpawnEmitter;

public final class SoulEating extends Effect implements OnExperienceReceivedListener
{
	private final int _expNeeded;

	public SoulEating(Env env, EffectTemplate template)
	{
		super(env, template);
		_expNeeded = template.getParameters().getInteger("expNeeded");
	}

	public SoulEating(Env env, Effect effect)
	{
		super(env, effect);
		_expNeeded = effect.getEffectTemplate().getParameters().getInteger("expNeeded");
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.NONE;
	}

	@Override
	public void onExperienceReceived(Player player, long exp)
	{
		if ((player != null) && (exp >= _expNeeded))
		{
			final int maxSouls = (int) player.calcStat(Stats.MAX_SOULS, 0, null, null);
			if (player.getChargedSouls() >= maxSouls)
			{
				player.sendPacket(SystemMessageId.SOUL_CANNOT_BE_ABSORBED_ANYMORE);
				return;
			}
			
			player.increaseSouls(1);

			final var target = player.getTarget();
			if ((target != null) && target.isNpc())
			{
				player.broadcastPacket(500, new ExSpawnEmitter(player, (Npc) target));
			}
		}
	}

	@Override
	public void onExit()
	{
		if (getEffected().isPlayer())
		{
			getEffected().getListeners().remove(this);
		}
		super.onExit();
	}

	@Override
	public boolean onStart()
	{
		final var effected = getEffected();
		if (effected.isPlayer())
		{
			Listener<Creature> old = null;
			for (final var listener : effected.getListeners().getListeners())
			{
				if (OnExperienceReceivedListener.class.isInstance(listener))
				{
					old = listener;
					break;
				}
			}
			
			if (old != null)
			{
				effected.getListeners().remove(old);
			}
			effected.getListeners().add(this);
		}
		return super.onStart();
	}
}