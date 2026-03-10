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
package scripts.ai.gracia;

import l2e.commons.util.ArrayUtils;
import l2e.commons.util.Rnd;
import gameserver.ai.model.CtrlEvent;
import gameserver.ai.npc.Fighter;
import gameserver.data.parser.SkillsParser;
import gameserver.instancemanager.ReflectionManager;
import gameserver.model.Location;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Npc;
import gameserver.model.entity.Reflection;
import gameserver.model.skills.effects.Effect;
import gameserver.utils.NpcUtils;

public class YehanBrother extends Fighter
{
	private long _spawnTimer = 0;
	private static final int[] _minions = ArrayUtils.createAscendingArray(22509, 22512);

	public YehanBrother(Attackable actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();
		_spawnTimer = System.currentTimeMillis();
	}

	private Npc getBrother()
	{
		final Attackable actor = getActiveChar();
		if (actor == null)
		{
			return null;
		}
		
		int brotherId = 0;
		if (actor.getId() == 25665)
		{
			brotherId = 25666;
		}
		else if (actor.getId() == 25666)
		{
			brotherId = 25665;
		}
		
		final Reflection inst = ReflectionManager.getInstance().getReflection(actor.getReflectionId());
		if (inst != null)
		{
			for (final Npc npc : inst.getNpcs())
			{
				if (npc.getId() == brotherId)
				{
					return npc;
				}
			}
		}
		return null;
	}

	@Override
	protected void thinkAttack()
	{
		final Attackable actor = getActiveChar();
		if (actor == null)
		{
			return;
		}
		
		final Npc brother = getBrother();
		if (brother != null && !brother.isDead() && !actor.isInRange(brother, 300))
		{
			actor.makeTriggerCast(SkillsParser.getInstance().getInfo(6371, 1), actor);
		}
		else
		{
			removeInvul(actor);
		}
		if(_spawnTimer + 40000 < System.currentTimeMillis())
		{
			_spawnTimer = System.currentTimeMillis();
			final Npc mob = NpcUtils.spawnSingle(_minions[Rnd.get(_minions.length)], Location.findAroundPosition(actor, 100, 300), actor.getReflection(), 0);
			final var target = getAttackTarget();
			if (target != null)
			{
				mob.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, target, 1000);
			}
		}
		super.thinkAttack();
	}

	private void removeInvul(Npc npc)
	{
		for(final Effect e : npc.getEffectList().getAllEffects())
		{
			if(e.getSkill().getId() == 6371)
			{
				e.exit(true);
			}
		}
	}
}