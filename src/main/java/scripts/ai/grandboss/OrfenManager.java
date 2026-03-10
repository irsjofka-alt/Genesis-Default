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
package scripts.ai.grandboss;

import java.util.concurrent.Future;

import gameserver.Config;
import gameserver.ThreadPoolManager;
import gameserver.instancemanager.EpicBossManager;
import gameserver.model.Location;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.actor.instance.GrandBossInstance;
import gameserver.network.serverpackets.PlaySound;
import scripts.ai.AbstractNpcAI;

public class OrfenManager extends AbstractNpcAI
{
	private GrandBossInstance _boss = null;
	private Future<?> _respawnTask = null;
	
	private OrfenManager()
	{
		addKillId(29014);
		
		final var manager = EpicBossManager.getInstance();
		final var info = manager.getStatsSet(29014);
		if (info != null)
		{
			final int status = manager.getBossStatus(29014);
			if (status == 3)
			{
				final long respawn = info.getLong("respawnTime") - System.currentTimeMillis();
				if (respawn > 0)
				{
					_respawnTask = ThreadPoolManager.getInstance().schedule(() -> spawn(), respawn);
				}
				else
				{
					spawn();
				}
			}
			else
			{
				_boss = (GrandBossInstance) addSpawn(29014, info.getInteger("loc_x"), info.getInteger("loc_y"), info.getInteger("loc_z"), info.getInteger("heading"), false, 0);
				_boss.setCurrentHpMp(info.getInteger("currentHP"), info.getInteger("currentMP"));
				spawnBoss(_boss);
			}
		}
	}

	public void spawnBoss(GrandBossInstance npc)
	{
		EpicBossManager.getInstance().addBoss(npc);
		npc.broadcastPacketToOthers(new PlaySound(1, "BS01_A", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
	}

	private void spawn()
	{
		final int i = getRandom(10);
		Location loc;
		if (i < 4)
		{
			loc = new Location(55024, 17368, -5412);
		}
		else if (i < 7)
		{
			loc = new Location(53504, 21248, -5486);
		}
		else
		{
			loc = new Location(53248, 24576, -5262);
		}
		_boss = (GrandBossInstance) addSpawn(29014, loc, false, 0);
		EpicBossManager.getInstance().setBossStatus(29014, 0, true);
		spawnBoss(_boss);
	}

	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		npc.broadcastPacketToOthers(new PlaySound(1, "BS02_D", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
		final long respawnTime = EpicBossManager.getInstance().setRespawnTime(29014, Config.ORFEN_RESPAWN_PATTERN);
		_respawnTask = ThreadPoolManager.getInstance().schedule(() -> spawn(), (respawnTime - System.currentTimeMillis()));
		_boss = null;
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public boolean unload(boolean removeFromList)
	{
		final var task = _respawnTask;
		if (task != null)
		{
			task.cancel(true);
			_respawnTask = null;
		}
		
		if (_boss != null)
		{
			_boss.deleteMe();
			_boss = null;
		}
		return super.unload(removeFromList);
	}

	public static void main(String[] args)
	{
		new OrfenManager();
	}
}
