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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import l2e.commons.util.AbstractSettings;
import gameserver.Config;
import gameserver.model.holders.ItemHolder;

public final class GameSettings extends AbstractSettings
{
	@Serial
	private static final long serialVersionUID = -2198933618009560301L;
	
	public GameSettings()
	{
	}
	
	public GameSettings(String name) throws IOException
	{
		try (FileInputStream fis = new FileInputStream(name))
		{
			load(fis);
		}
	}
	
	public GameSettings(File file) throws IOException
	{
		try (FileInputStream fis = new FileInputStream(file))
		{
			load(fis);
		}
	}
	
	public GameSettings(InputStream inStream) throws IOException
	{
		load(inStream);
	}
	
	public GameSettings(Reader reader) throws IOException
	{
		load(reader);
	}
	
	@Override
	public String getProperty(String key, String defaultValue)
	{
		return getProperty(key, defaultValue, true);
	}
	
	@Override
	public boolean getProperty(String name, final boolean defaultValue)
	{
		return getProperty(name, defaultValue, true);
	}
	
	@Override
	public int getProperty(final String name, final int defaultValue)
	{
		return getProperty(name, defaultValue, true);
	}
	
	@Override
	public long getProperty(final String name, final long defaultValue)
	{
		return getProperty(name, defaultValue, true);
	}
	
	@Override
	public float getProperty(final String name, final float defaultValue)
	{
		return getProperty(name, defaultValue, true);
	}
	
	@Override
	public double getProperty(final String name, final double defaultValue)
	{
		return getProperty(name, defaultValue, true);
	}
	
	@Override
	public short getProperty(final String name, final short defaultValue)
	{
		return getProperty(name, defaultValue, true);
	}
	
	@Override
	public String[] getProperty(final String name, final String[] defaultValue, final String delimiter)
	{
		return getProperty(name, defaultValue, delimiter, true);
	}
	
	@Override
	public boolean[] getProperty(final String name, final boolean[] defaultValue, final String delimiter)
	{
		return getProperty(name, defaultValue, delimiter, true);
	}
	
	@Override
	public int[] getIntProperty(final String name, String defaultValue, final String delimiter)
	{
		return getIntProperty(name, defaultValue, delimiter, true);
	}
	
	@Override
	public int[][] getDoubleIntProperty(final String name, String defaultValue, final String delimiter)
	{
		return getDoubleIntProperty(name, defaultValue, delimiter, true);
	}
	
	@Override
	public long[] getLongProperty(final String name, String defaultValue, final String delimiter)
	{
		return getLongProperty(name, defaultValue, delimiter, true);
	}
	
	@Override
	public List<String> getListProperty(String name, String defaultValue, String delimiter)
	{
		return getListProperty(name, defaultValue, delimiter, true);
	}
	
	@Override
	public Set<String> getSetProperty(String name, String defaultValue, String delimiter)
	{
		return getSetProperty(name, defaultValue, delimiter, true);
	}
	
	@Override
	public List<Integer> getIntegerProperty(String name, String defaultValue, String delimiter)
	{
		return getIntegerProperty(name, defaultValue, delimiter, true);
	}
	
	@Override
	public Map<Integer, Integer> getMapProperty(String name, String defaultValue, String delimiter)
	{
		return getMapProperty(name, defaultValue, delimiter, true);
	}
	
	@Override
	public Map<String, Integer> getStringMapProperty(String name, String defaultValue, String delimiter)
	{
		return getStringMapProperty(name, defaultValue, delimiter, true);
	}
	
	@Override
	public Map<Integer, String> getMapStringProperty(String name, String defaultValue, String delimiter)
	{
		return getMapStringProperty(name, defaultValue, delimiter, true);
	}
	
	public String getProperty(String key, String defaultValue, boolean debug)
	{
		if (Config.getPersonalConfigs().containsKey(key))
		{
			return Config.getPersonalConfigs().get(key);
		}
		final String property = super.getProperty(key);
		if (property == null)
		{
			if (debug)
			{
				_log.warn("Missing parameter for config - " + key);
			}
			return defaultValue;
		}
		return property.trim();
	}
	
	public boolean getProperty(String name, final boolean defaultValue, boolean debug)
	{
		boolean val = defaultValue;
		final String value;
		if ((value = getProperty(name, null, debug)) != null && !value.isEmpty())
		{
			val = parseBoolean(value);
		}
		return val;
	}
	
	public int getProperty(final String name, final int defaultValue, boolean debug)
	{
		int val = defaultValue;
		final String value;
		if ((value = getProperty(name, null, debug)) != null && !value.isEmpty())
		{
			val = Integer.parseInt(value);
		}
		
		return val;
	}
	
	public long getProperty(final String name, final long defaultValue, boolean debug)
	{
		long val = defaultValue;
		final String value;
		if ((value = getProperty(name, null, debug)) != null && !value.isEmpty())
		{
			val = Long.parseLong(value);
		}
		return val;
	}
	
	public float getProperty(final String name, final float defaultValue, boolean debug)
	{
		float val = defaultValue;
		final String value;
		if ((value = getProperty(name, null, debug)) != null && !value.isEmpty())
		{
			val = Float.parseFloat(value);
		}
		return val;
	}
	
	public double getProperty(final String name, final double defaultValue, boolean debug)
	{
		double val = defaultValue;
		final String value;
		if ((value = getProperty(name, null, debug)) != null && !value.isEmpty())
		{
			val = Double.parseDouble(value);
		}
		return val;
	}
	
	public short getProperty(final String name, final short defaultValue, boolean debug)
	{
		short val = defaultValue;
		final String value;
		if ((value = getProperty(name, null, debug)) != null && !value.isEmpty())
		{
			val = Short.parseShort(value);
		}
		return val;
	}
	
	public String[] getProperty(final String name, final String[] defaultValue, final String delimiter, boolean debug)
	{
		String[] val = defaultValue;
		final String value;
		if ((value = getProperty(name, null, debug)) != null)
		{
			val = value.split(delimiter);
		}
		return val;
	}
	
	public boolean[] getProperty(final String name, final boolean[] defaultValue, final String delimiter, boolean debug)
	{
		boolean[] val = defaultValue;
		final String value;
		if ((value = getProperty(name, null, debug)) != null && !value.isEmpty())
		{
			final String[] values = value.split(delimiter);
			val = new boolean[values.length];
			for (int i = 0; i < val.length; i++)
			{
				val[i] = parseBoolean(values[i]);
			}
		}
		return val;
	}
	
	public int[] getIntProperty(final String name, String defaultValue, final String delimiter, boolean debug)
	{
		int[] val = null;
		final String value;
		if ((value = getProperty(name, null, debug)) != null && !value.isEmpty())
		{
			final String[] values = value.split(delimiter);
			val = new int[values.length];
			for (int i = 0; i < val.length; i++)
			{
				val[i] = Integer.parseInt(values[i]);
			}
		}
		return val;
	}
	
	public int[][] getDoubleIntProperty(final String name, String defaultValue, final String delimiter, boolean debug)
	{
		int[][] result = null;
		final String value;
		if ((value = getProperty(name, defaultValue, debug)) != null && !value.isEmpty())
		{
			int i = 0;
			String[] valueSplit;
			final String[] values = value.split(delimiter);
			result = new int[values.length][];
			for (final String val : values)
			{
				valueSplit = val.split(",");
				if (valueSplit.length != 2)
				{
					_log.warn("[" + name + "]: invalid config property -> " + valueSplit[0] + ", should be int,int");
					return null;
				}
				
				result[i] = new int[2];
				try
				{
					result[i][0] = Integer.parseInt(valueSplit[0]);
				}
				catch (final NumberFormatException _)
				{
					_log.warn("[" + name + "]: invalid number config property -> " + valueSplit[0] + "");
					return null;
				}
				try
				{
					result[i][1] = Integer.parseInt(valueSplit[1]);
				}
				catch (final NumberFormatException _)
				{
					_log.warn("[" + name + "]: invalid number config property -> " + valueSplit[1] + "");
					return null;
				}
				i++;
			}
		}
		return result;
	}
	
	public long[] getLongProperty(final String name, String defaultValue, final String delimiter, boolean debug)
	{
		long[] val = null;
		final String value;
		if ((value = getProperty(name, defaultValue, debug)) != null && !value.isEmpty())
		{
			final String[] values = value.split(delimiter);
			val = new long[values.length];
			for (int i = 0; i < val.length; i++)
			{
				val[i] = Long.parseLong(values[i]);
			}
		}
		return val;
	}
	
	public List<String> getListProperty(String name, String defaultValue, String delimiter, boolean debug)
	{
		String value = null;
		if ((value = getProperty(name, defaultValue, debug)) != null && !value.isEmpty())
		{
			final String[] values = value.split(delimiter);
			final List<String> val = new ArrayList<>(values.length);
			for (final String i : values)
			{
				val.add(i);
			}
			return val;
		}
		return Collections.emptyList();
	}
	
	public Set<String> getSetProperty(String name, String defaultValue, String delimiter, boolean debug)
	{
		String value = null;
		if ((value = getProperty(name, defaultValue, debug)) != null && !value.isEmpty())
		{
			final String[] values = value.split(delimiter);
			final Set<String> val = new HashSet<>(values.length);
			for (final String i : values)
			{
				val.add(i);
			}
			return val;
		}
		return Collections.emptySet();
	}
	
	public List<Integer> getIntegerProperty(String name, String defaultValue, String delimiter, boolean debug)
	{
		String value = null;
		if ((value = getProperty(name, defaultValue, debug)) != null && !value.isEmpty())
		{
			final String[] values = value.split(delimiter);
			final List<Integer> val = new ArrayList<>(values.length);
			for (final String i : values)
			{
				val.add(Integer.parseInt(i));
			}
			return val;
		}
		return Collections.emptyList();
	}
	
	public Map<Integer, Integer> getMapProperty(String name, String defaultValue, String delimiter, boolean debug)
	{
		String value = null;
		if ((value = getProperty(name, defaultValue, debug)) != null && !value.isEmpty())
		{
			final String[] values = value.split(delimiter);
			final Map<Integer, Integer> val = new HashMap<>(values.length);
			for (final String i : values)
			{
				final String[] split = i.split(",");
				if (split.length != 2)
				{
					_log.warn("[" + name + "]: invalid config property -> " + i + "");
				}
				else
				{
					try
					{
						val.put(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
					}
					catch (final NumberFormatException _)
					{
						if (!i.isEmpty())
						{
							_log.warn("[" + name + "]: invalid number config property -> " + split[0] + " " + split[1] + "");
						}
					}
				}
			}
			return val;
		}
		return Collections.emptyMap();
	}
	
	public Map<String, Integer> getStringMapProperty(String name, String defaultValue, String delimiter, boolean debug)
	{
		String value = null;
		if ((value = getProperty(name, defaultValue, debug)) != null && !value.isEmpty())
		{
			final String[] values = value.split(delimiter);
			final Map<String, Integer> val = new HashMap<>(values.length);
			for (final String i : values)
			{
				final String[] split = i.split(",");
				if (split.length != 2)
				{
					_log.warn("[" + name + "]: invalid config property -> " + i + "");
				}
				else
				{
					try
					{
						val.put(split[0], Integer.parseInt(split[1]));
					}
					catch (final NumberFormatException _)
					{
						if (!i.isEmpty())
						{
							_log.warn("[" + name + "]: invalid number config property -> " + split[0] + " " + split[1] + "");
						}
					}
				}
			}
			return val;
		}
		return Collections.emptyMap();
	}
	
	public Map<Integer, String> getMapStringProperty(String name, String defaultValue, String delimiter, boolean debug)
	{
		String value = null;
		if ((value = getProperty(name, defaultValue, debug)) != null && !value.isEmpty())
		{
			final String[] values = value.split(delimiter);
			final Map<Integer, String> val = new HashMap<>(values.length);
			for (final String i : values)
			{
				final String[] split = i.split(",");
				if (split.length != 2)
				{
					_log.warn("[" + name + "]: invalid config property -> " + name + " " + i + "");
				}
				else
				{
					try
					{
						val.put(Integer.parseInt(split[0]), split[1]);
					}
					catch (final NumberFormatException _)
					{
						if (!i.isEmpty())
						{
							_log.warn("[" + name + "]: invalid config property -> " + split[0] + " " + split[1] + "");
						}
					}
				}
			}
			return val;
		}
		return Collections.emptyMap();
	}
	
	public List<ItemHolder> parseItemsList(String name, String defaultValue, String delimiter, boolean debug)
	{
		String values = null;
		if ((values = getProperty(name, defaultValue, debug)) != null && !values.isEmpty())
		{
			final String[] propertySplit = values.split(delimiter);
			if (values.equalsIgnoreCase("none") || (propertySplit.length == 0))
			{
				return Collections.emptyList();
			}
			
			String[] valueSplit;
			final List<ItemHolder> result = new ArrayList<>(propertySplit.length);
			for (final String value : propertySplit)
			{
				valueSplit = value.split(",");
				if (valueSplit.length != 2)
				{
					_log.warn("parseItemsList[Config.load()]: invalid entry -> " + valueSplit[0] + ", should be itemId,itemNumber. Skipping to the next entry in the list.");
					continue;
				}
				
				int itemId = -1;
				try
				{
					itemId = Integer.parseInt(valueSplit[0]);
				}
				catch (final NumberFormatException _)
				{
					_log.warn("parseItemsList[Config.load()]: invalid itemId -> " + valueSplit[0] + ", value must be an integer. Skipping to the next entry in the list.");
					continue;
				}
				int count = -1;
				try
				{
					count = Integer.parseInt(valueSplit[1]);
				}
				catch (final NumberFormatException _)
				{
					_log.warn("parseItemsList[Config.load()]: invalid item number -> " + valueSplit[1] + ", value must be an integer. Skipping to the next entry in the list.");
					continue;
				}
				if ((itemId > 0) && (count > 0))
				{
					result.add(new ItemHolder(itemId, count));
				}
			}
			return result;
		}
		return Collections.emptyList();
	}
}