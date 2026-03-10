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
import gameserver.ai.npc.Fighter;
import gameserver.data.parser.SkillsParser;
import gameserver.model.World;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Player;
import gameserver.model.skills.Skill;

/**
 * Rework by LordWinter
 */
public class KrateisCubeWatcherRed extends Fighter
{
	private static final int[][] SKILLS =
	{
	        {
	                1064, 14
			},
			{
			        1160, 15
			},
			{
			        1164, 19
			},
			{
			        1167, 6
			},
			{
			        1168, 7
			}
	};
	
	private boolean _isAttacked = false;

	public KrateisCubeWatcherRed(Attackable actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		final Attackable actor = getActiveChar();
		if (actor != null && actor.getArena() != null && !_isAttacked)
		{
			_isAttacked = true;
			actor.getArena().chaneWatcher(actor);
		}
	}

	@Override
	protected boolean thinkActive()
	{
		final Attackable actor = getActiveChar();
		if (actor.isDead())
		{
			return true;
		}
		
		for (final Player cha : World.getAroundPlayers(actor, 600, 200))
		{
			if (cha != null && !cha.isDead() && Rnd.chance(60))
			{
				final int rnd = Rnd.get(SKILLS.length);
				final Skill skill = SkillsParser.getInstance().getInfo(SKILLS[rnd][0], SKILLS[rnd][1]);
				if (skill != null && cha.getFirstEffect(skill) == null)
				{
					skill.getEffects(cha, cha, false, true);
				}
			}
		}
		return true;
	}

	@Override
	public void onEvtDead(Creature killer)
	{
		super.onEvtDead(killer);
		final Attackable actor = getActiveChar();
		if (actor != null && actor.getArena() != null)
		{
			actor.getArena().chaneWatcher(actor);
		}
	}
}
