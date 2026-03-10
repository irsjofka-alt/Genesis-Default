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
package scripts.instances;

import gameserver.ai.model.CtrlIntention;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.actor.templates.reflection.ReflectionTemplate;
import gameserver.model.entity.Reflection;

/**
 * Rework by LordWinter 02.10.2020
 */
public final class JiniaGuildHideout2 extends AbstractReflection
{
	private JiniaGuildHideout2()
	{
		super(141);

		addStartNpc(32020);
		addTalkId(32020);
	}
	
	private final synchronized void enterInstance(Player player, Npc npc)
	{
		enterReflection(player, npc, 141);
	}
	
	@Override
	protected void onTeleportEnter(Player player, ReflectionTemplate template, Reflection r, boolean firstEntrance)
	{
		if (firstEntrance)
		{
			r.addAllowed(player);
			player.getAI().setIntention(CtrlIntention.IDLE);
			final var teleLoc = template.getTeleportCoord();
			player.teleToLocation(teleLoc, true, r);
			if (player.hasSummon())
			{
				player.getSummon().getAI().setIntention(CtrlIntention.IDLE);
				player.getSummon().teleToLocation(teleLoc, true, r);
			}
		}
		else
		{
			player.getAI().setIntention(CtrlIntention.IDLE);
			final var teleLoc = template.getTeleportCoord();
			player.teleToLocation(teleLoc, true, r);
			if (player.hasSummon())
			{
				player.getSummon().getAI().setIntention(CtrlIntention.IDLE);
				player.getSummon().teleToLocation(teleLoc, true, r);
			}
		}
	}

	@Override
	public String onTalk(Npc npc, Player talker)
	{
		final var qs = talker.getQuestState("_10285_MeetingSirra");
		if ((qs != null) && qs.isMemoState(1))
		{
			enterInstance(talker, npc);
			if (qs.getCond() < 2)
			{
				qs.setCond(2, true);
			}
		}
		return super.onTalk(npc, talker);
	}


	public static void main(String[] args)
	{
		new JiniaGuildHideout2();
	}
}
