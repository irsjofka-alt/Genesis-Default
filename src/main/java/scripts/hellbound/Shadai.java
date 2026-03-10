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
package scripts.hellbound;

import java.util.concurrent.Future;

import gameserver.GameTimeController;
import gameserver.ThreadPoolManager;
import gameserver.model.actor.Npc;
import gameserver.model.quest.Quest;

public class Shadai extends Quest
{
	private Future<?> _checkTask = null;
	
	private static final int[] DAY_COORDS =
	{
	        111545, 220238, -3672
	};
	
	private static final int[] NIGHT_COORDS =
	{
	        9064, 253037, -1928
	};
	
	private Shadai()
	{
		super(-1);
		
		addSpawnId(32347);
	}
	
	@Override
	public final String onSpawn(Npc npc)
	{
		if (_checkTask == null)
		{
			_checkTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> checkPosition(npc), 60000, 60000);
		}
		return super.onSpawn(npc);
	}
	
	private void checkPosition(Npc npc)
	{
		if (npc == null || !npc.isVisible())
		{
			return;
		}
		
		int[] coords = DAY_COORDS;
		boolean mustRevalidate = false;
		if ((npc.getX() != NIGHT_COORDS[0]) && GameTimeController.getInstance().isNight())
		{
			coords = NIGHT_COORDS;
			mustRevalidate = true;
		}
		else if ((npc.getX() != DAY_COORDS[0]) && !GameTimeController.getInstance().isNight())
		{
			mustRevalidate = true;
		}
		
		if (mustRevalidate)
		{
			npc.getSpawn().setX(coords[0]);
			npc.getSpawn().setY(coords[1]);
			npc.getSpawn().setZ(coords[2]);
			npc.teleToLocation(coords[0], coords[1], coords[2], true, npc.getReflection());
		}
	}
	
	public static void main(String[] args)
	{
		new Shadai();
	}
}