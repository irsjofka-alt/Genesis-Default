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

import l2e.commons.util.Rnd;
import gameserver.handler.skillhandlers.ISkillHandler;
import gameserver.model.GameObject;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Player;
import gameserver.model.skills.Skill;
import gameserver.model.skills.SkillType;
import gameserver.network.serverpackets.ValidateLocation;

public class GetPlayer implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS =
	{
	        SkillType.GET_PLAYER
	};
	
	@Override
	public void useSkill(Creature activeChar, Skill skill, GameObject[] targets, double cubicPower)
	{
		if (activeChar.isAlikeDead())
		{
			return;
		}
		
		for (final GameObject target : targets)
		{
			if (target.isPlayer())
			{
				final Player trg = target.getActingPlayer();
				
				if (trg.isAlikeDead())
				{
					continue;
				}
				
				trg.setXYZ(activeChar.getX() + Rnd.get(-10, 10), activeChar.getY() + Rnd.get(-10, 10), activeChar.getZ());
				trg.sendPacket(new ValidateLocation(trg));
			}
		}
	}

	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}