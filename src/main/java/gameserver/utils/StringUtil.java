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

import gameserver.Config;

public final class StringUtil
{
	public static String concat(final String... strings)
	{
		final StringBuilder sbString = new StringBuilder();
		for (final String string : strings)
		{
			sbString.append(string);
		}
		return sbString.toString();
	}
	
	public static StringBuilder startAppend(final int sizeHint, final String... strings)
	{
		final int length = getLength(strings);
		final StringBuilder sbString = new StringBuilder(sizeHint > length ? sizeHint : length);
		
		for (final String string : strings)
		{
			sbString.append(string);
		}
		
		return sbString;
	}
	
	public static void append(StringBuilder sb, Object... content)
	{
		for (final Object obj : content)
		{
			sb.append((obj == null) ? null : obj.toString());
		}
	}
	
	public static void append(final StringBuilder sbString, final String... strings)
	{
		sbString.ensureCapacity(sbString.length() + getLength(strings));
		
		for (final String string : strings)
		{
			sbString.append(string);
		}
	}
	
	private static int getLength(final String[] strings)
	{
		int length = 0;
		
		for (final String string : strings)
		{
			if (string == null)
			{
				length += 4;
			}
			else
			{
				length += string.length();
			}
		}
		
		return length;
	}
	
	public static String getTraceString(StackTraceElement[] trace)
	{
		final StringBuilder sbString = new StringBuilder();
		for (final StackTraceElement element : trace)
		{
			sbString.append(element.toString()).append(Config.EOL);
		}
		return sbString.toString();
	}
	
	public static String substringBetween(String str, String open, String close)
	{
		final int INDEX_NOT_FOUND = -1;
		if (str == null || open == null || close == null)
		{
			return null;
		}
		final int start = str.indexOf(open);
		if (start != INDEX_NOT_FOUND)
		{
			final int end = str.indexOf(close, start + open.length());
			if (end != INDEX_NOT_FOUND)
			{
				return str.substring(start + open.length(), end);
			}
		}
		return null;
	}
}