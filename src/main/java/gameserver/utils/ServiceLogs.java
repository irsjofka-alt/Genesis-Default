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
import java.io.FileWriter;
import java.io.IOException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import gameserver.Config;

public final class ServiceLogs
{
	private static final Logger _log = LogManager.getLogger(ServiceLogs.class);

	public static void addServiceLogs(String message)
	{
		if (message == null || message.isEmpty())
		{
			return;
		}
		
		message = message + Config.EOL;

		final File file = new File("log/game/serviceLogs.txt");
		try (
		    FileWriter save = new FileWriter(file, true))
		{
			save.write(message);
		}
		catch (final IOException e)
		{
			_log.error("ServiceLogs could not be saved: ", e);
		}
	}
}