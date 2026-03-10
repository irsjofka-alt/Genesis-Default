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

import gameserver.data.parser.RecipeParser;
import gameserver.model.RecipeList;
import gameserver.model.actor.Player;

public class RecipeItemMakeInfo extends GameServerPacket
{
	private final int _id;
	private final Player _activeChar;
	private final boolean _success;
	
	public RecipeItemMakeInfo(int id, Player player, boolean success)
	{
		_id = id;
		_activeChar = player;
		_success = success;
	}
	
	public RecipeItemMakeInfo(int id, Player player)
	{
		_id = id;
		_activeChar = player;
		_success = true;
	}

	@Override
	protected final void writeImpl()
	{
		final RecipeList recipe = RecipeParser.getInstance().getRecipeList(_id);
		if (recipe != null)
		{
			writeD(_id);
			writeD(recipe.isDwarvenRecipe() ? 0x00 : 0x01);
			writeD((int) _activeChar.getCurrentMp());
			writeD((int) _activeChar.getMaxMp());
			writeD(_success ? 0x01 : 0x00);
		}
		else
		{
			_log.info("Character: " + getClient().getActiveChar() + ": Requested unexisting recipe with id = " + _id);
		}
	}
}