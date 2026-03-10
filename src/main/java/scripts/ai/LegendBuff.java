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
import gameserver.model.World;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;
import gameserver.model.skills.Skill;

/**
 * Created by LordWinter 29.12.2024
 */
public class LegendBuff extends Fighter
{
	private final Skill _buff = SkillsParser.getInstance().getInfo(6235, 1);
	private long _waitTimeout = 0;
	
	public LegendBuff(Attackable actor)
	{
		super(actor);
		
		actor.setIsImmobilized(true);
		actor.setIsInvul(true);
	}
	
	@Override
	protected boolean thinkActive()
	{
		final var actor = getActiveChar();
		if (actor == null)
		{
			return false;
		}
		
		if (_buff != null && _waitTimeout < System.currentTimeMillis())
		{
			_waitTimeout = System.currentTimeMillis() + 15000L;
			for (final var player : World.getAroundPlayers(actor, 300, 300))
			{
				if (player == null || player.isDead() || player.getFirstEffect(_buff.getId()) != null)
				{
					continue;
				}
				_buff.getEffects(player, player, false, true);
			}
		}
		
		return true;
	}

	@Override
	protected boolean checkAggression(Creature target)
	{
		return false;
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		return;
	}
}