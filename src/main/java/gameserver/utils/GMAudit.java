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
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gameserver.Config;

public class GMAudit
{
	private static final Logger _log = LogManager.getLogger(GMAudit.class);

	static
	{
		new File("log/GMAudit").mkdirs();
	}
	
	public static void auditGMAction(String gmName, String action, String target, String params)
	{
		final SimpleDateFormat _formatter = new SimpleDateFormat("dd/MM/yyyy H:mm:ss");
		final String date = _formatter.format(new Date());
		String name = Util.replaceIllegalCharacters(gmName);
		if (!Util.isValidFileName(name))
		{
			name = "INVALID_GM_NAME_" + date;
		}

		final File file = new File("log/GMAudit/" + name + ".txt");
		try (
		    FileWriter save = new FileWriter(file, true))
		{
			save.write(date + ">" + gmName + ">" + action + ">" + target + ">" + params + Config.EOL);
		}
		catch (final IOException e)
		{
			_log.error("GMAudit for GM " + gmName + " could not be saved: ", e);
		}
	}

	public static void auditGMAction(String gmName, String action, String target)
	{
		auditGMAction(gmName, action, target, "");
	}
}