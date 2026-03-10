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
package gameserver.model.actor.status;

import gameserver.ai.model.CtrlEvent;
import gameserver.model.actor.Creature;
import gameserver.model.actor.instance.PetInstance;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.SystemMessage;

public class PetStatus extends SummonStatus
{
	private int _currentFed = 0;

	public PetStatus(PetInstance activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public final void reduceHp(double value, Creature attacker)
	{
		reduceHp(value, attacker, true, false, false, true);
	}

	@Override
	public final void reduceHp(double value, Creature attacker, boolean awake, boolean isDOT, boolean isHpConsumption, boolean broadcastPacket)
	{
		if (getActiveChar().isDead())
		{
			return;
		}

		super.reduceHp(value, attacker, awake, isDOT, isHpConsumption, broadcastPacket);

		if (attacker != null)
		{
			if (!isDOT && getActiveChar().getOwner() != null)
			{
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PET_RECEIVED_S2_DAMAGE_BY_C1);
				sm.addCharName(attacker);
				sm.addNumber((int) value);
				getActiveChar().sendPacket(sm);
			}
			getActiveChar().getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, attacker, (int) value);
		}
	}

	public int getCurrentFed()
	{
		return _currentFed;
	}

	public void setCurrentFed(int value)
	{
		_currentFed = value;
	}

	@Override
	public PetInstance getActiveChar()
	{
		return (PetInstance) super.getActiveChar();
	}
}