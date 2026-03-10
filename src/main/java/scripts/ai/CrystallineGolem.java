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
import gameserver.model.Location;
import gameserver.model.World;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;
import gameserver.model.items.instance.ItemInstance;
import gameserver.network.NpcStringId;
import gameserver.network.serverpackets.CreatureSay;
import gameserver.network.serverpackets.MagicSkillUse;

public class CrystallineGolem extends Fighter
{
	private ItemInstance _closestItem;
	private long _nextEat;

	public CrystallineGolem(Attackable actor)
	{
		super(actor);
		
		actor.setIsRunner(true);
		actor.setCanReturnToSpawnPoint(false);
	}

	@Override
	protected boolean thinkActive()
	{
		final var actor = getActiveChar();
		if(actor.isDead())
		{
			return true;
		}

		final var r = actor.getReflection();
		if (!r.isDefault() && actor.isScriptValue(0) && _nextEat < System.currentTimeMillis())
		{
			if (!actor.isMoving())
			{
				if (_closestItem == null)
				{
					for (final var object : World.getAroundObjects(actor, 300, 200))
					{
						if (object.isItem() && object.getId() == 9693)
						{
							_closestItem = (ItemInstance) object;
							break;
						}
					}
					
					if (_closestItem != null)
					{
						actor.setWalking();
						moveTo(_closestItem.getLocation());
						return true;
					}
					else
					{
						if (Rnd.chance(5))
						{
							actor.broadcastPacketToOthers(2000, new CreatureSay(actor.getObjectId(), 1, actor.getName(null), NpcStringId.AH_IM_HUNGRY));
						}
					}
				}
			}
			
			if (!r.getParams().getBool("stage1", false))
			{
				final int dx = actor.getX() - 142999;
				final int dy = actor.getY() - 151671;
				if (dx * dx + dy * dy < 10000)
				{
					r.setParam("stage1", true);
					actor.setScriptValue(1);
					actor.broadcastPacket(new MagicSkillUse(actor, actor, 5441, 1, 1, 0));
				}
			}
			
			if (!r.getParams().getBool("stage2", false))
			{
				final int dx = actor.getX() - 139494;
				final int dy = actor.getY() - 151668;
				if (dx * dx + dy * dy < 10000)
				{
					r.setParam("stage2", true);
					actor.setScriptValue(1);
					actor.broadcastPacket(new MagicSkillUse(actor, actor, 5441, 1, 1, 0));
				}
			}
			
			if (r.getParams().getBool("stage1", false) && r.getParams().getBool("stage2", false))
			{
				r.openDoor(24220026);
				r.setStatus(4);
			}
		}
		return true;
	}
	
	@Override
	protected void onEvtArrived()
	{
		super.onEvtArrived();
		
		final var actor = getActiveChar();
		if (actor == null)
		{
			return;
		}
		
		if (_closestItem != null)
		{
			if (actor.getDistance(_closestItem) <= 40)
			{
				_nextEat = System.currentTimeMillis() + 2000;
				_closestItem.deleteMe();
				_closestItem = null;
			}
		}
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{}

	@Override
	protected void onEvtAggression(Creature target, int aggro)
	{}

	@Override
	protected boolean randomWalk(Attackable actor, Location loc)
	{
		return false;
	}
}