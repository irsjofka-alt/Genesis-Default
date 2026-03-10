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

import java.util.StringTokenizer;

import gameserver.Config;
import gameserver.SevenSigns;
import gameserver.model.Clan;
import gameserver.model.PcCondOverride;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.actor.instance.MerchantInstance;
import gameserver.model.entity.Castle;
import gameserver.network.serverpackets.NpcHtmlMessage;
import scripts.ai.AbstractNpcAI;

public class CastleMercenaryManager extends AbstractNpcAI
{
	private static final int[] NPCS =
	{
	        35102, 35144, 35186, 35228, 35276, 35318, 35365, 35511, 35557,
	};
	
	private CastleMercenaryManager()
	{
		addStartNpc(NPCS);
		addTalkId(NPCS);
		addFirstTalkId(NPCS);
	}
	
	private boolean hasRights(Player player, Npc npc)
	{
		return player.canOverrideCond(PcCondOverride.CASTLE_CONDITIONS) || ((player.getClanId() == npc.getCastle().getOwnerId()) && ((player.getClanPrivileges() & Clan.CP_CS_MERCENARIES) == Clan.CP_CS_MERCENARIES));
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = null;
		final StringTokenizer st = new StringTokenizer(event, " ");
		switch (st.nextToken())
		{
			case "limit" :
			{
				final Castle castle = npc.getCastle();
				final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
				if (castle.getId() == 5)
				{
					html.setFile(player, player.getLang(), "data/html/scripts/custom/CastleMercenaryManager/mercmanager-aden-limit.htm");
				}
				else if (castle.getId() == 8)
				{
					html.setFile(player, player.getLang(), "data/html/scripts/custom/CastleMercenaryManager/mercmanager-rune-limit.htm");
				}
				else
				{
					html.setFile(player, player.getLang(), "data/html/scripts/custom/CastleMercenaryManager/mercmanager-limit.htm");
				}
				html.replace("%feud_name%", String.valueOf(1001000 + castle.getId()));
				player.sendPacket(html);
				break;
			}
			case "buy" :
			{
				if (Config.ALLOW_CHECK_SEVEN_SIGN_STATUS && SevenSigns.getInstance().isSealValidationPeriod())
				{
					htmltext = "mercmanager-ssq.htm";
				}
				else
				{
					final int listId = Integer.parseInt(npc.getId() + st.nextToken());
					((MerchantInstance) npc).showBuyWindow(player, listId, false);
				}
				break;
			}
			case "main" :
			{
				htmltext = onFirstTalk(npc, player);
				break;
			}
			case "mercmanager-01.htm" :
			{
				htmltext = event;
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		final String htmltext;
		if (hasRights(player, npc))
		{
			if (npc.getCastle().getSiege().getIsInProgress())
			{
				htmltext = "mercmanager-siege.htm";
			}
			else if (Config.ALLOW_CHECK_SEVEN_SIGN_STATUS && SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE) == SevenSigns.CABAL_DUSK)
			{
				htmltext = "mercmanager-dusk.htm";
			}
			else if (Config.ALLOW_CHECK_SEVEN_SIGN_STATUS && SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE) == SevenSigns.CABAL_DAWN)
			{
				htmltext = "mercmanager-dawn.htm";
			}
			else
			{
				htmltext = "mercmanager.htm";
			}
		}
		else
		{
			htmltext = "mercmanager-no.htm";
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new CastleMercenaryManager();
	}
}
