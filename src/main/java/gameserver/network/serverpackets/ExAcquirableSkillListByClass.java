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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gameserver.model.actor.templates.player.SkillTemplate;
import gameserver.model.base.AcquireSkillType;
import gameserver.network.ServerPacketOpcodes;
import gameserver.utils.comparators.SkillTemplateComparator;

public final class ExAcquirableSkillListByClass extends GameServerPacket
{
	private final List<SkillTemplate> _skills;
	private final AcquireSkillType _skillType;

	public ExAcquirableSkillListByClass(AcquireSkillType type)
	{
		_skillType = type;
		_skills = new ArrayList<>();
	}

	public void addSkill(int id, int gtLvl, int nextLevel, int maxLevel, int spCost, int requirements)
	{
		_skills.add(new SkillTemplate(id, gtLvl, nextLevel, maxLevel, spCost, requirements));
	}

	@Override
	protected void writeImpl()
	{
		if (_skills.isEmpty())
		{
			return;
		}
		Collections.sort(_skills, SkillTemplateComparator.getInstance());
		
		writeD(_skillType.ordinal());
		writeD(_skills.size());
		for (final var temp : _skills)
		{
			writeD(temp._id);
			writeD(temp._nextLevel);
			writeD(temp._maxLevel);
			writeD(temp._spCost);
			writeD(temp._requirements);
			if (_skillType == AcquireSkillType.SUBPLEDGE)
			{
				writeD(2002);
			}
		}
	}
	
	@Override
	protected ServerPacketOpcodes getOpcodes()
	{
		return ServerPacketOpcodes.AcquireSkillList;
	}
}