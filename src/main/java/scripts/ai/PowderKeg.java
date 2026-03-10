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
package scripts.ai;

import gameserver.ai.npc.Fighter;
import gameserver.data.parser.SkillsParser;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;

/**
 * Created by LordWinter 16.09.2018
 */
public class PowderKeg extends Fighter
{
	public PowderKeg(Attackable actor)
	{
		super(actor);
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		final Attackable actor = getActiveChar();
		if (attacker == null || actor == null)
		{
			return;
		}
		actor.setTarget(actor);
		actor.doCast(SkillsParser.getInstance().getInfo(5714, 1));
		actor.doDie(null);
		
		super.onEvtAttacked(attacker, damage);
	}
}
