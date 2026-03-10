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
package gameserver.model.skills.funcs;

import java.lang.reflect.Constructor;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import gameserver.model.skills.conditions.Condition;
import gameserver.model.stats.Env;
import gameserver.model.stats.Stats;

public final class FuncTemplate
{
	protected static final Logger _log = LogManager.getLogger(FuncTemplate.class);

	public Condition attachCond;
	public Condition applayCond;
	public final Class<?> func;
	public final Constructor<?> constructor;
	public final Stats stat;
	public final int order;
	public final Lambda lambda;

	public FuncTemplate(Condition pAttachCond, Condition pApplayCond, String pFunc, Stats pStat, int pOrder, Lambda pLambda)
	{
		attachCond = pAttachCond;
		applayCond = pApplayCond;
		stat = pStat;
		order = pOrder;
		lambda = pLambda;
		try
		{
			func = Class.forName("gameserver.model.skills.funcs.Func" + pFunc);
		}
		catch (final ClassNotFoundException e)
		{
			throw new RuntimeException(e);
		}
		try
		{
			constructor = func.getConstructor(new Class<?>[]
			{
			        Stats.class, Integer.TYPE, Object.class, Lambda.class
			});
		}
		catch (final NoSuchMethodException e)
		{
			throw new RuntimeException(e);
		}
	}

	public Func getFunc(Env env, Object owner)
	{
		if ((attachCond != null) && !attachCond.test(env))
		{
			return null;
		}
		try
		{
			final Func f = (Func) constructor.newInstance(stat, order, owner, lambda);
			if (applayCond != null)
			{
				f.setCondition(applayCond);
			}
			return f;
		}
		catch (final Exception e)
		{
			_log.warn("", e);
			return null;
		}
	}
}