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
package scripts.ai.crypts_of_disgrace;

import gameserver.ai.npc.Fighter;
import gameserver.model.actor.Attackable;
import gameserver.network.NpcStringId;
import gameserver.network.clientpackets.Say2;
import gameserver.network.serverpackets.NpcSay;
import gameserver.utils.NpcUtils;

/**
 * Created by LordWinter 08.03.2025
 */
public class GuardoftheGrave extends Fighter
{
	private final long _despawnTime;
	private long _lastAttackTime = 0;

	public GuardoftheGrave(final Attackable actor)
	{
        super(actor);
		
		_despawnTime = actor.getTemplate().getParameter("despawnTime", 120);
		
        actor.setIsInvul(true);
		actor.setIsImmobilized(true);
    }

    @Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();
		final var actor = getActiveChar();
		_lastAttackTime = System.currentTimeMillis();
		actor.broadcastPacketToOthers(1000, new NpcSay(actor, Say2.NPC_SHOUT, NpcStringId.THOSE_WHO_ARE_IN_FRONT_OF_MY_EYES_WILL_BE_DESTROYED));
    }
	
	@Override
	protected void thinkAttack()
	{
		final var actor = getActiveChar();
		final var target = actor.getAggroList().getMostHated();
		if (target != null && !actor.isInRange(target, actor.getAggroRange()))
		{
			actor.getAggroList().remove(target.getObjectId());
			return;
		}
		super.thinkAttack();
	}
	
    @Override
	protected boolean thinkActive()
	{
		final Attackable actor = getActiveChar();
		if (_lastAttackTime != 0)
		{
			if ((_lastAttackTime + (_despawnTime * 1000L)) < System.currentTimeMillis())
			{
				NpcUtils.spawnSingle(18816, actor.getLocation());
				actor.broadcastPacketToOthers(1000, new NpcSay(actor, Say2.NPC_SHOUT, NpcStringId.I_AM_TIRED_DO_NOT_WAKE_ME_UP_AGAIN));
				actor.deleteMe();
			}
		}
		return super.thinkActive();
	}
}
