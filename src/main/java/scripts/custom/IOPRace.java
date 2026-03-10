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
package scripts.custom;

import gameserver.data.parser.SkillsParser;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.quest.Quest;
import gameserver.model.quest.QuestState;
import gameserver.model.skills.Skill;

public class IOPRace extends Quest
{
	final private static int RIGNOS = 32349;
	final private static int STAMP = 10013;
	final private static int KEY = 9694;

	private int _player = -1;

	public IOPRace()
	{
		super(-1);
		addStartNpc(RIGNOS);
		addTalkId(RIGNOS);
		addFirstTalkId(RIGNOS);
	}

	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}

		if (player.getLevel() < 78)
		{
			return "32349-notavailable.htm";
		}
		else if ((_player != -1) && (_player == player.getObjectId()) && (st.getQuestItemsCount(STAMP) == 4))
		{
			return "32349-return.htm";
		}
		else if (_player != -1)
		{
			return "32349-notavailable.htm";
		}

		npc.showChatWindow(player);
		return null;
	}

	@Override
	public String onTalk(Npc npc, Player player)
	{
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}

		if (_player == -1)
		{
			// clean old data
			player.stopSkillEffects(5239, true);
			if (player.hasSummon())
			{
				player.getSummon().stopSkillEffects(5239, true);
			}

			st.takeItems(STAMP, -1);
			st.set("1st", "0");
			st.set("2nd", "0");
			st.set("3rd", "0");
			st.set("4th", "0");

			final Skill skill = SkillsParser.getInstance().getInfo(5239, 5);
			if (skill != null)
			{
				skill.getEffects(npc, player, false, true);
				if (player.hasSummon())
				{
					skill.getEffects(npc, player.getSummon(), false, true);
				}
			}

			startQuestTimer("timer", 1800000, null, null);
			_player = player.getObjectId();
		}

		return null;
	}

	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final String htmltext = "";

		if (event.equalsIgnoreCase("timer"))
		{
			_player = -1;
			return null;
		}
		else if (event.equalsIgnoreCase("finish"))
		{
			if (_player == player.getObjectId())
			{
				final QuestState st = player.getQuestState(getName());
				st.giveItems(KEY, 3);
				st.takeItems(STAMP, -1);
				st.exitQuest(true);
			}
		}

		return htmltext;
	}

	public static void main(String[] args)
	{
		new IOPRace();
	}
}
