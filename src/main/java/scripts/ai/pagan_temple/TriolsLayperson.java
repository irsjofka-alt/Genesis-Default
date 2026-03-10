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
package scripts.ai.pagan_temple;

import l2e.commons.util.Rnd;
import gameserver.ai.npc.Fighter;
import gameserver.model.Location;
import gameserver.model.World;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Player;

/**
 * Created by LordWinter 06.12.2020
 */
public class TriolsLayperson extends Fighter
{
	private boolean _tele = false;

	public static final Location[] _locs =
	{
	        new Location(-16128, -35888, -10726), new Location(-17029, -39617, -10724), new Location(-15729, -42001, -10724)
	};

	public TriolsLayperson(Attackable actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtSpawn()
	{
		final Attackable actor = getActiveChar();
		if (actor == null)
		{
			return;
		}
		_tele = Rnd.chance(30);
		super.onEvtSpawn();
	}
	
	@Override
	protected boolean thinkActive()
	{
		final Attackable actor = getActiveChar();
		if (actor == null || !_tele)
		{
			return true;
		}

		if (Rnd.chance(5))
		{
			for (final Player player : World.getAroundPlayers(actor, 500, 500))
			{
				if (player == null || !player.isInParty())
				{
					continue;
				}

				if (player.getParty().getMemberCount() >= 5)
				{
					_tele = false;
					player.teleToLocation(Rnd.get(_locs), true, player.getReflection());
				}
			}
		}
		return true;
	}
}