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
package scripts.ai.gracia;

import gameserver.ai.npc.Fighter;
import gameserver.model.actor.Attackable;

/**
 * Created by LordWinter 30.09.2020
 */
public class FeralHound extends Fighter
{
	public FeralHound(Attackable actor)
	{
		super(actor);
		
		actor.setIsInvul(true);
		actor.setRandomAnimationEnabled(false);
		actor.setIsNoRndWalk(true);
	}
}