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

public final class FloodProtectorConfig
{
	public String FLOOD_PROTECTOR_TYPE;
	public int FLOOD_PROTECTION_INTERVAL;
	public boolean LOG_FLOODING;
	public int PUNISHMENT_LIMIT;
	public String PUNISHMENT_TYPE;
	public long PUNISHMENT_TIME;

	public FloodProtectorConfig(final String floodProtectorType)
	{
		FLOOD_PROTECTOR_TYPE = floodProtectorType;
	}
	
	public static FloodProtectorConfig load(final String type, final GameSettings properties)
	{
		final var config = new FloodProtectorConfig(type.toUpperCase());
		config.FLOOD_PROTECTION_INTERVAL = properties.getProperty(type + "_FLOOD_PROTECTION_INTERVAL", 1000, false);
		config.LOG_FLOODING = properties.getProperty(type + "_LOG_FLOODING", false, false);
		config.PUNISHMENT_LIMIT = properties.getProperty(type + "_PUNISHMENT_LIMIT", 1, false);
		config.PUNISHMENT_TYPE = properties.getProperty(type + "_PUNISHMENT_TYPE", "none", false);
		config.PUNISHMENT_TIME = properties.getProperty(type + "_PUNISHMENT_TIME", 0, false) * 60000;
		return config;
	}
}