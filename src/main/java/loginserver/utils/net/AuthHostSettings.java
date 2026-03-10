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
package loginserver.utils.net;

import java.io.File;

import org.HostInfo;

import l2e.commons.net.IPSettings;
import loginserver.data.DocumentParser;

public class AuthHostSettings extends DocumentParser
{
	public AuthHostSettings()
	{
	}
	
	public void loadLoginSettings()
	{
		final var f = new File("../game/config/ipconfig.xml");
		if (f.exists())
		{
			_log.info("Network Config: ipconfig.xml exists using manual configuration...");
			parseFile(new File("../game/config/ipconfig.xml"), false);
		}
		else
		{
			IPSettings.getInstance().setAuthServerHost(new HostInfo("127.0.0.1", 2106, false, "127.0.0.1", 9014));
		}
	}
	
	@Override
	public void load()
	{
	}
	
	@Override
	protected void reloadDocument()
	{
	}
	
	@Override
	protected void parseDocument()
	{
		for (var n = getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (var c = n.getFirstChild(); c != null; c = c.getNextSibling())
				{
					if ("authserver".equalsIgnoreCase(c.getNodeName()))
					{
						if (IPSettings.getInstance().getAuthServerHost() == null)
						{
							final boolean allowHaProxy = c.getAttributes().getNamedItem("allowHaProxy") != null ? Boolean.parseBoolean(c.getAttributes().getNamedItem("allowHaProxy").getNodeValue()) : false;
							final String address = c.getAttributes().getNamedItem("address").getNodeValue();
							final int port = Integer.parseInt(c.getAttributes().getNamedItem("port").getNodeValue());
							String gameAddress = null;
							int gamePort = 0;
							for (var d = c.getFirstChild(); d != null; d = d.getNextSibling())
							{
								if ("game".equalsIgnoreCase(d.getNodeName()))
								{
									gameAddress = d.getAttributes().getNamedItem("address").getNodeValue();
									gamePort = Integer.parseInt(d.getAttributes().getNamedItem("port").getNodeValue());
								}
							}
							IPSettings.getInstance().setAuthServerHost(new HostInfo(address, port, allowHaProxy, gameAddress, gamePort));
						}
					}
				}
			}
		}
	}
	
	public static AuthHostSettings getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final AuthHostSettings _instance = new AuthHostSettings();
	}
}