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

import java.util.HashMap;
import java.util.Map;

import gameserver.ThreadPoolManager;
import gameserver.ai.model.CtrlIntention;
import gameserver.model.Party;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.actor.templates.reflection.ReflectionTemplate;
import gameserver.model.entity.Reflection;
import gameserver.network.NpcStringId;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.ExShowScreenMessage;
import gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * Created by LordWinter 17.10.2020
 */
public final class RimPailaka726 extends AbstractReflection
{
	private final Map<Integer, Integer> _fortReflections = new HashMap<>(21);
	
	private RimPailaka726()
	{
		super(80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100);

		_fortReflections.put(35666, 80);
		_fortReflections.put(35698, 81);
		_fortReflections.put(35735, 82);
		_fortReflections.put(35767, 83);
		_fortReflections.put(35804, 84);
		_fortReflections.put(35835, 85);
		_fortReflections.put(35867, 86);
		_fortReflections.put(35904, 87);
		_fortReflections.put(35936, 88);
		_fortReflections.put(35974, 89);
		_fortReflections.put(36011, 90);
		_fortReflections.put(36043, 91);
		_fortReflections.put(36081, 92);
		_fortReflections.put(36118, 93);
		_fortReflections.put(36149, 94);
		_fortReflections.put(36181, 95);
		_fortReflections.put(36219, 96);
		_fortReflections.put(36257, 97);
		_fortReflections.put(36294, 98);
		_fortReflections.put(36326, 99);
		_fortReflections.put(36364, 100);
		
		for (final int i : _fortReflections.keySet())
		{
			addStartNpc(i);
			addTalkId(i);
		}
		addKillId(25661);
	}
	
	private synchronized void enterInstance(Player player, Npc npc, int reflectionId)
	{
		if (enterReflection(player, npc, reflectionId))
		{
			final var r = player.getReflection();
			if (isInReflection(r))
			{
				final long delay = r.getParams().getLong("firstWaveDelay");
				r.addTimer("FIRST_STAGE", ThreadPoolManager.getInstance().schedule(() -> firstStage(r), delay));
			}
		}
	}
	
	@Override
	protected boolean checkSoloType(Player player, Npc npc, ReflectionTemplate template)
	{
		final var fort = npc.getFort();
		final boolean checkConds = template.getParams().getBool("checkFortConditions");
		final var html = new NpcHtmlMessage(npc.getObjectId());
		if ((player == null) || (fort == null))
		{
			html.setFile(player, player.getLang(), "data/html/scripts/quests/_726_LightwithintheDarkness/FortWarden-01a.htm");
			player.sendPacket(html);
			return false;
		}
		if ((player.getClan() == null) || (player.getClan().getFortId() != fort.getId()))
		{
			html.setFile(player, player.getLang(), "data/html/scripts/quests/_726_LightwithintheDarkness/FortWarden-01a.htm");
			player.sendPacket(html);
			return false;
		}
		else if (fort.getFortState() == 0 && checkConds)
		{
			html.setFile(player, player.getLang(), "data/html/scripts/quests/_726_LightwithintheDarkness/FortWarden-07.htm");
			player.sendPacket(html);
			return false;
		}
		else if (fort.getFortState() == 2 && checkConds)
		{
			html.setFile(player, player.getLang(), "data/html/scripts/quests/_726_LightwithintheDarkness/FortWarden-08.htm");
			player.sendPacket(html);
			return false;
		}
		return super.checkSoloType(player, npc, template);
	}
	
	@Override
	protected boolean checkPartyType(Player player, Npc npc, ReflectionTemplate template)
	{
		final var fort = npc.getFort();
		final boolean checkConds = template.getParams().getBool("checkFortConditions");
		final var html = new NpcHtmlMessage(npc.getObjectId());
		
		if ((player == null) || (fort == null))
		{
			html.setFile(player, player.getLang(), "data/html/scripts/quests/_726_LightwithintheDarkness/FortWarden-01a.htm");
			player.sendPacket(html);
			return false;
		}
		
		final Party party = player.getParty();
		if (party == null || party.getMemberCount() < 2)
		{
			player.sendPacket(SystemMessageId.NOT_IN_PARTY_CANT_ENTER);
			return false;
		}
		
		if (party.getLeader() != player)
		{
			player.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
			return false;
		}
		
		if ((player.getClan() == null) || (player.getClan().getFortId() != fort.getId()))
		{
			html.setFile(player, player.getLang(), "data/html/scripts/quests/_726_LightwithintheDarkness/FortWarden-01a.htm");
			player.sendPacket(html);
			return false;
		}
		else if (fort.getFortState() == 0 && checkConds)
		{
			html.setFile(player, player.getLang(), "data/html/scripts/quests/_726_LightwithintheDarkness/FortWarden-07.htm");
			player.sendPacket(html);
			return false;
		}
		else if (fort.getFortState() == 2 && checkConds)
		{
			html.setFile(player, player.getLang(), "data/html/scripts/quests/_726_LightwithintheDarkness/FortWarden-08.htm");
			player.sendPacket(html);
			return false;
		}
		
		for (final var partyMember : party.getMembers())
		{
			if ((partyMember.getClan() == null) || (partyMember.getClan().getFortId() == 0) || (partyMember.getClan().getFortId() != fort.getId()))
			{
				html.setFile(player, player.getLang(), "data/html/scripts/quests/_726_LightwithintheDarkness/FortWarden-09.htm");
				html.replace("%player%", partyMember.getName(null));
				player.sendPacket(html);
				return false;
			}
		}
		return super.checkPartyType(player, npc, template);
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
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (event.equalsIgnoreCase("Enter"))
		{
			enterInstance(player, npc, _fortReflections.get(npc.getId()));
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isPet)
	{
		final var r = npc.getReflection();
		if (isInReflection(r))
		{
			switch (npc.getId())
			{
				case 25661 :
				{
					final var kills = r.getParams().getInteger("kills", 0) + 1;
					r.setParam("kills", kills);
					if (kills > 1)
					{
						final var party = player.getParty();
						if (party != null)
						{
							for (final var p : party.getMembers())
							{
								if (p != null && !p.isDead())
								{
									final var st = p.getQuestState("_726_LightwithintheDarkness");
									if (st != null && st.isCond(1) && p.isInsideRadius(npc, 1000, true, false))
									{
										st.setCond(2, true);
									}
								}
							}
						}
						else
						{
							final var st = player.getQuestState("_726_LightwithintheDarkness");
							if (st != null && st.isCond(1) && player.isInsideRadius(npc, 1000, true, false))
							{
								st.setCond(2, true);
							}
						}
						r.cancelTimer();
						r.cleanupNpcs();
						finishInstance(r, false);
					}
				}
			}
		}
		return super.onKill(npc, player, isPet);
	}
	
	private void firstStage(Reflection r)
	{
		if (r != null)
		{
			addSpawn(36562, 49384, -12232, -9384, 0, false, 0, false, r);
			addSpawn(36563, 49192, -12232, -9384, 0, false, 0, false, r);
			addSpawn(36564, 49192, -12456, -9392, 0, false, 0, false, r);
			addSpawn(36565, 49192, -11992, -9392, 0, false, 0, false, r);
			addSpawn(25659, 50536, -12232, -9384, 32768, false, 0, false, r);
			r.broadcastPacket(new ExShowScreenMessage(NpcStringId.BEGIN_STAGE_1, 2, 3000));
			for (int i = 0; i < 10; i++)
			{
				addSpawn(25662, 50536, -12232, -9384, 32768, false, 0, false, r);
			}
			final long delay = r.getParams().getLong("secondWaveDelay");
			r.addTimer("SECOND_STAGE", ThreadPoolManager.getInstance().schedule(() -> secondStage(r), delay));
		}
	}
	
	private void secondStage(Reflection r)
	{
		if (r != null)
		{
			r.broadcastPacket(new ExShowScreenMessage(NpcStringId.BEGIN_STAGE_2, 2, 3000));
			addSpawn(25660, 50536, -12232, -9384, 32768, false, 0, false, r);
			for (int i = 0; i < 10; i++)
			{
				addSpawn(25663, 50536, -12232, -9384, 32768, false, 0, false, r);
			}
			final long delay = r.getParams().getLong("thirdWaveDelay");
			r.addTimer("THIRD_STAGE", ThreadPoolManager.getInstance().schedule(() -> thirdStage(r), delay));
		}
	}
	
	private void thirdStage(Reflection r)
	{
		if (r != null)
		{
			r.broadcastPacket(new ExShowScreenMessage(NpcStringId.BEGIN_STAGE_3, 2, 3000));
			addSpawn(25661, 50536, -12232, -9384, 32768, false, 0, false, r);
			addSpawn(25661, 50536, -12232, -9384, 32768, false, 0, false, r);
			for (int i = 0; i < 10; i++)
			{
				addSpawn(25664, 50536, -12232, -9384, 32768, false, 0, false, r);
			}
		}
	}

	public static void main(String[] args)
	{
		new RimPailaka726();
	}
}
