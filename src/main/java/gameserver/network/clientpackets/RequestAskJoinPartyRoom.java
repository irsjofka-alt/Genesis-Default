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

import gameserver.instancemanager.ZoneManager;
import gameserver.model.GameObjectsStorage;
import gameserver.model.actor.Player;
import gameserver.model.matching.MatchingRoom;
import gameserver.model.zone.type.FunPvpZone;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.ExAskJoinPartyRoom;
import gameserver.network.serverpackets.SystemMessage;

public class RequestAskJoinPartyRoom extends GameClientPacket
{
	private static String _name;

	@Override
	protected void readImpl()
	{
		_name = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getActiveChar();
		if (player == null)
		{
			return;
		}
		
		player.isntAfk();

		final Player target = GameObjectsStorage.getPlayer(_name);
		if (target != null)
		{
			if (player.isInFightEvent() && !player.getFightEvent().canJoinParty(player, target))
			{
				player.sendMessage("You cannot do that on Fight Club!");
				return;
			}

			if (player.isInPvpFunZone())
			{
				final FunPvpZone zone = ZoneManager.getInstance().getZone(player, FunPvpZone.class);
				if (zone != null && !zone.canJoinParty(player, target))
				{
					player.sendMessage("You cannot do that in PvP zone!");
					return;
				}
			}
			
			if (!target.isProcessingRequest())
			{
				if (target.getMatchingRoom() != null)
				{
					return;
				}

				final MatchingRoom room = player.getMatchingRoom();
				if (room == null || room.getType() != MatchingRoom.PARTY_MATCHING)
				{
					return;
				}

				if (room.getPlayers().size() >= room.getMaxMembersSize())
				{
					player.sendPacket(SystemMessageId.PARTY_ROOM_FULL);
					return;
				}
				
				player.onTransactionRequest(target);
				target.sendPacket(new ExAskJoinPartyRoom(player.getName(null), room.getTopic()));
				
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_INVITED_YOU_TO_PARTY_ROOM).addPcName(player));
				target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_INVITED_YOU_TO_PARTY_ROOM).addPcName(player));
			}
			else
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_BUSY_TRY_LATER).addPcName(target));
			}
		}
		else
		{
			player.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
		}
	}
}