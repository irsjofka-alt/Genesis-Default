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

import gameserver.Config;
import gameserver.model.actor.Player;
import gameserver.model.skills.effects.Effect;
import gameserver.model.skills.effects.EffectTemplate;
import gameserver.model.skills.effects.EffectType;
import gameserver.model.stats.Env;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.ExPCCafePointInfo;
import gameserver.network.serverpackets.SystemMessage;

public final class PcBangPointUp extends Effect
{
	private final int _amount;
	
	public PcBangPointUp(Env env, EffectTemplate template)
	{
		super(env, template);
		
		_amount = template.getParameters().getInteger("amount", 0);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.NONE;
	}
	
	@Override
	public boolean onStart()
	{
		final Player player = getEffected().isPlayer() ? (Player) getEffected() : null;
		if (player == null)
		{
			return false;
		}
		
		int points = _amount;
		if ((player.getPcBangPoints() + points) > Config.MAX_PC_BANG_POINTS)
		{
			points = Config.MAX_PC_BANG_POINTS - player.getPcBangPoints();
		}
		
		if (Config.PC_POINT_ID < 0)
		{
			player.setPcBangPoints(player.getPcBangPoints() + points);
		}
		else
		{
			player.setPcBangPoints(player.getPcBangPoints() + points);
			player.addItem("PcPoints", Config.PC_POINT_ID, points, player, true);
		}
		final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_ACQUIRED_S1_PC_CAFE_POINTS);
		sm.addNumber(points);
		player.sendPacket(sm);
		player.sendPacket(new ExPCCafePointInfo(player.getPcBangPoints(), points, true, false, 1));
		return true;
	}
}
