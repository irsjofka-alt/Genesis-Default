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
package gameserver.model.actor.instance;

import gameserver.ai.model.CtrlIntention;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.actor.templates.npc.NpcTemplate;
import gameserver.model.entity.events.model.impl.CaptureTheFlagEvent;
import gameserver.model.skills.Skill;

public class FlagHolderInstance extends Npc
{
	public FlagHolderInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onAction(Player player, boolean interact, boolean shift)
	{
		if (!canTarget(player))
		{
			return;
		}
		
		if (!isInRange(player, INTERACTION_DISTANCE))
		{
			if (player.getAI().getIntention() != CtrlIntention.INTERACT)
			{
				player.getAI().setIntention(CtrlIntention.INTERACT, this, null);
			}
			return;
		}

		if (this != player.getTarget())
		{
			player.setTarget(this);
		}
		else if (interact)
		{
			if (!canInteract(player))
			{
				player.getAI().setIntention(CtrlIntention.INTERACT, this);
			}
			else
			{
				if (player.isInFightEvent())
				{
					if (player.getFightEvent() instanceof CaptureTheFlagEvent)
					{
						((CaptureTheFlagEvent) player.getFightEvent()).talkedWithFlagHolder(player, this);
					}
				}
			}
		}
		player.sendActionFailed();
	}

	@Override
	public void onForcedAttack(Player player, boolean shift)
	{
		onAction(player, false, shift);
	}
	
	@Override
	public void reduceCurrentHp(double damage, Creature attacker, boolean awake, boolean isDOT, Skill skill)
	{
		return;
	}
}
