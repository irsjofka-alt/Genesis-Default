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
package gameserver.utils.comparators;

import java.math.BigDecimal;
import java.util.Comparator;

import gameserver.model.actor.Player;
import gameserver.model.reward.CalculateRewardChances;
import gameserver.model.reward.CalculateRewardChances.NpcTemplateDrops;

public class ItemChanceComparator implements Comparator<NpcTemplateDrops>
{
	private final int _itemId;
	private final Player _player;
	
	public ItemChanceComparator(Player player, int itemId)
	{
		_itemId = itemId;
		_player = player;
	}
	
	@Override
	public int compare(NpcTemplateDrops o1, NpcTemplateDrops o2)
	{
		final BigDecimal maxDrop1 = BigDecimal.valueOf(CalculateRewardChances.getAmountAndChanceById(_player, o1._template, 1, o1._dropNoSpoil, _itemId, null)[1]);
		final BigDecimal maxDrop2 = BigDecimal.valueOf(CalculateRewardChances.getAmountAndChanceById(_player, o2._template, 1, o2._dropNoSpoil, _itemId, null)[1]);
		final BigDecimal chance1 = new BigDecimal(CalculateRewardChances.getAmountAndChanceById(_player, o1._template, 1, o1._dropNoSpoil, _itemId, null)[2]);
		final BigDecimal chance2 = new BigDecimal(CalculateRewardChances.getAmountAndChanceById(_player, o2._template, 1, o2._dropNoSpoil, _itemId, null)[2]);
		
		final int compare = chance2.multiply(maxDrop2).compareTo(chance1.multiply(maxDrop1));
		if (compare == 0)
		{
			return (o2._template.getName(_player.getLang()).compareTo(o1._template.getName(_player.getLang())));
		}
		return compare;
	}
}