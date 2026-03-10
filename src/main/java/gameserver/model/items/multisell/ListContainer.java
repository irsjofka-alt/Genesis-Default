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
package gameserver.model.items.multisell;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import l2e.commons.util.Rnd;

public class ListContainer
{
	protected int _listId;
	protected boolean _applyTaxes = false;
	protected boolean _isChance = false;
	protected boolean _isSkillProducts = false;
	protected boolean _maintainEnchantment = false;
	protected boolean _allowCheckEquipItems = false;
	protected double _useRate = 1.0;

	protected List<Entry> _entries = new ArrayList<>();
	protected Set<Integer> _npcsAllowed = null;
	protected Set<Integer> _itemAllowed = null;

	public ListContainer(int listId)
	{
		_listId = listId;
	}

	public final List<Entry> getEntries()
	{
		return _entries;
	}

	public final int getListId()
	{
		return _listId;
	}

	public final void setApplyTaxes(boolean applyTaxes)
	{
		_applyTaxes = applyTaxes;
	}

	public final boolean getApplyTaxes()
	{
		return _applyTaxes;
	}

	public final void setMaintainEnchantment(boolean maintainEnchantment)
	{
		_maintainEnchantment = maintainEnchantment;
	}
	
	public final void setIsAllowCheckEquipItems(boolean val)
	{
		_allowCheckEquipItems = val;
	}

	public double getUseRate()
	{
		return _useRate;
	}
	
	public void setUseRate(double rate)
	{
		_useRate = rate;
	}
	
	public final boolean getMaintainEnchantment()
	{
		return _maintainEnchantment;
	}
	
	public final boolean isAllowCheckEquipItems()
	{
		return _allowCheckEquipItems;
	}
	
	public void allowNpc(int npcId)
	{
		if (_npcsAllowed == null)
		{
			_npcsAllowed = new HashSet<>();
		}
		_npcsAllowed.add(npcId);
	}

	public boolean isNpcAllowed(int npcId)
	{
		return _npcsAllowed != null && _npcsAllowed.contains(npcId);
	}

	public boolean isNpcOnly()
	{
		return _npcsAllowed != null;
	}
	
	public void allowItem(int itemId)
	{
		if (_itemAllowed == null)
		{
			_itemAllowed = new HashSet<>();
		}
		_itemAllowed.add(itemId);
	}
	
	public boolean isItemAllowed(int itemId)
	{
		return _itemAllowed != null && _itemAllowed.contains(itemId);
	}
	
	public Set<Integer> getItemsAllowed()
	{
		return _itemAllowed != null ? _itemAllowed : Collections.emptySet();
	}
	
	public boolean isItemOnly()
	{
		return _itemAllowed != null;
	}
	
	public void setIsChanceMultiSell(boolean val)
	{
		_isChance = val;
	}
	
	public boolean isChanceMultiSell()
	{
		return _isChance;
	}
	
	public void setIsSkillProducts(boolean val)
	{
		_isSkillProducts = val;
	}
	
	public boolean isSkillProducts()
	{
		return _isSkillProducts;
	}
	
	public Ingredient getRandomHolder(List<Ingredient> holders)
	{
		double itemRandom = 100 * Rnd.nextDouble();
		for (final Ingredient holder : holders)
		{
			if (!Double.isNaN(holder.getChance()))
			{
				if (holder.getChance() > itemRandom)
				{
					return holder;
				}
				itemRandom -= holder.getChance();
			}
		}
		return null;
	}
}