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

import org.apache.commons.lang3.ArrayUtils;
import gameserver.ai.DefaultAI;
import gameserver.data.parser.SkillsParser;
import gameserver.model.World;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Npc;

/**
 * Created by LordWinter 27.09.2020
 */
public class AliveTumor extends DefaultAI
{
	private long _checkTimer = 0;
	private int _coffinsCount = 0;
	private static final int[] regenCoffins =
	{
	        18706, 18709, 18710
	};

	public AliveTumor(Attackable actor)
	{
		super(actor);
		
		actor.setIsImmobilized(true);
	}

	@Override
	protected boolean thinkActive()
	{
		final Attackable actor = getActiveChar();
		if (actor == null)
		{
			return false;
		}
		
		if (_checkTimer + 10000 < System.currentTimeMillis())
		{
			_checkTimer = System.currentTimeMillis();
			int i = 0;
			for (final Npc n : World.getAroundNpc(actor, 400, 300))
			{
				if (ArrayUtils.contains(regenCoffins, n.getId()) && !n.isDead())
				{
					i++;
				}
			}
			
			if (_coffinsCount != i)
			{
				_coffinsCount = i;
				_coffinsCount = Math.min(_coffinsCount, 12);
				if (_coffinsCount > 0)
				{
					actor.makeTriggerCast(SkillsParser.getInstance().getInfo(5940, _coffinsCount), actor);
				}
			}
		}
		return super.thinkActive();
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