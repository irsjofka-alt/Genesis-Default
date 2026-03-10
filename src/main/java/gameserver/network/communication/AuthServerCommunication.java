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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import l2e.commons.net.IPSettings;
import gameserver.ThreadPoolManager;
import gameserver.network.GameClient;
import gameserver.network.communication.gameserverpackets.AuthRequest;

public class AuthServerCommunication extends Thread
{
	private static final Logger _log = LogManager.getLogger(AuthServerCommunication.class);
	
	private static final AuthServerCommunication instance = new AuthServerCommunication();
	
	public static final AuthServerCommunication getInstance()
	{
		return instance;
	}
	
	private final Map<String, GameClient> _waitingClients = new ConcurrentHashMap<>();
	private final Map<String, GameClient> _authedClients = new ConcurrentHashMap<>();
	
	private final ByteBuffer _readBuffer = ByteBuffer.allocate(64 * 1024).order(ByteOrder.LITTLE_ENDIAN);
	private final ByteBuffer _writeBuffer = ByteBuffer.allocate(64 * 1024).order(ByteOrder.LITTLE_ENDIAN);
	
	private final Queue<SendablePacket> _sendQueue = new ArrayDeque<>();
	private final Lock _sendLock = new ReentrantLock();
	
	private final AtomicBoolean _isPengingWrite = new AtomicBoolean();
	
	private SelectionKey _key;
	private Selector _selector;
	
	private boolean _shutdown;
	private boolean _restart;
	
	private AuthServerCommunication()
	{
		try
		{
			_selector = Selector.open();
		}
		catch (final IOException e)
		{
			_log.warn("", e);
		}
	}
	
	private void connect() throws IOException
	{
		final var hostInfo = IPSettings.getInstance().getAuthServerHost();
		
		_log.info("Connecting to authserver on " + hostInfo.getGameAddress() + ":" + hostInfo.getGamePort());
		
		final var channel = SocketChannel.open();
		channel.configureBlocking(false);
		
		_key = channel.register(_selector, SelectionKey.OP_CONNECT);
		channel.connect(new InetSocketAddress(hostInfo.getGameAddress(), hostInfo.getGamePort()));
	}
	
	public void sendPacket(SendablePacket packet)
	{
		if (isShutdown())
		{
			return;
		}
		
		boolean wakeUp;
		
		_sendLock.lock();
		try
		{
			_sendQueue.add(packet);
			wakeUp = enableWriteInterest();
		}
		catch (final CancelledKeyException _)
		{
			return;
		}
		finally
		{
			_sendLock.unlock();
		}
		
		if (wakeUp)
		{
			_selector.wakeup();
		}
	}
	
	private boolean disableWriteInterest() throws CancelledKeyException
	{
		if (_isPengingWrite.compareAndSet(true, false))
		{
			_key.interestOps(_key.interestOps() & ~SelectionKey.OP_WRITE);
			return true;
		}
		return false;
	}
	
	private boolean enableWriteInterest() throws CancelledKeyException
	{
		if (_isPengingWrite.getAndSet(true) == false)
		{
			_key.interestOps(_key.interestOps() | SelectionKey.OP_WRITE);
			return true;
		}
		return false;
	}
	
	protected ByteBuffer getReadBuffer()
	{
		return _readBuffer;
	}
	
	protected ByteBuffer getWriteBuffer()
	{
		return _writeBuffer;
	}
	
	@Override
	public void run()
	{
		Set<SelectionKey> keys;
		Iterator<SelectionKey> iterator;
		SelectionKey key;
		int opts;
		
		while (!_shutdown)
		{
			_restart = false;
			try
			{
				loop : while (!isShutdown())
				{
					connect();
					
					_selector.select(5000L);
					keys = _selector.selectedKeys();
					if (keys.isEmpty())
					{
						throw new IOException("Connection timeout.");
					}
					
					iterator = keys.iterator();
					
					try
					{
						while (iterator.hasNext())
						{
							key = iterator.next();
							iterator.remove();
							
							opts = key.readyOps();
							
							switch (opts)
							{
								case SelectionKey.OP_CONNECT :
									connect(key);
									break loop;
							}
						}
					}
					catch (final CancelledKeyException _)
					{
						break;
					}
				}
				
				loop : while (!isShutdown())
				{
					_selector.select();
					keys = _selector.selectedKeys();
					iterator = keys.iterator();
					
					try
					{
						while (iterator.hasNext())
						{
							key = iterator.next();
							iterator.remove();
							
							opts = key.readyOps();
							
							switch (opts)
							{
								case SelectionKey.OP_WRITE :
									write(key);
									break;
								case SelectionKey.OP_READ :
									read(key);
									break;
								case SelectionKey.OP_READ | SelectionKey.OP_WRITE :
									write(key);
									read(key);
									break;
							}
						}
					}
					catch (final CancelledKeyException _)
					{
						break loop;
					}
				}
			}
			catch (final IOException _)
			{
				_log.warn("LoginServer not avaible, trying to reconnect...");
			}
			
			close();
			
			try
			{
				Thread.sleep(5000L);
			}
			catch (final InterruptedException _)
			{
				
			}
		}
	}
	
	private void read(SelectionKey key) throws IOException
	{
		final var channel = (SocketChannel) key.channel();
		final var buf = getReadBuffer();
		
		int count;
		
		count = channel.read(buf);
		
		if (count == -1)
		{
			throw new IOException("End of stream.");
		}
		
		if (count == 0)
		{
			return;
		}
		
		buf.flip();
		
		while (tryReadPacket(key, buf))
		{
		}
	}
	
	private boolean tryReadPacket(SelectionKey key, ByteBuffer buf) throws IOException
	{
		final int pos = buf.position();
		if (buf.remaining() > 2)
		{
			int size = buf.getShort() & 0xffff;
			if (size <= 2)
			{
				throw new IOException("Incorrect packet size: <= 2");
			}
			
			size -= 2;
			
			if (size <= buf.remaining())
			{
				final int limit = buf.limit();
				buf.limit(pos + size + 2);
				
				final var rp = PacketHandler.handlePacket(buf);
				
				if (rp != null)
				{
					if (rp.read())
					{
						ThreadPoolManager.getInstance().execute(rp);
					}
				}
				
				buf.limit(limit);
				buf.position(pos + size + 2);
				
				if (!buf.hasRemaining())
				{
					buf.clear();
					return false;
				}
				
				return true;
			}
			buf.position(pos);
		}
		
		buf.compact();
		
		return false;
	}
	
	private void write(SelectionKey key) throws IOException
	{
		final var channel = (SocketChannel) key.channel();
		final var buf = getWriteBuffer();
		
		boolean done;
		
		_sendLock.lock();
		try
		{
			int i = 0;
			SendablePacket sp;
			while (i++ < 64 && (sp = _sendQueue.poll()) != null)
			{
				final int headerPos = buf.position();
				buf.position(headerPos + 2);
				
				sp.write();
				
				final int dataSize = buf.position() - headerPos - 2;
				if (dataSize == 0)
				{
					buf.position(headerPos);
					continue;
				}
				buf.position(headerPos);
				buf.putShort((short) (dataSize + 2));
				buf.position(headerPos + dataSize + 2);
			}
			
			done = _sendQueue.isEmpty();
			if (done)
			{
				disableWriteInterest();
			}
		}
		finally
		{
			_sendLock.unlock();
		}
		buf.flip();
		
		channel.write(buf);
		
		if (buf.remaining() > 0)
		{
			buf.compact();
			done = false;
		}
		else
		{
			buf.clear();
		}
		
		if (!done)
		{
			if (enableWriteInterest())
			{
				_selector.wakeup();
			}
		}
	}
	
	private void connect(SelectionKey key) throws IOException
	{
		final var channel = (SocketChannel) key.channel();
		channel.finishConnect();
		
		key.interestOps(key.interestOps() & ~SelectionKey.OP_CONNECT);
		key.interestOps(key.interestOps() | SelectionKey.OP_READ);
		
		sendPacket(new AuthRequest());
	}
	
	private void close()
	{
		_restart = !_shutdown;
		
		_sendLock.lock();
		try
		{
			_sendQueue.clear();
		}
		finally
		{
			_sendLock.unlock();
		}
		
		_readBuffer.clear();
		_writeBuffer.clear();
		
		_isPengingWrite.set(false);
		
		try
		{
			if (_key != null)
			{
				_key.channel().close();
				_key.cancel();
			}
		}
		catch (final IOException _)
		{}
		
		_waitingClients.clear();
	}
	
	public void shutdown()
	{
		_shutdown = true;
		_selector.wakeup();
	}
	
	public boolean isShutdown()
	{
		return _shutdown || _restart;
	}
	
	public void restart()
	{
		_restart = true;
		_selector.wakeup();
	}
	
	public GameClient addWaitingClient(GameClient client)
	{
		return _waitingClients.put(client.getLogin(), client);
	}
	
	public GameClient removeWaitingClient(String account)
	{
		return _waitingClients.remove(account);
	}
	
	public GameClient addAuthedClient(GameClient client)
	{
		return _authedClients.put(client.getLogin(), client);
	}
	
	public GameClient removeAuthedClient(String login)
	{
		return _authedClients.remove(login);
	}
	
	public GameClient getAuthedClient(String login)
	{
		return _authedClients.get(login);
	}
	
	public List<GameClient> getAuthedClientsByIP(String ip)
	{
		return (ip == null || ip.isEmpty()) ? Collections.emptyList() : _authedClients.values().stream().filter(c -> c != null && c.getIPAddress() != null && c.getIPAddress().equalsIgnoreCase(ip)).toList();
	}
	
	public int getAuthedClientsAmountByIP(String ip)
	{
		return (ip == null || ip.isEmpty()) ? 0 : _authedClients.values().stream().filter(c -> c != null && c.getIPAddress() != null && c.getIPAddress().equalsIgnoreCase(ip)).toList().size();
	}
	
	public List<GameClient> getAuthedClientsByHWID(String hwid)
	{
		return (hwid == null || hwid.isEmpty()) ? Collections.emptyList() : _authedClients.values().stream().filter(c -> c != null && c.getHWID() != null && c.getHWID().equalsIgnoreCase(hwid)).toList();
	}
	
	public int getAuthedClientsAmountByHWID(String hwid)
	{
		return (hwid == null || hwid.isEmpty()) ? 0 : _authedClients.values().stream().filter(c -> c != null && c.getHWID() != null && !c.getHWID().isEmpty() && c.getHWID().equalsIgnoreCase(hwid)).toList().size();
	}
	
	public GameClient removeClient(GameClient client)
	{
		return client.isAuthed() ? _authedClients.remove(client.getLogin()) : _waitingClients.remove(client.getLogin());
	}
	
	public Set<String> getAccounts()
	{
		return _authedClients.keySet();
	}
}