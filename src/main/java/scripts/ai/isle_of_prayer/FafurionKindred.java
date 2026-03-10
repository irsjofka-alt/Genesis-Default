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
package scripts.ai.isle_of_prayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import l2e.commons.util.Rnd;
import gameserver.ThreadPoolManager;
import gameserver.ai.npc.Fighter;
import gameserver.data.parser.ItemsParser;
import gameserver.data.parser.NpcsParser;
import gameserver.model.Location;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Npc;
import gameserver.model.skills.Skill;
import gameserver.model.spawn.Spawner;

/**
 * Created by LordWinter 21.09.2018
 */
public class FafurionKindred extends Fighter
{
	private Future<?> _poisonTask;
	private Future<?> _despawnTask;

	private final List<Npc> _spawns = new ArrayList<>();

	public FafurionKindred(Attackable actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();

		_spawns.clear();

		ThreadPoolManager.getInstance().schedule(new SpawnTask(22270), 500);
		ThreadPoolManager.getInstance().schedule(new SpawnTask(22271), 500);
		ThreadPoolManager.getInstance().schedule(new SpawnTask(22270), 500);
		ThreadPoolManager.getInstance().schedule(new SpawnTask(22271), 500);

		_poisonTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new PoisonTask(), 3000, 3000);
		_despawnTask = ThreadPoolManager.getInstance().schedule(new DeSpawnTask(), 300000);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		cleanUp();

		super.onEvtDead(killer);
	}

	@Override
	protected void onEvtSeeSpell(Skill skill, Creature caster)
	{
		final var actor = getActiveChar();
		if (actor.isDead() || (skill == null))
		{
			return;
		}

		if (skill.getId() == 2368)
		{
			actor.setCurrentHp(actor.getCurrentHp() + 3000);
		}
		
		if (caster != null)
		{
			actor.getAggroList().remove(caster.getObjectId());
		}
	}

	private void cleanUp()
	{
		var task = _poisonTask;
		if (task != null)
		{
			task.cancel(false);
			_poisonTask = null;
		}
		
		task = _despawnTask;
		if (task != null)
		{
			task.cancel(false);
			_despawnTask = null;
		}

		_spawns.stream().filter(n -> n != null).forEach(n -> n.deleteMe());
		_spawns.clear();
	}

	private class SpawnTask implements Runnable
	{
		private final int _id;

		public SpawnTask(int id)
		{
			_id = id;
		}

		@Override
		public void run()
		{
			try
			{
				final var actor = getActiveChar();
				final var sp = new Spawner(NpcsParser.getInstance().getTemplate(_id));
				if (sp != null)
				{
					sp.setLocation(Location.findPointToStay(actor, 100, 120, true));
					sp.setRespawnDelay(30, 40);
					final var npc = sp.doSpawn(true, 0);
					if (npc != null)
					{
						_spawns.add(npc);
					}
				}
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	private class PoisonTask implements Runnable
	{
		@Override
		public void run()
		{
			final var actor = getActiveChar();
			actor.reduceCurrentHp(500, actor, null);
		}
	}

	private class DeSpawnTask implements Runnable
	{
		@Override
		public void run()
		{
			final var actor = getActiveChar();
			dropItem(actor, 9691, Rnd.get(1, 2));
			if (Rnd.chance(36))
			{
				dropItem(actor, 9700, Rnd.get(1, 3));
			}

			cleanUp();
			actor.deleteMe();
		}
	}

	private void dropItem(Attackable actor, int id, int count)
	{
		final var item = ItemsParser.getInstance().createItem(id);
		item.setCount(count);
		item.dropMe(actor, Location.findPointToStay(actor, 100, true), false);
	}
}
