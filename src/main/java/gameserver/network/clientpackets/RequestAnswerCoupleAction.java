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

import gameserver.model.GameObjectsStorage;
import gameserver.model.actor.Player;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.ExRotation;
import gameserver.network.serverpackets.SocialAction;
import gameserver.network.serverpackets.SystemMessage;
import gameserver.utils.Util;

public class RequestAnswerCoupleAction extends GameClientPacket
{
	private int _charObjId;
	private int _actionId;
	private int _answer;

	@Override
	protected void readImpl()
	{
		_actionId = readD();
		_answer = readD();
		_charObjId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		activeChar.isntAfk();

		final Player target = GameObjectsStorage.getPlayer(_charObjId);
		if (target == null)
		{
			return;
		}

		if ((target.getMultiSocialTarget() != activeChar.getObjectId()) || (target.getMultiSociaAction() != _actionId))
		{
			return;
		}
		if (_answer == 0)
		{
			target.sendPacket(SystemMessageId.COUPLE_ACTION_DENIED);
		}
		else if (_answer == 1)
		{
			final int distance = (int) Math.sqrt(activeChar.getPlanDistanceSq(target));
			if ((distance > 125) || (distance < 15) || (activeChar.getObjectId() == target.getObjectId()))
			{
				sendPacket(SystemMessageId.TARGET_DO_NOT_MEET_LOC_REQUIREMENTS);
				target.sendPacket(SystemMessageId.TARGET_DO_NOT_MEET_LOC_REQUIREMENTS);
				return;
			}
			int heading = Util.calculateHeadingFrom(activeChar, target);
			activeChar.broadcastPacket(new ExRotation(activeChar.getObjectId(), heading));
			activeChar.setHeading(heading);
			heading = Util.calculateHeadingFrom(target, activeChar);
			target.setHeading(heading);
			target.broadcastPacket(new ExRotation(target.getObjectId(), heading));
			activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), _actionId));
			target.broadcastPacket(new SocialAction(_charObjId, _actionId));
		}
		else if (_answer == -1)
		{
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_SET_TO_REFUSE_COUPLE_ACTIONS);
			sm.addPcName(activeChar);
			target.sendPacket(sm);
		}
		target.setMultiSocialAction(0, 0);
	}
}