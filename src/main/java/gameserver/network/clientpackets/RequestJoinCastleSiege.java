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

import gameserver.instancemanager.CHSiegeManager;
import gameserver.instancemanager.CastleManager;
import gameserver.instancemanager.FortManager;
import gameserver.instancemanager.TerritoryWarManager;
import gameserver.model.Clan;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.CastleSiegeInfo;
import gameserver.network.serverpackets.SystemMessage;

public final class RequestJoinCastleSiege extends GameClientPacket
{
	private int _castleId;
	private int _isAttacker;
	private int _isJoining;
	
	@Override
	protected void readImpl()
	{
		_castleId = readD();
		_isAttacker = readD();
		_isJoining = readD();
	}

	@Override
	protected void runImpl()
	{
		final var activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		if ((activeChar.getClanPrivileges() & Clan.CP_CS_MANAGE_SIEGE) != Clan.CP_CS_MANAGE_SIEGE)
		{
			activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}

		final var clan = activeChar.getClan();
		if (clan == null)
		{
			return;
		}

		final var castle = CastleManager.getInstance().getCastleById(_castleId);
		if (castle != null)
		{
			if (_isJoining == 1)
			{
				if (System.currentTimeMillis() < clan.getDissolvingExpiryTime())
				{
					activeChar.sendPacket(SystemMessageId.CANT_PARTICIPATE_IN_SIEGE_WHILE_DISSOLUTION_IN_PROGRESS);
					return;
				}
				if (_isAttacker == 1)
				{
					castle.getSiege().registerAttacker(activeChar);
				}
				else
				{
					castle.getSiege().registerDefender(activeChar);
				}
			}
			else
			{
				castle.getSiege().removeSiegeClan(activeChar);
			}
			castle.getSiege().listRegisterClan(activeChar);
		}
		
		final var fort = FortManager.getInstance().getFort(_castleId);
		if (fort != null)
		{
			if (_isJoining == 1)
			{
				if (System.currentTimeMillis() < clan.getDissolvingExpiryTime())
				{
					activeChar.sendPacket(SystemMessageId.CANT_PARTICIPATE_IN_SIEGE_WHILE_DISSOLUTION_IN_PROGRESS);
					return;
				}
				
				if ((System.currentTimeMillis() < TerritoryWarManager.getInstance().getTWStartTimeInMillis()) && TerritoryWarManager.getInstance().getIsRegistrationOver())
				{
					activeChar.sendPacket(SystemMessageId.NOT_SIEGE_REGISTRATION_TIME2);
					return;
				}
				else if ((System.currentTimeMillis() > TerritoryWarManager.getInstance().getTWStartTimeInMillis()) && TerritoryWarManager.getInstance().isTWChannelOpen())
				{
					activeChar.sendPacket(SystemMessageId.NOT_SIEGE_REGISTRATION_TIME2);
					return;
				}
				
				if (fort.getSiege().registerAttacker(activeChar, false))
				{
					final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.REGISTERED_TO_S1_FORTRESS_BATTLE);
					sm.addString(fort.getName());
					activeChar.sendPacket(sm);
				}
			}
			else
			{
				fort.getSiege().removeSiegeClan(activeChar.getClan());
			}
			activeChar.sendPacket(new CastleSiegeInfo(fort));
		}

		final var hall = CHSiegeManager.getInstance().getSiegableHall(_castleId);
		if (hall != null)
		{
			if (_isJoining == 1)
			{
				if (System.currentTimeMillis() < clan.getDissolvingExpiryTime())
				{
					activeChar.sendPacket(SystemMessageId.CANT_PARTICIPATE_IN_SIEGE_WHILE_DISSOLUTION_IN_PROGRESS);
					return;
				}
				CHSiegeManager.getInstance().registerClan(clan, hall, activeChar);
			}
			else
			{
				CHSiegeManager.getInstance().unRegisterClan(clan, hall);
			}
			activeChar.sendPacket(new CastleSiegeInfo(hall));
		}
	}
}