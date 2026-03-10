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

import gameserver.Config;
import gameserver.model.Clan;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.SystemMessage;
import scripts.ai.AbstractNpcAI;

public class ClanTrader extends AbstractNpcAI
{
	private static final int[] CLAN_TRADER =
	{
		32024,
		32025
	};

	private static final int BLOOD_ALLIANCE = 9911;
	private static final int BLOOD_ALLIANCE_COUNT = 1;
	private static final int BLOOD_OATH = 9910;
	private static final int BLOOD_OATH_COUNT = 10;
	private static final int KNIGHTS_EPAULETTE = 9912;
	private static final int KNIGHTS_EPAULETTE_COUNT = 100;
	
	private ClanTrader()
	{
		addStartNpc(CLAN_TRADER);
		addTalkId(CLAN_TRADER);
		addFirstTalkId(CLAN_TRADER);
	}
	
	private String giveReputation(Npc npc, Player player, int count, int itemId, int itemCount)
	{
		if (getQuestItemsCount(player, itemId) >= itemCount)
		{
			takeItems(player, itemId, itemCount);
			player.getClan().addReputationScore(count, true);
			
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_ADDED_S1S_POINTS_TO_REPUTATION_SCORE);
			sm.addNumber(count);
			player.sendPacket(sm);
			return npc.getId() + "-04.htm";
		}
		return npc.getId() + "-03.htm";
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		return switch (event)
		{
			case "32024.htm":
			case "32024-02.htm":
			case "32025.htm":
			case "32025-02.htm": yield event;
			case "repinfo": yield (player.getClan().getLevel() > 4) ? npc.getId() + "-02.htm" : npc.getId() + "-05.htm";
			case "exchange-ba": yield giveReputation(npc, player, Config.BLOODALLIANCE_POINTS, BLOOD_ALLIANCE, BLOOD_ALLIANCE_COUNT);
			case "exchange-bo": yield giveReputation(npc, player, Config.BLOODOATH_POINTS, BLOOD_OATH, BLOOD_OATH_COUNT);
			case "exchange-ke": yield giveReputation(npc, player, Config.KNIGHTSEPAULETTE_POINTS, KNIGHTS_EPAULETTE, KNIGHTS_EPAULETTE_COUNT);
			default:
				yield null;
		};
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		if (player.isClanLeader() || ((player.getClanPrivileges() & Clan.CP_CL_TROOPS_FAME) == Clan.CP_CL_TROOPS_FAME))
		{
			return npc.getId() + ".htm";
		}
		return npc.getId() + "-01.htm";
	}
	
	public static void main(String[] args)
	{
		new ClanTrader();
	}
}