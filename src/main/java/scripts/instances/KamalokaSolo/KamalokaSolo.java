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
package scripts.instances.KamalokaSolo;

import java.sql.Connection;
import java.sql.PreparedStatement;

import gameserver.Config;
import gameserver.ai.model.CtrlIntention;
import gameserver.data.parser.ReflectionParser;
import gameserver.database.DatabaseFactory;
import gameserver.instancemanager.ReflectionManager;
import gameserver.model.Location;
import gameserver.model.actor.Npc;
import gameserver.model.actor.Player;
import gameserver.model.actor.templates.reflection.ReflectionTemplate;
import gameserver.model.entity.Reflection;
import gameserver.model.stats.StatsSet;
import gameserver.model.strings.server.ServerMessage;
import gameserver.network.serverpackets.NpcHtmlMessage;
import scripts.instances.AbstractReflection;

/**
 * Rework by LordWinter 13.02.2020
 */
public class KamalokaSolo extends AbstractReflection
{
	public Location _rewardPosition;
	
	public KamalokaSolo()
	{
		super(46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56);
	}
	
	private synchronized void enterInstance(Player player, Npc npc, int reflectionId)
	{
		if (enterReflection(player, npc, reflectionId))
		{
			final var r = player.getReflection();
			if (isInReflection(r))
			{
				r.setParam("kanabions", 0);
				r.setParam("doplers", 0);
				r.setParam("voiders", 0);
				r.setParam("rewarded", 0);
				r.setParam("grade", 0);
				final long time = r.getParams().getLong("reflectTime") * 60000L;
				startQuestTimer("time", time, null, player);
			}
		}
	}
	
	@Override
	protected void onTeleportEnter(Player player, ReflectionTemplate template, Reflection r, boolean firstEntrance)
	{
		if (firstEntrance)
		{
			r.addAllowed(player);
			player.getAI().setIntention(CtrlIntention.IDLE);
			final var teleLoc = template.getTeleportCoord();
			player.teleToLocation(teleLoc, true, r);
			if (player.hasSummon())
			{
				player.getSummon().getAI().setIntention(CtrlIntention.IDLE);
				player.getSummon().teleToLocation(teleLoc, true, r);
			}
		}
		else
		{
			player.getAI().setIntention(CtrlIntention.IDLE);
			final var teleLoc = template.getTeleportCoord();
			player.teleToLocation(teleLoc, true, r);
			if (player.hasSummon())
			{
				player.getSummon().getAI().setIntention(CtrlIntention.IDLE);
				player.getSummon().teleToLocation(teleLoc, true, r);
			}
		}
	}
	
	public String onAdvEventTo(String event, Npc npc, Player player)
	{
		if (player == null)
		{
			return null;
		}
		
		final var st = player.getQuestState(getName());
		if (st == null)
		{
			return null;
		}
		
		Reflection ref = null;
		if (isInReflection(player.getReflection()))
		{
			ref = player.getReflection();
		}
		else
		{
			ref = ReflectionManager.getInstance().getPlayerReflection(player.getObjectId(), false);
		}
		
		if (ref == null)
		{
			return null;
		}
		
		if (event.equalsIgnoreCase("time"))
		{
			ref.setDuration(600000);
			ref.cleanupNpcs();
			
			addSpawn(32485, _rewardPosition, ref);
			
			final int count = ref.getParams().getInteger("kanabions", 0) * 10 + ref.getParams().getInteger("doplers", 0) * 20 + ref.getParams().getInteger("voiders", 0) * 50;
			final int grade = getRank(count, ref.getParams());
			ref.setParam("grade", grade);
			if (grade > 0)
			{
				final var template = ReflectionParser.getInstance().getReflectionId(ref.getTemplateId());
				if (template != null)
				{
					try (Connection con = DatabaseFactory.getInstance().getConnection())
					{
						final int code = (template.getMinLevel() * 100) + template.getMaxLevel();
						final PreparedStatement statement = con.prepareStatement("INSERT INTO kamaloka_results (char_name,Level,Grade,Count) VALUES (?,?,?,?)");
						statement.setString(1, player.getName(null));
						statement.setInt(2, code);
						statement.setInt(3, grade);
						statement.setInt(4, count);
						statement.executeUpdate();
						statement.close();
					}
					catch (final Exception e)
					{
						_log.warn("Error while inserting Kamaloka data: " + e);
					}
				}
			}
		}
		else if (event.equalsIgnoreCase("Reward"))
		{
			final var rewarded = ref.getParams().getInteger("rewarded", 0);
			if (rewarded <= 0)
			{
				ref.setParam("rewarded", 1);
				final var rewards = getRewardList(ref.getParams().getInteger("grade", 0), ref.getParams());
				if (rewards != null)
				{
					for (final var item : rewards)
					{
						if (item != null)
						{
							final int id = item[0];
							final int count = item[1];
							if (id > 0 && count > 0)
							{
								st.giveItems(id, count);
							}
						}
					}
				}
				final var html = new NpcHtmlMessage(npc.getObjectId());
				html.setFile(player, player.getLang(), "data/html/scripts/instances/KamalokaSolo/" + "1.htm");
				html.replace("%kamaloka%", getName());
				player.sendPacket(html);
			}
		}
		else if (event.equalsIgnoreCase("Exit"))
		{
			ref.collapse();
		}
		return null;
	}
	
	private static int[][] getRewardList(int rank, StatsSet params)
	{
		if (params == null || params.isEmpty())
		{
			return null;
		}
		
		final String rewards = params.getString("rewardLvl" + rank);
		if (rewards == null)
		{
			return null;
		}
		
		final String[] itemsList = rewards.split(";");
		final int rewardsCount = itemsList.length;
		final int[][] result = new int[rewardsCount][];
		for (int i = 0; i < rewardsCount; i++)
		{
			final String[] item = itemsList[i].split("-");
			if (item.length != 2)
			{
				continue;
			}
			final int[] it = new int[2];
			it[0] = Integer.parseInt(item[0]);
			it[1] = Integer.parseInt(item[1]);
			result[i] = it;
		}
		return result;
	}
	
	private static int getRank(int total, StatsSet params)
	{
		int rank = 0;
		if (params == null || params.isEmpty())
		{
			return rank;
		}
		
		for (int i = 1; i <= 5; i++)
		{
			final int points = params.getInteger("rankLvl" + i, 0);
			if (rank < i && total >= points)
			{
				rank = i;
			}
		}
		return rank;
	}
	
	public String onEnterTo(Npc npc, Player player, int reflectionId)
	{
		var st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}
		
		if (Config.ALT_KAMALOKA_SOLO_PREMIUM_ONLY && !player.hasPremiumBonus())
		{
			if (npc != null)
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
				html.setFile(player, player.getLang(), "data/html/scripts/instances/KamalokaSolo/32484-no.htm");
				player.sendPacket(html);
			}
			else
			{
				player.sendMessage((new ServerMessage("ServiceBBS.ONLY_FOR_PREMIUM", player.getLang())).toString());
			}
			return null;
		}
		enterInstance(player, npc, reflectionId);
		return null;
	}
	
	public String onTalkTo(Npc npc, Player player)
	{
		var st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}
		
		final var r = player.getReflection();
		if (isInReflection(r))
		{
			if (npc.getId() == 32485)
			{
				String msgReward = "0.htm";
				final var rewarded = r.getParams().getInteger("rewarded", 0);
				if (rewarded <= 0)
				{
					switch (r.getParams().getInteger("grade", 0))
					{
						case 1 :
							msgReward = "D.htm";
							break;
						case 2 :
							msgReward = "C.htm";
							break;
						case 3 :
							msgReward = "B.htm";
							break;
						case 4 :
							msgReward = "A.htm";
							break;
						case 5 :
							msgReward = "S.htm";
							break;
						default :
							msgReward = "1.htm";
							break;
					}
				}
				final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
				html.setFile(player, player.getLang(), "data/html/scripts/instances/KamalokaSolo/" + msgReward);
				html.replace("%kamaloka%", getName());
				player.sendPacket(html);
			}
		}
		return null;
	}
	
	public String onKillTo(Npc npc, Player player, boolean isPet, int KANABION, int[] APPEAR)
	{
		if (player == null)
		{
			return null;
		}

		final var r = npc.getReflection();
		if (isInReflection(r))
		{
			final int npcId = npc.getId();
			if (npcId == KANABION)
			{
				final int kanabions = r.getParams().getInteger("kanabions", 0) + 1;
				r.setParam("kanabions", kanabions);
			}
			else if (npcId == APPEAR[0])
			{
				final int doplers = r.getParams().getInteger("doplers", 0) + 1;
				r.setParam("doplers", doplers);
			}
			else if (npcId == APPEAR[1])
			{
				final int voiders = r.getParams().getInteger("voiders", 0) + 1;
				r.setParam("voiders", voiders);
			}
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new KamalokaSolo();
	}
}
