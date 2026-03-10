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

import java.util.ArrayList;
import java.util.List;

import gameserver.instancemanager.CursedWeaponsManager;
import gameserver.model.CursedWeapon;
import gameserver.model.Location;
import gameserver.model.actor.Creature;
import gameserver.network.serverpackets.ExCursedWeaponLocation;
import gameserver.network.serverpackets.ExCursedWeaponLocation.CursedWeaponInfo;

public final class RequestCursedWeaponLocation extends GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		final Creature activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		final List<CursedWeaponInfo> list = new ArrayList<>();
		for (final CursedWeapon cw : CursedWeaponsManager.getInstance().getCursedWeapons())
		{
			if (!cw.isActive())
			{
				continue;
			}
			
			final Location pos = cw.getWorldPosition();
			if (pos != null)
			{
				list.add(new CursedWeaponInfo(pos, cw.getItemId(), cw.isActivated() ? 1 : 0));
			}
		}
		
		if (!list.isEmpty())
		{
			activeChar.sendPacket(new ExCursedWeaponLocation(list));
		}
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}