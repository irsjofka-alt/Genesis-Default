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
import gameserver.network.NpcStringId;
import gameserver.network.clientpackets.Say2;
import gameserver.network.serverpackets.NpcSay;

/**
 * Rework by LordWinter 02.10.2020
 */
public class LibraryOfSages extends AbstractReflection
{
	private static final NpcStringId[] spam =
	{
	        NpcStringId.I_MUST_ASK_LIBRARIAN_SOPHIA_ABOUT_THE_BOOK, NpcStringId.THIS_LIBRARY_ITS_HUGE_BUT_THERE_ARENT_MANY_USEFUL_BOOKS_RIGHT, NpcStringId.AN_UNDERGROUND_LIBRARY_I_HATE_DAMP_AND_SMELLY_PLACES, NpcStringId.THE_BOOK_THAT_WE_SEEK_IS_CERTAINLY_HERE_SEARCH_INCH_BY_INCH
	};

	public LibraryOfSages()
	{
		super(156);

		addStartNpc(32861, 32596);
		addTalkId(32861, 32863, 32596, 32785);
	}
	
	private final synchronized void enterInstance(Player player, Npc npc)
	{
		if (enterReflection(player, npc, 156))
		{
			final var r = player.getReflection();
			if (isInReflection(r))
			{
				final var elcadia = addSpawn(32785, player.getX(), player.getY(), player.getZ(), 0, false, 0, false, r);
				startQuestTimer("check_follow", 3000, elcadia, player);
				r.setParam("elcadia", elcadia);
			}
		}
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

	private void teleportPlayer(Npc npc, Player player, Location loc, Reflection r)
	{
		player.teleToLocation(loc.getX(), loc.getY(), loc.getZ(), false, r);
		if (isInReflection(r))
		{
			final var elcadia = r.getParams().getObject("elcadia", Npc.class);
			if (elcadia != null)
			{
				cancelQuestTimer("check_follow", elcadia, player);
				elcadia.teleToLocation(player.getX(), player.getY(), player.getZ(), false, r);
				startQuestTimer("check_follow", 3000, elcadia, player);
			}
		}
	}

	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final String htmltext = getNoQuestMsg(player);
		var st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}

		if (event.equalsIgnoreCase("check_follow"))
		{
			npc.setIsRunning(true);
			npc.getAI().startFollow(player);
			npc.broadcastPacketToOthers(2000, new NpcSay(npc.getObjectId(), Say2.ALL, npc.getId(), spam[getRandom(0, spam.length - 1)]));
			startQuestTimer("check_follow", 10000, npc, player);
			return null;
		}
		else if (npc.getId() == 32596)
		{
			if (event.equalsIgnoreCase("tele1"))
			{
				enterInstance(player, npc);
				return null;
			}
		}
		else if (npc.getId() == 32861)
		{
			if (event.equalsIgnoreCase("tele2"))
			{
				teleportPlayer(player, new Location(37355, -50065, -1127), player.getReflection());
				return null;
			}
			else if (event.equalsIgnoreCase("tele3"))
			{
				final var r = npc.getReflection();
				if (isInReflection(r))
				{
					final var elcadia = r.getParams().getObject("elcadia", Npc.class);
					if (elcadia != null)
					{
						cancelQuestTimer("check_follow", elcadia, player);
						elcadia.deleteMe();
					}
					teleportPlayer(npc, player, new Location(37063, -49813, -1128), ReflectionManager.DEFAULT);
				}
				return null;
			}
		}
		else if (npc.getId() == 32863)
		{
			if (event.equalsIgnoreCase("tele4"))
			{
				teleportPlayer(npc, player, new Location(37063, -49813, -1128), player.getReflection());
				return null;
			}
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new LibraryOfSages();
	}
}