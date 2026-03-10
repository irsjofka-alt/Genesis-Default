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
package gameserver.listener.player;

import java.util.Set;

import gameserver.listener.actor.CharListenerList;
import gameserver.model.TradeItem;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Player;
import gameserver.model.actor.Summon;
import gameserver.model.entity.Siege;
import gameserver.model.holders.ItemHolder;
import gameserver.model.items.ItemRequest;
import gameserver.model.items.instance.ItemInstance;
import gameserver.model.olympiad.CompetitionType;

public class PlayerListenerList extends CharListenerList
{
	public PlayerListenerList(Player actor)
	{
		super(actor);
	}
	
	@Override
	public Player getActor()
	{
		return (Player) _actor;
	}
	
	public void onInaction(long time)
	{
		_global.getListeners().stream().filter(l -> l != null && OnInactionListener.class.isInstance(l)).forEach(l -> ((OnInactionListener) l).onInaction(getActor(), time));
		getListeners().stream().filter(l -> l != null && OnInactionListener.class.isInstance(l)).forEach(l -> ((OnInactionListener) l).onInaction(getActor(), time));
	}
	
	public void onDisconnect(boolean isDisconnect)
	{
		_global.getListeners().stream().filter(l -> l != null && OnDisconnectListener.class.isInstance(l)).forEach(l -> ((OnDisconnectListener) l).onDisconnect(getActor(), isDisconnect));
		getListeners().stream().filter(l -> l != null && OnDisconnectListener.class.isInstance(l)).forEach(l -> ((OnDisconnectListener) l).onDisconnect(getActor(), isDisconnect));
	}
	
	public void onAutoFarmStart()
	{
		_global.getListeners().stream().filter(l -> l != null && OnAutoFarmStartListener.class.isInstance(l)).forEach(l -> ((OnAutoFarmStartListener) l).onAutoFarmStart(getActor()));
		getListeners().stream().filter(l -> l != null && OnAutoFarmStartListener.class.isInstance(l)).forEach(l -> ((OnAutoFarmStartListener) l).onAutoFarmStart(getActor()));
	}
	
	public void onAutoFarmStop(Creature killer)
	{
		_global.getListeners().stream().filter(l -> l != null && OnAutoFarmStopListener.class.isInstance(l)).forEach(l -> ((OnAutoFarmStopListener) l).onAutoFarmStop(getActor(), killer));
		getListeners().stream().filter(l -> l != null && OnAutoFarmStopListener.class.isInstance(l)).forEach(l -> ((OnAutoFarmStopListener) l).onAutoFarmStop(getActor(), killer));
	}
	
	public void onDamageHit(Player attacker)
	{
		_global.getListeners().stream().filter(l -> l != null && OnDamageListener.class.isInstance(l)).forEach(l -> ((OnDamageListener) l).onDamageHit(getActor(), attacker));
		getListeners().stream().filter(l -> l != null && OnDamageListener.class.isInstance(l)).forEach(l -> ((OnDamageListener) l).onDamageHit(getActor(), attacker));
	}
	
	public void onEnter()
	{
		_global.getListeners().stream().filter(l -> l != null && OnPlayerEnterListener.class.isInstance(l)).forEach(l -> ((OnPlayerEnterListener) l).onPlayerEnter(getActor()));
		getListeners().stream().filter(l -> l != null && OnPlayerEnterListener.class.isInstance(l)).forEach(l -> ((OnPlayerEnterListener) l).onPlayerEnter(getActor()));
	}
	
	public void onExit()
	{
		_global.getListeners().stream().filter(l -> l != null && OnPlayerExitListener.class.isInstance(l)).forEach(l -> ((OnPlayerExitListener) l).onPlayerExit(getActor()));
		getListeners().stream().filter(l -> l != null && OnPlayerExitListener.class.isInstance(l)).forEach(l -> ((OnPlayerExitListener) l).onPlayerExit(getActor()));
	}
	
	public void onTeleport(int x, int y, int z)
	{
		_global.getListeners().stream().filter(l -> l != null && OnTeleportListener.class.isInstance(l)).forEach(l -> ((OnTeleportListener) l).onTeleport(getActor(), x, y, z));
		getListeners().stream().filter(l -> l != null && OnTeleportListener.class.isInstance(l)).forEach(l -> ((OnTeleportListener) l).onTeleport(getActor(), x, y, z));
	}
	
	public void onPartyInvite()
	{
		_global.getListeners().stream().filter(l -> l != null && OnPlayerPartyInviteListener.class.isInstance(l)).forEach(l -> ((OnPlayerPartyInviteListener) l).onPartyInvite(getActor()));
		getListeners().stream().filter(l -> l != null && OnPlayerPartyInviteListener.class.isInstance(l)).forEach(l -> ((OnPlayerPartyInviteListener) l).onPartyInvite(getActor()));
	}
	
	public void onLevelChange(int oldLvl, int newLvl)
	{
		_global.getListeners().stream().filter(l -> l != null && OnLevelChangeListener.class.isInstance(l)).forEach(l -> ((OnLevelChangeListener) l).onLevelChange(getActor(), oldLvl, newLvl));
		getListeners().stream().filter(l -> l != null && OnLevelChangeListener.class.isInstance(l)).forEach(l -> ((OnLevelChangeListener) l).onLevelChange(getActor(), oldLvl, newLvl));
	}
	
	public void onPartyLeave()
	{
		_global.getListeners().stream().filter(l -> l != null && OnPlayerPartyLeaveListener.class.isInstance(l)).forEach(l -> ((OnPlayerPartyLeaveListener) l).onPartyLeave(getActor()));
		getListeners().stream().filter(l -> l != null && OnPlayerPartyLeaveListener.class.isInstance(l)).forEach(l -> ((OnPlayerPartyLeaveListener) l).onPartyLeave(getActor()));
	}
	
	public void onSummonServitor(Summon summon)
	{
		_global.getListeners().stream().filter(l -> l != null && OnPlayerSummonServitorListener.class.isInstance(l)).forEach(l -> ((OnPlayerSummonServitorListener) l).onSummonServitor(getActor(), summon));
		getListeners().stream().filter(l -> l != null && OnPlayerSummonServitorListener.class.isInstance(l)).forEach(l -> ((OnPlayerSummonServitorListener) l).onSummonServitor(getActor(), summon));
	}
	
	public void onChatMessageReceive(int type, String charName, String targetName, String text)
	{
		_global.getListeners().stream().filter(l -> l != null && OnPlayerChatMessageReceive.class.isInstance(l)).forEach(l -> ((OnPlayerChatMessageReceive) l).onChatMessageReceive(getActor(), type, charName, targetName, text));
		getListeners().stream().filter(l -> l != null && OnPlayerChatMessageReceive.class.isInstance(l)).forEach(l -> ((OnPlayerChatMessageReceive) l).onChatMessageReceive(getActor(), type, charName, targetName, text));
	}
	
	public void onExperienceReceived(long exp)
	{
		_global.getListeners().stream().filter(l -> l != null && OnExperienceReceivedListener.class.isInstance(l)).forEach(l -> ((OnExperienceReceivedListener) l).onExperienceReceived(getActor(), exp));
		getListeners().stream().filter(l -> l != null && OnExperienceReceivedListener.class.isInstance(l)).forEach(l -> ((OnExperienceReceivedListener) l).onExperienceReceived(getActor(), exp));
	}

	public void onQuestionMarkClicked(int questionMarkId)
	{
		_global.getListeners().stream().filter(l -> l != null && OnQuestionMarkListener.class.isInstance(l)).forEach(l -> ((OnQuestionMarkListener) l).onQuestionMarkClicked(getActor(), questionMarkId));
		getListeners().stream().filter(l -> l != null && OnQuestionMarkListener.class.isInstance(l)).forEach(l -> ((OnQuestionMarkListener) l).onQuestionMarkClicked(getActor(), questionMarkId));
	}
	
	public void onFishing(ItemHolder item, boolean success)
	{
		_global.getListeners().stream().filter(l -> l != null && OnFishingListener.class.isInstance(l)).forEach(l -> ((OnFishingListener) l).onFishing(getActor(), item, success));
		getListeners().stream().filter(l -> l != null && OnFishingListener.class.isInstance(l)).forEach(l -> ((OnFishingListener) l).onFishing(getActor(), item, success));
	}
	
	public void onOlympiadFinishBattle(CompetitionType type, boolean winner)
	{
		_global.getListeners().stream().filter(l -> l != null && OnOlympiadFinishBattleListener.class.isInstance(l)).forEach(l -> ((OnOlympiadFinishBattleListener) l).onOlympiadFinishBattle(getActor(), type, winner));
		getListeners().stream().filter(l -> l != null && OnOlympiadFinishBattleListener.class.isInstance(l)).forEach(l -> ((OnOlympiadFinishBattleListener) l).onOlympiadFinishBattle(getActor(), type, winner));
	}
	
	public void onQuestFinish(int questId)
	{
		_global.getListeners().stream().filter(l -> l != null && OnQuestFinishListener.class.isInstance(l)).forEach(l -> ((OnQuestFinishListener) l).onQuestFinish(getActor(), questId));
		getListeners().stream().filter(l -> l != null && OnQuestFinishListener.class.isInstance(l)).forEach(l -> ((OnQuestFinishListener) l).onQuestFinish(getActor(), questId));
	}
	
	public void onParticipateInCastleSiege(Siege siege)
	{
		_global.getListeners().stream().filter(l -> l != null && OnParticipateInCastleSiegeListener.class.isInstance(l)).forEach(l -> ((OnParticipateInCastleSiegeListener) l).onParticipateInCastleSiege(getActor(), siege));
		getListeners().stream().filter(l -> l != null && OnParticipateInCastleSiegeListener.class.isInstance(l)).forEach(l -> ((OnParticipateInCastleSiegeListener) l).onParticipateInCastleSiege(getActor(), siege));
	}
	
	public long onItemDropListener(int itemId, long amount)
	{
		if (!_global.getListeners().isEmpty())
		{
			for (final var listener : _global.getListeners())
			{
				if (OnItemDropListener.class.isInstance(listener))
				{
					return ((OnItemDropListener) listener).onItemDropListener(getActor(), itemId, amount);
				}
			}
		}
		
		if (!getListeners().isEmpty())
		{
			for (final var listener : getListeners())
			{
				if (OnItemDropListener.class.isInstance(listener))
				{
					return ((OnItemDropListener) listener).onItemDropListener(getActor(), itemId, amount);
				}
			}
		}
		return amount;
	}
	
	public void onItemEquipListener(ItemInstance item)
	{
		_global.getListeners().stream().filter(l -> l != null && OnItemEquipListener.class.isInstance(l)).forEach(l -> ((OnItemEquipListener) l).onItemEquipListener(getActor(), item));
		getListeners().stream().filter(l -> l != null && OnItemEquipListener.class.isInstance(l)).forEach(l -> ((OnItemEquipListener) l).onItemEquipListener(getActor(), item));
	}
	
	public void onItemUnEquipListener(ItemInstance item)
	{
		_global.getListeners().stream().filter(l -> l != null && OnItemUnEquipListener.class.isInstance(l)).forEach(l -> ((OnItemUnEquipListener) l).onItemUnEquipListener(getActor(), item));
		getListeners().stream().filter(l -> l != null && OnItemUnEquipListener.class.isInstance(l)).forEach(l -> ((OnItemUnEquipListener) l).onItemUnEquipListener(getActor(), item));
	}
	
	public void onUseItem(final ItemInstance item)
	{
		_global.getListeners().stream().filter(l -> l != null && OnUseItemListener.class.isInstance(l)).forEach(l -> ((OnUseItemListener) l).onUseItem(getActor(), item));
		getListeners().stream().filter(l -> l != null && OnUseItemListener.class.isInstance(l)).forEach(l -> ((OnUseItemListener) l).onUseItem(getActor(), item));
	}
	
	public void onEnchantItem(final ItemInstance item, int oldEnchant, int newEnchant, final boolean success, boolean destroy)
	{
		_global.getListeners().stream().filter(l -> l != null && OnEnchantItemListener.class.isInstance(l)).forEach(l -> ((OnEnchantItemListener) l).onEnchantItem(getActor(), item, oldEnchant, newEnchant, success, destroy));
		getListeners().stream().filter(l -> l != null && OnEnchantItemListener.class.isInstance(l)).forEach(l -> ((OnEnchantItemListener) l).onEnchantItem(getActor(), item, oldEnchant, newEnchant, success, destroy));
	}
	
	public void onRaidPoints(long points)
	{
		_global.getListeners().stream().filter(l -> l != null && OnRaidPointListener.class.isInstance(l)).forEach(l -> ((OnRaidPointListener) l).onRaidPoints(getActor(), points));
		getListeners().stream().filter(l -> l != null && OnRaidPointListener.class.isInstance(l)).forEach(l -> ((OnRaidPointListener) l).onRaidPoints(getActor(), points));
	}
	
	public void onPvp(Player target)
	{
		_global.getListeners().stream().filter(l -> l != null && OnPvpListener.class.isInstance(l)).forEach(l -> ((OnPvpListener) l).onPvp(getActor(), target));
		getListeners().stream().filter(l -> l != null && OnPvpListener.class.isInstance(l)).forEach(l -> ((OnPvpListener) l).onPvp(getActor(), target));
	}
	
	public void onEventFinish(int eventId, boolean isWin)
	{
		_global.getListeners().stream().filter(l -> l != null && OnEventFinishListener.class.isInstance(l)).forEach(l -> ((OnEventFinishListener) l).onEventFinish(getActor(), eventId, isWin));
		getListeners().stream().filter(l -> l != null && OnEventFinishListener.class.isInstance(l)).forEach(l -> ((OnEventFinishListener) l).onEventFinish(getActor(), eventId, isWin));
	}
	
	public void onReflectionFinish(int refId)
	{
		_global.getListeners().stream().filter(l -> l != null && OnReflectionFinishListener.class.isInstance(l)).forEach(l -> ((OnReflectionFinishListener) l).onReflectionFinish(getActor(), refId));
		getListeners().stream().filter(l -> l != null && OnReflectionFinishListener.class.isInstance(l)).forEach(l -> ((OnReflectionFinishListener) l).onReflectionFinish(getActor(), refId));
	}
	
	public void onPrivateStoreSell(Player owner, Player player, ItemRequest[] items, long totalPrice)
	{
		_global.getListeners().stream().filter(l -> l != null && OnPrivateStoreSell.class.isInstance(l)).forEach(l -> ((OnPrivateStoreSell) l).onPrivateStoreSell(owner, player, items, totalPrice));
		getListeners().stream().filter(l -> l != null && OnPrivateStoreSell.class.isInstance(l)).forEach(l -> ((OnPrivateStoreSell) l).onPrivateStoreSell(owner, player, items, totalPrice));
	}
	
	public void onPrivateStoreBuy(Player owner, Player player, Set<ItemRequest> items, long totalPrice)
	{
		_global.getListeners().stream().filter(l -> l != null && OnPrivateStoreBuy.class.isInstance(l)).forEach(l -> ((OnPrivateStoreBuy) l).onPrivateStoreBuy(owner, player, items, totalPrice));
		getListeners().stream().filter(l -> l != null && OnPrivateStoreBuy.class.isInstance(l)).forEach(l -> ((OnPrivateStoreBuy) l).onPrivateStoreBuy(owner, player, items, totalPrice));
	}
	
	public void onTradeItems(Player owner, Player other, TradeItem[] ownerItems, TradeItem[] otherItems)
	{
		_global.getListeners().stream().filter(l -> l != null && OnTradeItems.class.isInstance(l)).forEach(l -> ((OnTradeItems) l).onTradeItems(owner, other, ownerItems, otherItems));
		getListeners().stream().filter(l -> l != null && OnTradeItems.class.isInstance(l)).forEach(l -> ((OnTradeItems) l).onTradeItems(owner, other, ownerItems, otherItems));
	}
}