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

import org.nio.impl.SendablePacket;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import gameserver.Config;
import gameserver.EternityWorld;
import gameserver.model.actor.Player;
import gameserver.model.items.instance.ItemInstance;
import gameserver.network.GameClient;
import gameserver.network.ServerPacketOpcodes;

public abstract class GameServerPacket extends SendablePacket<GameClient>
{
	protected static final Logger _log = LogManager.getLogger(GameServerPacket.class);
	
	protected boolean _invisible = false;
	
	public boolean isInvisible()
	{
		return _invisible;
	}
	
	protected void writeD(boolean b)
	{
		writeD(b ? 0x01 : 0x00);
	}

	protected void writeItemInfo(ItemInstance item)
	{
		writeItemInfo(item, item.getCount());
	}

	protected void writeItemInfo(ItemInstance item, long count)
	{
		writeD(item.getObjectId());
		writeD(item.getDisplayId());
		writeD(item.getLocationSlot());
		writeQ(count);
		writeH(item.getItem().getType2());
		writeH(item.getCustomType1());
		writeH(item.isEquipped() ? 0x01 : 0x00);
		writeD(item.getItem().getBodyPart());
		writeH(item.getEnchantLevel());
		writeH(item.getCustomType2());
		writeD(item.isAugmented() ? item.getAugmentation().getAugmentationId() : 0x00);
		writeD(item.getMana());
		writeD(item.isTimeLimitedItem() ? (int) (item.getRemainingTime() / 1000) : -9999);
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
	}
	
	@Override
	protected boolean write()
	{
		try
		{
			if (writeOpcodes())
			{
				writeImpl();
				if (Config.SERVER_PACKET_HANDLER_DEBUG)
				{
					_log.info(getClass().getSimpleName());
				}
				return true;
			}
		}
		catch (final RuntimeException e)
		{
			if (Config.SERVER_PACKET_HANDLER_DEBUG)
			{
				_log.warn("Client: " + getClient().toString() + " - Failed writing: " + getClass().getSimpleName() + " - Revision: " + EternityWorld._revision + "", e);
			}
		}
		return false;
	}
	
	protected boolean writeOpcodes()
	{
		final ServerPacketOpcodes opcodes = getOpcodes();
		if (opcodes == null)
		{
			return false;
		}
		
		final int opcode = opcodes.getId();
		if (opcode >= 0)
		{
			writeC(opcode);
			final int exOpcode = opcodes.getExId();
			if (exOpcode >= 0)
			{
				writeH(exOpcode);
			}
			return true;
		}
		return false;
	}
	
	protected ServerPacketOpcodes getOpcodes()
	{
		try
		{
			return ServerPacketOpcodes.valueOf(getClass().getSimpleName());
		}
		catch (final Exception _)
		{
			_log.warn("Cannot find serverpacket opcode: " + getClass().getSimpleName() + "!");
		}
		return null;
	}
	
	public void sendTo(Player player)
	{
		player.sendPacket(this);
	}
	
	protected abstract void writeImpl();
}