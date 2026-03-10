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
package gameserver.network.clientpackets;

import gameserver.data.holder.ClanHolder;
import gameserver.model.GameObjectsStorage;
import gameserver.network.serverpackets.ExGMViewQuestItemList;
import gameserver.network.serverpackets.GMHennaInfo;
import gameserver.network.serverpackets.GMViewCharacterInfo;
import gameserver.network.serverpackets.GMViewItemList;
import gameserver.network.serverpackets.GMViewPledgeInfo;
import gameserver.network.serverpackets.GMViewSkillInfo;
import gameserver.network.serverpackets.GMViewWarehouseWithdrawList;
import gameserver.network.serverpackets.GmViewQuestInfo;

public final class RequestGMCommand extends GameClientPacket
{
	private String _targetName;
	private int _command;

	@Override
	protected void readImpl()
	{
		_targetName = readS();
		_command = readD();
	}

	@Override
	protected void runImpl()
	{
		final var activeChar = getClient().getActiveChar();
		if (activeChar == null || !activeChar.getAccessLevel().allowAltG())
		{
			return;
		}

		final var player = GameObjectsStorage.getPlayer(_targetName);
		final var clan = ClanHolder.getInstance().getClanByName(_targetName);
		if ((player == null) && ((clan == null) || (_command != 6)))
		{
			return;
		}

		switch (_command)
		{
			case 1 :
			{
				activeChar.sendPacket(new GMViewCharacterInfo(player));
				activeChar.sendPacket(new GMHennaInfo(player));
				break;
			}
			case 2 :
			{
				if ((player != null) && (player.getClan() != null))
				{
					activeChar.sendPacket(new GMViewPledgeInfo(player.getClan(), player));
				}
				break;
			}
			case 3 :
			{
				activeChar.sendPacket(new GMViewSkillInfo(player));
				break;
			}
			case 4 :
			{
				activeChar.sendPacket(new GmViewQuestInfo(player));
				break;
			}
			case 5 :
			{
				final var items = player.getInventory().getItems();
				int questSize = 0;
				for (final var item : items)
				{
					if (item.isQuestItem())
					{
						questSize++;
					}
				}
				activeChar.sendPacket(new GMViewItemList(player, items, items.length - questSize));
				activeChar.sendPacket(new ExGMViewQuestItemList(player, items, questSize));
				activeChar.sendPacket(new GMHennaInfo(player));
				break;
			}
			case 6 :
			{
				if (player != null)
				{
					activeChar.sendPacket(new GMViewWarehouseWithdrawList(player));
				}
				else
				{
					activeChar.sendPacket(new GMViewWarehouseWithdrawList(clan));
				}
				break;
			}
		}
	}
}