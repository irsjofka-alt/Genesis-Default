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

import gameserver.Config;
import gameserver.data.parser.SkillsParser;
import gameserver.model.actor.Player;
import gameserver.model.skills.effects.EffectType;

public class RequestDispel extends GameClientPacket
{
	private int _objectId;
	private int _skillId;
	private int _skillLevel;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_skillId = readD();
		_skillLevel = readD();
	}

	@Override
	protected void runImpl()
	{
		if ((_skillId <= 0) || (_skillLevel <= 0))
		{
			return;
		}
		final Player activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			return;
		}
		
		final var effects = activeChar.getAllEffects();
		final var skill = SkillsParser.getInstance().getInfo(_skillId, _skillLevel);
		if (skill == null)
		{
			return;
		}
		
		if (skill.isAllowDispel())
		{
			if (activeChar.getObjectId() == _objectId)
			{
				activeChar.stopSkillEffects(_skillId, true);
			}
			else
			{
				if (activeChar.hasSummon() && (activeChar.getSummon().getObjectId() == _objectId))
				{
					activeChar.getSummon().stopSkillEffects(_skillId, true);
				}
			}
			return;
		}
		
		if (!skill.canBeDispeled() || skill.isStayAfterDeath() || skill.isDebuff() || skill.hasEffectType(EffectType.STUN))
		{
			return;
		}
		
		for (final var eff : effects)
		{
			if (eff != null && (eff.getAbnormalType().equalsIgnoreCase("TRANSFORM") || eff.toString().equalsIgnoreCase("Transformation")))
			{
				return;
			}
		}
		
		if (skill.isDance() && !Config.DANCE_CANCEL_BUFF)
		{
			return;
		}
		if (activeChar.getObjectId() == _objectId)
		{
			activeChar.stopSkillEffects(_skillId, true);
		}
		else
		{
			if (activeChar.hasSummon() && (activeChar.getSummon().getObjectId() == _objectId))
			{
				activeChar.getSummon().stopSkillEffects(_skillId, true);
			}
		}
	}
}