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
package gameserver.handler.actionhandlers.impl;

import java.util.List;

import gameserver.Config;
import gameserver.ai.model.CtrlEvent;
import gameserver.ai.model.CtrlIntention;
import gameserver.ai.model.NextAction;
import gameserver.ai.model.NextAction.NextActionCallback;
import gameserver.geodata.GeoEngine;
import gameserver.handler.actionhandlers.IActionHandler;
import gameserver.model.GameObject;
import gameserver.model.GameObject.InstanceType;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.quest.Quest;
import gameserver.model.quest.QuestEventType;
import gameserver.network.serverpackets.ValidateLocation;

public class NpcAction implements IActionHandler
{
	@Override
	public boolean action(Player activeChar, GameObject target, boolean interact, boolean shift)
	{
		final Npc npc = (Npc) target;
		if (!npc.canTarget(activeChar))
		{
			return false;
		}
		
		for (final var e : activeChar.getFightEvents())
		{
			if (e != null && !e.canAction((Creature) target, activeChar))
			{
				return false;
			}
		}

		activeChar.setLastFolkNPC(npc);

		if (npc != activeChar.getTarget())
		{
			activeChar.setTarget(target);
			activeChar.sendPacket(new ValidateLocation(npc));
			return false;
		}
		
		if (interact)
		{
			activeChar.sendPacket(new ValidateLocation(npc));
			if (npc.isAutoAttackable(activeChar, false) && !npc.isAlikeDead())
			{
				if (GeoEngine.getInstance().canSeeTarget(activeChar, npc) || shift)
				{
					activeChar.getAI().setIntention(CtrlIntention.ATTACK, npc, shift);
				}
				else
				{
					activeChar.sendActionFailed();
					activeChar.getAI().setIntention(CtrlIntention.MOVING, npc.getLocation(), 40);
					if (activeChar.isMoving() && !activeChar.getFarmSystem().isAutofarming())
					{
						final NextAction nextAction = new NextAction(activeChar, npc, CtrlEvent.EVT_ARRIVED_ATTACK, CtrlIntention.ATTACK, new NextActionCallback()
						{
							@Override
							public void doWork()
							{
								if (npc != null)
								{
									activeChar.getAI().setIntention(CtrlIntention.ATTACK, npc, false);
								}
							}
						});
						activeChar.getAI().setNextAction(nextAction);
					}
				}
			}
			else if (!npc.isAutoAttackable(activeChar, false))
			{
				if (!npc.canInteract(activeChar))
				{
					if (activeChar.getAI().getIntention() != CtrlIntention.INTERACT)
					{
						activeChar.getAI().setIntention(CtrlIntention.INTERACT, npc);
					}
					return false;
				}
				
				final List<Quest> qlsa = npc.getTemplate().getEventQuests(QuestEventType.QUEST_START);
				final List<Quest> qlst = npc.getTemplate().getEventQuests(QuestEventType.ON_FIRST_TALK);
					
				if ((qlsa != null) && !qlsa.isEmpty())
				{
					activeChar.setLastQuestNpcObject(target.getObjectId());
				}
					
				if ((qlst != null) && qlst.size() == 1)
				{
					qlst.getFirst().notifyFirstTalk(npc, activeChar);
				}
				else
				{
					npc.showChatWindow(activeChar);
				}
				npc.getListeners().onShowChat();

				if (Config.PLAYER_MOVEMENT_BLOCK_TIME > 0 && !activeChar.isGM())
				{
					activeChar.updateNotMoveUntil();
				}
			}
		}
		return true;
	}

	@Override
	public InstanceType getInstanceType()
	{
		return InstanceType.Npc;
	}
}