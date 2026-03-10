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
package gameserver.utils;

import java.util.Map;

import l2e.commons.util.Function;
import gameserver.data.holder.CharNameHolder;
import gameserver.instancemanager.MailManager;
import gameserver.model.GameObjectsStorage;
import gameserver.model.actor.Player;
import gameserver.model.entity.Message;
import gameserver.model.items.itemcontainer.Mail;
import gameserver.network.SystemMessageId;

public class Functions extends Function
{
	public static void sendSystemMail(Player receiver, String title, String body, Map<Integer, Long> items)
	{
		if (receiver == null || !receiver.isOnline())
		{
			return;
		}
		if (title == null)
		{
			return;
		}
		if (items.keySet().size() > 8)
		{
			return;
		}
		
		final Message msg = new Message(receiver.getObjectId(), title, body, Message.SenderType.NEWS_INFORMER);
		if (items != null && !items.isEmpty())
		{
			final Mail attachments = msg.createAttachments();
			for (final Map.Entry<Integer, Long> itm : items.entrySet())
			{
				attachments.addItem("reward", itm.getKey(), itm.getValue(), null, null);
			}
		}
		MailManager.getInstance().sendMessage(msg);
		receiver.sendPacket(SystemMessageId.MAIL_ARRIVED);
	}
	
	public static boolean sendSystemMail(String receiverName, String title, String body, Map<Integer, Long> items)
	{
		final Player receiver = GameObjectsStorage.getPlayer(receiverName);
		final int receiverObjectId = receiver != null ? receiver.getObjectId() : CharNameHolder.getInstance().getIdByName(receiverName);
		return receiverObjectId > 0 && sendSystemMail(receiverName, receiverObjectId, title, body, items);
	}
	
	public static boolean sendSystemMail(int receiverObjectId, String title, String body, Map<Integer, Long> items)
	{
		final String receiverName = CharNameHolder.getInstance().getNameById(receiverObjectId);
		return !receiverName.equals("") && sendSystemMail(receiverName, receiverObjectId, title, body, items);
	}
	
	public static boolean sendSystemMail(String receiverName, int receiverObjectId, String title, String body, Map<Integer, Long> items)
	{
		if (title == null || receiverObjectId <= 0)
		{
			return false;
		}
		
		if (items.keySet().size() > 8)
		{
			return false;
		}
		
		final Message msg = new Message(receiverObjectId, title, body, Message.SenderType.NEWS_INFORMER);
		if (items != null && !items.isEmpty())
		{
			final Mail attachments = msg.createAttachments();
			for (final Map.Entry<Integer, Long> itm : items.entrySet())
			{
				attachments.addItem("reward", itm.getKey(), itm.getValue(), null, null);
			}
		}
		MailManager.getInstance().sendMessage(msg);
		final Player receiver = GameObjectsStorage.getPlayer(receiverName);
		if (receiver != null)
		{
			receiver.sendPacket(SystemMessageId.MAIL_ARRIVED);
		}
		return true;
	}
}