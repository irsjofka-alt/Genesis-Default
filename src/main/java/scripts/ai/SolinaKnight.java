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

import gameserver.ai.model.CtrlEvent;
import gameserver.ai.npc.Fighter;
import gameserver.model.World;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Npc;

/**
 * Created by LordWinter 22.11.2018
 */
public class SolinaKnight extends Fighter
{
	private Npc scarecrow = null;

	public SolinaKnight(Attackable actor)
	{
		super(actor);
		
		actor.setIsGlobalAI(true);
	}

	@Override
	protected boolean thinkActive()
	{
		if (scarecrow == null)
		{
			for (final Npc npc : World.getAroundNpc(getActiveChar(), 400, 200))
			{
				if (npc.getId() == 18912)
				{
					if ((scarecrow == null) || (getActiveChar().getDistance3D(npc) < getActiveChar().getDistance3D(scarecrow)))
					{
						scarecrow = npc;
					}
				}
			}
		}
		else
		{
			getActiveChar().getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, scarecrow, 1);
			return true;
		}
		return super.thinkActive();
	}
}