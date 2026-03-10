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
import gameserver.model.GameObjectsStorage;
import gameserver.model.actor.Player;
import gameserver.network.SystemMessageId;
import gameserver.network.clientpackets.GameClientPacket;
import gameserver.network.serverpackets.AskJoinPledge;
import gameserver.network.serverpackets.SystemMessage;

public final class RequestJoinPledge extends GameClientPacket
{
	private int _target;
	private int _pledgeType;
	
	@Override
	protected void readImpl()
	{
		_target = readD();
		_pledgeType = readD();
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

		final Player target = GameObjectsStorage.getPlayer(_target);
		if (target == null)
		{
			activeChar.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET);
			return;
		}
		
		if (activeChar.isInFightEvent() && !activeChar.getFightEvent().canReceiveInvitations(activeChar, target))
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_BUSY_TRY_LATER).addString(target.getName(null)));
			return;
		}

		if (!clan.checkClanJoinCondition(activeChar, target, _pledgeType))
		{
			return;
		}

		if (!activeChar.getRequest().setRequest(target, this))
		{
			return;
		}

		final String pledgeName = activeChar.getClan().getName();
		final String subPledgeName = (activeChar.getClan().getSubPledge(_pledgeType) != null ? activeChar.getClan().getSubPledge(_pledgeType).getName() : null);
		target.sendPacket(new AskJoinPledge(activeChar.getObjectId(), subPledgeName, _pledgeType, pledgeName));
	}

	public int getPledgeType()
	{
		return _pledgeType;
	}
}