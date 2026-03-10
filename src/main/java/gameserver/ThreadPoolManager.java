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
package gameserver;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadFactory;

import l2e.commons.log.LoggerObject;
import l2e.commons.threading.RunnableWrapper;

public class ThreadPoolManager extends LoggerObject
{
	private static final long MAX_DELAY = TimeUnit.NANOSECONDS.toMillis(Long.MAX_VALUE - System.nanoTime()) / 2;
	
	private static int _threadPoolRandomizer;
	
	protected ScheduledExecutorService[] _scheduledExecutor;
	protected ExecutorService[] _executor;
	
	private boolean _shutdown;
	
	private ThreadPoolManager()
	{
		// Use Project Loom virtual threads for Java 25
		int poolCount = Config.SCHEDULED_THREAD_POOL_SIZE;
		_scheduledExecutor = new ScheduledExecutorService[poolCount];
		for (int i = 0; i < poolCount; i++)
		{
			// Create virtual thread scheduler
			_scheduledExecutor[i] = Executors.newScheduledThreadPool(4, Thread.ofVirtual().factory());
		}
		
		poolCount = Config.EXECUTOR_THREAD_POOL_SIZE;
		_executor = new ExecutorService[poolCount];
		for (int i = 0; i < poolCount; i++)
		{
			// Create virtual thread executor
			_executor[i] = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().factory());
		}
		
		for (final var threadPool : _scheduledExecutor)
		{
			if (threadPool instanceof ScheduledThreadPoolExecutor stpe)
			{
				stpe.setRemoveOnCancelPolicy(true);
				stpe.prestartAllCoreThreads();
			}
		}
		
		scheduleAtFixedRate(new Runnable()
		{
			@Override
			public void run()
			{
				for (final var threadPool : _scheduledExecutor)
				{
					if (threadPool instanceof ScheduledThreadPoolExecutor stpe)
					{
						stpe.purge();
					}
				}
			}
		}, 60000L, 60000L);
	}

	public boolean isShutdown()
	{
		return _shutdown;
	}
	
	private <T> T getPool(T[] threadPools)
	{
		return threadPools[_threadPoolRandomizer++ % threadPools.length];
	}
	
	private long validate(long delay)
	{
		return Math.max(0, Math.min(MAX_DELAY, delay));
	}
	
	public ScheduledFuture<?> schedule(Runnable r, long delay, TimeUnit timeUnit)
	{
		try
		{
			return getPool(_scheduledExecutor).schedule(new RunnableWrapper(r), validate(delay), timeUnit);
		}
		catch (final RejectedExecutionException _)
		{
			return null;
		}
	}
	
	public ScheduledFuture<?> schedule(Runnable r, long delay)
	{
		return schedule(r, delay, TimeUnit.MILLISECONDS);
	}
	
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable r, long initial, long delay, TimeUnit timeUnit)
	{
		try
		{
			return getPool(_scheduledExecutor).scheduleAtFixedRate(new RunnableWrapper(r), validate(initial), validate(delay), timeUnit);
		}
		catch (final RejectedExecutionException _)
		{
			return null;
		}
	}

	public ScheduledFuture<?> scheduleAtFixedRate(Runnable r, long initial, long delay)
	{
		return scheduleAtFixedRate(r, initial, delay, TimeUnit.MILLISECONDS);
	}
	
	public ScheduledFuture<?> scheduleAtFixedDelay(Runnable r, long initial, long delay, TimeUnit timeUnit)
	{
		try
		{
			return getPool(_scheduledExecutor).scheduleWithFixedDelay(new RunnableWrapper(r), validate(initial), validate(delay), timeUnit);
		}
		catch (final RejectedExecutionException _)
		{
			return null;
		}
	}

	public ScheduledFuture<?> scheduleAtFixedDelay(Runnable r, long initial, long delay)
	{
		return scheduleAtFixedDelay(r, initial, delay, TimeUnit.MILLISECONDS);
	}
	
	public void execute(Runnable r)
	{
		try
		{
			getPool(_executor).execute(new RunnableWrapper(r));
		}
		catch (final RejectedExecutionException _)
		{
		}
	}

	public void shutdown()
	{
		_shutdown = true;
		try
		{
			for (final var threadPool : _scheduledExecutor)
			{
				threadPool.shutdownNow();
			}
			
			for (final var threadPool : _executor)
			{
				threadPool.shutdownNow();
			}
		}
		catch (final Throwable _)
		{
		}
	}
	
	public void getInfo()
	{
		for (int i = 0; i < _scheduledExecutor.length; i++)
		{
			final var threadPool = _scheduledExecutor[i];
			info("=================================================");
			info("ScheduledPool #" + i + ":");
			if (threadPool instanceof ScheduledThreadPoolExecutor stpe)
			{
				info("getActiveCount: ...... " + stpe.getActiveCount());
				info("getCorePoolSize: ..... " + stpe.getCorePoolSize());
				info("getPoolSize: ......... " + stpe.getPoolSize());
				info("getLargestPoolSize: .. " + stpe.getLargestPoolSize());
				info("getMaximumPoolSize: .. " + stpe.getMaximumPoolSize());
				info("getCompletedTaskCount: " + stpe.getCompletedTaskCount());
				info("getQueuedTaskCount: .. " + stpe.getQueue().size());
				info("getTaskCount: ........ " + stpe.getTaskCount());
			}
			else
			{
				info("Virtual Thread Scheduler (Project Loom)");
			}
		}
		
		for (int i = 0; i < _executor.length; i++)
		{
			final var threadPool = _executor[i];
			info("=================================================");
			info("ExecutorPool #" + i + ":");
			if (threadPool instanceof ThreadPoolExecutor tpe)
			{
				info("getActiveCount: ...... " + tpe.getActiveCount());
				info("getCorePoolSize: ..... " + tpe.getCorePoolSize());
				info("getPoolSize: ......... " + tpe.getPoolSize());
				info("getLargestPoolSize: .. " + tpe.getLargestPoolSize());
				info("getMaximumPoolSize: .. " + tpe.getMaximumPoolSize());
				info("getCompletedTaskCount: " + tpe.getCompletedTaskCount());
				info("getQueuedTaskCount: .. " + tpe.getQueue().size());
				info("getTaskCount: ........ " + tpe.getTaskCount());
			}
			else
			{
				info("Virtual Thread Executor (Project Loom)");
			}
		}
	}
	
	public static ThreadPoolManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ThreadPoolManager _instance = new ThreadPoolManager();
	}
}