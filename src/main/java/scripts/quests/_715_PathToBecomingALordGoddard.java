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
package scripts.quests;

import gameserver.instancemanager.CastleManager;
import gameserver.instancemanager.FortManager;
import gameserver.instancemanager.TerritoryWarManager;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.quest.Quest;
import gameserver.model.quest.State;
import gameserver.network.NpcStringId;
import gameserver.network.clientpackets.Say2;
import gameserver.network.serverpackets.NpcSay;

/**
 * Created by LordWinter
 */
public final class _715_PathToBecomingALordGoddard extends Quest
{
	// NPC
	private static final int ALFRED = 35363;
	
	// Monsters
	private static final int WATER_SPIRIT = 25316;
	private static final int FLAME_SPIRIT = 25306;
	
	private _715_PathToBecomingALordGoddard()
	{
		super(715);
		
		addStartNpc(ALFRED);
		addTalkId(ALFRED);
		
		addKillId(WATER_SPIRIT, FLAME_SPIRIT);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final var st = getQuestState(player, false);
		String htmltext = null;
		if (st == null)
		{
			return htmltext;
		}
		
		final var castle = CastleManager.getInstance().getCastleById(7);
		switch (event)
		{
			case "35363-02.htm" :
			{
				htmltext = event;
				break;
			}
			case "35363-04a.htm" :
			{
				if (st.isCreated())
				{
					st.startQuest();
					htmltext = event;
				}
				break;
			}
			case "35363-05.htm" :
			{
				if (st.isCond(1))
				{
					st.setCond(2);
					htmltext = event;
				}
				break;
			}
			case "35363-06.htm" :
			{
				if (st.isCond(1))
				{
					st.setCond(3);
					htmltext = event;
				}
				break;
			}
			case "35363-12.htm" :
			{
				if (st.isCond(7))
				{
					if (castle.getSiege().getIsInProgress() || TerritoryWarManager.getInstance().isTWInProgress())
					{
						return "35363-11a.htm";
					}
					
					final var fort = FortManager.getInstance().getFort(109);
					if (fort != null)
					{
						if (fort.getSiege().getIsInProgress())
						{
							return "35363-11a.htm";
						}
						else if (fort.getContractedCastleId() != 7)
						{
							return "35363-11b.htm";
						}
					}
					final var packet = new NpcSay(npc.getObjectId(), Say2.NPC_SHOUT, npc.getId(), NpcStringId.S1_HAS_BECOME_THE_LORD_OF_THE_TOWN_OF_GODDARD_MAY_THERE_BE_GLORY_IN_THE_TERRITORY_OF_GODDARD);
					packet.addStringParameter(player.getName(null));
					npc.broadcastPacketToOthers(2000, packet);
					castle.getTerritory().changeOwner(castle.getOwner());
					st.exitQuest(true, true);
					htmltext = event;
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player talker)
	{
		final var st = getQuestState(talker, true);
		String htmltext = getNoQuestMsg(talker);
		final var castle = CastleManager.getInstance().getCastleById(7);
		if (castle.getOwner() == null)
		{
			return "Castle has no lord";
		}
		
		switch (st.getState())
		{
			case State.CREATED:
			{
				htmltext = (talker.getObjectId() == talker.getClan().getLeaderId() && castle.getTerritory().getLordObjectId() != talker.getObjectId()) ? "35363-01.htm" : "35363-03.htm";
				break;
			}
			case State.STARTED:
			{
				switch (st.getCond())
				{
					case 1:
					{
						htmltext = "35363-04a.htm";
						break;
					}
					case 2:
					{
						htmltext = "35363-07.htm";
						break;
					}
					case 3:
					{
						htmltext = "35363-08.htm";
						break;
					}
					case 4:
					{
						htmltext = "35363-09.htm";
						st.setCond(6);
						break;
					}
					case 5:
					{
						htmltext = "35363-10.htm";
						st.setCond(7);
						break;
					}
					case 6:
					{
						htmltext = "35363-09.htm";
						break;
					}
					case 7:
					{
						htmltext = "35363-10.htm";
						break;
					}
					case 8:
					case 9:
					{
						htmltext = "35363-11.htm";
						break;
					}
				}
			}
			case State.COMPLETED:
			{
				htmltext = getAlreadyCompletedMsg(talker);
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		if (killer.getClan() == null)
		{
			return super.onKill(npc, killer, isSummon);
		}
		
		final var leader = killer.getClan().getLeader().getPlayerInstance();
		if (leader == null || !leader.isOnline())
		{
			return super.onKill(npc, killer, isSummon);
		}
		
		final var st = leader.getQuestState(getName());
		if (st == null)
		{
			return super.onKill(npc, killer, isSummon);
		}
		
		switch (st.getCond())
		{
			case 2:
			{
				if (npc.getId() == FLAME_SPIRIT)
				{
					st.setCond(4);
				}
				break;
			}
			case 3:
			{
				if (npc.getId() == WATER_SPIRIT)
				{
					st.setCond(5);
				}
				break;
			}
			case 6:
			{
				if (npc.getId() == WATER_SPIRIT)
				{
					st.setCond(9);
				}
				break;
			}
			case 7:
			{
				if (npc.getId() == FLAME_SPIRIT)
				{
					st.setCond(8);
				}
				break;
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new _715_PathToBecomingALordGoddard();
	}
}