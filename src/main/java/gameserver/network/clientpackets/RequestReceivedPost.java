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
import gameserver.instancemanager.MailManager;
import gameserver.model.actor.Player;
import gameserver.model.entity.Message;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.ExChangePostState;
import gameserver.network.serverpackets.ExReplyReceivedPost;
import gameserver.utils.Util;

public final class RequestReceivedPost extends GameClientPacket
{
	private int _msgId;
	
	@Override
	protected void readImpl()
	{
		_msgId = readD();
	}

	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if ((activeChar == null) || !Config.ALLOW_MAIL)
		{
			return;
		}
		
		if (activeChar.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}
		
		final Message msg = MailManager.getInstance().getMessage(_msgId);
		if (msg == null)
		{
			return;
		}
		
		if (!activeChar.isInZonePeace() && msg.hasAttachments())
		{
			activeChar.sendPacket(SystemMessageId.CANT_USE_MAIL_OUTSIDE_PEACE_ZONE);
			return;
		}
		
		if (msg.getReceiverId() != activeChar.getObjectId())
		{
			Util.handleIllegalPlayerAction(activeChar, "" + activeChar.getName(null) + " tried to receive not own post!");
			return;
		}
		
		if (msg.isDeletedByReceiver())
		{
			return;
		}
		
		activeChar.sendPacket(new ExReplyReceivedPost(msg));
		activeChar.sendPacket(new ExChangePostState(true, _msgId, Message.READED));
		msg.markAsRead();
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}