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
package gameserver.network.clientpackets.Interface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gameserver.Config;
import gameserver.ai.model.CtrlIntention;
import gameserver.data.parser.MultiSellParser;
import gameserver.geodata.GeoEngine;
import gameserver.instancemanager.DailyTaskManager;
import gameserver.model.Augmentation;
import gameserver.model.Elementals;
import gameserver.model.GameObjectsStorage;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Npc;
import gameserver.model.items.instance.ItemInstance;
import gameserver.model.items.itemcontainer.PcInventory;
import gameserver.model.items.multisell.Entry;
import gameserver.model.items.multisell.Ingredient;
import gameserver.model.items.multisell.PreparedListContainer;
import gameserver.network.SystemMessageId;
import gameserver.network.clientpackets.GameClientPacket;
import gameserver.network.serverpackets.InventoryUpdate;
import gameserver.network.serverpackets.StatusUpdate;
import gameserver.network.serverpackets.SystemMessage;
import gameserver.network.serverpackets.Interface.ExDynamicPacket;

public class RequestDynamicPacket extends GameClientPacket
{
	private int _packetType = -1;
	private int _type = 0;
	private int _quetId = 0;
	protected int _farmId = 0;
	protected int _listId = -1;
	protected int _entryId = -1;
	protected int _amount = 1;
	
    @Override
    protected void readImpl()
    {
    	_packetType = readC();
    	switch(_packetType)
    	{
			case 2 :
			{
				_type = readC();
				break;
			}
			case 3 :
			{
				_farmId = readD();
				break;
			}
			case 4 :
			{
				_farmId = readD();
				break;
			}
			case 7 :
			{
				_listId = readC();
				_entryId = (readC() + 1) * 100000;
				break;
			}
			case 10 :
    			_type = readC();
				if (_type == 1)
        		{
        			_quetId = readH();
        		}
    		break;
    	}
    }

	@Override
	protected void runImpl()
	{
		final var player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
	   	switch(_packetType)
    	{
			case 2 :
			{
				if (_type == 1)
				{
					// check valid to use
				}
				player.sendPacket(new ExDynamicPacket(_type, _type == 0 ? 0 : 43200)); // time
				break;
			}
			case 3 :
			{
				final var target = player.getTarget();
				if (target != null && target instanceof Creature && !((Creature) target).isDead())
				{
					if (GeoEngine.getInstance().canSeeTarget(player, target))
					{
						player.getAI().setIntention(CtrlIntention.ATTACK, target);
					}
					else
					{
						player.getAI().setIntention(CtrlIntention.MOVING, target.getLocation(), 0);
					}
				}
				else
				{
					final var npc = GameObjectsStorage.getNpc(_farmId);
					if (npc != null)
					{
						player.setTarget(npc);
						if (GeoEngine.getInstance().canSeeTarget(player, npc))
						{
							player.getAI().setIntention(CtrlIntention.ATTACK, npc);
						}
						else
						{
							player.getAI().setIntention(CtrlIntention.MOVING, npc.getLocation(), 0);
						}
					}
				}
				break;
			}
			case 4 :
			{
				final var skill = player.getKnownSkill(_farmId);
				if (skill != null)
				{
					if (player.isMoving() && player.getCurrentSkill() != null)
					{
						return;
					}
					
					if (player.checkDoCastConditions(skill, false))
					{
						player.useMagic(skill, true, false, false);
					}
				}
				break;
			}
			case 7 :
			{
				if (player.isActionsDisabled() || player.getActiveTradeList() != null)
				{
					player.sendActionFailed();
					return;
				}
				
				if (!player.isInventoryUnder90(false))
				{
					player.sendPacket(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT);
					return;
				}
				
				final PreparedListContainer list = player.getMultiSell();
				if ((list == null) || (list.getListId() != _listId))
				{
					player.setMultiSell(null);
					return;
				}
				
				if (player.isProcessingTransaction())
				{
					player.setMultiSell(null);
					return;
				}
				
				final Npc target = player.getLastFolkNPC();
				
				for (final Entry entry : list.getEntries())
				{
					if (entry.getEntryId() == _entryId)
					{
						if (!entry.isStackable() && (_amount > 1))
						{
							_log.error("Character: " + player.getName(null) + " is trying to set amount > 1 on non-stackable multisell, id:" + _listId + ":" + _entryId);
							player.setMultiSell(null);
							return;
						}
						
						final PcInventory inv = player.getInventory();
						
						int slots = 0;
						int weight = 0;
						for (final Ingredient e : entry.getProducts())
						{
							if (e.getId() < 0 || e.getId() == Config.SP_ID || e.getId() == Config.EXP_ID)
							{
								continue;
							}
							
							if (!e.isStackable())
							{
								slots += e.getCount() * _amount;
							}
							else if (player.getInventory().getItemByItemId(e.getId()) == null)
							{
								slots++;
							}
							weight += e.getCount() * _amount * e.getWeight();
						}
						
						if (!inv.validateWeight(weight))
						{
							player.sendPacket(SystemMessageId.WEIGHT_LIMIT_EXCEEDED);
							return;
						}
						
						if (!inv.validateCapacity(slots))
						{
							player.sendPacket(SystemMessageId.SLOTS_FULL);
							return;
						}
						
						final ArrayList<Ingredient> ingredientsList = new ArrayList<>(entry.getIngredients().size());
						
						boolean newIng;
						for (final Ingredient e : entry.getIngredients())
						{
							newIng = true;
							for (int i = ingredientsList.size(); --i >= 0;)
							{
								final Ingredient ex = ingredientsList.get(i);
								if ((ex.getId() == e.getId()) && (ex.getEnchantLevel() == e.getEnchantLevel()))
								{
									if ((ex.getCount() + e.getCount()) > Long.MAX_VALUE)
									{
										player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
										return;
									}
									final Ingredient ing = ex.getCopy();
									ing.setCount(ex.getCount() + e.getCount());
									ingredientsList.set(i, ing);
									newIng = false;
									break;
								}
							}
							if (newIng)
							{
								ingredientsList.add(e);
							}
						}
						
						for (final Ingredient e : ingredientsList)
						{
							if ((e.getCount() * _amount) > Long.MAX_VALUE)
							{
								player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
								return;
							}
							if (e.getId() < 0 || e.getId() == Config.SP_ID || e.getId() == Config.EXP_ID)
							{
								if (!MultiSellParser.checkSpecialIngredient(e.getId(), e.getCount() * _amount, player))
								{
									return;
								}
							}
							else
							{
								final long required = ((Config.ALT_BLACKSMITH_USE_RECIPES || !e.getMaintainIngredient()) ? (e.getCount() * _amount) : e.getCount());
								if (list.getMaintainEnchantment() ? inv.getInventoryItemCount(e.getId(), e.getEnchantLevel(), list.isAllowCheckEquipItems()) < required : inv.getInventoryItemCount(e.getId(), e.getEnchantLevel(), e.getAugmentation(), e.getElementals(), list.isAllowCheckEquipItems(), e.isCheckParams()) < required)
								{
									final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_UNIT_OF_THE_ITEM_S1_REQUIRED);
									sm.addItemName(e.getTemplate());
									sm.addNumber((int) required);
									player.sendPacket(sm);
									return;
								}
							}
						}
						
						final List<Augmentation> augmentation = new ArrayList<>();
						Elementals[] elemental = null;
						int enchantLevel = 0;
						for (final Ingredient e : entry.getIngredients())
						{
							if (e.getId() < 0 || e.getId() == Config.SP_ID || e.getId() == Config.EXP_ID)
							{
								if (!MultiSellParser.getSpecialIngredient(e.getId(), e.getCount() * _amount, player))
								{
									return;
								}
							}
							else
							{
								ItemInstance itemToTake = inv.getItemByItemId(e.getId());
								if (itemToTake == null)
								{
									_log.error("Character: " + player.getName(null) + " is trying to cheat in multisell, id:" + _listId + ":" + _entryId);
									player.setMultiSell(null);
									return;
								}
								
								if (Config.ALT_BLACKSMITH_USE_RECIPES || !e.getMaintainIngredient())
								{
									if (itemToTake.isStackable())
									{
										if (list.isAllowCheckEquipItems() && itemToTake.isEquipped())
										{
											final var unequiped = player.getInventory().unEquipItemInSlotAndRecord(itemToTake.getLocationSlot(), true);
											final var iu = new InventoryUpdate();
											for (final ItemInstance itm : unequiped)
											{
												iu.addModifiedItem(itm);
											}
											player.sendInventoryUpdate(iu);
										}
										
										if (!player.destroyItem("Multisell", itemToTake.getObjectId(), (e.getCount() * _amount), player.getTarget(), true))
										{
											player.setMultiSell(null);
											return;
										}
									}
									else
									{
										if (list.getMaintainEnchantment())
										{
											final ItemInstance[] inventoryContents = inv.getAllItemsByItemId(e.getId(), e.getEnchantLevel(), list.isAllowCheckEquipItems());
											for (int i = 0; i < (e.getCount() * _amount); i++)
											{
												if (inventoryContents[i].isAugmented())
												{
													augmentation.add(inventoryContents[i].getAugmentation());
												}
												if (inventoryContents[i].getElementals() != null)
												{
													elemental = inventoryContents[i].getElementals();
												}
												
												if (inventoryContents[i].getEnchantLevel() > enchantLevel)
												{
													enchantLevel = inventoryContents[i].getEnchantLevel();
												}
												
												if (list.isAllowCheckEquipItems() && inventoryContents[i].isEquipped())
												{
													final var unequiped = player.getInventory().unEquipItemInSlotAndRecord(inventoryContents[i].getLocationSlot(), true);
													final var iu = new InventoryUpdate();
													for (final ItemInstance itm : unequiped)
													{
														iu.addModifiedItem(itm);
													}
													player.sendInventoryUpdate(iu);
												}
												
												if (!player.destroyItem("Multisell", inventoryContents[i].getObjectId(), 1, player.getTarget(), true))
												{
													player.setMultiSell(null);
													return;
												}
											}
										}
										else
										{
											for (int i = 1; i <= (e.getCount() * _amount); i++)
											{
												final Augmentation augment = e.getAugmentation();
												final Elementals[] elementals = e.getElementals();
												final int enchant = e.getEnchantLevel();
												itemToTake = inv.getInventoryNeedItem(e.getId(), enchant, augment, elementals, false, e.isCheckParams());
												if (itemToTake == null)
												{
													player.setMultiSell(null);
													return;
												}
												
												if (!player.destroyItem("Multisell", itemToTake.getObjectId(), 1, player.getTarget(), true))
												{
													player.setMultiSell(null);
													return;
												}
											}
										}
									}
								}
							}
						}
						
						final Ingredient randomProduct = list.getRandomHolder(entry.getProducts());
						final List<Ingredient> products = randomProduct != null ? Collections.singletonList(randomProduct) : Collections.emptyList();
						for (final Ingredient e : products)
						{
							if (e.getId() < 0 || e.getId() == Config.SP_ID || e.getId() == Config.EXP_ID)
							{
								MultiSellParser.addSpecialProduct(e.getId(), e.getCount() * _amount, player);
							}
							else
							{
								if (e.isStackable())
								{
									inv.addItem("Multisell", e.getId(), e.getCount() * _amount, player, player.getTarget());
								}
								else
								{
									ItemInstance product = null;
									for (int i = 0; i < (e.getCount() * _amount); i++)
									{
										product = player.addItem("Multisell", e.getId(), 1, player, false);
										if (product != null)
										{
											if (list.getMaintainEnchantment())
											{
												if (i < augmentation.size())
												{
													product.setAugmentation(new Augmentation(augmentation.get(i).getAugmentationId()));
												}
												
												if (elemental != null)
												{
													for (final Elementals elm : elemental)
													{
														product.setElementAttr(elm.getElement(), elm.getValue(), true);
													}
												}
												
												if (product.isEnchantable())
												{
													product.setEnchantLevel(enchantLevel);
												}
											}
											else
											{
												if (e.getAugmentation() != null)
												{
													product.setAugmentation(new Augmentation(e.getAugmentation().getAugmentationId()));
												}
												
												if (e.getElementals() != null)
												{
													for (final Elementals elm : e.getElementals())
													{
														product.setElementAttr(elm.getElement(), elm.getValue(), true);
													}
												}
												
												if (e.getTimeLimit() > 0)
												{
													product.setTime(e.getTimeLimit());
												}
												
												if (product.isEnchantable())
												{
													product.setEnchantLevel(e.getEnchantLevel());
												}
											}
											product.updateDatabase();
										}
									}
									SystemMessage sm;
									if ((e.getCount() * _amount) > 1)
									{
										sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
										sm.addItemName(e.getId());
										sm.addItemNumber(e.getCount() * _amount);
										player.sendPacket(sm);
										sm = null;
									}
									else
									{
										if (e.getEnchantLevel() > 0)
										{
											sm = SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_S2);
											sm.addItemNumber(e.getEnchantLevel());
											sm.addItemName(e.getId());
										}
										else
										{
											sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
											sm.addItemName(e.getId());
										}
										player.sendPacket(sm);
										sm = null;
									}
								}
							}
						}
						player.sendStatusUpdate(false, false, StatusUpdate.CUR_LOAD);
						if (entry.getTaxAmount() > 0)
						{
							target.getCastle().addToTreasury(entry.getTaxAmount() * _amount);
						}
						break;
					}
				}
				
				if (list.getMaintainEnchantment() && list.isNpcOnly())
				{
					MultiSellParser.getInstance().separateAndSend(_listId, player, target, true);
				}
				break;
			}
			case 10 :
				if (player.getActiveDailyTasks().isEmpty())
				{
					DailyTaskManager.getInstance().addAllValidTasks(player);
				}
				
				if (_type == 1)
        		{
					final var playerTask = player.getDailyTaskTemplate(_quetId);
					if ((playerTask != null) && playerTask.isComplete() && !playerTask.isRewarded())
					{
						final var task = DailyTaskManager.getInstance().getDailyTask(_quetId);
						if (task.getRewards() != null && !task.getRewards().isEmpty())
						{
							for (final int i : task.getRewards().keySet())
							{
								player.addItem("Task Reward", i, task.getRewards().get(i), null, true);
							}
						}
						playerTask.setIsRewarded(true);
						player.updateDailyRewardStatus(playerTask);
					}
        		}
        		player.sendPacket(new ExDynamicPacket(player));
        		break;
    	}
	}
}
