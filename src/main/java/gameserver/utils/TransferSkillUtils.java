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
package gameserver.utils;

import gameserver.Config;
import gameserver.data.parser.ClassListParser;
import gameserver.data.parser.SkillTreesParser;
import gameserver.model.PcCondOverride;
import gameserver.model.SkillLearn;
import gameserver.model.actor.Player;
import gameserver.model.holders.ItemHolder;
import gameserver.model.quest.QuestState;
import gameserver.model.skills.Skill;
import gameserver.network.SystemMessageId;

public class TransferSkillUtils
{
	private static final ItemHolder[] _items =
	{
	        new ItemHolder(15307, 1), new ItemHolder(15308, 1), new ItemHolder(15309, 4)
	};
	
	public static void checkTransferItems(Player player)
	{
		final int index = getTransferClassIndex(player);
		if (index >= 0)
		{
			boolean oldSupport = false;
			final QuestState st = player.getQuestState("SkillTransfer");
			if (st != null && !st.getGlobalQuestVar("SkillTransfer").isEmpty())
			{
				oldSupport = true;
			}
			
			final String varName = "SkillTransfer" + String.valueOf(player.getClassId().getId());
			if (!player.getVarB(varName, false))
			{
				if (player.getWeightPenalty() >= 3 || !player.isInventoryUnder90(false))
				{
					player.sendPacket(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT);
					return;
				}
				
				player.setVar(varName, true);
				if (!oldSupport)
				{
					player.addItem("PORMANDERS", _items[index].getId(), _items[index].getCount(), null, true);
				}
			}
			
			if (Config.SECURITY_SKILL_CHECK && !player.canOverrideCond(PcCondOverride.SKILL_CONDITIONS))
			{
				long count = _items[index].getCount() - player.getInventory().getInventoryItemCount(_items[index].getId(), -1, false);
				for (final Skill sk : player.getAllSkills())
				{
					for (final SkillLearn s : SkillTreesParser.getInstance().getTransferSkillTree(player.getClassId()).values())
					{
						if (s.getId() == sk.getId())
						{
							if ((sk.getId() == 1043) && (index == 2) && player.isInStance())
							{
								continue;
							}
							
							count--;
							if (count < 0)
							{
								final String className = ClassListParser.getInstance().getClass(player.getClassId()).getClassName();
								Util.handleIllegalPlayerAction(player, "" + player.getName(null) + " has too many transfered skills or items, skill:" + s.getName() + " (" + sk.getId() + "/" + sk.getLevel() + "), class:" + className);
								if (Config.SECURITY_SKILL_CHECK_CLEAR)
								{
									player.removeSkill(sk);
								}
							}
						}
					}
				}
			}
		}
	}
	
	private static int getTransferClassIndex(Player player)
	{
		return switch (player.getClassId().getId())
		{
			case 97  -> 0;
			case 105  -> 1;
			case 112  -> 2;
			default  -> -1;
		};
	}
}