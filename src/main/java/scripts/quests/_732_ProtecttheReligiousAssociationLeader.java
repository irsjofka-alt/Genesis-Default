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
public class _732_ProtecttheReligiousAssociationLeader extends TerritoryWarSuperClass
{
	public _732_ProtecttheReligiousAssociationLeader()
	{
		super(732);
		
		NPC_IDS = new int[]
		{
		        36510, 36516, 36522, 36528, 36534, 36540, 36546, 36552, 36558
		};
		registerAttackIds();
	}
	
	@Override
	public int getTerritoryIdForThisNPCId(int npcid)
	{
		return 81 + (npcid - 36510) / 6;
	}
}
