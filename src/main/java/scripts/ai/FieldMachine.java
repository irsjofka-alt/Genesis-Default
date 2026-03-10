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
import gameserver.ai.DefaultAI;
import gameserver.ai.model.CtrlEvent;
import gameserver.data.parser.CategoryParser;
import gameserver.model.CategoryType;
import gameserver.model.World;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Npc;
import gameserver.network.NpcStringId;
import gameserver.network.serverpackets.CreatureSay;

/**
 * Created by LordWinter 15.09.2018
 */
public class FieldMachine extends DefaultAI
{
	private long _lastAction;

	public FieldMachine(Attackable actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		final var actor = getActiveChar();

		if ((attacker == null) || (attacker.getActingPlayer() == null))
		{
			return;
		}

		if ((System.currentTimeMillis() - _lastAction) > 15000)
		{
			_lastAction = System.currentTimeMillis();
			actor.broadcastPacketToOthers(1500, new CreatureSay(actor.getObjectId(), 0, actor.getName(null), NpcStringId.THE_PURIFICATION_FIELD_IS_BEING_ATTACKED_GUARDIAN_SPIRITS_PROTECT_THE_MAGIC_FORCE));
			for (final Npc npc : World.getAroundNpc(actor, 1500, 200))
			{
				if (npc.isMonster() && (npc.getId() >= 22656) && (npc.getId() <= 22659))
				{
					npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 5000);
				}
			}
		}
		super.onEvtAttacked(attacker, damage);
	}
	
	@Override
	protected void onEvtDead(Creature killer)
	{
		super.onEvtDead(killer);
		if (killer != null && killer.isPlayable())
		{
			final var player = killer.getActingPlayer();
			if (player != null && CategoryParser.getInstance().isInCategory(CategoryType.WIZARD_GROUP, player.getClassId().getId()) && Rnd.get(100) < 33)
			{
				getActiveChar().dropSingleItem(player, 8605, 1);
			}
		}
	}
}
