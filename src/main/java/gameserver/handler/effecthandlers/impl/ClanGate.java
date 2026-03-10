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

import gameserver.model.Clan;
import gameserver.model.skills.effects.AbnormalEffect;
import gameserver.model.skills.effects.EffectTemplate;
import gameserver.model.skills.effects.Effect;
import gameserver.model.skills.effects.EffectType;
import gameserver.model.stats.Env;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.SystemMessage;

public class ClanGate extends Effect
{
	public ClanGate(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.CLAN_GATE;
	}
	
	@Override
	public boolean onStart()
	{
		getEffected().startAbnormalEffect(AbnormalEffect.MAGIC_CIRCLE);
		if (getEffected().isPlayer())
		{
			final Clan clan = getEffected().getActingPlayer().getClan();
			if (clan != null)
			{
				final SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.COURT_MAGICIAN_CREATED_PORTAL);
				clan.broadcastToOtherOnlineMembers(msg, getEffected().getActingPlayer());
			}
		}
		return true;
	}

	@Override
	public void onExit()
	{
		getEffected().stopAbnormalEffect(AbnormalEffect.MAGIC_CIRCLE);
	}
}