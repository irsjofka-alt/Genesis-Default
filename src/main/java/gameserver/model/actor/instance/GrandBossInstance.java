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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gameserver.Config;
import gameserver.data.parser.ExperienceParser;
import gameserver.instancemanager.RaidBossSpawnManager;
import gameserver.model.Clan;
import gameserver.model.CommandChannel;
import gameserver.model.Party;
import gameserver.model.World;
import gameserver.model.actor.Creature;
import gameserver.model.actor.Player;
import gameserver.model.actor.templates.npc.DamageInfo;
import gameserver.model.actor.templates.npc.NpcTemplate;
import gameserver.model.actor.templates.npc.aggro.GroupInfo;
import gameserver.model.entity.Hero;
import gameserver.model.skills.Skill;
import gameserver.model.strings.server.ServerStorage;
import gameserver.network.SystemMessageId;
import gameserver.network.serverpackets.ExShowScreenMessage;
import gameserver.network.serverpackets.SystemMessage;
import gameserver.taskmanager.RaidBossTaskManager;
import gameserver.utils.comparators.DamageComparator;

public final class GrandBossInstance extends MonsterInstance
{
	private boolean _useRaidCurse = true;
	private Map<String, Integer> _damageInfo;
	private long _infoUpdateTime = 0L;
	private long _infoTotalTime = 0L;

	public GrandBossInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.GrandBossInstance);
		setIsRaid(true);
		setIsEpicRaid(true);
		setIsGlobalAI(true);
	}
	
	@Override
	protected int getMaintenanceInterval()
	{
		return 10000;
	}
	
	@Override
	public void onSpawn()
	{
		setIsNoRndWalk(true);
		if (_damageInfo == null)
		{
			_damageInfo = new HashMap<>();
		}
		super.onSpawn();
	}
	
	@Override
	public void reduceCurrentHp(double damage, Creature attacker, boolean awake, boolean isDOT, Skill skill)
	{
		if (Config.ALLOW_DAMAGE_INFO)
		{
			if (attacker != null && damage > 0)
			{
				final var player = attacker.getActingPlayer();
				if (player != null && player.getClan() != null)
				{
					if (_infoTotalTime != 0 && (_infoTotalTime + (Config.DAMAGE_INFO_LIMIT_TIME * 3600000) < System.currentTimeMillis()))
					{
						_infoTotalTime = 0;
						_infoUpdateTime = 0;
						_damageInfo.clear();
					}
					checkInfoDamage(player.getClan(), damage);
				}
			}
		}
		super.reduceCurrentHp(damage, attacker, awake, isDOT, skill);
	}
	
	@Override
	public int getKilledInterval(MonsterInstance minion)
	{
		final int respawnTime = Config.MINIONS_RESPAWN_TIME.containsKey(minion.getId()) ? Config.MINIONS_RESPAWN_TIME.get(minion.getId()) * 1000 : -1;
		return respawnTime < 0 ? minion.getLeader().isRaid() ? (int) Config.RAID_MINION_RESPAWN_TIMER : 0 : respawnTime;
	}
	
	@Override
	public void notifyMinionDied(MonsterInstance minion)
	{
		final int respawnTime = getKilledInterval(minion);
		if (respawnTime > 0)
		{
			RaidBossTaskManager.getInstance().addToMinionList(minion, (System.currentTimeMillis() + respawnTime));
		}
		super.notifyMinionDied(minion);
	}
	
	@Override
	protected void onDeath(Creature killer)
	{
		final int points = getTemplate().getRewardRp();
		if (points > 0)
		{
			calcRaidPointsReward(points);
		}
		super.onDeath(killer);
		
		RaidBossTaskManager.getInstance().removeMinions(this);
		
		if (Config.ALLOW_DAMAGE_INFO)
		{
			_infoTotalTime = 0;
			_infoUpdateTime = 0;
			_damageInfo = null;
		}
		
		if (killer != null && killer.isPlayable())
		{
			final Player player = killer.getActingPlayer();
			if (player != null)
			{
				broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.RAID_WAS_SUCCESSFUL));
				if (player.getParty() != null)
				{
					for (final Player member : player.getParty().getMembers())
					{
						if (member.isNoble() && getDistance(member) <= Config.ALT_PARTY_RANGE)
						{
							Hero.getInstance().setRBkilled(member.getObjectId(), getId());
						}
					}
				}
				else
				{
					if (player.isNoble())
					{
						Hero.getInstance().setRBkilled(player.getObjectId(), getId());
					}
				}
				player.getCounters().addAchivementInfo("epicKiller", getId(), -1, false, true, false);
			}
		}
	}
	
	private void calcRaidPointsReward(int totalPoints)
	{
		final Map<Object, GroupInfo> groupsInfo = new HashMap<>();
		final double totalHp = getMaxHp();
		
		final var charMap = getAggroList().getCharMap();
		for (final var player : charMap.keySet())
		{
			if (player == null)
			{
				continue;
			}
			final var key = player.getParty() != null ? player.getParty().getCommandChannel() != null ? player.getParty().getCommandChannel() : player.getParty() : player.getActingPlayer();
			var info = groupsInfo.get(key);
			if (info == null)
			{
				info = new GroupInfo();
				groupsInfo.put(key, info);
			}
			
			if (key instanceof CommandChannel)
			{
				for (final var p : ((CommandChannel) key))
				{
					if (p.isInRangeZ(this, Config.ALT_PARTY_RANGE2))
					{
						info.getPlayer().add(p);
					}
				}
			}
			else if (key instanceof Party)
			{
				for (final var p : ((Party) key).getMembers())
				{
					if (p.isInRangeZ(this, Config.ALT_PARTY_RANGE2))
					{
						info.getPlayer().add(p);
					}
				}
			}
			else
			{
				info.getPlayer().add(player);
			}
			info.addReward(charMap.get(player).getDamage());
		}
		
		for (final var groupInfo : groupsInfo.values())
		{
			final var players = groupInfo.getPlayer();
			final int perPlayer = (int) Math.round(totalPoints * groupInfo.getReward() / (totalHp * players.size()));
			for (final var player : players)
			{
				if (player != null)
				{
					int playerReward = perPlayer;
					playerReward = (int) Math.round(playerReward * ExperienceParser.getInstance().penaltyModifier(calculateLevelDiffForDrop(player.getLevel()), 9));
					if (playerReward == 0)
					{
						continue;
					}
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1_RAID_POINTS).addNumber(playerReward));
					RaidBossSpawnManager.getInstance().addPoints(player, getId(), playerReward);
				}
			}
		}
	}
	
	@Override
	public void onDecay()
	{
		super.onDecay();
		RaidBossSpawnManager.getInstance().calculateRanking();
	}

	@Override
	public double getVitalityPoints(long damage)
	{
		return -super.getVitalityPoints(damage) / 100 + Config.VITALITY_RAID_BONUS;
	}

	@Override
	public boolean useVitalityRate()
	{
		return false;
	}

	public void setUseRaidCurse(boolean val)
	{
		_useRaidCurse = val;
	}

	@Override
	public boolean giveRaidCurse()
	{
		return _useRaidCurse;
	}
	
	private void checkInfoDamage(Clan clan, double damage)
	{
		if (_damageInfo == null)
		{
			return;
		}
		
		if (_infoUpdateTime == 0)
		{
			_infoUpdateTime = System.currentTimeMillis();
			_infoTotalTime = System.currentTimeMillis();
		}
		
		if (_damageInfo.containsKey(clan.getName()))
		{
			final double totalDamage = _damageInfo.get(clan.getName());
			_damageInfo.put(clan.getName(), (int) (totalDamage + damage));
		}
		else
		{
			_damageInfo.put(clan.getName(), (int) damage);
		}
		
		if ((_infoUpdateTime + (Config.DAMAGE_INFO_UPDATE * 1000L)) < System.currentTimeMillis())
		{
			_infoUpdateTime = System.currentTimeMillis();
			
			if (_damageInfo != null)
			{
				final List<DamageInfo> damageList = new ArrayList<>();
				
				final StringBuilder builderEn = new StringBuilder();
				final StringBuilder builderRu = new StringBuilder();
				for (final String clanName : _damageInfo.keySet())
				{
					damageList.add(new DamageInfo(clanName, _damageInfo.get(clanName)));
				}
				
				Collections.sort(damageList, DamageComparator.getInstance());
				
				for (final var info : damageList)
				{
					if (info != null)
					{
						builderEn.append("" + ServerStorage.getInstance().getString("en", "EpicDamageInfo.CLAN") + "").append(' ').append(info.getClanName()).append(": ").append(getDamageFormat(info.getDamage(), "en")).append('\n');
						builderRu.append("" + ServerStorage.getInstance().getString("ru", "EpicDamageInfo.CLAN") + "").append(' ').append(info.getClanName()).append(": ").append(getDamageFormat(info.getDamage(), "ru")).append('\n');
					}
				}
				
				final var msgEn = new ExShowScreenMessage(builderEn.toString(), (Config.DAMAGE_INFO_UPDATE * 1000), ExShowScreenMessage.TOP_LEFT, false);
				final var msgRu = new ExShowScreenMessage(builderRu.toString(), (Config.DAMAGE_INFO_UPDATE * 1000), ExShowScreenMessage.TOP_LEFT, false);
				var players = World.getAroundPlayers(this, 2000, 200);
				for (final var player : players)
				{
					player.sendPacket(player.getLang().equalsIgnoreCase("ru") ? msgRu : msgEn);
				}
				players = null;
			}
		}
	}
	
	private static String getDamageFormat(int damage, String lang)
	{
		final String scount = Integer.toString(damage);
		if (damage < 1000)
		{
			return scount;
		}
		if ((damage > 999) && (damage < 1000000))
		{
			return scount.substring(0, scount.length() - 3) + "" + ServerStorage.getInstance().getString(lang, "EpicDamageInfo.K");
		}
		if ((damage > 999999) && (damage < 1000000000))
		{
			return scount.substring(0, scount.length() - 6) + "" + ServerStorage.getInstance().getString(lang, "EpicDamageInfo.KK");
		}
		if (damage > 999999999)
		{
			return scount.substring(0, scount.length() - 9) + "" + ServerStorage.getInstance().getString(lang, "EpicDamageInfo.KKK");
		}
		return "0";
	}
}