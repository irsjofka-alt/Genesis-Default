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
package scripts.ai.kamaloka;

import java.util.ArrayList;
import java.util.List;

import l2e.commons.util.Rnd;
import gameserver.ai.model.CtrlIntention;
import gameserver.ai.npc.Mystic;
import gameserver.data.parser.SkillsParser;
import gameserver.model.Location;
import gameserver.model.World;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Player;

/**
 * Created by LordWinter 10.12.2018
 */
public class WhiteAllosceFollower extends Mystic
{
	private long _skillTimer = 0L;
	private final static long _skillInterval = 15000L;
	
	public WhiteAllosceFollower(Attackable actor)
	{
		super(actor);

		actor.setIsInvul(true);
	}
	
	@Override
	protected boolean thinkActive()
	{
		final Attackable actor = getActiveChar();

		if ((_skillTimer + _skillInterval) < System.currentTimeMillis())
		{
			final List<Player> aggressionList = new ArrayList<>();
			for (final Player p : World.getAroundPlayers(actor, 1000, 200))
			{
				if (!p.isDead() && !p.isInvisible())
				{
					actor.addDamageHate(p, 0, 10);
					aggressionList.add(p.getActingPlayer());
				}
			}
			
			if (!aggressionList.isEmpty())
			{
				final Player aggressionTarget = aggressionList.get(Rnd.get(aggressionList.size()));
				if (aggressionTarget != null)
				{
					actor.setTarget(aggressionTarget);
					actor.doCast(SkillsParser.getInstance().getInfo(5624, 1));
				}
			}
			setIntention(CtrlIntention.ACTIVE);
			moveTo(Location.findPointToStay(actor, 400, true));
			_skillTimer = System.currentTimeMillis() + Rnd.get(1L, 5000L);
		}
		return super.thinkActive();
	}
	
	@Override
	protected void thinkAttack()
	{
		setIntention(CtrlIntention.ACTIVE);
	}
}