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

import gameserver.model.actor.Player;
import gameserver.model.actor.templates.ShortCutTemplate;
import gameserver.model.base.ShortcutType;
import gameserver.network.serverpackets.ShortCutRegister;

public final class RequestShortCutReg extends GameClientPacket
{
	private ShortcutType _type;
	private int _id;
	private int _slot;
	private int _page;
	private int _lvl;
	private int _characterType;

	@Override
	protected void readImpl()
	{
		final int typeId = readD();
		_type = ShortcutType.values()[(typeId < 1) || (typeId > 6) ? 0 : typeId];
		final int slot = readD();
		_id = readD();
		_lvl = readD();
		_characterType = readD();
		
		_slot = slot % 12;
		_page = slot / 12;
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null || (_page > 12) || (_page < 0))
		{
			return;
		}
		
		final ShortCutTemplate sc = new ShortCutTemplate(_slot, _page, _type, _id, _lvl, _characterType);
		activeChar.registerShortCut(sc);
		sendPacket(new ShortCutRegister(sc));
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}