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

import org.apache.commons.lang3.ArrayUtils;

import gameserver.ai.model.CtrlIntention;
import gameserver.model.actor.Attackable;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.utils.Util;
import scripts.quests._605_AllianceWithKetraOrcs;
import scripts.quests._606_WarWithVarkaSilenos;
import scripts.quests._607_ProveYourCourage;
import scripts.quests._608_SlayTheEnemyCommander;
import scripts.quests._609_MagicalPowerOfWaterPart1;
import scripts.quests._610_MagicalPowerOfWaterPart2;
import scripts.quests._611_AllianceWithVarkaSilenos;
import scripts.quests._612_WarWithKetraOrcs;
import scripts.quests._613_ProveYourCourage;
import scripts.quests._614_SlayTheEnemyCommander;
import scripts.quests._615_MagicalPowerOfFirePart1;
import scripts.quests._616_MagicalPowerOfFirePart2;

public class VarkaKetra extends AbstractNpcAI
{
	private static final int[] KETRA =
	{
	        21324, 21325, 21327, 21328, 21329, 21331, 21332, 21334, 21336, 21338, 21339, 21340, 21342, 21343, 21344, 21345, 21346, 21347, 21348, 21349, 25299, 25302, 25305, 25306
	};
	
	private static final int[] VARKA =
	{
	        21350, 21351, 21353, 21354, 21355, 21357, 21358, 21360, 21361, 21362, 21364, 21365, 21366, 21368, 21369, 21370, 21371, 21372, 21373, 21374, 21375, 25309, 25312, 25315, 25316
	};
	
	private static final int[] KETRA_MARKS =
	{
	        7211, 7212, 7213, 7214, 7215
	};
	
	private static final int[] VARKA_MARKS =
	{
	        7221, 7222, 7223, 7224, 7225
	};
	
	private static final String[] KETRA_QUESTS =
	{
	        _605_AllianceWithKetraOrcs.class.getSimpleName(), _606_WarWithVarkaSilenos.class.getSimpleName(), _607_ProveYourCourage.class.getSimpleName(), _608_SlayTheEnemyCommander.class.getSimpleName(), _609_MagicalPowerOfWaterPart1.class.getSimpleName(), _610_MagicalPowerOfWaterPart2.class.getSimpleName()
	};
	
	private static final String[] VARKA_QUESTS =
	{
	        _611_AllianceWithVarkaSilenos.class.getSimpleName(), _612_WarWithKetraOrcs.class.getSimpleName(), _613_ProveYourCourage.class.getSimpleName(), _614_SlayTheEnemyCommander.class.getSimpleName(), _615_MagicalPowerOfFirePart1.class.getSimpleName(), _616_MagicalPowerOfFirePart2.class.getSimpleName()
	};
	
	private VarkaKetra()
	{
		addAggroRangeEnterId(KETRA);
		addAggroRangeEnterId(VARKA);
		addKillId(KETRA);
		addKillId(VARKA);
	}
	
	@Override
	public void actionForEachPlayer(Player player, Npc npc, boolean isSummon)
	{
		if (Util.checkIfInRange(1500, player, npc, false))
		{
			if (ArrayUtils.contains(KETRA, npc.getId()) && hasAtLeastOneQuestItem(player, KETRA_MARKS))
			{
				decreaseAlliance(player, KETRA_MARKS);
				exitQuests(player, KETRA_QUESTS);
			}
			else if (ArrayUtils.contains(VARKA, npc.getId()) && hasAtLeastOneQuestItem(player, VARKA_MARKS))
			{
				decreaseAlliance(player, VARKA_MARKS);
				exitQuests(player, VARKA_QUESTS);
			}
		}
	}
	
	private void decreaseAlliance(Player player, int[] marks)
	{
		for (int i = 0; i < marks.length; i++)
		{
			if (hasQuestItems(player, marks[i]))
			{
				takeItems(player, marks[i], -1);
				if (i > 0)
				{
					giveItems(player, marks[i - 1], 1);
				}
				return;
			}
		}
	}
	
	private void exitQuests(Player player, String[] quests)
	{
		for (final var quest : quests)
		{
			final var qs = player.getQuestState(quest);
			if ((qs != null) && qs.isStarted())
			{
				qs.exitQuest(true);
			}
		}
	}
	
	@Override
	public String onAggroRangeEnter(Npc npc, Player player, boolean isSummon)
	{
		if ((ArrayUtils.contains(KETRA, npc.getId()) && hasAtLeastOneQuestItem(player, KETRA_MARKS)) || (ArrayUtils.contains(VARKA, npc.getId()) && hasAtLeastOneQuestItem(player, VARKA_MARKS)))
		{
			final var attackable = (Attackable) npc;
			if (attackable.containsTarget(player))
			{
				attackable.getAggroList().stopHating(player);
				attackable.getAI().setIntention(CtrlIntention.ACTIVE);
			}
		}
		return super.onAggroRangeEnter(npc, player, isSummon);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		executeForEachPlayer(killer, npc, isSummon, true, false);
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new VarkaKetra();
	}
}
