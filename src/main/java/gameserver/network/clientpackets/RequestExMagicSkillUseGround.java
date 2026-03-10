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

import gameserver.data.parser.SkillsParser;
import gameserver.model.Location;
import gameserver.model.actor.Player;
import gameserver.model.skills.Skill;
import gameserver.utils.Util;

public final class RequestExMagicSkillUseGround extends GameClientPacket
{
	private int _x;
	private int _y;
	private int _z;
	private int _skillId;
	private boolean _ctrlPressed;
	private boolean _shiftPressed;

	@Override
	protected void readImpl()
	{
		_x = readD();
		_y = readD();
		_z = readD();
		_skillId = readD();
		_ctrlPressed = readD() != 0;
		_shiftPressed = readC() != 0;
	}

	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			return;
		}
		
		if (activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}
		
		final int level = activeChar.getSkillLevel(_skillId);
		if (level <= 0)
		{
			activeChar.sendActionFailed();
			return;
		}
		final Skill skill = SkillsParser.getInstance().getInfo(_skillId, level);
		
		if (skill != null)
		{
			activeChar.setCurrentSkillWorldPosition(new Location(_x, _y, _z));
			activeChar.setHeading(Util.calculateHeadingFrom(activeChar.getX(), activeChar.getY(), _x, _y));
			activeChar.useMagic(skill, _ctrlPressed, _shiftPressed, true);
		}
		else
		{
			activeChar.sendActionFailed();
			_log.warn("No skill found with id " + _skillId + " and level " + level + " !!");
		}
	}
}