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
package gameserver.handler.bypasshandlers.impl;

import java.util.StringTokenizer;

import gameserver.handler.bypasshandlers.IBypassHandler;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.holders.SkillHolder;

public class ElcardiaBuff implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
	        "Request_Blessing"
	};
	
	private final int[][] BUFFS =
	{
	        {
	                6714, 6715, 6716, 6718, 6719, 6720, 6727, 6729
			},
			{
			        6714, 6717, 6720, 6721, 6722, 6723, 6727, 6729
			}
	};

	@Override
	public boolean useBypass(String command, Player activeChar, Creature target)
	{
		if (!target.isNpc())
		{
			return false;
		}

		final Npc npc = (Npc) target;
		final StringTokenizer st = new StringTokenizer(command);
		try
		{
			final String cmd = st.nextToken();

			if (cmd.equalsIgnoreCase(COMMANDS[0]))
			{
				for (final int skillId : BUFFS[activeChar.isMageClass() ? 1 : 0])
				{
					final SkillHolder skill = new SkillHolder(skillId, 1);

					if (skill.getSkill() != null)
					{
						npc.setTarget(activeChar);
						npc.doCast(skill.getSkill());
					}
				}
				return true;
			}
		}
		catch (final Exception e)
		{
			_log.warn("Exception in " + getClass().getSimpleName(), e);
		}
		return false;
	}

	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}