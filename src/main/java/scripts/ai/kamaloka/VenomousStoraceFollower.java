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
package scripts.ai.kamaloka;


import gameserver.ai.npc.Fighter;
import gameserver.model.World;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Npc;
import gameserver.network.NpcStringId;
import gameserver.network.serverpackets.NpcSay;

/**
 * Created by LordWinter 10.12.2018
 */
public class VenomousStoraceFollower extends Fighter
{
	private long _skillTimer = 0L;
	private final static long _skillInterval = 20000L;

	public VenomousStoraceFollower(Attackable actor)
	{
		super(actor);
	}

	@Override
	protected void thinkAttack()
	{
		final Attackable actor = getActiveChar();
		
		if (_skillTimer == 0)
		{
			_skillTimer = System.currentTimeMillis();
		}
		
		if ((_skillTimer + _skillInterval) < System.currentTimeMillis())
		{
			Npc boss = null;
			for (final Npc npc : World.getAroundNpc(actor))
			{
				if (npc.getId() == 18571)
				{
					boss = npc;
				}
			}

			if (boss != null)
			{
				if (boss.getCurrentHpPercents() < 70)
				{
					boss.setCurrentHp(boss.getCurrentHp() + (boss.getMaxHp() * 0.2));
				}
				else
				{
					boss.setCurrentHp(boss.getMaxHp() - 10);
				}
				actor.broadcastPacketToOthers(2000, new NpcSay(actor.getObjectId(), 0, actor.getId(), NpcStringId.THERES_NOT_MUCH_I_CAN_DO_BUT_I_WILL_RISK_MY_LIFE_TO_HELP_YOU));
			}
			actor.doDie(null);
		}
		super.thinkAttack();
	}
}