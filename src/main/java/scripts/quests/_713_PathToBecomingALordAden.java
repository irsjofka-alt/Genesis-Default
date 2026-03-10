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
public class _713_PathToBecomingALordAden extends Quest
{
	// NPCs
	private static final int LOGAN = 35274;
	private static final int ORVEN = 30857;
	
	// Monsters
	private static final int TAIK_SEEKER = 20666;
	private static final int TAIK_LEADER = 20669;
	
	private static final int REQUIRED_CLAN_MEMBERS = 5;
	
	private _713_PathToBecomingALordAden()
	{
		super(713);
		
		addStartNpc(LOGAN);
		addTalkId(LOGAN, ORVEN);
		
		addKillId(TAIK_SEEKER, TAIK_LEADER);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final var qs = getQuestState(player, false);
		String htmltext = null;
		if (qs == null)
		{
			return htmltext;
		}
		
		final var castle = CastleManager.getInstance().getCastleById(5);
		switch (event)
		{
			case "30857-02.htm" :
			{
				htmltext = event;
				break;
			}
			case "35274-03.htm":
			{
				if (qs.isCreated())
				{
					qs.startQuest();
					htmltext = event;
				}
				break;
			}
			case "30857-03.htm" :
			{
				if (qs.isCond(1))
				{
					qs.setMemoState(0);
					qs.setCond(2);
					htmltext = event;
				}
				break;
			}
			case "35274-06.htm" :
			{
				if (qs.isCond(7))
				{
					if (castle.getSiege().getIsInProgress() || TerritoryWarManager.getInstance().isTWInProgress())
					{
						return "35274-05a.htm";
					}
					
					for (final var fort : FortManager.getInstance().getFortsById(106, 107))
					{
						if (fort != null)
						{
							if (fort.getSiege().getIsInProgress())
							{
								return "35274-05a.htm";
							}
							else if (fort.getContractedCastleId() != 5)
							{
								return "35274-05b.htm";
							}
						}
					}
					final var packet = new NpcSay(npc.getObjectId(), Say2.NPC_SHOUT, npc.getId(), NpcStringId.S1_HAS_BECOME_THE_LORD_OF_THE_TOWN_OF_ADEN_MAY_THERE_BE_GLORY_IN_THE_TERRITORY_OF_ADEN);
					packet.addStringParameter(player.getName(null));
					npc.broadcastPacketToOthers(2000, packet);
					castle.getTerritory().changeOwner(castle.getOwner());
					qs.exitQuest(true, true);
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
		final var qs = getQuestState(talker, true);
		String htmltext = getNoQuestMsg(talker);
		final var castle = CastleManager.getInstance().getCastleById(5);
		if (castle.getOwner() == null)
		{
			return "Castle has no lord";
		}
		
		switch (npc.getId())
		{
			case LOGAN:
			{
				switch (qs.getState())
				{
					case State.CREATED:
					{
						htmltext = (talker.getObjectId() == talker.getClan().getLeaderId() && castle.getTerritory().getLordObjectId() != talker.getObjectId()) ? "35274-01.htm" : "35274-02.htm";
						break;
					}
					case State.STARTED:
					{
						switch (qs.getCond())
						{
							case 1:
							case 2:
							case 3:
							case 5:
							{
								htmltext = "35274-04.htm";
								break;
							}
							case 7:
							{
								htmltext = "35274-05.htm";
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
				break;
			}
			case ORVEN:
			{
				switch (qs.getCond())
				{
					case 1:
					{
						htmltext = "30857-01.htm";
						break;
					}
					case 2:
					{
						htmltext = "30857-04.htm";
						break;
					}
					case 5:
					{
						int clanMemberCount = 0;
						for (final var clanMember : talker.getClan().getMembers())
						{
							final var member = clanMember.getPlayerInstance();
							if ((member != null) && member.isOnline() && (member.getObjectId() != talker.getObjectId()))
							{
								var st = getQuestState(member, false);
								st = member.getQuestState(_359_ForSleeplessDeadmen.class.getSimpleName());
								if (st != null && st.isCompleted())
								{
									clanMemberCount++;
								}
							}
						}
						if (clanMemberCount >= REQUIRED_CLAN_MEMBERS)
						{
							qs.setCond(7);
							htmltext = "30857-06.htm";
						}
						else
						{
							htmltext = "30857-05.htm";
						}
						break;
					}
					case 7:
					{
						htmltext = "30857-07.htm";
						break;
					}
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final var qs = killer.getQuestState(getName());
		if ((qs != null) && qs.isCond(2))
		{
			if (qs.getMemoState() < 100)
			{
				qs.setMemoState(qs.getMemoState() + 1);
			}
			else
			{
				qs.setCond(5);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new _713_PathToBecomingALordAden();
	}
}