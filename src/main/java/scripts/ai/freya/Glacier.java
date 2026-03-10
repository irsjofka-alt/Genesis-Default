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
package scripts.ai.freya;

import gameserver.ThreadPoolManager;
import gameserver.ai.npc.Fighter;
import gameserver.data.parser.SkillsParser;
import gameserver.model.World;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Player;

public class Glacier extends Fighter
{
	public Glacier(Attackable actor)
	{
		super(actor);
		actor.block();
	}

	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();
		getActiveChar().setDisplayEffect(1);
		ThreadPoolManager.getInstance().schedule(new Freeze(), 800);
		ThreadPoolManager.getInstance().schedule(new Despawn(), 30000L);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		for (final Player cha : World.getAroundPlayers(getActiveChar(), 350, 200))
		{
			cha.makeTriggerCast(SkillsParser.getInstance().getInfo(6301, 1), cha);
		}
		super.onEvtDead(killer);
	}

	private class Freeze implements Runnable
	{
		@Override
		public void run()
		{
			getActiveChar().setDisplayEffect(2);
		}
	}

	private class Despawn implements Runnable
	{
		@Override
		public void run()
		{
			getActor().deleteMe();
		}
	}
}
