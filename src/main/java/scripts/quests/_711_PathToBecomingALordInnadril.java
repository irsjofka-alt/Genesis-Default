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
import gameserver.model.quest.QuestState;
import gameserver.model.quest.State;
import gameserver.network.NpcStringId;
import gameserver.network.clientpackets.Say2;
import gameserver.network.serverpackets.NpcSay;

/**
 * Created by LordWinter
 */
public class _711_PathToBecomingALordInnadril extends Quest
{
	private static final int Neurath = 35316;
	private static final int IasonHeine = 30969;
	
	private static final int[] mobs =
	{
		20789,
		20790,
		20791,
		20792,
		20793,
		20804,
		20805,
		20806,
		20807,
		20808
	};
	
	private _711_PathToBecomingALordInnadril()
	{
		super(711);
		
		addStartNpc(Neurath);
		addTalkId(Neurath);
		addTalkId(IasonHeine);
		
		addKillId(mobs);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		final var st = player.getQuestState(getName());
		final var castle = CastleManager.getInstance().getCastleById(6);
		final var castleOwner = castle.getOwner().getLeader().getPlayerInstance();
		if (event.equals("neurath_q711_03.htm"))
		{
			st.startQuest();
		}
		else if (event.equals("neurath_q711_05.htm"))
		{
			st.setCond(2);
		}
		else if (event.equals("neurath_q711_08.htm"))
		{
			if (isLordAvailable(2, st))
			{
				castleOwner.getQuestState(getName()).set("confidant", String.valueOf(player.getObjectId()));
				castleOwner.getQuestState(getName()).setCond(3);
				st.setState(State.STARTED);
			}
			else
			{
				htmltext = "neurath_q711_07a.htm";
			}
			
		}
		else if (event.equals("heine_q711_03.htm"))
		{
			if (isLordAvailable(3, st))
			{
				castleOwner.getQuestState(getName()).setCond(4);
			}
			else
			{
				htmltext = "heine_q711_00a.htm";
			}
		}
		else if (event.equals("neurath_q711_12.htm"))
		{
			if (castle.getSiege().getIsInProgress() || TerritoryWarManager.getInstance().isTWInProgress())
			{
				return "neurath_q711_12a.htm";
			}
			
			final var fort = FortManager.getInstance().getFort(108);
			if (fort != null)
			{
				if (fort.getSiege().getIsInProgress())
				{
					return "neurath_q711_12a.htm";
				}
				else if (fort.getContractedCastleId() != 6)
				{
					return "neurath_q711_12b.htm";
				}
			}
			
			if (castleOwner != null)
			{
				final var packet = new NpcSay(npc.getObjectId(), Say2.NPC_SHOUT, npc.getId(), NpcStringId.S1_HAS_BECOME_THE_LORD_OF_THE_TOWN_OF_INNADRIL_MAY_THERE_BE_GLORY_IN_THE_TERRITORY_OF_INNADRIL);
				packet.addStringParameter(player.getName(null));
				npc.broadcastPacketToOthers(2000, packet);
				castle.getTerritory().changeOwner(castle.getOwner());
				st.exitQuest(true, true);
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		final var st = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		final var castle = CastleManager.getInstance().getCastleById(6);
		if (castle.getOwner() == null)
		{
			return "Castle has no lord";
		}
		
		final var castleOwner = castle.getOwner().getLeader().getPlayerInstance();
		switch (npc.getId())
		{
			case Neurath:
			{
				if (st.isCond(0))
				{
					if (castleOwner == player)
					{
						if (castle.getTerritory().getLordObjectId() != player.getObjectId())
						{
							htmltext = "neurath_q711_01.htm";
						}
						else
						{
							htmltext = "neurath_q711_00.htm";
							st.exitQuest(true);
						}
					}
					else if (isLordAvailable(2, st))
					{
						if (castleOwner.calculateDistance(npc, false, false) <= 200)
						{
							htmltext = "neurath_q711_07.htm";
						}
						else
						{
							htmltext = "neurath_q711_07a.htm";
						}
					}
					else if (st.getState() == State.STARTED)
					{
						htmltext = "neurath_q711_00b.htm";
					}
					else
					{
						htmltext = "neurath_q711_00a.htm";
						st.exitQuest(true);
					}
				}
				else if (st.isCond(1))
				{
					htmltext = "neurath_q711_04.htm";
				}
				else if (st.isCond(2))
				{
					htmltext = "neurath_q711_06.htm";
				}
				else if (st.isCond(3))
				{
					htmltext = "neurath_q711_09.htm";
				}
				else if (st.isCond(4))
				{
					st.setCond(5);
					htmltext = "neurath_q711_10.htm";
				}
				else if (st.isCond(5))
				{
					htmltext = "neurath_q711_10.htm";
				}
				else if (st.isCond(6))
				{
					htmltext = "neurath_q711_11.htm";
				}
				break;
			}
			case IasonHeine:
			{
				if ((st.getState() == State.STARTED) && st.isCond(0))
				{
					if (isLordAvailable(3, st))
					{
						if (castleOwner.getQuestState(getName()).getInt("confidant") == player.getObjectId())
						{
							htmltext = "heine_q711_01.htm";
						}
						else
						{
							htmltext = "heine_q711_00.htm";
						}
					}
					else if (isLordAvailable(4, st))
					{
						if (castleOwner.getQuestState(getName()).getInt("confidant") == player.getObjectId())
						{
							htmltext = "heine_q711_03.htm";
						}
						else
						{
							htmltext = "heine_q711_00.htm";
						}
					}
					else
					{
						htmltext = "heine_q711_00a.htm";
					}
				}
				break;
			}
		}
		
		return htmltext;
	}
	
	@Override
	public final String onKill(Npc npc, Player player, boolean isPet)
	{
		final var st = player.getQuestState(getName());
		if ((st != null) && st.isCond(5))
		{
			if (st.getInt("mobs") < 99)
			{
				st.set("mobs", String.valueOf(st.getInt("mobs") + 1));
			}
			else
			{
				st.setCond(6);
			}
		}
		return null;
	}
	
	private boolean isLordAvailable(int cond, QuestState qs)
	{
		final var castle = CastleManager.getInstance().getCastleById(6);
		final var owner = castle.getOwner();
		final var castleOwner = castle.getOwner().getLeader().getPlayerInstance();
		if (owner != null)
		{
			if ((castleOwner != null) && (castleOwner != qs.getPlayer()) && (owner == qs.getPlayer().getClan()) && (castleOwner.getQuestState(getName()) != null) && (castleOwner.getQuestState(getName()).isCond(cond)))
			{
				return true;
			}
		}
		return false;
	}
	
	public static void main(String[] args)
	{
		new _711_PathToBecomingALordInnadril();
	}
}