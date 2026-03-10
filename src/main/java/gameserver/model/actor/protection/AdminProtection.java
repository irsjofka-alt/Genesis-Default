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
package gameserver.model.actor.protection;

import gameserver.Config;
import gameserver.model.actor.Player;
import gameserver.utils.Util;

public class AdminProtection extends ExtensionProtection
{
	private boolean _safeadmin = false;
	private String _adminConfirmCmd = null;
	private boolean _inCameraMode = false;
	
	public AdminProtection(Player activeChar)
	{
		super(activeChar);
	}

	public void setIsSafeAdmin(boolean b)
	{
		_safeadmin = b;
	}
	
	public boolean isSafeAdmin()
	{
		return _safeadmin;
	}
	
	public boolean canUseAdminCommand()
	{
		if (Config.ENABLE_SAFE_ADMIN_PROTECTION && !getPlayer().getAdminProtection().isSafeAdmin())
		{
			_log.warn("Character " + getPlayer().getName(null) + "(" + getPlayer().getObjectId() + ") tryed to use an admin command.");
			punishUnSafeAdmin();
			return false;
		}
		return true;
	}
	
	public void punishUnSafeAdmin()
	{
		if (getPlayer() != null)
		{
			getPlayer().setAccessLevel(0);
			Util.handleIllegalPlayerAction(getPlayer(), "" + getPlayer().getName(null) + " not allowed to be a GM!");
		}
	}
	
	public String getAdminConfirmCmd()
	{
		return _adminConfirmCmd;
	}
	
	public void setAdminConfirmCmd(String adminConfirmCmd)
	{
		_adminConfirmCmd = adminConfirmCmd;
	}
	
	public void setCameraMode(boolean val)
	{
		_inCameraMode = val;
	}
	
	public boolean inCameraMode()
	{
		return _inCameraMode;
	}
}