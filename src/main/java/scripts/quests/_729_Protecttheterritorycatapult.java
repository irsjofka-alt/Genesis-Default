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
package scripts.quests;

/**
 * Based on L2J Eternity-World
 */
public class _729_Protecttheterritorycatapult extends TerritoryWarSuperClass
{
	public _729_Protecttheterritorycatapult()
	{
		super(729);
		
		NPC_IDS = new int[]
		{
		        36499, 36500, 36501, 36502, 36503, 36504, 36505, 36506, 36507
		};
		registerAttackIds();
	}
	
	@Override
	public int getTerritoryIdForThisNPCId(int npcid)
	{
		return npcid - 36418;
	}
}
