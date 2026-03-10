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

import gameserver.instancemanager.CHSiegeManager;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.entity.clanhall.SiegableHall;
import gameserver.model.quest.Quest;
import gameserver.model.quest.QuestState;
import gameserver.model.quest.State;

/**
 * Created by LordWinter 14.06.2013 Based on L2J Eternity-World
 */
public class _655_AGrandPlanForTamingWildBeasts extends Quest
{
	private static final int MESSENGER = 35627;

	private final static int STONE = 8084;
	private final static int TRAINER_LICENSE = 8293;

	private static final SiegableHall BEAST_STRONGHOLD = CHSiegeManager.getInstance().getSiegableHall(63);

	private _655_AGrandPlanForTamingWildBeasts()
	{
		super(655);

		addStartNpc(MESSENGER);
		addTalkId(MESSENGER);
	}

	@Override
	public final String onTalk(Npc npc, Player player)
	{
		String htmltext = getNoQuestMsg(player);
		
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}

		if (npc.getId() == MESSENGER)
		{
			switch (st.getState())
			{
				case State.CREATED:
					if (BEAST_STRONGHOLD.getSiege().getAttackers().size() >= 5)
					{
						htmltext = "35627-00.htm";
					}
					else
					{
						htmltext = "35627-01.htm";
						st.setState(State.STARTED);
						st.set("cond", "1");
						st.playSound("ItemSound.quest_accept");
					}
					break;
				case State.STARTED:
					if(st.getQuestItemsCount(STONE) < 10)
					{
						htmltext = "35627-02.htm";
					}
					else
					{
						st.takeItems(STONE, 10);
						st.giveItems(TRAINER_LICENSE, 1);
						st.exitQuest(true);
						htmltext = "35627-03.htm";
					}
					break;
			}
		}
		return htmltext;
	}

	public static void checkCrystalofPurity(Player player)
	{
		final QuestState st = player.getQuestState(_655_AGrandPlanForTamingWildBeasts.class.getSimpleName());
		if ((st != null) && st.isCond(1))
		{
			if (st.getQuestItemsCount(STONE) < 10)
			{
				st.giveItems(STONE, 1);
			}
		}
	}

	public static void main(String[] args)
	{
		new _655_AGrandPlanForTamingWildBeasts();
	}
}