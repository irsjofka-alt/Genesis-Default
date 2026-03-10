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
package gameserver.handler.actionshifthandlers.impl;

import gameserver.handler.actionhandlers.IActionHandler;
import gameserver.model.GameObject;
import gameserver.model.GameObject.InstanceType;
import gameserver.model.actor.Player;
import gameserver.model.actor.instance.StaticObjectInstance;
import gameserver.network.serverpackets.NpcHtmlMessage;
import gameserver.network.serverpackets.StaticObject;
import gameserver.utils.StringUtil;

public class StaticObjectActionShift implements IActionHandler
{
	@Override
	public boolean action(Player activeChar, GameObject target, boolean interact, boolean shift)
	{
		if (activeChar.getAccessLevel().allowStaticObjectActionShift())
		{
			activeChar.setTarget(target);
			activeChar.sendPacket(new StaticObject((StaticObjectInstance) target));

			final NpcHtmlMessage html = new NpcHtmlMessage(target.getObjectId());
			html.setHtml(activeChar, StringUtil.concat("<html><body><center><font color=\"LEVEL\">Static Object Info</font></center><br><table border=0><tr><td>Coords X,Y,Z: </td><td>", String.valueOf(target.getX()), ", ", String.valueOf(target.getY()), ", ", String.valueOf(target.getZ()), "</td></tr><tr><td>Object ID: </td><td>", String.valueOf(target.getObjectId()), "</td></tr><tr><td>Static Object ID: </td><td>", String.valueOf(((StaticObjectInstance) target).getId()), "</td></tr><tr><td>Mesh Index: </td><td>", String.valueOf(((StaticObjectInstance) target).getMeshIndex()), "</td></tr><tr><td><br></td></tr><tr><td>Class: </td><td>", target.getClass().getSimpleName(), "</td></tr></table></body></html>"));
			activeChar.sendPacket(html);
		}
		return true;
	}
	
	@Override
	public InstanceType getInstanceType()
	{
		return InstanceType.StaticObjectInstance;
	}
}