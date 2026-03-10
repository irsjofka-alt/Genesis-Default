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
 * Created by LordWinter 19.10.2020
 */
public final class Dungeon512 extends AbstractReflection
{
	private final Map<Integer, Integer> _castleReflections = new HashMap<>(9);
	
	private static final int[] RAIDS1 =
	{
	        25546, 25549, 25552
	};
	
	private static final int[] RAIDS2 =
	{
	        25553, 25554, 25557, 25560
	};
	
	private static final int[] RAIDS3 =
	{
	        25563, 25566, 25569
	};
	
	private Dungeon512()
	{
		super(13, 14, 15, 16, 17, 18, 19, 20, 21);

		_castleReflections.put(36403, 13);
		_castleReflections.put(36404, 14);
		_castleReflections.put(36405, 15);
		_castleReflections.put(36406, 16);
		_castleReflections.put(36407, 17);
		_castleReflections.put(36408, 18);
		_castleReflections.put(36409, 19);
		_castleReflections.put(36410, 20);
		_castleReflections.put(36411, 21);
		
		for (final int i : _castleReflections.keySet())
		{
			addStartNpc(i);
			addTalkId(i);
		}
		addKillId(25546, 25549, 25552, 25553, 25554, 25557, 25560, 25563, 25566, 25569);
	}
	
	private final synchronized void enterInstance(Player player, Npc npc, int reflectionId)
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
		final var castle = npc.getCastle();
		final var html = new NpcHtmlMessage(npc.getObjectId());
		
		if ((player == null) || (castle == null))
		{
			html.setFile(player, player.getLang(), "data/html/scripts/quests/_512_BladeUnderFoot/CastleWarden-01.htm");
			player.sendPacket(html);
			return false;
		}
		
		if ((player.getClan() == null) || (player.getClan().getCastleId() != castle.getId()))
		{
			html.setFile(player, player.getLang(), "data/html/scripts/quests/_512_BladeUnderFoot/CastleWarden-01.htm");
			player.sendPacket(html);
			return false;
		}
		
		final var st = player.getQuestState("_512_BladeUnderFoot");
		if ((st == null) || (st.getCond() < 1))
		{
			html.setFile(player, player.getLang(), "data/html/scripts/quests/_512_BladeUnderFoot/CastleWarden-02.htm");
			html.replace("%player%", player.getName(null));
			player.sendPacket(html);
			return false;
		}
		return super.checkSoloType(player, npc, template);
	}
	
	@Override
	protected boolean checkPartyType(Player player, Npc npc, ReflectionTemplate template)
	{
		final var castle = npc.getCastle();
		final var html = new NpcHtmlMessage(npc.getObjectId());
		if ((player == null) || (castle == null))
		{
			html.setFile(player, player.getLang(), "data/html/scripts/quests/_512_BladeUnderFoot/CastleWarden-01.htm");
			player.sendPacket(html);
			return false;
		}
		
		if ((player.getClan() == null) || (player.getClan().getCastleId() != castle.getId()))
		{
			html.setFile(player, player.getLang(), "data/html/scripts/quests/_512_BladeUnderFoot/CastleWarden-01.htm");
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
			final var st = partyMember.getQuestState("_512_BladeUnderFoot");
			if (st == null || st.getCond() < 1)
			{
				html.setFile(player, player.getLang(), "data/html/scripts/quests/_512_BladeUnderFoot/CastleWarden-02.htm");
				html.replace("%player%", partyMember.getName(null));
				player.sendPacket(html);
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
			enterInstance(player, npc, _castleReflections.get(npc.getId()));
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
				final int allowed = r.getReflectionPlayers().size();
				if (player.getParty() != null)
				{
					for (final var pl : player.getParty().getMembers())
					{
						if (pl != null && pl.getReflectionId() == r.getId())
						{
							final var st = pl.getQuestState("_512_BladeUnderFoot");
							if (st != null && st.isCond(1))
							{
								st.calcReward(512, allowed);
								st.playSound("ItemSound.quest_itemget");
							}
						}
					}
				}
				else
				{
					final var st = player.getQuestState("_512_BladeUnderFoot");
					if (st != null && st.isCond(1))
					{
						st.calcReward(512, allowed);
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
	
	public void stageRaid(Reflection r)
	{
		if (r != null)
		{
			int spawnId;
			switch (r.getStatus())
			{
				case 0 :
					spawnId = RAIDS1[getRandom(RAIDS1.length)];
					break;
				case 1 :
					spawnId = RAIDS2[getRandom(RAIDS2.length)];
					break;
				default :
					spawnId = RAIDS3[getRandom(RAIDS3.length)];
					break;
			}
			
			final var raid = addSpawn(spawnId, 12161, -49144, -3000, 0, false, 0, false, r);
			if (raid instanceof RaidBossInstance)
			{
				((RaidBossInstance) raid).setUseRaidCurse(false);
			}
		}
	}
	
	private void spawnStageRaid(Reflection r, long delay)
	{
		r.addTimer("STAGE_SPAWN", ThreadPoolManager.getInstance().schedule(() -> stageRaid(r), delay));
	}

	public static void main(String[] args)
	{
		new Dungeon512();
	}
}