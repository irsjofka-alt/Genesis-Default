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
package gameserver.instancemanager.mods;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

import l2e.commons.log.LoggerObject;
import l2e.commons.time.cron.SchedulingPattern;
import gameserver.Config;
import gameserver.data.parser.ItemsParser;
import gameserver.instancemanager.ServerVariables;

/**
 * Created by LordWinter
 */
public class DailyItemManager extends LoggerObject
{
	private final List<DailyItemTemplate> _rewards = new ArrayList<>();
	private final Map<Integer, Long> _sales = new ConcurrentHashMap<>();
	
	public DailyItemManager()
	{
		if (Config.ALLOW_DAILY_ITEMS)
		{
			_rewards.clear();
			_sales.clear();
			loadRewards();
			final long lastUpdate = ServerVariables.getLong("Daily_Items", 0);
			if (System.currentTimeMillis() > lastUpdate)
			{
				final long newTime = new SchedulingPattern("30 6 * * *").next(System.currentTimeMillis());
				ServerVariables.set("Daily_Items", newTime);
				ServerVariables.set("Daily_ItemID", 0);
				ServerVariables.set("Daily_Sales", 0);
			}
			else
			{
				final int itemId = ServerVariables.getInt("Daily_ItemID", 0);
				if (itemId != 0)
				{
					_sales.put(itemId, ServerVariables.getLong("Daily_Sales", 0));
				}
			}
		}
	}
	
	public void checkTimeTask()
	{
		final long lastUpdate = ServerVariables.getLong("Daily_Items", 0);
		if (System.currentTimeMillis() > lastUpdate)
		{
			final long newTime = new SchedulingPattern("30 6 * * *").next(System.currentTimeMillis());
			ServerVariables.set("Daily_Items", newTime);
			ServerVariables.set("Daily_ItemID", 0);
			ServerVariables.set("Daily_Sales", 0);
			_sales.clear();
		}
	}
	
	private void loadRewards()
	{
		try
		{
			final var file = new File(Config.DATAPACK_ROOT + "/data/stats/services/dailyItems.xml");
			final var factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			final var doc1 = factory.newDocumentBuilder().parse(file);

			int counter = 0;
			for (var n1 = doc1.getFirstChild(); n1 != null; n1 = n1.getNextSibling())
			{
				if ("list".equalsIgnoreCase(n1.getNodeName()))
				{
					for (var d1 = n1.getFirstChild(); d1 != null; d1 = d1.getNextSibling())
					{
						if ("day".equalsIgnoreCase(d1.getNodeName()))
						{
							counter++;
							DailyItemTemplate template = null;
							final Map<Integer, Integer> rewards = new HashMap<>();
							final Map<Integer, Integer> requests = new HashMap<>();
							final int number = Integer.parseInt(d1.getAttributes().getNamedItem("number").getNodeValue());
							String image = d1.getAttributes().getNamedItem("image") != null ? d1.getAttributes().getNamedItem("image").getNodeValue() : "";
							final int limit = d1.getAttributes().getNamedItem("limit") != null ? Integer.parseInt(d1.getAttributes().getNamedItem("limit").getNodeValue()) : 1;
							final String descr = d1.getAttributes().getNamedItem("descr") != null ? d1.getAttributes().getNamedItem("descr").getNodeValue() : "";
							int displayItemId = 0;
							for (var s1 = d1.getFirstChild(); s1 != null; s1 = s1.getNextSibling())
							{
								if ("reward".equalsIgnoreCase(s1.getNodeName()))
								{
									final int itemId = Integer.parseInt(s1.getAttributes().getNamedItem("itemId").getNodeValue());
									final int count = Integer.parseInt(s1.getAttributes().getNamedItem("count").getNodeValue());
									rewards.put(itemId, count);
									
									final boolean isDisplayId = s1.getAttributes().getNamedItem("displayId") != null && Boolean.parseBoolean(s1.getAttributes().getNamedItem("displayId").getNodeValue());
									if (isDisplayId)
									{
										if (image == "")
										{
											image = ItemsParser.getInstance().getTemplate(itemId).getIcon();
										}
										displayItemId = itemId;
									}
								}
								else if ("request".equalsIgnoreCase(s1.getNodeName()))
								{
									final int itemId = Integer.parseInt(s1.getAttributes().getNamedItem("itemId").getNodeValue());
									final int count = Integer.parseInt(s1.getAttributes().getNamedItem("count").getNodeValue());
									requests.put(itemId, count);
								}
							}
							template = new DailyItemTemplate(number, descr, requests, rewards, limit);
							if (image != "")
							{
								template.setDisplayImage(image);
							}
							template.setDisplayItemId(displayItemId);
							_rewards.add(template);
						}
					}
				}
			}
			info(getClass().getSimpleName() + ": Loaded " + counter + " daily rewards.");
		}
		catch (NumberFormatException | DOMException | ParserConfigurationException | SAXException e)
		{
			warn(getClass().getSimpleName() + ": dailyRewards.xml could not be initialized.", e);
		}
		catch (IOException | IllegalArgumentException e)
		{
			warn(getClass().getSimpleName() + ": IOException or IllegalArgumentException.", e);
		}
	}
	
	public DailyItemTemplate getDailyItem(int day)
	{
		for (final var dayReward : _rewards)
		{
			if (dayReward.getDay() == day)
			{
				return dayReward;
			}
		}
		return null;
	}
	
	public List<DailyItemTemplate> getDailyRewards()
	{
		return _rewards;
	}
	
	public long getItemSales(int id)
	{
		if (_sales.containsKey(id))
		{
			return _sales.get(id);
		}
		return 0;
	}
	
	public void addItemSale(int id)
	{
		if (_sales.containsKey(id))
		{
			final var amount = _sales.get(id);
			_sales.put(id, (amount + 1));
			ServerVariables.set("Daily_Sales", (amount + 1));
		}
		else
		{
			_sales.put(id, 1L);
			ServerVariables.set("Daily_ItemID", id);
			ServerVariables.set("Daily_Sales", 1);
		}
	}
	
	public static class DailyItemTemplate
	{
		private final int _day;
		private final String _descr;
		private final Map<Integer, Integer> _rewards;
		private final Map<Integer, Integer> _request;
		private final int _limit;
		private int _displayItemId;
		private String _displayImage;
		
		public DailyItemTemplate(int day, String descr, Map<Integer, Integer> request, Map<Integer, Integer> rewards, int limit)
		{
			_day = day;
			_descr = descr;
			_request = request;
			_rewards = rewards;
			_limit = limit;
		}
		
		public int getDay()
		{
			return _day;
		}
		
		public int getLimit()
		{
			return _limit;
		}
		
		public String getDescr()
		{
			return _descr;
		}
		
		public Map<Integer, Integer> getRequests()
		{
			return _request;
		}

		public Map<Integer, Integer> getRewards()
		{
			return _rewards;
		}
		
		public String getDisplayImage()
		{
			return _displayImage;
		}
		
		public void setDisplayImage(String image)
		{
			_displayImage = image;
		}
		
		public int getDisplayItemId()
		{
			return _displayItemId;
		}
		
		public void setDisplayItemId(int id)
		{
			_displayItemId = id;
		}
	}
	
	public static DailyItemManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final DailyItemManager _instance = new DailyItemManager();
	}
}