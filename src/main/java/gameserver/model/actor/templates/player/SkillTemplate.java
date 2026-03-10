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
package gameserver.model.actor.templates.player;

public class SkillTemplate
{
	public int _id;
	public int _getLvl;
	public int _nextLevel;
	public int _maxLevel;
	public int _spCost;
	public int _requirements;

	public SkillTemplate(int pId, int gtLvl, int pNextLevel, int pMaxLevel, int pSpCost, int pRequirements)
	{
		_id = pId;
		_getLvl = gtLvl;
		_nextLevel = pNextLevel;
		_maxLevel = pMaxLevel;
		_spCost = pSpCost;
		_requirements = pRequirements;
	}
}