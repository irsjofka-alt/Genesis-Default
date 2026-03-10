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
package gameserver.database;

import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import l2e.commons.log.LoggerObject;
import gameserver.Config;

public class DatabaseFactory extends LoggerObject
{
	private HikariDataSource _source;
	
	public DatabaseFactory()
	{
		try
		{
			final var config = new HikariConfig();
			config.setDriverClassName(Config.DATABASE_DRIVER);
			config.setJdbcUrl(Config.DATABASE_URL);
			config.setUsername(Config.DATABASE_LOGIN);
			config.setPassword(Config.DATABASE_PASSWORD);
			
			config.setMaximumPoolSize(Config.DATABASE_MAX_CONNECTIONS);
			config.setMinimumIdle(10);
			
			config.setIdleTimeout(Config.DATABASE_MAX_IDLE_TIMEOUT * 1000L);
			config.setMaxLifetime(Config.DATABASE_CONNECTION_LIFE_TIME * 1000L);
			config.setConnectionTimeout(Config.DATABASE_CONNECTION_TIMEOUT * 1000L);
			
			config.setLeakDetectionThreshold(30000);
			config.setValidationTimeout(5000);
			config.setInitializationFailTimeout(-1);
			config.setRegisterMbeans(true);
			
			config.setPoolName("l2eternityPool");
			
			_source = new HikariDataSource(config);
			
			info("Pool initialized successfully.");
		}
		catch (final Exception e)
		{
			error("Problem with database connector initialize...", e);
		}
	}
	
	public Connection getConnection()
	{
		Connection con = null;
		try
		{
			con = _source.getConnection();
		}
		catch (final SQLException e)
		{
			error("Could not get a connection.", e);
		}
		return con;
	}
	
	public synchronized void close()
	{
		if ((_source != null) && !_source.isClosed())
		{
			try
			{
				_source.close();
				info("Connection pool closed successfully.");
			}
			catch (final Exception e)
			{
				error("There was a problem closing the data source.", e);
			}
		}
	}
	
	public static DatabaseFactory getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final DatabaseFactory INSTANCE = new DatabaseFactory();
	}
}