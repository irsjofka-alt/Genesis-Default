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
package scripts.ai.fantasy_isle;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import l2e.commons.time.cron.SchedulingPattern;
import gameserver.Config;
import gameserver.ThreadPoolManager;
import gameserver.ai.model.CtrlIntention;
import gameserver.model.Location;
import gameserver.model.actor.Npc;
import scripts.ai.AbstractNpcAI;

/**
 * Created by LordWinter 26.10.2012 Based on L2J Eternity-World
 */
public class Parade extends AbstractNpcAI
{
	private final int[] ACTORS =
	{
	        32381, 32379, 32381, 32382, 32383, 32384, 32381, 32385, 32381, 32384, 32383, 32382, 32386, 32387, 32388, 32389, 32390, 32391, 32392, 32393, 32394, 32395, 32396, 32397, 32398, 32399, 32400, 32401, 32402, 32403, 32404, 32405, 32406, 32407, 32408, 32409, 32411, 32412, 32413, 32414, 32415, 32416, 32417, 32418, 32419, 32420, 32421, 32422, 32423, 32429, 32430, 32447, 32448, 32449, 32450, 32451, 32452, 32453, 32454, 32455, 32456, 0, 0, 0, 32415,
	};
	
	private final int[][] START1 =
	{
	        {
	                -54780, -56810, -2015, 49152
			},
			{
			        -54860, -56810, -2015, 49152
			},
			{
			        -54940, -56810, -2015, 49152
			}
	};
	
	private final int[][] GOAL1 =
	{
	        {
	                -54780, -57965, -2015, 49152
			},
			{
			        -54860, -57965, -2015, 49152
			},
			{
			        -54940, -57965, -2015, 49152
			}
	};
	
	private final int[][] START2 =
	{
	        {
	                -55715, -58900, -2015, 32768
			},
			{
			        -55715, -58820, -2015, 32768
			},
			{
			        -55715, -58740, -2015, 32768
			}
	};
	
	private final int[][] GOAL2 =
	{
	        {
	                -60850, -58900, -2015, 32768
			},
			{
			        -60850, -58820, -2015, 32768
			},
			{
			        -60850, -58740, -2015, 32768
			}
	};
	
	private final int[][] START3 =
	{
	        {
	                -61790, -57965, -2015, 16384
			},
			{
			        -61710, -57965, -2015, 16384
			},
			{
			        -61630, -57965, -2015, 16384
			}
	};
	
	private final int[][] GOAL3 =
	{
	        {
	                -61790, -53890, -2116, 16384
			},
			{
			        -61710, -53890, -2116, 16384
			},
			{
			        -61630, -53890, -2116, 16384
			}
	};
	
	private final int[][] START4 =
	{
	        {
	                -60840, -52990, -2108, 0
			},
			{
			        -60840, -53070, -2108, 0
			},
			{
			        -60840, -53150, -2108, 0
			}
	};
	
	private final int[][] GOAL4 =
	{
	        {
	                -58620, -52990, -2015, 0
			},
			{
			        -58620, -53070, -2015, 0
			},
			{
			        -58620, -53150, -2015, 0
			}
	};
	
	private final int[][] START5 =
	{
	        {
	                -57233, -53554, -2015, 57344
			},
			{
			        -57290, -53610, -2015, 57344
			},
			{
			        -57346, -53667, -2015, 57344
			}
	};
	
	private final int[][] GOAL5 =
	{
	        {
	                -55338, -55435, -2015, 57344
			},
			{
			        -55395, -55491, -2015, 57344
			},
			{
			        -55451, -55547, -2015, 57344
			}
	};
	
	private final int[][][] START =
	{
	        START1, START2, START3, START4, START5
	};
	
	private final int[][][] GOAL =
	{
	        GOAL1, GOAL2, GOAL3, GOAL4, GOAL5
	};
	
	private Future<?> _startTask;
	private Future<?> _spawnTask;
	private Future<?> _deleteTask;
	private Future<?> _cleanTask;
	
	private int _npcIndex;
	private final List<Npc> _spawns = new ArrayList<>();
	
	private void load()
	{
		_npcIndex = 0;
	}
	
	private void clean()
	{
		if (_spawns.size() > 0)
		{
			_spawns.forEach(Npc::deleteMe);
		}
		_spawns.clear();
		_npcIndex = 0;
	}
	
	private Parade()
	{
		cleanAll();
		final long diff = (new SchedulingPattern("0 20 * * *").next(System.currentTimeMillis()) - System.currentTimeMillis()), cycle = 3600000L;
		final SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		if (Config.DEBUG)
		{
			_log.info("Fantasy Isle: Parade script starting at " + format.format(System.currentTimeMillis() + diff) + " and is scheduled each next " + (cycle / 3600000) + " hours.");
		}
		
		if (_startTask == null)
		{
			_startTask= ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> start(), diff, cycle);
		}
	}
	
	private void start()
	{
		load();
		if (_spawnTask == null)
		{
			_spawnTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> spawnTask(), 100, 5000);
		}
		if (_deleteTask == null)
		{
			_deleteTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> deleteNpcs(), 10000, 1000);
		}
		if (_cleanTask == null)
		{
			_cleanTask = ThreadPoolManager.getInstance().schedule(() -> cleanAll(), 420000);
		}
	}
	
	private void spawnTask()
	{
		for (int i = 0; i < 3; ++i)
		{
			if (_npcIndex >= ACTORS.length)
			{
				final var task = _spawnTask;
				if (task != null)
				{
					task.cancel(false);
					_spawnTask = null;
				}
				break;
			}
			final int npcId = ACTORS[_npcIndex++];
			if (npcId == 0)
			{
				continue;
			}
			for (int route = 0; route < 5; ++route)
			{
				final int[] start = START[route][i];
				final int[] goal = GOAL[route][i];
				final var actor = addSpawn(npcId, start[0], start[1], start[2], start[3], false, 0);
				actor.getAI().setIntention(CtrlIntention.MOVING, new Location(goal[0], goal[1], goal[2], goal[3]), 0);
				_spawns.add(actor);
			}
		}
	}
	
	private void deleteNpcs()
	{
		if (_spawns.size() > 0)
		{
			
			final var iterator = _spawns.iterator();
			while (iterator.hasNext())
			{
				final var actor = iterator.next();
				if (actor != null)
				{
					if (actor.getPlanDistanceSq(actor.getDestination().getX(), actor.getDestination().getY()) < (100 * 100))
					{
						iterator.remove();
						actor.deleteMe();
					}
					else if (!actor.isMoving())
					{
						actor.getAI().setIntention(CtrlIntention.MOVING, actor.getDestination(), 0);
					}
				}
			}
			
			if (_spawns.size() == 0)
			{
				final var task = _deleteTask;
				if (task != null)
				{
					task.cancel(false);
					_deleteTask = null;
				}
			}
		}
	}
	
	private void cleanAll()
	{
		var task = _startTask;
		if (task != null)
		{
			task.cancel(false);
			_startTask = null;
		}
		task = _spawnTask;
		if (task != null)
		{
			task.cancel(false);
			_spawnTask = null;
		}
		task = _deleteTask;
		if (task != null)
		{
			task.cancel(false);
			_deleteTask = null;
		}
		task = _cleanTask;
		if (task != null)
		{
			task.cancel(false);
			_cleanTask = null;
		}
		clean();
	}
	
	public static void main(String[] args)
	{
		new Parade();
	}
}
