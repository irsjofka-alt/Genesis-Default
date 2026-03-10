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
package gameserver.network.clientpackets;

import gameserver.Config;
import gameserver.SevenSignsFestival;
import gameserver.instancemanager.ZoneManager;
import gameserver.model.actor.Player;
import gameserver.model.zone.ZoneId;
import gameserver.model.zone.type.FunPvpZone;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.SystemMessage;
import gameserver.taskmanager.AttackStanceTaskManager;
import gameserver.utils.Log;

public final class SendLogOut extends GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		if ((player.getActiveEnchantItemId() != Player.ID_NONE) || (player.getActiveEnchantAttrItemId() != Player.ID_NONE))
		{
			if (Config.DEBUG)
			{
				_log.info("Player " + player.getName(null) + " tried to logout while enchanting.");
			}
			player.sendActionFailed();
			return;
		}
		
		if (player.isLocked())
		{
			_log.warn("Player " + player.getName(null) + " tried to logout during class change.");
			player.sendActionFailed();
			return;
		}
		
		if (player.isInFightEvent())
		{
			player.sendMessage("Leave Fight Event first!");
			player.sendActionFailed();
			return;
		}

		if (player.isBlocked())
		{
			player.sendActionFailed();
			return;
		}

		if (player.isInsideZone(ZoneId.FUN_PVP))
		{
			final FunPvpZone zone = ZoneManager.getInstance().getZone(player, FunPvpZone.class);
			if (zone != null && zone.isNoLogoutZone())
			{
				player.sendMessage("You cannot logout while inside at this zone.");
				player.sendActionFailed();
				return;
			}
		}
		
		if (player.canOfflineFarmMode())
		{
			player.setInOfflineFarmMode(true);
		}
		else
		{
			if (AttackStanceTaskManager.getInstance().hasAttackStanceTask(player))
			{
				if (player.isGM() && player.getAccessLevel().allowRestartFighting())
				{
					return;
				}
				
				if (Config.DEBUG)
				{
					_log.info("Player " + player.getName(null) + " tried to logout while fighting.");
				}
				player.sendPacket(SystemMessageId.CANT_LOGOUT_WHILE_FIGHTING);
				player.sendActionFailed();
				return;
			}
			
			if (player.isFestivalParticipant())
			{
				if (SevenSignsFestival.getInstance().isFestivalInitialized())
				{
					player.sendMessage("You cannot log out while you are a participant in a Festival.");
					player.sendActionFailed();
					return;
				}
				
				if (player.isInParty())
				{
					player.getParty().broadCast(SystemMessage.sendString(player.getName(null) + " has been removed from the upcoming Festival."));
				}
			}
			
			player.removeFromBossZone();
			Log.addLogGame("DISCONNECT:", "Disconnected!", player.getName(null));
			player.kick();
		}
	}
}