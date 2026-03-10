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

import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.quest.Quest;
import gameserver.model.quest.State;

/**
 * Based on L2J Eternity-World
 */
public class _250_WatchWhatYouEat extends Quest
{
	private static final int[][] _mobs =
	{
	        {
	                18864, 15493
			},
	        {
	                18865, 15494
			},
	        {
	                18868, 15495
			}
	};
	
	private _250_WatchWhatYouEat()
	{
		super(250);
		
		addStartNpc(32743);
		addFirstTalkId(32743);
		addTalkId(32743);
		
		for (final int i[] : _mobs)
		{
			addKillId(i[0]);
		}
		
		questItemIds = new int[]
		{
		        15493, 15494, 15495
		};
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final var htmltext = event;
		final var st = player.getQuestState(getName());
		if (st == null || st.isCompleted())
		{
			return htmltext;
		}
		
		if (npc.getId() == 32743)
		{
			if (event.equalsIgnoreCase("32743-03.htm") && st.isCreated())
			{
				st.startQuest();
			}
			else if (event.equalsIgnoreCase("32743-end.htm") && st.isCond(2))
			{
				st.calcExpAndSp(getId());
				st.calcReward(getId());
				st.exitQuest(false, true);
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
		
		if (npc.getId() == 32743)
		{
			switch(st.getState())
			{
				case State.CREATED :
					if (player.getLevel() >= getMinLvl(getId()))
					{
						htmltext = "32743-01.htm";
					}
					else
					{
						htmltext = "32743-00.htm";
					}
					break;
				case State.STARTED :
					if (st.isCond(1))
					{
						htmltext = "32743-04.htm";
					}
					else if (st.isCond(2))
					{
						if(st.hasQuestItems(_mobs[0][1]) && st.hasQuestItems(_mobs[1][1]) && st.hasQuestItems(_mobs[2][1]))
						{
							htmltext = "32743-05.htm";
							for(final int items[] : _mobs)
							{
								st.takeItems(items[1], -1);
							}
						}
						else
						{
							htmltext = "32743-06.htm";
						}
					}
					break;
				case State.COMPLETED :
					htmltext = "32743-23.html";
					break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isSummon)
	{
		final var partyMember = getRandomPartyMember(player, 1);
		if (partyMember == null)
		{
			return super.onKill(npc, player, isSummon);
		}
		
		final var st = partyMember.getQuestState(getName());
		if (st.isCond(1))
		{
			for(final int mob[] : _mobs)
			{
				if (npc.getId() == mob[0])
				{
					if (!st.hasQuestItems(mob[1]))
					{
						st.giveItems(mob[1], 1);
						st.playSound("ItemSound.quest_itemget");
					}
				}
			}
			
			if(st.hasQuestItems(_mobs[0][1]) && st.hasQuestItems(_mobs[1][1]) && st.hasQuestItems(_mobs[2][1]))
			{
				st.setCond(2, true);
			}
		}
		return super.onKill(npc, player, isSummon);
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		var st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}
		
		if (npc.getId() == 32743)
		{
			return "32743-20.html";
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new _250_WatchWhatYouEat();
	}
}
