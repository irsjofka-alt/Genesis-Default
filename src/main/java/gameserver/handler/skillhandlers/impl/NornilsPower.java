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
package gameserver.handler.skillhandlers.impl;

import gameserver.handler.skillhandlers.ISkillHandler;
import gameserver.model.GameObject;
import gameserver.model.actor.Creature;
import gameserver.model.skills.Skill;
import gameserver.model.skills.SkillType;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.SystemMessage;

public class NornilsPower implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS =
	{
	        SkillType.NORNILS_POWER
	};

	@Override
	public void useSkill(Creature activeChar, Skill skill, GameObject[] targets, double cubicPower)
	{
		if (!activeChar.isPlayer())
		{
			return;
		}
		
		final var ref = activeChar.getReflection();
		if (!ref.isDefault() && ref.getTemplateId() == 11)
		{
			if (activeChar.isInsideRadius(-107393, 83677, 100, true))
			{
				activeChar.destroyItemByItemId("NornilsPower", 9713, 1, activeChar, true);
				final var door = ref.getDoor(16200010);
				if (door != null)
				{
					door.setMeshIndex(1);
					door.setTargetable(true);
					door.broadcastStatusUpdate();
				}
			}
			else
			{
				final var sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
				sm.addSkillName(skill);
				activeChar.sendPacket(sm);
			}
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
		}
	}

	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}