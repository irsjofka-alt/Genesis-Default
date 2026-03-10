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
package gameserver.model.actor.templates.community;

public class CharPointsInfo
{
	private final int _charId;
	private final int _points;
		
	public CharPointsInfo(int charId, int points)
	{
		_charId = charId;
		_points = points;
	}
		
	public int getCharId()
	{
		return _charId;
	}
		
	public int getPoints()
	{
		return _points;
	}
}