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
package gameserver.model.actor.instance.player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import gameserver.Config;
import gameserver.instancemanager.VipManager;
import gameserver.model.actor.Player;
import gameserver.model.actor.templates.player.vip.VipTemplate;
import gameserver.model.base.BonusType;

public class PremiumBonus
{
	private int _premiumId = 0;
	private boolean _isOnlineType;
	private boolean _isPersonal;
	private long _onlineTime;
	private boolean _isActive;
	private int _groupId;
	
	private final Map<BonusType, Double> _bonusList = new ConcurrentHashMap<>();
	private VipTemplate _vipTemplate = null;
	private final Player _player;
	
	public PremiumBonus(Player player)
	{
		_player = player;
	}
	
	public int getPremiumId()
	{
		return _premiumId;
	}
	
	public void setPremiumId(final int premiumId)
	{
		_premiumId = premiumId;
	}
	
	public int getGroupId()
	{
		return _groupId;
	}
	
	public void setGroupId(final int groupId)
	{
		_groupId = groupId;
	}

	public double getRateXp()
	{
		return getBonusType(BonusType.EXP, 1.) * (_vipTemplate != null ? _vipTemplate.getExpRate() : 1) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.EXP, 1.) : 1);
	}

	public double getRateSp()
	{
		return getBonusType(BonusType.SP, 1.) * (_vipTemplate != null ? _vipTemplate.getSpRate() : 1) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.SP, 1.) : 1);
	}

	public double getQuestExpRewardRate()
	{
		return getBonusType(BonusType.QUEST_EXP_REWARD, 1.) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.QUEST_EXP_REWARD, 1.) : 1);
	}
	
	public double getQuestSpRewardRate()
	{
		return getBonusType(BonusType.QUEST_SP_REWARD, 1.) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.QUEST_SP_REWARD, 1.) : 1);
	}
	
	public double getQuestRewardRate()
	{
		return getBonusType(BonusType.QUEST_REWARD, 1.) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.QUEST_REWARD, 1.) : 1);
	}

	public double getQuestDropRate()
	{
		return getBonusType(BonusType.QUEST_DROP, 1.) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.QUEST_DROP, 1.) : 1);
	}

	public double getDropAdena()
	{
		return getBonusType(BonusType.DROP_ADENA, 1.) * (_vipTemplate != null ? _vipTemplate.getAdenaRate() : 1) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.DROP_ADENA, 1.) : 1);
	}

	public double getDropItems()
	{
		return getBonusType(BonusType.DROP_ITEMS, 1.) * (_vipTemplate != null ? _vipTemplate.getDropRate() : 1) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.DROP_ITEMS, 1.) : 1);
	}

	public double getDropSpoil()
	{
		return getBonusType(BonusType.SPOIL, 1.) * (_vipTemplate != null ? _vipTemplate.getSpoilRate() : 1) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.SPOIL, 1.) : 1);
	}

	public double getDropSiege()
	{
		return getBonusType(BonusType.SIEGE, 1.) * (_vipTemplate != null ? _vipTemplate.getEpRate() : 1) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.SIEGE, 1.) : 1);
	}

	public double getDropElementStones()
	{
		return getBonusType(BonusType.ELEMENT_STONE, 1.) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.ELEMENT_STONE, 1.) : 1);
	}

	public double getWeight()
	{
		return getBonusType(BonusType.WEIGHT, 1.) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.WEIGHT, 1.) : 1);
	}

	public int getCraftChance()
	{
		return (int) (getBonusType(BonusType.CRAFT_CHANCE, 0.) + (_player.hasClan() ? _player.getClan().getBonusType(BonusType.CRAFT_CHANCE, 0.) : 0));
	}

	public int getMasterWorkChance()
	{
		return (int) (getBonusType(BonusType.MASTER_WORK_CHANCE, 0.) + (_player.hasClan() ? _player.getClan().getBonusType(BonusType.MASTER_WORK_CHANCE, 0.) : 0));
	}

	public double getFishingRate()
	{
		return getBonusType(BonusType.FISHING, 1.) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.FISHING, 1.) : 1);
	}
	
	public double getDropRaids()
	{
		return getBonusType(BonusType.DROP_RAID, 1.) * (_vipTemplate != null ? _vipTemplate.getDropRaidRate() : 1) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.DROP_RAID, 1.) : 1);
	}
	
	public double getDropEpics()
	{
		return getBonusType(BonusType.DROP_EPIC, 1.) * (_vipTemplate != null ? _vipTemplate.getDropRaidRate() : 1) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.DROP_EPIC, 1.) : 1);
	}
	
	public int getEnchantChance()
	{
		return (int) (getBonusType(BonusType.ENCHANT_CHANCE, 0.) + (_vipTemplate != null ? _vipTemplate.getEnchantChance() : 0) + (_player.hasClan() ? _player.getClan().getBonusType(BonusType.ENCHANT_CHANCE, 0.) : 0));
	}
	
	public double getFameBonus()
	{
		return getBonusType(BonusType.FAME, 1.) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.FAME, 1.) : 1);
	}
	
	public double getReflectionReduce()
	{
		return getBonusType(BonusType.REFLECTION_REDUCE, 1.) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.REFLECTION_REDUCE, 1.) : 1);
	}
	
	public double getEventBonus()
	{
		return getBonusType(BonusType.EVENTS, 1.) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.EVENTS, 1.) : 1);
	}
	
	public boolean isOnlineType()
	{
		return _isOnlineType;
	}
	
	public void setOnlineType(boolean isOnlineType)
	{
		_isOnlineType = isOnlineType;
	}
	
	public long getOnlineTime()
	{
		return _onlineTime;
	}
	
	public void setOnlineTime(final long onlineTime)
	{
		_onlineTime = onlineTime;
	}
	
	public boolean isActive()
	{
		return _isActive;
	}
	
	public void setActivate(boolean isActive)
	{
		_isActive = isActive;
	}
	
	public double getDropSealStones()
	{
		return getBonusType(BonusType.SEAL_STONE, 1.) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.SEAL_STONE, 1.) : 1);
	}
	
	public double getMinNobleStonesCount()
	{
		return getBonusType(BonusType.NOBLE_STONE_MIN_AMOUNT, Config.RATE_NOBLE_STONES_COUNT_MIN) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.NOBLE_STONE_MIN_AMOUNT, 1.) : 1);
	}
	
	public double getMaxNobleStonesCount()
	{
		return getBonusType(BonusType.NOBLE_STONE_MAX_AMOUNT, Config.RATE_NOBLE_STONES_COUNT_MAX) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.NOBLE_STONE_MAX_AMOUNT, 1.) : 1);
	}
	
	public double getMinSealStonesCount()
	{
		return getBonusType(BonusType.SEAL_STONE_MIN_AMOUNT, Config.RATE_SEAL_STONES_COUNT_MIN) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.SEAL_STONE_MIN_AMOUNT, 1.) : 1);
	}
	
	public double getMaxSealStonesCount()
	{
		return getBonusType(BonusType.SEAL_STONE_MAX_AMOUNT, Config.RATE_SEAL_STONES_COUNT_MAX) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.SEAL_STONE_MAX_AMOUNT, 1.) : 1);
	}
	
	public double getMinLifeStonesCount()
	{
		return getBonusType(BonusType.LIFE_STONE_MIN_AMOUNT, Config.RATE_LIFE_STONES_COUNT_MIN) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.LIFE_STONE_MIN_AMOUNT, 1.) : 1);
	}
	
	public double getMaxLifeStonesCount()
	{
		return getBonusType(BonusType.LIFE_STONE_MAX_AMOUNT, Config.RATE_LIFE_STONES_COUNT_MAX) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.LIFE_STONE_MAX_AMOUNT, 1.) : 1);
	}
	
	public double getMinEnchantScrollsCount()
	{
		return getBonusType(BonusType.ENCHANT_SCROLL_MIN_AMOUNT, Config.RATE_ENCHANT_SCROLLS_COUNT_MIN) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.ENCHANT_SCROLL_MIN_AMOUNT, 1.) : 1);
	}
	
	public double getMaxEnchantScrollsCount()
	{
		return getBonusType(BonusType.ENCHANT_SCROLL_MAX_AMOUNT, Config.RATE_ENCHANT_SCROLLS_COUNT_MAX) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.ENCHANT_SCROLL_MAX_AMOUNT, 1.) : 1);
	}
	
	public double getMinForgottenScrollsCount()
	{
		return getBonusType(BonusType.FORGOTTEN_SCROLL_MIN_AMOUNT, Config.RATE_FORGOTTEN_SCROLLS_COUNT_MIN) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.FORGOTTEN_SCROLL_MIN_AMOUNT, 1.) : 1);
	}
	
	public double getMaxForgottenScrollsCount()
	{
		return getBonusType(BonusType.FORGOTTEN_SCROLL_MAX_AMOUNT, Config.RATE_FORGOTTEN_SCROLLS_COUNT_MAX) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.FORGOTTEN_SCROLL_MAX_AMOUNT, 1.) : 1);
	}
	
	public double getMinMaterialsCount()
	{
		return getBonusType(BonusType.MATERIAL_MIN_AMOUNT, Config.RATE_KEY_MATHETIRALS_COUNT_MIN) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.MATERIAL_MIN_AMOUNT, 1.) : 1);
	}
	
	public double getMaxMaterialsCount()
	{
		return getBonusType(BonusType.MATERIAL_MAX_AMOUNT, Config.RATE_KEY_MATHETIRALS_COUNT_MAX) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.MATERIAL_MAX_AMOUNT, 1.) : 1);
	}
	
	public double getMinRepicesCount()
	{
		return getBonusType(BonusType.RECIPE_MIN_AMOUNT, Config.RATE_RECEPIES_COUNT_MIN) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.RECIPE_MIN_AMOUNT, 1.) : 1);
	}
	
	public double getMaxRepicesCount()
	{
		return getBonusType(BonusType.RECIPE_MIN_AMOUNT, Config.RATE_RECEPIES_COUNT_MAX) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.RECIPE_MAX_AMOUNT, 1.) : 1);
	}
	
	public double getMinBeltsCount()
	{
		return getBonusType(BonusType.BELT_MIN_AMOUNT, Config.RATE_BELTS_COUNT_MIN) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.BELT_MIN_AMOUNT, 1.) : 1);
	}
	
	public double getMaxBeltsCount()
	{
		return getBonusType(BonusType.BELT_MAX_AMOUNT, Config.RATE_BELTS_COUNT_MAX) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.BELT_MAX_AMOUNT, 1.) : 1);
	}
	
	public double getMinBraceletsCount()
	{
		return getBonusType(BonusType.BRACLET_MIN_AMOUNT, Config.RATE_BRACELETS_COUNT_MIN) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.BRACLET_MIN_AMOUNT, 1.) : 1);
	}
	
	public double getMaxBraceletsCount()
	{
		return getBonusType(BonusType.BRACLET_MAX_AMOUNT, Config.RATE_BRACELETS_COUNT_MAX) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.BRACLET_MAX_AMOUNT, 1.) : 1);
	}
	
	public double getMinCloaksCount()
	{
		return getBonusType(BonusType.CLOAK_MIN_AMOUNT, Config.RATE_CLOAKS_COUNT_MIN) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.CLOAK_MIN_AMOUNT, 1.) : 1);
	}
	
	public double getMaxCloaksCount()
	{
		return getBonusType(BonusType.CLOAK_MAX_AMOUNT, Config.RATE_CLOAKS_COUNT_MAX) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.CLOAK_MAX_AMOUNT, 1.) : 1);
	}
	
	public double getMinCodexCount()
	{
		return getBonusType(BonusType.CODEX_BOOK_MIN_AMOUNT, Config.RATE_CODEX_BOOKS_COUNT_MIN) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.CODEX_BOOK_MIN_AMOUNT, 1.) : 1);
	}
	
	public double getMaxCodexCount()
	{
		return getBonusType(BonusType.CODEX_BOOK_MAX_AMOUNT, Config.RATE_CODEX_BOOKS_COUNT_MAX) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.CODEX_BOOK_MAX_AMOUNT, 1.) : 1);
	}
	
	public double getMinAttStonesCount()
	{
		return getBonusType(BonusType.ATT_STONE_MIN_AMOUNT, Config.RATE_ATTRIBUTE_STONES_COUNT_MIN) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.ATT_STONE_MIN_AMOUNT, 1.) : 1);
	}
	
	public double getMaxAttStonesCount()
	{
		return getBonusType(BonusType.ATT_STONE_MAX_AMOUNT, Config.RATE_ATTRIBUTE_STONES_COUNT_MAX) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.ATT_STONE_MAX_AMOUNT, 1.) : 1);
	}
	
	public double getMinAttCrystalsCount()
	{
		return getBonusType(BonusType.ATT_CRYSTAL_MIN_AMOUNT, Config.RATE_ATTRIBUTE_CRYSTALS_COUNT_MIN) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.ATT_CRYSTAL_MIN_AMOUNT, 1.) : 1);
	}
	
	public double getMaxAttCrystalsCount()
	{
		return getBonusType(BonusType.ATT_CRYSTAL_MAX_AMOUNT, Config.RATE_ATTRIBUTE_CRYSTALS_COUNT_MAX) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.ATT_CRYSTAL_MAX_AMOUNT, 1.) : 1);
	}
	
	public double getMinAttJewelsCount()
	{
		return getBonusType(BonusType.ATT_JEWEL_MIN_AMOUNT, Config.RATE_ATTRIBUTE_JEWELS_COUNT_MIN) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.ATT_JEWEL_MIN_AMOUNT, 1.) : 1);
	}
	
	public double getMaxAttJewelsCount()
	{
		return getBonusType(BonusType.ATT_JEWEL_MAX_AMOUNT, Config.RATE_ATTRIBUTE_JEWELS_COUNT_MAX) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.ATT_JEWEL_MAX_AMOUNT, 1.) : 1);
	}
	
	public double getMinAttEnergyCount()
	{
		return getBonusType(BonusType.ATT_ENERGY_MIN_AMOUNT, Config.RATE_ATTRIBUTE_ENERGY_COUNT_MIN) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.ATT_ENERGY_MIN_AMOUNT, 1.) : 1);
	}
	
	public double getMaxAttEnergyCount()
	{
		return getBonusType(BonusType.ATT_ENERGY_MAX_AMOUNT, Config.RATE_ATTRIBUTE_ENERGY_COUNT_MAX) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.ATT_ENERGY_MAX_AMOUNT, 1.) : 1);
	}
	
	public double getMinWeaponsCount()
	{
		return getBonusType(BonusType.WEAPON_MIN_AMOUNT, Config.RATE_WEAPONS_COUNT_MIN) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.WEAPON_MIN_AMOUNT, 1.) : 1);
	}
	
	public double getMaxWeaponsCount()
	{
		return getBonusType(BonusType.WEAPON_MAX_AMOUNT, Config.RATE_WEAPONS_COUNT_MAX) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.WEAPON_MAX_AMOUNT, 1.) : 1);
	}
	
	public double getMinArmorsCount()
	{
		return getBonusType(BonusType.ARMOR_MIN_AMOUNT, Config.RATE_ARMOR_COUNT_MIN) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.ARMOR_MIN_AMOUNT, 1.) : 1);
	}
	
	public double getMaxArmorsCount()
	{
		return getBonusType(BonusType.ARMOR_MAX_AMOUNT, Config.RATE_ARMOR_COUNT_MAX) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.ARMOR_MAX_AMOUNT, 1.) : 1);
	}
	
	public double getMinAccessoryesCount()
	{
		return getBonusType(BonusType.ACCESSORY_MIN_AMOUNT, Config.RATE_ACCESSORY_COUNT_MIN) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.ACCESSORY_MIN_AMOUNT, 1.) : 1);
	}
	
	public double getMaxAccessoryesCount()
	{
		return getBonusType(BonusType.ACCESSORY_MAX_AMOUNT, Config.RATE_ACCESSORY_COUNT_MAX) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.ACCESSORY_MAX_AMOUNT, 1.) : 1);
	}
	
	public int getMaxSpoilItemsFromOneGroup()
	{
		return (int) (getBonusType(BonusType.MAX_SPOIL_PER_ONE_GROUP, Config.MAX_SPOIL_ITEMS_FROM_ONE_GROUP) + (_player.hasClan() ? _player.getClan().getBonusType(BonusType.MAX_SPOIL_PER_ONE_GROUP, 0.) : 0));
	}
	
	public int getMaxDropItemsFromOneGroup()
	{
		return (int) (getBonusType(BonusType.MAX_DROP_PER_ONE_GROUP, Config.MAX_DROP_ITEMS_FROM_ONE_GROUP) + (_player.hasClan() ? _player.getClan().getBonusType(BonusType.MAX_DROP_PER_ONE_GROUP, 0.) : 0));
	}
	
	public int getMaxRaidDropItemsFromOneGroup()
	{
		return (int) (getBonusType(BonusType.MAX_RAID_DROP_PER_ONE_GROUP, Config.MAX_DROP_ITEMS_FROM_ONE_GROUP_RAIDS) + (_player.hasClan() ? _player.getClan().getBonusType(BonusType.MAX_RAID_DROP_PER_ONE_GROUP, 0.) : 0));
	}
	
	public double getGroupRate()
	{
		return getBonusType(BonusType.GROUP_RATE, 1) * (_player.hasClan() ? _player.getClan().getBonusType(BonusType.GROUP_RATE, 1.) : 1);
	}
	
	public void setIsPersonal(boolean personal)
	{
		_isPersonal = personal;
	}
	
	public boolean isPersonal()
	{
		return _isPersonal;
	}
	
	public void setVipTemplate(final int level)
	{
		_vipTemplate = VipManager.getInstance().getVipLevel(level);
	}
	
	public VipTemplate getVipTemplate()
	{
		return _vipTemplate;
	}
	
	public void addBonusType(BonusType type, double value)
	{
		if (_bonusList.containsKey(type))
		{
			final var val = _bonusList.get(type);
			switch (type)
			{
				case CRAFT_CHANCE :
				case MASTER_WORK_CHANCE :
				case ENCHANT_CHANCE :
				case MAX_DROP_PER_ONE_GROUP :
				case MAX_SPOIL_PER_ONE_GROUP :
				case MAX_RAID_DROP_PER_ONE_GROUP :
					_bonusList.put(type, (val + value));
					break;
				default :
					_bonusList.put(type, (val + (value - 1)));
					break;
			}
			return;
		}
		_bonusList.put(type, value);
	}
	
	public void removeBonusType(BonusType type, double value)
	{
		if (_bonusList.containsKey(type))
		{
			final var val = _bonusList.get(type);
			if (val != value)
			{
				switch (type)
				{
					case CRAFT_CHANCE :
					case MASTER_WORK_CHANCE :
					case ENCHANT_CHANCE :
					case MAX_DROP_PER_ONE_GROUP :
					case MAX_SPOIL_PER_ONE_GROUP :
					case MAX_RAID_DROP_PER_ONE_GROUP :
						_bonusList.put(type, (val - value));
						break;
					default :
						_bonusList.put(type, (val - (value - 1)));
						break;
				}
				return;
			}
			_bonusList.remove(type);
		}
	}
	
	public double getBonusType(BonusType type, double defaultValue)
	{
		if (!_bonusList.containsKey(type))
		{
			return defaultValue;
		}
		return Math.max(0, _bonusList.get(type));
	}
	
	public Map<BonusType, Double> getBonusList()
	{
		return _bonusList;
	}
}