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
package loginserver.network.clientpackets;

import javax.crypto.Cipher;

import loginserver.Config;
import loginserver.GameServerManager;
import loginserver.IpBanManager;
import loginserver.accounts.Account;
import loginserver.accounts.SessionManager;
import loginserver.accounts.SessionManager.Session;
import loginserver.crypt.PasswordHash;
import loginserver.network.LoginClient;
import loginserver.network.LoginClient.LoginClientState;
import loginserver.network.communication.GameServer;
import loginserver.network.communication.loginserverpackets.GetAccountInfo;
import loginserver.network.serverpackets.LoginFail.LoginFailReason;
import loginserver.network.serverpackets.LoginOk;
import loginserver.network.serverpackets.ServerList;

public class RequestAuthLogin extends LoginClientPacket
{
	private final byte[] _raw1 = new byte[128];
	private final byte[] _raw2 = new byte[128];
	private boolean _newAuthMethod = false;

	@Override
	protected void readImpl()
	{
		if(_buf.remaining() >= (_raw1.length + _raw2.length))
		{
			_newAuthMethod = true;
			readB(_raw1);
			readB(_raw2);
		}

		if(_buf.remaining() >= _raw1.length)
		{
			readB(_raw1);
			readD();
			readD();
			readD();
			readD();
			readD();
			readD();
			readH();
			readC();
		}
	}

	@Override
	protected void runImpl() throws Exception
	{
		final LoginClient client = getClient();

		byte[] decUser = null;
		byte[] decPass = null;
		try
		{
			final Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
			rsaCipher.init(Cipher.DECRYPT_MODE, client.getRSAPrivateKey());
			decUser = rsaCipher.doFinal(_raw1, 0x00, 0x80);
			if(_newAuthMethod)
			{
				decPass = rsaCipher.doFinal(_raw2, 0x00, _raw2.length);
			}
		}
		catch(final Exception _)
		{
			client.closeNow(true);
			return;
		}

		String user = null;
		String password = null;
		if(_newAuthMethod)
		{
			user = new String(decUser, 0x4E, 32).trim().toLowerCase();
			password = new String(decPass, 0x5C, 16).trim();
		}
		else
		{
			user = new String(decUser, 0x5E, 14).trim().toLowerCase();
			password = new String(decUser, 0x6C, 16).trim();
		}

		final int currentTime = (int) (System.currentTimeMillis() / 1000L);

		final Account account = new Account(user);
		account.restore();

		final String passwordHash = Config.DEFAULT_CRYPT.encrypt(password);

		if(account.getPasswordHash() == null)
		{
			if(Config.AUTO_CREATE_ACCOUNTS && user.matches(Config.ANAME_TEMPLATE) && password.matches(Config.APASSWD_TEMPLATE))
			{
				account.setPasswordHash(Config.ALLOW_ENCODE_PASSWORD ? passwordHash : password);
				account.save();
			}
			else
			{
				client.close(LoginFailReason.REASON_USER_OR_PASS_WRONG);
				return;
			}
		}

		boolean passwordCorrect = true;
		if ((!account.getPasswordHash().equals(passwordHash) && Config.ALLOW_ENCODE_PASSWORD))
		{
			passwordCorrect = false;
		}
		
		if ((!account.getPasswordHash().equals(passwordHash) && !account.getPasswordHash().equals(password) && !Config.ALLOW_ENCODE_PASSWORD))
		{
			passwordCorrect = false;
		}
		
		if (!passwordCorrect)
		{
			for(final PasswordHash c : Config.LEGACY_CRYPT)
			{
				if(c.compare(password, account.getPasswordHash()))
				{
					passwordCorrect = true;
					account.setPasswordHash(passwordHash);
					break;
				}
			}
		}

		if(!IpBanManager.getInstance().tryLogin(client.getIpAddress(), passwordCorrect))
		{
			client.closeNow(false);
			return;
		}

		client.setPasswordCorrect(passwordCorrect);

		if(!Config.CHEAT_PASSWORD_CHECK)
		{
			if(!passwordCorrect)
			{
				client.close(LoginFailReason.REASON_USER_OR_PASS_WRONG);
				return;
			}
		}

		if(account.getAccessLevel() < 0)
		{
			client.close(LoginFailReason.REASON_ACCESS_FAILED);
			return;
		}

		if(account.getBanExpire() > currentTime)
		{
			client.close(LoginFailReason.REASON_ACCESS_FAILED);
			return;
		}

		if (!account.isAllowedIP(client.getIpAddress()))
		{
			client.close(LoginFailReason.REASON_ATTEMPTED_RESTRICTED_IP);
			return;
		}

		for(final GameServer gs : GameServerManager.getInstance().getGameServers())
		{
			if(gs.isAuthed())
			{
				gs.sendPacket(new GetAccountInfo(user));
			}
		}

		account.setLastAccess(currentTime);
		account.setLastIP(client.getIpAddress());

		final Session session = SessionManager.getInstance().openSession(account, client.getIpAddress());
		client.setAuthed(true);
		client.setLogin(user);
		client.setAccount(account);
		client.setSessionKey(session.getSessionKey());
		client.setState(LoginClientState.AUTHED);
		client.sendPacket(new LoginOk(client.getSessionKey()));
		if (Config.SHOW_LICENCE)
		{
			client.sendPacket(new LoginOk(getClient().getSessionKey()));
		}
		else
		{
			getClient().sendPacket(new ServerList(account));
		}
	}
}