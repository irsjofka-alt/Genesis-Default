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
 * this program. If not, see <http://l2jeternity.com/ >.
 */
package gameserver;

import org.apache.logging.log4j.Logger;

public class EternityWorld
{
	public static final int _revision = 2704;
	
	public static void getTeamInfo(Logger log)
	{
		final var revision = _revision;
		final var ver = Config.c ? "(Activated)" : "(Trial Version)";
		
		log.info("    Project Owner: ........... LordWinter");
		log.info("           Server: ........... High Five 5");
		log.info("         Revision: ........... " + revision);
		log.info("          License: ........... " + Config.USER_NAME + " " + ver + "");
		log.info("        Copyright: ........... 2010-2025 Eternity Project Team");
		log.info("Project Community: ........... www.l2jeternity.com");
	}
}