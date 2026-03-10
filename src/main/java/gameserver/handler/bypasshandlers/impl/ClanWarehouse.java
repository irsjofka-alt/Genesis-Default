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
package gameserver.handler.bypasshandlers.impl;

import gameserver.Config;
import gameserver.handler.bypasshandlers.IBypassHandler;
import gameserver.model.Clan;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.actor.instance.ClanHallManagerInstance;
import gameserver.model.actor.instance.WarehouseInstance;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.NpcHtmlMessage;
import gameserver.network.serverpackets.SortedWareHouseWithdrawalList;
import gameserver.network.serverpackets.SortedWareHouseWithdrawalList.WarehouseListType;
import gameserver.network.serverpackets.WareHouseDepositList;
import gameserver.network.serverpackets.WareHouseWithdrawList;

public class ClanWarehouse implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
	        "withdrawc", "withdrawsortedc", "depositc"
	};
	
	@Override
	public boolean useBypass(String command, Player activeChar, Creature target)
	{
		if (!(target instanceof WarehouseInstance) && !(target instanceof ClanHallManagerInstance))
		{
			return false;
		}
		
		if (activeChar.isEnchanting())
		{
			return false;
		}
		
		if (activeChar.getClan() == null)
		{
			activeChar.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_CLAN_WAREHOUSE);
			return false;
		}
		
		if (activeChar.getClan().getLevel() == 0)
		{
			activeChar.sendPacket(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE);
			return false;
		}
		
		try
		{
			if (command.toLowerCase().startsWith(COMMANDS[0])) // WithdrawC
			{
				if (Config.ENABLE_WAREHOUSESORTING_CLAN)
				{
					final NpcHtmlMessage msg = new NpcHtmlMessage(((Npc) target).getObjectId());
					msg.setFile(activeChar, activeChar.getLang(), "data/html/mods/WhSortedC.htm");
					msg.replace("%objectId%", String.valueOf(((Npc) target).getObjectId()));
					activeChar.sendPacket(msg);
				}
				else
				{
					showWithdrawWindow(activeChar, null, (byte) 0);
				}
				return true;
			}
			else if (command.toLowerCase().startsWith(COMMANDS[1]))
			{
				final String param[] = command.split(" ");
				
				if (param.length > 2)
				{
					showWithdrawWindow(activeChar, WarehouseListType.valueOf(param[1]), SortedWareHouseWithdrawalList.getOrder(param[2]));
				}
				else if (param.length > 1)
				{
					showWithdrawWindow(activeChar, WarehouseListType.valueOf(param[1]), SortedWareHouseWithdrawalList.A2Z);
				}
				else
				{
					showWithdrawWindow(activeChar, WarehouseListType.ALL, SortedWareHouseWithdrawalList.A2Z);
				}
				return true;
			}
			else if (command.toLowerCase().startsWith(COMMANDS[2]))
			{
				activeChar.sendActionFailed();
				activeChar.setActiveWarehouse(activeChar.getClan().getWarehouse());
				activeChar.setInventoryBlockingStatus(true);
				
				if (Config.DEBUG)
				{
					_log.info("Source: L2WarehouseInstance.java; Player: " + activeChar.getName(null) + "; Command: showDepositWindowClan; Message: Showing items to deposit.");
				}
				
				activeChar.sendPacket(new WareHouseDepositList(activeChar, WareHouseDepositList.CLAN));
				return true;
			}
			
			return false;
		}
		catch (final Exception e)
		{
			_log.warn("Exception in " + getClass().getSimpleName(), e);
		}
		return false;
	}
	
	private static final void showWithdrawWindow(Player player, WarehouseListType itemtype, byte sortorder)
	{
		player.sendActionFailed();
		
		if ((player.getClanPrivileges() & Clan.CP_CL_VIEW_WAREHOUSE) != Clan.CP_CL_VIEW_WAREHOUSE)
		{
			player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_CLAN_WAREHOUSE);
			return;
		}
		
		player.setActiveWarehouse(player.getClan().getWarehouse());
		
		if (player.getActiveWarehouse().getSize() == 0)
		{
			player.sendPacket(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH);
			return;
		}
		
		if (itemtype != null)
		{
			player.sendPacket(new SortedWareHouseWithdrawalList(player, WareHouseWithdrawList.CLAN, itemtype, sortorder));
		}
		else
		{
			player.sendPacket(new WareHouseWithdrawList(1, player, WareHouseWithdrawList.CLAN));
		}
		
		if (Config.DEBUG)
		{
			_log.info("Source: L2WarehouseInstance.java; Player: " + player.getName(null) + "; Command: showRetrieveWindowClan; Message: Showing stored items.");
		}
	}
	
	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}