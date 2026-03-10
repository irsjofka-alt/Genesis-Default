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
package scripts.ai.pagan_temple;

import gameserver.ai.npc.Mystic;
import gameserver.model.actor.Attackable;

/**
 * Created by LordWinter 05.12.2021
 */
public class PaganGuard extends Mystic
{
	public PaganGuard(Attackable actor)
	{
		super(actor);
		
		actor.setIsNoRndWalk(true);
		actor.setIsImmobilized(true);
	}

	@Override
	protected void movementDisable()
	{
		final var actor = getActiveChar();
		final var target = getAttackTarget();
		if (target != null && actor.getAggroList().get(target.getObjectId()) != null && Math.sqrt(actor.getDistanceSq(target)) >= 600)
		{
			actor.getAggroList().remove(target.getObjectId());
			return;
		}
		super.movementDisable();
	}
}