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
package gameserver.handler.itemhandlers.impl;

import java.util.ArrayList;
import java.util.List;

import l2e.commons.util.Rnd;
import gameserver.Config;
import gameserver.data.parser.ItemsParser;
import gameserver.handler.itemhandlers.IItemHandler;
import gameserver.model.actor.Playable;
import gameserver.model.actor.templates.items.EtcItem;
import gameserver.model.holders.ItemHolder;
import gameserver.model.items.instance.ItemInstance;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.ExPCCafePointInfo;
import gameserver.network.serverpackets.SystemMessage;
import gameserver.utils.Log;
import gameserver.utils.Util;

public class ExtractableItems implements IItemHandler
{
	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!playable.isPlayer())
		{
			playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
			return false;
		}
		
		final var activeChar = playable.getActingPlayer();
		final var etcitem = (EtcItem) item.getItem();
		final var exitem = etcitem.getRewardItems();
		if (exitem == null)
		{
			_log.info("No extractable data defined for " + etcitem);
			return false;
		}
		
		final var requestItems = item.getItem().getRequestItems();
		
		long totalAmount = 1L;
		if (item.isStackable())
		{
			if (forceUse)
			{
				totalAmount = activeChar.getInventory().getItemByItemId(item.getId()).getCount();
				if (etcitem.isCheckSlots())
				{
					int slots = 0;
					for (int i = 0; i < totalAmount; i++)
					{
						for (final var expi : exitem)
						{
							final var it = ItemsParser.getInstance().getTemplate(expi.getId());
							if (it != null)
							{
								if (!item.isStackable() || activeChar.getInventory().getItemByItemId(item.getId()) == null)
								{
									slots++;
								}
							}
						}
					}
					
					if (!activeChar.getInventory().validateCapacity(slots))
					{
						activeChar.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
						return false;
					}
				}
				
				if (requestItems != null && !requestItems.isEmpty())
				{
					for (final var it : requestItems)
					{
						if (it != null)
						{
							if (!playable.getInventory().haveItemsCountNotEquip(it.getId(), it.getCountMax() * totalAmount))
							{
								playable.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
								return false;
							}
						}
					}
				}
				
				if (!activeChar.destroyItemByItemId("Extract", item.getId(), totalAmount, activeChar, true))
				{
					return false;
				}
			}
			else
			{
				if (etcitem.isCheckSlots())
				{
					int slots = 0;
					for (final var expi : exitem)
					{
						final var it = ItemsParser.getInstance().getTemplate(expi.getId());
						if (it != null)
						{
							if (!item.isStackable() || activeChar.getInventory().getItemByItemId(item.getId()) == null)
							{
								slots++;
							}
						}
					}
					
					if (!activeChar.getInventory().validateCapacity(slots))
					{
						activeChar.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
						return false;
					}
				}
				
				if (requestItems != null && !requestItems.isEmpty())
				{
					for (final var it : requestItems)
					{
						if (it != null)
						{
							if (!playable.getInventory().haveItemsCountNotEquip(it.getId(), it.getCountMax() * totalAmount))
							{
								playable.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
								return false;
							}
						}
					}
				}
				
				if (!activeChar.destroyItemByItemId("Extract", item.getId(), totalAmount, activeChar, true))
				{
					return false;
				}
			}
		}
		else
		{
			if (forceUse)
			{
				final var items = activeChar.getInventory().getAllItemsByItemId(item.getId(), false);
				totalAmount = items.length;
				
				if (etcitem.isCheckSlots())
				{
					int slots = 0;
					for (int i = 0; i < items.length; i++)
					{
						for (final var expi : exitem)
						{
							final var it = ItemsParser.getInstance().getTemplate(expi.getId());
							if (it != null)
							{
								if (!item.isStackable() || activeChar.getInventory().getItemByItemId(item.getId()) == null)
								{
									slots++;
								}
							}
						}
					}
					
					if (!activeChar.getInventory().validateCapacity(slots))
					{
						activeChar.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
						return false;
					}
				}
				
				if (requestItems != null && !requestItems.isEmpty())
				{
					for (final var it : requestItems)
					{
						if (it != null)
						{
							if (!playable.getInventory().haveItemsCountNotEquip(it.getId(), it.getCountMax() * totalAmount))
							{
								playable.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
								return false;
							}
						}
					}
				}
				
				for (int i = 0; i < items.length; i++)
				{
					if (!activeChar.destroyItem("Extract", items[i].getObjectId(), 1, activeChar, true))
					{
						return false;
					}
				}
			}
			else
			{
				if (etcitem.isCheckSlots())
				{
					int slots = 0;
					for (final var expi : exitem)
					{
						final var it = ItemsParser.getInstance().getTemplate(expi.getId());
						if (it != null)
						{
							if (!item.isStackable() || activeChar.getInventory().getItemByItemId(item.getId()) == null)
							{
								slots++;
							}
						}
					}
					
					if (!activeChar.getInventory().validateCapacity(slots))
					{
						activeChar.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
						return false;
					}
				}
				
				if (requestItems != null && !requestItems.isEmpty())
				{
					for (final var it : requestItems)
					{
						if (it != null)
						{
							if (!playable.getInventory().haveItemsCountNotEquip(it.getId(), it.getCountMax()))
							{
								playable.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
								return false;
							}
						}
					}
				}
				
				if (!activeChar.destroyItem("Extract", item.getObjectId(), 1, activeChar, true))
				{
					return false;
				}
			}
		}
		
		if (requestItems != null && !requestItems.isEmpty())
		{
			final long finalAmount = totalAmount;
			requestItems.stream().filter(i -> i != null).forEach(p -> playable.destroyItemByItemId("Consume", p.getId(), (p.getCountMax() * finalAmount), null, false));
		}
		
		var created = false;
		int min;
		int max;
		int createitemAmount;
		
		final var forceRandom = etcitem.isForceRandom();
		final var singleRandom = etcitem.isSingleRandom();
		final List<ItemHolder> holders = new ArrayList<>();
		for (int i = 0; i < totalAmount; i++)
		{
			boolean isCreated = false;
			for (final var expi : exitem)
			{
				if (Rnd.chance(expi.getChance()))
				{
					min = (int) (expi.getCount() * Config.RATE_EXTRACTABLE);
					max = (int) (expi.getCountMax() * Config.RATE_EXTRACTABLE);
					
					createitemAmount = (max == min) ? min : (Rnd.get((max - min) + 1) + min);
					final var it = ItemsParser.getInstance().getTemplate(expi.getId());
					if (expi.getId() < 0 || (expi.getId() > 0 && it != null && it.isStackable()))
					{
						boolean found = false;
						for (final var holder : holders)
						{
							if (holder != null && holder.getId() == expi.getId())
							{
								holder.setCount(holder.getCount() + createitemAmount);
								isCreated = true;
								found = true;
								if (singleRandom)
								{
									break;
								}
							}
						}
						
						if (found && singleRandom)
						{
							break;
						}
						
						if (!found)
						{
							holders.add(new ItemHolder(expi.getId(), createitemAmount, 100, expi.getEnchantLevel()));
							isCreated = true;
							if (singleRandom)
							{
								break;
							}
						}
					}
					else
					{
						holders.add(new ItemHolder(expi.getId(), createitemAmount, 100, expi.getEnchantLevel()));
						isCreated = true;
						if (singleRandom)
						{
							break;
						}
					}
				}
			}
			
			if (forceRandom && !isCreated)
			{
				loopGive(holders, exitem);
			}
		}
			
		if (!holders.isEmpty())
		{
			for (final var holder : holders)
			{
				switch (holder.getId())
				{
					case -100 :
						
						if (activeChar.getPcBangPoints() < Config.MAX_PC_BANG_POINTS)
						{
							long count = holder.getCount();
							if ((activeChar.getPcBangPoints() + count) > Config.MAX_PC_BANG_POINTS)
							{
								count = Config.MAX_PC_BANG_POINTS - activeChar.getPcBangPoints();
							}
							
							activeChar.setPcBangPoints((int) (activeChar.getPcBangPoints() + count));
							final var smsg = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_ACQUIRED_S1_PC_CAFE_POINTS);
							smsg.addNumber((int) count);
							activeChar.sendPacket(smsg);
							activeChar.sendPacket(new ExPCCafePointInfo(activeChar.getPcBangPoints(), (int) count, false, false, 1));
							created = true;
						}
						break;
					case -200 :
						if (activeChar.getClan() != null)
						{
							activeChar.getClan().addReputationScore((int) holder.getCount(), true);
							created = true;
						}
						break;
					case -300 :
						activeChar.setFame((int) (activeChar.getFame() + holder.getCount()));
						activeChar.sendUserInfo();
						created = true;
						break;
					case -1 :
						activeChar.setGamePoints((activeChar.getGamePoints() + holder.getCount()));
						activeChar.sendMessage("Your game points count changed to " + activeChar.getGamePoints());
						created = true;
						break;
					default :
						final var it = ItemsParser.getInstance().getTemplate(holder.getId());
						if (it == null)
						{
							break;
						}
						
						if (it.isStackable())
						{
							final var newItem = activeChar.addItem("Extract", holder.getId(), holder.getCount(), activeChar, true);
							if (newItem != null && holder.getEnchantLevel() > 0)
							{
								newItem.setEnchantLevel(holder.getEnchantLevel());
								newItem.updateDatabase();
							}
						}
						else
						{
							ItemInstance newItem = null;
							for (int i = 0; i < holder.getCount(); i++)
							{
								newItem = activeChar.addItem("Multisell", holder.getId(), 1, activeChar, true);
								if (newItem != null && holder.getEnchantLevel() > 0)
								{
									newItem.setEnchantLevel(holder.getEnchantLevel());
									newItem.updateDatabase();
								}
							}
						}
						created = true;
						break;
				}
				Log.addLogItem("ExtractableItems", activeChar.getName(null) + " -> receive ->", Util.getItemName(holder.getId()), holder.getCount(), holder.getEnchantLevel(), null);
			}
			holders.clear();
		}
		
		if (!created)
		{
			activeChar.sendPacket(SystemMessageId.NOTHING_INSIDE_THAT);
		}
		return true;
	}
	
	private static void loopGive(List<ItemHolder> holders, List<ItemHolder> exitem)
	{
		boolean doneAfterOneItem = false;
		int min;
		int max;
		int createitemAmount;
		for (final var expi : exitem)
		{
			if (Rnd.chance(expi.getChance()) && !doneAfterOneItem)
			{
				min = (int) (expi.getCount() * Config.RATE_EXTRACTABLE);
				max = (int) (expi.getCountMax() * Config.RATE_EXTRACTABLE);
				
				createitemAmount = (max == min) ? min : (Rnd.get((max - min) + 1) + min);
				final var it = ItemsParser.getInstance().getTemplate(expi.getId());
				if (expi.getId() < 0 || (expi.getId() > 0 && it != null && it.isStackable()))
				{
					for (final var holder : holders)
					{
						if (holder != null && holder.getId() == expi.getId())
						{
							holder.setCount(holder.getCount() + createitemAmount);
							doneAfterOneItem = true;
							break;
						}
					}
					
					if (!doneAfterOneItem)
					{
						holders.add(new ItemHolder(expi.getId(), createitemAmount, 100, expi.getEnchantLevel()));
						doneAfterOneItem = true;
					}
				}
				else
				{
					holders.add(new ItemHolder(expi.getId(), createitemAmount, 100, expi.getEnchantLevel()));
					doneAfterOneItem = true;
				}
				break;
			}
		}
		
		if (!doneAfterOneItem)
		{
			loopGive(holders, exitem);
		}
	}
}