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
package gameserver.model.actor.instance;

import gameserver.data.parser.CategoryParser;
import gameserver.model.CategoryType;
import gameserver.model.actor.templates.npc.NpcTemplate;
import gameserver.model.base.ClassId;
import gameserver.model.base.Race;

public final class VillageMasterPriestInstance extends VillageMasterInstance
{
	public VillageMasterPriestInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	protected boolean checkVillageMasterRace(ClassId pclass)
	{
		return pclass == null ? false : pclass.getRace() == Race.HUMAN || pclass.getRace() == Race.ELF;
	}
	
	@Override
	protected boolean checkVillageMasterTeachType(ClassId pclass)
	{
		return pclass == null ? false : CategoryParser.getInstance().isInCategory(CategoryType.CLERIC_GROUP, pclass.getId());
	}
}