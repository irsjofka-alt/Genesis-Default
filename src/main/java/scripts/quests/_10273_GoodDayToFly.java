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

import gameserver.data.parser.SkillsParser;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.quest.Quest;
import gameserver.model.quest.QuestState;
import gameserver.model.quest.State;

/**
 * Rework by LordWinter 16.12.2019
 */
public class _10273_GoodDayToFly extends Quest
{
	private _10273_GoodDayToFly()
	{
		super(10273);

		addStartNpc(32557);
		addTalkId(32557);
		
		addKillId(22614, 22615);

		questItemIds = new int[]
		{
		        13856
		};
	}

	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final String htmltext = event;
		final QuestState st = player.getQuestState(getName());
		if (st == null || st.isCompleted())
		{
			return htmltext;
		}

		switch (event)
		{
			case "32557-06.htm":
				if (st.isCreated())
				{
					st.startQuest();
				}
				break;
			case "32557-09.htm":
				st.set("transform", "1");
				SkillsParser.getInstance().getInfo(5982, 1).getEffects(player, player, false, true);
				break;
			case "32557-10.htm":
				st.set("transform", "2");
				SkillsParser.getInstance().getInfo(5983, 1).getEffects(player, player, false, true);
				break;
			case "32557-13.htm":
				if (st.getInt("transform") == 1)
				{
					SkillsParser.getInstance().getInfo(5982, 1).getEffects(player, player, false, true);
				}
				else if (st.getInt("transform") == 2)
				{
					SkillsParser.getInstance().getInfo(5983, 1).getEffects(player, player, false, true);
				}
				break;
		}
		return htmltext;
	}

	@Override
	public String onTalk(Npc npc, Player player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}

		final int transform = st.getInt("transform");
		switch (st.getState())
		{
			case State.COMPLETED:
				htmltext = "32557-0a.htm";
				break;
			case State.CREATED:
				htmltext = player.getLevel() < getMinLvl(getId()) ? "32557-00.htm" : "32557-01.htm";
				break;
			default:
				if (st.getQuestItemsCount(13856) >= 5)
				{
					htmltext = "32557-14.htm";
					st.calcExpAndSp(getId());
					if (transform == 1)
					{
						st.calcReward(getId(), 1);
					}
					else if (transform == 2)
					{
						st.calcReward(getId(), 2);
					}
					st.exitQuest(false, true);
				}
				else if (transform == 0)
				{
					htmltext = "32557-07.htm";
				}
				else
				{
					htmltext = "32557-11.htm";
				}
				break;
		}
		return htmltext;
	}

	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final Player partyMember = getRandomPartyMember(killer, 1);
		if (partyMember == null)
		{
			return super.onKill(npc, killer, isSummon);
		}
		final QuestState st = partyMember.getQuestState(getName());
		if (st != null && st.calcDropItems(getId(), 13856, npc.getId(), 5))
		{
			st.setCond(2);
		}
		return super.onKill(npc, killer, isSummon);
	}

	public static void main(String[] args)
	{
		new _10273_GoodDayToFly();
	}
}