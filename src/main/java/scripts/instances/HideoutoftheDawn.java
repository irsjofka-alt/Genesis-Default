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
import gameserver.instancemanager.ReflectionManager;
import gameserver.model.Location;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.actor.templates.reflection.ReflectionTemplate;
import gameserver.model.entity.Reflection;

/**
 * Rework by LordWinter 02.10.2020
 */
public class HideoutoftheDawn extends AbstractReflection
{
	public HideoutoftheDawn()
	{
		super(113);

		addStartNpc(32593, 32617);
		addTalkId(32593, 32617);
	}
	
	private final synchronized void enterInstance(Player player, Npc npc)
	{
		enterReflection(player, npc, 113);
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
	public String onTalk(Npc npc, Player player)
	{
		final String htmltext = getNoQuestMsg(player);
		var st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}

		if (npc.getId() == 32593)
		{
			enterInstance(player, npc);
			return null;
		}
		else if (npc.getId() == 32617)
		{
			final var r = player.getReflection();
			if (isInReflection(r))
			{
				r.removeAllowed(player);
			}
			player.teleToLocation(new Location(147072, 23743, -1984, 0), true, ReflectionManager.DEFAULT);
			return null;
		}
		return htmltext;
	}

	void main()
	{
		new HideoutoftheDawn();
	}
}