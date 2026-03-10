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
package gameserver.model.actor.instance;

import java.util.List;

import gameserver.Config;
import gameserver.data.parser.SkillTreesParser;
import gameserver.data.parser.SkillsParser;
import gameserver.instancemanager.games.FishingChampionship;
import gameserver.model.SkillLearn;
import gameserver.model.actor.Player;
import gameserver.model.actor.templates.npc.NpcTemplate;
import gameserver.model.base.AcquireSkillType;
import gameserver.model.skills.Skill;
import gameserver.model.strings.server.ServerStorage;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.AcquireSkillDone;
import gameserver.network.serverpackets.ExAcquirableSkillListByClass;
import gameserver.network.serverpackets.NpcHtmlMessage;
import gameserver.network.serverpackets.SystemMessage;
import gameserver.utils.TimeUtils;
import gameserver.utils.Util;

public final class FishermanInstance extends MerchantInstance
{
	public FishermanInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		
		setInstanceType(InstanceType.FishermanInstance);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		
		if (val == 0)
		{
			pom = "" + npcId;
		}
		else
		{
			pom = npcId + "-" + val;
		}
		
		return "data/html/fisherman/" + pom + ".htm";
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if (command.equalsIgnoreCase("FishSkillList"))
		{
			showFishSkillList(player);
		}
		else if (command.startsWith("FishingChampionship"))
		{
			showChampScreen(player);
		}
		else if (command.startsWith("FishingReward"))
		{
			FishingChampionship.getInstance().getReward(player);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	public static void showFishSkillList(Player player)
	{
		final List<SkillLearn> skills = SkillTreesParser.getInstance().getAvailableFishingSkills(player);
		final ExAcquirableSkillListByClass asl = new ExAcquirableSkillListByClass(AcquireSkillType.FISHING);
		
		int count = 0;
		
		for (final SkillLearn s : skills)
		{
			final Skill sk = SkillsParser.getInstance().getInfo(s.getId(), s.getLvl());
			
			if (sk == null)
			{
				continue;
			}
			count++;
			asl.addSkill(s.getId(), s.getGetLevel(), s.getLvl(), s.getLvl(), s.getLevelUpSp(), 1);
		}
		
		if (count == 0)
		{
			final int minlLevel = SkillTreesParser.getInstance().getMinLevelForNewSkill(player, SkillTreesParser.getInstance().getFishingSkillTree());
			
			if (minlLevel > 0)
			{
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN_S1);
				sm.addNumber(minlLevel);
				player.sendPacket(sm);
			}
			else
			{
				player.sendPacket(SystemMessageId.NO_MORE_SKILLS_TO_LEARN);
			}
			player.sendPacket(AcquireSkillDone.STATIC);
		}
		else
		{
			player.sendPacket(asl);
		}
	}
	
	public void showChampScreen(Player player)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		
		String str = "<html><head><title>" + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.ROYAL_TOURNAMENT") + "</title></head>";
		str = str + "" + ServerStorage.getInstance().getString(player.getLang(), "L2FishermanInstance.GUILD_OF_FISHERS") + ":<br><br>";
		str = str + "" + ServerStorage.getInstance().getString(player.getLang(), "L2FishermanInstance.HELLO") + "<br>";
		str = str + "" + ServerStorage.getInstance().getString(player.getLang(), "L2FishermanInstance.YOUR_NAME_IN_LIST") + "<br>";
		str = str + "" + ServerStorage.getInstance().getString(player.getLang(), "L2FishermanInstance.REMEMBER") + "<br>";
		str = str + "" + ServerStorage.getInstance().getString(player.getLang(), "L2FishermanInstance.BE_NOT_UPSET") + "<br>";
		str = str + "" + ServerStorage.getInstance().getString(player.getLang(), "L2FishermanInstance.MESSAGE") + " " + TimeUtils.formatTime(player, FishingChampionship.getInstance().getTimeRemaining(), false) + "<br>";
		str = str + "<center><a action=\"bypass -h npc_%objectId%_FishingReward\">" + ServerStorage.getInstance().getString(player.getLang(), "L2FishermanInstance.WIN_PRIZE") + "</a><br></center>";
		str = str + "<table width=280 border=0 bgcolor=\"000000\"><tr><td width=70 align=center>" + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.PLACES") + "</td><td width=110 align=center>" + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.FISHERMAN") + "</td><td width=80 align=center>" + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.LENGTH") + "</td></tr></table><table width=280>";
		for (int x = 1; x <= 5; x++)
		{
			str = str + "<tr><td width=70 align=center>" + x + " " + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.PLACES") + ":</td>";
			str = str + "<td width=110 align=center>" + FishingChampionship.getInstance().getWinnerName(player, x) + "</td>";
			str = str + "<td width=80 align=center>" + FishingChampionship.getInstance().getFishLength(x) + "</td></tr>";
		}
		str = str + "<td width=80 align=center>0</td></tr></table><br>";
		str = str + "" + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.PRIZES_LIST") + "<br><table width=280 border=0 bgcolor=\"000000\"><tr><td width=70 align=center>" + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.PLACES") + "</td><td width=110 align=center>" + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.PRIZE") + "</td><td width=80 align=center>" + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.AMOUNT") + "</td></tr></table><table width=280>";
		for (int i = 1; i <= 5; i++)
		{
			final var rewards = Config.FISHING_REWARDS.get(i);
			if (rewards != null)
			{
				int itemId = 0;
				long amount = 0;
				final String[] price = rewards.split(":");
				if (price != null && price.length == 2)
				{
					itemId = Integer.parseInt(price[0]);
					amount = Long.parseLong(price[1]);
				}
				
				if (itemId != 0)
				{
					str = str + "<tr><td width=70 align=center>" + i + " " + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.PLACES") + ":</td><td width=110 align=center>" + Util.getItemName(player, itemId) + "</td><td width=80 align=center>" + amount + "</td></tr>";
				}
			}
		}
		str = str + "</table></body></html>";
		html.setHtml(player, str);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}
}