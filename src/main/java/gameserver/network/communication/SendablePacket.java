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
package gameserver.network.communication;

import java.nio.ByteBuffer;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public abstract class SendablePacket extends org.nio.SendablePacket<AuthServerCommunication>
{
	public static final Logger _log = LogManager.getLogger(SendablePacket.class);
	
	@Override
	public AuthServerCommunication getClient()
	{
		return AuthServerCommunication.getInstance();
	}
	
	@Override
	protected ByteBuffer getByteBuffer()
	{
		return getClient().getWriteBuffer();
	}
	
	@Override
	public boolean write()
	{
		try
		{
			writeImpl();
		}
		catch (final Exception e)
		{
			_log.warn("", e);
		}
		return true;
	}
	
	protected abstract void writeImpl();
}