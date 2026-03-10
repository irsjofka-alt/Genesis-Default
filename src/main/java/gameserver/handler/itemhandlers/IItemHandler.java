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
package gameserver.handler.itemhandlers;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import gameserver.model.actor.Playable;
import gameserver.model.items.instance.ItemInstance;

public interface IItemHandler
{
	public static final Logger _log = LogManager.getLogger(IItemHandler.class);

	public boolean useItem(Playable playable, ItemInstance item, boolean forceUse);
}