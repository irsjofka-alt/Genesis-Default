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
package gameserver.network.communication.loginserverpackets;

import gameserver.Config;
import gameserver.data.htm.HtmCache;
import gameserver.instancemanager.DoubleSessionManager;
import gameserver.model.actor.Player;
import gameserver.model.base.Language;
import gameserver.network.GameClient;
import gameserver.network.SystemMessageId;
import gameserver.network.communication.AuthServerCommunication;
import gameserver.network.communication.ReceivablePacket;
import gameserver.network.communication.SessionKey;
import gameserver.network.communication.gameserverpackets.PlayerInGame;
import gameserver.network.serverpackets.CharacterSelectionInfo;
import gameserver.network.serverpackets.LoginFail;
import gameserver.network.serverpackets.ServerClose;
import gameserver.network.serverpackets.SystemMessage;
import gameserver.network.serverpackets.TutorialShowHtml;

public class PlayerAuthResponse extends ReceivablePacket
{
	private String _account;
	private boolean _authed;
	private int _playOkId1;
	private int _playOkId2;
	private int _loginOkId1;
	private int _loginOkId2;
	private String _ip, _hwid, _lockedIp;
	private int _requestId;
  
	@Override
	public void readImpl()
	{
		_account = readS();
		_authed = (readC() == 1);
		if (_authed)
		{
			_playOkId1 = readD();
			_playOkId2 = readD();
			_loginOkId1 = readD();
			_loginOkId2 = readD();
			_ip = readS();
			_requestId = readD();
			_hwid = readS();
			_lockedIp = readS();
		}
	}
  
	@Override
	protected void runImpl()
	{
		final SessionKey skey = new SessionKey(_loginOkId1, _loginOkId2, _playOkId1, _playOkId2);
		final GameClient client = AuthServerCommunication.getInstance().removeWaitingClient(_account);
		if (client == null)
		{
			return;
		}
		
		if ((_authed) && (client.getSessionId().equals(skey)))
		{
			client.setAuthed(true);
			client.setState(GameClient.GameClientState.AUTHED);
			client.updateHWID();
			
			final int limit = DoubleSessionManager.getInstance().getHardWareLimit(Config.DOUBLE_SESSIONS_HWIDS ? client.getHWID() : client.getIPAddress());
			if (limit > 0 && AuthServerCommunication.getInstance().getAuthedClient(_account) == null)
			{
				var clients = 0;
				if (Config.DOUBLE_SESSIONS_HWIDS)
				{
					clients = AuthServerCommunication.getInstance().getAuthedClientsAmountByHWID(client.getHWID());
				}
				else
				{
					clients = AuthServerCommunication.getInstance().getAuthedClientsAmountByIP(client.getIPAddress());
				}
				
				if (clients >= limit)
				{
					String html = HtmCache.getInstance().getHtm(client.getActiveChar(), Language.getById(client.getLang()), "data/html/windows_limit_hwid.htm");
					if (html != null)
					{
						html = html.replace("<?active_windows?>", String.valueOf(clients));
						html = html.replace("<?windows_limit?>", String.valueOf(limit));
						client.close(new TutorialShowHtml(html));
					}
					else
					{
						client.close(new LoginFail(4));
					}
					return;
				}
			}
			
			final GameClient oldClient = AuthServerCommunication.getInstance().addAuthedClient(client);
			if ((!Config.ALLOW_MULILOGIN) && (oldClient != null))
			{
				oldClient.setAuthed(false);
				final Player activeChar = oldClient.getActiveChar();
				if (activeChar != null)
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ANOTHER_LOGIN_WITH_ACCOUNT));
					activeChar.logout();
				}
				else
				{
					oldClient.close(ServerClose.STATIC_PACKET);
				}
			}
			sendPacket(new PlayerInGame(client.getLogin()));
      
			final CharacterSelectionInfo csi = new CharacterSelectionInfo(client.getLogin(), client.getSessionId().playOkID1);
			client.sendPacket(csi);
			client.setCharSelection(csi.getCharInfo());
			client.setRealIpAddress(_ip);
			client.setRequestId(_requestId);
			client.setLockedHwid(_hwid);
			client.setLockedIp(_lockedIp);
		}
		else
		{
			client.close(new LoginFail(4));
		}
	}
}
