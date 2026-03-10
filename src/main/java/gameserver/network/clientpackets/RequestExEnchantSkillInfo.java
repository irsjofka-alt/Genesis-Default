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
package gameserver.network.clientpackets;

import gameserver.data.parser.EnchantSkillGroupsParser;
import gameserver.data.parser.SkillsParser;
import gameserver.model.actor.Player;
import gameserver.model.skills.Skill;
import gameserver.network.serverpackets.ExEnchantSkillInfo;

public final class RequestExEnchantSkillInfo extends GameClientPacket
{
	private int _skillId;
	private int _skillLvl;

	@Override
	protected void readImpl()
	{
		_skillId = readD();
		_skillLvl = readD();
	}

	@Override
	protected void runImpl()
	{
		if ((_skillId <= 0) || (_skillLvl <= 0))
		{
			return;
		}

		final Player activeChar = getClient().getActiveChar();

		if (activeChar == null)
		{
			return;
		}

		if (activeChar.getLevel() < 76)
		{
			return;
		}

		final Skill skill = SkillsParser.getInstance().getInfo(_skillId, _skillLvl);
		if ((skill == null) || (skill.getId() != _skillId))
		{
			return;
		}

		if (EnchantSkillGroupsParser.getInstance().getSkillEnchantmentBySkillId(_skillId) == null)
		{
			return;
		}

		final int playerSkillLvl = activeChar.getSkillLevel(_skillId);
		if ((playerSkillLvl == -1) || (playerSkillLvl != _skillLvl))
		{
			return;
		}

		activeChar.sendPacket(new ExEnchantSkillInfo(_skillId, _skillLvl));
	}
}