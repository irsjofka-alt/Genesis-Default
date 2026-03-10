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
package scripts.custom;

import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import scripts.ai.AbstractNpcAI;

public final class FreyasSteward extends AbstractNpcAI
{
	private FreyasSteward()
	{
		addStartNpc(32029);
		addFirstTalkId(32029);
		addTalkId(32029);
	}

	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return "32029.htm";
	}

	@Override
	public String onTalk(Npc npc, Player player)
	{
		if (player.getLevel() >= 82)
		{
			player.teleToLocation(103045, -124361, -2768, true, player.getReflection());
			return null;
		}
		return "32029-1.htm";
	}

	public static void main(String[] args)
	{
		new FreyasSteward();
	}
}
