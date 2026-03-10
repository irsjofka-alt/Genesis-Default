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

import gameserver.data.parser.EnchantItemHPBonusParser;
import gameserver.model.items.instance.ItemInstance;
import gameserver.model.stats.Env;
import gameserver.model.stats.Stats;

public class FuncEnchantHp extends Func
{
	public FuncEnchantHp(Stats pStat, int pOrder, Object owner, Lambda lambda)
	{
		super(pStat, pOrder, owner);
	}

	@Override
	public void calc(Env env)
	{
		if ((cond != null) && !cond.test(env))
		{
			return;
		}
		
		final ItemInstance item = (ItemInstance) funcOwner;
		if (item.getEnchantLevel() > 0)
		{
			env.addValue(EnchantItemHPBonusParser.getInstance().getHPBonus(item));
		}
	}
}