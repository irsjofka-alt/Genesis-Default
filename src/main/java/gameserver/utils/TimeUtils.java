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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import l2e.commons.util.DeclensionKey;
import gameserver.model.actor.Player;

public class TimeUtils
{
	public static final long SECOND_IN_MILLIS = 1000L;
	public static final long MINUTE_IN_MILLIS = 60000L;
	public static final long HOUR_IN_MILLIS = 3600000L;
	public static final long DAY_IN_MILLIS = 86400000L;
	private static final SimpleDateFormat SIMPLE_FORMAT = new SimpleDateFormat("HH:mm dd.MM.yyyy");
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");

	public static long parse(String time) throws ParseException
	{
		return SIMPLE_FORMAT.parse(time).getTime();
	}
	
	public static String toSimpleFormat(Calendar cal)
	{
		return SIMPLE_FORMAT.format(cal.getTime());
	}

	public static String toSimpleFormat(long cal)
	{
		return SIMPLE_FORMAT.format(cal);
	}
	
	public static String toSimpleDateFormat(long cal)
	{
		return DATE_FORMAT.format(cal);
	}
	
	public static String convertDateToString(long time)
	{
		final Date dt = new Date(time);
		final String stringDate = SIMPLE_FORMAT.format(dt);
		return stringDate;
	}

	public static String minutesToFullString(int period)
	{
		final StringBuilder sb = new StringBuilder();

		if (period > 1440)
		{
			sb.append((period - (period % 1440)) / 1440).append(" days.");
			period = period % 1440;
		}

		if (period > 60)
		{
			if (sb.length() > 0)
			{
				sb.append(", ");
			}

			sb.append((period - (period % 60)) / 60).append(" hours.");

			period = period % 60;
		}

		if (period > 0)
		{
			if (sb.length() > 0)
			{
				sb.append(", ");
			}

			sb.append(period).append(" min.");
		}
		if (sb.length() < 1)
		{
			sb.append("less than 1 minute.");
		}

		return sb.toString();
	}

	public static long getMilisecondsToNextDay(List<Integer> days, int hourOfTheEvent)
	{
		return getMilisecondsToNextDay(days, hourOfTheEvent, 5);
	}

	public static long getMilisecondsToNextDay(List<Integer> days, int hourOfTheEvent, int minuteOfTheEvent)
	{
		final int[] hours = new int[days.size()];
		for (int i = 0; i < hours.length; i++)
		{
			hours[i] = days.get(i).intValue();
		}
		return getMilisecondsToNextDay(hours, hourOfTheEvent, minuteOfTheEvent);
	}

	public static long getMilisecondsToNextDay(int[] days, int hourOfTheEvent, int minuteOfTheEvent)
	{
		final Calendar tempCalendar = Calendar.getInstance();
		tempCalendar.set(Calendar.SECOND, 0);
		tempCalendar.set(Calendar.MILLISECOND, 0);
		tempCalendar.set(Calendar.HOUR_OF_DAY, hourOfTheEvent);
		tempCalendar.set(Calendar.MINUTE, minuteOfTheEvent);

		final long currentTime = System.currentTimeMillis();
		final Calendar eventCalendar = Calendar.getInstance();

		boolean found = false;
		long smallest = Long.MAX_VALUE;

		for (final int day : days)
		{
			tempCalendar.set(Calendar.DAY_OF_MONTH, day);
			final long timeInMillis = tempCalendar.getTimeInMillis();

			if (timeInMillis <= currentTime)
			{
				if (timeInMillis < smallest)
				{
					smallest = timeInMillis;
				}
				continue;
			}

			if (!found || timeInMillis < eventCalendar.getTimeInMillis())
			{
				found = true;
				eventCalendar.setTimeInMillis(timeInMillis);
			}
		}

		if (!found)
		{
			eventCalendar.setTimeInMillis(smallest);
			eventCalendar.add(Calendar.MONTH, 1);
		}
		return eventCalendar.getTimeInMillis() - currentTime;
	}
	
	public static long addDay(int count)
	{
		final long DAY = count * 60 * 60 * 24 * 1000L;
		return DAY;
	}
	
	public static long addHours(int count)
	{
		final long HOUR = count * 60 * 60 * 1000L;
		return HOUR;
	}
	
	public static long addMinutes(int count)
	{
		final long MINUTE = count * 60 * 1000L;
		return MINUTE;
	}
	
	public static long addSecond(int count)
	{
		final long SECONDS = count * 1000L;
		return SECONDS;
	}
	public static String formatTime(Player player, int time)
	{
		return formatTime(player, time, true);
	}
	
	public static String formatTime(Player player, int time, boolean cut)
	{
		int days = 0;
		int hours = 0;
		int minutes = 0;
		
		days = time / 86400;
		hours = (time - days * 24 * 3600) / 3600;
		minutes = (time - days * 24 * 3600 - hours * 3600) / 60;
		
		String result;
		
		if (days >= 1)
		{
			if ((hours < 1) || (cut))
			{
				result = days + " " + Util.declension(player, days, DeclensionKey.DAYS);
			}
			else
			{
				result = days + " " + Util.declension(player, days, DeclensionKey.DAYS) + " " + hours + " " + Util.declension(player, hours, DeclensionKey.HOUR);
			}
		}
		else
		{
			if (hours >= 1)
			{
				if ((minutes < 1) || (cut))
				{
					result = hours + " " + Util.declension(player, hours, DeclensionKey.HOUR);
				}
				else
				{
					result = hours + " " + Util.declension(player, hours, DeclensionKey.HOUR) + " " + minutes + " " + Util.declension(player, minutes, DeclensionKey.MINUTES);
				}
			}
			else
			{
				result = minutes + " " + Util.declension(player, minutes, DeclensionKey.MINUTES);
			}
		}
		return result;
	}
}