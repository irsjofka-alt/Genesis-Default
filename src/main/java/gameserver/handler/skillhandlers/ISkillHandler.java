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
package gameserver.handler.skillhandlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gameserver.model.GameObject;
import gameserver.model.actor.Creature;
import gameserver.model.skills.Skill;
import gameserver.model.skills.SkillType;

public interface ISkillHandler
{
	public static final Logger _log = LogManager.getLogger(ISkillHandler.class);

	public void useSkill(Creature activeChar, Skill skill, GameObject[] targets, double cubicPower);
	
	public SkillType[] getSkillIds();
}