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
package gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gameserver.model.actor.Player;
import gameserver.model.actor.templates.items.Item;
import gameserver.model.actor.templates.items.WarehouseItem;
import gameserver.model.items.instance.ItemInstance;
import gameserver.model.items.type.EtcItemType;
import gameserver.network.ServerPacketOpcodes;
import gameserver.utils.comparators.WarehouseItemBodypartComparator;
import gameserver.utils.comparators.WarehouseItemGradeComparator;
import gameserver.utils.comparators.WarehouseItemNameComparator;
import gameserver.utils.comparators.WarehouseItemRecipeComparator;
import gameserver.utils.comparators.WarehouseItemTypeComparator;

public class SortedWareHouseWithdrawalList extends GameServerPacket
{
	@Override
	protected ServerPacketOpcodes getOpcodes()
	{
		return ServerPacketOpcodes.WareHouseWithdrawList;
	}
	
	public static final int PRIVATE = 1;
	public static final int CLAN = 2;
	public static final int CASTLE = 3;
	public static final int FREIGHT = 4;
	private Player _activeChar;
	private long _playerAdena;
	private List<WarehouseItem> _objects = new ArrayList<>();
	private int _whType;
	private byte _sortorder;
	private WarehouseListType _itemtype;

	public static enum WarehouseListType
	{
		WEAPON, ARMOR, ETCITEM, MATERIAL, RECIPE, AMULETT, SPELLBOOK, SHOT, SCROLL, CONSUMABLE, SEED, POTION, QUEST, PET, OTHER, ALL
	}

	public static final byte A2Z = 1;
	public static final byte Z2A = -1;
	public static final byte GRADE = 2;
	public static final byte LEVEL = 3;
	public static final byte TYPE = 4;
	public static final byte WEAR = 5;
	public static final int MAX_SORT_LIST_ITEMS = 300;

	public SortedWareHouseWithdrawalList(Player player, int type, WarehouseListType itemtype, byte sortorder)
	{
		_activeChar = player;
		_whType = type;
		_itemtype = itemtype;
		_sortorder = sortorder;

		_playerAdena = _activeChar.getAdena();
		if (_activeChar.getActiveWarehouse() == null)
		{
			_log.warn("error while sending withdraw request to: " + _activeChar.getName(null));
			return;
		}

		switch (_itemtype)
		{
			case WEAPON :
				_objects = createWeaponList(_activeChar.getActiveWarehouse().getItems());
				break;
			case ARMOR :
				_objects = createArmorList(_activeChar.getActiveWarehouse().getItems());
				break;
			case ETCITEM :
				_objects = createEtcItemList(_activeChar.getActiveWarehouse().getItems());
				break;
			case MATERIAL :
				_objects = createMatList(_activeChar.getActiveWarehouse().getItems());
				break;
			case RECIPE :
				_objects = createRecipeList(_activeChar.getActiveWarehouse().getItems());
				break;
			case AMULETT :
				_objects = createAmulettList(_activeChar.getActiveWarehouse().getItems());
				break;
			case SPELLBOOK :
				_objects = createSpellbookList(_activeChar.getActiveWarehouse().getItems());
				break;
			case CONSUMABLE :
				_objects = createConsumableList(_activeChar.getActiveWarehouse().getItems());
				break;
			case SHOT :
				_objects = createShotList(_activeChar.getActiveWarehouse().getItems());
				break;
			case SCROLL :
				_objects = createScrollList(_activeChar.getActiveWarehouse().getItems());
				break;
			case SEED :
				_objects = createSeedList(_activeChar.getActiveWarehouse().getItems());
				break;
			case OTHER :
				_objects = createOtherList(_activeChar.getActiveWarehouse().getItems());
				break;
			case ALL :
			default :
				_objects = createAllList(_activeChar.getActiveWarehouse().getItems());
				break;
		}

		try
		{
			switch (_sortorder)
			{
				case A2Z :
				case Z2A :
					Collections.sort(_objects, new WarehouseItemNameComparator(_sortorder));
					break;
				case GRADE :
					if ((_itemtype == WarehouseListType.ARMOR) || (_itemtype == WarehouseListType.WEAPON))
					{
						Collections.sort(_objects, new WarehouseItemNameComparator(A2Z));
						Collections.sort(_objects, new WarehouseItemGradeComparator(A2Z));
					}
					break;
				case LEVEL :
					if (_itemtype == WarehouseListType.RECIPE)
					{
						Collections.sort(_objects, new WarehouseItemNameComparator(A2Z));
						Collections.sort(_objects, new WarehouseItemRecipeComparator(A2Z));
					}
					break;
				case TYPE :
					if (_itemtype == WarehouseListType.MATERIAL)
					{
						Collections.sort(_objects, new WarehouseItemNameComparator(A2Z));
						Collections.sort(_objects, new WarehouseItemTypeComparator(A2Z));
					}
					break;
				case WEAR :
					if (_itemtype == WarehouseListType.ARMOR)
					{
						Collections.sort(_objects, new WarehouseItemNameComparator(A2Z));
						Collections.sort(_objects, new WarehouseItemBodypartComparator(A2Z));
					}
					break;
			}
		}
		catch (final Exception e)
		{}
	}

	public static byte getOrder(String order)
	{
		if (order == null)
		{
			return A2Z;
		}
		else if (order.startsWith("A2Z"))
		{
			return A2Z;
		}
		else if (order.startsWith("Z2A"))
		{
			return Z2A;
		}
		else if (order.startsWith("GRADE"))
		{
			return GRADE;
		}
		else if (order.startsWith("TYPE"))
		{
			return TYPE;
		}
		else if (order.startsWith("WEAR"))
		{
			return WEAR;
		}
		else
		{
			try
			{
				return Byte.parseByte(order);
			}
			catch (final NumberFormatException ex)
			{
				return A2Z;
			}
		}
	}

	private List<WarehouseItem> createWeaponList(ItemInstance[] _items)
	{
		final List<WarehouseItem> _list = new ArrayList<>();
		for (final ItemInstance item : _items)
		{
			if (item.isWeapon() || (item.getItem().getType2() == Item.TYPE2_WEAPON) || (item.isEtcItem() && (item.getItemType() == EtcItemType.ARROW)) || (item.getItem().getType2() == Item.TYPE2_MONEY))
			{
				if (_list.size() < MAX_SORT_LIST_ITEMS)
				{
					_list.add(new WarehouseItem(item));
				}
				else
				{
					continue;
				}
			}
		}
		return _list;
	}

	private List<WarehouseItem> createArmorList(ItemInstance[] _items)
	{
		final List<WarehouseItem> _list = new ArrayList<>();
		for (final ItemInstance item : _items)
		{
			if (item.isArmor() || (item.getItem().getType2() == Item.TYPE2_MONEY))
			{
				if (_list.size() < MAX_SORT_LIST_ITEMS)
				{
					_list.add(new WarehouseItem(item));
				}
				else
				{
					continue;
				}
			}
		}
		return _list;
	}

	private List<WarehouseItem> createEtcItemList(ItemInstance[] _items)
	{
		final List<WarehouseItem> _list = new ArrayList<>();
		for (final ItemInstance item : _items)
		{
			if (item.isEtcItem() || (item.getItem().getType2() == Item.TYPE2_MONEY))
			{
				if (_list.size() < MAX_SORT_LIST_ITEMS)
				{
					_list.add(new WarehouseItem(item));
				}
				else
				{
					continue;
				}
			}
		}
		return _list;
	}

	private List<WarehouseItem> createMatList(ItemInstance[] _items)
	{
		final List<WarehouseItem> _list = new ArrayList<>();
		for (final ItemInstance item : _items)
		{
			if ((item.isEtcItem() && (item.getEtcItem().getItemType() == EtcItemType.MATERIAL)) || (item.getItem().getType2() == Item.TYPE2_MONEY))
			{
				if (_list.size() < MAX_SORT_LIST_ITEMS)
				{
					_list.add(new WarehouseItem(item));
				}
				else
				{
					continue;
				}
			}
		}
		return _list;
	}

	private List<WarehouseItem> createRecipeList(ItemInstance[] _items)
	{
		final List<WarehouseItem> _list = new ArrayList<>();
		for (final ItemInstance item : _items)
		{
			if ((item.isEtcItem() && (item.getEtcItem().getItemType() == EtcItemType.RECIPE)) || (item.getItem().getType2() == Item.TYPE2_MONEY))
			{
				if (_list.size() < MAX_SORT_LIST_ITEMS)
				{
					_list.add(new WarehouseItem(item));
				}
				else
				{
					continue;
				}
			}
		}
		return _list;
	}

	private List<WarehouseItem> createAmulettList(ItemInstance[] _items)
	{
		final List<WarehouseItem> _list = new ArrayList<>();
		for (final ItemInstance item : _items)
		{
			if ((item.isEtcItem() && (item.getItem().getName("en").toUpperCase().startsWith("AMULET"))) || (item.getItem().getType2() == Item.TYPE2_MONEY))
			{
				if (_list.size() < MAX_SORT_LIST_ITEMS)
				{
					_list.add(new WarehouseItem(item));
				}
				else
				{
					continue;
				}
			}
		}
		return _list;
	}

	private List<WarehouseItem> createSpellbookList(ItemInstance[] _items)
	{
		final List<WarehouseItem> _list = new ArrayList<>();
		for (final ItemInstance item : _items)
		{
			if ((item.isEtcItem() && (!item.getItem().getName("en").toUpperCase().startsWith("AMULET"))) || (item.getItem().getType2() == Item.TYPE2_MONEY))
			{
				if (_list.size() < MAX_SORT_LIST_ITEMS)
				{
					_list.add(new WarehouseItem(item));
				}
				else
				{
					continue;
				}
			}
		}
		return _list;
	}

	private List<WarehouseItem> createConsumableList(ItemInstance[] _items)
	{
		final List<WarehouseItem> _list = new ArrayList<>();
		for (final ItemInstance item : _items)
		{
			if ((item.isEtcItem() && ((item.getEtcItem().getItemType() == EtcItemType.SCROLL) || (item.getEtcItem().getItemType() == EtcItemType.SHOT))) || (item.getItem().getType2() == Item.TYPE2_MONEY))
			{
				if (_list.size() < MAX_SORT_LIST_ITEMS)
				{
					_list.add(new WarehouseItem(item));
				}
				else
				{
					continue;
				}
			}
		}
		return _list;
	}

	private List<WarehouseItem> createShotList(ItemInstance[] _items)
	{
		final List<WarehouseItem> _list = new ArrayList<>();
		for (final ItemInstance item : _items)
		{
			if ((item.isEtcItem() && (item.getEtcItem().getItemType() == EtcItemType.SHOT)) || (item.getItem().getType2() == Item.TYPE2_MONEY))
			{
				if (_list.size() < MAX_SORT_LIST_ITEMS)
				{
					_list.add(new WarehouseItem(item));
				}
				else
				{
					continue;
				}
			}
		}
		return _list;
	}

	private List<WarehouseItem> createScrollList(ItemInstance[] _items)
	{
		final List<WarehouseItem> _list = new ArrayList<>();
		for (final ItemInstance item : _items)
		{
			if ((item.isEtcItem() && (item.getEtcItem().getItemType() == EtcItemType.SCROLL)) || (item.getItem().getType2() == Item.TYPE2_MONEY))
			{
				if (_list.size() < MAX_SORT_LIST_ITEMS)
				{
					_list.add(new WarehouseItem(item));
				}
				else
				{
					continue;
				}
			}
		}
		return _list;
	}

	private List<WarehouseItem> createSeedList(ItemInstance[] _items)
	{
		final List<WarehouseItem> _list = new ArrayList<>();
		for (final ItemInstance item : _items)
		{
			if ((item.isEtcItem() && (item.getEtcItem().getItemType() == EtcItemType.SEED)) || (item.getItem().getType2() == Item.TYPE2_MONEY))
			{
				if (_list.size() < MAX_SORT_LIST_ITEMS)
				{
					_list.add(new WarehouseItem(item));
				}
				else
				{
					continue;
				}
			}
		}
		return _list;
	}

	private List<WarehouseItem> createOtherList(ItemInstance[] _items)
	{
		final List<WarehouseItem> _list = new ArrayList<>();
		for (final ItemInstance item : _items)
		{
			if ((item.isEtcItem() && ((item.getEtcItem().getItemType() != EtcItemType.MATERIAL) && (item.getEtcItem().getItemType() != EtcItemType.RECIPE) && (item.getEtcItem().getItemType() != EtcItemType.SCROLL) && (item.getEtcItem().getItemType() != EtcItemType.SHOT))) || (item.getItem().getType2() == Item.TYPE2_MONEY))
			{
				if (_list.size() < MAX_SORT_LIST_ITEMS)
				{
					_list.add(new WarehouseItem(item));
				}
				else
				{
					continue;
				}
			}
		}
		return _list;
	}

	private List<WarehouseItem> createAllList(ItemInstance[] _items)
	{
		final List<WarehouseItem> _list = new ArrayList<>();
		for (final ItemInstance item : _items)
		{
			if (_list.size() < MAX_SORT_LIST_ITEMS)
			{
				_list.add(new WarehouseItem(item));
			}
			else
			{
				continue;
			}
		}
		return _list;
	}

	@Override
	protected final void writeImpl()
	{
		writeH(_whType);
		writeQ(_playerAdena);
		writeH(_objects.size());

		for (final WarehouseItem item : _objects)
		{
			writeD(item.getObjectId());
			writeD(item.getItem().getDisplayId());
			writeD(item.getLocationSlot());
			writeQ(item.getCount());
			writeH(item.getItem().getType2());
			writeH(item.getCustomType1());
			writeH(0x00);
			writeD(item.getItem().getBodyPart());
			writeH(item.getEnchantLevel());
			writeH(item.getCustomType2());
			if (item.isAugmented())
			{
				writeD(item.getAugmentationId());
			}
			else
			{
				writeD(0x00);
			}
			writeD(item.getMana());
			writeD(item.getTime());
			writeH(item.getAttackElementType());
			writeH(item.getAttackElementPower());
			for (byte i = 0; i < 6; i++)
			{
				writeH(item.getElementDefAttr(i));
			}
			for (final int op : item.getEnchantOptions())
			{
				writeH(op);
			}
			writeD(item.getObjectId());
		}
	}
}