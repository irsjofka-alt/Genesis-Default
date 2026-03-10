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
package gameserver.model.items.itemcontainer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import l2e.commons.dbutils.DbUtils;
import gameserver.Config;
import gameserver.data.parser.ArmorSetsParser;
import gameserver.data.parser.ItemsParser;
import gameserver.database.DatabaseFactory;
import gameserver.handler.skillhandlers.SkillHandler;
import gameserver.model.GameObjectsStorage;
import gameserver.model.PcCondOverride;
import gameserver.model.actor.Player;
import gameserver.model.actor.templates.items.Item;
import gameserver.model.holders.SkillHolder;
import gameserver.model.items.instance.ItemInstance;
import gameserver.model.items.instance.ItemInstance.ItemLocation;
import gameserver.model.items.type.EtcItemType;
import gameserver.model.items.type.WeaponType;
import gameserver.model.skills.Skill;
import gameserver.model.stats.Stats;
import gameserver.network.serverpackets.updatetype.UserInfoType;
import gameserver.utils.StringUtil;

public abstract class Inventory extends ItemContainer
{
	public interface PaperdollListener
	{
		public void notifyEquiped(int slot, ItemInstance inst, Inventory inventory, boolean sendInfo);

		public void notifyUnequiped(int slot, ItemInstance inst, Inventory inventory, boolean sendInfo);
	}
	
	public static final int PAPERDOLL_UNDER = 0;
	public static final int PAPERDOLL_HEAD = 1;
	public static final int PAPERDOLL_HAIR = 2;
	public static final int PAPERDOLL_HAIR2 = 3;
	public static final int PAPERDOLL_NECK = 4;
	public static final int PAPERDOLL_RHAND = 5;
	public static final int PAPERDOLL_CHEST = 6;
	public static final int PAPERDOLL_LHAND = 7;
	public static final int PAPERDOLL_REAR = 8;
	public static final int PAPERDOLL_LEAR = 9;
	public static final int PAPERDOLL_GLOVES = 10;
	public static final int PAPERDOLL_LEGS = 11;
	public static final int PAPERDOLL_FEET = 12;
	public static final int PAPERDOLL_RFINGER = 13;
	public static final int PAPERDOLL_LFINGER = 14;
	public static final int PAPERDOLL_LBRACELET = 15;
	public static final int PAPERDOLL_RBRACELET = 16;
	public static final int PAPERDOLL_DECO1 = 17;
	public static final int PAPERDOLL_DECO2 = 18;
	public static final int PAPERDOLL_DECO3 = 19;
	public static final int PAPERDOLL_DECO4 = 20;
	public static final int PAPERDOLL_DECO5 = 21;
	public static final int PAPERDOLL_DECO6 = 22;
	public static final int PAPERDOLL_CLOAK = 23;
	public static final int PAPERDOLL_BELT = 24;
	public static final int PAPERDOLL_TOTALSLOTS = 25;
	
	public static final double MAX_ARMOR_WEIGHT = 12000;

	protected final ItemInstance[] _paperdoll;
	private final List<PaperdollListener> _paperdollListeners;

	protected int _totalWeight;
	private int _wearedMask;
	
	private static boolean _skillsUpdate = false;
	private static boolean _timeStampUpdate = false;
	private static boolean _userInfoUpdate = false;

	private static final class ChangeRecorder implements PaperdollListener
	{
		private final Inventory _inventory;
		private final List<ItemInstance> _changed = new ArrayList<>();

		ChangeRecorder(Inventory inventory)
		{
			_inventory = inventory;
			_inventory.addPaperdollListener(this);
		}

		@Override
		public void notifyEquiped(int slot, ItemInstance item, Inventory inventory, boolean sendInfo)
		{
			if (!_changed.contains(item))
			{
				_changed.add(item);
			}
		}

		@Override
		public void notifyUnequiped(int slot, ItemInstance item, Inventory inventory, boolean sendInfo)
		{
			if (!_changed.contains(item))
			{
				_changed.add(item);
			}
		}

		public List<ItemInstance> getChangedItems()
		{
			return _changed;
		}
	}

	private static final class BowCrossRodListener implements PaperdollListener
	{
		private static BowCrossRodListener instance = new BowCrossRodListener();

		public static BowCrossRodListener getInstance()
		{
			return instance;
		}

		@Override
		public void notifyUnequiped(int slot, ItemInstance item, Inventory inventory, boolean sendInfo)
		{
			if (slot != PAPERDOLL_RHAND)
			{
				return;
			}

			if (item.getItemType() == WeaponType.BOW)
			{
				final var arrow = inventory.getPaperdollItem(PAPERDOLL_LHAND);
				if (arrow != null)
				{
					inventory.setPaperdollItem(PAPERDOLL_LHAND, null, sendInfo);
				}
			}
			else if (item.getItemType() == WeaponType.CROSSBOW)
			{
				final var bolts = inventory.getPaperdollItem(PAPERDOLL_LHAND);
				if (bolts != null)
				{
					inventory.setPaperdollItem(PAPERDOLL_LHAND, null, sendInfo);
				}
			}
			else if (item.getItemType() == WeaponType.FISHINGROD)
			{
				final var lure = inventory.getPaperdollItem(PAPERDOLL_LHAND);
				if (lure != null)
				{
					inventory.setPaperdollItem(PAPERDOLL_LHAND, null, sendInfo);
				}
			}
		}

		@Override
		public void notifyEquiped(int slot, ItemInstance item, Inventory inventory, boolean sendInfo)
		{
			if (slot != PAPERDOLL_RHAND)
			{
				return;
			}

			if (item.getItemType() == WeaponType.BOW)
			{
				final var arrow = inventory.findArrowForBow(item.getItem());
				if (arrow != null)
				{
					inventory.setPaperdollItem(PAPERDOLL_LHAND, arrow, sendInfo);
				}
			}
			else if (item.getItemType() == WeaponType.CROSSBOW)
			{
				final var bolts = inventory.findBoltForCrossBow(item.getItem());
				if (bolts != null)
				{
					inventory.setPaperdollItem(PAPERDOLL_LHAND, bolts, sendInfo);
				}
			}
		}
	}

	private static final class StatsListener implements PaperdollListener
	{
		private static StatsListener instance = new StatsListener();

		public static StatsListener getInstance()
		{
			return instance;
		}

		@Override
		public void notifyUnequiped(int slot, ItemInstance item, Inventory inventory, boolean sendInfo)
		{
			inventory.getOwner().removeStatsOwner(item);
		}

		@Override
		public void notifyEquiped(int slot, ItemInstance item, Inventory inventory, boolean sendInfo)
		{
			inventory.getOwner().addStatFuncs(item.getStatFuncs(inventory.getOwner()));
		}
	}

	private static final class ItemSkillsListener implements PaperdollListener
	{
		private static ItemSkillsListener instance = new ItemSkillsListener();

		public static ItemSkillsListener getInstance()
		{
			return instance;
		}

		@Override
		public void notifyUnequiped(int slot, ItemInstance item, Inventory inventory, boolean sendInfo)
		{
			final var player = inventory.getOwner().getActingPlayer();
			if (player == null)
			{
				return;
			}
			
			Skill enchant4Skill, itemSkill;
			final var it = item.getItem();
			
			if (item.isAugmented())
			{
				item.getAugmentation().removeBonus(player);
			}

			item.unChargeAllShots();
			item.removeElementAttrBonus(player);

			if (item.getEnchantLevel() >= 4)
			{
				enchant4Skill = it.getEnchant4Skill();

				if (enchant4Skill != null)
				{
					player.removeSkill(enchant4Skill, false, enchant4Skill.isPassive());
					_skillsUpdate = true;
				}
			}

			item.clearEnchantStats();

			final var skills = it.getSkills();
			if (skills != null)
			{
				for (final var skillInfo : skills)
				{
					if (skillInfo == null)
					{
						continue;
					}

					itemSkill = skillInfo.getSkill();

					if (itemSkill != null)
					{
						player.removeSkill(itemSkill, false, itemSkill.isPassive());
						_skillsUpdate = true;
					}
					else
					{
						_log.warn("Inventory.ItemSkillsListener.Weapon: Incorrect skill: " + skillInfo + ".");
					}
				}
			}

			if (item.isArmor())
			{
				for (final var itm : inventory.getItems())
				{
					if (itm == null || itm.getObjectId() == item.getObjectId() || !itm.isEquipped())
					{
						continue;
					}
					
					final var itemSkills = itm.getItem().getSkills();
					if (itemSkills != null)
					{
						for (final var sk : itemSkills)
						{
							if (player.getSkillLevel(sk.getId()) >= sk.getLvl())
							{
								continue;
							}
							
							itemSkill = sk.getSkill();
							
							if (itemSkill != null)
							{
								player.addSkill(itemSkill, false);
								itemSkill.setItemSkill(true);
								
								if (itemSkill.isActive())
								{
									if (!player.hasSkillReuse(itemSkill.getReuseHashCode()))
									{
										final int equipDelay = item.getEquipReuseDelay();
										if (equipDelay > 0)
										{
											player.addTimeStamp(itemSkill, equipDelay);
											player.disableSkill(itemSkill, equipDelay);
										}
									}
									_timeStampUpdate = true;
								}
								_skillsUpdate = true;
							}
						}
					}
				}
			}
			
			if (item.isShadowItem())
			{
				item.stopManaConsumeTask();
			}
			
			final var unequipSkill = it.getUnequipSkill();
			if (unequipSkill != null)
			{
				final var handler = SkillHandler.getInstance().getHandler(unequipSkill.getSkillType());
				final Player[] targets =
				{
				        player
				};

				if (handler != null)
				{
					handler.useSkill(player, unequipSkill, targets, 0);
				}
				else
				{
					unequipSkill.useSkill(player, targets, 0);
				}
			}

			if (sendInfo && _skillsUpdate)
			{
				player.sendSkillList(_timeStampUpdate);
				_skillsUpdate = false;
				_timeStampUpdate = false;
			}
		}

		@Override
		public void notifyEquiped(int slot, ItemInstance item, Inventory inventory, boolean sendInfo)
		{
			final var player = inventory.getOwner().getActingPlayer();
			if (player == null)
			{
				return;
			}

			Skill enchant4Skill, itemSkill;
			final var it = item.getItem();
			if (item.isAugmented())
			{
				item.getAugmentation().applyBonus(player);
			}

			item.rechargeShots(true, true);
			item.updateElementAttrBonus(player);

			if (item.getEnchantLevel() >= 4)
			{
				enchant4Skill = it.getEnchant4Skill();

				if (enchant4Skill != null)
				{
					player.addSkill(enchant4Skill, false);
					enchant4Skill.setItemSkill(true);
					_skillsUpdate = true;
				}
			}
			
			item.applyEnchantStats();
			
			final var skills = it.getSkills();
			if (skills != null)
			{
				for (final var skillInfo : skills)
				{
					if (skillInfo == null)
					{
						continue;
					}

					itemSkill = skillInfo.getSkill();

					if (itemSkill != null)
					{
						player.addSkill(itemSkill, false);
						itemSkill.setItemSkill(true);
						if (itemSkill.isActive())
						{
							if (!player.hasSkillReuse(itemSkill.getReuseHashCode()))
							{
								final int equipDelay = item.getEquipReuseDelay();
								if (equipDelay > 0)
								{
									player.addTimeStamp(itemSkill, equipDelay);
									player.disableSkill(itemSkill, equipDelay);
								}
							}
							_timeStampUpdate = true;
						}
						_skillsUpdate = true;
					}
					else
					{
						_log.warn("Inventory.ItemSkillsListener.Weapon: Incorrect skill: " + skillInfo + ".");
					}
				}
			}

			if (sendInfo && _skillsUpdate)
			{
				player.sendSkillList(_timeStampUpdate);
				_skillsUpdate = false;
				_timeStampUpdate = false;
			}
		}
	}

	private static final class ArmorSetListener implements PaperdollListener
	{
		private static ArmorSetListener instance = new ArmorSetListener();

		public static ArmorSetListener getInstance()
		{
			return instance;
		}

		@Override
		public void notifyEquiped(int slot, ItemInstance item, Inventory inventory, boolean sendInfo)
		{
			final var player = inventory.getOwner().getActingPlayer();
			if (player == null)
			{
				return;
			}

			final var chestItem = inventory.getPaperdollItem(PAPERDOLL_CHEST);
			if (chestItem == null)
			{
				return;
			}
			
			if (player.getInventory().hasAllDressMeItemsEquipped())
			{
				player.getInventory().setMustShowDressMe(true);
			}

			if (!ArmorSetsParser.getInstance().isArmorSet(chestItem.getId()))
			{
				if (sendInfo)
				{
					player.broadcastUserInfo(true);
				}
				else
				{
					_userInfoUpdate = true;
				}
				return;
			}
			final var armorSet = ArmorSetsParser.getInstance().getSet(chestItem.getId());
			if (armorSet.containItem(slot, item.getId()))
			{
				if (armorSet.containAll(player))
				{
					Skill itemSkill;
					final var skills = armorSet.getSkills();
					if (skills != null)
					{
						for (final var holder : skills)
						{
							itemSkill = holder.getSkill();
							if (itemSkill != null)
							{
								final var sk = player.getKnownSkill(itemSkill.getId());
								if (sk == null || sk.getLevel() < itemSkill.getLevel())
								{
									player.addSkill(itemSkill, false);
									itemSkill.setItemSkill(true);
									
									if (itemSkill.isActive())
									{
										if (!player.hasSkillReuse(itemSkill.getReuseHashCode()))
										{
											final int equipDelay = item.getEquipReuseDelay();
											if (equipDelay > 0)
											{
												player.addTimeStamp(itemSkill, equipDelay);
												player.disableSkill(itemSkill, equipDelay);
											}
										}
										_timeStampUpdate = true;
									}
									_skillsUpdate = true;
								}
							}
						}
					}
					
					if (armorSet.containShield(player))
					{
						for (final var holder : armorSet.getShieldSkillId())
						{
							if (holder.getSkill() != null)
							{
								player.addSkill(holder.getSkill(), false);
								holder.getSkill().setItemSkill(true);
								_skillsUpdate = true;
							}
						}
					}
					
					if (armorSet.isEnchanted6(player))
					{
						for (final var holder : armorSet.getEnchant6skillId())
						{
							if (holder.getSkill() != null)
							{
								player.addSkill(holder.getSkill(), false);
								holder.getSkill().setItemSkill(true);
								_skillsUpdate = true;
							}
						}
					}
					
					if (!armorSet.getEnchantByLevel().isEmpty())
					{
						for (final int enchLvl : armorSet.getEnchantByLevel().keySet())
						{
							if (armorSet.isEnchantedByLevel(player, enchLvl))
							{
								final var holder = armorSet.getEnchantByLevel().get(enchLvl);
								if (holder.getSkill() != null)
								{
									player.addSkill(holder.getSkill(), false);
									holder.getSkill().setItemSkill(true);
									_skillsUpdate = true;
								}
							}
						}
					}
				}
			}
			else if (armorSet.containShield(item.getId()))
			{
				for (final var holder : armorSet.getShieldSkillId())
				{
					if (holder.getSkill() != null)
					{
						player.addSkill(holder.getSkill(), false);
						holder.getSkill().setItemSkill(true);
						_skillsUpdate = true;
					}
					else
					{
						_log.warn("Inventory.ArmorSetListener: Incorrect skill: " + holder + ".");
					}
				}
			}

			if (sendInfo)
			{
				if (_skillsUpdate)
				{
					player.sendSkillList(_timeStampUpdate);
					_skillsUpdate = false;
					_timeStampUpdate = false;
				}
				player.broadcastUserInfo(true);
			}
			else
			{
				_userInfoUpdate = true;
			}
		}

		@Override
		public void notifyUnequiped(int slot, ItemInstance item, Inventory inventory, boolean sendInfo)
		{
			final var player = inventory.getOwner().getActingPlayer();
			if (player == null)
			{
				return;
			}

			Skill itemSkill;
			List<SkillHolder> skills = null;
			List<SkillHolder> shieldSkill = null;
			List<SkillHolder> skillId6 = null;
			Map<Integer, SkillHolder> skillIdByLevel = null;
			
			if (player.getInventory().mustShowDressMe() && !player.getInventory().hasAllDressMeItemsEquipped())
			{
				player.getInventory().setMustShowDressMe(false);
			}
			
			if (slot == PAPERDOLL_CHEST)
			{
				if (!ArmorSetsParser.getInstance().isArmorSet(item.getId()))
				{
					if (sendInfo)
					{
						player.broadcastUserInfo(true);
					}
					else
					{
						_userInfoUpdate = true;
					}
					return;
				}
				final var armorSet = ArmorSetsParser.getInstance().getSet(item.getId());
				_skillsUpdate = true;
				skills = armorSet.getSkills();
				shieldSkill = armorSet.getShieldSkillId();
				skillId6 = armorSet.getEnchant6skillId();
				skillIdByLevel = armorSet.getEnchantByLevel();
			}
			else
			{
				final var chestItem = inventory.getPaperdollItem(PAPERDOLL_CHEST);
				if (chestItem == null)
				{
					if (sendInfo)
					{
						player.broadcastUserInfo(true);
					}
					else
					{
						_userInfoUpdate = true;
					}
					return;
				}

				final var armorSet = ArmorSetsParser.getInstance().getSet(chestItem.getId());
				if (armorSet == null)
				{
					if (sendInfo)
					{
						player.broadcastUserInfo(true);
					}
					else
					{
						_userInfoUpdate = true;
					}
					return;
				}

				if (armorSet.containItem(slot, item.getId()))
				{
					_skillsUpdate = true;
					skills = armorSet.getSkills();
					shieldSkill = armorSet.getShieldSkillId();
					skillId6 = armorSet.getEnchant6skillId();
					skillIdByLevel = armorSet.getEnchantByLevel();
				}
				else if (armorSet.containShield(item.getId()))
				{
					_skillsUpdate = true;
					shieldSkill = armorSet.getShieldSkillId();
				}
			}

			if (_skillsUpdate)
			{
				if (skills != null)
				{
					for (final var holder : skills)
					{
						itemSkill = holder.getSkill();
						if (itemSkill != null && player.getKnownSkill(itemSkill.getId()) != null)
						{
							player.removeSkill(itemSkill, false, itemSkill.isPassive());
						}
					}
				}

				if (shieldSkill != null)
				{
					for (final var holder : shieldSkill)
					{
						itemSkill = holder.getSkill();
						if (itemSkill != null)
						{
							player.removeSkill(itemSkill, false, itemSkill.isPassive());
						}
					}
				}

				if (skillId6 != null)
				{
					for (final var holder : skillId6)
					{
						itemSkill = holder.getSkill();
						if (itemSkill != null)
						{
							player.removeSkill(itemSkill, false, itemSkill.isPassive());
						}
					}
				}
				
				if ((skillIdByLevel != null) && !skillIdByLevel.isEmpty())
				{
					for (final int enchLvl : skillIdByLevel.keySet())
					{
						itemSkill = skillIdByLevel.get(enchLvl).getSkill();
						if (itemSkill != null)
						{
							player.removeSkill(itemSkill, false, itemSkill.isPassive());
						}
					}
				}
				player.checkItemRestriction(sendInfo);
			}
			
			if (sendInfo)
			{
				if (_skillsUpdate)
				{
					player.sendSkillList(_timeStampUpdate);
					_skillsUpdate = false;
					_timeStampUpdate = false;
				}
				player.broadcastUserInfo(true);
			}
			else
			{
				_userInfoUpdate = true;
			}
		}
	}

	private static final class BraceletListener implements PaperdollListener
	{
		private static BraceletListener instance = new BraceletListener();

		public static BraceletListener getInstance()
		{
			return instance;
		}

		@Override
		public void notifyUnequiped(int slot, ItemInstance item, Inventory inventory, boolean sendInfo)
		{
			if (item.getItem().getBodyPart() == Item.SLOT_R_BRACELET)
			{
				inventory.unEquipItemInSlot(PAPERDOLL_DECO1, sendInfo);
				inventory.unEquipItemInSlot(PAPERDOLL_DECO2, sendInfo);
				inventory.unEquipItemInSlot(PAPERDOLL_DECO3, sendInfo);
				inventory.unEquipItemInSlot(PAPERDOLL_DECO4, sendInfo);
				inventory.unEquipItemInSlot(PAPERDOLL_DECO5, sendInfo);
				inventory.unEquipItemInSlot(PAPERDOLL_DECO6, sendInfo);
			}
		}

		@Override
		public void notifyEquiped(int slot, ItemInstance item, Inventory inventory, boolean sendInfo)
		{
			final var player = inventory.getOwner().getActingPlayer();
			if (player == null)
			{
				return;
			}
			
			if (sendInfo)
			{
				if (item.getItem().isAccessory() || item.getItem().isTalisman() || item.getItem().isBracelet())
				{
					player.sendUserInfo(true);
				}
				else
				{
					player.broadcastCharInfo(UserInfoType.SLOTS, UserInfoType.SPEED, UserInfoType.STATS, UserInfoType.MAX_HPCPMP);
				}
			}
			else
			{
				if (item.getItem().isBracelet())
				{
					player.calcStat(Stats.TALISMAN_SLOTS, 0, null, null);
				}
			}
		}
	}

	protected Inventory()
	{
		_paperdoll = new ItemInstance[PAPERDOLL_TOTALSLOTS];
		_paperdollListeners = new ArrayList<>();

		if (this instanceof PcInventory)
		{
			addPaperdollListener(ArmorSetListener.getInstance());
			addPaperdollListener(BowCrossRodListener.getInstance());
			addPaperdollListener(ItemSkillsListener.getInstance());
			addPaperdollListener(BraceletListener.getInstance());
		}
		addPaperdollListener(StatsListener.getInstance());

	}

	protected abstract ItemLocation getEquipLocation();

	public ChangeRecorder newRecorder()
	{
		return new ChangeRecorder(this);
	}

	public ItemInstance dropItem(String process, ItemInstance item, Player actor, Object reference)
	{
		if (item == null)
		{
			return null;
		}

		synchronized (item)
		{
			if (!_items.contains(item))
			{
				return null;
			}

			removeItem(item);
			item.setOwnerId(0);
			item.setItemLocation(ItemLocation.VOID);
			item.setLastChange(ItemInstance.REMOVED);

			item.updateDatabase();
			refreshWeight();
		}
		return item;
	}

	public ItemInstance dropItem(String process, int objectId, long count, Player actor, Object reference)
	{
		ItemInstance item = getItemByObjectId(objectId);
		if (item == null)
		{
			return null;
		}

		synchronized (item)
		{
			if (!_items.contains(item))
			{
				return null;
			}

			if (item.getCount() > count)
			{
				item.changeCount(process, -count, actor, reference);
				item.setLastChange(ItemInstance.MODIFIED);
				item.updateDatabase();

				item = ItemsParser.getInstance().createItem(process, item.getId(), count, actor, reference);
				item.updateDatabase();
				refreshWeight();
				return item;
			}
		}
		return dropItem(process, item, actor, reference);
	}

	@Override
	protected void addItem(ItemInstance item)
	{
		super.addItem(item);
		if (item.isEquipped())
		{
			equipItem(item, true, true);
		}
	}

	@Override
	public boolean removeItem(ItemInstance item)
	{
		for (int i = 0; i < _paperdoll.length; i++)
		{
			if (_paperdoll[i] == item)
			{
				unEquipItemInSlot(i, true);
			}
		}
		return super.removeItem(item);
	}

	public ItemInstance getPaperdollItem(int slot)
	{
		return _paperdoll[slot];
	}
	
	public ItemInstance[] getPaperdollItems()
	{
		return _paperdoll;
	}
	
	public boolean isPaperdollSlotEmpty(int slot)
	{
		return _paperdoll[slot] == null;
	}
	
	public int getPaperdollItem(ItemInstance item)
	{
		for (int i = 0; i < _paperdoll.length; i++)
		{
			if (_paperdoll[i] == item)
			{
				return i;
			}
		}
		return -1;
	}

	public static int getPaperdollIndex(int slot)
	{
		if (slot == Item.SLOT_UNDERWEAR)
		{
			return PAPERDOLL_UNDER;
		}
		else if (slot == Item.SLOT_R_EAR)
		{
			return PAPERDOLL_REAR;
		}
		else if ((slot == Item.SLOT_LR_EAR) || (slot == Item.SLOT_L_EAR))
		{
			return PAPERDOLL_LEAR;
		}
		else if (slot == Item.SLOT_NECK)
		{
			return PAPERDOLL_NECK;
		}
		else if ((slot == Item.SLOT_R_FINGER) || (slot == Item.SLOT_LR_FINGER))
		{
			return PAPERDOLL_RFINGER;
		}
		else if (slot == Item.SLOT_L_FINGER)
		{
			return PAPERDOLL_LFINGER;
		}
		else if (slot == Item.SLOT_HEAD)
		{
			return PAPERDOLL_HEAD;
		}
		else if ((slot == Item.SLOT_R_HAND) || (slot == Item.SLOT_LR_HAND))
		{
			return PAPERDOLL_RHAND;
		}
		else if (slot == Item.SLOT_L_HAND)
		{
			return PAPERDOLL_LHAND;
		}
		else if (slot == Item.SLOT_GLOVES)
		{
			return PAPERDOLL_GLOVES;
		}
		else if ((slot == Item.SLOT_CHEST) || (slot == Item.SLOT_FULL_ARMOR) || (slot == Item.SLOT_ALLDRESS))
		{
			return PAPERDOLL_CHEST;
		}
		else if (slot == Item.SLOT_LEGS)
		{
			return PAPERDOLL_LEGS;
		}
		else if (slot == Item.SLOT_FEET)
		{
			return PAPERDOLL_FEET;
		}
		else if (slot == Item.SLOT_BACK)
		{
			return PAPERDOLL_CLOAK;
		}
		else if ((slot == Item.SLOT_HAIR) || (slot == Item.SLOT_HAIRALL))
		{
			return PAPERDOLL_HAIR;
		}
		else if (slot == Item.SLOT_HAIR2)
		{
			return PAPERDOLL_HAIR2;
		}
		else if (slot == Item.SLOT_R_BRACELET)
		{
			return PAPERDOLL_RBRACELET;
		}
		else if (slot == Item.SLOT_L_BRACELET)
		{
			return PAPERDOLL_LBRACELET;
		}
		else if (slot == Item.SLOT_DECO)
		{
			return PAPERDOLL_DECO1;
		}
		else if (slot == Item.SLOT_BELT)
		{
			return PAPERDOLL_BELT;
		}
		return -1;
	}

	public ItemInstance getPaperdollItemByL2ItemId(int slot)
	{
		final int index = getPaperdollIndex(slot);
		if (index == -1)
		{
			return null;
		}
		return _paperdoll[index];
	}
	
	public ItemInstance getPaperdollItemByItemId(int slot)
	{
		final int index = getPaperdollIndex(slot);
		if (index == -1)
		{
			return null;
		}
		return _paperdoll[index];
	}

	public int getPaperdollItemId(int slot)
	{
		final ItemInstance item = _paperdoll[slot];
		if (item != null)
		{
			return item.getId();
		}
		return 0;
	}
	
	public int getPaperdollVisualItemId(int slot)
	{
		ItemInstance item = _paperdoll[slot];
		if (item != null)
		{
			return item.getId();
		}
		else if (slot == PAPERDOLL_HAIR)
		{
			item = _paperdoll[PAPERDOLL_HAIR2];
			if (item != null)
			{
				return item.getId();
			}
		}
		return 0;
	}

	public int getPaperdollItemDisplayId(int slot)
	{
		final ItemInstance item = _paperdoll[slot];
		return (item != null) ? item.getDisplayId() : 0;
	}

	public int getPaperdollAugmentationId(int slot)
	{
		final ItemInstance item = _paperdoll[slot];
		return ((item != null) && (item.getAugmentation() != null)) ? item.getAugmentation().getAugmentationId() : 0;
	}

	public int getPaperdollObjectId(int slot)
	{
		final ItemInstance item = _paperdoll[slot];
		return (item != null) ? item.getObjectId() : 0;
	}

	public synchronized void addPaperdollListener(PaperdollListener listener)
	{
		assert !_paperdollListeners.contains(listener);
		_paperdollListeners.add(listener);
	}

	public synchronized void removePaperdollListener(PaperdollListener listener)
	{
		_paperdollListeners.remove(listener);
	}

	public synchronized ItemInstance setPaperdollItem(int slot, ItemInstance item, boolean sendInfo)
	{
		final ItemInstance old = _paperdoll[slot];
		final var owner = getOwner();
		if (old != item)
		{
			if (old != null)
			{
				if (owner != null && owner.isPlayer())
				{
					if (old.isEventItem() && owner.getActingPlayer().isInFightEvent())
					{
						return old;
					}
				}
				_paperdoll[slot] = null;

				old.setItemLocation(getBaseLocation());
				old.setLastChange(ItemInstance.MODIFIED);

				int mask = 0;
				final int slots = PAPERDOLL_TOTALSLOTS;
				for (int i = 0; i < slots; i++)
				{
					final ItemInstance pi = _paperdoll[i];
					if (pi != null)
					{
						mask |= pi.getItem().getItemMask();
					}
				}
				_wearedMask = mask;
				_paperdollListeners.stream().filter(l -> l != null).forEach(l -> l.notifyUnequiped(slot, old, this, sendInfo));
				old.updateDatabase();
				if (owner != null && owner.isPlayer())
				{
					owner.getActingPlayer().getListeners().onItemUnEquipListener(old);
				}
			}

			if (item != null)
			{
				_paperdoll[slot] = item;
				item.setItemLocation(getEquipLocation(), slot);
				item.setLastChange(ItemInstance.MODIFIED);
				_wearedMask |= item.getItem().getItemMask();
				_paperdollListeners.stream().filter(l -> l != null).forEach(l -> l.notifyEquiped(slot, item, this, sendInfo));
				item.updateDatabase();
				if (owner != null && owner.isPlayer())
				{
					owner.getActingPlayer().getListeners().onItemEquipListener(item);
				}
			}
		}
		return old;
	}

	public int getWearedMask()
	{
		return _wearedMask;
	}

	public int getSlotFromItem(ItemInstance item)
	{
		int slot = -1;
		final int location = item.getLocationSlot();
		switch (location)
		{
			case PAPERDOLL_UNDER :
				slot = Item.SLOT_UNDERWEAR;
				break;
			case PAPERDOLL_LEAR :
				slot = Item.SLOT_L_EAR;
				break;
			case PAPERDOLL_REAR :
				slot = Item.SLOT_R_EAR;
				break;
			case PAPERDOLL_NECK :
				slot = Item.SLOT_NECK;
				break;
			case PAPERDOLL_RFINGER :
				slot = Item.SLOT_R_FINGER;
				break;
			case PAPERDOLL_LFINGER :
				slot = Item.SLOT_L_FINGER;
				break;
			case PAPERDOLL_HAIR :
				slot = Item.SLOT_HAIR;
				break;
			case PAPERDOLL_HAIR2 :
				slot = Item.SLOT_HAIR2;
				break;
			case PAPERDOLL_HEAD :
				slot = Item.SLOT_HEAD;
				break;
			case PAPERDOLL_RHAND :
				slot = Item.SLOT_R_HAND;
				break;
			case PAPERDOLL_LHAND :
				slot = Item.SLOT_L_HAND;
				break;
			case PAPERDOLL_GLOVES :
				slot = Item.SLOT_GLOVES;
				break;
			case PAPERDOLL_CHEST :
				slot = item.getItem().getBodyPart();
				break;
			case PAPERDOLL_LEGS :
				slot = Item.SLOT_LEGS;
				break;
			case PAPERDOLL_CLOAK :
				slot = Item.SLOT_BACK;
				break;
			case PAPERDOLL_FEET :
				slot = Item.SLOT_FEET;
				break;
			case PAPERDOLL_LBRACELET :
				slot = Item.SLOT_L_BRACELET;
				break;
			case PAPERDOLL_RBRACELET :
				slot = Item.SLOT_R_BRACELET;
				break;
			case PAPERDOLL_DECO1 :
			case PAPERDOLL_DECO2 :
			case PAPERDOLL_DECO3 :
			case PAPERDOLL_DECO4 :
			case PAPERDOLL_DECO5 :
			case PAPERDOLL_DECO6 :
				slot = Item.SLOT_DECO;
				break;
			case PAPERDOLL_BELT :
				slot = Item.SLOT_BELT;
				break;
		}
		return slot;
	}

	public void unEquipItem(ItemInstance item, boolean sendInfo)
	{
		if (getOwner() != null && getOwner().isPlayer())
		{
			if (item.isEventItem() && getOwner().getActingPlayer().isInFightEvent())
			{
				return;
			}
		}
		
		if (item.isEquipped())
		{
			unEquipItemInBodySlot(item.getItem().getBodyPart(), sendInfo);
		}
	}
	
	public void unEquipEventItem(ItemInstance item, boolean sendInfo)
	{
		if (item.isEquipped())
		{
			unEquipItemInBodySlot(item.getItem().getBodyPart(), sendInfo);
		}
	}

	public List<ItemInstance> unEquipItemInBodySlotAndRecord(int slot, boolean sendInfo)
	{
		final Inventory.ChangeRecorder recorder = newRecorder();

		try
		{
			unEquipItemInBodySlot(slot, sendInfo);
		}
		finally
		{
			removePaperdollListener(recorder);
		}
		return recorder.getChangedItems();
	}

	public ItemInstance unEquipItemInSlot(int pdollSlot, boolean sendInfo)
	{
		return setPaperdollItem(pdollSlot, null, sendInfo);
	}

	public List<ItemInstance> unEquipItemInSlotAndRecord(int slot, boolean sendInfo)
	{
		final Inventory.ChangeRecorder recorder = newRecorder();

		try
		{
			unEquipItemInSlot(slot, sendInfo);
			if (getOwner().isPlayer())
			{
				((Player) getOwner()).refreshExpertisePenalty();
			}
		}
		finally
		{
			removePaperdollListener(recorder);
		}
		return recorder.getChangedItems();
	}

	public ItemInstance unEquipItemInBodySlot(int slot, boolean sendInfo)
	{
		if (Config.DEBUG)
		{
			_log.info(Inventory.class.getSimpleName() + ": Unequip body slot:" + slot);
		}

		int pdollSlot = -1;

		if (slot == Item.SLOT_L_EAR)
		{
			pdollSlot = PAPERDOLL_LEAR;
		}
		else if (slot == Item.SLOT_R_EAR)
		{
			pdollSlot = PAPERDOLL_REAR;
		}
		else if (slot == Item.SLOT_NECK)
		{
			pdollSlot = PAPERDOLL_NECK;
		}
		else if (slot == Item.SLOT_R_FINGER)
		{
			pdollSlot = PAPERDOLL_RFINGER;
		}
		else if (slot == Item.SLOT_L_FINGER)
		{
			pdollSlot = PAPERDOLL_LFINGER;
		}
		else if (slot == Item.SLOT_HAIR)
		{
			pdollSlot = PAPERDOLL_HAIR;
		}
		else if (slot == Item.SLOT_HAIR2)
		{
			pdollSlot = PAPERDOLL_HAIR2;
		}
		else if (slot == Item.SLOT_HAIRALL)
		{
			setPaperdollItem(PAPERDOLL_HAIR, null, sendInfo);
			pdollSlot = PAPERDOLL_HAIR;
		}
		else if (slot == Item.SLOT_HEAD)
		{
			pdollSlot = PAPERDOLL_HEAD;
		}
		else if ((slot == Item.SLOT_R_HAND) || (slot == Item.SLOT_LR_HAND))
		{
			pdollSlot = PAPERDOLL_RHAND;
		}
		else if (slot == Item.SLOT_L_HAND)
		{
			pdollSlot = PAPERDOLL_LHAND;
		}
		else if (slot == Item.SLOT_GLOVES)
		{
			pdollSlot = PAPERDOLL_GLOVES;
		}
		else if ((slot == Item.SLOT_CHEST) || (slot == Item.SLOT_ALLDRESS) || (slot == Item.SLOT_FULL_ARMOR))
		{
			pdollSlot = PAPERDOLL_CHEST;
		}
		else if (slot == Item.SLOT_LEGS)
		{
			pdollSlot = PAPERDOLL_LEGS;
		}
		else if (slot == Item.SLOT_BACK)
		{
			pdollSlot = PAPERDOLL_CLOAK;
		}
		else if (slot == Item.SLOT_FEET)
		{
			pdollSlot = PAPERDOLL_FEET;
		}
		else if (slot == Item.SLOT_UNDERWEAR)
		{
			pdollSlot = PAPERDOLL_UNDER;
		}
		else if (slot == Item.SLOT_L_BRACELET)
		{
			pdollSlot = PAPERDOLL_LBRACELET;
		}
		else if (slot == Item.SLOT_R_BRACELET)
		{
			pdollSlot = PAPERDOLL_RBRACELET;
		}
		else if (slot == Item.SLOT_DECO)
		{
			pdollSlot = PAPERDOLL_DECO1;
		}
		else if (slot == Item.SLOT_BELT)
		{
			pdollSlot = PAPERDOLL_BELT;
		}
		else
		{
			if (Config.DEBUG)
			{
				_log.info("Unhandled slot type: " + slot);
				_log.info(StringUtil.getTraceString(Thread.currentThread().getStackTrace()));
			}
		}
		
		if (pdollSlot >= 0)
		{
			final ItemInstance old = setPaperdollItem(pdollSlot, null, sendInfo);
			if ((old != null) && getOwner().isPlayer())
			{
				((Player) getOwner()).refreshExpertisePenalty();
			}
			return old;
		}
		return null;
	}

	public List<ItemInstance> equipItemAndRecord(ItemInstance item, boolean sendInfo)
	{
		final Inventory.ChangeRecorder recorder = newRecorder();

		try
		{
			equipItem(item, sendInfo, true);
		}
		finally
		{
			removePaperdollListener(recorder);
		}
		return recorder.getChangedItems();
	}

	public void equipItem(ItemInstance item, boolean sendInfo, boolean checkCondition)
	{
		if ((getOwner().isPlayer()) && (((Player) getOwner()).getPrivateStoreType() != Player.STORE_PRIVATE_NONE))
		{
			return;
		}

		if (getOwner().isPlayer())
		{
			final Player player = (Player) getOwner();
			if (item.isEventItem() && !player.isInFightEvent())
			{
				return;
			}
			if (!player.canOverrideCond(PcCondOverride.ITEM_CONDITIONS) && !player.isHero() && item.isHeroItem())
			{
				return;
			}
		}

		final int targetSlot = item.getItem().getBodyPart();

		final ItemInstance formal = getPaperdollItem(PAPERDOLL_CHEST);
		if ((item.getId() != 21163) && (formal != null) && (formal.getItem().getBodyPart() == Item.SLOT_ALLDRESS))
		{
			if (formal.getItem().isCostume())
			{
				if ((targetSlot == Item.SLOT_LEGS) || (targetSlot == Item.SLOT_FEET) || (targetSlot == Item.SLOT_GLOVES) || (targetSlot == Item.SLOT_HEAD))
				{
					return;
				}
			}
			else
			{
				if ((targetSlot == Item.SLOT_LR_HAND) || (targetSlot == Item.SLOT_L_HAND) || (targetSlot == Item.SLOT_R_HAND) || (targetSlot == Item.SLOT_LEGS) || (targetSlot == Item.SLOT_FEET) || (targetSlot == Item.SLOT_GLOVES) || (targetSlot == Item.SLOT_HEAD))
				{
					return;
				}
			}
		}

		if (targetSlot == Item.SLOT_LR_HAND)
		{
			setPaperdollItem(PAPERDOLL_LHAND, null, sendInfo);
			setPaperdollItem(PAPERDOLL_RHAND, item, sendInfo);
		}
		else if (targetSlot == Item.SLOT_L_HAND)
		{
			final ItemInstance rh = getPaperdollItem(PAPERDOLL_RHAND);
			if ((rh != null) && (rh.getItem().getBodyPart() == Item.SLOT_LR_HAND) && !(((rh.getItemType() == WeaponType.BOW) && (item.getItemType() == EtcItemType.ARROW)) || (rh.getItemType() == WeaponType.CROSSBOW && (item.getItemType() == EtcItemType.BOLT)) || ((rh.getItemType() == WeaponType.FISHINGROD) && (item.getItemType() == EtcItemType.LURE))))
			{
				setPaperdollItem(PAPERDOLL_RHAND, null, sendInfo);
			}
			setPaperdollItem(PAPERDOLL_LHAND, item, sendInfo);
		}
		else if (targetSlot == Item.SLOT_R_HAND)
		{
			setPaperdollItem(PAPERDOLL_RHAND, item, sendInfo);
		}
		else if ((targetSlot == Item.SLOT_L_EAR) || (targetSlot == Item.SLOT_R_EAR) || (targetSlot == Item.SLOT_LR_EAR))
		{
			if (_paperdoll[PAPERDOLL_LEAR] == null)
			{
				setPaperdollItem(PAPERDOLL_LEAR, item, sendInfo);
			}
			else if (_paperdoll[PAPERDOLL_REAR] == null)
			{
				setPaperdollItem(PAPERDOLL_REAR, item, sendInfo);
			}
			else
			{
				setPaperdollItem(PAPERDOLL_LEAR, item, sendInfo);
			}
		}
		else if ((targetSlot == Item.SLOT_L_FINGER) || (targetSlot == Item.SLOT_R_FINGER) || (targetSlot == Item.SLOT_LR_FINGER))
		{
			if (_paperdoll[PAPERDOLL_LFINGER] == null)
			{
				setPaperdollItem(PAPERDOLL_LFINGER, item, sendInfo);
			}
			else if (_paperdoll[PAPERDOLL_RFINGER] == null)
			{
				setPaperdollItem(PAPERDOLL_RFINGER, item, sendInfo);
			}
			else
			{
				setPaperdollItem(PAPERDOLL_LFINGER, item, sendInfo);
			}
		}
		else if (targetSlot == Item.SLOT_NECK)
		{
			setPaperdollItem(PAPERDOLL_NECK, item, sendInfo);
		}
		else if (targetSlot == Item.SLOT_FULL_ARMOR)
		{
			setPaperdollItem(PAPERDOLL_LEGS, null, sendInfo);
			setPaperdollItem(PAPERDOLL_CHEST, item, sendInfo);
		}
		else if (targetSlot == Item.SLOT_CHEST)
		{
			setPaperdollItem(PAPERDOLL_CHEST, item, sendInfo);
			
		}
		else if (targetSlot == Item.SLOT_LEGS)
		{
			final ItemInstance chest = getPaperdollItem(PAPERDOLL_CHEST);
			if ((chest != null) && (chest.getItem().getBodyPart() == Item.SLOT_FULL_ARMOR))
			{
				setPaperdollItem(PAPERDOLL_CHEST, null, sendInfo);
			}
			setPaperdollItem(PAPERDOLL_LEGS, item, sendInfo);
		}
		else if (targetSlot == Item.SLOT_FEET)
		{
			setPaperdollItem(PAPERDOLL_FEET, item, sendInfo);
		}
		else if (targetSlot == Item.SLOT_GLOVES)
		{
			setPaperdollItem(PAPERDOLL_GLOVES, item, sendInfo);
		}
		else if (targetSlot == Item.SLOT_HEAD)
		{
			setPaperdollItem(PAPERDOLL_HEAD, item, sendInfo);
		}
		else if (targetSlot == Item.SLOT_HAIR)
		{
			final ItemInstance hair = getPaperdollItem(PAPERDOLL_HAIR);
			if ((hair != null) && (hair.getItem().getBodyPart() == Item.SLOT_HAIRALL))
			{
				setPaperdollItem(PAPERDOLL_HAIR2, null, sendInfo);
			}
			else
			{
				setPaperdollItem(PAPERDOLL_HAIR, null, sendInfo);
			}
			setPaperdollItem(PAPERDOLL_HAIR, item, sendInfo);
		}
		else if (targetSlot == Item.SLOT_HAIR2)
		{
			final ItemInstance hair2 = getPaperdollItem(PAPERDOLL_HAIR);
			if ((hair2 != null) && (hair2.getItem().getBodyPart() == Item.SLOT_HAIRALL))
			{
				setPaperdollItem(PAPERDOLL_HAIR, null, sendInfo);
			}
			else
			{
				setPaperdollItem(PAPERDOLL_HAIR2, null, sendInfo);
			}
			setPaperdollItem(PAPERDOLL_HAIR2, item, sendInfo);
		}
		else if (targetSlot == Item.SLOT_HAIRALL)
		{
			setPaperdollItem(PAPERDOLL_HAIR2, null, sendInfo);
			setPaperdollItem(PAPERDOLL_HAIR, item, sendInfo);
		}
		else if (targetSlot == Item.SLOT_UNDERWEAR)
		{
			setPaperdollItem(PAPERDOLL_UNDER, item, sendInfo);
		}
		else if (targetSlot == Item.SLOT_BACK)
		{
			setPaperdollItem(PAPERDOLL_CLOAK, item, sendInfo);
		}
		else if (targetSlot == Item.SLOT_L_BRACELET)
		{
			setPaperdollItem(PAPERDOLL_LBRACELET, item, sendInfo);
		}
		else if (targetSlot == Item.SLOT_R_BRACELET)
		{
			setPaperdollItem(PAPERDOLL_RBRACELET, item, sendInfo);
		}
		else if (targetSlot == Item.SLOT_DECO)
		{
			equipTalisman(item, sendInfo, checkCondition);
		}
		else if (targetSlot == Item.SLOT_BELT)
		{
			setPaperdollItem(PAPERDOLL_BELT, item, sendInfo);
		}
		else if (targetSlot == Item.SLOT_ALLDRESS)
		{
			setPaperdollItem(PAPERDOLL_LEGS, null, sendInfo);
			setPaperdollItem(PAPERDOLL_LHAND, null, sendInfo);
			setPaperdollItem(PAPERDOLL_RHAND, null, sendInfo);
			setPaperdollItem(PAPERDOLL_RHAND, null, sendInfo);
			setPaperdollItem(PAPERDOLL_LHAND, null, sendInfo);
			setPaperdollItem(PAPERDOLL_HEAD, null, sendInfo);
			setPaperdollItem(PAPERDOLL_FEET, null, sendInfo);
			setPaperdollItem(PAPERDOLL_GLOVES, null, sendInfo);
			setPaperdollItem(PAPERDOLL_CHEST, item, sendInfo);
		}
		else
		{
			_log.warn("Unknown body slot " + targetSlot + " for Item ID:" + item.getId());
		}
	}

	@Override
	protected void refreshWeight()
	{
		long weight = 0;

		for (final ItemInstance item : _items)
		{
			if ((item != null) && (item.getItem() != null))
			{
				weight += item.getItem().getWeight() * item.getCount();
			}
		}
		_totalWeight = Math.max((int) Math.min(weight - getOwner().getBonusWeightPenalty(), Integer.MAX_VALUE), 0);
	}

	public int getTotalWeight()
	{
		return _totalWeight;
	}
	
	public void reduceAmmunitionCount(EtcItemType type)
	{
	}
	
	public boolean reduceShortsCount(ItemInstance item, int count)
	{
		return false;
	}
	
	public boolean reduceItemsCount(String process, ItemInstance item, int count)
	{
		return false;
	}
	
	public ItemInstance findArrowForBow(Item bow)
	{
		if (bow != null)
		{
			for (final var item : getItems())
			{
				if (item != null && item.isEtcItem() && (item.getItem().getItemGradeSPlus() == bow.getItemGradeSPlus()) && (item.getEtcItem().getItemType() == EtcItemType.ARROW))
				{
					return item;
				}
			}
		}
		return null;
	}

	public ItemInstance findBoltForCrossBow(Item crossbow)
	{
		if (crossbow != null)
		{
			for (final var item : getItems())
			{
				if (item != null && item.isEtcItem() && (item.getItem().getItemGradeSPlus() == crossbow.getItemGradeSPlus()) && (item.getEtcItem().getItemType() == EtcItemType.BOLT))
				{
					return item;
				}
			}
		}
		return null;
	}

	@Override
	public void restore()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet inv = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT object_id, item_id, count, enchant_level, loc, loc_data, custom_type1, custom_type2, mana_left, time, visual_itemId, agathion_energy, is_event FROM items WHERE owner_id=? AND (loc=? OR loc=?) ORDER BY loc_data");
			statement.setInt(1, getOwnerId());
			statement.setString(2, getBaseLocation().name());
			statement.setString(3, getEquipLocation().name());
			inv = statement.executeQuery();

			ItemInstance item;
			while (inv.next())
			{
				item = ItemInstance.restoreFromDb(getOwnerId(), inv);
				if (item == null)
				{
					continue;
				}

				if (getOwner().isPlayer())
				{
					final Player player = (Player) getOwner();

					if (!player.canOverrideCond(PcCondOverride.ITEM_CONDITIONS) && !player.isHero() && item.isHeroItem())
					{
						item.setItemLocation(ItemLocation.INVENTORY);
					}
				}

				GameObjectsStorage.addItem(item);
				
				if (item.isStackable() && (getItemByItemId(item.getId()) != null))
				{
					addItem("Restore", item, getOwner().getActingPlayer(), null);
				}
				else
				{
					addItem(item);
				}
			}
			refreshWeight();
		}
		catch (final Exception e)
		{
			_log.warn("Could not restore inventory: " + e.getMessage(), e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, inv);
		}
	}

	public int getMaxTalismanCount()
	{
		return (int) getOwner().getStat().calcStat(Stats.TALISMAN_SLOTS, 0, null, null);
	}

	private void equipTalisman(ItemInstance item, boolean sendInfo, boolean checkCondition)
	{
		final var count = checkCondition ? getMaxTalismanCount() : 6;
		if (count == 0 && checkCondition)
		{
			return;
		}

		for (int i = PAPERDOLL_DECO1; i < (PAPERDOLL_DECO1 + count); i++)
		{
			if (_paperdoll[i] != null)
			{
				if (getPaperdollItemId(i) == item.getId())
				{
					setPaperdollItem(i, item, sendInfo);
					return;
				}
			}
		}

		for (int i = PAPERDOLL_DECO1; i < (PAPERDOLL_DECO1 + count); i++)
		{
			if (_paperdoll[i] == null)
			{
				setPaperdollItem(i, item, sendInfo);
				return;
			}
		}
		setPaperdollItem(PAPERDOLL_DECO1, item, sendInfo);
	}

	public int getCloakStatus()
	{
		if (Config.ALLOW_OPEN_CLOAK_SLOT)
		{
			return 1;
		}
		return (int) getOwner().getStat().calcStat(Stats.CLOAK_SLOT, 0, null, null);
	}
	
	public int getHeroStatus()
	{
		return (int) getOwner().getStat().calcStat(Stats.HERO_STATUS, 0, null, null);
	}

	public void reloadEquippedItems()
	{
		int slot;
		for (final ItemInstance item : _paperdoll)
		{
			if (item == null)
			{
				continue;
			}

			slot = item.getLocationSlot();
			for (final PaperdollListener listener : _paperdollListeners)
			{
				if (listener == null)
				{
					continue;
				}

				listener.notifyUnequiped(slot, item, this, false);
				listener.notifyEquiped(slot, item, this, false);
			}
		}
		_userInfoUpdate = true;
		inventoryUpdate();
	}
	
	public boolean isItemEquipped(int itemId)
	{
		for (final ItemInstance item : getItemsByItemId(itemId))
		{
			if (item != null && item.isEquipped())
			{
				return true;
			}
		}
		return false;
	}
	
	public void inventoryUpdate()
	{
		final var owner = getOwner();
		if (owner != null && owner.isPlayer())
		{
			if (_skillsUpdate)
			{
				owner.getActingPlayer().sendSkillList(_timeStampUpdate);
				_skillsUpdate = false;
				_timeStampUpdate = false;
			}
			
			if (_userInfoUpdate)
			{
				owner.getActingPlayer().broadcastUserInfo(true);
				_userInfoUpdate = false;
			}
		}
	}
}