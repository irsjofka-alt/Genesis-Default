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
package loginserver.accounts;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import loginserver.ThreadPoolManager;
import loginserver.network.SessionKey;

public class SessionManager
{
	public final class Session
	{
		private final Account _account;
		private final SessionKey _skey;
		private final long _expireTime;
		private final String _ip;

		private Session(Account account, String ip)
		{
			_account = account;
			_ip = ip;
			_skey = SessionKey.create();
			_expireTime = System.currentTimeMillis() + 60000L;
		}

		public SessionKey getSessionKey()
		{
			return _skey;
		}

		public Account getAccount()
		{
			return _account;
		}
		
		public String getIP()
		{
			return _ip;
		}

		public long getExpireTime()
		{
			return _expireTime;
		}
	}

	private final Map<SessionKey, Session> _sessions = new ConcurrentHashMap<>();

	private SessionManager()
	{
		ThreadPoolManager.getInstance().scheduleAtFixedRate(() ->
		{
			final long currentMillis = System.currentTimeMillis();
			Session session;
			for (final var itr = _sessions.values().iterator(); itr.hasNext();)
			{
				session = itr.next();
				if (session.getExpireTime() < currentMillis)
				{
					itr.remove();
				}
			}
		}, 30000L, 30000L);
	}

	public Session openSession(Account account, String ip)
	{
		final var session = new Session(account, ip);
		_sessions.put(session.getSessionKey(), session);
		return session;
	}

	public Session closeSession(SessionKey skey)
	{
		return _sessions.remove(skey);
	}

	public Session getSessionByName(String name)
	{
		for (final var session : _sessions.values())
		{
			if (session.getAccount().getLogin().equalsIgnoreCase(name))
			{
				return session;
			}
		}
		return null;
	}
	
	public static final SessionManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final SessionManager _instance = new SessionManager();
	}
}
