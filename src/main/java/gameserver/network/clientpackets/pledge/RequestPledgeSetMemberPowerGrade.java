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
package gameserver.network.clientpackets.pledge;

import gameserver.model.Clan;
import gameserver.model.ClanMember;
import gameserver.model.actor.Player;
import gameserver.network.SystemMessageId;
import gameserver.network.clientpackets.GameClientPacket;
import gameserver.network.serverpackets.SystemMessage;
import gameserver.network.serverpackets.pledge.PledgeReceivePowerInfo;
import gameserver.network.serverpackets.pledge.PledgeShowMemberListUpdate;

public final class RequestPledgeSetMemberPowerGrade extends GameClientPacket
{
	private String _member;
	private int _powerGrade;
	
	@Override
	protected void readImpl()
	{
		_member = readS();
		_powerGrade = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		final Clan clan = activeChar.getClan();
		if (clan == null)
		{
			return;
		}

		if ((activeChar.getClanPrivileges() & Clan.CP_CL_MANAGE_RANKS) != Clan.CP_CL_MANAGE_RANKS)
		{
			return;
		}

		final ClanMember member = clan.getClanMember(_member);
		if (member == null)
		{
			return;
		}

		if (member.getObjectId() == clan.getLeaderId())
		{
			return;
		}

		if (member.getPledgeType() == Clan.SUBUNIT_ACADEMY)
		{
			activeChar.sendMessage("You cannot change academy member grade");
			return;
		}

		member.setPowerGrade(_powerGrade);
		clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(member));
		clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_C1_PRIVILEGE_CHANGED_TO_S2).addString(member.getName()).addInt(_powerGrade));
		final var player = member.getPlayerInstance();
		if (player != null)
		{
			player.sendUserInfo();
			player.sendPacket(new PledgeReceivePowerInfo(member));
		}
	}
}