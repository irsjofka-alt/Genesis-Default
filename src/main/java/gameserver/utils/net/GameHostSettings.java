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
package gameserver.utils.net;

import java.io.File;

import org.HostInfo;

import l2e.commons.net.IPSettings;
import gameserver.Config;
import gameserver.data.DocumentParser;

public class GameHostSettings extends DocumentParser
{
	public GameHostSettings()
	{
	}
	
	public void loadGameSettings()
	{
		final var f = new File("./config/ipconfig.xml");
		if (f.exists())
		{
			_log.info("Network Config: ipconfig.xml exists using manual configuration...");
			parseFile(new File("./config/ipconfig.xml"), false);
		}
		else
		{
			autoIpConfig();
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
					else if ("gameserver".equalsIgnoreCase(c.getNodeName()))
					{
						final boolean allowHaProxy = c.getAttributes().getNamedItem("allowHaProxy") != null ? Boolean.parseBoolean(c.getAttributes().getNamedItem("allowHaProxy").getNodeValue()) : false;
						for (var d = c.getFirstChild(); d != null; d = d.getNextSibling())
						{
							if ("host".equalsIgnoreCase(d.getNodeName()))
							{
								if (IPSettings.getInstance().getGameServerHost() == null)
								{
									final int id = Integer.parseInt(d.getAttributes().getNamedItem("id").getNodeValue());
									Config.REQUEST_ID = id;
									final String address = d.getAttributes().getNamedItem("address") != null ? d.getAttributes().getNamedItem("address").getNodeValue() : "127.0.0.1";
									final int port = Integer.parseInt(d.getAttributes().getNamedItem("port").getNodeValue());
									final String key = d.getAttributes().getNamedItem("key").getNodeValue();
									final var hostInfo = new HostInfo(id, address, port, key, allowHaProxy);
									for (var s = d.getFirstChild(); s != null; s = s.getNextSibling())
									{
										if ("advanced".equalsIgnoreCase(s.getNodeName()))
										{
											final String advanced_address = s.getAttributes().getNamedItem("address").getNodeValue();
											final String advanced_subnet = s.getAttributes().getNamedItem("subnet").getNodeValue();
											hostInfo.addSubnet(advanced_address, advanced_subnet);
										}
									}
									IPSettings.getInstance().setGameServerHost(hostInfo);
								}
							}
						}
					}
				}
			}
		}
	}
	
	public void autoIpConfig()
	{
		try
		{
			final var hostInfo = new HostInfo(1, "127.0.0.1", 7777, "ENTER_RANDOM_KEY", false);
			Config.REQUEST_ID = hostInfo.getId();
			hostInfo.addSubnet("127.0.0.1", "127.0.0.0/8");
			hostInfo.addSubnet("10.0.0.0", "10.0.0.0/8");
			hostInfo.addSubnet("172.16.0.0", "172.16.0.0/12");
			hostInfo.addSubnet("192.168.0.0", "192.168.0.0/16");
			hostInfo.addSubnet("169.254.0.0", "169.254.0.0/16");
			IPSettings.getInstance().setGameServerHost(hostInfo);
		}
		catch (final Exception e)
		{
		}
		Config.c = false;
	}
	
	public static GameHostSettings getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final GameHostSettings _instance = new GameHostSettings();
	}
}