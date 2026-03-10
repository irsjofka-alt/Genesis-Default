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

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gameserver.GameTimeController;
import gameserver.instancemanager.PunishmentManager;
import gameserver.model.PcCondOverride;
import gameserver.model.punishment.PunishmentAffect;
import gameserver.model.punishment.PunishmentSort;
import gameserver.model.punishment.PunishmentTemplate;
import gameserver.model.punishment.PunishmentType;
import gameserver.network.GameClient;

public final class FloodProtectorAction
{
	private static final Logger _log = LogManager.getLogger(FloodProtectorAction.class);

	private final GameClient _client;
	private final FloodProtectorConfig _config;
	private volatile long _nextTime = System.currentTimeMillis();
	private final AtomicInteger _count = new AtomicInteger(0);
	private boolean _logged;
	private volatile boolean _punishmentInProgress;
	
	public FloodProtectorAction(final GameClient client, final FloodProtectorConfig config)
	{
		_client = client;
		_config = config;
	}
	
	public boolean tryPerformAction(final String command)
	{
		final long curTime = System.currentTimeMillis();

		if (_client.getActiveChar() != null && _client.getActiveChar().canOverrideCond(PcCondOverride.FLOOD_CONDITIONS))
		{
			return true;
		}
		
		if ((curTime < _nextTime) || _punishmentInProgress)
		{
			if (_config.LOG_FLOODING && !_logged)
			{
				log(" called command ", command, " ~", String.valueOf(_config.FLOOD_PROTECTION_INTERVAL - (_nextTime - curTime)), " ms after previous command");
				_logged = true;
			}
			
			_count.incrementAndGet();
			
			if (!_punishmentInProgress && _config.PUNISHMENT_LIMIT > 0 && _count.get() >= _config.PUNISHMENT_LIMIT && _config.PUNISHMENT_TYPE != null)
			{
				_punishmentInProgress = true;
				
				if ("kick".equals(_config.PUNISHMENT_TYPE))
				{
					kickPlayer();
				}
				else if ("ban".equals(_config.PUNISHMENT_TYPE))
				{
					banAccount();
				}
				else if ("jail".equals(_config.PUNISHMENT_TYPE))
				{
					jailChar();
				}
				
				_punishmentInProgress = false;
			}
			return false;
		}
		
		if (_count.get() > 0)
		{
			if (_config.LOG_FLOODING)
			{
				log(" issued ", String.valueOf(_count), " extra requests within ~", String.valueOf(_config.FLOOD_PROTECTION_INTERVAL * GameTimeController.MILLIS_IN_TICK), " ms");
			}
		}
		
		_nextTime = curTime + _config.FLOOD_PROTECTION_INTERVAL;
		_logged = false;
		_count.set(0);
		
		return true;
	}
	
	private void kickPlayer()
	{
		if (_client.getActiveChar() != null)
		{
			_client.getActiveChar().logout();
		}
		else
		{
			_client.closeNow(false);
		}
		log("kicked for flooding");
	}
	
	private void banAccount()
	{
		if (_client != null && _client.getActiveChar() != null)
		{
			PunishmentManager.getInstance().addPunishment(_client.getActiveChar(), null, new PunishmentTemplate(_client.getLogin(), _client.getLogin(), PunishmentSort.ACCOUNT, PunishmentAffect.ACCOUNT, PunishmentType.BAN, (System.currentTimeMillis() + _config.PUNISHMENT_TIME), getClass().getSimpleName(), _client.getActiveChar().getName(null)), true);
		}
		log(" banned for flooding ", _config.PUNISHMENT_TIME <= 0 ? "forever" : "for " + _config.PUNISHMENT_TIME + " mins");
	}
	
	private void jailChar()
	{
		if (_client != null && _client.getActiveChar() != null)
		{
			final int charId = _client.getActiveChar().getObjectId();
			if (charId > 0)
			{
				PunishmentManager.getInstance().addPunishment(_client.getActiveChar(), null, new PunishmentTemplate(String.valueOf(charId), _client.getActiveChar().getName(null), PunishmentSort.CHARACTER, PunishmentAffect.CHARACTER, PunishmentType.JAIL, (System.currentTimeMillis() + _config.PUNISHMENT_TIME), getClass().getSimpleName(), _client.getActiveChar().getName(null)), true);
			}
			log(" jailed for flooding ", _config.PUNISHMENT_TIME <= 0 ? "forever" : "for " + _config.PUNISHMENT_TIME + " mins");
		}
	}
	
	private void log(String... lines)
	{
		final StringBuilder output = StringUtil.startAppend(100, _config.FLOOD_PROTECTOR_TYPE, ": ");
		String address = null;
		try
		{
			if (!_client.isDetached())
			{
				address = _client.getConnection().getSocket().getInetAddress().getHostAddress();
			}
		}
		catch (final Exception _)
		{}
		
		switch (_client.getState())
		{
			case IN_GAME :
				if (_client.getActiveChar() != null)
				{
					StringUtil.append(output, _client.getActiveChar().getName(null));
					StringUtil.append(output, "(", String.valueOf(_client.getActiveChar().getObjectId()), ") ");
				}
				break;
			case AUTHED :
				if (_client.getLogin() != null)
				{
					StringUtil.append(output, _client.getLogin(), " ");
				}
				break;
			case CONNECTED :
				if (address != null)
				{
					StringUtil.append(output, address);
				}
				break;
			default :
				throw new IllegalStateException("Missing state on switch");
		}
		
		StringUtil.append(output, lines);
		_log.warn(output.toString());
	}
}