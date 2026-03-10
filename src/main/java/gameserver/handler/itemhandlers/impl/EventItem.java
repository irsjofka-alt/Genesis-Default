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
package gameserver.handler.itemhandlers.impl;

import gameserver.handler.itemhandlers.IItemHandler;
import gameserver.instancemanager.HandysBlockCheckerManager;
import gameserver.model.ArenaParticipantsHolder;
import gameserver.model.World;
import gameserver.model.actor.Playable;
import gameserver.model.actor.Player;
import gameserver.model.actor.instance.BlockInstance;
import gameserver.model.items.instance.ItemInstance;
import gameserver.model.skills.Skill;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.SystemMessage;

public class EventItem implements IItemHandler
{
	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!playable.isPlayer())
		{
			playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
			return false;
		}

		boolean used = false;
		
		final Player activeChar = playable.getActingPlayer();
		
		final int itemId = item.getId();
		switch (itemId)
		{
			case 13787 :
				used = useBlockCheckerItem(activeChar, item);
				break;
			case 13788 :
				used = useBlockCheckerItem(activeChar, item);
				break;
			default :
				_log.warn("EventItemHandler: Item with id: " + itemId + " is not handled");
		}
		return used;
	}
	
	private final boolean useBlockCheckerItem(final Player castor, ItemInstance item)
	{
		final int blockCheckerArena = castor.getBlockCheckerArena();
		if (blockCheckerArena == -1)
		{
			final SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			msg.addItemName(item);
			castor.sendPacket(msg);
			return false;
		}

		final Skill sk = item.getEtcItem().getSkills()[0].getSkill();
		if (sk == null)
		{
			return false;
		}
		
		if (!castor.destroyItem("Consume", item, 1, castor, true))
		{
			return false;
		}
		
		final BlockInstance block = (BlockInstance) castor.getTarget();
		
		final ArenaParticipantsHolder holder = HandysBlockCheckerManager.getInstance().getHolder(blockCheckerArena);
		if (holder != null)
		{
			final int team = holder.getPlayerTeam(castor);
			for (final Player pc : World.getAroundPlayers(block, sk.getAffectRange(), 200))
			{
				final int enemyTeam = holder.getPlayerTeam(pc);
				if (enemyTeam != -1 && enemyTeam != team)
				{
					sk.getEffects(castor, pc, false, true);
				}
			}
			return true;
		}
		_log.warn("Char: " + castor.getName(null) + "[" + castor.getObjectId() + "] has unknown block checker arena");
		return false;
	}
}