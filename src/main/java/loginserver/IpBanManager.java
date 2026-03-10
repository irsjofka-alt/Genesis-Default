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
package loginserver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IpBanManager
{
	private static final Logger _log = LogManager.getLogger(IpBanManager.class);

	private class IpSession
	{
		public int tryCount;
		public long lastTry;
		public long banExpire;
	}

	private final Map<String, IpSession> _list = new ConcurrentHashMap<>();

	private IpBanManager()
	{
		ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> checkBanTask(), 1000L, 1000L);
	}
	
	private void checkBanTask()
	{
		final long currentMillis = System.currentTimeMillis();
		for (final var entry : _list.entrySet())
		{
			final var session = entry.getValue();
			if (session.banExpire < currentMillis && session.lastTry < currentMillis - Config.LOGIN_TRY_TIMEOUT)
			{
				_list.remove(entry.getKey());
			}
		}
	}

	public boolean isIpBanned(String ip)
	{
		if (Config.WHITE_IPS.contains(ip))
		{
			return false;
		}
		
		IpSession ipsession;
		if ((ipsession = _list.get(ip)) == null)
		{
			return false;
		}
		return ipsession.banExpire > System.currentTimeMillis();
	}

	public boolean tryLogin(String ip, boolean success)
	{
		if (Config.WHITE_IPS.contains(ip))
		{
			return true;
		}
		
		IpSession ipsession;
		if ((ipsession = _list.get(ip)) == null)
		{
			_list.put(ip, ipsession = new IpSession());
		}
		
		final long currentMillis = System.currentTimeMillis();
		
		if (currentMillis - ipsession.lastTry < Config.LOGIN_TRY_TIMEOUT)
		{
			success = false;
		}
		
		if (success)
		{
			if (ipsession.tryCount > 0)
			{
				ipsession.tryCount--;
			}
		}
		else
		{
			if (ipsession.tryCount < Config.LOGIN_TRY_BEFORE_BAN)
			{
				ipsession.tryCount++;
			}
		}
		
		ipsession.lastTry = currentMillis;
		if (ipsession.tryCount == Config.LOGIN_TRY_BEFORE_BAN && Config.IP_BAN_TIME > 0)
		{
			_log.warn("IpBanManager: " + ip + " banned for " + Config.IP_BAN_TIME + " seconds.");
			ipsession.banExpire = currentMillis + (Config.IP_BAN_TIME * 1000L);
			return false;
		}
		return true;
	}
	
	public static final IpBanManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final IpBanManager _instance = new IpBanManager();
	}
}
