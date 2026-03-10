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

import l2e.commons.util.Rnd;
import gameserver.ai.DefaultAI;
import gameserver.model.Location;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;

public class RndWalkPets extends DefaultAI
{
	public RndWalkPets(Attackable actor)
	{
        super(actor);
		
		actor.setIsInvul(true);
		actor.setAutoAttackable(false);
    }

    @Override
	protected boolean thinkActive()
	{
		final var actor = getActiveChar();
		if (actor.isDead() || actor.isMoving())
		{
			return false;
		}

        final int val = Rnd.get(100);
		if (val < 10)
		{
			final var sloc = actor.getSpawnedLoc();
			if (sloc == null)
			{
				return false;
			}
			randomWalk(actor, sloc);
		}
		else if (val < 20)
		{
            actor.onRandomAnimation();
        }
        return false;
    }

    @Override
	protected boolean randomWalk(Attackable actor, Location loc)
	{
		final int x = (loc.getX() - 50) + Rnd.get(100);
		final int y = (loc.getY() - 50) + Rnd.get(100);
        actor.setRunning();
		moveTo(new Location(x, y, actor.getZ(), 0));
        return true;
    }

    @Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
    }

    @Override
	protected void onEvtAggression(Creature target, int aggro)
	{
    }
}
