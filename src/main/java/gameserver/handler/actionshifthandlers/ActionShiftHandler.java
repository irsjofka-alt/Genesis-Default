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
package gameserver.handler.actionshifthandlers;

import java.util.HashMap;
import java.util.Map;

import l2e.commons.log.LoggerObject;
import gameserver.handler.actionhandlers.IActionHandler;
import gameserver.handler.actionshifthandlers.impl.DoorActionShift;
import gameserver.handler.actionshifthandlers.impl.ItemActionShift;
import gameserver.handler.actionshifthandlers.impl.NpcActionShift;
import gameserver.handler.actionshifthandlers.impl.PlayerActionShift;
import gameserver.handler.actionshifthandlers.impl.StaticObjectActionShift;
import gameserver.handler.actionshifthandlers.impl.SummonActionShift;
import gameserver.model.GameObject.InstanceType;

public class ActionShiftHandler extends LoggerObject
{
	private final Map<InstanceType, IActionHandler> _handlers;

	public static ActionShiftHandler getInstance()
	{
		return SingletonHolder._instance;
	}

	protected ActionShiftHandler()
	{
		_handlers = new HashMap<>();
		
		registerHandler(new DoorActionShift());
		registerHandler(new ItemActionShift());
		registerHandler(new NpcActionShift());
		registerHandler(new PlayerActionShift());
		registerHandler(new StaticObjectActionShift());
		registerHandler(new SummonActionShift());
		
		info("Loaded " + _handlers.size() + " ActionShiftHandlers");
	}

	public void registerHandler(IActionHandler handler)
	{
		_handlers.put(handler.getInstanceType(), handler);
	}
	
	public synchronized void removeHandler(IActionHandler handler)
	{
		_handlers.remove(handler.getInstanceType());
	}
	
	public IActionHandler getHandler(InstanceType iType)
	{
		IActionHandler result = null;
		for (InstanceType t = iType; t != null; t = t.getParent())
		{
			result = _handlers.get(t);
			if (result != null)
			{
				break;
			}
		}
		return result;
	}

	public int size()
	{
		return _handlers.size();
	}

	private static class SingletonHolder
	{
		protected static final ActionShiftHandler _instance = new ActionShiftHandler();
	}
}