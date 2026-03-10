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
package scripts.ai.sevensigns;


import gameserver.ai.npc.Mystic;
import gameserver.model.World;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Npc;
import gameserver.network.serverpackets.MagicSkillUse;

/**
 * Created by LordWinter 10.12.2018
 */
public class Lilith extends Mystic
{
	private long _lastSkillTime = 0;

	public Lilith(Attackable actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtSpawn()
	{
		getActiveChar().setIsNoRndWalk(true);
		getActiveChar().setIsInvul(true);
		super.onEvtSpawn();
	}

	@Override
	protected boolean thinkActive()
	{
		if (_lastSkillTime < System.currentTimeMillis())
		{
			if (getAnakim() != null)
			{
				getActiveChar().broadcastPacketToOthers(2000, new MagicSkillUse(getActiveChar(), getAnakim(), 6187, 1, 5000, 10));
			}
			_lastSkillTime = System.currentTimeMillis() + 6500;
		}
		return true;
	}

	private Npc getAnakim()
	{
		Npc anakim = null;
		for (final Npc npc : World.getAroundNpc(getActiveChar()))
		{
			if (npc.getId() == 32718)
			{
				anakim = npc;
			}
		}
		return anakim;
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
	}

	@Override
	protected void onEvtAggression(Creature attacker, int aggro)
	{
	}
}