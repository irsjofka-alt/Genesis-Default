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
package gameserver.model.base;

public enum SortType
{
	NAME_ASC(1), NAME_DESC(-1), LEVEL_ASC(2), LEVEL_DESC(-2), STATUS_ASC(3), STATUS_DESC(-3), STATUS_ALIVE(4), STATUS_DEATH(-4);

	public final int _index;

	SortType(int index)
	{
		_index = index;
	}
}