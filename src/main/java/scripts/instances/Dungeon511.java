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

import org.apache.commons.lang3.ArrayUtils;

import gameserver.ThreadPoolManager;
import gameserver.ai.model.CtrlIntention;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.actor.instance.RaidBossInstance;
import gameserver.model.actor.templates.reflection.ReflectionTemplate;
import gameserver.model.entity.Reflection;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * Created by LordWinter 18.10.2020
 */
public final class Dungeon511 extends AbstractReflection
{
	private final Map<Integer, Integer> _fortReflections = new HashMap<>(21);
	
	private static final int[] RAIDS1 =
	{
	        25572, 25575, 25578
	};
	
	private static final int[] RAIDS2 =
	{
	        25579, 25582, 25585, 25588
	};
	
	private static final int[] RAIDS3 =
	{
	        25589, 25592, 25593
	};
	
	private Dungeon511()
	{
		super(22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42);

		_fortReflections.put(35666, 22);
		_fortReflections.put(35698, 23);
		_fortReflections.put(35735, 24);
		_fortReflections.put(35767, 25);
		_fortReflections.put(35804, 26);
		_fortReflections.put(35835, 27);
		_fortReflections.put(35867, 28);
		_fortReflections.put(35904, 29);
		_fortReflections.put(35936, 30);
		_fortReflections.put(35974, 31);
		_fortReflections.put(36011, 32);
		_fortReflections.put(36043, 33);
		_fortReflections.put(36081, 34);
		_fortReflections.put(36118, 35);
		_fortReflections.put(36149, 36);
		_fortReflections.put(36181, 37);
		_fortReflections.put(36219, 38);
		_fortReflections.put(36257, 39);
		_fortReflections.put(36294, 40);
		_fortReflections.put(36326, 41);
		_fortReflections.put(36364, 42);
		
		for (final int i : _fortReflections.keySet())
		{
			addStartNpc(i);
			addTalkId(i);
		}
		addKillId(25572, 25575, 25578, 25579, 25582, 25585, 25588, 25589, 25592, 25593);
	}
	
	private synchronized void enterInstance(Player player, Npc npc, int reflectionId)
	{
		if (enterReflection(player, npc, reflectionId))
		{
			final var r = player.getReflection();
			if (isInReflection(r))
			{
				spawnStageRaid(r, r.getParams().getLong("stageSpawnDelay"));
			}
		}
	}
	
	@Override
	protected boolean checkSoloType(Player player, Npc npc, ReflectionTemplate template)
	{
		final var fortress = npc.getFort();
		final boolean checkConds = template.getParams().getBool("checkFortConditions");
		final var html = new NpcHtmlMessage(npc.getObjectId());
		if ((player == null) || (fortress == null))
		{
			html.setFile(player, player.getLang(), "data/html/scripts/quests/_511_AwlUnderFoot/FortressWarden-01.htm");
			player.sendPacket(html);
			return false;
		}
		
		if ((player.getClan() == null) || (player.getClan().getFortId() != fortress.getId()))
		{
			html.setFile(player, player.getLang(), "data/html/scripts/quests/_511_AwlUnderFoot/FortressWarden-01.htm");
			player.sendPacket(html);
			return false;
		}
		else if (fortress.getFortState() == 0 && checkConds)
		{
			html.setFile(player, player.getLang(), "data/html/scripts/quests/_511_AwlUnderFoot/FortressWarden-02.htm");
			player.sendPacket(html);
			return false;
		}
		else if (fortress.getFortState() == 2 && checkConds)
		{
			html.setFile(player, player.getLang(), "data/html/scripts/quests/_511_AwlUnderFoot/FortressWarden-03.htm");
			player.sendPacket(html);
			return false;
		}
		
		final var st = player.getQuestState("_511_AwlUnderFoot");
		if ((st == null) || (st.getCond() < 1))
		{
			html.setFile(player, player.getLang(), "data/html/scripts/quests/_511_AwlUnderFoot/FortressWarden-04.htm");
			html.replace("%player%", player.getName(null));
			player.sendPacket(html);
			return false;
		}
		return super.checkSoloType(player, npc, template);
	}
	
	@Override
	protected boolean checkPartyType(Player player, Npc npc, ReflectionTemplate template)
	{
		final var fortress = npc.getFort();
		final boolean checkConds = template.getParams().getBool("checkFortConditions");
		final var html = new NpcHtmlMessage(npc.getObjectId());
		if ((player == null) || (fortress == null))
		{
			html.setFile(player, player.getLang(), "data/html/scripts/quests/_511_AwlUnderFoot/FortressWarden-01.htm");
			player.sendPacket(html);
			return false;
		}
		
		if ((player.getClan() == null) || (player.getClan().getFortId() != fortress.getId()))
		{
			html.setFile(player, player.getLang(), "data/html/scripts/quests/_511_AwlUnderFoot/FortressWarden-01.htm");
			player.sendPacket(html);
			return false;
		}
		else if (fortress.getFortState() == 0 && checkConds)
		{
			html.setFile(player, player.getLang(), "data/html/scripts/quests/_511_AwlUnderFoot/FortressWarden-02.htm");
			player.sendPacket(html);
			return false;
		}
		else if (fortress.getFortState() == 2 && checkConds)
		{
			html.setFile(player, player.getLang(), "data/html/scripts/quests/_511_AwlUnderFoot/FortressWarden-03.htm");
			player.sendPacket(html);
			return false;
		}
		
		final var party = player.getParty();
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
		
		for (final var partyMember : party.getMembers())
		{
			final var st = partyMember.getQuestState("_511_AwlUnderFoot");
			if ((st == null || st.getCond() < 1) || (partyMember.getClan() == null) || (partyMember.getClan().getFortId() == 0) || (partyMember.getClan().getFortId() != fortress.getId()))
			{
				html.setFile(player, player.getLang(), "data/html/scripts/quests/_511_AwlUnderFoot/FortressWarden-04.htm");
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
			if (ArrayUtils.contains(RAIDS3, npc.getId()))
			{
				if (player.getParty() != null)
				{
					for (final var pl : player.getParty().getMembers())
					{
						if (pl != null && pl.getReflectionId() == r.getId())
						{
							final var st = pl.getQuestState("_511_AwlUnderFoot");
							if (st != null && st.isCond(1))
							{
								st.calcReward(511);
								st.playSound("ItemSound.quest_itemget");
							}
						}
					}
				}
				else
				{
					final var st = player.getQuestState("_511_AwlUnderFoot");
					if (st != null && st.isCond(1))
					{
						st.calcReward(511);
						st.playSound("ItemSound.quest_itemget");
					}
				}
				finishInstance(r, false);
			}
			else
			{
				r.incStatus();
				spawnStageRaid(r, r.getParams().getLong("stageSpawnDelay"));
			}
		}
		return super.onKill(npc, player, isPet);
	}
	
	private void stageRaid(Reflection r)
	{
		if (r != null)
		{
			int spawnId = switch (r.getStatus())
			{
				case 0  -> RAIDS1[getRandom(RAIDS1.length)];
				case 1  -> RAIDS2[getRandom(RAIDS2.length)];
				default  -> RAIDS3[getRandom(RAIDS3.length)];
			};
			
			final var raid = addSpawn(spawnId, 53319, 245814, -6576, 0, false, 0, false, r);
			if (raid instanceof RaidBossInstance instance)
			{
				instance.setUseRaidCurse(false);
			}
		}
	}
	
	private void spawnStageRaid(Reflection r, long delay)
	{
		r.addTimer("STAGE_SPAWN", ThreadPoolManager.getInstance().schedule(() -> stageRaid(r), delay));
	}

	public static void main(String[] args)
	{
		new Dungeon511();
	}
}