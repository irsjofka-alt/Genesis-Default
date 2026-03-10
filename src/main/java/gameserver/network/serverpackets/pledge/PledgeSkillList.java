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
package gameserver.network.serverpackets.pledge;

import java.util.List;

import gameserver.model.Clan;
import gameserver.model.skills.Skill;
import gameserver.network.serverpackets.GameServerPacket;

public class PledgeSkillList extends GameServerPacket
{
	private final List<Skill> _skills;
	private final SubPledgeSkill[] _subSkills;

	public static class SubPledgeSkill
	{
		int _subType;
		int _skillId;
		int _skillLvl;
		
		public SubPledgeSkill(int subType, int skillId, int skillLvl)
		{
			super();
			_subType = subType;
			_skillId = skillId;
			_skillLvl = skillLvl;
		}
	}

	public PledgeSkillList(Clan clan)
	{
		_skills = clan.getAllSkills();
		_subSkills = clan.getAllSubSkills();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_skills.size());
		writeD(_subSkills.length);
		for (final var sk : _skills)
		{
			writeD(sk.getDisplayId());
			writeD(sk.getDisplayLevel());
		}
		for (final var sk : _subSkills)
		{
			writeD(sk._subType);
			writeD(sk._skillId);
			writeD(sk._skillLvl);
		}
	}
}