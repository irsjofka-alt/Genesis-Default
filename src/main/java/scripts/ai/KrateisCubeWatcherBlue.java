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
import gameserver.model.World;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Player;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.SystemMessage;

/**
 * Rework by LordWinter
 */
public class KrateisCubeWatcherBlue extends Fighter
{
	private boolean _isAttacked = false;
	
	public KrateisCubeWatcherBlue(Attackable actor)
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
				final double valCP = cha.getMaxCp() - cha.getCurrentCp();
				if (valCP > 0)
				{
					cha.setCurrentCp(valCP + cha.getCurrentCp());
					cha.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CP_WILL_BE_RESTORED).addNumber(Math.round((int) valCP)));
				}

				final double valHP = cha.getMaxHp() - cha.getCurrentHp();
				if (valHP > 0)
				{
					cha.setCurrentHp(valHP + cha.getCurrentHp());
					cha.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HP_RESTORED).addNumber(Math.round((int) valHP)));
				}

				final double valMP = cha.getMaxMp() - cha.getCurrentMp();
				if (valMP > 0)
				{
					cha.setCurrentMp(valMP + cha.getCurrentMp());
					cha.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_MP_RESTORED).addNumber(Math.round((int) valMP)));
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
