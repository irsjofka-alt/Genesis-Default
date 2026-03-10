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

import gameserver.SevenSignsFestival;
import gameserver.instancemanager.MapRegionManager;
import gameserver.instancemanager.ReflectionManager;
import gameserver.model.Location;
import gameserver.model.TeleportWhereType;
import gameserver.model.holders.SummonRequestHolder;
import gameserver.model.skills.effects.Effect;
import gameserver.model.skills.effects.EffectTemplate;
import gameserver.model.skills.effects.EffectType;
import gameserver.model.stats.Env;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.ConfirmDlg;

public class Escape extends Effect
{
	private final TeleportWhereType _escapeType;

	public Escape(Env env, EffectTemplate template)
	{
		super(env, template);
		
		_escapeType = template.getParameters().getEnum("escapeType", TeleportWhereType.class, null);
	}

	@Override
	public EffectType getEffectType()
	{
		return EffectType.TELEPORT;
	}
	
	@Override
	public boolean onStart()
	{
		final var player = getEffected().getActingPlayer();
		if (player == null)
		{
			return false;
		}
		
		if ((_escapeType == null && getSkill().getId() != 2588) || (getSkill().getId() == 2588 && player.getBookmarkLocation() == null))
		{
			return false;
		}
		
		if (getSkill().getId() == 1255 && player.getBlockPartyRecall())
		{
			player.addScript(new SummonRequestHolder(getEffector().getActingPlayer(), getSkill(), false));
			final ConfirmDlg confirm = new ConfirmDlg(SystemMessageId.C1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT.getId());
			confirm.addCharName(getEffector());
			final Location loc = MapRegionManager.getInstance().getTeleToLocation(player, _escapeType);
			confirm.addZoneName(loc.getX(), loc.getY(), loc.getZ());
			confirm.addTime(30000);
			confirm.addRequesterId(getEffector().getObjectId());
			player.sendPacket(confirm);
		}
		else
		{
			if (player.isInKrateisCube())
			{
				player.getArena().removePlayer(player);
			}
			
			if (player.isFestivalParticipant())
			{
				SevenSignsFestival.getInstance().removeParticipant(player);
			}
			
			player.setIsIn7sDungeon(false);
			
			if (getSkill().getId() == 2588)
			{
				player.teleToLocation(player.getBookmarkLocation(), false, ReflectionManager.DEFAULT);
				player.setBookmarkLocation(null);
			}
			else
			{
				player.teleToLocation(MapRegionManager.getInstance().getTeleToLocation(player, _escapeType), true, ReflectionManager.DEFAULT);
			}
		}
		return true;
	}
}