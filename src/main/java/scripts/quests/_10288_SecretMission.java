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

import gameserver.instancemanager.QuestManager;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.quest.Quest;
import gameserver.model.quest.State;

/**
 * Rework by LordWinter 22.12.2019
 */
public class _10288_SecretMission extends Quest
{
	private _10288_SecretMission()
	{
		super(10288);
		
		addStartNpc(31350, 32780);
		addTalkId(31350, 32757, 32780);
		addFirstTalkId(32780);
		
		questItemIds = new int[]
		{
		        15529
		};
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final String htmltext = event;
		final var st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		if (npc.getId() == 31350)
		{
			if (event.equalsIgnoreCase("31350-05.htm"))
			{
				if (st.isCreated())
				{
					st.giveItems(15529, 1);
					st.startQuest();
				}
			}
		}
		else if ((npc.getId() == 32757) && event.equalsIgnoreCase("32757-03.htm"))
		{
			if (st.isCond(2))
			{
				st.takeItems(15529, -1);
				st.calcExpAndSp(getId());
				st.calcReward(getId());
				st.exitQuest(false, true);
			}
		}
		else if (npc.getId() == 32780)
		{
			if (st.getState() == State.STARTED)
			{
				if (event.equalsIgnoreCase("32780-05.html"))
				{
					st.setCond(2, true);
				}
			}
			else if ((st.getState() == State.COMPLETED) && event.equalsIgnoreCase("teleport"))
			{
				player.teleToLocation(118833, -80589, -2688, true, player.getReflection());
				return null;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		String htmltext = getNoQuestMsg(player);
		final var st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		if (npc.getId() == 31350)
		{
			switch (st.getState())
			{
				case State.CREATED:
					if (player.getLevel() >= getMinLvl(getId()))
					{
						htmltext = "31350-01.htm";
					}
					else
					{
						htmltext = "31350-00.htm";
					}
					break;
				case State.STARTED:
					if (st.isCond(1))
					{
						htmltext = "31350-06.htm";
					}
					else if (st.isCond(2))
					{
						htmltext = "31350-07.htm";
					}
					break;
				case State.COMPLETED:
					htmltext = "31350-08.htm";
					break;
			}
		}
		else if (npc.getId() == 32780)
		{
			if (st.isCond(1))
			{
				htmltext = "32780-03.html";
			}
			else if (st.isCond(2))
			{
				htmltext = "32780-06.html";
			}
		}
		else if ((npc.getId() == 32757) && (st.isCond(2)))
		{
			return "32757-01.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		var st = player.getQuestState(getName());
		if (st == null)
		{
			final var q = QuestManager.getInstance().getQuest(getName());
			st = q.newQuestState(player);
		}
		
		if (npc.getId() == 32780)
		{
			if (st.getState() == State.COMPLETED)
			{
				return "32780-01.html";
			}
			return "32780-00.html";
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _10288_SecretMission();
	}
}