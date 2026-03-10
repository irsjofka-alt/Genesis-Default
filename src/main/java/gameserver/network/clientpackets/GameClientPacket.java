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

import java.nio.BufferUnderflowException;

import org.nio.impl.ReceivablePacket;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import gameserver.Config;
import gameserver.EternityWorld;
import gameserver.model.actor.Player;
import gameserver.network.GameClient;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.ActionFail;
import gameserver.network.serverpackets.GameServerPacket;
import gameserver.network.serverpackets.SystemMessage;

public abstract class GameClientPacket extends ReceivablePacket<GameClient>
{
	protected static final Logger _log = LogManager.getLogger(GameClientPacket.class);
	
	@Override
	public boolean read()
	{
		if (!getClient().checkFloodProtection(getFloodProtectorType(), getClass().getSimpleName()))
		{
			return false;
		}
		
		try
		{
			readImpl();
			return true;
		}
		catch (final BufferUnderflowException _)
		{
			getClient().onBufferUnderflow();
		}
		catch (final RuntimeException e)
		{
			if (Config.CLIENT_PACKET_HANDLER_DEBUG)
			{
				_log.warn("Client: " + getClient().toString() + " - Failed reading: " + getType() + " - Revision: " + EternityWorld._revision + "", e);
			}
		}
		return false;
	}

	protected abstract void readImpl();

	@Override
	public void run()
	{
		try
		{
			runImpl();

			if (triggersOnActionRequest())
			{
				final Player actor = getClient().getActiveChar();
				if ((actor != null) && (actor.isSpawnProtected() || actor.isInvul()))
				{
					actor.onActionRequest();
					if (Config.DEBUG)
					{
						_log.info("Spawn protection for player " + actor.getName(null) + " removed by packet: " + getType());
					}
				}
			}
		}
		catch (final Throwable t)
		{
			_log.warn("Client: " + getClient().toString() + " - Failed running: " + getType() + " - L2J Eternity-World Server Version: " + EternityWorld._revision + " ; " + t.getMessage(), t);

			if (this instanceof RequestEnterWorld)
			{
				getClient().closeNow(false);
			}
		}
	}

	protected abstract void runImpl();

	protected final void sendPacket(GameServerPacket gsp)
	{
		getClient().sendPacket(gsp);
	}

	public void sendPacket(SystemMessageId id)
	{
		sendPacket(SystemMessage.getSystemMessage(id));
	}

	protected boolean triggersOnActionRequest()
	{
		return true;
	}

	protected final Player getActiveChar()
	{
		return getClient().getActiveChar();
	}

	protected final void sendActionFailed()
	{
		if (getClient() != null)
		{
			getClient().sendPacket(ActionFail.STATIC_PACKET);
		}
	}
	
	protected String getFloodProtectorType()
	{
		return getClass().getSimpleName();
	}

	public String getType()
	{
		return "[C] " + getClass().getSimpleName();
	}
}