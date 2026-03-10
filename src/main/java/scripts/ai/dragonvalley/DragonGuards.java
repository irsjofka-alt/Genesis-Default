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
package scripts.ai.dragonvalley;

import gameserver.ai.model.CtrlIntention;
import gameserver.model.World;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.actor.instance.MonsterInstance;
import scripts.ai.AbstractNpcAI;

/**
 * Created by LordWinter 10.05.2012 Based on L2J Eternity-World
 */
public class DragonGuards extends AbstractNpcAI
{
	private static final int DRAGON_GUARD = 22852;
	private static final int DRAGON_MAGE = 22853;

	private static final int[] WALL_MONSTERS =
	{
	                DRAGON_GUARD, DRAGON_MAGE
	};

	private DragonGuards()
	{
		for (final int mobId : WALL_MONSTERS)
		{
			addAggroRangeEnterId(mobId);
			addAttackId(mobId);
		}
	}

	@Override
	public String onAggroRangeEnter(Npc npc, Player player, boolean isSummon)
	{
		if ((!npc.isCastingNow()) && (!npc.isAttackingNow()) && (!npc.isInCombat()) && (!player.isDead()))
		{
			npc.setIsImmobilized(false);
			npc.setRunning();
			((Attackable) npc).addDamageHate(player, 0, 999);
			((Attackable) npc).getAI().setIntention(CtrlIntention.ATTACK, player);
		}
		return super.onAggroRangeEnter(npc, player, isSummon);
	}

	@Override
	public String onAttack(Npc npc, Player player, int damage, boolean isSummon)
	{
		if (npc instanceof MonsterInstance)
		{
			for (final int mobId : WALL_MONSTERS)
			{
				if (mobId == npc.getId())
				{
					final MonsterInstance monster = (MonsterInstance) npc;
					monster.setIsImmobilized(false);
					monster.setRunning();
					if (!monster.getFaction().isNone())
					{
						for (final var called : World.getAroundAttackable(monster, (int) (monster.getFaction().getRange() + monster.getColRadius()), 300))
						{
							if (called == null || (!called.getFaction().isNone() && !npc.isInFaction(called)))
							{
								continue;
							}
							
							final var ai = called.getAI();
							if (ai != null)
							{
								called.setIsImmobilized(false);
								called.addDamageHate(player, 0, 999);
							}
						}
					}
					break;
				}
			}
		}
		return super.onAttack(npc, player, damage, isSummon);
	}

	public static void main(String[] args)
	{
		new DragonGuards();
	}
}
